package net.godlycow.org.essc.user;

import net.godlycow.org.essc.EssentialsC;

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
}
