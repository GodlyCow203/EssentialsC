package net.godlycow.org.essc.api.event.home;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface Home {

    // Get the Unique ID of the player who owns this home
    @NotNull
    UUID getOwner();

    // Get the name of the home (like "base" or "farm")
    @NotNull
    String getName();

    // Get the name of the world the home is located in
    @NotNull
    String getWorld();

    // Get the X (East/West) coordinate of the home
    double getX();

    // Get the Y (Height) coordinate of the home
    double getY();

    // Get the Z (North/South) coordinate of the home
    double getZ();

    // Get the horizontal direction the player is looking
    float getYaw();

    // Get the vertical direction (up/down) the player is looking
    float getPitch();

    // Get the exact time the home was created
    long getCreatedAt();

    // Turn this data into a Minecraft location (returns null if the world is missing)
    @Nullable
    Location toLocation();

    // Create a copy of this home at a new spot while keeping the same name
    @NotNull
    Home withLocation(@NotNull Location location);

    // Create a copy of this home with a new name while keeping the same spot
    @NotNull
    Home withName(@NotNull String name);
}