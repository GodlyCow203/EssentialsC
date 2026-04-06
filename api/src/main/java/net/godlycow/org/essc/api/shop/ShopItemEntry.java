package net.godlycow.org.essc.api.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

/**
 * An immutable snapshot of a shop item.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.ShopApi}.
 * Items support buying, selling, custom enchantments, spawners, and enchanted books.</p>
 *
 * @see net.godlycow.org.essc.api.ShopApi#getItem(String, String)
 */
public record ShopItemEntry(

        /**
         * The unique identifier of this item.
         */
        String id,

        /**
         * The category ID this item belongs to.
         */
        String category,

        /**
         * The material type of this item.
         */
        Material material,

        /**
         * The default amount when purchasing.
         */
        int amount,

        /**
         * The display name (may contain MiniMessage formatting), or {@code null}.
         */
        String displayName,

        /**
         * The lore lines (may contain MiniMessage formatting).
         */
        List<String> lore,

        /**
         * The buy price per unit, or {@code 0} if not buyable.
         */
        double buyPrice,

        /**
         * The sell price per unit, or {@code 0} if not sellable.
         */
        double sellPrice,

        /**
         * Whether this item can be purchased.
         */
        boolean buyable,

        /**
         * Whether this item can be sold.
         */
        boolean sellable,

        /**
         * The slot position in the category GUI.
         */
        int slot,

        /**
         * The page number in the category GUI.
         */
        int page,

        /**
         * The permission required to buy/sell this item, or {@code null} if none.
         */
        String permission,

        /**
         * The current stock, or {@code -1} for unlimited.
         */
        int stock,

        /**
         * The maximum stack size for this item.
         */
        int maxStack,

        /**
         * Whether this item is a spawner.
         */
        boolean spawner,

        /**
         * The entity type for spawners, or {@code null} if not a spawner.
         */
        String spawnerType,

        /**
         * Whether this item is an enchanted book.
         */
        boolean enchantedBook,

        /**
         * The enchantments applied to this item.
         */
        Map<Enchantment, Integer> enchantments,

        /**
         * The stored enchantments for enchanted books.
         */
        Map<Enchantment, Integer> storedEnchantments,

        /**
         * Commands to execute on purchase.
         */
        List<String> commands
) {

    /**
     * Returns whether this item is in stock.
     *
     * @return {@code false} if stock is {@code 0}
     */
    public boolean isInStock() {
        return stock != 0;
    }

    /**
     * Returns whether this item has limited stock.
     *
     * @return {@code true} if stock is not {@code -1}
     */
    public boolean hasLimitedStock() {
        return stock >= 0;
    }
}