package net.godlycow.org.essc.api.event.shop;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface ShopItem {

    // Get the unique ID string used to identify this item
    @NotNull
    String getId();

    // Get the ID of the category this item is listed in
    @Nullable
    String getCategory();

    // Get the Minecraft material type (like DIAMOND or STONE)
    @NotNull
    Material getMaterial();

    // Get the number of items included in a single purchase
    int getAmount();

    // Get the name shown in the shop menu (supports formatting)
    @Nullable
    String getDisplayName();

    // Get the descriptive text shown when hovering over the item
    @NotNull
    List<String> getLore();

    // Get the cost to buy one unit of this item
    double getBuyPrice();

    // Get the money earned by selling one unit of this item
    double getSellPrice();

    // Check if players are allowed to purchase this item
    boolean isBuyable();

    // Check if players are allowed to sell this item back to the shop
    boolean isSellable();

    // Get the available quantity (-1 means there is no limit)
    int getStock();

    // Get the largest amount a player can buy at one time
    int getMaxStack();

    // Get the inventory slot number where this item sits in the GUI
    int getSlot();

    // Get the page number where this item is displayed
    int getPage();

    // Get the permission node required to interact with this item
    @Nullable
    String getPermission();

    // Check if the item has an enchantment glow effect in the menu
    boolean isGlow();

    // Check if this item is a Mob Spawner
    boolean isSpawner();

    // Get the type of mob inside the spawner (like "ZOMBIE")
    @Nullable
    String getSpawnerType();

    // Check if this item is an Enchanted Book
    boolean isEnchantedBook();

    // Get a list of console commands that run when this item is bought
    @NotNull
    List<String> getCommands();

    // Turn this shop definition into a real Minecraft ItemStack for the player
    @NotNull
    ItemStack createItemStack();

    // Create an item specifically for checking against a player's inventory
    @NotNull
    ItemStack createComparisonItem(int amount);

    // Create a new version of this item with a modified buying price
    @NotNull
    ShopItem withBuyPrice(double price);

    // Create a new version of this item with a modified selling price
    @NotNull
    ShopItem withSellPrice(double price);

    // Create a new version of this item with a updated stock count
    @NotNull
    ShopItem withStock(int stock);

    // Create a new version of this item moved to a different category
    @NotNull
    ShopItem withCategory(@NotNull String category);
}