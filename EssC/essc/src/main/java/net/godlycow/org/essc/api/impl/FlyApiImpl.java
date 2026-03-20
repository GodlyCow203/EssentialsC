package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.FlyApi;
import net.godlycow.org.essc.fly.FlyManager;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class FlyApiImpl implements FlyApi {

    private final FlyManager manager;

    public FlyApiImpl(FlyManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isFlying(Player player) {
        return player.isFlying() && player.getAllowFlight();
    }

    @Override
    public boolean hasPersistentFly(UUID uuid) {
        return manager.hasPersistentFly(uuid);
    }

    @Override
    public void setFlying(Player player, boolean flying) {
        player.setAllowFlight(flying);
        player.setFlying(flying);
    }

    @Override
    public void toggleFlying(Player player) {
        setFlying(player, !isFlying(player));
    }

    @Override
    public void setPersistentFly(UUID uuid, boolean persistent) {
        manager.setPersistentFly(uuid, persistent);
    }

    @Override
    public Set<UUID> getPersistentFlyPlayers() {
        return manager.getPersistentFlyPlayers();
    }
}