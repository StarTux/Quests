package com.cavetale.quests.goal;

import com.cavetale.quests.QuestsPlugin;
import com.cavetale.quests.session.QuestInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

@Data @EqualsAndHashCode(callSuper = true)
public final class ChatGoal extends Goal {
    private String message;
    private boolean caseInsensitive;

    public boolean onChat(QuestInstance questInstance, AsyncPlayerChatEvent event) {
        if (message != null) {
            if (caseInsensitive) {
                if (event.getMessage().toLowerCase().contains(message.toLowerCase())) return false;
            } else {
                if (event.getMessage().contains(message)) return false;
            }
        }
        questInstance.increaseAmount();
        return true;
    }

    @Data
    public static final class Holder implements GoalHolder {
        private final Class<? extends Goal> goalClass = ChatGoal.class;
        private final EventListener eventListener = new EventListener();
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
            Bukkit.getScheduler().runTask(QuestsPlugin.getInst(), () -> onDelayedAsyncPlayerChat(event));
        }

        void onDelayedAsyncPlayerChat(AsyncPlayerChatEvent event) {
            Player player = event.getPlayer();
            for (QuestInstance questInstance : QuestInstance.of(player, ChatGoal.class)) {
                ChatGoal goal = (ChatGoal) questInstance.getCurrentGoal();
                goal.onChat(questInstance, event);
            }
        }
    }
}
