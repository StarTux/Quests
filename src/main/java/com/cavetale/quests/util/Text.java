package com.cavetale.quests.util;

import java.util.ArrayList;
import java.util.List;
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

    public static ClickEvent bookmark(int pageIndex) {
        return new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "" + pageIndex);
    }

    public static String colorize(String in) {
        return ChatColor.translateAlternateColorCodes('&', in);
    }

    public static String toCamelCase(String in) {
        return in.substring(0, 1).toUpperCase()
            + in.substring(1).toLowerCase();
    }

    public static String toCamelCase(String[] in) {
        String[] out = new String[in.length];
        for (int i = 0; i < in.length; i += 1) {
            out[i] = toCamelCase(in[i]);
        }
        return String.join(" ", out);
    }

    public static String toCamelCase(Enum en) {
        return toCamelCase(en.name().split("_"));
    }

    public static String getProgressString(int amount, int required) {
        return required > 1
            ? (amount >= required
               ? "&9" + amount + "/" + Math.max(1, required)
               : "&1" + amount + "&8/&1" + Math.max(1, required))
            : (amount == 0
               ? "&4\u2610"
               : "&2\u2611");
    }

    public static List<String> wrapLine(String what, int maxLineLength) {
        String[] words = what.split("\\s+");
        List<String> lines = new ArrayList<>();
        if (words.length == 0) return lines;
        StringBuilder line = new StringBuilder(words[0]);
        int lineLength = ChatColor.stripColor(words[0]).length();
        String lastColors = "";
        for (int i = 1; i < words.length; ++i) {
            String word = words[i];
            int wordLength = ChatColor.stripColor(word).length();
            if (lineLength + wordLength + 1 > maxLineLength) {
                String lineStr = lastColors + line.toString();
                lines.add(lineStr);
                lastColors = org.bukkit.ChatColor.getLastColors(lineStr);
                line = new StringBuilder(word);
                lineLength = wordLength;
            } else {
                line.append(" ");
                line.append(word);
                lineLength += wordLength + 1;
            }
        }
        if (line.length() > 0) lines.add(lastColors + line.toString());
        return lines;
    }

    public static ChatColor brighten(ChatColor color) {
        if (color == ChatColor.DARK_AQUA) return ChatColor.AQUA;
        if (color == ChatColor.DARK_BLUE) return ChatColor.BLUE;
        if (color == ChatColor.DARK_GRAY) return ChatColor.GRAY;
        if (color == ChatColor.DARK_GREEN) return ChatColor.GREEN;
        if (color == ChatColor.DARK_PURPLE) return ChatColor.LIGHT_PURPLE;
        if (color == ChatColor.DARK_RED) return ChatColor.RED;
        return color;
    }
}
