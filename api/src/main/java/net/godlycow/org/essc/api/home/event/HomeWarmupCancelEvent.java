package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomeWarmupCancelEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private final CancelReason reason;

    public enum CancelReason {
        PLAYER_OFFLINE,
        PLAYER_MOVED,
        EVENT_CANCELLED
    }

    public HomeWarmupCancelEvent(Player player, Home home, CancelReason reason) {
        this.player = player;
        this.home = home;
        this.reason = reason;
    }

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
    }

    public CancelReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
