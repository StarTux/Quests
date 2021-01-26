package com.cavetale.quests.goal;

import com.cavetale.quests.Quest;
import com.cavetale.quests.QuestCategory;
import com.cavetale.quests.QuestsPlugin;
import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.util.Items;
import com.cavetale.quests.util.Text;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.SmokingRecipe;

@Data @EqualsAndHashCode(callSuper = true)
public final class CookItemGoal extends Goal {
    private Material itemType;
    /**
     * May be FURNACE, SMOKER, BLAST_FURNACE.
     */
    private InventoryType cookerType;

    @Override
    public boolean isValid() {
        return itemType != null;
    }

    @Override
    public String getDescription() {
        return description != null
            ? description
            : ((itemType.isEdible() ? "Cook " : "Smelt ")
               + getItemName(itemType)
               + (cookerType != null
                  ? " in a " + Text.toCamelCase(cookerType)
                  : ""));
    }

    public boolean onCookerExtract(QuestInstance questInstance, ItemStack item, int extracted, InventoryType inventoryType) {
        if (itemType != item.getType()) return false;
        if (cookerType != null && cookerType != inventoryType) return false;
        questInstance.increaseAmount(extracted);
        return true;
    }

    public static String getItemName(Material material) {
        String result = Items.getName(material);
        if (result.startsWith("Cooked ")) {
            result = result.substring(7);
        }
        if (result.startsWith("Baked ")) {
            result = result.substring(6);
        }
        return result;
    }

    @Getter
    public static final class Holder implements RegularGoalHolder {
        private final Class<? extends Goal> goalClass = CookItemGoal.class;
        private final Listener eventListener = new EventListener();
        EnumMap<InventoryType, EnumSet<Material>> itemTypes;

        public EnumMap<InventoryType, EnumSet<Material>> getItemTypes() {
            if (itemTypes == null) {
                itemTypes = new EnumMap<>(InventoryType.class);
                Iterator<Recipe> iter = Bukkit.recipeIterator();
                while (iter.hasNext()) {
                    Recipe recipe = iter.next();
                    ItemStack result = recipe.getResult();
                    if (!new ItemStack(result.getType()).isSimilar(result)) continue;
                    switch (result.getType()) {
                    case DIAMOND:
                    case EMERALD:
                    case COAL:
                    case QUARTZ:
                    case LAPIS_LAZULI:
                    case REDSTONE:
                    case IRON_NUGGET:
                    case GOLD_NUGGET:
                    case NETHERITE_SCRAP:
                        continue;
                    default:
                        break;
                    }
                    InventoryType inventoryType;
                    if (recipe instanceof BlastingRecipe) {
                        inventoryType = InventoryType.BLAST_FURNACE;
                    } else if (recipe instanceof FurnaceRecipe) {
                        inventoryType = InventoryType.FURNACE;
                    } else if (recipe instanceof SmokingRecipe) {
                        inventoryType = InventoryType.SMOKER;
                    } else {
                        continue;
                    }
                    itemTypes.computeIfAbsent(inventoryType, i -> EnumSet.noneOf(Material.class))
                        .add(result.getType());
                }
            }
            return itemTypes;
        }

        public InventoryType randomInventoryType() {
            List<InventoryType> list = Arrays.asList(InventoryType.BLAST_FURNACE,
                                                     InventoryType.FURNACE,
                                                     InventoryType.SMOKER);
            return list.get(ThreadLocalRandom.current().nextInt(list.size()));
        }

        @Override
        public Quest newDailyQuest() {
            Quest quest = Quest.newInstance();
            quest.setCategory(QuestCategory.DAILY);
            Random random = ThreadLocalRandom.current();
            InventoryType inventoryType = randomInventoryType();
            List<Material> list = new ArrayList<>(getItemTypes().get(inventoryType));
            Material itemType = list.get(random.nextInt(list.size()));
            String itemName = getItemName(itemType);
            quest.setTitle(itemName + (itemType.isEdible() ? " Chef" : " Smelter"));
            CookItemGoal goal = (CookItemGoal) GoalType.COOK_ITEM.newGoal();
            goal.setItemType(itemType);
            goal.setCookerType(inventoryType);
            goal.setAmount(itemType.getMaxStackSize());
            quest.getGoals().add(goal);
            return quest;
        }

        @Override
        public Quest newWeeklyQuest() {
            Quest quest = Quest.newInstance();
            Random random = ThreadLocalRandom.current();
            InventoryType inventoryType = randomInventoryType();
            List<Material> list = new ArrayList<>(getItemTypes().get(inventoryType));
            Collections.shuffle(list, random);
            int amount = Math.min(10, list.size());
            quest.setCategory(QuestCategory.WEEKLY);
            String cookerName = Text.toCamelCase(inventoryType);
            switch (inventoryType) {
            case SMOKER:
                quest.setTitle(cookerName + " Chef");
                quest.setDescription("Cook " + amount + " different meals in a " + cookerName);
                break;
            case BLAST_FURNACE:
                quest.setTitle(cookerName + " Smith");
                quest.setDescription("Smelt " + amount + " different items in a " + cookerName);
                break;
            default:
                quest.setTitle(cookerName + " Pro");
                quest.setDescription("Cook " + amount + " different items in a " + cookerName);
            }
            for (int i = 0; i < amount; i += 1) {
                Material itemType = list.get(i);
                String itemName = getItemName(itemType);
                CookItemGoal goal = (CookItemGoal) GoalType.COOK_ITEM.newGoal();
                goal.setItemType(itemType);
                goal.setCookerType(inventoryType);
                goal.setAmount(itemType.getMaxStackSize());
                quest.getGoals().add(goal);
            }
            return quest;
        }
    }

    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        void onInventoryClick(InventoryClickEvent event) {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player) event.getWhoClicked();
            if (event.getSlotType() != SlotType.RESULT) return;
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            if (!(inventory.getHolder() instanceof BlockInventoryHolder)) return;
            InventoryType inventoryType = inventory.getType();
            switch (inventoryType) {
            case BLAST_FURNACE:
            case FURNACE:
            case SMOKER:
                break;
            default:
                return;
            }
            switch (event.getAction()) {
            case COLLECT_TO_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
            case HOTBAR_MOVE_AND_READD:
            case MOVE_TO_OTHER_INVENTORY:
            case PICKUP_ALL:
            case PICKUP_HALF:
            case PICKUP_ONE:
            case PICKUP_SOME:
                break;
            default:
                return;
            }
            ItemStack itemStack = event.getCurrentItem();
            if (itemStack == null || itemStack.getAmount() == 0) return;
            ItemStack oldItem = itemStack.clone();
            int slot = event.getSlot();
            Bukkit.getScheduler().runTask(QuestsPlugin.getInst(), () -> {
                    ItemStack newItem = inventory.getItem(slot);
                    if (newItem != null && newItem.getAmount() != 0 && !newItem.isSimilar(oldItem)) return;
                    int extracted = oldItem.getAmount() - (newItem != null ? newItem.getAmount() : 0);
                    if (extracted <= 0) return;
                    for (QuestInstance questInstance : QuestInstance.of(player, CookItemGoal.class)) {
                        CookItemGoal goal = (CookItemGoal) questInstance.getCurrentGoal();
                        goal.onCookerExtract(questInstance, oldItem, extracted, inventoryType);
                    }
                });
        }
    }
}
