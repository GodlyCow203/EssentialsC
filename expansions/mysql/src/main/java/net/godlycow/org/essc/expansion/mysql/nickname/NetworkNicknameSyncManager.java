package net.godlycow.org.essc.expansion.mysql.nickname;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.config.SyncConfig;
import net.godlycow.org.essc.expansion.mysql.database.SyncDatabase;
import net.godlycow.org.essc.modules.nick.NicknameSyncHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;

public class NetworkNicknameSyncManager implements NicknameSyncHook {

    private final MySQLDatabaseExpansion plugin;
    private final EssentialsC essc;
    private final SyncConfig config;
    private final NetworkNicknameDatabase db;

    private BukkitTask pollTask;

    public NetworkNicknameSyncManager(MySQLDatabaseExpansion plugin,
                                      EssentialsC essc,
                                      SyncConfig config,
                                      SyncDatabase syncDb) throws SQLException {
        this.plugin = plugin;
        this.essc = essc;
        this.config = config;
        this.db = new NetworkNicknameDatabase(syncDb, plugin.getLogger());
        this.db.createTable();
    }

    public void start() {
        long intervalTicks = config.getNicknamePollIntervalTicks();
        pollTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin, this::pollOnlinePlayers, intervalTicks, intervalTicks);
        plugin.getLogger().info("[NetworkNicknames] Poll loop started.");
    }

    private void pollOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            pullAndApply(player);
        }
    }

    private void pullAndApply(Player player) {
        UUID uuid = player.getUniqueId();

        db.fetchNickname(uuid).thenAccept(row -> {
            if (row == null) {
                return;
            }

            String networkNick = row.nickname();
            String cachedNick = essc.getNickManager().getCachedNickname(uuid);

            boolean unchanged = networkNick == null
                    ? cachedNick == null
                    : networkNick.equals(cachedNick);

            if (unchanged) {
                return;
            }

            if (networkNick == null) {
                essc.getNickManager().removeNickname(uuid).thenRun(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (player.isOnline()) {
                                essc.getNickManager().clearNickname(player);
                            }
                        })
                );
            } else {
                String nick = networkNick;
                essc.getNickManager().setNickname(uuid, nick).thenRun(() ->
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (player.isOnline()) {
                                essc.getNickManager().applyNickname(player);
                            }
                        })
                );
            }
        }).exceptionally(ex -> {
            plugin.getLogger().log(Level.WARNING,
                    "[NetworkNicknames] Failed to fetch nickname for " + uuid, ex);
            return null;
        });
    }

    public void onPlayerJoin(Player player) {
        pullAndApply(player);
    }

    @Override
    public void onNicknameSet(UUID uuid, String nickname) {
        db.upsertNickname(uuid, nickname, System.currentTimeMillis(), config.getServerId())
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING,
                            "[NetworkNicknames] Failed to push nickname set for " + uuid, ex);
                    return null;
                });
    }

    @Override
    public void onNicknameCleared(UUID uuid) {
        db.clearNickname(uuid, System.currentTimeMillis(), config.getServerId())
                .exceptionally(ex -> {
                    plugin.getLogger().log(Level.WARNING,
                            "[NetworkNicknames] Failed to push nickname clear for " + uuid, ex);
                    return null;
                });
    }

    public void shutdown() {
        if (pollTask != null) {
            pollTask.cancel();
        }
        essc.getNickManager().clearNetworkHook();
    }
}