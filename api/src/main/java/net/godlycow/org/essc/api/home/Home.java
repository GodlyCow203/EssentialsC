package net.godlycow.org.essc.api.home;

import net.godlycow.org.essc.api.HomeApi;
import org.bukkit.Location;
import org.bukkit.Server;

import java.util.UUID;

/**
 * Represents a saved home belonging to a player.
 *
 * <p>Instances are obtained via {@link HomeApi} — you never construct them directly.
 * Home names are always stored and compared in lowercase.</p>
 *
 * @see HomeApi
 */
public class Home {

    private final UUID owner;
    private final String name;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final float yaw;
    private final float pitch;
    private final long createdAt;

    public Home(UUID owner, String name, String world,
                double x, double y, double z,
                float yaw, float pitch, long createdAt) {
        this.owner = owner;
        this.name = name.toLowerCase();
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.createdAt = createdAt;
    }

    /**
     * Returns the UUID of the player who owns this home.
     *
     * @return the owner's UUID; never {@code null}
     */
    public UUID getOwner() { return owner; }

    /**
     * Returns the name of this home in lowercase.
     *
     * @return the home name; never {@code null}
     */
    public String getName() { return name; }

    /**
     * Returns the name of the world this home is located in.
     *
     * @return the world name; never {@code null}
     */
    public String getWorld() { return world; }

    /** @return the X coordinate of this home */
    public double getX() { return x; }

    /** @return the Y coordinate of this home */
    public double getY() { return y; }

    /** @return the Z coordinate of this home */
    public double getZ() { return z; }

    /** @return the yaw (horizontal rotation) of this home */
    public float getYaw() { return yaw; }

    /** @return the pitch (vertical rotation) of this home */
    public float getPitch() { return pitch; }

    /**
     * Returns the Unix timestamp (seconds) at which this home was created.
     *
     * @return the creation timestamp in seconds since epoch
     */
    public long getCreatedAt() { return createdAt; }

    /**
     * Resolves this home to a Bukkit {@link Location}.
     *
     * @param server the server instance used to look up the world; must not be {@code null}
     * @return the {@link Location}, or {@code null} if the world is not loaded
     */
    public Location toLocation(Server server) {
        org.bukkit.World w = server.getWorld(world);
        if (w == null) return null;
        return new Location(w, x, y, z, yaw, pitch);
    }
}