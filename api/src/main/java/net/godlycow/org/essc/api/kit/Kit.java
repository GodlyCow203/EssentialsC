package net.godlycow.org.essc.api.kit;

import org.bukkit.inventory.ItemStack;
import java.util.List;

/**
 * A kit definition. Read only, get them from {@link KitManager}.
 */
public interface Kit {
    /**
     * Gets the kit name (lowercase).
     *
     * @return the kit name
     */
    String getName();

    /**
     * Gets the display name shown in messages and GUIs.
     *
     * @return the display name
     */
    String getDisplayName();

    /**
     * Gets the permission needed to claim this kit.
     *
     * @return the permission node
     */
    String getRequiredPermission();

    /**
     * Gets the cooldown between claims in seconds, 0 means none.
     *
     * @return cooldown in seconds
     */
    long getCooldownInSeconds();

    /**
     * Checks if this kit can only be claimed once ever.
     *
     * @return true if one time use
     */
    boolean isOneTimeUse();

    /**
     * Checks if new players get this kit automatically.
     *
     * @return true if granted on first join
     */
    boolean isGrantedOnFirstJoin();

    /**
     * Gets how many times a player may claim this, -1 means unlimited.
     *
     * @return max claims allowed
     */
    int getMaximumClaimsAllowed();

    /**
     * Gets the items this kit gives.
     *
     * @return the kit items
     */
    List<ItemStack> getItemStacks();

    /**
     * Gets the description shown in GUIs.
     *
     * @return the kit description
     */
    String getKitDescription();

    /**
     * Checks if this kit syncs across the network.
     *
     * @return true if network synced
     */
    boolean isSynchronizedAcrossNetwork();

    /**
     * Gets the slot in the kits GUI.
     *
     * @return GUI slot
     */
    int getGuiSlot();

    /**
     * Gets the page in the kits GUI.
     *
     * @return GUI page
     */
    int getGuiPage();
}
