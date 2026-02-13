package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
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

        String message = plugin.getConfigManager().getJoinMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        Component componentMessage = formatMessage(message, event.getPlayer().getName());

        event.setJoinMessage(null);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(componentMessage));

        plugin.debug("Custom join message sent for " + event.getPlayer().getName());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfigManager().isJoinLeaveEnabled()) {
            return;
        }

        String message = plugin.getConfigManager().getLeaveMessage();
        if (message == null || message.isEmpty()) {
            return;
        }

        Component componentMessage = formatMessage(message, event.getPlayer().getName());

        event.setQuitMessage(null);
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(componentMessage));

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
