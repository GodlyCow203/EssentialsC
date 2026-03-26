package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.home.Home;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HomeCommand extends Command {

    public HomeCommand(EssentialsC plugin) {
        super(plugin, "home", "essentialsc.home", true, 0, "command.usage.home");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean guiMode = plugin.getConfigManager().getHomeMode().equals("gui");

        if (args.length == 0) {
            if (guiMode) {
                plugin.getHomeGuiManager().openHomeList(player);
            } else {
                showHomeList(player);
            }
            return true;
        }

        if (args.length > 1 && player.hasPermission("essentialsc.home.admin")) {
            String targetName = args[0];
            String homeName = args[1];

            Player target = plugin.getServer().getPlayer(targetName);
            if (target == null) {
                player.sendMessage(lang.get(player, "home.admin.player_not_found", Map.of("player", targetName)));
                return true;
            }

            teleportToHome(player, target.getUniqueId(), homeName, targetName);
            return true;
        }

        String name = args[0].toLowerCase();
        teleportToHome(player, player.getUniqueId(), name, null);
        return true;
    }

    private void teleportToHome(Player player, UUID targetUuid, String name, String targetName) {
        plugin.getHomeManager().getHome(targetUuid, name).whenComplete((home, err) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (home == null) {
                    player.sendMessage(lang.get(player, "home.teleport.not_found", Map.of("name", name)));
                    return;
                }

                if (targetName != null) {
                    Location loc = home.toLocation(plugin.getServer());
                    if (loc != null) {
                        player.teleport(loc);
                        player.sendMessage(lang.get(player, "home.admin.teleported_to_other",
                                Map.of("player", targetName, "name", name)));
                    }
                } else {
                    plugin.getHomeManager().startTeleport(player, home);
                }
            });
        });
    }

    private void showHomeList(Player player) {
        plugin.getHomeManager().getHomes(player.getUniqueId()).whenComplete((homes, err) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (homes == null || homes.isEmpty()) {
                    player.sendMessage(lang.get(player, "home.list.empty"));
                    return;
                }

                int max = plugin.getHomeManager().getMaxHomes(player);
                String maxStr = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);

                player.sendMessage(lang.get(player, "home.list.header",
                        Map.of("count", String.valueOf(homes.size()), "max", maxStr)));

                for (Home home : homes) {
                    player.sendMessage(lang.get(player, "home.list.entry",
                            Map.of("name", home.getName(),
                                    "world", home.getWorld(),
                                    "x", String.valueOf((int)home.getX()),
                                    "y", String.valueOf((int)home.getY()),
                                    "z", String.valueOf((int)home.getZ()))));
                }
            });
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}