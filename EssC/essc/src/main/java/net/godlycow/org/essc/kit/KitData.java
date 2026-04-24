package net.godlycow.org.essc.kit;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.impl.kit.KitImpl;
import net.godlycow.org.essc.api.kit.event.KitDataLoadEvent;
import net.godlycow.org.essc.api.kit.event.KitDataSaveEvent;
import net.godlycow.org.essc.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class KitData {
    private final EssentialsC plugin;
    private final Database database;
    private final ConcurrentHashMap<UUID, Map<String, PlayerKitData>> playerCache = new ConcurrentHashMap<>();

    public KitData(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "kits.db");
    }

    public void connect() {
        try {
            database.connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize kit database: " + e.getMessage());
        }
    }

    public void disconnect() {
        database.disconnect();
    }

    private void createTables() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE TABLE IF NOT EXISTS kit_claims (" +
                             "uuid TEXT NOT NULL, " +
                             "kit_name TEXT NOT NULL, " +
                             "last_claimed INTEGER DEFAULT 0, " +
                             "claim_count INTEGER DEFAULT 0, " +
                             "PRIMARY KEY (uuid, kit_name)" +
                             ")"
             )) {
            stmt.execute();
        }
    }

    public void loadPlayerData(UUID uuid) {
        database.async(conn -> {
            Map<String, PlayerKitData> data = new HashMap<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT kit_name, last_claimed, claim_count FROM kit_claims WHERE uuid = ?"
            )) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    data.put(rs.getString("kit_name"), new PlayerKitData(
                            rs.getLong("last_claimed"),
                            rs.getInt("claim_count")
                    ));
                }
            }
            return data;
        }).thenAccept(data -> {
            playerCache.put(uuid, data);

            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            if (playerName == null) {
                playerName = uuid.toString();
            }

            KitDataLoadEvent loadEvent = new KitDataLoadEvent(uuid, playerName, data.size());
            Bukkit.getPluginManager().callEvent(loadEvent);

            plugin.debug("Loaded kit data for " + uuid + " (" + data.size() + " entries)");
        });
    }

    public void removePlayerCache(UUID uuid) {
        playerCache.remove(uuid);
    }

    public Map<String, PlayerKitData> getPlayerData(UUID uuid) {
        Map<String, PlayerKitData> data = playerCache.getOrDefault(uuid, new HashMap<>());
        return data;
    }

    public PlayerKitData getKitData(UUID uuid, String kitName) {
        Map<String, PlayerKitData> data = playerCache.get(uuid);
        if (data == null) {
            return null;
        }
        PlayerKitData kitData = data.get(kitName);
        return kitData;
    }

    public boolean hasClaimed(UUID uuid, String kitName) {
        Map<String, PlayerKitData> data = playerCache.get(uuid);
        if (data == null) {
            return false;
        }
        PlayerKitData kitData = data.get(kitName);
        boolean claimed = kitData != null && kitData.claimCount > 0;
        return claimed;
    }

    public int getClaimCount(UUID uuid, String kitName) {
        Map<String, PlayerKitData> data = playerCache.get(uuid);
        if (data == null) {
            return 0;
        }
        PlayerKitData kitData = data.get(kitName);
        int count = kitData != null ? kitData.claimCount : 0;
        return count;
    }

    public long getLastClaimed(UUID uuid, String kitName) {
        Map<String, PlayerKitData> data = playerCache.get(uuid);
        if (data == null) {
            return 0;
        }
        PlayerKitData kitData = data.get(kitName);
        if (kitData == null) {
            return 0;
        }
        return kitData.lastClaimed;
    }

    public CompletableFuture<Void> recordClaim(UUID uuid, String kitName, long timestamp) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO kit_claims (uuid, kit_name, last_claimed, claim_count) " +
                            "VALUES (?, ?, ?, 1) " +
                            "ON CONFLICT(uuid, kit_name) DO UPDATE SET " +
                            "last_claimed = excluded.last_claimed, " +
                            "claim_count = claim_count + 1"
            )) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, kitName);
                stmt.setLong(3, timestamp);
                stmt.executeUpdate();
            }
            return null;
        }).thenRun(() -> {
            playerCache.computeIfAbsent(uuid, k -> new HashMap<>())
                    .merge(kitName, new PlayerKitData(timestamp, 1),
                            (old, newData) -> new PlayerKitData(timestamp, old.claimCount + 1));

            int newCount = getClaimCount(uuid, kitName);
            KitImpl apiKit = new KitImpl(new Kit(kitName, kitName, "", 0, false, false, 0, new java.util.ArrayList<>(), "", false));
            KitDataSaveEvent saveEvent = new KitDataSaveEvent(uuid, apiKit, timestamp, newCount);
            Bukkit.getPluginManager().callEvent(saveEvent);
        });
    }

    public void clearCache() {
        playerCache.clear();
    }
}