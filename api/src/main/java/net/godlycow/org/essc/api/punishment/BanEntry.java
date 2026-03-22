package net.godlycow.org.essc.api.punishment;

import java.util.UUID;

/**
 * An immutable snapshot of a ban entry.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.PunishmentApi}.
 * A {@code null} entry means the player is not banned.</p>
 *
 * @see net.godlycow.org.essc.api.PunishmentApi#getBanEntry(UUID)
 */
public record BanEntry(

        /**
         * The UUID of the banned player.
         */
        UUID uuid,

        /**
         * The last known name of the banned player.
         */
        String name,

        /**
         * The reason for the ban.
         */
        String reason,

        /**
         * The name of the staff member or console who issued the ban.
         */
        String banner,

        /**
         * The Unix timestamp (milliseconds) at which the ban was issued.
         */
        long time,

        /**
         * The Unix timestamp (milliseconds) at which the ban expires,
         * or {@code -1} / {@code 0} for a permanent ban.
         */
        long expires
) {

    /**
     * Returns whether this ban is permanent.
     *
     * @return {@code true} if the ban has no expiry
     */
    public boolean isPermanent() {
        return expires <= 0;
    }

    /**
     * Returns whether this ban has already expired based on the current time.
     *
     * @return {@code true} if the ban has passed its expiry timestamp
     */
    public boolean isExpired() {
        return !isPermanent() && expires < System.currentTimeMillis();
    }
}