package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player home with all its data.
 * This is immutable - to modify, delete and recreate.
 */
public interface Home {

    /**
     * Gets the owner's UUID.
     * @return The owner's UUID
     */
    @NotNull
    UUID getOwner();

    /**
     * Gets the home name (lowercase).
     * @return The home name
     */
    @NotNull
    String getName();

    /**
     * Gets the world name.
     * @return The world name
     */
    @NotNull
    String getWorld();

    /**
     * Gets the X coordinate.
     * @return X coordinate
     */
    double getX();

    /**
     * Gets the Y coordinate.
     * @return Y coordinate
     */
    double getY();

    /**
     * Gets the Z coordinate.
     * @return Z coordinate
     */
    double getZ();

    /**
     * Gets the yaw rotation.
     * @return Yaw
     */
    float getYaw();

    /**
     * Gets the pitch rotation.
     * @return Pitch
     */
    float getPitch();

    /**
     * Gets the creation timestamp (Unix epoch seconds).
     * @return Creation time
     */
    long getCreatedAt();

    /**
     * Converts to Bukkit Location.
     * @return Location, or null if world is not loaded
     */
    @Nullable
    Location toLocation();

    /**
     * Creates a new Home instance with modified location.
     * @param location New location
     * @return New Home instance
     */
    @NotNull
    Home withLocation(@NotNull Location location);

    /**
     * Creates a new Home instance with modified name.
     * @param name New name
     * @return New Home instance
     */
    @NotNull
    Home withName(@NotNull String name);
}