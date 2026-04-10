package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.kit.Kit;
import net.godlycow.org.essc.api.kit.KitSyncHook;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's kit system.
 *
 * <p>Most methods are synchronous and safe to call on the main thread. Kit data
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
     * <p><strong>Note:</strong> This method only checks local cooldown data. For kits with
     * {@link Kit#isNetworkSync()} enabled, use {@link #canClaimAsync(Player, Kit)} to ensure
     * network-wide cooldowns are respected.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return {@code true} if the player may claim the kit right now
     * @see #canClaimAsync(Player, Kit)
     */
    boolean canClaim(Player player, Kit kit);

    /**
     * Returns whether the given player is currently eligible to claim the given kit,
     * checking network-wide cooldowns if the kit has network sync enabled.
     *
     * <p>This is the recommended method for checking claim eligibility when dealing
     * with kits that may have {@link Kit#isNetworkSync()} enabled. It performs an
     * async lookup to the network sync hook if one is registered.</p>
     *
     * <p>The returned future completes with {@code true} if:</p>
     * <ul>
     *   <li>The player has permission</li>
     *   <li>The kit is not one-time already claimed</li>
     *   <li>Max claims not exceeded</li>
     *   <li>No local or network cooldown is active</li>
     * </ul>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return a future that completes with {@code true} if the player may claim the kit
     */
    CompletableFuture<Boolean> canClaimAsync(Player player, Kit kit);

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
     * <p><strong>Note:</strong> This method only returns local cooldown data.
     * For kits with {@link Kit#isNetworkSync()} enabled, use
     * {@link #getCooldownRemainingAsync(Player, Kit)} to get the accurate
     * network-wide cooldown.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return seconds until the player may claim again; {@code 0} if not on cooldown
     * @see #getCooldownRemainingAsync(Player, Kit)
     */
    long getCooldownRemaining(Player player, Kit kit);

    /**
     * Returns the number of seconds remaining on the given player's cooldown
     * for the given kit, checking network-wide cooldowns if applicable.
     *
     * <p>If the kit has {@link Kit#isNetworkSync()} enabled and a network sync hook
     * is registered, this method queries the network for the most recent claim time
     * and returns the maximum of local and network cooldowns.</p>
     *
     * <p>Players with {@code essentialsc.kits.networksync.bypass} permission skip
     * the network cooldown check.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @param kit    the kit to check; must not be {@code null}
     * @return a future that completes with seconds remaining; {@code 0} if not on cooldown
     */
    CompletableFuture<Long> getCooldownRemainingAsync(Player player, Kit kit);

    /**
     * Gives the given kit to the given player, bypassing all permission and cooldown
     * checks.
     *
     * <p>Items that do not fit in the player's inventory are dropped naturally at
     * their location. The claim is recorded in the database and the in-memory cache
     * is updated. A success message is sent to the player and a DiscordSRV embed is
     * dispatched if the integration is active.</p>
     *
     * <p>If the kit has {@link Kit#isNetworkSync()} enabled and a network sync hook
     * is registered, the claim is broadcast to the network.</p>
     *
     * <p>This method must be called on the main thread.</p>
     *
     * @param player the player to give the kit to; must not be {@code null} and must be online
     * @param kit    the kit to give; must not be {@code null}
     */
    void giveKit(Player player, Kit kit);

    /**
     * Registers a network synchronization hook for cross-server kit claim tracking.
     *
     * <p>This hook is called when:</p>
     * <ul>
     *   <li>A player claims a kit with {@link Kit#isNetworkSync()} enabled</li>
     *   <li>Async cooldown checks need to query network-wide claim times</li>
     * </ul>
     *
     * @param hook the sync hook to register, or {@code null} to clear
     */
    void setNetworkSyncHook(KitSyncHook hook);

    /**
     * Returns the currently registered network sync hook, or {@code null} if none.
     *
     * @return the active {@link KitSyncHook}, or {@code null}
     */
    KitSyncHook getNetworkSyncHook();

    /**
     * Clears the network synchronization hook.
     *
     * <p>After calling this, network sync features will be disabled until a new
     * hook is registered.</p>
     */
    default void clearNetworkSyncHook() {
        setNetworkSyncHook(null);
    }
}