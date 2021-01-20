package com.cavetale.quests.goal;

import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import com.winthier.exploits.Exploits;
import java.util.Objects;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

@Data @EqualsAndHashCode(callSuper = true)
public final class MineBlockGoal extends Goal {
    private Material material;
    private String blockData;
    private boolean natural;

    public boolean onBlockBreak(QuestInstance questInstance, BlockBreakEvent event) {
        if (material != event.getBlock().getType()) return false;
        if (blockData != null) {
            String string = event.getBlock().getBlockData().getAsString();
            if (!Objects.equals(string, blockData)) return false;
        }
        if (natural) {
            if (Exploits.isPlayerPlaced(event.getBlock())) return false;
        }
        questInstance.increaseAmount();
        return true;
    }

    @Getter
    public static final class Holder implements GoalHolder {
        private final Class<? extends Goal> goalClass = MineBlockGoal.class;
        private final Listener eventListener = new EventListener();
    }

    public static final class EventListener implements Listener {
        // MONITOR would break Exploits
        @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
        void onBlockBreak(BlockBreakEvent event) {
            Player player = (Player) event.getPlayer();
            for (QuestInstance questInstance : QuestInstance.of(player, MineBlockGoal.class)) {
                MineBlockGoal goal = (MineBlockGoal) questInstance.getCurrentGoal();
                goal.onBlockBreak(questInstance, event);
            }
        }
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Mine " + (natural ? "natural " : "") + Items.getName(material);
    }
}
