package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

@Data @EqualsAndHashCode(callSuper = true)
public final class EatGoal extends Goal {
    private Material material;

    @Override
    public boolean isValid() {
        return material != null;
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Eat " + Items.getName(material);
    }

    public boolean onEat(QuestInstance questInstance, ItemStack itemStack) {
        if (material != itemStack.getType()) return false;
        questInstance.increaseAmount();
        return true;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = EatGoal.class;
        private final Listener eventListener = new EventListener();
        List<Material> materials;

        public List<Material> getMaterials() {
            if (materials == null) {
                materials = new ArrayList<>();
                for (Material material : Material.values()) {
                    if (material.isEdible()) {
                        materials.add(material);
                    }
                }
            }
            return materials;
        }

        @Override
        public Quest newDailyQuest() {
            Random random = ThreadLocalRandom.current();
            List<Material> list = getMaterials();
            Material material = list.get(random.nextInt(list.size()));
            String materialName = Items.getName(material);
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle(materialName + " Eater");
            EatGoal goal = (EatGoal) GoalType.EAT.newGoal();
            goal.setMaterial(material);
            goal.setAmount(3 + 3 * random.nextInt(3));
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle("Professional Eater");
            int amount = 7;
            quest.setDescription("Eat " + amount + " different foods");
            Random random = ThreadLocalRandom.current();
            List<Material> list = getMaterials();
            Collections.shuffle(list, random);
            for (int i = 0; i < amount; i += 1) {
                Material material = list.get(i);
                EatGoal goal = (EatGoal) GoalType.EAT.newGoal();
                goal.setMaterial(material);
                goal.setAmount(1);
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onPlayerItemConsume(PlayerItemConsumeEvent event) {
            Player player = (Player) event.getPlayer();
            for (QuestInstance questInstance : QuestInstance.of(player, EatGoal.class)) {
                EatGoal goal = (EatGoal) questInstance.getCurrentGoal();
                goal.onEat(questInstance, event.getItem());
            }
        }
    }
}
