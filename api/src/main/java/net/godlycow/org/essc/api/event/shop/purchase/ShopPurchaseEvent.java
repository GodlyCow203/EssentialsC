package net.godlycow.org.essc.api.event.shop.purchase;

import net.godlycow.org.essc.api.event.shop.ShopItem;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public class ShopPurchaseEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;

    private final Player player;
    private final ShopItem item;
    private final int amount;
    private final double totalPrice;
    private final boolean adminPurchase;
    private String cancelReason;

    public ShopPurchaseEvent(@NotNull Player player,
                             @NotNull ShopItem item,
                             int amount,
                             double totalPrice,
                             boolean adminPurchase) {
        super(false); // Runs on the main server thread
        this.player = player;
        this.item = item;
        this.amount = amount;
        this.totalPrice = totalPrice;
        this.adminPurchase = adminPurchase;
    }

    // Get the player who is trying to buy the item
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the specific shop item being bought
    @NotNull
    public ShopItem getItem() {
        return item;
    }

    // Get the number of items the player is buying
    public int getAmount() {
        return amount;
    }

    // Get the total cost of the transaction
    public double getTotalPrice() {
        return totalPrice;
    }

    // Check if an admin is forcing this purchase
    public boolean isAdminPurchase() {
        return adminPurchase;
    }

    // Another way to check if an admin is forcing the purchase
    public boolean isForcedByAdmin() {
        return adminPurchase;
    }

    // Get the reason why the purchase was blocked (if any)
    @Nullable
    public String getCancelReason() {
        return cancelReason;
    }

    // Set a reason to show the player if you stop the transaction
    public void setCancelReason(@Nullable String reason) {
        this.cancelReason = reason;
    }

    // Check if the purchase was cancelled
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    // Stop the purchase from going through
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