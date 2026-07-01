package net.godlycow.org.essc.expansion.mysql.command;

import net.godlycow.org.essc.expansion.mysql.MySQLDatabaseExpansion;
import net.godlycow.org.essc.expansion.mysql.sync.BalanceSyncManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class SyncCommand implements CommandExecutor, TabCompleter {

    private static final String PERM = "esscmysql.admin";

    private final MySQLDatabaseExpansion plugin;
    private final BalanceSyncManager syncManager;

    public SyncCommand(MySQLDatabaseExpansion plugin, BalanceSyncManager syncManager) {
        this.plugin      = plugin;
        this.syncManager = syncManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERM)) {
            sender.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "status" -> {
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Server ID: " + ChatColor.WHITE + plugin.getSyncConfig().getServerId());
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Last poll: " + ChatColor.WHITE
                        + timeSince(syncManager.getLastPollTime()) + " ago");
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Poll interval: " + ChatColor.WHITE
                        + plugin.getSyncConfig().getPollIntervalTicks() + " ticks");
            }
            case "forcepush" -> {
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Pushing all online player balances to MySQL...");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    syncManager.pushNow(p.getUniqueId());
                }
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.GREEN + "Done.");
            }
            case "forcepull" -> {
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Pulling balances for all online players from MySQL...");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    syncManager.onPlayerJoin(p);
                }
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.GREEN + "Done.");
            }
            case "reload" -> {
                plugin.getSyncConfig().load();
                sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.GREEN + "Config reloaded.");
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "[MySQLSync] " + ChatColor.YELLOW + "Commands:");
        sender.sendMessage(ChatColor.GRAY + "/mysqlsync status " + ChatColor.DARK_GRAY + "— " + ChatColor.WHITE + "Show sync status");
        sender.sendMessage(ChatColor.GRAY + "/mysqlsync forcepush " + ChatColor.DARK_GRAY + "— " + ChatColor.WHITE + "Push all online balances to MySQL");
        sender.sendMessage(ChatColor.GRAY + "/mysqlsync forcepull " + ChatColor.DARK_GRAY + "— " + ChatColor.WHITE + "Pull MySQL balances for all online players");
        sender.sendMessage(ChatColor.GRAY + "/mysqlsync reload " + ChatColor.DARK_GRAY + "— " + ChatColor.WHITE + "Reload config");
    }

    private String timeSince(long epochMs) {
        long diff = System.currentTimeMillis() - epochMs;
        if (diff < 1000) return diff + "ms";
        if (diff < 60_000) return (diff / 1000) + "s";
        return (diff / 60_000) + "m";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("status", "forcepush", "forcepull", "reload");
        }
        return List.of();
    }
}