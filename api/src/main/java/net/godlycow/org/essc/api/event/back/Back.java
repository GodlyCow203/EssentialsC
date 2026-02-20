package net.godlycow.org.essc.api.event.back;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface Back {

    // Get the Unique ID of the player who owns this back location
    @NotNull
    UUID getOwner();

    // Get the name of the world the back location is in
    @NotNull
    String getWorld();

    // Get the X (East/West) coordinate
    double getX();

    // Get the Y (Height) coordinate
    double getY();

    // Get the Z (North/South) coordinate
    double getZ();

    // Get the horizontal direction the player was looking
    float getYaw();

    // Get the vertical direction (up/down) the player was looking
    float getPitch();

    // Get the exact time when this back location was saved
    long getTimestamp();

    // Turn this data into a Minecraft location (returns null if world is missing)
    @Nullable
    Location toLocation();

    // Create a copy of this back location at a new spot
    @NotNull
    Back withLocation(@NotNull Location location);
}