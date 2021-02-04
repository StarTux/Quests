package com.cavetale.quests.sql;

import com.cavetale.quests.QuestsPlugin;
import com.winthier.sql.SQLDatabase;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class Database {
    private final QuestsPlugin plugin;
    @Getter private SQLDatabase db;

    public void enable() {
        db = new SQLDatabase(plugin);
        db.registerTables(SQLQuest.class, SQLPlayer.class, SQLGlobalQuests.class);
        db.createAllTables();
    }

    public void disable() { }
}
