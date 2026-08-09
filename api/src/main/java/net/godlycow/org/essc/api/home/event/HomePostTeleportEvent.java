package net.godlycow.org.essc.api.home.event;

import net.godlycow.org.essc.api.home.Home;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomePostTeleportEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Home home;
    private final Location destination;

    public HomePostTeleportEvent(Player player, Home home, Location destination) {
        this.player = player;
        this.home = home;
        this.destination = destination.clone();
    }

    public Player getPlayer() {
        return player;
    }

    public Home getHome() {
        return home;
    }

    public Location getDestination() {
        return destination.clone();
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
