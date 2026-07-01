package net.godlycow.org.essc;

import net.godlycow.org.essc.server.FeatureFlags;
import net.godlycow.org.essc.server.software.ServerSoftware;
import net.godlycow.org.essc.server.software.SoftwareCapabilities;
import org.bukkit.plugin.Plugin;

public final class EnvironmentProfile {

    private final ServerSoftware software;
    private final String softwareSummary;
    private final boolean foliaCompatibilityMode;
    private final boolean paperApi;

    private EnvironmentProfile(Plugin plugin) {
        this.software = ServerSoftware.get();
        this.softwareSummary = SoftwareCapabilities.getSummary();
        this.foliaCompatibilityMode = ServerSoftware.isFolia();
        this.paperApi = ServerSoftware.isPaper();

        logEnvironment(plugin);
    }

    public static EnvironmentProfile detect(Plugin plugin) {
        return new EnvironmentProfile(plugin);
    }

    private void logEnvironment(Plugin plugin) {
        plugin.getLogger().info("Detected server: " + softwareSummary);
        plugin.getLogger().info("Folia mode: " + foliaCompatibilityMode);
        plugin.getLogger().info("Paper API: " + paperApi);

        if (!FeatureFlags.supportsScoreboard()) {
            plugin.getLogger().warning("Scoreboard is not supported on this software.");
        }
        if (!FeatureFlags.supportsAsyncPlayerChatEvent()) {
            plugin.getLogger().info("Using Paper AsyncChatEvent instead of legacy AsyncPlayerChatEvent.");
        }
    }

//    public ServerSoftware getSoftware() {
//        return software;
//    }
//
//    public String getSoftwareSummary() {
//        return softwareSummary;
//    }
//
//    public boolean isFoliaCompatibilityMode() {
//        return foliaCompatibilityMode;
//    }
//
//    public boolean hasPaperApi() {
//        return paperApi;
//    }
//
//    public boolean supportsFeature(String featureName) {
//        return switch (featureName.toLowerCase()) {
//            case "scoreboard" -> FeatureFlags.supportsScoreboard();
//            case "async_chat" -> FeatureFlags.supportsAsyncPlayerChatEvent();
//            case "paper_chat" -> FeatureFlags.supportsPaperChatEvent();
//            case "region_scheduler" -> FeatureFlags.supportsRegionScheduler();
//            case "entity_scheduler" -> FeatureFlags.supportsEntityScheduler();
//            case "async_teleport" -> FeatureFlags.supportsNativeAsyncTeleport();
//            default -> false;
//        };
//    }
}