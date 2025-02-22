package com.cavetale.quests.gui;

import com.cavetale.quests.QuestsPlugin;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public final class Gui implements InventoryHolder {
    public static final int OUTSIDE = -999;
    private Inventory inventory;
    private Map<Integer, Slot> slots = new TreeMap<>();
    private Consumer<InventoryCloseEvent> onClose = null;
    private Consumer<InventoryOpenEvent> onOpen = null;
    @Getter @Setter private boolean editable = false;
    @Getter private int size = 3 * 9;
    @Getter private String title = "";
    boolean locked = false;

    public Gui() { }

    public Gui title(String newTitle) {
        title = newTitle;
        return this;
    }

    public Gui size(int newSize) {
        if (newSize <= 0 || newSize % 9 != 0) {
            throw new IllegalArgumentException("newSize=" + newSize);
        }
        size = newSize;
        return this;
    }

    public Gui rows(int rowCount) {
        if (rowCount <= 0) throw new IllegalArgumentException("rowCount=" + rowCount);
        size = rowCount * 9;
        return this;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            if (title == null) title = "";
            inventory = Bukkit.getServer().createInventory(this, size, title);
            for (int i = 0; i < size; i += 1) {
                Slot slot = slots.get(i);
                if (slot != null) {
                    inventory.setItem(i, slot.item);
                }
            }
        }
        return inventory;
    }

    public ItemStack getItem(int index) {
        if (index < 0) index = OUTSIDE;
        Slot slot = slots.get(index);
        return slot != null
            ? slot.item
            : null;
    }

    public void setItem(int index, ItemStack item) {
        setItem(index, item, null);
    }

    public void setItem(int index, ItemStack item, Function<InventoryClickEvent, Boolean> responder) {
        if (inventory != null && index >= 0 && inventory.getSize() > index) {
            inventory.setItem(index, item);
        }
        if (index < 0) index = OUTSIDE;
        Slot slot = new Slot(index, item, responder);
        slots.put(index, slot);
    }

    public void setItem(int column, int row, ItemStack item, Function<InventoryClickEvent, Boolean> responder) {
        if (column < 0 || column > 8) {
            throw new IllegalArgumentException("column=" + column);
        }
        if (row < 0) throw new IllegalArgumentException("row=" + row);
        setItem(column + row * 9, item, responder);
    }

    public Gui open(Player player) {
        player.openInventory(getInventory());
        return this;
    }

    public Gui reopen(Player player) {
        player.closeInventory();
        inventory = null;
        player.openInventory(getInventory());
        return this;
    }

    public Gui onClose(Consumer<InventoryCloseEvent> responder) {
        onClose = responder;
        return this;
    }

    public Gui onOpen(Consumer<InventoryOpenEvent> responder) {
        onOpen = responder;
        return this;
    }

    public Gui clear() {
        if (inventory != null) inventory.clear();
        slots.clear();
        onOpen = null;
        onClose = null;
        return this;
    }

    void onInventoryOpen(InventoryOpenEvent event) {
        if (onOpen != null) {
            onOpen.accept(event);
        }
    }

    void onInventoryClose(InventoryCloseEvent event) {
        if (onClose != null) {
            onClose.accept(event);
        }
    }

    void onInventoryClick(InventoryClickEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
        if (locked) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != null
            && !inventory.equals(event.getClickedInventory())) {
            return;
        }
        Slot slot = slots.get(event.getSlot());
        if (slot != null && slot.onClick != null) {
            locked = true;
            Bukkit.getScheduler().runTask(QuestsPlugin.getInst(), () -> {
                    locked = false;
                    slot.onClick.apply(event);
                });
        }
    }

    void onInventoryDrag(InventoryDragEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
    }

    public static Gui of(Player player) {
        InventoryView view = player.getOpenInventory();
        if (view == null) return null;
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) return null;
        InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof Gui)) return null;
        Gui gui = (Gui) holder;
        return gui;
    }

    public static void enable(QuestsPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new GuiListener(), plugin);
    }

    public static void disable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Gui gui = Gui.of(player);
            if (gui != null) {
                player.closeInventory();
            }
        }
    }
}
