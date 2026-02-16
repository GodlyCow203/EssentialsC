package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HomeCommand extends Command {

    public HomeCommand(EssentialsC plugin) {
        super(plugin, "home", "essentialsc.home", true, 0, "command.usage.home");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        String targetPlayerName = null;
        String homeName = plugin.getConfig().getString("home.default-name", "home");

        if (args.length > 0) {
            String arg = args[0];
            if (arg.contains(":") && player.hasPermission("essentialsc.home.admin")) {
                String[] parts = arg.split(":", 2);
                targetPlayerName = parts[0];
                homeName = parts[1].toLowerCase();
            } else {
                homeName = arg.toLowerCase();
            }
        }

        if (targetPlayerName != null) {
            OfflinePlayer target = Bukkit.getOfflinePlayer(targetPlayerName);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage(lang.get(player, "error.player_not_found"));
                return true;
            }

            plugin.debug("Admin teleport: " + player.getName() + " -> " + target.getName() + ":" + homeName);

            String finalHomeName1 = homeName;
            plugin.getHomeManager().getHome(target.getUniqueId(), homeName).thenAccept(home -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (home == null) {
                        player.sendMessage(lang.get(player, "home.admin.not_found",
                                Map.of("player", target.getName(), "name", finalHomeName1)));
                        return;
                    }

                    Location loc = home.toLocation(plugin.getServer());
                    if (loc != null) {
                        player.teleport(loc);
                        player.sendMessage(lang.get(player, "home.admin.teleported",
                                Map.of("player", target.getName(), "name", finalHomeName1)));
                    }
                });
            });
            return true;
        }

        if (plugin.getHomeManager().hasPendingTeleport(player)) {
            player.sendMessage(lang.get(player, "home.teleport.already_pending"));
            return true;
        }

        plugin.debug("Home teleport: " + player.getName() + " -> '" + homeName + "'");

        final String finalHomeName = homeName;
        plugin.getHomeManager().getHome(player.getUniqueId(), finalHomeName).thenAccept(home -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (home == null) {
                    player.sendMessage(lang.get(player, "home.teleport.not_found", Map.of("name", finalHomeName)));
                    plugin.debug("Home not found: " + finalHomeName + " for " + player.getName());
                    return;
                }

                plugin.getHomeManager().startTeleport(player, home);
            });
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();

            plugin.getHomeManager().getHomes(player.getUniqueId()).thenAccept(homes -> {
                for (Home home : homes) {
                    suggestions.add(home.getName());
                }
            });

            if (player.hasPermission("essentialsc.home.admin")) {
                suggestions.add("player:home");
            }

            return suggestions;
        }
        return super.tabComplete(sender, args);
    }
}