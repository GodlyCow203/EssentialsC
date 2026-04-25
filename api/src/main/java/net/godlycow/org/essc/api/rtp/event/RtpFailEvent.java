package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RtpFailEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final FailureReason reason;
    private final String detailMessage;

    public enum FailureReason {
        NO_PERMISSION,
        NO_WORLD_PERMISSION,
        ALREADY_IN_PROGRESS,
        COOLDOWN_ACTIVE,
        WORLD_DISABLED,
        NO_SAFE_LOCATION,
        TELEPORT_FAILED,
        WARMUP_CANCELLED,
        EVENT_CANCELLED
    }

    public RtpFailEvent(Player player, World world, FailureReason reason) {
        this(player, world, reason, "");
    }

    public RtpFailEvent(Player player, World world, FailureReason reason, String detailMessage) {
        this.player = player;
        this.world = world;
        this.reason = reason;
        this.detailMessage = detailMessage;
    }

    public Player getPlayer() {
        return player;
    }

    public World getWorld() {
        return world;
    }

    public FailureReason getReason() {
        return reason;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}