package net.godlycow.org.essc.api.kit;

import net.godlycow.org.essc.api.KitApi;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a kit definition loaded from {@code kits.yml}.
 *
 * <p>Instances are obtained via {@link KitApi} — you never construct them directly.
 * Kit names are always stored in lowercase.</p>
 *
 * @see KitApi
 */
public class Kit {

    private final String name;
    private final String displayName;
    private final String permission;
    private final long cooldown;
    private final boolean oneTime;
    private final boolean firstJoin;
    private final int maxClaims;
    private final List<ItemStack> items;
    private final String description;

    public Kit(String name, String displayName, String permission, long cooldown,
               boolean oneTime, boolean firstJoin, int maxClaims,
               List<ItemStack> items, String description) {
        this.name = name;
        this.displayName = displayName;
        this.permission = permission;
        this.cooldown = cooldown;
        this.oneTime = oneTime;
        this.firstJoin = firstJoin;
        this.maxClaims = maxClaims;
        this.items = items;
        this.description = description;
    }

    /**
     * Returns the internal name of this kit in lowercase (e.g. {@code "starter"}).
     *
     * @return the kit name; never {@code null}
     */
    public String getName() { return name; }

    /**
     * Returns the display name of this kit as configured in {@code kits.yml}.
     * May contain MiniMessage formatting.
     *
     * @return the display name; never {@code null}
     */
    public String getDisplayName() { return displayName; }

    /**
     * Returns the permission node required to claim this kit
     * (e.g. {@code "essentialsc.kit.starter"}).
     *
     * @return the permission node; never {@code null}
     */
    public String getPermission() { return permission; }

    /**
     * Returns the cooldown between claims of this kit, in seconds.
     *
     * @return the cooldown in seconds; {@code 0} means no cooldown
     */
    public long getCooldown() { return cooldown; }

    /**
     * Returns whether this kit may only be claimed once per player lifetime.
     *
     * @return {@code true} if the kit is one-time only
     */
    public boolean isOneTime() { return oneTime; }

    /**
     * Returns whether this kit is automatically given to players on their first join.
     *
     * @return {@code true} if the kit is a first-join kit
     */
    public boolean isFirstJoin() { return firstJoin; }

    /**
     * Returns the maximum number of times a player may claim this kit.
     *
     * @return the max claim count; {@code 0} means unlimited
     */
    public int getMaxClaims() { return maxClaims; }

    /**
     * Returns the list of items included in this kit.
     *
     * <p>Items are returned as-is from the loaded config — clone them before
     * modifying.</p>
     *
     * @return the kit's item list; never {@code null}, may be empty
     */
    public List<ItemStack> getItems() { return items; }

    /**
     * Returns the description of this kit as configured in {@code kits.yml}.
     * May contain MiniMessage formatting.
     *
     * @return the description string; never {@code null}, may be empty
     */
    public String getDescription() { return description; }
}