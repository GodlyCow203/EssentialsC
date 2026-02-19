package net.godlycow.org.essc.api.event.auction;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface AuctionManager {

    // Get a list of all auctions that are currently active
    @NotNull
    List<Auction> getActiveAuctions();

    // Look for a specific auction using its ID number
    @NotNull
    Optional<Auction> getAuction(int id);

    // Get all auctions belonging to a specific player
    @NotNull
    List<Auction> getPlayerAuctions(@NotNull UUID playerUuid);

    // Count how many active auctions a player currently has
    int getPlayerAuctionCount(@NotNull UUID playerUuid);

    // Count every single active auction on the server
    int getTotalAuctionCount();

    // Get the maximum number of items a player is allowed to list at once
    int getMaxAuctionsPerPlayer();

    // Start a new auction for a player with a set item, price, and time
    @NotNull
    CompletableFuture<Boolean> createAuction(@NotNull Player seller, @NotNull ItemStack item,
                                             @NotNull BigDecimal price, long durationMs);

    // Handle a player buying an item, including money and item transfer
    @NotNull
    CompletableFuture<Boolean> buyAuction(@NotNull Player buyer, int auctionId);

    // A simpler way to buy an auction if you already have the auction object
    @NotNull
    default CompletableFuture<Boolean> buyAuction(@NotNull Player buyer, @NotNull Auction auction) {
        return buyAuction(buyer, auction.getId());
    }

    // Stop an auction and move the item to the seller's claim list
    @NotNull
    CompletableFuture<Boolean> cancelAuction(@NotNull Player seller, int auctionId);

    // Get the list of items a player needs to pick up (expired or cancelled)
    @NotNull
    List<ItemStack> getPlayerExpiredItems(@NotNull UUID playerUuid);

    // Give a player all their waiting items back
    boolean claimExpiredItems(@NotNull Player player);

    // Check if a player has any items waiting to be claimed
    boolean hasExpiredItems(@NotNull UUID playerUuid);

    // Check if the Auction House system is turned on
    boolean isEnabled();

    // Refresh all auction data from the database
    void reload();

    // Get the lowest price anyone is allowed to set
    @NotNull
    BigDecimal getMinPrice();

    // Get the highest price anyone is allowed to set
    @Nullable
    BigDecimal getMaxPrice();

    // Get the standard amount of time an auction lasts
    long getDefaultDuration();

    /* Permission Strings */
    String PERM_USE = "essentialsc.ah.use";
    String PERM_SELL = "essentialsc.ah.sell";
    String PERM_BUY = "essentialsc.ah.buy";
    String PERM_CANCEL = "essentialsc.ah.cancel";
    String PERM_ADMIN = "essentialsc.ah.admin";
    String PERM_BYPASS_LIMIT = "essentialsc.ah.bypass.limit";
    String PERM_BYPASS_PRICE_MIN = "essentialsc.ah.bypass.price.min";
    String PERM_BYPASS_PRICE_MAX = "essentialsc.ah.bypass.price.max";
    String PERM_RELOAD = "essentialsc.ah.reload";
}