package com.cavetale.quests.gui;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor @AllArgsConstructor
final class Slot {
    final int index;
    ItemStack item;
    Function<InventoryClickEvent, Boolean> onClick;
}
