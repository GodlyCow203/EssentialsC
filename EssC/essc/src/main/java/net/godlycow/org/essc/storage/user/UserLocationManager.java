package net.godlycow.org.essc.storage.user;

import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserLocationManager {
    private final UserManager userManager;

    public UserLocationManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public CompletableFuture<Boolean> setBackLocation(UUID uuid, Location location) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setBackLocation(location);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public CompletableFuture<Boolean> setDeathLocation(UUID uuid, Location location) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setDeathLocation(location);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public CompletableFuture<Boolean> setLogoutLocation(UUID uuid, Location location) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setLogoutLocation(location);
        profile.setLogoutTime(System.currentTimeMillis() / 1000L);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }
}
