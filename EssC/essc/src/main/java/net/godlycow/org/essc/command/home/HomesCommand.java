package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.softwares.SchedulerTask;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.home.Home;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class HomesCommand extends Command {

    public HomesCommand(EssentialsC plugin) {
        super(plugin, "homes", "essentialsc.homes", true, 0);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

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
                int max = plugin.getHomeManager().getMaxHomes(player);
                String used = String.valueOf(homes.size());
                String limit = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);

                player.sendMessage(lang.get(player, "home.list.header",
                        Map.of("used", used, "limit", limit)));

                if (homes.isEmpty()) {
                    player.sendMessage(lang.get(player, "home.list.empty"));
                    return;
                }

                StringBuilder sb = new StringBuilder();
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
}