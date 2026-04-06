package net.godlycow.org.essc.api;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * API interface for interacting with EssentialsC's Random Teleport (RTP) system.
 *
 * <p>Allows external plugins to query RTP state, trigger teleports, and inspect
 * per-world configuration without direct dependency on internal classes.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getRtpApi()}.</p>
 *
 * <pre>{@code
 * RtpApi rtp = APIProvider.getAPI().getRtpApi();
 *
 * if (!rtp.isOnCooldown(player)) {
 *     World world = Bukkit.getWorld("world");
 *     rtp.startRTP(player, world);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface RtpApi {

    /**
     * Returns whether the RTP system is enabled globally in config.
     *
     * @return {@code true} if RTP is enabled
     */
    boolean isEnabled();

    /**
     * Returns whether RTP is enabled for the given world name.
     *
     * @param worldName the world to check; must not be {@code null}
     * @return {@code true} if RTP is enabled for that world
     */
    boolean isWorldEnabled(String worldName);

    /**
     * Returns whether the given player is currently on RTP cooldown.
     *
     * <p>Players with the {@code essentialsc.rtp.bypass.cooldown} permission
     * are never considered on cooldown.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player must wait before using RTP again
     */
    boolean isOnCooldown(Player player);

    /**
     * Returns the remaining cooldown time in seconds for the given player.
     *
     * <p>Returns {@code 0} if the player is not on cooldown or has bypass permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return remaining cooldown in seconds, or {@code 0}
     */
    long getRemainingCooldown(Player player);

    /**
     * Returns whether a random teleport is currently in progress for the given player
     * (i.e. they are in warmup or location-search phase).
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if an RTP is already running for this player
     */
    boolean isRtpInProgress(Player player);

    /**
     * Returns whether the given player has permission to use RTP in the specified world.
     *
     * @param player    the player to check; must not be {@code null}
     * @param worldName the world name; must not be {@code null}
     * @return {@code true} if the player has the required world permission
     */
    boolean hasWorldPermission(Player player, String worldName);

    /**
     * Returns the list of world names that are configured for RTP and currently loaded.
     *
     * @return an unmodifiable sorted list of world names; never {@code null}
     */
    List<String> getConfiguredWorldNames();

    /**
     * Returns the number of players currently in the given world.
     *
     * @param worldName the world to query; must not be {@code null}
     * @return the player count, or {@code 0} if the world is not loaded
     */
    int getPlayerCountInWorld(String worldName);

    /**
     * Initiates a random teleport for the given player in the specified world.
     *
     * <p>Performs all standard checks (permission, cooldown, world enabled, in-progress)
     * and sends the appropriate lang messages. Warmup is applied unless the player
     * has bypass permission.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the online player to teleport; must not be {@code null}
     * @param world  the world to teleport into; must not be {@code null}
     */
    void startRTP(Player player, World world);
}