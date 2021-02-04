package com.cavetale.quests;

import com.cavetale.quests.sql.SQLGlobalQuests;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

/**
 * A GlobalQuest instance holds a cache of quests for a certain
 * category for the current time period. It gets notified by the
 * plugin (onEnable) or the Timer when it should update its
 * cache. Updating the cache means entails attempt to load an existing
 * row from the database table (global_quests). If it doesn't exist, a
 * new one unique one is created. It is then populated with new
 * quests. If at any step there is a failure indicating that a
 * different thread or server is already working on this, we quit and
 * trigger another update a few seconds from now.
 *
 * This enables daily, weekly and monthly quests which are the same
 * for all players, across all servers.
 */
@Getter @RequiredArgsConstructor
public final class GlobalQuests {
    private final QuestsPlugin plugin;
    private final QuestCategory category;
    private int timeId;
    private List<Quest> quests;

    public void enable() {
        update();
    }

    private void log(String msg) {
        plugin.getLogger().info("[Global] [" + category.humanName + "] " + msg);
    }

    public void update() {
        log("update()");
        final int newTimeId = Timer.getTimeId(category);
        if (timeId == newTimeId) {
            log("update(): timeId == newTimeId");
            return;
        }
        plugin.getDatabase().getDb().find(SQLGlobalQuests.class)
            .eq("category", category.key)
            .eq("time_id", newTimeId)
            .findUniqueAsync(row -> {
                    if (newTimeId != Timer.getTimeId(category)) {
                        log("find row: time id changed");
                        return;
                    }
                    if (row != null) {
                        if (row.getJson() == null) {
                            // Another server is already generating. Just wait.
                            log("find row: json is null");
                            Bukkit.getScheduler().runTaskLater(plugin, this::update, 200L);
                            return;
                        }
                        String[] lines = row.getJson().split("\n");
                        List<Quest> newQuests = new ArrayList<>();
                        for (String line : lines) {
                            Quest quest = Quest.deserialize(line);
                            if (quest == null) {
                                log("Parsing global quest: " + category + ": " + line);
                                continue;
                            }
                            newQuests.add(quest);
                        }
                        log("fetched " + newQuests.size() + " new quests");
                        quests = newQuests;
                        timeId = newTimeId;
                        plugin.sessions.updateAll(this);
                    } else {
                        SQLGlobalQuests newRow;
                        newRow = new SQLGlobalQuests(category.key, newTimeId);
                        plugin.getDatabase().getDb().insertIgnoreAsync(newRow, count -> {
                                if (newTimeId != Timer.getTimeId(category)) {
                                    log("create new: time id changed");
                                    return;
                                }
                                if (count == 0) {
                                    // Another server is already generating. Just wait.
                                    log("create new: row already exists");
                                    Bukkit.getScheduler().runTaskLater(plugin, this::update, 200L);
                                    return;
                                }
                                List<Quest> newQuests = plugin.generateQuests(category);
                                newRow.setJson(newQuests.stream().map(Quest::serialize).collect(Collectors.joining("\n")));
                                plugin.getDatabase().getDb().updateAsync(newRow, unused -> {
                                        if (newTimeId != Timer.getTimeId(category)) {
                                            log("created new: time id changed");
                                            return;
                                        }
                                        log("created " + newQuests.size() + " new quests");
                                        quests = newQuests;
                                        timeId = newTimeId;
                                        plugin.sessions.updateAll(this);
                                    }, "json");
                            });
                    }
                });
    }
}
