package net.godlycow.org.essc.api.rtp;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RtpManager {
    boolean isRtpSystemEnabled();
    Collection<RtpWorldSettings> getConfiguredWorlds();
    RtpWorldSettings getWorldSettings(String worldName);
    boolean isWorldEnabled(String worldName);
    boolean isRtpInProgress(Player player);
    boolean isOnCooldown(Player player);
    long getRemainingCooldownSeconds(Player player);
    boolean hasBypassPermission(Player player, String type);
    boolean hasWorldPermission(Player player, String worldName);
    RtpPlayerState getPlayerState(Player player);
    RtpRequest getActiveRequest(Player player);
    CompletableFuture<RtpResult> requestRtp(Player player, World world);
    void cancelPendingRtp(Player player);
    List<String> getAvailableWorldNamesFor(Player player);
    int getPlayerCountInWorld(String worldName);
    boolean isWorldBorderGloballyEnabled();
    long getGlobalCooldownSeconds();
    long getGlobalWarmupSeconds();
    boolean isCancelOnMovementEnabled();
    boolean areParticlesEnabled();
    int getMaxSearchAttempts();
    int getGlobalMinY();
    int getGlobalMaxY();
}