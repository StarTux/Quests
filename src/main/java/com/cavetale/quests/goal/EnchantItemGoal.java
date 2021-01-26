package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import com.cavetale.quests.util.Text;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;

/**
 * Enchant an item.
 * Either itemType or enchantment must not be null. Level is optional.
 */
@Data @EqualsAndHashCode(callSuper = true)
public final class EnchantItemGoal extends Goal {
    private Material itemType;
    private EnchantmentTarget enchantmentTarget;
    private String enchantmentKey; // e.g. "unbreaking"
    private int level;
    private transient Enchantment enchantment;

    @Override
    public boolean isValid() {
        return (itemType != null || enchantmentTarget != null)
            || enchantmentKey != null;
    }

    public Enchantment getEnchantment() {
        if (enchantment != null) return enchantment;
        if (enchantmentKey == null) return null;
        NamespacedKey namespacedKey = NamespacedKey.minecraft(enchantmentKey);
        enchantment = Enchantment.getByKey(namespacedKey);
        return enchantment;
    }

    public void setEnchantment(Enchantment theEnchantment) {
        this.enchantment = theEnchantment;
        enchantmentKey = theEnchantment.getKey().getKey();
    }

    @Override
    public String getDescription() {
        getEnchantment();
        return description != null
            ? description
            : ("Enchant "
               + (itemType != null
                  ? Items.getName(itemType)
                  : (enchantmentTarget != null
                     ? Items.getName(enchantmentTarget)
                     : "any item"))
               + (enchantment != null
                  ? " with " + Text.toCamelCase(enchantment.getKey())
                  : "")
               + (level > 0 ? " " + Text.roman(level) : ""));
    }

    public boolean onEnchantItem(QuestInstance questInstance, EnchantItemEvent event) {
        if (itemType != null && event.getItem().getType() != itemType) {
            return false;
        }
        if (enchantmentTarget != null && !enchantmentTarget.includes(event.getItem().getType())) {
            return false;
        }
        getEnchantment();
        if (enchantment != null && !event.getEnchantsToAdd().containsKey(enchantment)) {
            return false;
        }
        if (level > 0) {
            Integer newLevel = event.getEnchantsToAdd().get(enchantment);
            if (newLevel == null || newLevel < level) return false;
        }
        questInstance.increaseAmount();
        return true;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = EnchantItemGoal.class;
        private final Listener eventListener = new EventListener();
        private List<Enchantment> enchantments;

        public List<Enchantment> getEnchantments() {
            if (enchantments == null) {
                enchantments = new ArrayList<>();
                for (Enchantment enchantment : Enchantment.values()) {
                    if (enchantment.isTreasure()) continue;
                    if (enchantment.isCursed()) continue;
                    enchantments.add(enchantment);
                }
            }
            return enchantments;
        }

        @Override
        public Quest newDailyQuest() {
            Quest quest = Quest.newInstance();
            Random random = ThreadLocalRandom.current();
            List<Enchantment> list = getEnchantments();
            Enchantment enchantment = list.get(random.nextInt(list.size()));
            quest.setCategory(QuestCategory.DAILY);
            quest.setTitle(Text.toCamelCase(enchantment.getKey()) + " Mage");
            EnchantItemGoal goal = (EnchantItemGoal) GoalType.ENCHANT_ITEM.newGoal();
            goal.setEnchantment(enchantment);
            goal.setAmount(1);
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            Random random = ThreadLocalRandom.current();
            List<Enchantment> list = getEnchantments();
            Enchantment enchantment = list.get(random.nextInt(list.size()));
            quest.setCategory(QuestCategory.WEEKLY);
            quest.setTitle(Text.toCamelCase(enchantment.getKey()) + " Wizard");
            EnchantItemGoal goal = (EnchantItemGoal) GoalType.ENCHANT_ITEM.newGoal();
            goal.setEnchantment(enchantment);
            goal.setLevel(Math.min(4, enchantment.getMaxLevel()));
            goal.setEnchantmentTarget(enchantment.getItemTarget());
            goal.setAmount(1);
            quest.getGoals().add(goal);
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onEnchantItem(EnchantItemEvent event) {
            Player player = (Player) event.getEnchanter();
            for (QuestInstance questInstance : QuestInstance.of(player, EnchantItemGoal.class)) {
                EnchantItemGoal goal = (EnchantItemGoal) questInstance.getCurrentGoal();
                goal.onEnchantItem(questInstance, event);
            }
        }
    }
}
