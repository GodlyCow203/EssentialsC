package net.godlycow.org.essc.expansion.mysql.sync;

import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SyncListener implements Listener {

    private final MySQLDatabaseExpansion plugin;
    private final BalanceSyncManager syncManager;

    private final ConcurrentHashMap<UUID, String> onlinePlayers = new ConcurrentHashMap<>();

    public SyncListener(MySQLDatabaseExpansion plugin, BalanceSyncManager syncManager) {
        this.plugin = plugin;
        this.syncManager = syncManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        onlinePlayers.put(uuid, name);
        syncManager.onPlayerJoin(player);

        var nickSyncManager = plugin.getNicknameSyncManager();
        if (nickSyncManager != null) {
            nickSyncManager.onPlayerJoin(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        onlinePlayers.remove(uuid);
        syncManager.onPlayerQuit(player);
    }

    public Set<UUID> getOnlinePlayers() {
        return Collections.unmodifiableSet(onlinePlayers.keySet());
    }

    public void shutdown() {
        onlinePlayers.clear();
    }
}