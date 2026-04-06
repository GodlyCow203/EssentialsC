package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.RtpApi;
import net.godlycow.org.essc.rtp.RTPManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public class RtpApiImpl implements RtpApi {

    private final RTPManager manager;

    public RtpApiImpl(RTPManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isEnabled() {
        return manager.isEnabled();
    }

    @Override
    public boolean isWorldEnabled(String worldName) {
        return manager.isWorldEnabled(worldName);
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
    public boolean isRtpInProgress(Player player) {
        return manager.isRtpInProgress(player);
    }

    @Override
    public boolean hasWorldPermission(Player player, String worldName) {
        return manager.hasWorldPermission(player, worldName);
    }

    @Override
    public List<String> getConfiguredWorldNames() {
        return manager.getConfiguredWorldNames();
    }

    @Override
    public int getPlayerCountInWorld(String worldName) {
        return manager.getPlayerCountInWorld(worldName);
    }

    @Override
    public void startRTP(Player player, World world) {
        manager.startRTP(player, world);
    }
}