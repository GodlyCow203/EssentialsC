package net.godlycow.org.essc.api.punishment;

import java.util.UUID;

/**
 * An immutable snapshot of a mute entry.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.PunishmentApi}.
 * A {@code null} entry means the player is not muted.</p>
 *
 * @see net.godlycow.org.essc.api.PunishmentApi#getMuteEntry(UUID)
 */
public record MuteEntry(

        /**
         * The UUID of the muted player.
         */
        UUID uuid,

        /**
         * The last known name of the muted player.
         */
        String name,

        /**
         * The reason for the mute.
         */
        String reason,

        /**
         * The name of the staff member or console who issued the mute.
         */
        String muter,

        /**
         * The Unix timestamp (milliseconds) at which the mute was issued.
         */
        long time,

        /**
         * The Unix timestamp (milliseconds) at which the mute expires,
         * or {@code -1} / {@code 0} for a permanent mute.
         */
        long expires
) {

    /**
     * Returns whether this mute is permanent.
     *
     * @return {@code true} if the mute has no expiry
     */
    public boolean isPermanent() {
        return expires <= 0;
    }

    /**
     * Returns whether this mute has already expired based on the current time.
     *
     * @return {@code true} if the mute has passed its expiry timestamp
     */
    public boolean isExpired() {
        return !isPermanent() && expires < System.currentTimeMillis();
    }
}