package com.cavetale.quests.goal;

import com.cavetale.quests.session.QuestInstance;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@Data @EqualsAndHashCode(callSuper = true)
public final class CommandGoal extends Goal {
    private String command;
    private List<String> arguments;
    private boolean registered; // Must be an actual command?

    @Override
    public boolean isValid() {
        return true;
    }

    public boolean onCommand(QuestInstance questInstance, PlayerCommandPreprocessEvent event) {
        if (command == null) return false;
        String[] toks = event.getMessage().split("\\s+");
        String cmd = toks[0];
        if (cmd.startsWith("/")) cmd = cmd.substring(1);
        String[] args = Arrays.copyOfRange(toks, 1, toks.length);
        if (registered) {
            // Fetch the command by the entered alias, and see if its
            // name equals the name in the condition.
            Command registeredCommand = Bukkit.getCommandMap().getCommand(cmd);
            if (registeredCommand == null) return false;
            if (!Objects.equals(registeredCommand.getName(), command)) return false;
        } else {
            if (!Objects.equals(command, cmd)) return false;
        }
        // Name matches!
        if (arguments != null) {
            if (args.length < arguments.size()) return false;
            for (int i = 0; i < args.length; i += 1) {
                if (!Objects.equals(args[i], arguments.get(i))) return false;
            }
        }
        questInstance.increaseAmount();
        return true;
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
            Player player = event.getPlayer();
            for (QuestInstance questInstance : QuestInstance.of(player, CommandGoal.class)) {
                CommandGoal goal = (CommandGoal) questInstance.getCurrentGoal();
                goal.onCommand(questInstance, event);
            }
        }
    }

    @Getter
    public static final class Holder implements GoalHolder {
        private final Class<? extends Goal> goalClass = CommandGoal.class;
        private final EventListener eventListener = new EventListener();
    }

    @Override
    public String getDescription() {
        return description != null ? description : "Type /" + command;
    }
}
