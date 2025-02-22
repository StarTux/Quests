package com.cavetale.quests;

import com.cavetale.core.command.CommandNode;
import com.cavetale.quests.goal.GoalType;
import com.cavetale.quests.goal.RegularGoalHolder;
import com.cavetale.quests.session.QuestInstance;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class AdminCommand implements TabExecutor {
    private final QuestsPlugin plugin;
    private CommandNode rootNode;

    public void enable() {
        rootNode = new CommandNode("questsadmin");
        rootNode.addChild("test").playerCaller(this::test);
        rootNode.addChild("clear").playerCaller(this::clear);
        rootNode.addChild("timer").senderCaller(this::timer);
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

    boolean timer(CommandSender sender, String[] args) {
        plugin.getTimer().debug();
        sender.sendMessage("Timer debug printed");
        return true;
    }
}
