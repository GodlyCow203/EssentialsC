package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.event.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class HomeImpl implements Home {
    private final UUID owner;
    private final String name;
    private final String world;
    private final double x, y, z;
    private final float yaw, pitch;
    private final long createdAt;

    public HomeImpl(UUID owner, String name, String world,
                    double x, double y, double z, float yaw, float pitch, long createdAt) {
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

    public HomeImpl(UUID owner, String name, Location location, long createdAt) {
        this(owner, name, location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch(), createdAt);
    }

    public static HomeImpl fromInternal(net.godlycow.org.essc.home.Home home) {
        return new HomeImpl(
                home.getOwner(),
                home.getName(),
                home.getWorld(),
                home.getX(), home.getY(), home.getZ(),
                home.getYaw(), home.getPitch(),
                home.getCreatedAt()
        );
    }

    @Override @NotNull public UUID getOwner() { return owner; }
    @Override @NotNull public String getName() { return name; }
    @Override @NotNull public String getWorld() { return world; }
    @Override public double getX() { return x; }
    @Override public double getY() { return y; }
    @Override public double getZ() { return z; }
    @Override public float getYaw() { return yaw; }
    @Override public float getPitch() { return pitch; }
    @Override public long getCreatedAt() { return createdAt; }

    @Override
    public @Nullable Location toLocation() {
        org.bukkit.World w = Bukkit.getWorld(world);
        return w != null ? new Location(w, x, y, z, yaw, pitch) : null;
    }

    @Override
    public @NotNull Home withLocation(@NotNull Location location) {
        return new HomeImpl(owner, name, location, System.currentTimeMillis() / 1000);
    }

    @Override
    public @NotNull Home withName(@NotNull String name) {
        return new HomeImpl(owner, name, world, x, y, z, yaw, pitch, createdAt);
    }
}