package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.BackApi;
import net.godlycow.org.essc.back.BackManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class BackApiImpl implements BackApi {

    private final BackManager manager;

    public BackApiImpl(BackManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean hasBackLocation(UUID uuid) {
        return manager.hasBackLocation(uuid);
    }

    @Override
    public Optional<Location> getBackLocation(UUID uuid) {
        return manager.getBackLocation(uuid);
    }

    @Override
    public boolean hasPendingTeleport(UUID uuid) {
        return manager.hasPendingTeleport(uuid);
    }

    @Override
    public boolean isOnCooldown(UUID uuid) {
        return manager.isOnCooldown(uuid);
    }

    @Override
    public long getRemainingCooldown(UUID uuid) {
        return manager.getRemainingCooldown(uuid);
    }

    @Override
    public long getWarmupSeconds() {
        return manager.getWarmupSeconds();
    }

    @Override
    public long getCooldownSeconds() {
        return manager.getCooldownSeconds();
    }

    @Override
    public boolean isParticlesEnabled() {
        return manager.isParticlesEnabled();
    }

    @Override
    public boolean isSoundsEnabled() {
        return manager.isSoundsEnabled();
    }

    @Override
    public boolean isCancelOnMovementEnabled() {
        return manager.isCancelOnMovementEnabled();
    }

    @Override
    public void setBackLocation(Player player, Location location) {
        manager.setBackLocation(player, location);
    }

    @Override
    public void removeBackLocation(Player player) {
        manager.removeBackLocation(player);
    }

    @Override
    public void teleportBack(Player player) {
        manager.teleportBack(player);
    }

    @Override
    public void cancelTeleport(Player player, String reason) {
        manager.cancelTeleport(player, reason);
    }
}