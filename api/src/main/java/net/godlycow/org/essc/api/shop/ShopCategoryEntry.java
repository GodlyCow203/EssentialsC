package net.godlycow.org.essc.api.shop;

import org.bukkit.Material;

import java.util.List;

/**
 * An immutable snapshot of a shop category.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.ShopApi}.
 * Categories are loaded from shop/main.yml and individual category files.</p>
 *
 * @see net.godlycow.org.essc.api.ShopApi#getCategory(String)
 */
public record ShopCategoryEntry(

        /**
         * The unique identifier of this category.
         */
        String id,

        /**
         * The display name of this category (may contain MiniMessage formatting).
         */
        String displayName,

        /**
         * The icon material shown in the main menu.
         */
        Material icon,

        /**
         * The texture URL for custom head icons, or {@code null} if not a head.
         */
        String textureUrl,

        /**
         * The lore lines shown on the category icon.
         */
        List<String> lore,

        /**
         * The slot position in the main menu GUI.
         */
        int slot,

        /**
         * Whether this category is enabled.
         */
        boolean enabled,

        /**
         * The permission required to view this category, or {@code null} if none.
         */
        String permission,

        /**
         * The maximum page number in this category.
         */
        int maxPage
) {
}