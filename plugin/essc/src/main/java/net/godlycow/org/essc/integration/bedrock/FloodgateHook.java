package net.godlycow.org.essc.integration.bedrock;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.UUID;

public class FloodgateHook {

    private final EssentialsC plugin;
    private FloodgateApi api;
    private boolean available;

    public FloodgateHook(EssentialsC plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        if (!plugin.getServer().getPluginManager().isPluginEnabled("floodgate")) {
            plugin.debug("Floodgate not found — Bedrock player detection disabled.");
            this.available = false;
            return;
        }

        try {
            this.api = FloodgateApi.getInstance();
            this.available = true;
            plugin.debug("Floodgate hook initialized. Prefix: \"" + getPrefix() + "\"");
        } catch (Exception e) {
            plugin.getLogger().warning("Floodgate is installed but the API could not be loaded: " + e.getMessage());
            this.available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isBedrockPlayer(UUID uuid) {
        if (!available) return false;
        try {
            return api.isFloodgatePlayer(uuid);
        } catch (Exception e) {
            plugin.debug("FloodgateHook: isFloodgatePlayer threw for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean isBedrockPlayer(Player player) {
        return isBedrockPlayer(player.getUniqueId());
    }

    public boolean isBedrockId(UUID uuid) {
        if (!available || uuid == null)
            return false;
        try {
            return api.isFloodgateId(uuid) || api.isFloodgatePlayer(uuid);
        } catch (Exception e) {
            plugin.debug("FloodgateHook: isFloodgateId threw for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public String getGamertag(UUID uuid) {
        FloodgatePlayer fp = getFloodgatePlayer(uuid);
        return fp == null ? null : fp.getUsername();
    }

    public String getDeviceOs(UUID uuid) {

        FloodgatePlayer fp = getFloodgatePlayer(uuid);
        if (fp == null | fp.getDeviceOs() ==   null)
            return null;

        return fp.getDeviceOs().toString();
    }

    public String getInputMode(UUID uuid) {
        FloodgatePlayer fp = getFloodgatePlayer(uuid);
        if (fp == null || fp.getInputMode() == null)
            return null;

        return fp.getInputMode().name();
    }

    public int getBedrockPlayerCount() {
        if (!available)
            return 0;
        try {
            return api.getPlayerCount();
        } catch (Exception e) {
            return 0;
        }
    }

    private FloodgatePlayer getFloodgatePlayer(UUID uuid) {
        if (!available || uuid == null)
            return null;
        try {
            return api.getPlayer(uuid);
        } catch (Exception e) {
            plugin.debug("FloodgateHook: getPlayer threw for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    public String getPrefix() {
        if (!available) return "";
        try {
            return String.valueOf(api.getPlayerPrefix());
        } catch (Exception e) {
            return ".";
        }
    }
}
