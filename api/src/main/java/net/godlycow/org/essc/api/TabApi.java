package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

/**
 * API interface for interacting with EssentialsC's tab list system.
 *
 * <p>Manages player tab list names, integrating with LuckPerms for prefixes/suffixes
 * and TAB plugin when available. Handles AFK indicators and nickname display.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getTabApi()}.</p>
 *
 * <pre>{@code
 * TabApi tab = APIProvider.getAPI().getTabApi();
 *
 * tab.updatePlayerTab(player);
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface TabApi {

    /**
     * Updates the tab list display for the given player.
     *
     * <p>Applies LuckPerms prefix/suffix, AFK status, and nickname if configured.
     * Automatically delegates to TAB plugin if installed.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to update; must not be {@code null}
     */
    void updatePlayerTab(Player player);

    /**
     * Refreshes the tab list display for all online players.
     *
     * <p>Iterates through all players and calls {@link #updatePlayerTab(Player)}
     * for each. Use sparingly to avoid performance impact.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void refreshAll();

    /**
     * Returns whether LuckPerms integration is enabled for tab formatting.
     *
     * @return {@code true} if LuckPerms is present and enabled in config
     */
    boolean isLuckPermsEnabled();

    /**
     * Returns whether the TAB plugin is being used for tab management.
     *
     * @return {@code true} if TAB plugin is installed and active
     */
    boolean isUsingTABPlugin();

    /**
     * Reloads the tab configuration from disk.
     *
     * <p>Re-reads config values and re-detects available plugins (LuckPerms, TAB).</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}