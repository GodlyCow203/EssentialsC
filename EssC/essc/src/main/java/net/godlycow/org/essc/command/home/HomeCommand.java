package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import net.godlycow.org.essc.modules.home.Home;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        if (name.equals("bed") && player.hasPermission("essentialsc.home.bed")) {
            Location bedLoc = player.getBedSpawnLocation();
            if (bedLoc == null) {
                player.sendMessage(lang.get(player, "home.bed.no_bed"));
                return true;
            }
            plugin.getEssScheduler().teleportAsync(player, bedLoc).thenAccept(success -> {
                if (success) {
                    plugin.getEssScheduler().runForEntity(player, () ->
                            player.sendMessage(lang.get(player, "home.bed.teleported")));
                }
            });
            return true;
        }

        teleportToHome(player, player.getUniqueId(), name, null);
        return true;
    }

    private void teleportToHome(Player player, UUID targetUuid, String name, String targetName) {
        plugin.getHomeManager().getHome(targetUuid, name).whenComplete((home, err) -> {
            plugin.getEssScheduler().runForEntity(player, () -> {
                if (home == null) {
                    player.sendMessage(lang.get(player, "home.teleport.not_found", Map.of("name", name)));
                    return;
                }

                if (targetName != null) {
                    Location loc = home.toLocation(plugin.getServer());
                    if (loc != null) {
                        plugin.getEssScheduler().teleportAsync(player, loc).thenAccept(success -> {
                            if (success) player.sendMessage(lang.get(player, "home.admin.teleported_to_other",
                                    Map.of("player", targetName, "name", name)));
                        });
                    }
                } else {
                    plugin.getHomeManager().startTeleport(player, home);
                }
            });
        });
    }

    private void showHomeList(Player player) {
        plugin.getHomeManager().getHomes(player.getUniqueId()).whenComplete((homes, err) -> {
            plugin.getEssScheduler().runForEntity(player, () -> {
                boolean hasBed = player.hasPermission("essentialsc.home.bed")
                        && player.getBedSpawnLocation() != null;

                if ((homes == null || homes.isEmpty()) && !hasBed) {
                    player.sendMessage(lang.get(player, "home.list.empty"));
                    return;
                }

                int max = plugin.getHomeManager().getMaxHomes(player);
                int used = (homes == null ? 0 : homes.size()) + (hasBed && plugin.getConfigManager().isBedHomeCountsInLimit() ? 1 : 0);
                String maxStr = max == Integer.MAX_VALUE ? "∞" : String.valueOf(max);

                player.sendMessage(lang.get(player, "home.list.header",
                        Map.of("used", String.valueOf(used), "limit", maxStr)));

                if (hasBed) {
                    Location bedLoc = player.getBedSpawnLocation();
                    player.sendMessage(lang.get(player, "home.list.entry",
                            Map.of("name", "bed",
                                    "world", bedLoc.getWorld() != null ? bedLoc.getWorld().getName() : "?",
                                    "x", String.valueOf((int) bedLoc.getX()),
                                    "y", String.valueOf((int) bedLoc.getY()),
                                    "z", String.valueOf((int) bedLoc.getZ()))));
                }

                if (homes != null) {
                    for (Home home : homes) {
                        player.sendMessage(lang.get(player, "home.list.entry",
                                Map.of("name", home.getName(),
                                        "world", home.getWorld(),
                                        "x", String.valueOf((int) home.getX()),
                                        "y", String.valueOf((int) home.getY()),
                                        "z", String.valueOf((int) home.getZ()))));
                    }
                }
            });
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        Player player = (Player) sender;

        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            if ("bed".startsWith(partial) && player.hasPermission("essentialsc.home.bed")
                    && player.getBedSpawnLocation() != null) {
                completions.add("bed");
            }

            Set<String> homeNames = plugin.getHomeManager().getCachedHomeNames(player.getUniqueId());
            for (String name : homeNames) {
                if (name.startsWith(partial)) {
                    completions.add(name);
                }
            }

            if (player.hasPermission("essentialsc.home.admin")) {
                for (Player online : plugin.getServer().getOnlinePlayers()) {
                    if (online.getName().toLowerCase().startsWith(partial)) {
                        completions.add(online.getName());
                    }
                }
            }

            return completions;
        }
        else if (args.length == 2 && player.hasPermission("essentialsc.home.admin")) {
            String targetName = args[0];
            String partial = args[1].toLowerCase();
            Player target = plugin.getServer().getPlayer(targetName);

            if (target != null) {
                Set<String> homeNames = plugin.getHomeManager().getCachedHomeNames(target.getUniqueId());
                List<String> completions = new ArrayList<>();
                for (String name : homeNames) {
                    if (name.startsWith(partial)) {
                        completions.add(name);
                    }
                }
                return completions;
            }
        }

        return Collections.emptyList();
    }
}