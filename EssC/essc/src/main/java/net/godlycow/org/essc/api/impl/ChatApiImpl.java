package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.ChatApi;
import net.godlycow.org.essc.chat.ChatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class ChatApiImpl implements ChatApi {

    private final ChatManager manager;

    public ChatApiImpl(ChatManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean isLuckPermsChatEnabled() {
        return manager.isLuckPermsChatEnabled();
    }

    @Override
    public boolean isLuckPermsAvailable() {
        return manager.isLuckPermsAvailable();
    }

    @Override
    public boolean isActive() {
        return manager.isLuckPermsChatEnabled() && manager.isLuckPermsAvailable();
    }

    @Override
    public boolean canUseColorCodes(Player player) {
        return player.hasPermission("essentialsc.chat.legacycodes");
    }

    @Override
    public boolean canUseRgbCodes(Player player) {
        return player.hasPermission("essentialsc.chat.rgbcodes");
    }

    @Override
    public Component formatMessage(Player player, String message) {
        return manager.formatMessage(player, message);
    }
}