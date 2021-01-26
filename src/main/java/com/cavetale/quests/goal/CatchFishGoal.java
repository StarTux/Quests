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
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Catch some fish with a fishing pole.
 */
@Data @EqualsAndHashCode(callSuper = true)
public final class CatchFishGoal extends Goal {
    Material itemType;

    @Override
    public boolean isValid() {
        return itemType != null;
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : "Catch " + getFishName(itemType);
    }

    public boolean onPlayerFish(QuestInstance questInstance, ItemStack itemStack) {
        if (itemType != itemStack.getType()) return false;
        questInstance.increaseAmount();
        return true;
    }

    public static String getFishName(Material mat) {
        String result = Items.getName(mat);
        if (result.startsWith("Raw ")) result = result.substring(4);
        return result;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = CatchFishGoal.class;
        private final Listener eventListener = new EventListener();
        private List<Material> itemTypes;

        List<Material> getItemTypes() {
            if (itemTypes == null) {
                itemTypes = new ArrayList<>(MaterialTags.RAW_FISH.getValues());
            }
            return itemTypes;
        }

        @Override
        public Quest newDailyQuest() {
            Random random = ThreadLocalRandom.current();
            List<Material> list = getItemTypes();
            Material itemType = list.get(random.nextInt(list.size()));
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle(getFishName(itemType) + " Fisher");
            CatchFishGoal goal = (CatchFishGoal) GoalType.CATCH_FISH.newGoal();
            goal.setItemType(itemType);
            goal.setAmount(1);
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Random random = ThreadLocalRandom.current();
            List<Material> list = getItemTypes();
            int amount = Math.min(list.size(), 7);
            Collections.shuffle(list, random);
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle("Master Angler");
            quest.setDescription("Catch " + amount + " different kinds of Fish");
            for (int i = 0; i < amount; i += 1) {
                CatchFishGoal goal = (CatchFishGoal) GoalType.CATCH_FISH.newGoal();
                Material itemType = list.get(i);
                goal.setItemType(itemType);
                goal.setAmount(1);
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onPlayerFish(PlayerFishEvent event) {
            if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) return;
            if (!(event.getCaught() instanceof Item)) return;
            ItemStack itemStack = ((Item) event.getCaught()).getItemStack();
            if (itemStack == null || itemStack.getAmount() == 0) return;
            for (QuestInstance questInstance : QuestInstance.of(event.getPlayer(), CatchFishGoal.class)) {
                CatchFishGoal goal = (CatchFishGoal) questInstance.getCurrentGoal();
                goal.onPlayerFish(questInstance, itemStack);
            }
        }
    }
}
