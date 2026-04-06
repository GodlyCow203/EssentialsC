package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.shop.ShopCategoryEntry;
import net.godlycow.org.essc.api.shop.ShopItemEntry;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's shop system.
 *
 * <p>The shop system provides GUI-based buying and selling with category
 * organization, stock management, and transaction logging to SQLite.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getShopApi()}.</p>
 *
 * <pre>{@code
 * ShopApi shop = APIProvider.getAPI().getShopApi();
 *
 * shop.getCategory("farming").ifPresent(cat -> {
 *     player.sendMessage("Category: " + cat.displayName());
 * });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface ShopApi {

    /**
     * Returns whether the shop system is globally enabled.
     *
     * @return {@code true} if shops are enabled in config
     */
    boolean isEnabled();

    /**
     * Returns a list of all loaded category IDs.
     *
     * @return an unmodifiable list of category IDs; never {@code null}
     */
    List<String> getCategoryIds();

    /**
     * Returns the category with the given ID, if it exists and is enabled.
     *
     * @param id the category ID; must not be {@code null}
     * @return an {@link Optional} containing the category, or empty if not found
     */
    Optional<ShopCategoryEntry> getCategory(String id);

    /**
     * Returns all loaded categories.
     *
     * @return an unmodifiable list of categories; never {@code null}
     */
    List<ShopCategoryEntry> getAllCategories();

    /**
     * Returns the item from the specified category and item ID.
     *
     * @param categoryId the category ID; must not be {@code null}
     * @param itemId     the item ID; must not be {@code null}
     * @return an {@link Optional} containing the item, or empty if not found
     */
    Optional<ShopItemEntry> getItem(String categoryId, String itemId);

    /**
     * Returns all items in the specified category.
     *
     * @param categoryId the category ID; must not be {@code null}
     * @return an unmodifiable list of items; never {@code null}
     */
    List<ShopItemEntry> getItemsInCategory(String categoryId);

    /**
     * Opens the main shop menu for the given player.
     *
     * <p>Displays all enabled categories the player has permission to view.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to open the menu for; must not be {@code null}
     */
    void openMainShop(Player player);

    /**
     * Opens a specific category page for the given player.
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player     the player to open the menu for; must not be {@code null}
     * @param categoryId the category ID to open; must not be {@code null}
     * @param page       the page number to open (1-based)
     */
    void openCategory(Player player, String categoryId, int page);

    /**
     * Processes a purchase for the given player.
     *
     * <p>Checks permissions, stock, inventory space, and balance before
     * completing the transaction. Sends appropriate messages to the player.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param player the player buying the item; must not be {@code null}
     * @param itemId the item ID to purchase; must not be {@code null}
     * @param amount the amount to purchase; must be positive
     * @return a {@link CompletableFuture} resolving to {@code true} if the
     *         purchase was successful
     */
    CompletableFuture<Boolean> purchase(Player player, String itemId, int amount);

    /**
     * Processes a sale for the given player.
     *
     * <p>Checks inventory for matching items before completing the transaction.
     * Sends appropriate messages to the player.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param player the player selling the item; must not be {@code null}
     * @param itemId the item ID to sell; must not be {@code null}
     * @param amount the amount to sell; must be positive
     * @return a {@link CompletableFuture} resolving to {@code true} if the
     *         sale was successful
     */
    CompletableFuture<Boolean> sell(Player player, String itemId, int amount);

    /**
     * Reloads all shop configuration from disk.
     *
     * <p>Re-reads main.yml and all category files, clearing and rebuilding
     * the category cache. This is equivalent to {@code /shop reload}.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}