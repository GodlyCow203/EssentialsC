package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.VanishApi;
import net.godlycow.org.essc.vanish.VanishManager;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public class VanishApiImpl implements VanishApi {

    private final VanishManager manager;

    public VanishApiImpl(VanishManager manager) {
        this.manager = manager;
    }

    @Override
    public void vanish(Player player) {
        manager.vanish(player);
    }

    @Override
    public void unvanish(Player player) {
        manager.unvanish(player);
    }

    @Override
    public boolean isVanished(Player player) {
        return manager.isVanished(player);
    }

    @Override
    public Set<UUID> getVanishedPlayers() {
        return manager.getVanishedPlayers();
    }

    @Override
    public boolean canSeeVanished(Player player) {
        return player.hasPermission("essentialsc.vanish.see");
    }

    @Override
    public void reload() {
        manager.loadConfig();
    }
}