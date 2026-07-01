package net.godlycow.org.essc.storage.user;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserPManager {
    private final UserManager userManager;

    public UserPManager(UserManager userManager) {
        this.userManager = userManager;
    }


    public CompletableFuture<Boolean> setMuteOfflineNotification(UUID uuid, boolean enabled) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setMuteOfflineNotification(enabled);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }
}
