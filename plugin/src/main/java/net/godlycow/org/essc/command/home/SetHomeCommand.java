package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SetHomeCommand extends Command {

    public SetHomeCommand(EssentialsC plugin) {
        super(plugin, "sethome", "essentialsc.sethome", true, 0, "command.usage.sethome");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean guiMode = plugin.getConfigManager().getHomeMode().equals("gui");

        if (args.length == 0) {
            if (guiMode) {
                plugin.getHomeGuiManager().openCreateGui(player);
            } else {
                setHome(player, plugin.getConfigManager().getDefaultHomeName());
            }
            return true;
        }

        setHome(player, args[0]);
        return true;
    }

    private void setHome(Player player, String name) {
        if (!name.matches("^[a-zA-Z0-9_-]+$")) {
            player.sendMessage(lang.get(player, "home.set.invalid_name"));
            return;
        }

        if (name.length() > 16) {
            player.sendMessage(lang.get(player, "home.set.name_too_long"));
            return;
        }

        String worldName = player.getWorld().getName();
        if (plugin.getConfigManager().getHomeBlockedWorlds().contains(worldName)) {
            player.sendMessage(lang.get(player, "home.set.blocked_world",
                    Map.of("world", worldName)));
            return;
        }

        plugin.getHomeManager().homeExists(player.getUniqueId(), name).whenComplete((alreadyExists, err1) -> {
            plugin.getHomeManager().getEffectiveHomeCount(player).whenComplete((count, err2) -> {
                int max = plugin.getHomeManager().getMaxHomes(player);

                if (!alreadyExists && count >= max) {
                    plugin.getEssScheduler().runForEntity(player, () ->
                            player.sendMessage(lang.get(player, "home.set.limit_reached",
                                    Map.of("limit", String.valueOf(max)))));
                    return;
                }

                plugin.getHomeManager().setHome(player, name, player.getLocation()).whenComplete((success, err3) -> {
                    plugin.getEssScheduler().runForEntity(player, () -> {
                        if (success) {
                            String key = alreadyExists ? "home.set.updated" : "home.set.success";
                            player.sendMessage(lang.get(player, key, Map.of("name", name)));
                        } else {
                            player.sendMessage(lang.get(player, "home.set.failed", Map.of("name", name)));
                        }
                    });
                });
            });
        });
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}