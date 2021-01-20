package com.cavetale.quests;

import com.cavetale.quests.gui.Gui;
import com.cavetale.quests.util.Items;
import com.winthier.generic_events.GenericEvents;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Saved via Json.
 */
@Data
public final class QuestReward {
    List<Item> items;
    double money;
    List<String> commands;

    public boolean addItemStack(ItemStack itemStack) {
        if (itemStack == null || itemStack.getAmount() == 0) return false;
        if (items == null) items = new ArrayList<>();
        items.add(Item.of(itemStack));
        return true;
    }

    public List<ItemStack> getItemStacks() {
        if (items == null) return Collections.emptyList();
        List<ItemStack> list = new ArrayList<>(items.size());
        for (Item item : items) {
            list.add(item.toItemStack());
        }
        return list;
    }

    @Data
    public static final class Item {
        Material material;
        int amount;
        String base64;

        public static Item of(ItemStack itemStack) {
            Item item = new Item();
            item.setMaterial(itemStack.getType());
            item.setAmount(itemStack.getAmount());
            if (!new ItemStack(itemStack.getType()).isSimilar(itemStack)) {
                item.setBase64(Items.toBase64(itemStack));
            }
            return item;
        }

        public ItemStack toItemStack() {
            return base64 != null
                ? Items.fromBase64(base64)
                : new ItemStack(material, amount);
        }
    }

    public void givePlayer(Player player, Quest quest) {
        List<ItemStack> itemStacks = getItemStacks();
        if (!itemStacks.isEmpty()) {
            int rows = Math.max(3, ((itemStacks.size() - 1) / 9) * 9 + 1);
            Gui gui = new Gui()
                .title(quest.getTitle())
                .size(rows * 9);
            gui.setEditable(true);
            if (itemStacks.size() < 9) {
                int[] slots = {4, 3, 5, 2, 6, 1, 7, 0, 8};
                for (int i = 0; i < itemStacks.size(); i += 1) {
                    gui.getInventory().setItem(slots[i] + 9, itemStacks.get(i));
                }
            } else {
                for (ItemStack itemStack : itemStacks) {
                    gui.getInventory().addItem(itemStack);
                }
            }
            gui.onClose(unused -> onClose(player, gui, quest));
            gui.open(player);
            player.playSound(player.getLocation(),
                             Sound.BLOCK_CHEST_OPEN,
                             SoundCategory.MASTER,
                             0.5f, 1.0f);
        } else {
            postItems(player, quest);
        }
    }

    void giveMoney(Player player, Quest quest) {
        if (money < 0.01) return;
        GenericEvents.givePlayerMoney(player.getUniqueId(), money, QuestsPlugin.getInst(),
                                      "Quest Reward: " + quest.getTitle());
        player.sendMessage("Received "
                           + ChatColor.GOLD + GenericEvents.formatMoney(money)
                           + ChatColor.WHITE + ": " + quest.getTitle());
    }

    void runCommands(Player player) {
        if (commands == null) return;
        for (String command : commands) {
            command = command.replace("{player}", player.getName());
            QuestsPlugin.getInst().getLogger().info("Running command: " + command);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    void playSound(Player player) {
        player.playSound(player.getLocation(),
                         Sound.ENTITY_PLAYER_LEVELUP,
                         SoundCategory.MASTER,
                         0.5f, 2.0f);
    }

    void postItems(Player player, Quest quest) {
        giveMoney(player, quest);
        runCommands(player);
        playSound(player);
    }

    void onClose(Player player, Gui gui, Quest quest) {
        for (int i = 0; i < gui.getInventory().getSize(); i += 1) {
            ItemStack itemStack = gui.getInventory().getItem(i);
            if (itemStack == null || itemStack.getAmount() == 0) continue;
            gui.getInventory().setItem(i, null);
            for (ItemStack drop : player.getInventory().addItem(itemStack).values()) {
                player.getWorld().dropItem(player.getEyeLocation(), drop);
            }
        }
        postItems(player, quest);
    }
}
