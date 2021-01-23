package com.cavetale.quests.session;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestState;
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
    private final QuestState state;
    private boolean enabled;
    private ProgressBar progressBar = new ProgressBar(this);

    /**
     * Constructor with an existing sql row.
     */
    public QuestInstance(final Session session, final SQLQuest row) {
        this.session = session;
        this.row = row;
        this.quest = Quest.deserialize(row.getQuest()); // throws
        this.state = QuestState.deserialize(row.getState(), quest);
    }

    /**
     * Constructor with a new quest.
     */
    public QuestInstance(final Session session, final Quest quest) {
        this.session = session;
        this.quest = quest;
        this.state = new QuestState();
        this.state.prepare(quest);
        this.row = new SQLQuest();
    }

    public Goal getCurrentGoal() {
        int index = state.getTag().getCurrentGoal();
        return quest.getGoals().get(index);
    }

    public Progress getCurrentProgress() {
        int index = state.getTag().getCurrentGoal();
        return state.getGoals().get(index);
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
        row.store(quest);
        row.store(state);
        row.setCreated(new Date());
        session.getPlugin().getDatabase().getDb().insertAsync(row, null);
        return true;
    }

    public boolean saveStateToDatabase() {
        if (row.getId() == null) return false;
        row.store(state);
        if (session.getPlugin().isEnabled()) {
            session.getPlugin().getDatabase().getDb().updateAsync(row, null, "state");
        } else {
            session.getPlugin().getDatabase().getDb().update(row, "state");
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
     * Return true if this quest is ready to make state and save.
     */
    public boolean isActive() {
        return isReady() && row.isAccepted() && !row.isComplete();
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
     * Increase the amount within state. If the amount within the
     * current Goal is reached, trigger the completion of the current
     * goal.
     */
    public void increaseAmount() {
        Progress progress = getCurrentProgress();
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
        saveStateToDatabase();
    }

    /**
     * Move on to the next goal or mark the current quest as completed.
     */
    public void completeGoal() {
        Progress progress = getCurrentProgress();
        int newGoalIndex = state.getTag().getCurrentGoal() + 1;
        if (newGoalIndex < quest.getGoals().size()) {
            // Next goal
            state.getTag().setCurrentGoal(newGoalIndex);
            session.getPlayer().sendMessage("Goal complete!");
            progressBar.onNewGoal();
            session.getPlayer().playSound(session.getPlayer().getLocation(),
                                          Sound.ENTITY_PLAYER_LEVELUP,
                                          SoundCategory.MASTER,
                                          0.5f, 2.0f);
        } else {
            setComplete();
            session.getPlayer().sendTitle(new Title("", ChatColor.GOLD + "Quest complete"));
            session.getPlayer().playSound(session.getPlayer().getLocation(),
                                          Sound.UI_TOAST_CHALLENGE_COMPLETE,
                                          SoundCategory.MASTER,
                                          0.1f, 2.0f);
        }
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

    public boolean setSeen(boolean seen) {
        if (row.isSeen() == seen) return false;
        row.setSeen(seen);
        session.getPlugin().getDatabase().getDb().updateAsync(row, null, "seen");
        return true;
    }

    public boolean setFocus(boolean focus) {
        if (row.isFocus() == focus) return false;
        row.setFocus(focus);
        session.getPlugin().getDatabase().getDb().updateAsync(row, null, "focus");
        return true;
    }

    /**
     * Update the accepted status of the sql row.
     */
    public boolean setAccepted() {
        if (row.isAccepted()) return false;
        row.setAccepted(true);
        session.getPlugin().getDatabase().getDb().saveAsync(row, null, "accepted");
        return true;
    }

    /**
     * Update the completion status of the sql row.
     */
    public boolean setComplete() {
        if (row.isComplete()) return false;
        row.setComplete(true);
        session.getPlugin().getDatabase().getDb().saveAsync(row, null, "complete");
        return true;
    }

    /**
     * Called via command button. Maybe gui.
     */
    public boolean playerClaim() {
        if (row.isClaimed()) return false;
        row.setClaimed(true);
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
            row.setClaimed(false);
            session.getPlugin().getDatabase().getDb().update(row, null, "claimed");
            return;
        }
        quest.getReward().givePlayer(session.getPlayer(), quest);
    }

    /**
     * Player accepts this quest.
     */
    public boolean playerAccept() {
        if (!setAccepted()) return false;
        progressBar.showProgress();
        session.getPlayer().playSound(session.getPlayer().getLocation(),
                                      Sound.ENTITY_PLAYER_LEVELUP,
                                      SoundCategory.MASTER,
                                      0.2f, 2.0f);
        session.getPlayer().sendMessage("Quest accepted: " + ChatColor.GOLD + quest.getTitle());
        return true;
    }
}
