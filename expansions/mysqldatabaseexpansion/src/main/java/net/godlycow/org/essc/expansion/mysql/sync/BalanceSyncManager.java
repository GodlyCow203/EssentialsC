package net.godlycow.org.essc.expansion.mysql.sync;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BalanceSyncManager {

    private final MySQLDatabaseExpansion plugin;
    private final EssentialsC essc;
    private final SyncConfig config;
    private final SyncDatabase db;
    private volatile long lastPollTime = 0L;
    private final Map<UUID, BukkitTask> pendingPush = new ConcurrentHashMap<>();

    private BukkitTask reconcileTask;

    public BalanceSyncManager(MySQLDatabaseExpansion plugin, EssentialsC essc, SyncConfig config) {
        this.plugin = plugin;
        this.essc   = essc;
        this.config = config;
        this.db     = new SyncDatabase(config, plugin.getLogger());
    }

    public void init() throws SQLException {
        db.connect();
        lastPollTime = System.currentTimeMillis();
        startReconcileTask();
        plugin.getLogger().info("[MySQLExpansion] Sync manager started. Poll interval: "
                + config.getPollIntervalTicks() + " ticks, debounce: "
                + config.getPushDebounceMs() + " ms");
    }

    public void shutdown() {
        if (reconcileTask != null) reconcileTask.cancel();
        pendingPush.values().forEach(BukkitTask::cancel);
        pendingPush.clear();
        db.disconnect();
    }

    public void schedulePush(UUID uuid) {
        BukkitTask existing = pendingPush.remove(uuid);
        if (existing != null) existing.cancel();

        long delayTicks = Math.max(1L, config.getPushDebounceMs() / 50L);

        BukkitTask task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            pendingPush.remove(uuid);
            doPush(uuid);
        }, delayTicks);

        pendingPush.put(uuid, task);
    }

    public void pushNow(UUID uuid) {
        BukkitTask existing = pendingPush.remove(uuid);
        if (existing != null) existing.cancel();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> doPush(uuid));
    }

    private void doPush(UUID uuid) {
        essc.getEconomyManager().getBalance(uuid).thenAccept(balance -> {
            Player player = Bukkit.getPlayer(uuid);
            String name = player != null ? player.getName()
                    : (Bukkit.getOfflinePlayer(uuid).getName() != null
                    ? Bukkit.getOfflinePlayer(uuid).getName()
                    : uuid.toString());

            db.pushBalance(uuid, name, balance, config.getServerId())
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.WARNING,
                                "[MySQLExpansion] Failed to push balance for " + uuid, ex);
                        return null;
                    });
        });
    }

    private void startReconcileTask() {
        reconcileTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long since = lastPollTime;
            lastPollTime = System.currentTimeMillis();

            db.fetchUpdatedSince(since, config.getServerId())
                    .thenAccept(this::applyRemoteChanges)
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.WARNING,
                                "[MySQLExpansion] Reconcile poll failed", ex);
                        return null;
                    });

        }, config.getPollIntervalTicks(), config.getPollIntervalTicks());
    }

    private void applyRemoteChanges(Map<UUID, BigDecimal> changes) {
        if (changes.isEmpty()) return;

        plugin.getLogger().info("[MySQLExpansion] Reconcile: applying " + changes.size() + " remote balance(s).");

        for (Map.Entry<UUID, BigDecimal> entry : changes.entrySet()) {
            UUID uuid = entry.getKey();
            BigDecimal remoteBalance = entry.getValue();

            essc.getEconomyManager().setBalance(uuid, remoteBalance).thenAccept(success -> {
                if (!success) {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    if (name != null) {
                        essc.getEconomyManager().createAccount(uuid, name).thenRun(() ->
                                essc.getEconomyManager().setBalance(uuid, remoteBalance));
                    }
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.WARNING,
                        "[MySQLExpansion] Failed to apply remote balance for " + uuid, ex);
                return null;
            });
        }
    }

    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();

        db.fetchBalance(uuid).thenAccept(remoteOpt -> {
            if (remoteOpt.isPresent()) {
                BigDecimal remoteBalance = remoteOpt.get();
                essc.getEconomyManager().setBalance(uuid, remoteBalance).thenRun(() ->
                        plugin.getLogger().info("[MySQLExpansion] Synced " + player.getName()
                                + " balance from MySQL: " + remoteBalance));
            } else {
                essc.getEconomyManager().getBalance(uuid).thenAccept(localBalance ->
                        db.ensureAccount(uuid, player.getName(), localBalance, config.getServerId()));
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.WARNING,
                    "[MySQLExpansion] Join sync failed for " + player.getName(), ex);
            return null;
        });
    }

    public SyncDatabase getDatabase() {
        return db;
    }

    public long getLastPollTime() {
        return lastPollTime;
    }
}