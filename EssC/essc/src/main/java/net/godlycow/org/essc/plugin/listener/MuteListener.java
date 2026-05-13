package net.godlycow.org.essc.plugin.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.punishment.PunishmentManager;
import net.godlycow.org.essc.server.FeatureFlags;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;
import java.util.Map;

public class MuteListener implements Listener {

    private final EssentialsC plugin;
    private final PunishmentManager punishmentManager;

    public MuteListener(EssentialsC plugin) {
        this.plugin = plugin;
        this.punishmentManager = plugin.getPunishmentManager();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPaperChat(AsyncChatEvent event) {
        if (!FeatureFlags.supportsPaperChatEvent()) return;
        handleMute(event.getPlayer(), event::setCancelled);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onLegacyChat(AsyncPlayerChatEvent event) {
        if (FeatureFlags.supportsPaperChatEvent()) return;
        handleMute(event.getPlayer(), event::setCancelled);
    }

    private void handleMute(Player player, java.util.function.Consumer<Boolean> cancel) {
        if (!punishmentManager.isMuted(player.getUniqueId())) return;

        var entry = punishmentManager.getMuteEntry(player.getUniqueId());
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("reason", entry.reason());
        placeholders.put("muter", entry.muter());
        placeholders.put("expires", entry.expires() > 0 ? "temporarily" : "permanently");

        player.sendMessage(plugin.getLanguageManager().get(player, "mute.chat_blocked", placeholders));
        cancel.accept(true);
        plugin.debug("Blocked chat from muted player: " + player.getName());
    }
}