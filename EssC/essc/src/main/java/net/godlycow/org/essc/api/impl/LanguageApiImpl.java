package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.LanguageApi;
import net.godlycow.org.essc.language.LanguageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.UUID;

public class LanguageApiImpl implements LanguageApi {

    private final LanguageManager manager;

    public LanguageApiImpl(LanguageManager manager) {
        this.manager = manager;
    }

    @Override
    public Component get(CommandSender sender, String key, Map<String, String> placeholders) {
        return manager.get(sender, key, placeholders);
    }

    @Override
    public Component get(CommandSender sender, String key) {
        return manager.get(sender, key);
    }

    @Override
    public void setPlayerLanguage(UUID playerUuid, String languageCode) {
        manager.setPlayerLanguage(playerUuid, languageCode);
    }

    @Override
    public void removePlayerLanguage(UUID playerUuid) {
        manager.removePlayerLanguage(playerUuid);
    }

    @Override
    public String getPlayerLanguage(UUID playerUuid) {
        return manager.getPlayerLanguage(playerUuid);
    }

    @Override
    public boolean hasPlayerLanguage(UUID playerUuid) {
        return manager.hasPlayerLanguage(playerUuid);
    }

    @Override
    public Map<UUID, String> getPlayerLanguages() {
        return manager.getPlayerLanguages();
    }

    @Override
    public String getDefaultLanguage() {
        return manager.getDefaultLang();
    }
}