package net.godlycow.org.essc.api.event.shop;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface ShopManager {

    // Get a map of all available shop categories indexed by their ID
    @NotNull
    Map<String, ShopCategory> getCategories();

    // Look up a specific shop category by its ID string
    @Nullable
    ShopCategory getCategory(@NotNull String id);

    // Check if a category with the given ID exists in the system
    boolean hasCategory(@NotNull String id);

    // Get the total number of categories currently loaded
    int getCategoryCount();

    // Search for a specific shop item across all categories using its ID
    @NotNull
    CompletableFuture<Optional<ShopItem>> findItem(@NotNull String itemId);

    // Get a list of every item available within a specific category
    @NotNull
    CompletableFuture<List<ShopItem>> getItemsByCategory(@NotNull String categoryId);

    // Get the total count of all items listed in the entire shop
    int getTotalItemCount();

    // Process a purchase for a player and handle the economy and inventory changes
    @NotNull
    CompletableFuture<PurchaseResult> purchase(@NotNull Player player, @NotNull ShopItem item, int amount);

    // Convenience method to purchase an item using its ID string
    @NotNull
    CompletableFuture<PurchaseResult> purchase(@NotNull Player player, @NotNull String itemId, int amount);

    // Process a sale for a player, removing items and giving them money
    @NotNull
    CompletableFuture<SellResult> sell(@NotNull Player player, @NotNull ShopItem item, int amount);

    // Convenience method to sell an item using its ID string
    @NotNull
    CompletableFuture<SellResult> sell(@NotNull Player player, @NotNull String itemId, int amount);

    // Sell every matching item found in the player's inventory at once
    @NotNull
    CompletableFuture<SellResult> sellAll(@NotNull Player player, @NotNull ShopItem item);

    // Count how many of a specific shop item a player is currently carrying
    int getPlayerItemCount(@NotNull Player player, @NotNull ShopItem item);

    // Get the player's current money balance without lagging the main thread
    @NotNull
    CompletableFuture<BigDecimal> getBalance(@NotNull Player player);

    // Turn a money amount into a pretty string (e.g., "$1,000")
    @NotNull
    String formatBalance(@NotNull BigDecimal amount);

    // Check if a player has enough money to afford a specific cost
    @NotNull
    CompletableFuture<Boolean> hasEnough(@NotNull Player player, @NotNull BigDecimal amount);

    // Open the primary shop selection menu for a player
    void openMainShop(@NotNull Player player);

    // Open a specific shop category on a specific page for a player
    void openCategory(@NotNull Player player, @NotNull String categoryId, int page);

    // Open the first page of a specific shop category
    default void openCategory(@NotNull Player player, @NotNull String categoryId) {
        openCategory(player, categoryId, 1);
    }

    // Refresh all shop categories, items, and prices from the configuration
    void reload();

    // Check if the shop system is currently active and usable
    boolean isEnabled();

    // Get the name of the currency when there is only one (e.g., "Dollar")
    @NotNull
    String getCurrencySingular();

    // Get the name of the currency for multiple units (e.g., "Dollars")
    @NotNull
    String getCurrencyPlural();

    // (Admin) Set the available stock for a specific item
    @NotNull
    CompletableFuture<Boolean> setItemStock(@NotNull String itemId, int stock);

    // (Admin) Change the price it costs to buy a specific item
    @NotNull
    CompletableFuture<Boolean> setItemBuyPrice(@NotNull String itemId, double price);

    // (Admin) Change the money received when selling a specific item
    @NotNull
    CompletableFuture<Boolean> setItemSellPrice(@NotNull String itemId, double price);

    // Detailed information about the outcome of a purchase
    interface PurchaseResult {
        boolean success();
        @Nullable String getErrorMessage();
        int getAmountPurchased();
        double getTotalPrice();
        @Nullable ShopItem getItem();
    }

    // Detailed information about the outcome of a sale
    interface SellResult {
        boolean success();
        @Nullable String getErrorMessage();
        int getAmountSold();
        double getTotalPrice();
        @Nullable ShopItem getItem();
    }

    // A record of a single shop transaction (buy or sell)
    interface Transaction {
        @NotNull String getItemId();
        int getAmount();
        double getPrice();
        long getTimestamp();
        boolean isPurchase(); // true if they bought it, false if they sold it
    }
}