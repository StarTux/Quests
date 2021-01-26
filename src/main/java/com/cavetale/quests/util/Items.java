package com.cavetale.quests.util;

import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class Items {
    private Items() { }

    public static String toBase64(ItemStack itemStack) {
        byte[] bytes = itemStack.serializeAsBytes();
        String string = Base64.getEncoder().encodeToString(bytes);
        return string;
    }

    public static ItemStack fromBase64(String string) {
        byte[] bytes = Base64.getDecoder().decode(string);
        ItemStack itemStack = ItemStack.deserializeBytes(bytes);
        return itemStack;
    }

    public static String getName(Material material) {
        return material.isItem()
            ? new ItemStack(material).getI18NDisplayName()
            : Stream.of(material.name().split("_")).map(Text::toCamelCase).collect(Collectors.joining(" "));
    }

    public static String getName(EnchantmentTarget enchantmentTarget) {
        switch (enchantmentTarget) {
        case ALL: return "Anything";
        case ARMOR_FEET: return "Boots";
        case ARMOR_HEAD: return "Helmet";
        case ARMOR_LEGS: return "Leggings";
        case ARMOR_TORSO: return "Chestplate";
        default: return Text.toCamelCase(enchantmentTarget);
        }
    }

    public static void setTooltip(ItemStack itemStack, String tooltip) {
        ItemMeta meta = itemStack.getItemMeta();
        String[] toks = tooltip.split("\n");
        meta.setDisplayName(toks[0]);
        toks = Arrays.copyOfRange(toks, 1, toks.length);
        meta.setLore(Arrays.asList(toks));
        meta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(meta);
    }

    public static void glow(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.values());
        itemStack.setItemMeta(meta);
    }
}
