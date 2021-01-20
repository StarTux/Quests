package com.cavetale.quests.goal;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Listener;

@Data @EqualsAndHashCode(callSuper = true)
public final class ChatGoal extends Goal {
    public static final class EventListener implements Listener {
    }

    @Data
    public static final class Holder implements GoalHolder {
        private final Class<? extends Goal> goalClass = ChatGoal.class;
        private final Class<? extends Progress> progressClass = Progress.class;
        private final EventListener eventListener = new EventListener();
    }
}
