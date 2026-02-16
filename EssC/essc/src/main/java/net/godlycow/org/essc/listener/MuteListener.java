package net.godlycow.org.essc.listener;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.punishment.PunishmentManager;
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
    public void onChat(AsyncPlayerChatEvent event) {
        if (punishmentManager.isMuted(event.getPlayer().getUniqueId())) {
            var entry = punishmentManager.getMuteEntry(event.getPlayer().getUniqueId());
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("reason", entry.reason());
            placeholders.put("muter", entry.muter());
            placeholders.put("expires", entry.expires() > 0 ? "temporarily" : "permanently");

            event.getPlayer().sendMessage(plugin.getLanguageManager().get(event.getPlayer(), "mute.chat_blocked", placeholders));
            event.setCancelled(true);
            plugin.debug("Blocked chat from muted player: " + event.getPlayer().getName());
        }
    }
}