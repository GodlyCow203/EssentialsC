package net.godlycow.org.essc.api.event.afk.activity;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/* * Note: These comments were written by AI to keep the code
 * clear and easy to understand for everyone.
 */
public class AFKActivityEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Player player;
    private final ActivityType type;

    // A list of things a player can do to show they aren't AFK
    public enum ActivityType {
        MOVE, CHAT, COMMAND, INTERACT, BLOCK_BREAK, BLOCK_PLACE,
        ITEM_DROP, ITEM_PICKUP, DAMAGE_DEALT, DAMAGE_TAKEN
    }

    public AFKActivityEvent(@NotNull Player player, @NotNull ActivityType type) {
        super(false); // This event runs normally on the main thread
        this.player = player;
        this.type = type;
    }

    // Get the player who did the action
    @NotNull
    public Player getPlayer() {
        return player;
    }

    // Get the type of action they did
    @NotNull
    public ActivityType getType() {
        return type;
    }

    // Standard Bukkit code to handle the event
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    // Standard Bukkit code to handle the event
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}