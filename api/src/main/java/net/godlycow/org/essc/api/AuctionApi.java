package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.auction.Auction;
import net.godlycow.org.essc.api.auction.BuyHistoryEntry;
import net.godlycow.org.essc.api.auction.SellHistoryEntry;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's Auction House system.
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getAuctionApi()}.</p>
 *
 * <pre>{@code
 * AuctionApi ah = APIProvider.getAPI().getAuctionApi();
 *
 * // list all active auctions
 * List<Auction> auctions = ah.getActiveAuctions();
 *
 * // programmatically create a listing
 * ah.createAuction(player, item, BigDecimal.valueOf(500), 86400000L)
 *   .thenAccept(success -> {
 *       if (success) player.sendMessage("Listed!");
 *   });
 * }</pre>
 *
 * <p>Methods that interact with the economy or database return a
 * {@link CompletableFuture} and must <strong>not</strong> be awaited
 * on the main server thread.</p>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 * @see Auction
 * @see SellHistoryEntry
 * @see BuyHistoryEntry
 */
public interface AuctionApi {


    /**
     * Returns a snapshot of all currently active (non-expired, non-claimed) auctions.
     *
     * <p>The returned list is not backed by the internal auction map — modifications
     * to it have no effect on the Auction House. The order of entries is not
     * guaranteed.</p>
     *
     * @return a list of active {@link Auction}s; never {@code null}, may be empty
     */
    List<Auction> getActiveAuctions();

    /**
     * Returns all active auctions listed by the given player.
     *
     * <p>Returns an empty list if the player has no active listings. The returned
     * list is not backed by internal state.</p>
     *
     * @param uuid the UUID of the seller; must not be {@code null}
     * @return a list of that player's active {@link Auction}s; never {@code null}, may be empty
     */
    List<Auction> getPlayerAuctions(UUID uuid);

    /**
     * Looks up a single active auction by its database ID.
     *
     * @param id the auction ID to look up
     * @return an {@link Optional} containing the {@link Auction} if found and still active,
     *         or {@link Optional#empty()} if the ID is unknown or the auction has already
     *         been claimed or expired
     */
    Optional<Auction> getAuction(int id);

    /**
     * Returns the cached list of unclaimed expired items for the given player.
     *
     * <p>Items appear here when an auction expires without a buyer or is cancelled.
     * The list is loaded from the database on player join and kept in memory until
     * the player claims their items or disconnects. The returned list is a copy.</p>
     *
     * <p>Note: only items for <em>currently online</em> players are guaranteed to be
     * cached. For offline players the list will typically be empty even if unclaimed
     * items exist in the database.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return a list of {@link ItemStack}s awaiting claim; never {@code null}, may be empty
     */
    List<ItemStack> getExpiredItems(UUID uuid);

    /**
     * Returns whether the given player has any unclaimed expired items waiting.
     *
     * <p>Equivalent to checking {@code !getExpiredItems(uuid).isEmpty()} but avoids
     * allocating the list copy.</p>
     *
     * <p>Subject to the same online-only caching caveat as {@link #getExpiredItems(UUID)}.</p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return {@code true} if the player has at least one item awaiting claim;
     *         {@code false} if the cache is empty or the player is offline
     */
    boolean hasExpiredItems(UUID uuid);

    /**
     * Returns the sell history for the given player, most recent entry first.
     *
     * <p>The history is kept in memory and automatically capped at 100 entries per
     * player. Returns an empty list if no sales have been recorded yet for this
     * player, or if the player has never been online since the server started.</p>
     *
     * @param uuid the UUID of the seller; must not be {@code null}
     * @return a list of {@link SellHistoryEntry} records; never {@code null}, may be empty
     */
    List<SellHistoryEntry> getSellHistory(UUID uuid);

    /**
     * Returns the buy history for the given player, most recent entry first.
     *
     * <p>The history is kept in memory and automatically capped at 100 entries per
     * player. Returns an empty list if no purchases have been recorded yet for this
     * player, or if the player has never been online since the server started.</p>
     *
     * @param uuid the UUID of the buyer; must not be {@code null}
     * @return a list of {@link BuyHistoryEntry} records; never {@code null}, may be empty
     */
    List<BuyHistoryEntry> getBuyHistory(UUID uuid);


    /**
     * Creates a new auction listing on behalf of the given player.
     *
     * <p>Before inserting into the database, the following check is performed:</p>
     * <ul>
     *   <li>The seller's current active listing count is compared against the configured
     *       maximum. This check is bypassed if the player holds the
     *       {@code essentialsc.ah.bypass.limit} permission.</li>
     * </ul>
     *
     * <p>The provided {@link ItemStack} is cloned internally — you may safely modify
     * or discard the original after this call.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param seller   the player creating the listing; must not be {@code null}
     * @param item     the item to list; must not be {@code null}
     * @param price    the buy-now price; must be positive, must not be {@code null}
     * @param duration the auction duration in milliseconds; must be positive
     * @return a {@link CompletableFuture} resolving to {@code true} if the auction was
     *         created successfully, or {@code false} if the player is at their listing
     *         limit or the database insert failed
     */
    CompletableFuture<Boolean> createAuction(Player seller, ItemStack item, BigDecimal price, long duration);

    /**
     * Processes the purchase of an auction by the given player.
     *
     * <p>The future resolves to {@code false} and no action is taken if any of the
     * following conditions are met:</p>
     * <ul>
     *   <li>The auction ID does not exist or is already claimed/expired.</li>
     *   <li>The buyer is the seller of the auction.</li>
     *   <li>The buyer does not have sufficient funds.</li>
     *   <li>The auction is already being processed by a concurrent request.</li>
     * </ul>
     *
     * <p>On success: the buyer is charged, the item is delivered to their inventory
     * (dropped at their feet if the inventory is full), the seller receives payment,
     * and the auction is marked as claimed and removed from the active listings.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param buyer the player purchasing the auction; must not be {@code null}
     * @param id    the ID of the auction to buy
     * @return a {@link CompletableFuture} resolving to {@code true} on a successful
     *         purchase, {@code false} otherwise
     */
    CompletableFuture<Boolean> buyAuction(Player buyer, int id);

    /**
     * Cancels an active auction and returns the item to the seller's expired-items queue.
     *
     * <p>The future resolves to {@code false} and no action is taken if any of the
     * following conditions are met:</p>
     * <ul>
     *   <li>The auction ID does not exist in the active listings.</li>
     *   <li>The requesting player is not the seller and does not have the
     *       {@code essentialsc.ah.admin} permission.</li>
     *   <li>The auction is already being processed by a concurrent request.</li>
     * </ul>
     *
     * <p>On success, the cancelled item is placed in the seller's expired-items queue
     * and persisted to the database. The seller can retrieve it via
     * {@link #claimExpiredItems(Player)}.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param player the player requesting cancellation; must not be {@code null}
     * @param id     the ID of the auction to cancel
     * @return a {@link CompletableFuture} resolving to {@code true} if the auction
     *         was cancelled, {@code false} otherwise
     */
    CompletableFuture<Boolean> cancelAuction(Player player, int id);

    /**
     * Claims all expired items for the given player, delivering them to their inventory.
     *
     * <p>Items that do not fit in the player's inventory are dropped naturally at
     * their current location. If a claim operation is already in progress for this
     * player (concurrent guard), this method returns {@code false} immediately
     * without delivering anything.</p>
     *
     * <p>This method is synchronous and safe to call on the main thread.</p>
     *
     * @param player the player claiming their expired items; must not be {@code null}
     * @return {@code true} if at least one item was found and delivered successfully,
     *         {@code false} if the player had no expired items or a claim was already
     *         in progress for them
     */
    boolean claimExpiredItems(Player player);
}
