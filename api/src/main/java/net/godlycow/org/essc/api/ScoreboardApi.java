package net.godlycow.org.essc.api;

import org.bukkit.entity.Player;

/**
 * API interface for interacting with EssentialsC's scoreboard system.
 *
 * <p>Provides control over per-player scoreboard visibility and global
 * system state. Scoreboards are configured in config.yml and update
 * asynchronously with placeholder support.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getScoreboardApi()}.</p>
 *
 * <pre>{@code
 * ScoreboardApi sb = APIProvider.getAPI().getScoreboardApi();
 *
 * if (sb.isEnabled(player)) {
 *     sb.toggle(player);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface ScoreboardApi {

    /**
     * Toggles the scoreboard visibility for the given player.
     *
     * <p>If the scoreboard is currently shown, it will be hidden and the
     * player is added to the disabled list. If hidden, it will be shown.
     * Persists across sessions if persistence is enabled in config.</p>
     *
     * <p>Must be called on the main thread.</p>
     *
     * @param player the player to toggle; must not be {@code null}
     */
    void toggle(Player player);

    /**
     * Returns whether the scoreboard is currently enabled for the given player.
     *
     * <p>Returns {@code false} if the player has toggled it off, even if
     * the global system is enabled.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has the scoreboard visible
     */
    boolean isEnabled(Player player);

    /**
     * Returns whether the scoreboard system is globally enabled in config.
     *
     * @return {@code true} if scoreboards are enabled server-wide
     */
    boolean isGloballyEnabled();

    /**
     * Reloads the scoreboard configuration from disk.
     *
     * <p>Re-reads config.yml, stops all current scoreboards, and restarts
     * them with the new configuration. This is equivalent to
     * {@code /scoreboard reload}.</p>
     *
     * <p>Must be called on the main thread.</p>
     */
    void reload();
}