package net.godlycow.org.essc.api.home;

import org.bukkit.Location;
import org.bukkit.Server;

import java.util.UUID;

/**
 * A player's home. Read only, get them from {@link HomeManager}.
 * Coords are stored raw so they stay valid if the world is unloaded,
 * use {@link #toLocation(Server)} to turn them into a real location.
 */
public interface Home {
    /**
     * Gets the owner.
     *
     * @return the owner UUID
     */
    UUID getOwner();

    /**
     * Gets the home name.
     *
     * @return the home name
     */
    String getName();

    /**
     * Gets the world name.
     *
     * @return the world name
     */
    String getWorldName();

    /**
     * Gets the X coord.
     *
     * @return x
     */
    double getX();

    /**
     * Gets the Y coord.
     *
     * @return y
     */
    double getY();

    /**
     * Gets the Z coord.
     *
     * @return z
     */
    double getZ();

    /**
     * Gets the yaw.
     *
     * @return yaw
     */
    float getYaw();

    /**
     * Gets the pitch.
     *
     * @return pitch
     */
    float getPitch();

    /**
     * Gets when the home was created, in millis.
     *
     * @return creation time in millis
     */
    long getCreatedAt();

    /**
     * Turns the stored coords into a real location.
     *
     * @param server the server to resolve the world
     * @return the location, or null if the world is not loaded
     */
    Location toLocation(Server server);
}
