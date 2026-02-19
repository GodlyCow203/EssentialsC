package net.godlycow.org.essc.api.event.shop.open;

import net.godlycow.org.essc.api.event.shop.ShopCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class ShopOpenEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final OpenContext context;
    private final ShopCategory category; // null when opening the main menu
    private final int page;
    private String cancelReason;

    // The different ways a shop menu can be triggered
    public enum OpenContext {
        MAIN_MENU,          // Opening the starting shop list
        CATEGORY_VIEW,      // Opening a specific group of items (like "Blocks")
        FORCED_BY_ADMIN     // Triggered by an administrator command
    }

    public ShopOpenEvent(@NotNull Player player,
                         @NotNull OpenContext context,
                         @Nullable ShopCategory category,
                         int page) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.context = context;
        this.category = category;
        this.page = page;
    }

    // Get the player who is trying to view the shop
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the situation that caused the shop to open
    @NotNull
    public OpenContext getContext() {
        return context;
    }

    // Get the specific category being opened (returns null for the main menu)
    @Nullable
    public ShopCategory getCategory() {
        return category;
    }

    // Get the page number the player is looking at
    public int getPage() {
        return page;
    }

    // Get the message explaining why the shop was blocked (if any)
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to show the player if you stop them from opening the shop
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the shop opening was stopped
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the shop GUI from appearing for the player
    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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