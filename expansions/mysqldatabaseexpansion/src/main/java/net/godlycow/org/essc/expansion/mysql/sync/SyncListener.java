package net.godlycow.org.essc.expansion.mysql.sync;

import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SyncListener implements Listener {

    private final MySQLDatabaseExpansion plugin;
    private final BalanceSyncManager syncManager;
    private final ConcurrentMap<UUID, java.math.BigDecimal> lastKnownBalance = new ConcurrentHashMap<>();
    private static final long SNAPSHOT_INTERVAL_TICKS = 40L;

    private BukkitTask snapshotTask;

    public SyncListener(MySQLDatabaseExpansion plugin, BalanceSyncManager syncManager) {
        this.plugin      = plugin;
        this.syncManager = syncManager;
        startSnapshotTask();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        syncManager.onPlayerJoin(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        lastKnownBalance.remove(uuid);
        syncManager.pushNow(uuid);
    }

    private void startSnapshotTask() {
        snapshotTask = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : org.bukkit.Bukkit.getOnlinePlayers()) {
                    UUID uuid = player.getUniqueId();
                    plugin.getEssentialsC().getEconomyManager()
                            .getBalance(uuid)
                            .thenAccept(current -> {
                                java.math.BigDecimal last = lastKnownBalance.get(uuid);
                                if (last == null) {
                                    lastKnownBalance.put(uuid, current);
                                    return;
                                }
                                if (current.compareTo(last) != 0) {
                                    lastKnownBalance.put(uuid, current);
                                    syncManager.schedulePush(uuid);
                                }
                            });
                }
            }
        }.runTaskTimerAsynchronously(plugin, SNAPSHOT_INTERVAL_TICKS, SNAPSHOT_INTERVAL_TICKS);
    }

    public void shutdown() {
        if (snapshotTask != null) snapshotTask.cancel();
        lastKnownBalance.clear();
    }
}