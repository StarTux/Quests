package com.cavetale.quests;

import com.cavetale.core.command.CommandNode;
import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.goal.KillMobGoal;
import com.cavetale.quests.goal.RegularGoalHolder;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.session.Session;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class AdminCommand implements TabExecutor {
    private final QuestsPlugin plugin;
    private CommandNode rootNode;

    public void enable() {
        rootNode = new CommandNode("questsadmin");
        rootNode.addChild("test").playerCaller(this::test);
        rootNode.addChild("clear").playerCaller(this::clear);
        plugin.getCommand("questsadmin").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    boolean test(Player player, String[] args) {
        int count = 0;
        for (GoalType goalType : GoalType.values()) {
            if (!(goalType.holder instanceof RegularGoalHolder)) continue;
            RegularGoalHolder holder = (RegularGoalHolder) goalType.holder;
            Quest dailyQuest = holder.newDailyQuest();
            dailyQuest.getReward().setMoney(100.0);
            if (goalType == GoalType.KILL_MOB) {
                KillMobGoal goal = (KillMobGoal) dailyQuest.getGoals().get(0);
                goal.setEntityType(EntityType.ZOGLIN);
                goal.setWeaponType(KillMobGoal.WeaponType.MELEE);
            }
            plugin.sessions.of(player).addNewQuest(dailyQuest);
            count += 1;
            Quest weeklyQuest = holder.newWeeklyQuest();
            weeklyQuest.getReward().setMoney(1000.0);
            plugin.sessions.of(player).addNewQuest(weeklyQuest);
            count += 1;
        }
        player.sendMessage(count + " new quests added");
        return true;
    }

    boolean clear(Player player, String[] args) {
        int count = 0;
        for (QuestInstance questInstance : plugin.sessions.of(player).getQuests()) {
            if (!plugin.sessions.of(player).removeQuest(questInstance)) {
                player.sendMessage("Something went wrong: " + questInstance.getRow().getId());
                continue;
            }
            count += 1;
        }
        player.sendMessage(count + " quests removed!");
        return true;
    }

    public static void testDailies(Session session) {
        for (GoalType goalType : GoalType.values()) {
            if (!(goalType.holder instanceof RegularGoalHolder)) continue;
            RegularGoalHolder holder = (RegularGoalHolder) goalType.holder;
            Quest dailyQuest = holder.newDailyQuest();
            dailyQuest.getReward().setMoney(100.0);
            dailyQuest.getReward().addItemStack(new ItemStack(Material.DIAMOND));
            session.addNewQuest(dailyQuest);
            Quest weeklyQuest = holder.newWeeklyQuest();
            weeklyQuest.getReward().setMoney(1000.0);
            weeklyQuest.getReward().addItemStack(new ItemStack(Material.GOLDEN_APPLE));
            session.addNewQuest(weeklyQuest);
        }
    }
}
