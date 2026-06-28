package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.integration.metrics.faststats.FastStatsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class CrashHandler {

    private static final Component CRASH_MESSAGE = Component.text()
            .append(Component.text("[EssentialsC] ", NamedTextColor.RED))
            .append(Component.text("Failed to load — check the console for details.", NamedTextColor.WHITE))
            .build();

    private final EssentialsC plugin;

    public CrashHandler(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void handle(Throwable ex) {
        plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        plugin.getLogger().severe("  EssentialsC encountered a fatal error during startup.");
        plugin.getLogger().severe("  " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        plugin.getLogger().severe("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        ex.printStackTrace();

        FastStatsManager.ERROR_TRACKER.trackError(ex)
                .attributes(Attributes.empty()
                        .put("phase", "startup"))
                .handled(false);

        notifyOnlinePlayers();
        registerJoinNotifier();

        plugin.getServer().getPluginManager().disablePlugin(plugin);
    }

    private void notifyOnlinePlayers() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isOp() || player.hasPermission("essentialsc.admin")) {
                player.sendMessage(CRASH_MESSAGE);
            }
        }
    }

    private void registerJoinNotifier() {
        plugin.getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR)
            public void onPlayerJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                if (player.isOp() || player.hasPermission("essentialsc.admin")) {
                    player.sendMessage(CRASH_MESSAGE);
                }
            }
        }, plugin);
    }
}