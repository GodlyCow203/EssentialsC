package net.godlycow.org.expansions.profiles.messages;

import net.godlycow.org.expansions.profiles.EssentialsCProfiles;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MessagesManager {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final EssentialsCProfiles plugin;
    private FileConfiguration cfg;

    public MessagesManager(EssentialsCProfiles plugin) {
        this.plugin = plugin;
        load();
    }
    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        cfg = YamlConfiguration.loadConfiguration(file);

        InputStream defStream = plugin.getResource("messages.yml");
        if (defStream != null) {
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defStream, StandardCharsets.UTF_8));
            cfg.setDefaults(defaults);
            cfg.options().copyDefaults(true);
            try { cfg.save(file); } catch (Exception ignored) {}
        }
    }


    public String raw(String key) {
        return cfg.getString(key, "<red>[Missing message: " + key + "]");
    }


    public Component get(String key) {
        return MM.deserialize(raw(key));
    }

    public Component get(String key, TagResolver... resolvers) {
        return MM.deserialize(raw(key), resolvers);
    }
    public Component get(String key, String placeholderKey, String value) {
        return MM.deserialize(raw(key), Placeholder.unparsed(placeholderKey, value));
    }
    public String guiTitleRaw(String key) {
        return cfg.getString(key, "<dark_gray>Profile");
    }
}
