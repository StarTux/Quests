package com.cavetale.quests;

import com.cavetale.core.command.CommandNode;
import com.cavetale.quests.goal.CommandGoal;
import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.goal.MineBlockGoal;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.session.Session;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
        Quest quest = Quest.newInstance();
        quest.getTag().setCategory(QuestCategory.DEBUG);
        quest.getTag().setTitle("The big testing quest!");
        quest.getTag().setDescription("Learn the basics.");
        do {
            CommandGoal goal = (CommandGoal) GoalType.COMMAND.newGoal();
            goal.setCommand("rules");
            goal.setRegistered(true);
            quest.getGoals().add(goal);
        } while (false);
        do {
            MineBlockGoal goal = (MineBlockGoal) GoalType.MINE_BLOCK.newGoal();
            goal.setMaterial(Material.DIAMOND_ORE);
            goal.setAmount(8);
            goal.setNatural(true);
            quest.getGoals().add(goal);
        } while (false);
        quest.getReward().addItemStack(new ItemStack(Material.DIAMOND));
        quest.getReward().setMoney(1337.0);
        plugin.sessions.of(player).addNewQuest(quest);
        player.sendMessage("New quest added");
        return true;
    }

    boolean clear(Player player, String[] args) {
        int count = 0;
        for (QuestInstance questInstance : plugin.sessions.of(player).getQuests(QuestCategory.DEBUG)) {
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
        do {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.getTag().setTitle("Rules Refresher");
            CommandGoal goal = (CommandGoal) GoalType.COMMAND.newGoal();
            goal.setCommand("rules");
            goal.setRegistered(true);
            quest.getGoals().add(goal);
            quest.getReward().addItemStack(new ItemStack(Material.EMERALD));
            quest.getReward().setMoney(500.0);
            session.addNewQuest(quest);
        } while (false);
        do {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.getTag().setTitle("Iron Miner");
            MineBlockGoal goal = (MineBlockGoal) GoalType.MINE_BLOCK.newGoal();
            goal.setMaterial(Material.IRON_ORE);
            goal.setNatural(true);
            goal.setAmount(32);
            quest.getGoals().add(goal);
            quest.getReward().addItemStack(new ItemStack(Material.IRON_INGOT, 16));
            quest.getReward().setMoney(1000.0);
            session.addNewQuest(quest);
        } while (false);
        do {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.getTag().setTitle("Diamond Miner");
            MineBlockGoal goal = (MineBlockGoal) GoalType.MINE_BLOCK.newGoal();
            goal.setMaterial(Material.DIAMOND_ORE);
            goal.setNatural(true);
            goal.setAmount(16);
            quest.getGoals().add(goal);
            quest.getReward().addItemStack(new ItemStack(Material.DIAMOND, 4));
            quest.getReward().setMoney(1000.0);
            session.addNewQuest(quest);
        } while (false);
    }
}
