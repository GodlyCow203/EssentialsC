package net.godlycow.org.essc.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.database.Database;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager implements Listener {
    private final EssentialsC plugin;
    private final Database database;

    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final Map<UUID, Home> pendingDestination = new ConcurrentHashMap<>();

    public HomeManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.database = new Database(plugin);
        try {
            database.connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize home database: " + e.getMessage());
        }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.debug("HomeManager initialized");
    }

    private void createTables() throws SQLException {
        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                CREATE TABLE IF NOT EXISTS homes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    uuid TEXT NOT NULL,
                    name TEXT NOT NULL,
                    world TEXT NOT NULL,
                    x REAL NOT NULL,
                    y REAL NOT NULL,
                    z REAL NOT NULL,
                    yaw REAL NOT NULL,
                    pitch REAL NOT NULL,
                    created_at INTEGER DEFAULT (strftime('%s', 'now')),
                    UNIQUE(uuid, name)
                )
            """)) {
            stmt.execute();
        }

        try (Connection conn = database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "CREATE INDEX IF NOT EXISTS idx_homes_uuid ON homes(uuid)"
             )) {
            stmt.execute();
        }
        plugin.debug("Home database tables initialized");
    }

    public int getMaxHomes(Player player) {
        if (player.hasPermission("essentialsc.sethome.admin") || player.hasPermission("essentialsc.sethome.unlimited")) {
            return Integer.MAX_VALUE;
        }

        for (int i = 100; i >= 1; i--) {
            if (player.hasPermission("essentialsc.sethome." + i)) {
                return i;
            }
        }

        return plugin.getConfig().getInt("home.max-homes", 3);
    }

    public CompletableFuture<Integer> getHomeCount(UUID uuid) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT COUNT(*) FROM homes WHERE uuid = ?"
            )) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();
                return rs.next() ? rs.getInt(1) : 0;
            }
        });
    }

    public CompletableFuture<Boolean> homeExists(UUID uuid, String name) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT 1 FROM homes WHERE uuid = ? AND name = ?"
            )) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name.toLowerCase());
                ResultSet rs = stmt.executeQuery();
                return rs.next();
            }
        });
    }

    public CompletableFuture<Boolean> setHome(Player player, String name, Location location) {
        plugin.debug("Setting home '" + name + "' for " + player.getName());

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO homes (uuid, name, world, x, y, z, yaw, pitch)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(uuid, name) DO UPDATE SET
                    world = excluded.world,
                    x = excluded.x,
                    y = excluded.y,
                    z = excluded.z,
                    yaw = excluded.yaw,
                    pitch = excluded.pitch,
                    created_at = strftime('%s', 'now')
            """)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, name.toLowerCase());
                stmt.setString(3, location.getWorld().getName());
                stmt.setDouble(4, location.getX());
                stmt.setDouble(5, location.getY());
                stmt.setDouble(6, location.getZ());
                stmt.setFloat(7, location.getYaw());
                stmt.setFloat(8, location.getPitch());
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to set home: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Boolean> deleteHome(UUID uuid, String name) {
        plugin.debug("Deleting home '" + name + "' for " + uuid);

        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "DELETE FROM homes WHERE uuid = ? AND name = ?"
            )) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name.toLowerCase());
                return stmt.executeUpdate() > 0;
            }
        });
    }

    public CompletableFuture<Home> getHome(UUID uuid, String name) {
        return database.async(conn -> {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM homes WHERE uuid = ? AND name = ?"
            )) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, name.toLowerCase());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return new Home(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch"),
                            rs.getLong("created_at")
                    );
                }
                return null;
            }
        });
    }

    public CompletableFuture<List<Home>> getHomes(UUID uuid) {
        return database.async(conn -> {
            List<Home> homes = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT * FROM homes WHERE uuid = ? ORDER BY name"
            )) {
                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    homes.add(new Home(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getString("world"),
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch"),
                            rs.getLong("created_at")
                    ));
                }
            }
            return homes;
        });
    }

    public boolean isOnCooldown(Player player) {
        if (player.hasPermission("essentialsc.home.admin")) return false;

        long cooldown = plugin.getConfig().getLong("home.cooldown", 0);
        if (cooldown <= 0) return false;

        Long last = teleportCooldowns.get(player.getUniqueId());
        if (last == null) return false;

        return System.currentTimeMillis() - last < cooldown * 1000;
    }

    public long getRemainingCooldown(Player player) {
        long cooldown = plugin.getConfig().getLong("home.cooldown", 0);
        long last = teleportCooldowns.getOrDefault(player.getUniqueId(), 0L);
        return Math.max(0, (last + (cooldown * 1000) - System.currentTimeMillis()) / 1000);
    }

    public boolean hasPendingTeleport(Player player) {
        return pendingTeleports.containsKey(player.getUniqueId());
    }

    public void cancelTeleport(Player player) {
        BukkitTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            pendingDestination.remove(player.getUniqueId());
            plugin.debug("Cancelled teleport for " + player.getName());
        }
    }

    public void startTeleport(Player player, Home home) {
        cancelTeleport(player);

        if (isOnCooldown(player)) {
            player.sendMessage(plugin.getLanguageManager().get(player, "home.teleport.cooldown",
                    Map.of("seconds", String.valueOf(getRemainingCooldown(player)))));
            return;
        }

        Location loc = home.toLocation(plugin.getServer());
        if (loc == null || loc.getWorld() == null) {
            player.sendMessage(plugin.getLanguageManager().get(player, "home.teleport.invalid_world"));
            return;
        }

        long warmup = plugin.getConfig().getLong("home.warmup", 0);

        if (warmup > 0 && !player.hasPermission("essentialsc.home.admin")) {
            pendingDestination.put(player.getUniqueId(), home);

            player.sendMessage(plugin.getLanguageManager().get(player, "home.teleport.pending",
                    Map.of("seconds", String.valueOf(warmup))));

            plugin.debug("Starting warmup for " + player.getName() + " (" + warmup + "s)");

            BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                completeTeleport(player, home);
            }, warmup * 20L);

            pendingTeleports.put(player.getUniqueId(), task);
        } else {
            completeTeleport(player, home);
        }
    }

    private void completeTeleport(Player player, Home home) {
        pendingTeleports.remove(player.getUniqueId());
        pendingDestination.remove(player.getUniqueId());

        Location loc = home.toLocation(plugin.getServer());
        if (loc == null) return;

        player.teleport(loc);
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        player.sendMessage(plugin.getLanguageManager().get(player, "home.teleport.success",
                Map.of("name", home.getName())));

        if (plugin.getConfig().getBoolean("home.particles", true)) {
            loc.getWorld().spawnParticle(Particle.PORTAL, loc, 50, 0.5, 1, 0.5);
        }
        if (plugin.getConfig().getBoolean("home.sounds", true)) {
            player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }

        plugin.debug("Teleported " + player.getName() + " to home '" + home.getName() + "'");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfig().getBoolean("home.cancel-on-movement", true)) return;

        Player player = event.getPlayer();
        if (!hasPendingTeleport(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        cancelTeleport(player);
        player.sendMessage(plugin.getLanguageManager().get(player, "home.teleport.cancelled"));
        plugin.debug("Cancelled teleport for " + player.getName() + " due to movement");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelTeleport(event.getPlayer());
    }

    public void shutdown() {
        pendingTeleports.values().forEach(BukkitTask::cancel);
        pendingTeleports.clear();
        database.disconnect();
        plugin.debug("HomeManager shutdown complete");
    }

    public void reload() {
        plugin.debug("Reloading HomeManager configuration");
    }
}