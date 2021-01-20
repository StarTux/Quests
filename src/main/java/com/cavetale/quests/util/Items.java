package com.cavetale.quests.util;

import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
}
