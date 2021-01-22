package com.cavetale.quests;

import com.cavetale.quests.session.QuestInstance;
import com.cavetale.quests.session.Session;
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
        int notAccepted = 0;
        int notClaimed = 0;
        for (QuestInstance questInstance : session.getQuests()) {
            if (questInstance.isReady() && !questInstance.getRow().isClaimed()) {
                if (!questInstance.getRow().isAccepted()) {
                    notAccepted += 1;
                }
                if (questInstance.getRow().isComplete() && !questInstance.getRow().isClaimed()) {
                    notClaimed += 1;
                }
            }
        }
        if (notClaimed > 0) {
            event.addLines(plugin, Priority.HIGH, ChatColor.GOLD + "You have a " + ChatColor.WHITE + "/quest" + ChatColor.GOLD + " reward");
        } else if (notAccepted > 0) {
            event.addLines(plugin, Priority.HIGH, ChatColor.AQUA + "You have a " + ChatColor.YELLOW + "/quest");
        }
    }
}
