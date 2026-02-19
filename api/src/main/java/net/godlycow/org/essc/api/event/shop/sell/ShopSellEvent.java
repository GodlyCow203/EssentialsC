package net.godlycow.org.essc.api.event.shop.sell;

import net.godlycow.org.essc.api.event.shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class ShopSellEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final ShopItem item;
    private final int amount;
    private final double totalPrice;
    private final boolean adminSell;
    private String cancelReason;

    public ShopSellEvent(@NotNull Player player, @NotNull ShopItem item,
                         int amount, double totalPrice, boolean adminSell) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.item = item;
        this.amount = amount;
        this.totalPrice = totalPrice;
        this.adminSell = adminSell;
    }

    // Get the player who is trying to sell their items
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the specific shop item definition being sold
    @NotNull
    public ShopItem getItem() {
        return item;
    }

    // Get the total number of items the player is selling
    public int getAmount() {
        return amount;
    }

    // Get the total money the player will receive from this sale
    public double getTotalPrice() {
        return totalPrice;
    }

    // Check if an administrator triggered this sale
    public boolean isAdminSell() {
        return adminSell;
    }

    // Get the reason why the sale was stopped (if any)
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to explain to the player why the sale was blocked
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the sale process was cancelled
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the sale from being completed
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