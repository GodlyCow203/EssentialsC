package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SetHomeCommand extends Command {

    public SetHomeCommand(EssentialsC plugin) {
        super(plugin, "sethome", "essentialsc.sethome", true, 0, "command.usage.sethome");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        String homeName = args.length > 0 ? args[0].toLowerCase() :
                plugin.getConfig().getString("home.default-name", "home");

        plugin.debug("SetHome command: " + player.getName() + " attempting to set '" + homeName + "'");

        if (!homeName.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(lang.get(player, "home.set.invalid_name"));
            return true;
        }

        if (homeName.length() > 16) {
            player.sendMessage(lang.get(player, "home.set.name_too_long"));
            return true;
        }

        List<String> blockedWorlds = plugin.getConfig().getStringList("home.blocked-worlds");
        World world = player.getWorld();
        if (blockedWorlds.contains(world.getName())) {
            player.sendMessage(lang.get(player, "home.set.blocked_world"));
            return true;
        }

        plugin.getHomeManager().getHomeCount(player.getUniqueId()).thenAccept(count -> {
            plugin.getHomeManager().homeExists(player.getUniqueId(), homeName).thenAccept(exists -> {
                if (exists) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Location loc = player.getLocation();
                        plugin.getHomeManager().setHome(player, homeName, loc).thenAccept(success -> {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                player.sendMessage(lang.get(player, "home.set.updated", Map.of("name", homeName)));
                                plugin.debug("Updated home '" + homeName + "' for " + player.getName());
                            });
                        });
                    });
                    return;
                }

                int maxHomes = plugin.getHomeManager().getMaxHomes(player);

                if (count >= maxHomes) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        player.sendMessage(lang.get(player, "home.set.limit_reached",
                                Map.of("limit", String.valueOf(maxHomes))));
                        plugin.debug("Denied home creation: " + player.getName() + " has " + count + "/" + maxHomes);
                    });
                    return;
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    Location loc = player.getLocation();
                    plugin.getHomeManager().setHome(player, homeName, loc).thenAccept(success -> {
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            player.sendMessage(lang.get(player, "home.set.success", Map.of("name", homeName)));
                            plugin.debug("Created home '" + homeName + "' for " + player.getName());
                        });
                    });
                });
            });
        });

        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("home", "base", "shop", "farm");
        }
        return super.tabComplete(sender, args);
    }
}