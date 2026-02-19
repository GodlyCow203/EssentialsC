package net.godlycow.org.essc.api.event.shop;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface ShopCategory {

    // Get the unique ID string for this category (e.g., "blocks")
    @NotNull
    String getId();

    // Get the name of the category shown in the menu (supports formatting)
    @NotNull
    String getDisplayName();

    // Get the Minecraft material used as the icon in the main menu
    @NotNull
    Material getIcon();

    // Get the URL for a custom head texture if the icon is a player skull
    @Nullable
    String getTextureUrl();

    // Get the descriptive text shown when hovering over the category icon
    @NotNull
    List<String> getLore();

    // Get the slot number where this category appears in the main shop GUI
    int getSlot();

    // Check if players are currently allowed to see or use this category
    boolean isEnabled();

    // Get the permission node required to access this specific category
    @Nullable
    String getPermission();

    // Get a map of items assigned to a specific page number
    @NotNull
    Map<Integer, ShopItem> getPageItems(int page);

    // Get the total number of pages available in this category
    int getMaxPage();

    // Check if a specific page number actually contains items
    boolean hasPage(int page);

    // Count how many total items are listed under this category
    int getItemCount();

    // Look for a specific item within this category using its ID
    @Nullable
    ShopItem getItem(@NotNull String itemId);

    // Get a complete list of every item available in this category
    @NotNull
    List<ShopItem> getAllItems();
}