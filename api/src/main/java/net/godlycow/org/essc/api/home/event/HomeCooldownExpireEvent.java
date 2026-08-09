package net.godlycow.org.essc.api.home.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomeCooldownExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final long previousTeleportTime;

    public HomeCooldownExpireEvent(Player player, long previousTeleportTime) {
        this.player = player;
        this.previousTeleportTime = previousTeleportTime;
    }

    public Player getPlayer() {
        return player;
    }

    public long getPreviousTeleportTime() {
        return previousTeleportTime;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
