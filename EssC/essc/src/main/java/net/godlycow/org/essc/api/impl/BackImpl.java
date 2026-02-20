package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.event.back.Back;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BackImpl implements Back {
    private final UUID owner;
    private final String world;
    private final double x, y, z;
    private final float yaw, pitch;
    private final long timestamp;

    public BackImpl(@NotNull UUID owner, @NotNull String world,
                    double x, double y, double z, float yaw, float pitch, long timestamp) {
        this.owner = owner;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.timestamp = timestamp;
    }

    public BackImpl(@NotNull UUID owner, @NotNull Location location, long timestamp) {
        this(owner, location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(), timestamp);
    }

    public static BackImpl fromInternal(@NotNull UUID owner, @NotNull Location location) {
        return new BackImpl(owner, location, System.currentTimeMillis());
    }

    @Override @NotNull public UUID getOwner() { return owner; }
    @Override @NotNull public String getWorld() { return world; }
    @Override public double getX() { return x; }
    @Override public double getY() { return y; }
    @Override public double getZ() { return z; }
    @Override public float getYaw() { return yaw; }
    @Override public float getPitch() { return pitch; }
    @Override public long getTimestamp() { return timestamp; }

    @Override
    public @Nullable Location toLocation() {
        org.bukkit.World w = Bukkit.getWorld(world);
        return w != null ? new Location(w, x, y, z, yaw, pitch) : null;
    }

    @Override
    public @NotNull Back withLocation(@NotNull Location location) {
        return new BackImpl(owner, location, System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "BackImpl{" +
                "owner=" + owner +
                ", world='" + world + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}