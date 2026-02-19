package net.godlycow.org.essc.api.event.shop.reload;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class ShopReloadEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final CommandSender reloader;
    private final int categoriesLoaded;
    private final int itemsLoaded;

    public ShopReloadEvent(@Nullable CommandSender reloader, int categoriesLoaded, int itemsLoaded) {
        super(false); // Runs on the main server thread
        this.reloader = reloader;
        this.categoriesLoaded = categoriesLoaded;
        this.itemsLoaded = itemsLoaded;
    }

    // Get the person or console that triggered the shop reload
    @Nullable
    public CommandSender getReloader() {
        return reloader;
    }

    // See how many shop categories were successfully loaded from the config
    public int getCategoriesLoaded() {
        return categoriesLoaded;
    }

    // See the total number of items loaded across all categories
    public int getItemsLoaded() {
        return itemsLoaded;
    }

    // Required Bukkit method for event handling
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Required Bukkit method for event handling
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}