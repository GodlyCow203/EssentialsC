package net.godlycow.org.essc.warp;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class WarpManager {
    private final EssentialsC plugin;
    private Connection connection;
    private final File databaseFile;
    private final Map<String, Warp> warpCache = new ConcurrentHashMap<>();
    private final Map<UUID, Warp> pendingWarps = new ConcurrentHashMap<>();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Location> movementTracker = new ConcurrentHashMap<>();
    private final Map<UUID, SchedulerTask> warmupTasks = new ConcurrentHashMap<>();

    public WarpManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.databaseFile = new File(plugin.getDataFolder(), "warps.db");
        initializeDatabase();
        loadWarps();
    }

    private void initializeDatabase() {
        try {
            if (!databaseFile.exists()) {
                databaseFile.getParentFile().mkdirs();
                databaseFile.createNewFile();
            }

            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            }

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS warps (
                        name TEXT PRIMARY KEY,
                        world TEXT NOT NULL,
                        x REAL NOT NULL,
                        y REAL NOT NULL,
                        z REAL NOT NULL,
                        yaw REAL NOT NULL,
                        pitch REAL NOT NULL,
                        permission TEXT,
                        cost REAL DEFAULT 0.0,
                        hidden INTEGER DEFAULT 0,
                        description TEXT DEFAULT '',
                        category TEXT DEFAULT 'default'
                    )
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS player_warp_usage (
                        uuid TEXT NOT NULL,
                        warp_name TEXT NOT NULL,
                        uses INTEGER DEFAULT 0,
                        last_used INTEGER DEFAULT 0,
                        PRIMARY KEY (uuid, warp_name)
                    )
                """);
            }

            plugin.debug("Warp database initialized successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize warp database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setWarmupTask(UUID uuid, SchedulerTask task) {
        SchedulerTask existing = warmupTasks.put(uuid, task);
        if (existing != null && !existing.isCancelled()) {
            existing.cancel();
        }
    }

    public void cancelWarmupTask(UUID uuid) {
        SchedulerTask task = warmupTasks.remove(uuid);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    public void reload() {
        plugin.debug("Reloading warp system...");

        for (SchedulerTask task : warmupTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        warmupTasks.clear();
        pendingWarps.clear();
        movementTracker.clear();

        loadWarps();

        plugin.debug("Warp system reloaded. Loaded " + warpCache.size() + " warps.");
        plugin.debug("Config - Enabled: " + plugin.getConfigManager().isWarpEnabled() +
                ", Cooldown: " + plugin.getConfigManager().getWarpCooldown() +
                "s, Warmup: " + plugin.getConfigManager().getWarpWarmup() +
                "s, CancelOnMove: " + plugin.getConfigManager().isWarpCancelOnMovement() +
                ", Particles: " + plugin.getConfigManager().isWarpParticles() +
                ", Sounds: " + plugin.getConfigManager().isWarpSounds());
    }

    public void loadWarps() {
        warpCache.clear();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM warps")) {

            while (rs.next()) {
                String name = rs.getString("name");
                String worldName = rs.getString("world");
                World world = plugin.getServer().getWorld(worldName);

                if (world == null) {
                    plugin.debug("World '" + worldName + "' not found for warp '" + name + "'");
                    continue;
                }

                Location loc = new Location(world,
                        rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                        rs.getFloat("yaw"), rs.getFloat("pitch"));

                Warp warp = new Warp(name, loc);
                warp.setPermission(rs.getString("permission"));
                warp.setCost(rs.getDouble("cost"));
                warp.setHidden(rs.getInt("hidden") == 1);
                warp.setDescription(rs.getString("description"));
                warp.setCategory(rs.getString("category"));

                warpCache.put(name.toLowerCase(), warp);
            }

            plugin.debug("Loaded " + warpCache.size() + " warps from database");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load warps: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isSystemEnabled() {
        return plugin.getConfigManager().isWarpEnabled();
    }

    public boolean createWarp(String name, Location location) {
        if (warpCache.containsKey(name.toLowerCase())) {
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO warps (name, world, x, y, z, yaw, pitch, permission, cost, hidden, description, category) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            stmt.setString(1, name);
            stmt.setString(2, location.getWorld().getName());
            stmt.setDouble(3, location.getX());
            stmt.setDouble(4, location.getY());
            stmt.setDouble(5, location.getZ());
            stmt.setFloat(6, location.getYaw());
            stmt.setFloat(7, location.getPitch());
            stmt.setString(8, null);
            stmt.setDouble(9, 0.0);
            stmt.setInt(10, 0);
            stmt.setString(11, "");
            stmt.setString(12, "default");

            stmt.executeUpdate();

            Warp warp = new Warp(name, location);
            warpCache.put(name.toLowerCase(), warp);

            plugin.debug("Created warp: " + name);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create warp: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteWarp(String name) {
        if (!warpCache.containsKey(name.toLowerCase())) {
            return false;
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM warps WHERE name = ?")) {
            stmt.setString(1, name);
            stmt.executeUpdate();

            warpCache.remove(name.toLowerCase());
            plugin.debug("Deleted warp: " + name);
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete warp: " + e.getMessage());
            return false;
        }
    }

    public boolean updateWarp(Warp warp) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE warps SET world=?, x=?, y=?, z=?, yaw=?, pitch=?, permission=?, cost=?, hidden=?, description=?, category=? " +
                        "WHERE name=?")) {

            stmt.setString(1, warp.getLocation().getWorld().getName());
            stmt.setDouble(2, warp.getLocation().getX());
            stmt.setDouble(3, warp.getLocation().getY());
            stmt.setDouble(4, warp.getLocation().getZ());
            stmt.setFloat(5, warp.getLocation().getYaw());
            stmt.setFloat(6, warp.getLocation().getPitch());
            stmt.setString(7, warp.getPermission());
            stmt.setDouble(8, warp.getCost());
            stmt.setInt(9, warp.isHidden() ? 1 : 0);
            stmt.setString(10, warp.getDescription());
            stmt.setString(11, warp.getCategory());
            stmt.setString(12, warp.getName());

            stmt.executeUpdate();

            warpCache.put(warp.getName().toLowerCase(), warp);
            plugin.debug("Updated warp: " + warp.getName());
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to update warp: " + e.getMessage());
            return false;
        }
    }

    public Warp getWarp(String name) {
        return warpCache.get(name.toLowerCase());
    }

    public Collection<Warp> getAllWarps() {
        return Collections.unmodifiableCollection(warpCache.values());
    }

    public List<Warp> getVisibleWarps() {
        return warpCache.values().stream()
                .filter(w -> !w.isHidden())
                .sorted(Comparator.comparing(Warp::getName))
                .collect(Collectors.toList());
    }

    public List<Warp> getWarpsByCategory(String category) {
        return warpCache.values().stream()
                .filter(w -> w.getCategory().equalsIgnoreCase(category))
                .sorted(Comparator.comparing(Warp::getName))
                .collect(Collectors.toList());
    }

    public Set<String> getCategories() {
        return warpCache.values().stream()
                .map(Warp::getCategory)
                .collect(Collectors.toSet());
    }

    public boolean warpExists(String name) {
        return warpCache.containsKey(name.toLowerCase());
    }

    public void setPendingWarp(UUID uuid, Warp warp) {
        pendingWarps.put(uuid, warp);
    }

    public Warp getPendingWarp(UUID uuid) {
        return pendingWarps.get(uuid);
    }

    public void removePendingWarp(UUID uuid) {
        pendingWarps.remove(uuid);
    }

    public boolean hasPendingWarp(UUID uuid) {
        return pendingWarps.containsKey(uuid);
    }

    public void setCooldown(UUID uuid) {
        cooldowns.put(uuid, System.currentTimeMillis());
    }

    public long getRemainingCooldown(UUID uuid) {
        long cooldownMs = plugin.getConfigManager().getWarpCooldown() * 1000;
        Long lastUse = cooldowns.get(uuid);

        if (lastUse == null) return 0;

        long remaining = (lastUse + cooldownMs) - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public void trackMovement(UUID uuid, Location location) {
        movementTracker.put(uuid, location);
    }

    public Location getTrackedLocation(UUID uuid) {
        return movementTracker.get(uuid);
    }

    public void clearMovementTrack(UUID uuid) {
        movementTracker.remove(uuid);
    }

    public void recordWarpUsage(UUID uuid, String warpName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO player_warp_usage (uuid, warp_name, uses, last_used) " +
                        "VALUES (?, ?, 1, ?) " +
                        "ON CONFLICT(uuid, warp_name) DO UPDATE SET " +
                        "uses = uses + 1, last_used = excluded.last_used")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, warpName);
            stmt.setLong(3, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.debug("Failed to record warp usage: " + e.getMessage());
        }
    }

    public int getWarpUsage(UUID uuid, String warpName) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT uses FROM player_warp_usage WHERE uuid = ? AND warp_name = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, warpName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("uses");
            }
        } catch (SQLException e) {
            plugin.debug("Failed to get warp usage: " + e.getMessage());
        }
        return 0;
    }

    public void close() {
        for (SchedulerTask task : warmupTasks.values()) {
            if (!task.isCancelled()) {
                task.cancel();
            }
        }
        warmupTasks.clear();
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Error closing warp database: " + e.getMessage());
        }
    }
}