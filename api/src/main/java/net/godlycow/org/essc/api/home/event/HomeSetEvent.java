package net.godlycow.org.essc.api.home.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomeSetEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final String homeName;
    private final Location location;
    private boolean cancelled;
    private String cancelReason;

    public HomeSetEvent(Player player, String homeName, Location location) {
        this.player = player;
        this.homeName = homeName;
        this.location = location.clone();
        this.cancelled = false;
        this.cancelReason = "";
    }

    public Player getPlayer() {
        return player;
    }

    public String getHomeName() {
        return homeName;
    }

    public Location getLocation() {
        return location.clone();
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
