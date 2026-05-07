package net.godlycow.org.essc.user;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class UserPunishmentManager {
    private final UserManager userManager;

    public UserPunishmentManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public CompletableFuture<Boolean> banPlayer(UUID uuid, String reason, String banner, long expires) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setBanReason(reason);
        profile.setBanBanner(banner);
        profile.setBanTime(System.currentTimeMillis() / 1000L);
        profile.setBanExpires(expires);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public CompletableFuture<Boolean> unbanPlayer(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setBanReason(null);
        profile.setBanBanner(null);
        profile.setBanTime(0);
        profile.setBanExpires(0);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public boolean isBanned(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) return false;
        if (profile.getBanExpires() == 0) return true; // permanent
        return profile.getBanExpires() > System.currentTimeMillis() / 1000L;
    }

    public String getBanReason(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? profile.getBanReason() : null;
    }

    public CompletableFuture<Boolean> mutePlayer(UUID uuid, String reason, String muter, long expires) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setMuteReason(reason);
        profile.setMuteMuter(muter);
        profile.setMuteTime(System.currentTimeMillis() / 1000L);
        profile.setMuteExpires(expires);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public CompletableFuture<Boolean> unmutePlayer(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(false);
        }
        profile.setMuteReason(null);
        profile.setMuteMuter(null);
        profile.setMuteTime(0);
        profile.setMuteExpires(0);
        profile.setUpdatedAt(System.currentTimeMillis() / 1000L);
        return userManager.saveAsync(profile);
    }

    public boolean isMuted(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile == null) return false;
        if (profile.getMuteExpires() == 0) return true; // permanent
        return profile.getMuteExpires() > System.currentTimeMillis() / 1000L;
    }

    public String getMuteReason(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? profile.getMuteReason() : null;
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

    public boolean isMuteOfflineNotification(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && profile.isMuteOfflineNotification();
    }
}