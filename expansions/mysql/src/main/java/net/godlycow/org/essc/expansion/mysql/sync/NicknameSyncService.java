package net.godlycow.org.essc.expansion.mysql.sync;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;
import net.godlycow.org.essc.expansion.mysql.config.ExpansionConfig;
import net.godlycow.org.essc.expansion.mysql.storage.ConnectionPool;
import net.godlycow.org.essc.expansion.mysql.storage.SchemaManager;
import net.godlycow.org.essc.modules.nick.NickManager;
import net.godlycow.org.essc.modules.nick.NicknameSyncHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class NicknameSyncService implements NicknameSyncHook {


    private final MySQLExpansion plugin;
    private final EssentialsC essentialsC;
    private final ConnectionPool pool;
    private final SchemaManager schema;
    private final ExpansionConfig config;
    private final String serverId;

    private final Set<UUID> applyingRemote = ConcurrentHashMap.newKeySet(); //skip echo
    private volatile long watermark = 0L;
    private boolean enabled = false;
    private ScheduledTask mirrorTask;

    public NicknameSyncService(MySQLExpansion plugin, EssentialsC essentialsC, ConnectionPool pool,
                               SchemaManager schema, ExpansionConfig config, String serverId) {
        this.plugin = plugin;
        this.essentialsC = essentialsC;
        this.pool = pool;
        this.schema = schema;
        this.config = config;
        this.serverId = serverId;
    }

    public void init() {
        if (!config.isNicknamesEnabled()) {
            enabled = false;

            return;
        }
        NickManager manager = essentialsC.getNickManager();
        if (manager == null) {
            enabled = false;
            plugin.getLogger().warning("Nickname module is disabled in EssentialsC, nickname sync will not run !!!");

            return;
        }
        manager.setNetworkHook(this);
        enabled = true;
        plugin.getLogger().info("Nickname network sync initialised (server-id: " + serverId + ")");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void start() {

        if (!enabled) {
            return;
        }

        int intervalInSeconds = config.getNicknameMirrorIntervalSeconds();

        mirrorTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> mirror(), intervalInSeconds, intervalInSeconds, TimeUnit.SECONDS);
        plugin.debug("Scheduled nickname mirror every " +  intervalInSeconds + "s");
    }

    public void stop() {
        if (mirrorTask != null) {
            mirrorTask.cancel();
            mirrorTask = null;
        }

        NickManager manager = essentialsC.getNickManager();
        //dont unhook a never service
        if (manager  != null && manager.getNetworkHook() == this) {
            manager.clearNetworkHook();
        }


        enabled = false;
    }

    @Override
    public void onNicknameSet(UUID uuid, String nickname) {
        if (!enabled || applyingRemote.contains(uuid)) {
            return;
        }
        push( uuid,nickname );
    }

    @Override
    public void onNicknameCleared(UUID uuid) {
        if (!enabled || applyingRemote.contains(uuid)) {
            return;
        }
        push(uuid, null);
    }

    private CompletableFuture<Integer> push( UUID uuid, String nickname) {

        String sql = "INSERT INTO " + schema.nicknamesTable()
                + " (uuid, nickname, server_id, last_updated) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE nickname = VALUES(nickname), "
                + "server_id = VALUES(server_id), last_updated = VALUES(last_updated)";

        return pool.update(sql, uuid.toString(), nickname, serverId, System.currentTimeMillis())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        plugin.getLogger().warning("Failed to push nickname for " + uuid + ": " + error.getMessage());
                    } else {
                        plugin.debug("Pushed nickname for " + uuid + ": " + (nickname == null ? "<cleared>" : nickname));
                    }

                });

    }

    public void onJoin(Player player) {
        if (!enabled || !config.isNicknamePullOnJoin()) {
            return;
        }


        UUID  uuid = player.getUniqueId();
        String sql = "SELECT nickname FROM " + schema.nicknamesTable() + " WHERE uuid = ?";
        pool.query(sql, rs -> rs.next() ? new NickRow(uuid, rs.getString("nickname"), 0L) : null, uuid.toString())
                .thenAccept(row -> {
                    if (row != null) {
                        applyLocally(row);
                    }
                })
                .exceptionally(error -> {
                    plugin.getLogger().warning("Failed to pull nickname for " + player.getName() + ": " + error.getMessage());
                    return null;

                });
    }

    private void mirror() {
        if (!enabled) {
            return;
        }
        String sql = "SELECT uuid, nickname, last_updated FROM " + schema.nicknamesTable()
                + " WHERE last_updated > ? AND server_id <> ? ORDER BY last_updated";
        pool.query(sql, rs -> {


            List<NickRow> rows = new ArrayList<>();

            while (rs.next()) {
                try {
                    rows.add(new NickRow(UUID.fromString(rs.getString("uuid")),
                            rs.getString("nickname"), rs.getLong("last_updated")));
                } catch (IllegalArgumentException ignored) {
                }

            }

            return rows;


        }, watermark, serverId).thenAccept(rows -> {
            for (NickRow row : rows) {
                applyLocally(row);
                watermark = Math.max(watermark, row.updated());
            }
        }).exceptionally(error -> {
            plugin.getLogger().warning("Failed to mirror network nicknames: " + error.getMessage());
            return null;
        });
    }

    private void applyLocally( NickRow row) {
        NickManager manager = essentialsC.getNickManager();
        if (manager == null) {
            return;
        }
        UUID uuid = row.uuid();

        manager.getNickname(uuid).thenCompose(current -> {
            String local = current.orElse(null);


            if (Objects.equals(local, row.nickname())) {
                return CompletableFuture.completedFuture(false);
            }
            //hook fires inside the write, so this covers it
            applyingRemote.add(uuid);
            CompletableFuture<Boolean> change = row.nickname() == null? manager.removeNickname(uuid) : manager.setNickname(uuid, row.nickname());

            return change.whenComplete((ok, error)
                    -> applyingRemote.remove(uuid)).thenApply(ok -> true);


        }).thenAccept(changed -> {

            if (!changed) {
                return;
            }

            plugin.debug("Applied network nickname for " + uuid + ": " + ( row.nickname() == null ? "<cleared>" :   row.nickname()));
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                return;
            }

            if (row.nickname() == null) {
                player.getScheduler().run(plugin, task -> manager.clearNickname(player), null);
            } else {
                manager.applyNickname(player);
            }
        }).exceptionally(error -> {
            plugin.getLogger().warning("Failed to apply network nickname for " + uuid + ": " + error.getMessage());
            return null;
        });
    }


    private record NickRow(UUID uuid, String nickname, long updated) {
    }
}
