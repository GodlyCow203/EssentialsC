package net.godlycow.org.essc.api.teleport;

import java.util.UUID;

/**
 * An immutable snapshot of a TPA/TPAHere request.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.TpaApi}.
 * Requests expire after a configurable timeout if not accepted or denied.</p>
 *
 * @see net.godlycow.org.essc.api.TpaApi#getIncomingRequests(org.bukkit.entity.Player)
 */
public record TPARequestEntry(

        /**
         * The UUID of the player who sent the request.
         */
        UUID requester,

        /**
         * The UUID of the target player.
         */
        UUID target,

        /**
         * The type of request: {@code TPA} or {@code TPAHERE}.
         */
        Type type,

        /**
         * The Unix timestamp (milliseconds) when the request was created.
         */
        long timestamp,

        /**
         * Whether this request has been marked as expired.
         */
        boolean expired
) {

    /**
     * The type of teleport request.
     */
    public enum Type {
        /**
         * Request to teleport to the target player.
         */
        TPA,

        /**
         * Request the target player to teleport to you.
         */
        TPAHERE
    }

    /**
     * Returns whether this request has expired based on the given timeout.
     *
     * @param timeoutMillis the timeout duration in milliseconds
     * @return {@code true} if the request has exceeded the timeout
     */
    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - timestamp > timeoutMillis;
    }
}