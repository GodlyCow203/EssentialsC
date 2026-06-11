package net.godlycow.org.essc.storage.user;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.storage.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UserDatabase implements UserRepo {
    private final Database database;

    private static final String CREATE_USERS_TABLE = """
            CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                last_known_name TEXT,
                first_join_time INTEGER DEFAULT 0,
                last_join_time INTEGER DEFAULT 0,
                last_ip TEXT,
                logout_location TEXT,
                logout_time INTEGER DEFAULT 0,
                language_code TEXT,
                back_location TEXT,
                death_location TEXT,
                fly_enabled BOOLEAN DEFAULT FALSE,
                vanished BOOLEAN DEFAULT FALSE,
                tpa_blocked BOOLEAN DEFAULT FALSE,
                last_reply_target TEXT,
                rtp_last_used INTEGER DEFAULT 0,
                spawn_last_teleport INTEGER DEFAULT 0,
                ban_reason TEXT,
                ban_banner TEXT,
                ban_time INTEGER DEFAULT 0,
                ban_expires INTEGER DEFAULT 0,
                mute_reason TEXT,
                mute_muter TEXT,
                mute_time INTEGER DEFAULT 0,
                mute_expires INTEGER DEFAULT 0,
                mute_offline_notification BOOLEAN DEFAULT FALSE,
                scoreboard_disabled BOOLEAN DEFAULT FALSE,
                rules_accepted BOOLEAN DEFAULT FALSE,
                created_at INTEGER DEFAULT (strftime('%s','now')),
                updated_at INTEGER DEFAULT (strftime('%s','now'))
            );
            """;

    private static final String CREATE_IGNORED_TABLE = """
            CREATE TABLE IF NOT EXISTS user_ignored_players (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                ignored_uuid TEXT NOT NULL,
                ignored_name TEXT,
                UNIQUE(uuid, ignored_uuid)
            );
            """;

    private static final String CREATE_IP_HISTORY_TABLE = """
            CREATE TABLE IF NOT EXISTS user_ip_history (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                uuid TEXT NOT NULL,
                ip TEXT NOT NULL,
                recorded_at INTEGER DEFAULT (strftime('%s','now'))
            );
            """;

    private static final String CREATE_INVENTORY_TABLE = """
            CREATE TABLE IF NOT EXISTS user_inventories (
                uuid TEXT PRIMARY KEY,
                inventory_data TEXT NOT NULL,
                saved_at INTEGER DEFAULT (strftime('%s','now'))
            );
            """;

    public UserDatabase(EssentialsC plugin) {
        this.database = new Database(plugin, "users.db");
        initialize();
    }

    public Database getDatabase() {
        return database;
    }

    private void initialize() {
        try (Connection conn = database.getConnection(); Statement statement = conn.createStatement()) {
            statement.execute(CREATE_USERS_TABLE);
            statement.execute(CREATE_IGNORED_TABLE);
            statement.execute(CREATE_IP_HISTORY_TABLE);
            statement.execute(CREATE_INVENTORY_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize user database", e);
        }
    }

    @Override
    public java.util.concurrent.CompletableFuture<UserProfile> findOrCreate(UUID uuid, String username) {
        return findByUuid(uuid).thenCompose(profile -> {
            if (profile != null) {
                return java.util.concurrent.CompletableFuture.completedFuture(profile);
            }
            long now = Instant.now().getEpochSecond();
            UserProfile created = UserProfile.createDefault(uuid, username, now);
            return save(created).thenApply(success -> created);
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<UserProfile> findByUuid(UUID uuid) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        return null;
                    }
                    return mapRow(resultSet);
                }
            }
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<Boolean> save(UserProfile profile) {
        return database.async(connection -> {
            long now = Instant.now().getEpochSecond();
            profile.setUpdatedAt(now);
            if (profile.getCreatedAt() <= 0) {
                profile.setCreatedAt(now);
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (uuid, username, last_known_name, first_join_time, last_join_time, last_ip, logout_location, logout_time, language_code, back_location, death_location, fly_enabled, vanished, tpa_blocked, last_reply_target, rtp_last_used, spawn_last_teleport, ban_reason, ban_banner, ban_time, ban_expires, mute_reason, mute_muter, mute_time, mute_expires, mute_offline_notification, scoreboard_disabled, rules_accepted, created_at, updated_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                            "ON CONFLICT(uuid) DO UPDATE SET " +
                            "username = excluded.username, " +
                            "last_known_name = excluded.last_known_name, " +
                            "first_join_time = excluded.first_join_time, " +
                            "last_join_time = excluded.last_join_time, " +
                            "last_ip = excluded.last_ip, " +
                            "logout_location = excluded.logout_location, " +
                            "logout_time = excluded.logout_time, " +
                            "language_code = excluded.language_code, " +

                            "back_location = excluded.back_location, " +
                            "death_location = excluded.death_location, " +
                            "fly_enabled = excluded.fly_enabled, " +
                            "vanished = excluded.vanished, " +
                            "tpa_blocked = excluded.tpa_blocked, " +
                            "last_reply_target = excluded.last_reply_target, " +
                            "rtp_last_used = excluded.rtp_last_used, " +
                            "spawn_last_teleport = excluded.spawn_last_teleport, " +
                            "ban_reason = excluded.ban_reason, " +
                            "ban_banner = excluded.ban_banner, " +
                            "ban_time = excluded.ban_time, " +
                            "ban_expires = excluded.ban_expires, " +
                            "mute_reason = excluded.mute_reason, " +
                            "mute_muter = excluded.mute_muter, " +
                            "mute_time = excluded.mute_time, " +
                            "mute_expires = excluded.mute_expires, " +
                            "mute_offline_notification = excluded.mute_offline_notification, " +
                            "scoreboard_disabled = excluded.scoreboard_disabled, " +
                            "rules_accepted = excluded.rules_accepted, " +
                            "created_at = excluded.created_at, " +
                            "updated_at = excluded.updated_at")) {
                int index = 1;
                statement.setString(index++, profile.getUuid().toString());
                statement.setString(index++, profile.getUsername());
                statement.setString(index++, profile.getLastKnownName());
                statement.setLong(index++, profile.getFirstJoinTime());
                statement.setLong(index++, profile.getLastJoinTime());
                statement.setString(index++, profile.getLastIp());
                statement.setString(index++, profile.getRawLogoutLocation());
                statement.setLong(index++, profile.getLogoutTime());
                statement.setString(index++, profile.getLanguageCode());
                statement.setString(index++, profile.getRawBackLocation());
                statement.setString(index++, profile.getRawDeathLocation());
                statement.setBoolean(index++, profile.isFlyEnabled());
                statement.setBoolean(index++, profile.isVanished());
                statement.setBoolean(index++, profile.isTpaBlocked());
                statement.setString(index++, profile.getRawLastReplyTarget());
                statement.setLong(index++, profile.getRtpLastUsed());
                statement.setLong(index++, profile.getSpawnLastTeleport());
                statement.setString(index++, profile.getBanReason());
                statement.setString(index++, profile.getBanBanner());
                statement.setLong(index++, profile.getBanTime());
                statement.setLong(index++, profile.getBanExpires());
                statement.setString(index++, profile.getMuteReason());
                statement.setString(index++, profile.getMuteMuter());
                statement.setLong(index++, profile.getMuteTime());
                statement.setLong(index++, profile.getMuteExpires());
                statement.setBoolean(index++, profile.isMuteOfflineNotification());
                statement.setBoolean(index++, profile.isScoreboardDisabled());
                statement.setBoolean(index++, profile.isRulesAccepted());
                statement.setLong(index++, profile.getCreatedAt());
                statement.setLong(index, profile.getUpdatedAt());
                return statement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<Boolean> recordIp(UUID uuid, String ip) {
        return database.async(connection -> {
            try (PreparedStatement insert = connection.prepareStatement("INSERT INTO user_ip_history (uuid, ip) VALUES (?, ?)");
                 PreparedStatement update = connection.prepareStatement("UPDATE users SET last_ip = ?, updated_at = ? WHERE uuid = ?")) {
                insert.setString(1, uuid.toString());
                insert.setString(2, ip);
                insert.executeUpdate();

                update.setString(1, ip);
                update.setLong(2, Instant.now().getEpochSecond());
                update.setString(3, uuid.toString());
                update.executeUpdate();
                return true;
            }
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<Set<UUID>> getIgnoredPlayers(UUID uuid) {
        return database.async(connection -> {
            Set<UUID> ignoredPlayers = new HashSet<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT ignored_uuid FROM user_ignored_players WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        try {
                            ignoredPlayers.add(UUID.fromString(resultSet.getString("ignored_uuid")));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
            return ignoredPlayers;
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<Boolean> addIgnoredPlayer(UUID uuid, UUID ignoredUuid, String ignoredName) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT OR IGNORE INTO user_ignored_players (uuid, ignored_uuid, ignored_name) VALUES (?, ?, ?)") ) {
                statement.setString(1, uuid.toString());
                statement.setString(2, ignoredUuid.toString());
                statement.setString(3, ignoredName);
                return statement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<Boolean> removeIgnoredPlayer(UUID uuid, UUID ignoredUuid) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM user_ignored_players WHERE uuid = ? AND ignored_uuid = ?")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, ignoredUuid.toString());
                return statement.executeUpdate() > 0;
            }
        });
    }

    @Override
    public java.util.concurrent.CompletableFuture<List<String>> getIpHistory(UUID uuid) {
        return database.async(connection -> {
            List<String> entries = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement("SELECT ip FROM user_ip_history WHERE uuid = ? ORDER BY recorded_at DESC")) {
                statement.setString(1, uuid.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        entries.add(resultSet.getString("ip"));
                    }
                }
            }
            return entries;
        });
    }

    private UserProfile mapRow(ResultSet row) throws SQLException {
        UUID uuid = UUID.fromString(row.getString("uuid"));
        UserProfile profile = new UserProfile(uuid, row.getString("username"));
        profile.setLastKnownName(row.getString("last_known_name"));
        profile.setFirstJoinTime(row.getLong("first_join_time"));
        profile.setLastJoinTime(row.getLong("last_join_time"));
        profile.setLastIp(row.getString("last_ip"));
        profile.setRawLogoutLocation(row.getString("logout_location"));
        profile.setLogoutTime(row.getLong("logout_time"));
        profile.setLanguageCode(row.getString("language_code"));
        profile.setRawBackLocation(row.getString("back_location"));
        profile.setRawDeathLocation(row.getString("death_location"));
        profile.setFlyEnabled(row.getBoolean("fly_enabled"));
        profile.setVanished(row.getBoolean("vanished"));
        profile.setTpaBlocked(row.getBoolean("tpa_blocked"));
        profile.setRawLastReplyTarget(row.getString("last_reply_target"));
        profile.setRtpLastUsed(row.getLong("rtp_last_used"));
        profile.setSpawnLastTeleport(row.getLong("spawn_last_teleport"));
        profile.setBanReason(row.getString("ban_reason"));
        profile.setBanBanner(row.getString("ban_banner"));
        profile.setBanTime(row.getLong("ban_time"));
        profile.setBanExpires(row.getLong("ban_expires"));
        profile.setMuteReason(row.getString("mute_reason"));
        profile.setMuteMuter(row.getString("mute_muter"));
        profile.setMuteTime(row.getLong("mute_time"));
        profile.setMuteExpires(row.getLong("mute_expires"));
        profile.setMuteOfflineNotification(row.getBoolean("mute_offline_notification"));
        profile.setScoreboardDisabled(row.getBoolean("scoreboard_disabled"));
        profile.setRulesAccepted(row.getBoolean("rules_accepted"));
        profile.setCreatedAt(row.getLong("created_at"));
        profile.setUpdatedAt(row.getLong("updated_at"));
        return profile;
    }

    public java.util.concurrent.CompletableFuture<Boolean> updateFlyEnabled(UUID uuid, boolean flyEnabled) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "UPDATE users SET fly_enabled = ?, updated_at = ? WHERE uuid = ?")) {
                statement.setBoolean(1, flyEnabled);
                statement.setLong(2, Instant.now().getEpochSecond());
                statement.setString(3, uuid.toString());
                return statement.executeUpdate() > 0;
            }
        });
    }

    public java.util.concurrent.CompletableFuture<Boolean> saveInventory(UUID uuid, String base64) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO user_inventories (uuid, inventory_data, saved_at) VALUES (?, ?, strftime('%s','now')) " +
                            "ON CONFLICT(uuid) DO UPDATE SET inventory_data = excluded.inventory_data, saved_at = excluded.saved_at")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, base64);
                return statement.executeUpdate() > 0;
            }
        });
    }

    public java.util.concurrent.CompletableFuture<String> loadInventory(UUID uuid) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT inventory_data FROM user_inventories WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                try (ResultSet rs = statement.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("inventory_data");
                    }
                    return null;
                }
            }
        });
    }

    public java.util.concurrent.CompletableFuture<Boolean> deleteInventory(UUID uuid) {
        return database.async(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM user_inventories WHERE uuid = ?")) {
                statement.setString(1, uuid.toString());
                return statement.executeUpdate() > 0;
            }
        });
    }
}