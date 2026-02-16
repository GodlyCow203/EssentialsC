package net.godlycow.org.essc.api;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Represents a player home. This is immutable, so if you wanna change something
 * you gotta delete and recreate it.
 */
public interface Home {

    /** Who owns this home */
    @NotNull
    UUID getOwner();

    /** Name of the home (always lowercase internally) */
    @NotNull
    String getName();

    /** World name where the home is */
    @NotNull
    String getWorld();

    /** X coordinate */
    double getX();

    /** Y coordinate */
    double getY();

    /** Z coordinate */
    double getZ();

    /** Yaw rotation (where youre looking left/right) */
    float getYaw();

    /** Pitch rotation (where youre looking up/down) */
    float getPitch();

    /** When the home was created (unix timestamp in seconds) */
    long getCreatedAt();

    /**
     * Converts to Bukkit Location. Returns null if the world isnt loaded.
     */
    @Nullable
    Location toLocation();

    /**
     * Creates a copy of this home with a different location.
     * Useful if you wanna move a home without changing its name.
     */
    @NotNull
    Home withLocation(@NotNull Location location);

    /**
     * Creates a copy of this home with a different name.
     */
    @NotNull
    Home withName(@NotNull String name);
}