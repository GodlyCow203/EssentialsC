package net.godlycow.org.essc.language;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LanguageManager {
    private final EssentialsC plugin;
    private final MiniMessage miniMessage;
    private final Gson gson = new Gson();

    private final Map<String, Map<String, String>> cache = new HashMap<>();
    private final Map<UUID, String> playerLanguages = new HashMap<>();
    private String defaultLang;

    public LanguageManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.miniMessage = plugin.getMiniMessage();
    }

    public void load(String defaultLanguage) {
        this.defaultLang = defaultLanguage;
        cache.clear();

        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            plugin.saveResource("lang/en_US.json", false);
        }

        loadIntoCache(defaultLanguage);
        plugin.debug("Loaded default language: " + defaultLanguage);
    }

    private void loadIntoCache(String code) {
        File file = new File(plugin.getDataFolder(), "lang/" + code + ".json");
        if (!file.exists()) {
            if (code.equals(defaultLang)) {
                plugin.getLogger().severe("Default language file missing: " + code + ".json");
            }
            plugin.debug("Language file not found: " + code + ".json");
            return;
        }

        Map<String, String> messages = new HashMap<>();
        try (var reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                json.entrySet().forEach(e -> messages.put(e.getKey(), e.getValue().getAsString()));
            }
            cache.put(code, messages);
            plugin.debug("Cached language: " + code + " (" + messages.size() + " keys)");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load language " + code + ": " + e.getMessage());
            plugin.debug("Exception loading " + code + ": " + e.getMessage());
        }
    }

    public Component get(CommandSender sender, String key, Map<String, String> placeholders) {
        String locale = defaultLang;

        if (sender instanceof Player player) {
            String playerLang = playerLanguages.get(player.getUniqueId());
            if (playerLang != null) {
                locale = playerLang;
            } else {
                locale = player.locale().toString();
            }
        }

        if (!cache.containsKey(locale)) {
            loadIntoCache(locale);
        }

        Map<String, String> messages = cache.getOrDefault(locale, cache.get(defaultLang));

        if (messages == null) {
            plugin.debug("No messages loaded for locale: " + locale + " or default");
            return miniMessage.deserialize("<red>Missing lang files</red>");
        }

        String raw = messages.get(key);

        if (raw == null) {
            plugin.debug("Missing key '" + key + "' in locale '" + locale + "'");
            raw = messages.getOrDefault("error.missing_key", "<red>Missing key: <key></red>");
            raw = raw.replace("<key>", key);
        }

        if (placeholders != null) {
            for (var entry : placeholders.entrySet()) {
                raw = raw.replace("<" + entry.getKey() + ">", entry.getValue());
            }
        }

        String prefix = messages.get("prefix");
        if (prefix == null && cache.containsKey(defaultLang)) {
            prefix = cache.get(defaultLang).get("prefix");
        }
        if (prefix != null) {
            raw = raw.replace("<prefix>", prefix);
        }

        return miniMessage.deserialize(raw);
    }

    public Component get(CommandSender sender, String key) {
        return get(sender, key, null);
    }

    public void setPlayerLanguage(UUID playerUuid, String languageCode) {
        playerLanguages.put(playerUuid, languageCode);
        if (!cache.containsKey(languageCode)) {
            loadIntoCache(languageCode);
        }
    }

    public void removePlayerLanguage(UUID playerUuid) {
        playerLanguages.remove(playerUuid);
    }

    public String getPlayerLanguage(UUID playerUuid) {
        return playerLanguages.get(playerUuid);
    }

    public boolean hasPlayerLanguage(UUID playerUuid) {
        return playerLanguages.containsKey(playerUuid);
    }

    public Map<UUID, String> getPlayerLanguages() {
        return new HashMap<>(playerLanguages);
    }

    public String getDefaultLang() {
        return defaultLang;
    }

    public void reload() {
        plugin.debug("Reloading language manager...");
        load(defaultLang);
    }
}