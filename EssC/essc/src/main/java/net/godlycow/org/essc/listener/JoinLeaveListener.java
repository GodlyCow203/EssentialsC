package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    private final EssentialsC plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public JoinLeaveListener(EssentialsC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfigManager().isJoinLeaveEnabled()) {
            return;
        }

        boolean hideVanished = plugin.getConfigManager().isJoinLeaveHideVanished();
        boolean isFirstJoin = !event.getPlayer().hasPlayedBefore();
        String message;

        if (isFirstJoin) {
            message = plugin.getConfigManager().getFirstJoinMessage();
        } else {
            message = plugin.getConfigManager().getJoinMessage();
        }

        if (message == null || message.isEmpty()) {
            return;
        }

        Component componentMessage = formatMessage(message, event.getPlayer().getName());

        event.setJoinMessage(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hideVanished && plugin.getVanishManager() != null && plugin.getVanishManager().isVanished(event.getPlayer()) && !player.hasPermission("essentialsc.vanish.see")) {
                continue;
            }
            player.sendMessage(componentMessage);
        }

        plugin.debug("Custom join message sent for " + event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().isJoinLeaveEnabled()) {
            return;
        }

        boolean hideVanished = plugin.getConfigManager().isJoinLeaveHideVanished();
        String message = plugin.getConfigManager().getLeaveMessage();

        if (message == null || message.isEmpty()) {
            return;
        }

        Component componentMessage = formatMessage(message, event.getPlayer().getName());

        event.setQuitMessage(null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hideVanished && plugin.getVanishManager() != null && plugin.getVanishManager().isVanished(event.getPlayer()) && !player.hasPermission("essentialsc.vanish.see")) {
                continue;
            }
            player.sendMessage(componentMessage);
        }

        plugin.debug("Custom leave message sent for " + event.getPlayer().getName());
    }

    private Component formatMessage(String message, String playerName) {
        return miniMessage.deserialize(message, Placeholder.unparsed("player", playerName));
    }

    public void reload() {
        plugin.getConfigManager().reload();
        plugin.debug("Join/Leave messages reloaded.");
    }
}