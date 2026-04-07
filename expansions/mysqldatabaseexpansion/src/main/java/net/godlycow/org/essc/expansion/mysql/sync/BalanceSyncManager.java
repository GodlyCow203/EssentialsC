package net.godlycow.org.essc.expansion.mysql.sync;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class BalanceSyncManager {

    private final MySQLDatabaseExpansion plugin;
    private final EssentialsC essc;
    private final SyncConfig config;
    private final SyncDatabase db;
    private BukkitTask pushTask;
    private BukkitTask pullTask;
    private long lastPullTime = 0;
    private long lastOfflineCheck = 0;
    private final Map<UUID, BigDecimal> lastKnownBalances = new ConcurrentHashMap<>();
    private final Set<UUID> onlineOnThisServer = ConcurrentHashMap.newKeySet();

    public BalanceSyncManager(MySQLDatabaseExpansion plugin, EssentialsC essc, SyncConfig config) {
        this.plugin = plugin;
        this.essc = essc;
        this.config = config;
        this.db = new SyncDatabase(config, plugin.getLogger());
    }

    public void init() throws SQLException {
        db.connect();
        lastPullTime = System.currentTimeMillis();
        lastOfflineCheck = (System.currentTimeMillis() / 1000) - 5;
        pushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pushCycle, 20L, 20L);
        pullTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::pullCycle, 20L, 20L);

        plugin.getLogger().info("[MySQLExpansion] Aggressive sync started - 1 second intervals");
    }

    public void shutdown() {
        if (pushTask != null) pushTask.cancel();
        if (pullTask != null) pullTask.cancel();

        for (UUID uuid : onlineOnThisServer) {
            try {
                doPush(uuid, true);
            } catch (Exception e) {
            }
        }

        db.disconnect();
    }

    public void onPlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        onlineOnThisServer.add(uuid);

        db.fetchBalance(uuid).thenAccept(remoteOpt -> {
            essc.getEconomyManager().getBalance(uuid).thenAccept(localBalance -> {
                if (remoteOpt.isPresent()) {
                    BigDecimal remote = remoteOpt.get();
                    if (remote.compareTo(localBalance) != 0) {
                        essc.getEconomyManager().setBalance(uuid, remote).thenRun(() -> {
                            lastKnownBalances.put(uuid, remote);
                            plugin.getLogger().info("[MySQLExpansion] Join sync: " + name + " updated to " + remote);
                        });
                    } else {
                        lastKnownBalances.put(uuid, localBalance);
                    }
                } else {
                    lastKnownBalances.put(uuid, localBalance);
                    db.pushBalance(uuid, name, localBalance, config.getServerId());
                }
            });
        });
    }

    public void onPlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        onlineOnThisServer.remove(uuid);
        pushNow(uuid);
    }

    public void pushNow(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> doPush(uuid, false));
    }

    private void pushCycle() {
        try {
            for (UUID uuid : onlineOnThisServer) {
                doPush(uuid, false);
            }

            pushOfflineChanges();

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[MySQLExpansion] Push cycle error", e);
        }
    }

    private void pullCycle() {
        try {
            long since = lastPullTime;
            lastPullTime = System.currentTimeMillis();

            db.fetchUpdatedSince(since, config.getServerId()).thenAccept(remoteChanges -> {
                if (remoteChanges.isEmpty()) return;

                plugin.getLogger().fine("[MySQLExpansion] Pulling " + remoteChanges.size() + " remote updates");

                for (Map.Entry<UUID, BigDecimal> entry : remoteChanges.entrySet()) {
                    UUID uuid = entry.getKey();
                    BigDecimal newBalance = entry.getValue();

                    essc.getEconomyManager().setBalance(uuid, newBalance).thenAccept(success -> {
                        if (success) {
                            lastKnownBalances.put(uuid, newBalance);

                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null && onlineOnThisServer.contains(uuid)) {
                                plugin.getLogger().fine("[MySQLExpansion] Updated " + player.getName() + " to " + newBalance);
                            }
                        }
                    });
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.WARNING, "[MySQLExpansion] Pull failed", ex);
                return null;
            });

        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "[MySQLExpansion] Pull cycle error", e);
        }
    }

    private void doPush(UUID uuid, boolean force) {
        try {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null && !force) return;

            String name = player != null ? player.getName() : Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString();

            String finalName = name;
            essc.getEconomyManager().getBalance(uuid).thenAccept(balance -> {
                BigDecimal lastKnown = lastKnownBalances.get(uuid);

                if (force || lastKnown == null || lastKnown.compareTo(balance) != 0) {
                    db.pushBalance(uuid, finalName, balance, config.getServerId())
                            .thenRun(() -> lastKnownBalances.put(uuid, balance))
                            .exceptionally(ex -> {
                                plugin.getLogger().log(Level.FINE, "[MySQLExpansion] Push failed for " + uuid, ex);
                                return null;
                            });
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.FINE, "[MySQLExpansion] Failed to get balance for push", ex);
                return null;
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.FINE, "[MySQLExpansion] Push error", e);
        }
    }


    private void pushOfflineChanges() {
        long windowEnd = System.currentTimeMillis() / 1000;
        long windowStart = lastOfflineCheck;

        if (windowStart >= windowEnd) return;

        essc.getEconomyManager().getDatabase().async(conn -> {
            List<AccountSnapshot> changed = new ArrayList<>();

            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT uuid, username, balance FROM economy WHERE last_updated > ? AND last_updated <= ?")) {

                ps.setLong(1, windowStart);
                ps.setLong(2, windowEnd);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));

                    if (onlineOnThisServer.contains(uuid)) continue;

                    String username = rs.getString("username");
                    BigDecimal balance = BigDecimal.valueOf(rs.getDouble("balance"));

                    BigDecimal lastKnown = lastKnownBalances.get(uuid);
                    if (lastKnown == null || lastKnown.compareTo(balance) != 0) {
                        changed.add(new AccountSnapshot(uuid, username, balance));
                    }
                }
            }
            return changed;

        }).thenAccept(changed -> {
            if (!changed.isEmpty()) {
                plugin.getLogger().info("[MySQLExpansion] Pushing " + changed.size() + " offline player change(s)");

                for (AccountSnapshot acc : changed) {
                    db.pushBalance(acc.uuid, acc.username, acc.balance, config.getServerId())
                            .thenRun(() -> lastKnownBalances.put(acc.uuid, acc.balance))
                            .exceptionally(ex -> {
                                plugin.getLogger().log(Level.WARNING, "[MySQLExpansion] Failed to push offline change for " + acc.uuid, ex);
                                return null;
                            });
                }
            }

            lastOfflineCheck = windowEnd;

        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.SEVERE, "[MySQLExpansion] Offline push query failed", ex);
            return null;
        });
    }

    public SyncDatabase getDatabase() {
        return db;
    }

    public long getLastPollTime() {
        return lastPullTime;
    }

    private record AccountSnapshot(UUID uuid, String username, BigDecimal balance) {}
}