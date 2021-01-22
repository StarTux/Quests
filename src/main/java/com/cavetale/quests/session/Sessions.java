package com.cavetale.quests.session;

import com.cavetale.quests.QuestsPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Player sessions manager.
 */
@RequiredArgsConstructor
public final class Sessions implements Listener {
    private final QuestsPlugin plugin;
    private Map<UUID, Session> sessions = new HashMap<>();

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) {
            of(player);
        }
        Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    public void disable() {
        for (Session session : sessions.values()) {
            session.disable();
        }
        sessions.clear();
    }

    public Session of(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), u -> new Session(plugin, player).enable());
    }

    public static Sessions getInst() {
        return QuestsPlugin.getInst().getSessions();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        of(player);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    void onPlayerQuit(PlayerQuitEvent event) {
        System.out.println("Quests.Sessions.onPlayerQuit " + event.getPlayer().getName());
        Player player = event.getPlayer();
        Session session = sessions.remove(player.getUniqueId());
        if (session != null) session.disable();
    }

    void tick() {
        for (Session session : sessions.values()) {
            session.tick();
        }
    }
}
