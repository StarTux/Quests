package com.cavetale.quests.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public final class GuiListener implements Listener {
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    void onInventoryOpen(final InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof Gui) {
            ((Gui) event.getInventory().getHolder())
                .onInventoryOpen(event);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    void onInventoryClose(final InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof Gui) {
            ((Gui) event.getInventory().getHolder())
                .onInventoryClose(event);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    void onInventoryClick(final InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof Gui) {
            ((Gui) event.getInventory().getHolder())
                .onInventoryClick(event);
        }
    }

    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    void onInventoryDrag(final InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof Gui) {
            ((Gui) event.getInventory().getHolder())
                .onInventoryDrag(event);
        }
    }
}
