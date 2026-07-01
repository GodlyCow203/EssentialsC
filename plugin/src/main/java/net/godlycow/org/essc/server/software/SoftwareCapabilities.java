package net.godlycow.org.essc.server.software;

import org.bukkit.Bukkit;

public final class SoftwareCapabilities {

    private static final String SERVER_VERSION = Bukkit.getVersion();
    private static final String BUKKIT_VERSION = Bukkit.getBukkitVersion();
    private static final int[] MINECRAFT_VERSION = parseMinecraftVersion();

    private SoftwareCapabilities() {}

    private static int[] parseMinecraftVersion() {
        try {
            String raw = Bukkit.getBukkitVersion();
            String version = raw.split("-")[0];
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new int[]{major, minor, patch};
        } catch (Exception e) {
            return new int[]{1, 17, 0};
        }
    }

    public static boolean isAtLeast(int major, int minor) {
        if (MINECRAFT_VERSION[0] != major) return MINECRAFT_VERSION[0] > major;
        return MINECRAFT_VERSION[1] >= minor;
    }

    public static boolean isAtLeast(int major, int minor, int patch) {
        if (MINECRAFT_VERSION[0] != major) return MINECRAFT_VERSION[0] > major;
        if (MINECRAFT_VERSION[1] != minor) return MINECRAFT_VERSION[1] > minor;
        return MINECRAFT_VERSION[2] >= patch;
    }

    public static int getMajorVersion() {
        return MINECRAFT_VERSION[0];
    }

    public static int getMinorVersion() {
        return MINECRAFT_VERSION[1];
    }

    public static int getPatchVersion() {
        return MINECRAFT_VERSION[2];
    }

    public static String getServerVersion() {
        return SERVER_VERSION;
    }

    public static String getBukkitVersion() {
        return BUKKIT_VERSION;
    }

    public static String getSoftwareName() {
        return switch (ServerSoftware.get()) {
            case FOLIA -> "Folia";
            case PAPER -> "Paper";
            case SPIGOT -> "Spigot";
        };
    }

    public static String getSummary() {
        return getSoftwareName() + " " + getBukkitVersion();
    }
}