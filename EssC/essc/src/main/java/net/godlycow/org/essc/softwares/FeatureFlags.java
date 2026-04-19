package net.godlycow.org.essc.softwares;

public final class FeatureFlags {

    private FeatureFlags() {}

    public static boolean supportsScoreboard() {
        return !ServerSoftware.isFolia();
    }

    public static boolean supportsAsyncPlayerChatEvent() {
        return !ServerSoftware.isFolia();
    }

    public static boolean supportsLegacyBukkitScheduler() {
        return !ServerSoftware.isFolia();
    }

    public static boolean supportsRegionScheduler() {
        return ServerSoftware.isFolia();
    }

    public static boolean supportsEntityScheduler() {
        return ServerSoftware.isFolia();
    }

    public static boolean supportsNativeAsyncTeleport() {
        return ServerSoftware.isPaper() || ServerSoftware.isFolia();
    }

    public static boolean supportsPaperChatEvent() {
        return ServerSoftware.isPaper() || ServerSoftware.isFolia();
    }

    public static boolean supportsAdventureNatively() {
        return ServerSoftware.isPaper() || ServerSoftware.isFolia();
    }
}