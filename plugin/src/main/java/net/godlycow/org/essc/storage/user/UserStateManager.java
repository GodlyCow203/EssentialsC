package net.godlycow.org.essc.storage.user;

import java.util.UUID;

public class UserStateManager {

    private final UserManager userManager;

    public UserStateManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setFlyEnabled(UUID uuid, boolean enabled) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile != null) {
            if (enabled) {
                UserStateManager.enableFly(profile);
            } else {
                UserStateManager.disableFly(profile);
            }
            userManager.saveAsync(profile);
        }
    }

    public boolean isFlyEnabled(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && UserStateManager.hasFlyEnabled(profile);
    }

    public void setVanished(UUID uuid, boolean vanished) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile != null) {
            if (vanished) {
                UserStateManager.enableVanish(profile);
            } else {
                UserStateManager.disableVanish(profile);
            }
            userManager.saveAsync(profile);
        }
    }

    public boolean isVanished(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && UserStateManager.isVanished(profile);
    }

    public boolean isTpaBlocked(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && UserStateManager.isTpaBlocked(profile);
    }


    public void setScoreboardDisabled(UUID uuid, boolean disabled) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        if (profile != null) {
            if (disabled) {
                UserStateManager.disableScoreboard(profile);
            } else {
                UserStateManager.enableScoreboard(profile);
            }
            userManager.saveAsync(profile);
        }
    }

    public boolean isScoreboardDisabled(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && UserStateManager.isScoreboardDisabled(profile);
    }

    public boolean hasAcceptedRules(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null && UserStateManager.hasAcceptedRules(profile);
    }

    public UUID getLastReplyTarget(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? UserStateManager.getLastReplyTarget(profile) : null;
    }

    public String getStatesSummary(UUID uuid) {
        UserProfile profile = userManager.getCachedProfile(uuid);
        return profile != null ? UserStateManager.getStatesSummary(profile) : "Unknown";
    }

    public static void enableFly(UserProfile user) {
        user.setFlyEnabled(true);
    }

    public static void disableFly(UserProfile user) {
        user.setFlyEnabled(false);
    }

    public static void toggleFly(UserProfile user) {
        user.setFlyEnabled(!user.isFlyEnabled());
    }

    public static boolean hasFlyEnabled(UserProfile user) {
        return user.isFlyEnabled();
    }

    public static void enableVanish(UserProfile user) {
        user.setVanished(true);
    }

    public static void disableVanish(UserProfile user) {
        user.setVanished(false);
    }

    public static void toggleVanish(UserProfile user) {
        user.setVanished(!user.isVanished());
    }

    public static boolean isVanished(UserProfile user) {
        return user.isVanished();
    }

    public static void blockTpa(UserProfile user) {
        user.setTpaBlocked(true);
    }

    public static void unblockTpa(UserProfile user) {
        user.setTpaBlocked(false);
    }

    public static void toggleTpaBlock(UserProfile user) {
        user.setTpaBlocked(!user.isTpaBlocked());
    }

    public static boolean isTpaBlocked(UserProfile user) {
        return user.isTpaBlocked();
    }

    public static void disableScoreboard(UserProfile user) {
        user.setScoreboardDisabled(true);
    }

    public static void enableScoreboard(UserProfile user) {
        user.setScoreboardDisabled(false);
    }

    public static void toggleScoreboard(UserProfile user) {
        user.setScoreboardDisabled(!user.isScoreboardDisabled());
    }

    public static boolean isScoreboardDisabled(UserProfile user) {
        return user.isScoreboardDisabled();
    }

    public static void acceptRules(UserProfile user) {
        user.setRulesAccepted(true);
    }

    public static void rejectRules(UserProfile user) {
        user.setRulesAccepted(false);
    }

    public static boolean hasAcceptedRules(UserProfile user) {
        return user.isRulesAccepted();
    }

    public static void setLastReplyTarget(UserProfile user, UUID targetUuid) {
        user.setLastReplyTarget(targetUuid);
    }

    public static UUID getLastReplyTarget(UserProfile user) {
        return user.getLastReplyTarget();
    }

    public static void clearLastReplyTarget(UserProfile user) {
        user.setLastReplyTarget(null);
    }


    public static String getStatesSummary(UserProfile user) {
        StringBuilder sb = new StringBuilder();
        
        if (user.isFlyEnabled()) sb.append("Fly ");
        if (user.isVanished()) sb.append("Vanished ");
        if (user.isTpaBlocked()) sb.append("TPA-Blocked ");
        if (user.isScoreboardDisabled()) sb.append("NoScoreboard ");
        if (!user.isRulesAccepted()) sb.append("UnreadyRules ");
        
        String summary = sb.toString().trim();
        return summary.isEmpty() ? "Normal" : summary;
    }
}
