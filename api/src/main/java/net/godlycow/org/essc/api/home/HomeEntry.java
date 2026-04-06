package net.godlycow.org.essc.api.home;

import org.bukkit.Location;

import java.util.UUID;

/**
 * An immutable snapshot of a player home.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.HomeApi}.
 * Homes are stored in SQLite with world, coordinates, and creation timestamp.</p>
 *
 * @see net.godlycow.org.essc.api.HomeApi#getHome(UUID, String)
 */
public record HomeEntry(

        /**
         * The UUID of the home owner.
         */
        UUID owner,

        /**
         * The unique name of this home.
         */
        String name,

        /**
         * The world name where this home is located.
         */
        String world,

        /**
         * The X coordinate.
         */
        double x,

        /**
         * The Y coordinate.
         */
        double y,

        /**
         * The Z coordinate.
         */
        double z,

        /**
         * The yaw rotation.
         */
        float yaw,

        /**
         * The pitch rotation.
         */
        float pitch,

        /**
         * The Unix timestamp (seconds) when this home was created.
         */
        long createdAt
) {

    /**
     * Returns the location as a Bukkit Location object.
     *
     * @param server the Bukkit server instance
     * @return the Location, or {@code null} if world is not loaded
     */
    public Location toLocation(org.bukkit.Server server) {
        org.bukkit.World w = server.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }

    /**
     * Returns a formatted coordinate string.
     *
     * @return formatted coordinates (x, y, z)
     */
    public String formatCoordinates() {
        return String.format("%.1f, %.1f, %.1f", x, y, z);
    }
}