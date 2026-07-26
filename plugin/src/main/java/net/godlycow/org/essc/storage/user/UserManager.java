package net.godlycow.org.essc.storage.user;

import net.godlycow.org.essc.EssentialsC;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {
    private final EssentialsC plugin;
    private final UserDatabase repository;
    private final Map<UUID, UserProfile> cache = new ConcurrentHashMap<>();

    private final UserLocationManager locationManager;
    private final UserPManager preferenceManager;
    private final UserPunishmentManager punishmentManager;
    private final UserStateManager stateManager;
    private final UserCooldownManager cooldownManager;

    public UserManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.repository = new UserDatabase(plugin);
        this.locationManager = new UserLocationManager(this);
        this.preferenceManager = new UserPManager(this);
        this.punishmentManager = new UserPunishmentManager(this);
        this.stateManager = new UserStateManager(this);
        this.cooldownManager = new UserCooldownManager(this);
    }

    public CompletableFuture<UserProfile> loadProfile(UUID uuid, String username) {
        return repository.findByUuid(uuid).thenCompose(profile -> {
            long now = System.currentTimeMillis() / 1000L;
            if (profile == null) {
                UserProfile created = UserProfile.createDefault(uuid, username, now);
                return repository.save(created).thenApply(saved -> {
                    cache.put(uuid, created);
                    return created;
                });
            }
            UserProfile cached = cache.get(uuid);
            if (cached != null) {
                profile.setVanished(cached.isVanished());
                profile.setFlyEnabled(cached.isFlyEnabled());
                profile.setTpaBlocked(cached.isTpaBlocked());
                profile.setScoreboardDisabled(cached.isScoreboardDisabled());
                profile.setRulesAccepted(cached.isRulesAccepted());
            }
            if (!username.equals(profile.getUsername())) {
                profile.setLastKnownName(profile.getUsername());
                profile.setUsername(username);
            }
            if (profile.getFirstJoinTime() <= 0) {
                profile.setFirstJoinTime(now);
            }
            profile.setLastJoinTime(now);
            profile.setUpdatedAt(now);
            return repository.save(profile).thenApply(saved -> {
                cache.put(uuid, profile);
                return profile;
            });
        });
    }

    public CompletableFuture<Boolean> saveAsync(UserProfile profile) {
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        cache.put(profile.getUuid(), profile);
        return repository.save(profile);
    }

    public UserProfile getCachedProfile(UUID uuid) {
        return cache.get(uuid);
    }

    public void clearCache(UUID uuid) {
        cache.remove(uuid);
    }

    public Collection<UserProfile> getCachedProfiles() {
        return cache.values();
    }

    public void shutdown() {
        cache.clear();
        repository.getDatabase().disconnect();
        plugin.debug("UserManager shutdown complete.");
    }

    public UserRepo getRepository() {
        return repository;
    }

    public UserLocationManager getLocationManager() {
        return locationManager;
    }

    public UserPManager getPreferenceManager() {
        return preferenceManager;
    }

    public UserPunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public UserStateManager getStateManager() {
        return stateManager;
    }

    public UserCooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public CompletableFuture<Boolean> recordIp(UUID uuid, String ip) {
        UserProfile cached = cache.get(uuid);
        if (cached != null) {
            cached.setLastIp(ip);
        }
        return repository.recordIp(uuid, ip);
    }

    public String getLastIp(UUID uuid) {
        UserProfile cached = cache.get(uuid);
        if (cached != null) {
            return cached.getLastIp();
        }
        return null;
    }

    public java.util.concurrent.CompletableFuture<Boolean> saveInventory(java.util.UUID uuid, String base64) {
        return repository.saveInventory(uuid, base64);
    }

    public java.util.concurrent.CompletableFuture<String> loadInventory(java.util.UUID uuid) {
        return repository.loadInventory(uuid);
    }

    public java.util.concurrent.CompletableFuture<Boolean> deleteInventory(java.util.UUID uuid) {
        return repository.deleteInventory(uuid);
    }
}