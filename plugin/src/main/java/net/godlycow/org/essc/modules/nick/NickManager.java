package net.godlycow.org.essc.modules.nick;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.storage.database.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NickManager implements Listener {

    private final EssentialsC plugin;
    private final Database database;
    private final Map<UUID, String> nickCache = new ConcurrentHashMap<>();

    private NicknameSyncHook networkHook;

    public NickManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "nicks.db");
        try {
            database.connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize nick database: " + e.getMessage());
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("NickManager initialized with nicks.db");
    }

    private void createTables() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS nicknames (
                    uuid TEXT PRIMARY KEY,
                    nickname TEXT NOT NULL,
                    updated_at INTEGER DEFAULT (strftime('%s', 'now'))
                )
            """)) {
            stmt.execute();
        }

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE INDEX IF NOT EXISTS idx_nick_name ON nicknames(nickname)"
             )) {
            stmt.execute();
        }
        plugin.debug("Nick database tables initialized");
    }

    public CompletableFuture<Optional<String>> getNickname(UUID uuid) {
        if (nickCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(Optional.ofNullable(nickCache.get(uuid)));
        }

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT nickname FROM nicknames WHERE uuid = ?"
            )) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String nick = rs.getString("nickname");
                    nickCache.put(uuid, nick);
                    return Optional.of(nick);
                }
                return Optional.<String>empty();
            }
        });
    }

    public String getCachedNickname(UUID uuid) {
        return nickCache.get(uuid);
    }

    public CompletableFuture<Boolean> setNickname(UUID uuid, String nickname) {
        plugin.debug("Setting nickname for " + uuid + " to: " + nickname);

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO nicknames (uuid, nickname, updated_at)
                VALUES (?, ?, strftime('%s', 'now'))
                ON CONFLICT(uuid) DO UPDATE SET
                    nickname = excluded.nickname,
                    updated_at = excluded.updated_at
            """)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, nickname);
                stmt.executeUpdate();

                nickCache.put(uuid, nickname);

                if (networkHook != null) {
                    networkHook.onNicknameSet(uuid, nickname);
                }

                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set nickname: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> removeNickname(UUID uuid) {
        plugin.debug("Removing nickname for " + uuid);

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM nicknames WHERE uuid = ?"
            )) {
                stmt.setString(1, uuid.toString());
                boolean removed = stmt.executeUpdate() > 0;
                if (removed) {
                    nickCache.remove(uuid);

                    if (networkHook != null) {
                        networkHook.onNicknameCleared(uuid);
                    }
                }
                return removed;
            }
        });
    }

    public CompletableFuture<Optional<UUID>> getUUIDByNickname(String nickname) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT uuid FROM nicknames WHERE nickname = ? COLLATE NOCASE"
            )) {
                stmt.setString(1, nickname);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(UUID.fromString(rs.getString("uuid")));
                }
                return Optional.<UUID>empty();
            }
        });
    }

    public CompletableFuture<Boolean> isNicknameTaken(String nickname, UUID excludeUuid) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT 1 FROM nicknames WHERE nickname = ? AND uuid != ?"
            )) {
                stmt.setString(1, nickname);
                stmt.setString(2, excludeUuid.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        });
    }

    public void applyNickname(Player player) {
        if (!plugin.getConfigManager().isNickEnabled()) return;

        getNickname(player.getUniqueId()).thenAccept(opt -> {
            player.getScheduler().run(plugin, task -> {
                if (opt.isPresent()) {
                    String nick = opt.get();

                    Component nickComponent = plugin.getMiniMessage().deserialize(nick);
                    player.displayName(nickComponent);

                    if (plugin.getConfigManager().isDiscordNickShowRealname()) {
                        String plainNick = PlainTextComponentSerializer.plainText().serialize(nickComponent);
                        String discordName = plainNick + " (" + player.getName() + ")";
                        player.setDisplayName(discordName);
                    }

                    plugin.debug("Applied nickname to " + player.getName() + ": " + nick);
                }
                if (plugin.getTabManager() != null) {
                    plugin.getTabManager().updatePlayerTab(player);
                }
            }, null);
        });
    }

    public void clearNickname(Player player) {
        player.displayName(Component.text(player.getName()));
        player.setDisplayName(player.getName());
        nickCache.remove(player.getUniqueId());

        if (plugin.getTabManager() != null) {
            plugin.getTabManager().updatePlayerTab(player);
        }

        plugin.debug("Cleared nickname for " + player.getName());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        applyNickname(event.getPlayer());
    }

    public void setNetworkHook(NicknameSyncHook hook) {
        this.networkHook = hook;
    }

    public void clearNetworkHook() {
        this.networkHook = null;
    }

    public NicknameSyncHook getNetworkHook() {
        return networkHook;
    }

    public void reload() {
        nickCache.clear();
        plugin.debug("NickManager reloaded, cache cleared");
    }

    public void shutdown() {
        database.disconnect();
        nickCache.clear();
        plugin.debug("NickManager shutdown complete");
    }
}