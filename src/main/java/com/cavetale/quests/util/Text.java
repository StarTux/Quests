package com.cavetale.quests.util;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public final class Text {
    private Text() { }

    public static HoverEvent tooltip(String msg) {
        BaseComponent[] lore = TextComponent.fromLegacyText(msg);
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, lore);
    }

    public static ClickEvent button(String cmd) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, cmd);
    }

    public static String colorize(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static String toCamelCase(String in) {
        return in.substring(0, 1).toUpperCase()
            + in.substring(1).toLowerCase();
    }
}
