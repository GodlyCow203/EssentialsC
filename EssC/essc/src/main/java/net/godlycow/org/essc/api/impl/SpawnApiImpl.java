package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.SpawnApi;
import net.godlycow.org.essc.spawn.SpawnManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpawnApiImpl implements SpawnApi {

    private final SpawnManager manager;

    public SpawnApiImpl(SpawnManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isSpawnSet() {
        return manager.isSpawnSet();
    }

    @Override
    public Location getSpawn() {
        return manager.getSpawn();
    }

    @Override
    public void setSpawn(Location location) {
        manager.setSpawn(location);
    }

    @Override
    public boolean isOnCooldown(Player player) {
        return manager.isOnCooldown(player);
    }

    @Override
    public long getRemainingCooldown(Player player) {
        return manager.getRemainingCooldown(player);
    }

    @Override
    public boolean hasPendingTeleport(Player player) {
        return manager.hasPendingTeleport(player);
    }

    @Override
    public void teleportToSpawn(Player player) {
        manager.teleportToSpawn(player, false);
    }

    @Override
    public void teleportToSpawnImmediate(Player player) {
        manager.teleportToSpawn(player, true);
    }

    @Override
    public void cancelTeleport(Player player) {
        manager.cancelTeleport(player);
    }

    @Override
    public void reload() {
        manager.reload();
    }
}
