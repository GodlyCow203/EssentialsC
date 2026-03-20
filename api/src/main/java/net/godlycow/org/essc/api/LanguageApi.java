package net.godlycow.org.essc.api;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's language system.
 *
 * <p>EssentialsC resolves messages per-sender using the following priority:</p>
 * <ol>
 *   <li>The player's manually set language override (via {@code /language set}).</li>
 *   <li>The player's client locale as reported by Bukkit.</li>
 *   <li>The configured server default language.</li>
 * </ol>
 *
 * <p>Messages are returned as Adventure {@link Component}s with MiniMessage formatting
 * already applied. Placeholders in the raw message string are substituted before
 * deserialization.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getLanguageApi()}.</p>
 *
 * <pre>{@code
 * LanguageApi lang = APIProvider.getAPI().getLanguageApi();
 *
 * // resolve a message for a player with placeholders
 * Component msg = lang.get(player, "home.teleport.success",
 *         Map.of("name", "base"));
 * player.sendMessage(msg);
 *
 * // resolve a message with no placeholders
 * Component header = lang.get(player, "kit.list.header");
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface LanguageApi {

    /**
     * Resolves the message for the given key in the sender's active language,
     * substituting the provided placeholders.
     *
     * <p>Placeholders are applied by replacing {@code <key>} tokens in the raw message
     * string. For example, a placeholder entry of {@code "name" -> "base"} replaces
     * all occurrences of {@code <name>} in the raw string.</p>
     *
     * <p>If the key is not found in the sender's language, the default language is
     * tried as a fallback. If still not found, an error component is returned.</p>
     *
     * @param sender       the sender whose language preference is used for resolution;
     *                     must not be {@code null}
     * @param key          the message key to look up (e.g. {@code "home.teleport.success"});
     *                     must not be {@code null}
     * @param placeholders a map of placeholder names to replacement values, or
     *                     {@code null} if there are no placeholders
     * @return the resolved {@link Component}; never {@code null}
     */
    Component get(CommandSender sender, String key, Map<String, String> placeholders);

    /**
     * Resolves the message for the given key in the sender's active language
     * with no placeholder substitution.
     *
     * <p>Equivalent to {@code get(sender, key, null)}.</p>
     *
     * @param sender the sender whose language preference is used for resolution;
     *               must not be {@code null}
     * @param key    the message key to look up; must not be {@code null}
     * @return the resolved {@link Component}; never {@code null}
     */
    Component get(CommandSender sender, String key);


    /**
     * Sets a manual language override for the given player.
     *
     * <p>Once set, this language takes precedence over the player's client locale.
     * If the language file for {@code languageCode} has not yet been loaded it is
     * loaded into the cache immediately.</p>
     *
     * @param playerUuid   the UUID of the player; must not be {@code null}
     * @param languageCode the language code to set (e.g. {@code "de_DE"});
     *                     must not be {@code null}
     */
    void setPlayerLanguage(UUID playerUuid, String languageCode);

    /**
     * Removes the manual language override for the given player, returning them
     * to automatic locale detection.
     *
     * <p>Has no effect if the player has no override set.</p>
     *
     * @param playerUuid the UUID of the player; must not be {@code null}
     */
    void removePlayerLanguage(UUID playerUuid);

    /**
     * Returns the manually set language code for the given player, or {@code null}
     * if no override has been set.
     *
     * <p>A {@code null} return means the player's message resolution falls back to
     * their client locale and then the server default.</p>
     *
     * @param playerUuid the UUID of the player; must not be {@code null}
     * @return the language code, or {@code null} if no override is set
     */
    String getPlayerLanguage(UUID playerUuid);

    /**
     * Returns whether the given player has a manual language override set.
     *
     * <p>Equivalent to {@code getPlayerLanguage(playerUuid) != null}.</p>
     *
     * @param playerUuid the UUID of the player; must not be {@code null}
     * @return {@code true} if the player has a language override
     */
    boolean hasPlayerLanguage(UUID playerUuid);

    /**
     * Returns a snapshot of all currently active manual player language overrides.
     *
     * <p>The returned map is a copy — modifications have no effect on the internal
     * state.</p>
     *
     * @return a map of player UUID to language code; never {@code null}, may be empty
     */
    Map<UUID, String> getPlayerLanguages();


    /**
     * Returns the server's configured default language code.
     *
     * <p>This is used as the final fallback when neither a player override nor a
     * matching client locale file is available.</p>
     *
     * @return the default language code (e.g. {@code "en_US"}); never {@code null}
     */
    String getDefaultLanguage();
}