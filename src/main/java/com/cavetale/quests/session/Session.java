package com.cavetale.quests.session;

import com.cavetale.quests.GlobalQuests;
import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.QuestsPlugin;
import com.cavetale.quests.Timer;
import com.cavetale.quests.sql.SQLPlayer;
import com.cavetale.quests.sql.SQLQuest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor @Getter
public final class Session {
    private final QuestsPlugin plugin;
    private final Player player;
    boolean disabled = false;
    boolean ready = false;
    SQLPlayer playerRow;
    final List<QuestInstance> quests = new ArrayList<>();
    final QuestBook questBook = new QuestBook(this);

    public void addNewQuest(Quest quest) {
        QuestInstance questInstance = new QuestInstance(this, quest);
        quests.add(questInstance);
        Collections.sort(quests);
        questInstance.insertIntoDatabase();
        questInstance.enable();
    }

    public boolean removeQuest(QuestInstance questInstance) {
        if (!quests.remove(questInstance)) return false;
        questInstance.disable();
        questInstance.removeFromDatabase();
        return true;
    }

    public int removeObsoleteQuests() {
        int count = 0;
        for (QuestInstance questInstance : new ArrayList<>(quests)) {
            if (questInstance.isExpired()) {
                removeQuest(questInstance);
                count += 1;
            }
        }
        return count;
    }

    Session enable() {
        loadData();
        return this;
    }

    void disable() {
        disabled = true;
        for (QuestInstance questInstance : quests) {
            questInstance.disable();
        }
        quests.clear();
    }

    void loadData() {
        UUID uuid = player.getUniqueId();
        plugin.getDatabase().getDb().scheduleAsyncTask(() -> {
                // Async thread
                plugin.getDatabase().getDb().insertIgnore(new SQLPlayer(uuid));
                playerRow = plugin.getDatabase().getDb().find(SQLPlayer.class)
                    .eq("uuid", uuid).findUnique();
                List<SQLQuest> questRows = plugin.getDatabase().getDb().find(SQLQuest.class)
                    .eq("player", player.getUniqueId()).findList();
                Bukkit.getScheduler().runTask(plugin, () -> {
                        // Main thread
                        if (disabled) return;
                        for (SQLQuest row : questRows) {
                            QuestInstance questInstance;
                            try {
                                questInstance = new QuestInstance(this, row);
                            } catch (RuntimeException re) {
                                // from Quest::deserialize
                                plugin.getLogger().log(Level.SEVERE, row.toString(), re);
                                continue;
                            }
                            quests.add(questInstance);
                        }
                        for (QuestInstance questInstance : quests) {
                            questInstance.enable();
                        }
                        ready = true;
                        update(plugin.getDailyGlobalQuests());
                        update(plugin.getWeeklyGlobalQuests());
                    });
            });
    }

    void tick() {
        if (!ready) return;
        for (QuestInstance questInstance : quests) {
            if (!questInstance.isReady()) continue;
            questInstance.tick();
        }
    }

    public List<QuestInstance> getQuests() {
        return new ArrayList<>(quests);
    }

    public List<QuestInstance> getQuests(QuestCategory category) {
        List<QuestInstance> list = new ArrayList<>();
        if (!ready) return list;
        for (QuestInstance questInstance : quests) {
            if (category == questInstance.getQuest().getCategory()) {
                list.add(questInstance);
            }
        }
        return list;
    }

    public List<QuestInstance> getVisibleQuests() {
        List<QuestInstance> list = new ArrayList<>();
        if (!ready) return list;
        for (QuestInstance questInstance : quests) {
            if (!questInstance.isReady()) continue;
            list.add(questInstance);
        }
        return list;
    }

    public QuestInstance findQuest(int id) {
        for (QuestInstance questInstance : quests) {
            if (!questInstance.isReady()) continue;
            if (questInstance.getRow().getId() == id) return questInstance;
        }
        return null;
    }

    private void log(String msg) {
        plugin.getLogger().info("[Session] [" + player.getName() + "] " + msg);
    }

    public void update(GlobalQuests globalQuests) {
        QuestCategory category = globalQuests.getCategory();
        log("update: " + category.humanName);
        if (globalQuests.getTimeId() != Timer.getTimeId(category)) return;
        if (!player.hasPermission("quests." + category.key)) return;
        int currentTimeId;
        int newTimeId = globalQuests.getTimeId();
        switch (category) {
        case DAILY:
            currentTimeId = playerRow.getDailyId();
            break;
        case WEEKLY:
            currentTimeId = playerRow.getWeeklyId();
            break;
        case MONTHLY:
            currentTimeId = playerRow.getMonthlyId();
            break;
        default:
            throw new IllegalStateException(category.name());
        }
        if (currentTimeId == newTimeId) return;
        String columnName = category.key + "_id";
        String sql = "UPDATE `" + plugin.getDatabase().getDb().getTable(SQLPlayer.class).getTableName() + "`"
            + " SET `" + columnName + "` = " + newTimeId
            + " WHERE `id` = " + playerRow.getId()
            + " AND `" + columnName + "` = " + currentTimeId;
        plugin.getDatabase().getDb().executeUpdateAsync(sql, count -> {
                if (count == 0) return;
                if (Timer.getTimeId(category) != newTimeId) return;
                switch (category) {
                case DAILY:
                    playerRow.setDailyId(newTimeId);
                    break;
                case WEEKLY:
                    playerRow.setWeeklyId(newTimeId);
                    break;
                case MONTHLY:
                    playerRow.setMonthlyId(newTimeId);
                    break;
                default:
                    throw new IllegalStateException(category.name());
                }
                for (Quest quest : globalQuests.getQuests()) {
                    addNewQuest(quest);
                }
            });
    }
}
