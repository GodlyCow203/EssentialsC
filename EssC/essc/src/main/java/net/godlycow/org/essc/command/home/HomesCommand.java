package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomesCommand extends Command {

    public HomesCommand(EssentialsC plugin) {
        super(plugin, "homes", "essentialsc.homes", true, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        if (args.length > 0 && args[0].equalsIgnoreCase("notifications")) {
            handleNotifications(player);
            return true;
        }

        if (plugin.getConfigManager().isHomeGuiMode()) {
            plugin.getHomeGuiManager().openHomeList(player);
            return true;
        }

        if (args.length > 0 && player.hasPermission("essentialsc.home.admin")) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(lang.get(player, "error.player_not_found"));
                return true;
            }

            plugin.debug("Listing homes for " + target.getName() + " (requested by " + player.getName() + ")");

            plugin.getHomeManager().getHomes(target.getUniqueId()).thenAccept(homes -> {
                plugin.getEssScheduler().runForEntity(player, () -> {
                    sendHomeList(player, homes, target.getName());
                });
            });
            return true;
        }

        plugin.debug("Listing homes for " + player.getName());

        plugin.getHomeManager().getHomes(player.getUniqueId()).thenAccept(homes -> {
            plugin.getEssScheduler().runForEntity(player, () -> {
                boolean hasBed = player.hasPermission("essentialsc.home.bed")
                        && player.getBedSpawnLocation() != null;

                int max = plugin.getHomeManager().getMaxHomes(player);
                int usedCount = homes.size() + (hasBed && plugin.getConfigManager().isBedHomeCountsInLimit() ? 1 : 0);
                String used = String.valueOf(usedCount);
                String limit = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);

                player.sendMessage(lang.get(player, "home.list.header",
                        Map.of("used", used, "limit", limit)));

                if (homes.isEmpty() && !hasBed) {
                    player.sendMessage(lang.get(player, "home.list.empty"));
                    return;
                }

                StringBuilder sb = new StringBuilder();

                if (hasBed) {
                    sb.append("<click:run_command:/home bed>")
                            .append("<yellow>bed</yellow>")
                            .append("</click>");
                    if (!homes.isEmpty()) {
                        sb.append("<gray>, </gray>");
                    }
                }

                for (int i = 0; i < homes.size(); i++) {
                    Home home = homes.get(i);
                    sb.append("<click:run_command:/home ").append(home.getName()).append(">")
                            .append("<yellow>").append(home.getName()).append("</yellow>")
                            .append("</click>");

                    if (i < homes.size() - 1) {
                        sb.append("<gray>, </gray>");
                    }
                }

                player.sendMessage(lang.get(player, "home.list.entries", Map.of("homes", sb.toString())));
            });
        });

        return true;
    }

    private void handleNotifications(Player player) {
        if (!player.hasPermission("essentialsc.home.notifications")) {
            player.sendMessage(lang.get(player, "error.no_permission"));
            return;
        }

        boolean current = plugin.getHomeNotificationManager().isNotificationsEnabled(player.getUniqueId());
        boolean newValue = !current;
        plugin.getHomeNotificationManager().setNotificationsEnabled(player.getUniqueId(), newValue);
        player.sendMessage(lang.get(player, newValue
                ? "home.notifications.enabled"
                : "home.notifications.disabled"));
    }

    private void sendHomeList(Player player, List<Home> homes, String targetName) {
        player.sendMessage(lang.get(player, "home.list.header_other", Map.of("player", targetName)));

        if (homes.isEmpty()) {
            player.sendMessage(lang.get(player, "home.list.empty_other", Map.of("player", targetName)));
            return;
        }

        for (Home home : homes) {
            player.sendMessage(lang.get(player, "home.list.entry",
                    Map.of("name", home.getName(), "world", home.getWorld())));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            if (sender.hasPermission("essentialsc.home.notifications")
                    && "notifications".startsWith(args[0].toLowerCase())) {
                completions.add("notifications");
            }
            if (sender.hasPermission("essentialsc.home.admin")) {
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (online.getName().toLowerCase().startsWith(args[0].toLowerCase())) {
                        completions.add(online.getName());
                    }
                }
            }
        }
        return completions;
    }
}