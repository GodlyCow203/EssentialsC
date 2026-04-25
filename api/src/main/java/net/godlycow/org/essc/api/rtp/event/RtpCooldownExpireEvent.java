package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpCooldownExpireEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final long previousClaimTime;

    public RtpCooldownExpireEvent(Player player, long previousClaimTime) {
        this.player = player;
        this.previousClaimTime = previousClaimTime;
    }

    public Player getPlayer() {
        return player;
    }

    public long getPreviousClaimTime() {
        return previousClaimTime;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}