package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.punishment.BanEntry;
import net.godlycow.org.essc.api.punishment.IpBanEntry;
import net.godlycow.org.essc.api.punishment.MuteEntry;

import java.util.List;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's punishment system.
 *
 * <p>Bans and mutes are stored in YAML files ({@code bans.yml} / {@code mutes.yml})
 * and checked entirely in memory after load. All methods are synchronous and safe
 * to call on the main thread.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getPunishmentApi()}.</p>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 * @see BanEntry
 * @see MuteEntry
 * @see IpBanEntry
 */
public interface PunishmentApi {

    /**
     * Issues a ban for the given player.
     *
     * <p>Writes the ban to {@code bans.yml} immediately. If the player is online
     * they will not be kicked automatically — use {@code Player#kick()} after
     * calling this method.</p>
     *
     * @param uuid    the UUID of the player to ban; must not be {@code null}
     * @param name    the player's current name; must not be {@code null}
     * @param reason  the ban reason; must not be {@code null}
     * @param banner  the name of the staff member or console issuing the ban; must not be {@code null}
     * @param expires the Unix timestamp (milliseconds) when the ban expires,
     *                or {@code -1} for a permanent ban
     */
    void banPlayer(UUID uuid, String name, String reason, String banner, long expires);

    /**
     * Removes the ban for the given player.
     *
     * <p>Has no effect if the player is not currently banned.</p>
     *
     * @param uuid the UUID of the player to unban; must not be {@code null}
     */
    void unbanPlayer(UUID uuid);

    /**
     * Returns whether the given player is currently banned.
     *
     * <p>Expired temporary bans are removed automatically when this method is called.</p>
     *
     * @param uuid the UUID of the player to check; must not be {@code null}
     * @return {@code true} if the player has an active ban
     */
    boolean isBanned(UUID uuid);

    /**
     * Returns the {@link BanEntry} for the given player, or {@code null} if
     * the player is not banned.
     *
     * <p>Expired bans are cleaned up automatically before this returns.</p>
     *
     * @param uuid the UUID of the player to look up; must not be {@code null}
     * @return the active {@link BanEntry}, or {@code null} if the player is not banned
     */
    BanEntry getBanEntry(UUID uuid);

    /**
     * Returns a list of all currently active ban entries.
     *
     * <p>Expired bans encountered during iteration are removed automatically.
     * The returned list is a snapshot and is not backed by internal state.</p>
     *
     * @return a list of all active {@link BanEntry} records; never {@code null}, may be empty
     */
    List<BanEntry> getActiveBans();

    /**
     * Returns a list of all ban entries including expired ones.
     *
     * <p>The returned list is a snapshot and is not backed by internal state.</p>
     *
     * @return a list of all {@link BanEntry} records; never {@code null}, may be empty
     */
    List<BanEntry> getAllBans();

    /**
     * Issues an IP ban.
     *
     * <p>Writes the IP ban to {@code bans.yml} immediately. Online players
     * with the banned IP will not be kicked automatically.</p>
     *
     * @param ip      the IP address to ban; must not be {@code null}
     * @param reason  the ban reason; must not be {@code null}
     * @param banner  the name of the staff member or console issuing the ban; must not be {@code null}
     * @param expires the Unix timestamp (milliseconds) when the ban expires,
     *                or {@code -1} for a permanent ban
     */
    void banIp(String ip, String reason, String banner, long expires);

    /**
     * Removes the IP ban for the given address.
     *
     * <p>Has no effect if the IP is not currently banned.</p>
     *
     * @param ip the IP address to unban; must not be {@code null}
     */
    void unbanIp(String ip);

    /**
     * Returns whether the given IP address is currently banned.
     *
     * <p>Expired temporary IP bans are removed automatically when this method is called.</p>
     *
     * @param ip the IP address to check; must not be {@code null}
     * @return {@code true} if the IP has an active ban
     */
    boolean isIpBanned(String ip);

    /**
     * Returns the {@link IpBanEntry} for the given IP, or {@code null} if
     * the IP is not banned.
     *
     * <p>Expired IP bans are cleaned up automatically before this returns.</p>
     *
     * @param ip the IP address to look up; must not be {@code null}
     * @return the active {@link IpBanEntry}, or {@code null} if the IP is not banned
     */
    IpBanEntry getIpBanEntry(String ip);

    /**
     * Returns a list of all currently active IP ban entries.
     *
     * <p>Expired IP bans encountered during iteration are removed automatically.
     * The returned list is a snapshot and is not backed by internal state.</p>
     *
     * @return a list of all active {@link IpBanEntry} records; never {@code null}, may be empty
     */
    List<IpBanEntry> getActiveIpBans();

    /**
     * Returns a list of all IP ban entries including expired ones.
     *
     * <p>The returned list is a snapshot and is not backed by internal state.</p>
     *
     * @return a list of all {@link IpBanEntry} records; never {@code null}, may be empty
     */
    List<IpBanEntry> getAllIpBans();


    /**
     * Issues a mute for the given player.
     *
     * <p>Writes the mute to {@code mutes.yml} immediately. Chat messages from
     * this player will be blocked by EssentialsC's mute listener automatically.</p>
     *
     * @param uuid    the UUID of the player to mute; must not be {@code null}
     * @param name    the player's current name; must not be {@code null}
     * @param reason  the mute reason; must not be {@code null}
     * @param muter   the name of the staff member or console issuing the mute; must not be {@code null}
     * @param expires the Unix timestamp (milliseconds) when the mute expires,
     *                or {@code -1} for a permanent mute
     */
    void mutePlayer(UUID uuid, String name, String reason, String muter, long expires);

    /**
     * Removes the mute for the given player.
     *
     * <p>Has no effect if the player is not currently muted.</p>
     *
     * @param uuid the UUID of the player to unmute; must not be {@code null}
     */
    void unmutePlayer(UUID uuid);

    /**
     * Returns whether the given player is currently muted.
     *
     * <p>Expired temporary mutes are removed automatically when this method is called.</p>
     *
     * @param uuid the UUID of the player to check; must not be {@code null}
     * @return {@code true} if the player has an active mute
     */
    boolean isMuted(UUID uuid);

    /**
     * Returns the {@link MuteEntry} for the given player, or {@code null} if
     * the player is not muted.
     *
     * <p>Expired mutes are cleaned up automatically before this returns.</p>
     *
     * @param uuid the UUID of the player to look up; must not be {@code null}
     * @return the active {@link MuteEntry}, or {@code null} if the player is not muted
     */
    MuteEntry getMuteEntry(UUID uuid);

    /**
     * Returns a list of all currently active mute entries.
     *
     * <p>Expired mutes encountered during iteration are removed automatically.
     * The returned list is a snapshot and is not backed by internal state.</p>
     *
     * @return a list of all active {@link MuteEntry} records; never {@code null}, may be empty
     */
    List<MuteEntry> getAllMutes();
}