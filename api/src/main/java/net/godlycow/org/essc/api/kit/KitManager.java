package net.godlycow.org.essc.api.kit;

import org.bukkit.entity.Player;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * The kit system. Get it from {@link net.godlycow.org.essc.api.EssentialsCAPI#getKitManager()}.
 * If kits are disabled everything just returns empty or false.
 */
public interface KitManager {
    /**
     * Gets all loaded kits.
     *
     * @return loaded kits, never null
     */
    Collection<Kit> getLoadedKits();

    /**
     * Finds a kit by name.
     *
     * @param name the kit name
     * @return the kit, or null if not found
     */
    Kit findKitByName(String name);

    /**
     * Gets the kits a player can claim right now.
     *
     * @param player the player
     * @return available kits
     */
    Collection<Kit> getKitsAvailableTo(Player player);

    /**
     * Checks if the player's cooldown for a kit is over.
     *
     * @param player the player
     * @param kit the kit
     * @return true if no cooldown left
     */
    boolean hasCooldownExpiredFor(Player player, Kit kit);

    /**
     * Gets the remaining cooldown, fetching fresh data.
     *
     * @param player the player
     * @param kit the kit
     * @return future with remaining seconds, 0 if none
     */
    CompletableFuture<Long> fetchCooldownRemainingAsync(Player player, Kit kit);

    /**
     * Gets the cached remaining cooldown in seconds, 0 if none.
     *
     * @param player the player
     * @param kit the kit
     * @return remaining seconds
     */
    long getRemainingCooldownSeconds(Player player, Kit kit);

    /**
     * Checks if the player ever claimed the kit.
     *
     * @param player the player
     * @param kit the kit
     * @return true if claimed before
     */
    boolean hasPlayerClaimed(Player player, Kit kit);

    /**
     * Gets how many times the player claimed the kit.
     *
     * @param player the player
     * @param kit the kit
     * @return total claims
     */
    int getPlayerClaimCount(Player player, Kit kit);

    /**
     * Gets the player's claim history for the kit.
     *
     * @param player the player
     * @param kit the kit
     * @return the claim profile
     */
    KitClaimProfile fetchClaimProfile(Player player, Kit kit);

    /**
     * Checks if the player is allowed to claim the kit right now.
     *
     * @param player the player
     * @param kit the kit
     * @return true if allowed
     */
    boolean isClaimAllowedFor(Player player, Kit kit);

    /**
     * Checks if the player has the kit permission.
     *
     * @param player the player
     * @param kit the kit
     * @return true if permitted
     */
    boolean isPermittedToUse(Player player, Kit kit);

    /**
     * Reloads kits from disk.
     */
    void reloadKitDefinitions();

    /**
     * Claims the kit for the player. The future fails if the claim
     * is rejected (permission, cooldown, limits).
     *
     * @param player the player
     * @param kit the kit
     * @return future that completes when the items are given
     */
    CompletableFuture<Void> claimKitForPlayer(Player player, Kit kit);

    /**
     * Gets how many kits are loaded.
     *
     * @return loaded kit count
     */
    int getTotalLoadedKitCount();

    /**
     * Checks if a kit with this name is loaded.
     *
     * @param name the kit name
     * @return true if loaded
     */
    boolean isKitLoaded(String name);

    /**
     * Checks if custom item names are stripped on claim.
     *
     * @return true if stripped
     */
    boolean areItemNamesStrippedOnClaim();
}
