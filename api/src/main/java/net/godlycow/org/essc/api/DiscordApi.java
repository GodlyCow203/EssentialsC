package net.godlycow.org.essc.api;

import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's DiscordSRV integration.
 *
 * <p>Lets external plugins push event embeds to the configured Discord punishment
 * channel through EssentialsC's existing DiscordSRV connection. All send methods
 * are no-ops if DiscordSRV is not hooked — check {@link #isHooked()} first if you
 * need to branch on availability.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getDiscordApi()}.</p>
 *
 * <pre>{@code
 * DiscordApi discord = APIProvider.getAPI().getDiscordApi();
 *
 * if (discord.isHooked()) {
 *     discord.sendBanEmbed(player.getUniqueId(), player.getName(),
 *             "Cheating", "AdminName", -1L);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface DiscordApi {

    /**
     * Returns whether the DiscordSRV hook is fully established and the JDA instance
     * is available.
     *
     * <p>This is {@code false} if DiscordSRV is not installed, if the Discord bot has
     * not yet connected, or if DiscordSRV integration is disabled in the EssentialsC
     * config. All {@code send*} methods silently do nothing when this returns
     * {@code false}.</p>
     *
     * @return {@code true} if DiscordSRV is connected and ready to send messages
     */
    boolean isHooked();

    /**
     * Sends a ban notification embed to the configured Discord punishments channel.
     *
     * <p>Has no effect if {@link #isHooked()} is {@code false}.</p>
     *
     * @param targetUUID the UUID of the banned player; must not be {@code null}
     * @param targetName the name of the banned player; must not be {@code null}
     * @param reason     the ban reason displayed in the embed; must not be {@code null}
     * @param banner     the name of the staff member or console who issued the ban; must not be {@code null}
     * @param expires    the Unix timestamp (milliseconds) when the ban expires, or {@code -1} for permanent
     */
    void sendBanEmbed(UUID targetUUID, String targetName, String reason, String banner, long expires);

    /**
     * Sends a kick notification embed to the configured Discord punishments channel.
     *
     * <p>Has no effect if {@link #isHooked()} is {@code false}.</p>
     *
     * @param targetUUID the UUID of the kicked player; must not be {@code null}
     * @param targetName the name of the kicked player; must not be {@code null}
     * @param reason     the kick reason displayed in the embed; must not be {@code null}
     * @param kicker     the name of the staff member or console who issued the kick; must not be {@code null}
     */
    void sendKickEmbed(UUID targetUUID, String targetName, String reason, String kicker);

    /**
     * Sends a mute notification embed to the configured Discord punishments channel.
     *
     * <p>Has no effect if {@link #isHooked()} is {@code false}.</p>
     *
     * @param targetUUID the UUID of the muted player; must not be {@code null}
     * @param targetName the name of the muted player; must not be {@code null}
     * @param reason     the mute reason displayed in the embed; must not be {@code null}
     * @param muter      the name of the staff member or console who issued the mute; must not be {@code null}
     * @param expires    the Unix timestamp (milliseconds) when the mute expires, or {@code -1} for permanent
     */
    void sendMuteEmbed(UUID targetUUID, String targetName, String reason, String muter, long expires);

    /**
     * Sends a home-set notification embed to the configured Discord channel.
     *
     * <p>Has no effect if {@link #isHooked()} is {@code false}.</p>
     *
     * @param playerUUID the UUID of the player who set the home; must not be {@code null}
     * @param playerName the name of the player; must not be {@code null}
     * @param homeName   the name given to the new home; must not be {@code null}
     * @param worldName  the name of the world the home was set in; must not be {@code null}
     * @param homeCount  the player's current total number of homes after setting this one
     * @param maxHomes   the player's current home limit
     */
    void sendHomeSetEmbed(UUID playerUUID, String playerName, String homeName,
                          String worldName, int homeCount, int maxHomes);

    /**
     * Sends a home-deleted notification embed to the configured Discord channel.
     *
     * <p>Has no effect if {@link #isHooked()} is {@code false}.</p>
     *
     * @param playerUUID     the UUID of the player who deleted the home; must not be {@code null}
     * @param playerName     the name of the player; must not be {@code null}
     * @param homeName       the name of the home that was deleted; must not be {@code null}
     * @param remainingHomes the number of homes the player has left after the deletion
     * @param maxHomes       the player's current home limit
     */
    void sendHomeDeleteEmbed(UUID playerUUID, String playerName, String homeName,
                             int remainingHomes, int maxHomes);
}