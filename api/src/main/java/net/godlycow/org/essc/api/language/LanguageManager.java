package net.godlycow.org.essc.api.language;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The language system. Get it from {@link net.godlycow.org.essc.api.EssentialsCAPI#getLanguageManager()}.
 * Messages are sent in each players own language. Add your own keys with
 * {@link #registerMessages(Plugin, String...)} and prefix them with your
 * addon name, like "myverycoolplugin.somemessage".
 */
public interface LanguageManager {
    /**
     * Gets a message in the sender's language.
     *
     * @param sender the sender, or null for the default language
     * @param key the message key
     * @param placeholders placeholder values, may be null
     * @return the message, never null
     */
    Component get(CommandSender sender, String key, Map<String, String> placeholders);

    /**
     * Gets a message in the sender's language.
     *
     * @param sender the sender, or null for the default language
     * @param key the message key
     * @return the message, never null
     */
    Component get(CommandSender sender, String key);

    /**
     * Gets a message as a raw MiniMessage string.
     *
     * @param sender the sender, or null for the default language
     * @param key the message key
     * @param placeholders placeholder values, may be null
     * @return the raw message, never null
     */
    String getRaw(CommandSender sender, String key, Map<String, String> placeholders);

    /**
     * Sends a message to the sender.
     *
     * @param sender the sender
     * @param key the message key
     * @param placeholders placeholder values, may be null
     */
    void send(CommandSender sender, String key, Map<String, String> placeholders);

    /**
     * Sends a message to the sender.
     *
     * @param sender the sender
     * @param key the message key
     */
    void send(CommandSender sender, String key);

    /**
     * Sends a message to all players with the permission and to the console.
     *
     * @param permission the permission
     * @param key the message key
     * @param placeholders placeholder values, may be null
     */
    void broadcast(String permission, String key, Map<String, String> placeholders);

    /**
     * Checks if a key exists.
     *
     * @param key the message key
     * @return true if found
     */
    boolean hasKey(String key);

    /**
     * Registers your addon's messages. Copies lang/&lt;code&gt;.json from your jar
     * to your plugin folder and adds any new keys. Call it in onEnable.
     *
     * @param plugin your plugin
     * @param languageCodes the language files in your jar, for example "en_US"
     */
    void registerMessages(Plugin plugin, String... languageCodes);

    /**
     * Gets the language a sender sees messages in.
     *
     * @param sender the sender, or null for the default language
     * @return the language code, for example "en_US"
     */
    String getLanguage(CommandSender sender);

    /**
     * Gets the language a player picked with /language.
     *
     * @param playerId the player UUID
     * @return the language code, or null if not set
     */
    String getPlayerLanguage(UUID playerId);

    /**
     * Gets the default language from the config.
     *
     * @return the language code, never null
     */
    String getDefaultLanguage();

    /**
     * Gets all loaded languages.
     *
     * @return language codes, never null
     */
    Set<String> getAvailableLanguages();
}
