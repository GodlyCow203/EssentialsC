package net.godlycow.org.essc.command.home;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class DelHomeCommand extends Command {

    private final Map<UUID, String> pendingDeletions = new HashMap<>();
    private final Map<UUID, BukkitTask> pendingTasks = new HashMap<>();

    public DelHomeCommand(EssentialsC plugin) {
        super(plugin, "delhome", "essentialsc.delhome", true, 0, "command.usage.delhome");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;
        boolean guiMode = plugin.getConfigManager().getHomeMode().equals("gui");

        if (args.length == 0) {
            if (guiMode) {
                plugin.getHomeGuiManager().openHomeList(player);
            } else {
                player.sendMessage(lang.get(player, "home.delete.no_name_provided"));
            }
            return true;
        }

        String name = args[0].toLowerCase();

        if (guiMode) {
            plugin.getHomeManager().getHome(player.getUniqueId(), name).whenComplete((home, err) -> {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (home == null) {
                        player.sendMessage(lang.get(player, "home.delete.not_found", Map.of("name", name)));
                        return;
                    }
                    plugin.getHomeGuiManager().openConfirmDelete(player, home, player.getUniqueId());
                });
            });
        } else {
            String pending = pendingDeletions.get(player.getUniqueId());

            if (pending != null && pending.equals(name)) {
                cancelPending(player.getUniqueId());
                deleteHome(player, name);
            } else {
                setPendingDeletion(player.getUniqueId(), name);
                player.sendMessage(lang.get(player, "home.delete.confirm", Map.of("name", name)));
            }
        }

        return true;
    }

    private void setPendingDeletion(UUID uuid, String name) {
        cancelPending(uuid);

        pendingDeletions.put(uuid, name);

        BukkitTask task = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (pendingDeletions.remove(uuid) != null) {
                Player player = plugin.getServer().getPlayer(uuid);
                if (player != null) {
                }
            }
        }, 15 * 20L);

        pendingTasks.put(uuid, task);
    }

    private void cancelPending(UUID uuid) {
        BukkitTask task = pendingTasks.remove(uuid);
        if (task != null) {
            task.cancel();
        }
        pendingDeletions.remove(uuid);
    }

    private void deleteHome(Player player, String name) {
        plugin.getHomeManager().deleteHome(player.getUniqueId(), name).whenComplete((success, err) -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(lang.get(player, "home.delete.success", Map.of("name", name)));
                } else {
                    player.sendMessage(lang.get(player, "home.delete.not_found", Map.of("name", name)));
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

            Set<String> homeNames = plugin.getHomeManager().getCachedHomeNames(player.getUniqueId());
            for (String homeName : homeNames) {
                if (homeName.startsWith(partial)) {
                    completions.add(homeName);
                }
            }
            return completions;
        }

        return Collections.emptyList();
    }
}