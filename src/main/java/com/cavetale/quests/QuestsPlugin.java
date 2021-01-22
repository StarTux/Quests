package com.cavetale.quests;

import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.gui.Gui;
import com.cavetale.quests.session.Sessions;
import com.cavetale.quests.sql.Database;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class QuestsPlugin extends JavaPlugin {
    @Getter protected static QuestsPlugin inst;
    protected QuestsCommand questsCommand = new QuestsCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    protected Database database = new Database(this);
    protected Sessions sessions = new Sessions(this);

    @Override
    public void onEnable() {
        inst = this;
        database.enable();
        questsCommand.enable();
        adminCommand.enable();
        for (GoalType goalType : GoalType.values()) {
            Listener listener = goalType.holder.getEventListener();
            if (listener != null) {
                Bukkit.getPluginManager().registerEvents(listener, this);
            }
        }
        sessions.enable();
        if (Bukkit.getPluginManager().isPluginEnabled("Sidebar")) {
            Bukkit.getPluginManager().registerEvents(new SidebarListener(this), this);
        }
        Gui.enable(this);
    }

    @Override
    public void onDisable() {
        Gui.disable();
        sessions.disable();
        database.disable();
    }
}
