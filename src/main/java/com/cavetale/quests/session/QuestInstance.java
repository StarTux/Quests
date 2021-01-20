package com.cavetale.quests.session;

import com.cavetale.quests.Quest;
import com.cavetale.quests.goal.Goal;
import com.cavetale.quests.goal.Progress;
import com.cavetale.quests.sql.SQLQuest;
import com.destroystokyo.paper.Title;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * One quest currently available to one player, with current
 * progress. Possibly inactive, possibly accepted, possibly finished.
 * Bundled with the database row.
 *
 * Lifetime:
 * Created, enabled, ready, accepted, completed, claimed, deleted.
 */
@Getter @Setter
public final class QuestInstance implements Comparable<QuestInstance> {
    private final Session session;
    private final SQLQuest row;
    private final Quest quest;
    private Progress progress;
    private boolean enabled;
    private ProgressBar progressBar = new ProgressBar(this);

    public QuestInstance(final Session session, final SQLQuest row) {
        this.session = session;
        this.row = row;
        this.quest = Quest.deserialize(row.getQuest()); // throws
        this.progress = Progress.deserialize(row.getProgress(), quest);
    }

    /**
     * Constructor with a new quest.
     */
    public QuestInstance(final Session session, final Quest quest) {
        this.session = session;
        this.quest = quest;
        this.progress = quest.getGoals().get(0).getHolder().newProgress();
        this.row = new SQLQuest();
    }

    public Goal getCurrentGoal() {
        return quest.getGoals().get(progress.getGoalIndex());
    }

    @Override
    public int compareTo(QuestInstance other) {
        int result = Integer.compare(quest.getCategory().ordinal(), other.quest.getCategory().ordinal());
        return result != 0
            ? result
            : Integer.compare(quest.getIndex(), other.quest.getIndex());
    }

    /**
     * Called after an instance with a new Quest is created.
     */
    public boolean insertIntoDatabase() {
        if (row.getId() != null) return false;
        row.setPlayer(session.getPlayer().getUniqueId());
        row.store(quest, progress);
        row.setCreated(new Date());
        session.getPlugin().getDatabase().getDb().insertAsync(row, null);
        return true;
    }

    public boolean saveToDatabase() {
        if (row.getId() == null) return false;
        row.store(quest, progress);
        if (session.getPlugin().isEnabled()) {
            session.getPlugin().getDatabase().getDb().saveAsync(row, null);
        } else {
            session.getPlugin().getDatabase().getDb().save(row);
        }
        return true;
    }

    public boolean removeFromDatabase() {
        if (row.getId() == null) return false;
        session.getPlugin().getDatabase().getDb().deleteAsync(row, null);
        return true;
    }

    /**
     * Ready to be accepted and saved.
     */
    public boolean isReady() {
        return row.getId() != null && enabled;
    }

    /**
     * Return true if this quest is ready to make progress and save.
     */
    public boolean isActive() {
        return isReady() && progress.isAccepted() && !progress.isCompleted();
    }

    /**
     * Helper function for implementors.
     */
    public static List<QuestInstance> of(Player player, Class<? extends Goal> goalClass) {
        List<QuestInstance> result = new ArrayList<>();
        Session session = Sessions.getInst().of(player);
        if (!session.isReady()) return result;
        for (QuestInstance qi : session.getQuests()) {
            if (qi.isActive() && goalClass.isInstance(qi.getCurrentGoal())) {
                result.add(qi);
            }
        }
        return result;
    }

    /**
     * Increase the amount within Progress. If the amount within the
     * current Goal is reached, trigger the completion of the current
     * goal.
     */
    public void increaseAmount() {
        progress.increaseAmount();
        if (progress.getAmount() >= getCurrentGoal().getAmount()) {
            completeGoal();
        } else {
            session.getPlayer().playSound(session.getPlayer().getLocation(),
                                          Sound.ENTITY_ITEM_PICKUP,
                                          SoundCategory.MASTER,
                                          0.5f, 0.5f);
        }
        progressBar.showProgress();
    }

    /**
     * Move on to the next goal or mark the current quest as completed.
     */
    public void completeGoal() {
        int goalIndex = progress.getGoalIndex() + 1;
        if (goalIndex < quest.getGoals().size()) {
            // Next goal
            Goal newGoal = quest.getGoals().get(goalIndex);
            progress = newGoal.getHolder().newProgress();
            progress.setGoalIndex(goalIndex);
            progress.setAccepted(true);
            session.getPlayer().sendMessage("Goal complete!");
            progressBar.onNewGoal();
            session.getPlayer().playSound(session.getPlayer().getLocation(),
                                          Sound.ENTITY_PLAYER_LEVELUP,
                                          SoundCategory.MASTER,
                                          0.5f, 2.0f);
        } else {
            progress.setCompleted(true);
            session.getPlayer().sendTitle(new Title("", ChatColor.GOLD + "Quest complete"));
            session.getPlayer().playSound(session.getPlayer().getLocation(),
                                          Sound.UI_TOAST_CHALLENGE_COMPLETE,
                                          SoundCategory.MASTER,
                                          0.1f, 2.0f);
        }
        saveToDatabase();
    }

    void tick() {
        if (isActive()) {
            getCurrentGoal().onTick();
        }
        progressBar.tick();
    }

    /**
     * Called by Session.
     */
    void enable() {
        progressBar.enable();
        enabled = true;
    }

    /**
     * Called by Session.
     */
    void disable() {
        enabled = false;
        progressBar.disable();
    }

    /**
     * Called via command button. Maybe gui.
     */
    public boolean playerClaim() {
        if (row.isClaimed()) return false;
        String sql = "UPDATE `"
            + session.getPlugin().getDatabase().getDb().getTable(SQLQuest.class).getTableName()
            + "` SET `claimed` = 1"
            + " WHERE `id` = " + row.getId()
            + " AND `claimed` = 0";
        session.getPlugin().getDatabase().getDb().executeUpdateAsync(sql, this::playerClaimCallback);
        session.getPlugin().getLogger()
            .info("Claiming quest: " + session.getPlayer().getName() + ", " + row.getId());
        return true;
    }

    public void playerClaimCallback(int rowCount) {
        if (rowCount == 0) return;
        if (!enabled) {
            session.getPlugin().getLogger()
                .warning("Claiming quest interrupted: Session disabled: "
                         + session.getPlayer().getName() + ", " + rowCount);
            saveToDatabase();
            return;
        }
        row.setClaimed(true);
        quest.getReward().givePlayer(session.getPlayer(), quest);
    }

    /**
     * Player accepts this quest.
     */
    public boolean playerAccept() {
        if (progress.isAccepted()) return false;
        progress.setAccepted(true);
        saveToDatabase();
        progressBar.showProgress();
        session.getPlayer().playSound(session.getPlayer().getLocation(),
                                      Sound.ENTITY_PLAYER_LEVELUP,
                                      SoundCategory.MASTER,
                                      0.2f, 2.0f);
        session.getPlayer().sendMessage("Quest accepted: " + ChatColor.GOLD + quest.getTitle());
        return true;
    }
}
