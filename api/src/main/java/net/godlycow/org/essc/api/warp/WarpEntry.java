package net.godlycow.org.essc.api.warp;

import org.bukkit.Location;

/**
 * An immutable snapshot of a warp location.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.WarpApi}.
 * Warps are stored in SQLite with support for permissions, costs, and categories.</p>
 *
 * @see net.godlycow.org.essc.api.WarpApi#getWarp(String)
 */
public record WarpEntry(

        /**
         * The unique name of this warp.
         */
        String name,

        /**
         * The location this warp teleports to.
         */
        Location location,

        /**
         * The permission required to use this warp, or {@code null} if none.
         */
        String permission,

        /**
         * The economy cost to use this warp.
         */
        double cost,

        /**
         * Whether this warp is hidden from public lists.
         */
        boolean hidden,

        /**
         * The description of this warp.
         */
        String description,

        /**
         * The category this warp belongs to.
         */
        String category
) {

    /**
     * Returns whether this warp requires a permission to use.
     *
     * @return {@code true} if a permission is set
     */
    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    /**
     * Returns whether this warp has an economy cost.
     *
     * @return {@code true} if cost is greater than {@code 0}
     */
    public boolean hasCost() {
        return cost > 0;
    }
}