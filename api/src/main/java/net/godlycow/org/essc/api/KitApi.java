package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.kit.Kit;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * API interface for interacting with EssentialsC's kit system.
 *
 * <p>All methods are synchronous and safe to call on the main thread. Kit data
 * is loaded from {@code kits.yml} at startup and kept in memory.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getKitApi()}.</p>
 *
 * <pre>{@code
 * KitApi kits = APIProvider.getAPI().getKitApi();
 *
 * Kit starter = kits.getKit("starter");
 * if (starter != null && kits.canClaim(player, starter)) {
 *     kits.giveKit(player, starter);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 * @see Kit
 */
public interface KitApi {

    /**
     * Looks up a kit by its internal name.
     *
     * <p>Name matching is case-insensitive.</p>
     *
     * @param name the name of the kit to look up; must not be {@code null}
     * @return the {@link Kit}, or {@code null} if no kit with that name exists
     */
    Kit getKit(String name);

    /**
     * Returns all currently loaded kits.
     *
     * <p>The returned collection reflects the live kit map — it will change if kits
     * are reloaded. Do not assume a stable order.</p>
     *
     * @return a collection of all loaded {@link Kit}s; never {@code null}, may be empty
     */
    Collection<Kit> getKits();

    /**
     * Returns whether the given player has the permission to access the given kit.
     *
     * <p>Returns {@code true} if the player has the kit's specific permission node
     * or the {@code essentialsc.kits.admin} bypass permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check against; must not be {@code null}
     * @return {@code true} if the player has permission to claim this kit
     */
    boolean hasPermission(Player player, Kit kit);

    /**
     * Returns whether the given player is currently eligible to claim the given kit.
     *
     * <p>This checks, in order:</p>
     * <ul>
     *   <li>Whether the player has the required permission (see {@link #hasPermission}).</li>
     *   <li>Whether the kit is one-time and has already been claimed.</li>
     *   <li>Whether the kit's max-claims limit has been reached.</li>
     *   <li>Whether the player is still on cooldown (bypassed by {@code essentialsc.kits.admin}).</li>
     * </ul>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return {@code true} if the player may claim the kit right now
     */
    boolean canClaim(Player player, Kit kit);

    /**
     * Returns whether the given player has claimed the given kit at least once.
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return {@code true} if the player has at least one claim recorded
     */
    boolean hasClaimed(Player player, Kit kit);

    /**
     * Returns the number of times the given player has claimed the given kit.
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return the claim count; {@code 0} if the player has never claimed this kit
     */
    int getClaimCount(Player player, Kit kit);

    /**
     * Returns the number of seconds remaining on the given player's cooldown
     * for the given kit.
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return seconds until the player may claim again; {@code 0} if not on cooldown
     */
    long getCooldownRemaining(Player player, Kit kit);

    /**
     * Gives the given kit to the given player, bypassing all permission and cooldown
     * checks.
     *
     * <p>Items that do not fit in the player's inventory are dropped naturally at
     * their location. The claim is recorded in the database and the in-memory cache
     * is updated. A success message is sent to the player and a DiscordSRV embed is
     * dispatched if the integration is active.</p>
     *
     * <p>This method must be called on the main thread.</p>
     *
     * @param player the player to give the kit to; must not be {@code null} and must be online
     * @param kit    the kit to give; must not be {@code null}
     */
    void giveKit(Player player, Kit kit);
}