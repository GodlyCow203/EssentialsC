package net.godlycow.org.essc.modules.punishment;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.storage.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PunishmentManager {
    private final EssentialsC plugin;
    private final Database database;

    //cached
    private final Map<UUID, BanEntry> bans = new ConcurrentHashMap<>();
    private final Map<String, IpBanEntry> ipBans = new ConcurrentHashMap<>();
    private final Map<UUID, MuteEntry> mutes = new ConcurrentHashMap<>();
    private NetworkPunishmentHook networkHook = null;

    public PunishmentManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin, "users.db");
        ensureTables();
        loadAll();
    }

    public void setNetworkHook(NetworkPunishmentHook hook) {
        this.networkHook = hook;

        plugin.debug("[PunishmentManager] Network punishment hook registered.");
    }

    public void clearNetworkHook() {
        this.networkHook = null;
    }

    public NetworkPunishmentHook getNetworkHook() {
        return networkHook;
    }

    private void ensureTables() {//new > create sqlite tables
        try (Connection conn = database.openFreshConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS punishment_bans (uuid TEXT PRIMARY KEY, name TEXT, reason TEXT, banner TEXT, time INTEGER DEFAULT 0, expires INTEGER DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS punishment_ip_bans (ip TEXT PRIMARY KEY, reason TEXT, banner TEXT, time INTEGER DEFAULT 0, expires INTEGER DEFAULT 0)");
            stmt.execute("CREATE TABLE IF NOT EXISTS punishment_mutes (uuid TEXT PRIMARY KEY, name TEXT, reason TEXT, muter TEXT, time INTEGER DEFAULT 0, expires INTEGER DEFAULT 0, offline_notification BOOLEAN DEFAULT 0)");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create punishment tables: " + e.getMessage());
        }
    }

    private void loadAll() {
        //clear old cache before reloading
        bans.clear();
        ipBans.clear();
        mutes.clear();
        try (Connection conn = database.openFreshConnection();

             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM punishment_bans")) {
            while (rs.next()) {
                try {

                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    bans.put(uuid, new BanEntry(uuid, rs.getString("name"), rs.getString("reason"),

                            rs.getString("banner"), rs.getLong("time"), rs.getLong("expires")));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load bans: " + e.getMessage());
        }

        try (Connection conn = database.openFreshConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM punishment_ip_bans")) {
            while (rs.next()) {
                ipBans.put(rs.getString("ip"), new IpBanEntry(rs.getString("ip"),
                        rs.getString("reason"), rs.getString("banner"),
                        rs.getLong("time"), rs.getLong("expires")));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load IP bans: " + e.getMessage());
        }
        try ( Connection conn = database.openFreshConnection();

             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM punishment_mutes")) {
            while (rs.next()) {
                try {
                    UUID uuid = UUID.fromString(rs.getString("uuid"));
                    mutes.put(uuid, new MuteEntry(uuid, rs.getString("name"), rs.getString("reason"),
                            rs.getString("muter"), rs.getLong("time"), rs.getLong("expires"),
                            rs.getBoolean("offline_notification")));
                } catch (IllegalArgumentException ignored) {}
            }
        } catch (SQLException e ) {

            plugin.getLogger().severe("Failed to load mutes: " + e.getMessage());
        }

        plugin.debug("PunishmentManager initialized (" + bans.size() + " bans, " + ipBans.size() + " IP bans, " + mutes.size() + " mutes)");
    }

    public void banPlayer(UUID uuid, String name, String reason, String banner, long expires) {

        BanEntry entry = new BanEntry(uuid, name, reason, banner, System.currentTimeMillis(), expires);
        bans.put(uuid, entry);
        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO punishment_bans (uuid, name, reason, banner, time, expires) VALUES (?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name);
                stmt.setString(3, reason);
                stmt.setString(4, banner);
                stmt.setLong(5, entry.time());
                stmt.setLong(6, expires);
                stmt.executeUpdate();
            }
            return null;
        });

        plugin.debug("Banned " + name + " (" + uuid + ") by " + banner + " until " + expires);

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().banPlayer(uuid, reason, banner, expires);
        }

        //sync with other servers if available
        if (networkHook != null)
            networkHook.onBan(uuid, name, reason, banner, expires);
    }

    public void unbanPlayer(UUID uuid) {
        bans.remove(uuid);

        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_bans WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            return null;
        });

        plugin.debug("Unbanned " + uuid);

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().unbanPlayer(uuid);
        }

        if (networkHook != null) networkHook.onUnban(uuid);
    }

    public boolean isBanned(UUID uuid) {
        BanEntry entry = bans.get(uuid);
        if (entry == null) return false;
        if (entry.expires() > 0 && entry.expires() < System.currentTimeMillis()) {

            bans.remove(uuid);
            database.async(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_bans WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }
                return null;
            });

            return false;
        }
        return true;
    }

    public BanEntry getBanEntry(UUID uuid) {
        if (!isBanned(uuid))
            return null;
        return bans.get(uuid);
    }

    public List<BanEntry> getActiveBans() {
        long now = System.currentTimeMillis();
        List<BanEntry> active = new ArrayList<>();

        for (BanEntry e : bans.values()) {
            if (e.expires() <= 0 || e.expires() > now) {
                active.add(e);
            }
        }
        return active;
    }

    public List<BanEntry> getAllBans() {
        return new ArrayList<>(bans.values());
    }

    public void banIp(String ip, String reason, String banner, long expires) {
        IpBanEntry entry = new IpBanEntry(ip, reason, banner, System.currentTimeMillis(),  expires);
        ipBans.put(ip, entry);
        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO punishment_ip_bans (ip, reason, banner, time, expires) VALUES (?, ?, ?, ?, ?)")) {
                stmt.setString(1, ip);
                stmt.setString(2, reason);
                stmt.setString(3, banner);
                stmt.setLong(4, entry.time());
                stmt.setLong(5, expires);
                stmt.executeUpdate();
            }

            return null;
        });

        plugin.debug("IP Banned " + ip + " by " + banner + " until " + expires);

        if (networkHook != null)
            networkHook.onIpBan(ip, reason, banner, expires);
    }

    public void unbanIp(String ip) {
        ipBans.remove(ip);
        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_ip_bans WHERE ip = ?")) {
                stmt.setString(1, ip);
                stmt.executeUpdate();
            }
            return null;
        });

        plugin.debug("Unbanned IP " + ip);

        if (networkHook != null)
            networkHook.onIpUnban(ip);
    }

    public boolean isIpBanned(String ip) {
        IpBanEntry entry = ipBans.get(ip);
        if (entry == null) return false;
        if (entry.expires() > 0 && entry.expires() < System.currentTimeMillis()) {
            ipBans.remove(ip);

            database.async(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_ip_bans WHERE ip = ?")) {
                    stmt.setString(1, ip);
                    stmt.executeUpdate();
                }
                return null;
            });

            return false;
        }

        return true;
    }

    public IpBanEntry getIpBanEntry(String ip) {
        if (!isIpBanned(ip))
            return null;

        return ipBans.get(ip);
    }

    public List<IpBanEntry> getActiveIpBans( ) {
        long now = System.currentTimeMillis();
        List<IpBanEntry> active = new ArrayList<>();
        for (IpBanEntry e : ipBans.values()) {

            if (e.expires() <= 0 || e.expires() > now) {

                active.add(e);
            }
        }
        return active;
    }

    public List<IpBanEntry> getAllIpBans() {
        return new ArrayList<>(ipBans.values());
    }

    public void mutePlayer(UUID uuid, String name, String reason, String muter, long expires) {
        mutePlayer(uuid, name, reason, muter, expires, false);
    }

    public void mutePlayer(UUID  uuid, String name, String reason,  String muter, long expires, boolean offlineNotification) {
        MuteEntry entry = new MuteEntry(uuid, name, reason, muter, System.currentTimeMillis(), expires, offlineNotification);

        mutes.put(uuid, entry);
        database.async(conn -> {

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT OR REPLACE INTO punishment_mutes (uuid, name, reason, muter, time, expires, offline_notification) VALUES (?, ?, ?, ?, ?, ?, ?)")) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name);
                stmt.setString(3, reason);
                stmt.setString(4, muter);
                stmt.setLong(5, entry.time());
                stmt.setLong(6, expires);
                stmt.setBoolean(7, offlineNotification);
                stmt.executeUpdate();
            }

            return null;

        });

        plugin.debug("Muted " + name + " by " + muter + " until " + expires);

        if (plugin.getUserManager() != null) {
            plugin.getUserManager().mutePlayer(uuid, reason, muter, expires);
            if (offlineNotification) {

                plugin.getUserManager().setMuteOfflineNotification(uuid, true);
            }
        }

        if (networkHook != null) networkHook.onMute(uuid, name, reason, muter, expires);
    }

    public void unmutePlayer(UUID uuid) {
        mutes.remove(uuid);
        database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_mutes WHERE uuid = ?")) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
            }
            return null;
        });

        plugin.debug("Unmuted " + uuid);


        if (plugin.getUserManager() != null) {
            plugin.getUserManager().unmutePlayer(uuid);
        }

        if (networkHook != null)
            networkHook.onUnmute(uuid);
    }

    public boolean isMuted(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        if (entry == null)
            return false;
        if (entry.expires() > 0 && entry.expires() < System.currentTimeMillis()) {
            mutes.remove(uuid);
            database.async(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM punishment_mutes WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }

                return null;
            });
            return false;
        }
        return true;
    }

    public MuteEntry getMuteEntry(UUID uuid) {
        if (!isMuted(uuid))
            return null;
        return mutes.get(uuid);
    }

    public boolean hasOfflineMuteNotification(UUID uuid) {
        MuteEntry entry = mutes.get(uuid);
        return entry != null && entry.offlineNotification();
    }

    public void clearOfflineMuteNotification(UUID uuid) {
        MuteEntry existing = mutes.get(uuid);
        if (existing != null) {
            MuteEntry updated = new MuteEntry(existing.uuid(), existing.name(), existing.reason(),
                    existing.muter(), existing.time(), existing.expires(), false);
            mutes.put(uuid, updated);

            database.async(conn -> {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE punishment_mutes SET offline_notification = 0 WHERE uuid = ?")) {
                    stmt.setString(1, uuid.toString());
                    stmt.executeUpdate();
                }

                return null;
            });
        }
    }

    public List<MuteEntry> getAllMutes() {
        return new ArrayList<>(mutes.values());
    }

    public void shutdown() {
        networkHook = null;

        plugin.debug("Shutting down the Punishment Manager");
    }

    public record BanEntry(UUID uuid, String name, String reason, String banner, long time, long expires) {}
    public record IpBanEntry(String ip, String reason, String banner, long time, long expires) {}
    public record MuteEntry(UUID uuid, String name, String reason, String muter, long time, long expires, boolean offlineNotification) {}
}
