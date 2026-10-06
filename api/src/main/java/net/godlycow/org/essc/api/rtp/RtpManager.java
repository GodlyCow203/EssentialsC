package net.godlycow.org.essc.api.rtp;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * The RTP system. Get it from {@link net.godlycow.org.essc.api.EssentialsCAPI#getRtpManager()}.
 * Calling requestRtp runs the whole flow: checks, warmup, search and teleport.
 */
public interface RtpManager {
    /**
     * Checks if RTP is enabled.
     *
     * @return true if enabled
     */
    boolean isRtpSystemEnabled();

    /**
     * Gets every configured world.
     *
     * @return configured worlds, never null
     */
    Collection<RtpWorldSettings> getConfiguredWorlds();

    /**
     * Gets the settings for one world.
     *
     * @param worldName the world name
     * @return the settings, or null if not configured
     */
    RtpWorldSettings getWorldSettings(String worldName);

    /**
     * Checks if RTP is on for a world.
     *
     * @param worldName the world name
     * @return true if enabled
     */
    boolean isWorldEnabled(String worldName);

    /**
     * Checks if the player has an RTP running right now.
     *
     * @param player the player
     * @return true if in progress
     */
    boolean isRtpInProgress(Player player);

    /**
     * Checks if the player is on RTP cooldown.
     *
     * @param player the player
     * @return true if on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Gets remaining RTP cooldown in seconds, 0 if none.
     *
     * @param player the player
     * @return remaining seconds
     */
    long getRemainingCooldownSeconds(Player player);

    /**
     * Checks if the player skips an RTP restriction, like cooldown or warmup.
     *
     * @param player the player
     * @param type the bypass type
     * @return true if bypassed
     */
    boolean hasBypassPermission(Player player, String type);

    /**
     * Checks if the player may RTP in a world.
     *
     * @param player the player
     * @param worldName the world name
     * @return true if allowed
     */
    boolean hasWorldPermission(Player player, String worldName);

    /**
     * Gets a snapshot of the player's RTP state.
     *
     * @param player the player
     * @return the player state
     */
    RtpPlayerState getPlayerState(Player player);

    /**
     * Gets the player's active RTP request, if any.
     *
     * @param player the player
     * @return the active request, or null if none
     */
    RtpRequest getActiveRequest(Player player);

    /**
     * Runs an RTP for the player in the given world.
     *
     * @param player the player
     * @param world the world to teleport in
     * @return future with the result
     */
    CompletableFuture<RtpResult> requestRtp(Player player, World world);

    /**
     * Cancels the player's pending RTP, if any.
     *
     * @param player the player
     */
    void cancelPendingRtp(Player player);

    /**
     * Gets the worlds the player may RTP in.
     *
     * @param player the player
     * @return usable world names
     */
    List<String> getAvailableWorldNamesFor(Player player);

    /**
     * Gets how many players are counted in a world.
     *
     * @param worldName the world name
     * @return player count
     */
    int getPlayerCountInWorld(String worldName);

    /**
     * Checks if RTP respects the vanilla world border.
     *
     * @return true if enforced
     */
    boolean isWorldBorderGloballyEnabled();

    /**
     * Gets the global RTP cooldown in seconds.
     *
     * @return cooldown seconds
     */
    long getGlobalCooldownSeconds();

    /**
     * Gets the global RTP warmup in seconds.
     *
     * @return warmup seconds
     */
    long getGlobalWarmupSeconds();

    /**
     * Checks if moving cancels a pending warmup.
     *
     * @return true if movement cancels
     */
    boolean isCancelOnMovementEnabled();

    /**
     * Checks if RTP particles are on.
     *
     * @return true if enabled
     */
    boolean areParticlesEnabled();

    /**
     * Gets max safe-spot search attempts per request.
     *
     * @return max attempts
     */
    int getMaxSearchAttempts();

    /**
     * Gets the lowest Y used in the search.
     *
     * @return min Y
     */
    int getGlobalMinY();

    /**
     * Gets the highest Y used in the search.
     *
     * @return max Y
     */
    int getGlobalMaxY();
}
