package com.cavetale.quests;

import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.goal.RegularGoalHolder;
import com.cavetale.quests.gui.Gui;
import com.cavetale.quests.session.Sessions;
import com.cavetale.quests.sql.Database;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class QuestsPlugin extends JavaPlugin {
    @Getter protected static QuestsPlugin inst;
    protected QuestsCommand questsCommand = new QuestsCommand(this);
    protected AdminCommand adminCommand = new AdminCommand(this);
    protected Database database = new Database(this);
    protected Sessions sessions = new Sessions(this);
    protected Timer timer = new Timer();
    protected GlobalQuests dailyGlobalQuests = new GlobalQuests(this, QuestCategory.DAILY);
    protected GlobalQuests weeklyGlobalQuests = new GlobalQuests(this, QuestCategory.WEEKLY);

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
        timer.enable();
        Gui.enable(this);
        dailyGlobalQuests.enable();
        weeklyGlobalQuests.enable();
    }

    @Override
    public void onDisable() {
        Gui.disable();
        sessions.disable();
        database.disable();
    }

    public List<Quest> generateQuests(QuestCategory category) {
        switch (category) {
        case DAILY: return generateDailyQuests();
        case WEEKLY: return generateWeeklyQuests();
        default: throw new IllegalStateException(category.name());
        }
    }

    List<Quest> generateDailyQuests() {
        List<Quest> quests = new ArrayList<>();
        List<RegularGoalHolder> holders = new ArrayList<>();
        for (GoalType goalType : GoalType.values()) {
            if (goalType.holder instanceof RegularGoalHolder) {
                holders.add((RegularGoalHolder) goalType.holder);
            }
        }
        Collections.shuffle(holders);
        for (int i = 0; i < 3; i += 1) {
            Quest quest = holders.get(i).newDailyQuest();
            quest.setCategory(QuestCategory.DAILY);
            quest.getReward().setExperience(400);
            quest.getReward().setMoney(1000.0);
            quest.getReward().addItemStack(new ItemStack(Material.DIAMOND));
            quest.getTag().setExpiry(timer.getTomorrow());
            quests.add(quest);
        }
        return quests;
    }

    List<Quest> generateWeeklyQuests() {
        List<Quest> quests = new ArrayList<>();
        List<RegularGoalHolder> holders = new ArrayList<>();
        for (GoalType goalType : GoalType.values()) {
            if (goalType.holder instanceof RegularGoalHolder) {
                holders.add((RegularGoalHolder) goalType.holder);
            }
        }
        Collections.shuffle(holders);
        for (int i = 0; i < 5; i += 1) {
            Quest quest = holders.get(i).newWeeklyQuest();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.getReward().setExperience(1395);
            quest.getReward().setMoney(3000.0);
            quest.getReward().addItemStack(new ItemStack(Material.DIAMOND, 8));
            quest.getTag().setExpiry(timer.getNextWeek());
            quests.add(quest);
        }
        return quests;
    }
}
