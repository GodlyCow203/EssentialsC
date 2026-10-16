package net.godlycow.org.essc.api.impl.language;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.language.LanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LanguageManagerImpl implements LanguageManager {
    private final EssentialsC plugin;

    public LanguageManagerImpl(EssentialsC plugin) {
        this.plugin = plugin;
    }

    private net.godlycow.org.essc.language.LanguageManager internal() {
        return plugin.getLanguageManager();
    }

    @Override
    public Component get(CommandSender sender, String key, Map<String, String> placeholders) {
        return internal().get(sender, key, placeholders);
    }

    @Override
    public Component get(CommandSender sender, String key) {
        return internal().get(sender, key);
    }

    @Override
    public String getRaw(CommandSender sender, String key, Map<String, String> placeholders) {
        return internal().getRaw(sender, key, placeholders);
    }

    @Override
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(internal().get(sender, key, placeholders));
    }

    @Override
    public void send(CommandSender sender, String key) {
        sender.sendMessage(internal().get(sender, key));
    }

    @Override
    public void broadcast(String permission, String key, Map<String, String> placeholders) {
        internal().broadcast(permission, key, placeholders);
    }

    @Override
    public boolean hasKey(String key) {
        return internal().hasKey(key);
    }

    @Override
    public void registerMessages(Plugin plugin, String... languageCodes) {
        if (plugin == null) {
            throw new IllegalArgumentException("plugin must not be null");
        }
        internal().registerAddon(plugin, languageCodes);
    }

    @Override
    public String getLanguage(CommandSender sender) {
        return internal().getEffectiveLanguage(sender);
    }

    @Override
    public String getPlayerLanguage(UUID playerId) {
        return internal().getPlayerLanguage(playerId);
    }

    @Override
    public String getDefaultLanguage() {
        return internal().getDefaultLang();
    }

    @Override
    public Set<String> getAvailableLanguages() {
        return internal().getLoadedLanguages();
    }
}
