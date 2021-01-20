package com.cavetale.quests;

import com.cavetale.quests.util.Text;
import net.md_5.bungee.api.ChatColor;

/**
 * Stored by ordinal in SQLQuest. Do not change the order!
 */
public enum QuestCategory {
    DEFAULT(ChatColor.DARK_GRAY),
    DEBUG(ChatColor.DARK_RED),
    DAILY(ChatColor.DARK_GREEN),
    WEEKLY(ChatColor.DARK_BLUE),
    MONTHLY(ChatColor.GOLD),
    STORY(ChatColor.DARK_AQUA);

    public final ChatColor color;
    public final String humanName;

    QuestCategory(final ChatColor color) {
        this.color = color;
        this.humanName = Text.toCamelCase(name());
    }
}
