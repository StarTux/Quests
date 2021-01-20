package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import com.destroystokyo.paper.MaterialTags;
import com.winthier.exploits.Exploits;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
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

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Mine " + (natural ? "natural " : "") + Items.getName(material);
    }

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
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = MineBlockGoal.class;
        private final Listener eventListener = new EventListener();
        List<Material> materials;

        public List<Material> getMaterials() {
            if (materials == null) {
                materials = new ArrayList<>();
                for (Material material : Material.values()) {
                    if (MaterialTags.ORES.isTagged(material)) {
                        materials.add(material);
                    }
                }
            }
            return materials;
        }

        @Override
        public Quest newDailyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle("Apprentice Miner");
            Random random = ThreadLocalRandom.current();
            List<Material> list = getMaterials();
            Material material = list.get(random.nextInt(list.size()));
            MineBlockGoal goal = (MineBlockGoal) GoalType.MINE_BLOCK.newGoal();
            goal.setMaterial(material);
            goal.setNatural(true);
            goal.setAmount(4 + 4 * random.nextInt(4));
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle("Master Miner");
            int amount = 5;
            quest.setDescription("Mine " + amount + " different ores");
            Random random = ThreadLocalRandom.current();
            List<Material> list = getMaterials();
            Collections.shuffle(list, random);
            for (int i = 0; i < amount; i += 1) {
                Material material = list.get(i);
                MineBlockGoal goal = (MineBlockGoal) GoalType.MINE_BLOCK.newGoal();
                goal.setMaterial(material);
                goal.setNatural(true);
                goal.setAmount(4 + 4 * random.nextInt(4));
                quest.getGoals().add(goal);
            }
            return quest;
        }
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
}
