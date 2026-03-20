package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.DiscordApi;
import net.godlycow.org.essc.discord.DiscordSRVHook;

import java.util.UUID;

public class DiscordApiImpl implements DiscordApi {

    private final DiscordSRVHook hook;

    public DiscordApiImpl(DiscordSRVHook hook) {
        this.hook = hook;
    }

    @Override
    public boolean isHooked() {
        return hook != null && hook.isHooked();
    }

    @Override
    public void sendBanEmbed(UUID targetUUID, String targetName, String reason, String banner, long expires) {
        if (isHooked()) hook.sendBanEmbed(targetUUID, targetName, reason, banner, expires);
    }

    @Override
    public void sendKickEmbed(UUID targetUUID, String targetName, String reason, String kicker) {
        if (isHooked()) hook.sendKickEmbed(targetUUID, targetName, reason, kicker);
    }

    @Override
    public void sendMuteEmbed(UUID targetUUID, String targetName, String reason, String muter, long expires) {
        if (isHooked()) hook.sendMuteEmbed(targetUUID, targetName, reason, muter, expires);
    }

    @Override
    public void sendHomeSetEmbed(UUID playerUUID, String playerName, String homeName,
                                 String worldName, int homeCount, int maxHomes) {
        if (isHooked()) hook.sendHomeSetEmbed(playerUUID, playerName, homeName, worldName, homeCount, maxHomes);
    }

    @Override
    public void sendHomeDeleteEmbed(UUID playerUUID, String playerName, String homeName,
                                    int remainingHomes, int maxHomes) {
        if (isHooked()) hook.sendHomeDeleteEmbed(playerUUID, playerName, homeName, remainingHomes, maxHomes);
    }
}