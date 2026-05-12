package net.godlycow.org.essc.rtp;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.rtp.event.*;
import net.godlycow.org.essc.softwares.SchedulerTask;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;

public class RTPManager {
    private final EssentialsC plugin;
    private final Random random = new Random();
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, SchedulerTask> pendingTeleports = new ConcurrentHashMap<>();
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
        return worldSettings.keySet().stream().filter(name -> resolveWorld(name) != null).sorted().collect(Collectors.toList());
    }

    public World resolveWorld(String configuredName) {
        World exact = Bukkit.getWorld(configuredName);
        if (exact != null) {
            return exact;
        }
        for (World world : Bukkit.getWorlds()) {
            if (world.getName().endsWith("-" + configuredName)) {
                return world;
            }
        }
        return null;
    }

    public void reload() {
        loadConfig();
        plugin.debug("RTP configuration reloaded");
    }

    public void shutdown() {
        pendingTeleports.values().forEach(SchedulerTask::cancel);
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
        Long cooldownStart = cooldowns.get(player.getUniqueId());
        if (cooldownStart == null) return false;
        boolean expired = System.currentTimeMillis() - cooldownStart >= cooldown * 1000;
        if (expired) {
            cooldowns.remove(player.getUniqueId());
            RtpCooldownExpireEvent event = new RtpCooldownExpireEvent(player, cooldownStart);
            Bukkit.getPluginManager().callEvent(event);
            return false;
        }
        return true;
    }

    public long getRemainingCooldown(Player player) {
        if (hasBypassPermission(player, "cooldown")) return 0;
        Long cooldownStart = cooldowns.get(player.getUniqueId());
        if (cooldownStart == null) return 0;
        long elapsed = System.currentTimeMillis() - cooldownStart;
        if (elapsed >= cooldown * 1000) {
            cooldowns.remove(player.getUniqueId());
            RtpCooldownExpireEvent event = new RtpCooldownExpireEvent(player, cooldownStart);
            Bukkit.getPluginManager().callEvent(event);
            return 0;
        }
        return cooldown - (elapsed / 1000);
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
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.NO_PERMISSION);
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        if (!hasWorldPermission(player, world.getName())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_world_permission"));
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.NO_WORLD_PERMISSION);
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        if (rtpInProgress.contains(player.getUniqueId())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.in_progress"));
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.ALREADY_IN_PROGRESS);
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        if (isOnCooldown(player)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("time", String.valueOf(getRemainingCooldown(player)));
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.cooldown", placeholders));
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.COOLDOWN_ACTIVE);
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        if (!isWorldEnabled(world.getName())) {
            player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.world_disabled"));
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.WORLD_DISABLED);
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        RtpRequestEvent requestEvent = new RtpRequestEvent(player, world);
        Bukkit.getPluginManager().callEvent(requestEvent);
        if (requestEvent.isCancelled()) {
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.EVENT_CANCELLED, requestEvent.getCancelReason());
            Bukkit.getPluginManager().callEvent(failEvent);
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

        RtpWarmupStartEvent warmupEvent = new RtpWarmupStartEvent(player, world, actualWarmup);
        Bukkit.getPluginManager().callEvent(warmupEvent);
        if (warmupEvent.isCancelled()) {
            rtpInProgress.remove(player.getUniqueId());
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.EVENT_CANCELLED, warmupEvent.getCancelReason());
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }
        actualWarmup = warmupEvent.getWarmupSeconds();

        long finalActualWarmup = actualWarmup;
        SchedulerTask task = plugin.getEssScheduler().runForEntityTimer(player, new Runnable() {
            int seconds = (int) finalActualWarmup;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    rtpInProgress.remove(player.getUniqueId());
                    SchedulerTask t = pendingTeleports.remove(player.getUniqueId());
                    if (t != null) t.cancel();
                    RtpWarmupCancelEvent cancelEvent = new RtpWarmupCancelEvent(player, world, RtpWarmupCancelEvent.CancelReason.PLAYER_OFFLINE);
                    Bukkit.getPluginManager().callEvent(cancelEvent);
                    RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.WARMUP_CANCELLED);
                    Bukkit.getPluginManager().callEvent(failEvent);
                    return;
                }

                if (cancelOnMovement && !hasBypassPermission(player, "movement") && hasMoved(initialLocation, player.getLocation())) {
                    rtpInProgress.remove(player.getUniqueId());
                    SchedulerTask t = pendingTeleports.remove(player.getUniqueId());
                    if (t != null) t.cancel();
                    player.sendMessage(plugin.getLanguageManager().get(player, "rtp.warmup.cancelled"));
                    RtpWarmupCancelEvent cancelEvent = new RtpWarmupCancelEvent(player, world, RtpWarmupCancelEvent.CancelReason.PLAYER_MOVED);
                    Bukkit.getPluginManager().callEvent(cancelEvent);
                    RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.WARMUP_CANCELLED);
                    Bukkit.getPluginManager().callEvent(failEvent);
                    return;
                }

                seconds--;

                if (seconds <= 0) {
                    SchedulerTask t = pendingTeleports.remove(player.getUniqueId());
                    if (t != null) t.cancel();
                    executeRTP(player, world);
                } else {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 0.5f, 1.0f);
                    Map<String, String> ph = new HashMap<>();
                    ph.put("time", String.valueOf(seconds));
                    player.sendActionBar(plugin.getLanguageManager().get(player, "rtp.warmup.actionbar", ph));
                }
            }
        }, 20L, 20L);

        pendingTeleports.put(player.getUniqueId(), task);
    }

    private boolean hasMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() ||
                from.getBlockY() != to.getBlockY() ||
                from.getBlockZ() != to.getBlockZ();
    }

    private void executeRTP(Player player, World world) {
        player.sendMessage(plugin.getLanguageManager().get(player, "rtp.searching"));

        RtpSearchStartEvent searchStartEvent = new RtpSearchStartEvent(player, world);
        Bukkit.getPluginManager().callEvent(searchStartEvent);
        if (searchStartEvent.isCancelled()) {
            rtpInProgress.remove(player.getUniqueId());
            RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.EVENT_CANCELLED, searchStartEvent.getCancelReason());
            Bukkit.getPluginManager().callEvent(failEvent);
            return;
        }

        findSafeLocation(world).thenAccept(searchResult -> plugin.getEssScheduler().runForEntity(player, () -> {
            if (!player.isOnline()) {
                rtpInProgress.remove(player.getUniqueId());
                return;
            }

            RtpSearchCompleteEvent searchCompleteEvent = new RtpSearchCompleteEvent(player, world, searchResult.location(), searchResult.attempts());
            Bukkit.getPluginManager().callEvent(searchCompleteEvent);

            if (searchResult.location() == null) {
                player.sendMessage(plugin.getLanguageManager().get(player, "rtp.error.no_safe_location"));
                rtpInProgress.remove(player.getUniqueId());
                RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.NO_SAFE_LOCATION);
                Bukkit.getPluginManager().callEvent(failEvent);
                return;
            }

            Location finalLoc = searchResult.location().clone();
            finalLoc.setYaw(random.nextFloat() * 360);
            finalLoc.setPitch(0);

            RtpTeleportEvent teleportEvent = new RtpTeleportEvent(player, world, finalLoc);
            Bukkit.getPluginManager().callEvent(teleportEvent);
            if (teleportEvent.isCancelled()) {
                rtpInProgress.remove(player.getUniqueId());
                RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.EVENT_CANCELLED, teleportEvent.getCancelReason());
                Bukkit.getPluginManager().callEvent(failEvent);
                return;
            }
            finalLoc = teleportEvent.getDestination();

            Location finalLoc1 = finalLoc;
            plugin.getEssScheduler().teleportAsync(player, finalLoc).thenAccept(success -> {
                if (!success) {
                    rtpInProgress.remove(player.getUniqueId());
                    RtpFailEvent failEvent = new RtpFailEvent(player, world, RtpFailEvent.FailureReason.TELEPORT_FAILED);
                    Bukkit.getPluginManager().callEvent(failEvent);
                    return;
                }

                cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
                rtpInProgress.remove(player.getUniqueId());

                if (plugin.getUserManager() != null) {
                    plugin.getUserManager().getCooldownManager().setRtpLastUsed(player.getUniqueId(), System.currentTimeMillis() / 1000L);
                }

                RtpPostTeleportEvent postEvent = new RtpPostTeleportEvent(player, world, finalLoc1);
                Bukkit.getPluginManager().callEvent(postEvent);

                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("x", String.valueOf(finalLoc1.getBlockX()));
                placeholders.put("y", String.valueOf(finalLoc1.getBlockY()));
                placeholders.put("z", String.valueOf(finalLoc1.getBlockZ()));
                placeholders.put("world", world.getName());
                player.sendMessage(plugin.getLanguageManager().get(player, "rtp.success", placeholders));

                if (particles) {
                    spawnTeleportParticles(finalLoc1);
                }
                player.playSound(finalLoc1, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                world.playSound(finalLoc1, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
            });
        }));
    }

    private CompletableFuture<SearchResult> findSafeLocation(World world) {
        WorldRTPSettings settings = getWorldSettings(world.getName());
        Location spawn = world.getSpawnLocation();
        int attempts = world.getEnvironment() == World.Environment.THE_END ? maxAttempts * 3 : maxAttempts;

        return tryFindSafe(world, settings, spawn, attempts, 0);
    }

    private CompletableFuture<SearchResult> tryFindSafe(World world, WorldRTPSettings settings,
                                                        Location spawn, int maxAttempts, int attempt) {
        if (attempt >= maxAttempts) {
            return CompletableFuture.completedFuture(new SearchResult(null, attempt));
        }

        double angle = random.nextDouble() * Math.PI * 2;
        int distance = settings.minRadius() + random.nextInt(settings.maxRadius() - settings.minRadius());
        int x = spawn.getBlockX() + (int) (Math.cos(angle) * distance);
        int z = spawn.getBlockZ() + (int) (Math.sin(angle) * distance);

        if (useBorder) {
            WorldBorder border = world.getWorldBorder();
            Location center = border.getCenter();
            double radius = border.getSize() / 2;
            if (Math.abs(x - center.getBlockX()) > radius || Math.abs(z - center.getBlockZ()) > radius) {
                return tryFindSafe(world, settings, spawn, maxAttempts, attempt + 1);
            }
        }

        return world.getChunkAtAsync(x >> 4, z >> 4).thenCompose(chunk -> {
            int y;
            if (world.getEnvironment() == World.Environment.NETHER) {
                y = findSafeNetherY(world, x, z);
                if (y <= 0) return tryFindSafe(world, settings, spawn, maxAttempts, attempt + 1);
            } else {
                y = world.getHighestBlockYAt(x, z);
                if (world.getEnvironment() == World.Environment.THE_END && y <= 0) {
                    return tryFindSafe(world, settings, spawn, maxAttempts, attempt + 1);
                }
            }

            if (y < minY) y = minY;
            if (y > maxY) y = maxY;

            Location loc = new Location(world, x + 0.5, y + 1, z + 0.5);

            Biome biome = world.getBiome(x, y, z);
            if (settings.blockedBiomes().contains(biome.name().toLowerCase())) {
                return tryFindSafe(world, settings, spawn, maxAttempts, attempt + 1);
            }

            if (isSafeLocation(loc)) {
                return CompletableFuture.completedFuture(new SearchResult(loc, attempt + 1));
            }

            return tryFindSafe(world, settings, spawn, maxAttempts, attempt + 1);
        });
    }

    private int findSafeNetherY(World world, int x, int z) {
        int searchStart = Math.min(126, maxY);
        int searchEnd = Math.max(1, minY);

        for (int y = searchStart; y >= searchEnd; y--) {
            Material ground = world.getBlockAt(x, y - 1, z).getType();
            Material feet = world.getBlockAt(x, y, z).getType();
            Material head = world.getBlockAt(x, y + 1, z).getType();

            if (ground == Material.BEDROCK && y <= 5) continue;

            if (ground.isSolid() &&
                    ground != Material.LAVA &&
                    ground != Material.CACTUS &&
                    ground != Material.FIRE &&
                    ground != Material.MAGMA_BLOCK &&
                    (feet.isAir() || !feet.isSolid()) &&
                    (head.isAir() || !head.isSolid())) {
                return y;
            }
        }

        return -1;
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

    public void cancelRtp(Player player) {
        SchedulerTask task = pendingTeleports.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        rtpInProgress.remove(player.getUniqueId());
    }

    public boolean isUseBorder() {
        return useBorder;
    }

    public long getCooldown() {
        return cooldown;
    }

    public long getWarmup() {
        return warmup;
    }

    public boolean isCancelOnMovement() {
        return cancelOnMovement;
    }

    public boolean isParticles() {
        return particles;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public record WorldRTPSettings(
            int minRadius,
            int maxRadius,
            List<String> blockedBiomes,
            boolean enabled,
            String displayName
    ) {}

    public record SearchResult(Location location, int attempts) {}
}