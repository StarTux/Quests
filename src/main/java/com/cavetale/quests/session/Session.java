package com.cavetale.quests.session;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.QuestsPlugin;
import com.cavetale.quests.sql.SQLQuest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor @Getter
public final class Session {
    private final QuestsPlugin plugin;
    private final Player player;
    boolean disabled = false;
    boolean ready = false;
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
        plugin.getDatabase().getDb().find(SQLQuest.class)
            .eq("player", player.getUniqueId())
            .findListAsync(this::loadData2);
    }

    void loadData2(List<SQLQuest> rows) {
        if (disabled) return;
        for (SQLQuest row : rows) {
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
        // TODO: Testing code. Move somewhere else!
        if (getQuests(QuestCategory.DAILY).size() == 0) {
            com.cavetale.quests.AdminCommand.testDailies(this);
        }
    }

    void tick() {
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
}
