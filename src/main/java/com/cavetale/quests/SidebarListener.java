package com.cavetale.quests;

import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.session.Session;
import com.cavetale.quests.util.Text;
import com.cavetale.sidebar.PlayerSidebarEvent;
import com.cavetale.sidebar.Priority;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public final class SidebarListener implements Listener {
    private final QuestsPlugin plugin;

    @EventHandler
    void onPlayerSidebar(PlayerSidebarEvent event) {
        if (!(event.getPlayer().hasPermission("quests.quests"))) return;
        Session session = plugin.sessions.of(event.getPlayer());
        if (!session.isReady()) return;
        int notSeen = 0;
        int notClaimed = 0;
        QuestInstance focus = null;
        for (QuestInstance questInstance : session.getQuests()) {
            if (questInstance.isReady() && !questInstance.getRow().isClaimed()) {
                if (!questInstance.getRow().isSeen()) {
                    notSeen += 1;
                }
                if (questInstance.getRow().isComplete() && !questInstance.getRow().isClaimed()) {
                    notClaimed += 1;
                }
                if (!questInstance.getRow().isComplete() && questInstance.getRow().isAccepted() && questInstance.getRow().isFocus()) {
                    focus = questInstance;
                }
            }
        }
        if (focus != null) {
            String longLine =  ChatColor.GOLD + "Quest: " + ChatColor.GRAY + focus.getCurrentGoal().getDescription();
            event.addLines(plugin, Priority.DEFAULT, Text.wrapLine(longLine, 18));
        } else if (notClaimed > 0) {
            event.addLines(plugin, Priority.HIGH, ChatColor.GOLD + "You have a " + ChatColor.WHITE + "/quest" + ChatColor.GOLD + " reward");
        } else if (notSeen > 0) {
            event.addLines(plugin, Priority.HIGH, ChatColor.AQUA + "You have a new " + ChatColor.YELLOW + "/quest");
        }
    }
}
