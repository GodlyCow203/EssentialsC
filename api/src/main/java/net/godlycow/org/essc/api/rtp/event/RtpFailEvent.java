package net.godlycow.org.essc.api.rtp.event;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when an RTP request fails.
 *
 * <p>Observe-only: the request already failed, so this event cannot be cancelled.
 * Inspect {@link #getReason()} to react precisely.
 */
public class RtpFailEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final World world;
    private final FailureReason reason;
    private final String detailMessage;

    /**
     * Why an RTP request failed.
     */
    public enum FailureReason {
        /** Missing the base RTP permission. */
        NO_PERMISSION,
        /** Not allowed to RTP in the target world. */
        NO_WORLD_PERMISSION,
        /** A request is already active for the player. */
        ALREADY_IN_PROGRESS,
        /** The cooldown is still ticking. */
        COOLDOWN_ACTIVE,
        /** The target world has RTP disabled. */
        WORLD_DISABLED,
        /** The search exhausted its attempts without a safe spot. */
        NO_SAFE_LOCATION,
        /** The Bukkit teleport itself failed. */
        TELEPORT_FAILED,
        /** The warmup was abandoned mid-flight. */
        WARMUP_CANCELLED,
        /** An event listener cancelled the operation. */
        EVENT_CANCELLED
    }

    /**
     * Creates a new RTP fail event without a detail message.
     *
     * @param player the requesting player
     * @param world the world
     * @param reason why the request failed
     */
    public RtpFailEvent(Player player, World world, FailureReason reason) {
        this(player, world, reason, "");
    }

    /**
     * Creates a new RTP fail event.
     *
     * @param player the requesting player
     * @param world the world
     * @param reason why the request failed
     * @param detailMessage extra detail, or an empty string
     */
    public RtpFailEvent(Player player, World world, FailureReason reason, String detailMessage) {
        this.player = player;
        this.world = world;
        this.reason = reason;
        this.detailMessage = detailMessage;
    }

    /**
     * Returns the requesting player.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Returns why the request failed.
     *
     * @return the failure reason
     */
    public FailureReason getReason() {
        return reason;
    }

    /**
     * Returns extra failure detail, or an empty string if none.
     *
     * @return the detail message
     */
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