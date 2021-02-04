package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import com.destroystokyo.paper.MaterialTags;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

@Data @EqualsAndHashCode(callSuper = true)
public final class GrowTreeGoal extends Goal {
    private Material blockType;

    @Override
    public boolean isValid() {
        return blockType != null;
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Grow " + getTreeName(blockType) + " with bonemeal";
    }

    public boolean onStructureGrow(QuestInstance questInstance, Block block) {
        if (blockType != block.getType()) return false;
        questInstance.increaseAmount();
        return true;
    }

    public static String getTreeName(Material material) {
        String result = Items.getName(material);
        if (result.endsWith(" Sapling")) {
            result = result.substring(0, result.length() - 7) + "Tree";
        }
        return result;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = GrowTreeGoal.class;
        private final Listener eventListener = new EventListener();
        List<Material> blockTypes;

        public List<Material> getBlockTypes() {
            if (blockTypes == null) {
                blockTypes = new ArrayList<>();
                for (Material mat : Tag.SAPLINGS.getValues()) blockTypes.add(mat);
                for (Material mat : Material.values()) {
                    if (MaterialTags.MUSHROOMS.isTagged(mat)) {
                        blockTypes.add(mat);
                    }
                }
            }
            return blockTypes;
        }

        @Override
        public Quest newDailyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            Random random = ThreadLocalRandom.current();
            List<Material> list = getBlockTypes();
            Material blockType = list.get(random.nextInt(list.size()));
            String blockName = getTreeName(blockType);
            quest.setTitle(blockName + " Gardener");
            GrowTreeGoal goal = (GrowTreeGoal) GoalType.GROW_TREE.newGoal();
            goal.setBlockType(blockType);
            goal.setAmount(2 + 2 * random.nextInt(4));
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            Random random = ThreadLocalRandom.current();
            List<Material> list = getBlockTypes();
            Collections.shuffle(list, random);
            int amount = Math.min(8, list.size());
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle("Master Gardener");
            quest.setDescription("Grow " + amount + " different trees with bonemeal");
            for (int i = 0; i < amount; i += 1) {
                Material blockType = list.get(i);
                String blockName = getTreeName(blockType);
                GrowTreeGoal goal = (GrowTreeGoal) GoalType.GROW_TREE.newGoal();
                goal.setBlockType(blockType);
                goal.setAmount(3);
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onStructureGrow(StructureGrowEvent event) {
            if (!event.isFromBonemeal()) return;
            Player player = event.getPlayer();
            if (player == null) return;
            Block block = event.getLocation().getBlock();
            for (QuestInstance questInstance : QuestInstance.of(player, GrowTreeGoal.class)) {
                GrowTreeGoal goal = (GrowTreeGoal) questInstance.getCurrentGoal();
                goal.onStructureGrow(questInstance, block);
            }
        }
    }
}
