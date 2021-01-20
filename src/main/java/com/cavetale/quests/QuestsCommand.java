package com.cavetale.quests;

import com.cavetale.core.command.CommandNode;
import com.cavetale.quests.session.QuestInstance;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class QuestsCommand implements TabExecutor {
    private final QuestsPlugin plugin;
    private CommandNode rootNode;

    public void enable() {
        rootNode = new CommandNode("quests")
            .description("View your quests")
            .playerCaller(this::quests);
        rootNode.addChild("accept").denyTabCompletion()
            .hidden(true)
            .playerCaller(this::accept);
        rootNode.addChild("claim").denyTabCompletion()
            .hidden(true)
            .playerCaller(this::claim);
        rootNode.addChild("view").denyTabCompletion()
            .hidden(true)
            .playerCaller(this::view);
        plugin.getCommand("quests").setExecutor(this);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.call(sender, command, alias, args);
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return rootNode.complete(sender, command, alias, args);
    }

    boolean quests(Player player, String[] args) {
        if (args.length != 0) return false;
        plugin.sessions.of(player).getQuestBook().openBook();
        return true;
    }

    /**
     * Called via Component click.
     * Therefore we fail silently.
     */
    boolean accept(Player player, String[] args) {
        if (args.length != 1) return true;
        int questId;
        try {
            questId = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            return true;
        }
        QuestInstance questInstance = plugin.sessions.of(player).findQuest(questId);
        if (questInstance == null || questInstance.getProgress().isAccepted()) return true;
        questInstance.playerAccept();
        plugin.sessions.of(player).getQuestBook().openBook(questInstance);
        return true;
    }

    /**
     * Called via Component click.
     * Therefore we fail silently.
     */
    boolean claim(Player player, String[] args) {
        if (args.length != 1) return true;
        int questId;
        try {
            questId = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            return true;
        }
        QuestInstance questInstance = plugin.sessions.of(player).findQuest(questId);
        if (questInstance == null || questInstance.getRow().isClaimed()) return true;
        questInstance.playerClaim();
        return true;
    }

    /**
     */
    boolean view(Player player, String[] args) {
        if (args.length != 1) return true;
        int questId;
        try {
            questId = Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            return true;
        }
        QuestInstance questInstance = plugin.sessions.of(player).findQuest(questId);
        plugin.sessions.of(player).getQuestBook().openBook(questInstance);
        return true;
    }
}
