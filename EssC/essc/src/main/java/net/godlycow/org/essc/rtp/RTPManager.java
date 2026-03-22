package net.godlycow.org.essc.rtp;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;

public class RTPManager {
    private final EssentialsC plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> pendingTeleports = new ConcurrentHashMap<>();
    private final Set<UUID> rtpInProgress = ConcurrentHashMap.newKeySet();

    private boolean enabled;
    private long cooldown;
    private long warmup;
    private boolean cancelOnMovement;
    private boolean particles;
    private int maxAttempts;
    private int minY;
    private int maxY;
    private boolean useBorder;

    private final Map<String, WorldRTPSettings> worldSettings = new HashMap<>();

    public RTPManager(EssentialsC plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();

        this.enabled = config.getBoolean("rtp.enabled", true);
        this.cooldown = config.getLong("rtp.cooldown", 60);
        this.warmup = config.getLong("rtp.warmup", 5);
        this.cancelOnMovement = config.getBoolean("rtp.cancel-on-movement", true);
        this.particles = config.getBoolean("rtp.particles", true);
        this.maxAttempts = config.getInt("rtp.max-attempts", 10);
        this.minY = config.getInt("rtp.min-y", 64);
        this.maxY = config.getInt("rtp.max-y", 128);
        this.useBorder = config.getBoolean("rtp.use-world-border", true);

        int globalMinRadius = config.getInt("rtp.global.min-radius", 1000);
        int globalMaxRadius = config.getInt("rtp.global.max-radius", 10000);
        List<String> globalBlockedBiomes = config.getStringList("rtp.global.blocked-biomes");

        worldSettings.clear();
        var worldsSection = config.getConfigurationSection("rtp.worlds");
        if (worldsSection != null) {
            for (String worldName : worldsSection.getKeys(false)) {
                String path = "rtp.worlds." + worldName;
                int minRadius = config.getInt(path + ".min-radius", globalMinRadius);
                int maxRadius = config.getInt(path + ".max-radius", globalMaxRadius);
                List<String> blockedBiomes = config.getStringList(path + ".blocked-biomes");
                if (blockedBiomes.isEmpty()) blockedBiomes = globalBlockedBiomes;
                boolean worldEnabled = config.getBoolean(path + ".enabled", true);
                String displayName = config.getString(path + ".display-name", defaultDisplayName(worldName));

                worldSettings.put(worldName, new WorldRTPSettings(minRadius, maxRadius, blockedBiomes, worldEnabled, displayName));
            }
        }
    }

    private String defaultDisplayName(String worldName) {
        return Arrays.stream(worldName.replace("_", " ").split(" "))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    public List<String> getConfiguredWorldNames() {
        return worldSettings.keySet().stream()
                .filter(name -> Bukkit.getWorld(name) != null)
                .sorted()
                .collect(Collectors.toList());
    }

    public void reload() {
        loadConfig();
        plugin.debug("RTP configuration reloaded");
    }

    public void shutdown() {
        pendingTeleports.values().forEach(BukkitTask::cancel);
        pendingTeleports.clear();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isWorldEnabled(String worldName) {
        WorldRTPSettings settings = worldSettings.get(worldName);
        return settings != null && settings.enabled();
    }

    public WorldRTPSettings getWorldSettings(String worldName) {
        return worldSettings.getOrDefault(worldName, worldSettings.values().iterator().next());
    }

    public boolean isOnCooldown(Player player) {
        if (hasBypassPermission(player, "cooldown")) return false;
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        return System.currentTimeMillis() - cooldowns.get(player.getUniqueId()) < cooldown * 1000;
    }

    public long getRemainingCooldown(Player player) {
        if (hasBypassPermission(player, "cooldown")) return 0;
        if (!isOnCooldown(player)) return 0;
        return cooldown - ((System.currentTimeMillis() - cooldowns.get(player.getUniqueId())) / 1000);
    }

    public boolean isRtpInProgress(Player player) {
        return rtpInProgress.contains(player.getUniqueId());
    }

    public boolean hasBypassPermission(Player player, String type) {
        return player.hasPermission("essentialsc.rtp.bypass." + type);
    }

    public boolean hasWorldPermission(Player player, String worldName) {
        String worldKey = worldName.toLowerCase().replace("world_", "");
        return player.hasPermission("essentialsc.rtp.world." + worldKey) ||
                player.hasPermission("essentialsc.rtp.world.*");
    }

    public void startRTP(Player player, World world) {
        if (!player.hasPermission("essentialsc.rtp")) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_permission"));
            return;
        }

        if (!hasWorldPermission(player, world.getName())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_world_permission"));
            return;
        }

        if (rtpInProgress.contains(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.in_progress"));
            return;
        }

        if (isOnCooldown(player)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(getRemainingCooldown(player)));
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.cooldown", placeholders));
            return;
        }

        if (!isWorldEnabled(world.getName())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.world_disabled"));
            return;
        }

        rtpInProgress.add(player.getUniqueId());

        long actualWarmup = hasBypassPermission(player, "warmup") ? 0 : warmup;

        if (actualWarmup > 0) {
            startWarmup(player, world, actualWarmup);
        } else {
            executeRTP(player, world);
        }
    }

    private void startWarmup(Player player, World world, long actualWarmup) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("time", String.valueOf(actualWarmup));
        player.sendMessage(plugin.getLanguageManager().get(player, "rtp.warmup.start", placeholders));

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);

        Location initialLocation = player.getLocation().clone();

        BukkitTask task = new BukkitRunnable() {
            int seconds = (int) actualWarmup;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    rtpInProgress.remove(player.getUniqueId());
                    pendingTeleports.remove(player.getUniqueId());
                    return;
                }

                if (cancelOnMovement && !hasBypassPermission(player, "movement") && hasMoved(initialLocation, player.getLocation())) {
                    cancel();
                    rtpInProgress.remove(player.getUniqueId());
                    pendingTeleports.remove(player.getUniqueId());
                    player.sendMessage(plugin.getLanguageManager().get(player, "rtp.warmup.cancelled"));
                    return;
                }

                seconds--;

                if (seconds <= 0) {
                    cancel();
                    pendingTeleports.remove(player.getUniqueId());
                    executeRTP(player, world);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                    Map<String, String> ph = new HashMap<>();
                    ph.put("time", String.valueOf(seconds));
                    player.sendActionBar(plugin.getLanguageManager().get(player, "rtp.warmup.actionbar", ph));
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    private boolean hasMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }

    private void executeRTP(Player player, World world) {
        player.sendMessage(plugin.getLanguageManager().get(player, "rtp.searching"));

        findSafeLocation(world).thenAccept(location -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                rtpInProgress.remove(player.getUniqueId());
                return;
            }

            if (location == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_safe_location"));
                rtpInProgress.remove(player.getUniqueId());
                return;
            }

            Location finalLoc = location.clone();
            finalLoc.getChunk().load();
            finalLoc.setYaw(random.nextFloat() * 360);
            finalLoc.setPitch(0);

            player.teleport(finalLoc);

            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
            rtpInProgress.remove(player.getUniqueId());

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("x", String.valueOf(finalLoc.getBlockX()));
            placeholders.put("y", String.valueOf(finalLoc.getBlockY()));
            placeholders.put("z", String.valueOf(finalLoc.getBlockZ()));
            placeholders.put("world", world.getName());
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.success", placeholders));

            if (particles) {
                spawnTeleportParticles(finalLoc);
            }
            player.playSound(finalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            world.playSound(finalLoc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
        }));
    }

    private CompletableFuture<Location> findSafeLocation(World world) {

        return CompletableFuture.supplyAsync(() -> {

            WorldRTPSettings settings = getWorldSettings(world.getName());
            Location spawn = world.getSpawnLocation();

            int attempts = world.getEnvironment() == World.Environment.THE_END ? maxAttempts * 3 : maxAttempts;

            for (int i = 0; i < attempts; i++) {

                double angle = random.nextDouble() * Math.PI * 2;
                int distance = settings.minRadius() + random.nextInt(settings.maxRadius() - settings.minRadius());

                int x = spawn.getBlockX() + (int) (Math.cos(angle) * distance);
                int z = spawn.getBlockZ() + (int) (Math.sin(angle) * distance);

                if (useBorder) {
                    WorldBorder border = world.getWorldBorder();
                    Location center = border.getCenter();
                    double radius = border.getSize() / 2;

                    if (Math.abs(x - center.getBlockX()) > radius || Math.abs(z - center.getBlockZ()) > radius) {
                        continue;
                    }
                }

                int y = world.getHighestBlockYAt(x, z);

                if (world.getEnvironment() == World.Environment.THE_END && y <= 0) {
                    continue;
                }

                if (y < minY) y = minY;
                if (y > maxY) y = maxY;

                Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

                Biome biome = world.getBiome(x, y, z);
                if (settings.blockedBiomes().contains(biome.name().toLowerCase())) {
                    continue;
                }

                if (isSafeLocation(loc)) {
                    return loc;
                }
            }

            return null;
        });
    }

    private boolean isSafeLocation(Location location) {

        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Material ground = world.getBlockAt(x, y - 1, z).getType();
        Material feet = world.getBlockAt(x, y, z).getType();
        Material head = world.getBlockAt(x, y + 1, z).getType();

        if (!ground.isSolid()) return false;

        if (ground == Material.LAVA ||
                ground == Material.CACTUS ||
                ground == Material.FIRE ||
                ground == Material.MAGMA_BLOCK) {
            return false;
        }

        return (feet.isAir() || !feet.isSolid()) &&
                (head.isAir() || !head.isSolid());
    }

    private void spawnTeleportParticles(Location loc) {
        World world = loc.getWorld();
        for (int i = 0; i < 30; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 2;
            double offsetY = random.nextDouble() * 2;
            double offsetZ = (random.nextDouble() - 0.5) * 2;
            world.spawnParticle(Particle.PORTAL, loc.clone().add(offsetX, offsetY, offsetZ), 1, 0, 0, 0, 0.1);
        }
        world.spawnParticle(Particle.EXPLOSION, loc, 1);
    }

    public int getPlayerCountInWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        return world != null ? world.getPlayers().size() : 0;
    }


    public record WorldRTPSettings(
            int minRadius,
            int maxRadius,
            List<String> blockedBiomes,
            boolean enabled,
            String displayName
    ) {}
}