package net.godlycow.org.essc.api;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * API interface for interacting with EssentialsC's chat formatting system.
 *
 * <p>The chat system processes outgoing player messages through LuckPerms group
 * formats and PlaceholderAPI placeholders when both are available and enabled.
 * This API lets you query the current state of the system and run a message
 * through the same formatting pipeline manually.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getChatApi()}.</p>
 *
 * <pre>{@code
 * ChatApi chat = APIProvider.getAPI().getChatApi();
 *
 * if (chat.isActive()) {
 *     Component formatted = chat.formatMessage(player, "Hello world");
 *     player.getServer().broadcast(formatted);
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface ChatApi {

    /**
     * Returns whether LuckPerms-based chat formatting is enabled in the plugin config.
     *
     * <p>This reflects the config value only — the LuckPerms plugin may still be
     * unavailable at runtime even if this returns {@code true}. Use {@link #isActive()}
     * to check both conditions at once.</p>
     *
     * @return {@code true} if LuckPerms chat formatting is switched on in the config
     */
    boolean isLuckPermsChatEnabled();

    /**
     * Returns whether the LuckPerms API is currently loaded and available.
     *
     * <p>This can be {@code false} even when LuckPerms is installed if the plugin
     * loaded before LuckPerms finished registering its service.</p>
     *
     * @return {@code true} if the LuckPerms API was successfully obtained at startup
     */
    boolean isLuckPermsAvailable();

    /**
     * Returns whether chat formatting is fully active — that is, both the config
     * option is enabled and the LuckPerms API is available.
     *
     * <p>Equivalent to {@code isLuckPermsChatEnabled() && isLuckPermsAvailable()}.</p>
     *
     * @return {@code true} if the chat system will process outgoing messages
     */
    boolean isActive();

    /**
     * Returns whether the given player is allowed to use legacy color codes
     * (e.g. {@code &c}, {@code &l}) in chat.
     *
     * <p>Checks for the {@code essentialsc.chat.legacycodes} permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has the legacy color code permission
     */
    boolean canUseColorCodes(Player player);

    /**
     * Returns whether the given player is allowed to use hex/RGB color codes
     * (e.g. {@code &#FF5500}) in chat.
     *
     * <p>Checks for the {@code essentialsc.chat.rbgcodes} permission.</p>
     *
     * @param player the player to check; must not be {@code null}
     * @return {@code true} if the player has the RGB color code permission
     */
    boolean canUseRgbCodes(Player player);

    /**
     * Formats a raw chat message for the given player through the full EssentialsC
     * chat pipeline and returns the result as an Adventure {@link Component}.
     *
     * <p>The pipeline applies, in order:</p>
     * <ul>
     *   <li>Hex color translation ({@code &#RRGGBB}) if the player has
     *       {@code essentialsc.chat.rbgcodes}</li>
     *   <li>Legacy color code translation ({@code &c}, etc.) if the player has
     *       {@code essentialsc.chat.legacycodes}</li>
     *   <li>LuckPerms group format lookup ({@code group-formats.<group>} in config,
     *       falling back to {@code chat-format})</li>
     *   <li>PlaceholderAPI placeholder expansion if PlaceholderAPI is present</li>
     *   <li>Prefix/suffix/name substitution from LuckPerms metadata</li>
     * </ul>
     *
     * <p>If the chat system is not active (see {@link #isActive()}), this method
     * returns the raw message deserialized as a plain {@link Component} with no
     * LuckPerms formatting applied.</p>
     *
     * @param player  the player whose format and permissions are used; must not be {@code null}
     * @param message the raw message string as typed by the player; must not be {@code null}
     * @return the fully formatted {@link Component}; never {@code null}
     */
    Component formatMessage(Player player, String message);
}