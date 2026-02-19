package net.godlycow.org.essc.api.event.auction;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface Auction {

    // Get the unique identification number for this auction
    int getId();

    // Get the Unique ID of the player who is selling the item
    @NotNull
    UUID getSellerUuid();

    // Get the name of the player who listed the item
    @NotNull
    String getSellerName();

    // Get the actual item that is being sold
    @NotNull
    ItemStack getItem();

    // Get the price set for the auction
    @NotNull
    BigDecimal getPrice();

    // Get the exact time (in milliseconds) when the auction started
    long getListedTime();

    // Get how long the auction is set to last
    long getDuration();

    // Get the exact time (in milliseconds) when the auction will end
    long getExpiryTime();

    // Check if the auction's time has already run out
    boolean isExpired();

    // See how much time is left before the auction ends
    long getTimeRemaining();

    // Create a copy of this auction but with a new price
    @NotNull
    Auction withPrice(@NotNull BigDecimal price);

    // Create a copy of this auction but with a new duration
    @NotNull
    Auction withDuration(long duration);
}