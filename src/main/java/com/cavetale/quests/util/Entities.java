package com.cavetale.quests.util;

import org.bukkit.entity.EntityType;

public final class Entities {
    private Entities() { }

    public static String singular(EntityType entityType) {
        return Text.toCamelCase(entityType);
    }

    public static String plural(EntityType entityType) {
        switch (entityType) {
        case WOLF: return "Wolves";
        case WITCH: return "Witches";
        default:
            return Text.toCamelCase(entityType) + "s";
        }
    }

    public static String singularOrPlural(EntityType entityType, int amount) {
        return amount == 1 ? singular(entityType) : plural(entityType);
    }
}
