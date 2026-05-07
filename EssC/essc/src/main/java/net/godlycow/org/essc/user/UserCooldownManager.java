package net.godlycow.org.essc.user;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserCooldownManager {
    private final UserManager userManager;

    public UserCooldownManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public CompletableFuture<Boolean> setRtpLastUsed(UUID uuid, long timestamp) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setRtpLastUsed(timestamp);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public CompletableFuture<Boolean> setSpawnLastTeleport(UUID uuid, long timestamp) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setSpawnLastTeleport(timestamp);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public long getRtpLastUsed(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? profile.getRtpLastUsed() : 0;
    }

    public long getSpawnLastTeleport(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? profile.getSpawnLastTeleport() : 0;
    }
}
