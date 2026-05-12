package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.backup.BackupManager;
import net.godlycow.org.essc.command.admin.DumpCommand;
import net.godlycow.org.essc.placeholderapi.PlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
public class EsscCommand extends Command {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EsscCommand(EssentialsC plugin) {
        super(plugin, "essc", "essentialsc.admin", false);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "dump" -> {
                String[] dumpArgs = args.length > 1
                        ? java.util.Arrays.copyOfRange(args, 1, args.length)
                        : new String[0];
                new DumpCommand(plugin).execute(sender, dumpArgs);
            }

            case "reload" -> {
                plugin.debug("Reload requested by " + sender.getName());

                boolean wasEconomyEnabled = plugin.getConfigManager().isEconomyEnabled();

                plugin.getConfigManager().reload();
                plugin.getLanguageManager().reload();
                if (plugin.getHelpManager() != null) plugin.getHelpManager().reload();

                boolean isEconomyEnabled = plugin.getConfigManager().isEconomyEnabled();

                if (wasEconomyEnabled && !isEconomyEnabled) {
                    plugin.debug("Disabling economy due to config change");
                    plugin.getEconomyRegistrar().disable();
                    sender.sendMessage(lang.get(sender, "essc.reload.economy_disabled"));
                } else if (!wasEconomyEnabled && isEconomyEnabled) {
                    plugin.debug("Enabling economy due to config change");
                    plugin.getEconomyRegistrar().enable();
                    sender.sendMessage(lang.get(sender, "essc.reload.economy_enabled"));
                } else if (isEconomyEnabled && plugin.getEconomyManager() != null) {
                    plugin.getEconomyManager().reload();
                    plugin.debug("Economy configuration reloaded");
                }

                if (plugin.getTPAManager() != null) {
                    plugin.getTPAManager().reload();
                    plugin.debug("TPA configuration reloaded");
                }

                if (plugin.getHomeManager() != null) {
                    plugin.getHomeManager().reload();
                    plugin.debug("Home configuration reloaded");
                }

                if (plugin.getSpawnManager() != null) {
                    plugin.getSpawnManager().reload();
                    plugin.debug("Spawn configuration reloaded");
                }

                if (plugin.getJoinLeaveListener() != null) {
                    plugin.getJoinLeaveListener().reload();
                    plugin.debug("Join / Leave messages reloaded");
                }

                if (plugin.getBackManager() != null) {
                    plugin.getBackManager().reload();
                    plugin.debug("Back Manager reloaded");
                }

                if (plugin.getKitManager() != null) {
                    plugin.getKitManager().reload();
                    plugin.debug("Kit Manager reloaded");
                }

                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().reload();
                    plugin.debug("Scoreboard System reloaded");
                }

                if (plugin.getRenameCommand() != null) {
                    plugin.getRenameCommand().loadConfig();
                    plugin.debug("Rename command reloaded");
                }

                if (plugin.getVanishManager() != null) {
                    plugin.getVanishManager().loadConfig();
                    plugin.debug("Vanish config options reloaded");
                }

                if (plugin.getNickManager() != null) {
                    plugin.getNickManager().reload();
                    plugin.debug("NickManager reloaded");
                }

                if (plugin.getShopManager() != null) {
                    plugin.getShopManager().reload();
                    plugin.debug("Shop reloaded");
                }

                if (plugin.getHatCommand() != null) {
                    plugin.getHatCommand().reload();
                    plugin.debug("Hat config reloaded");
                }

                if (plugin.getAuctionManager() != null) {
                    plugin.getAuctionManager().reload();
                    plugin.debug("Auction reloaded");
                }
                if (plugin.getAhGuiManager() != null) {
                    plugin.getAhGuiManager().reload();
                    plugin.debug("Auction GUI reloaded");
                }

                if (plugin.getWarpManager() != null) {
                    plugin.getWarpManager().reload();
                    plugin.debug("Warps reloaded");
                }

                if (plugin.getAfkManager() != null) {
                    plugin.getAfkManager().reload();
                    plugin.debug("AFK system reloaded");
                }

                if (plugin.getChatManager() != null) {
                    plugin.getChatManager().reload();
                    plugin.debug("Chat configuration reloaded");
                }

                if (plugin.getRtpManager() != null) {
                    plugin.getRtpManager().reload();
                    plugin.debug("RTP configuration reloaded");
                }

                if (plugin.getTabManager() != null) {
                    plugin.getTabManager().reload();
                    plugin.getTabManager().refreshAll();
                    plugin.debug("TAB reloaded");
                }

                if (plugin.getRulesManager() != null) {
                    plugin.getRulesManager().reload();
                    plugin.debug("Rules reloaded");
                }

                if (plugin.getScheduleManager() != null) {
                    plugin.getScheduleManager().reload();
                    plugin.debug("Schedules reloaded");
                }

                if (plugin.getMotdManager() != null) {
                    plugin.getMotdManager().reload();
                    plugin.debug("MOTD reloaded");
                }

                if (plugin.getSellManager() != null) {
                    plugin.getSellManager().reload();
                    plugin.debug("SellGUI reloaded");
                }

                if (plugin.getFlyManager() != null) {
                    plugin.getFlyManager().reload();
                    plugin.debug("FlyManager reloaded");
                }

                sender.sendMessage(lang.get(sender, "essc.reload.success", Map.of(
                        "version", plugin.getDescription().getVersion()
                )));

                Bukkit.getLogger().info("Reload completed by " + sender.getName());
            }

            case "backup" -> executeBackup(sender, args);

            case "version" -> {
                String version = plugin.getDescription().getVersion();
                sender.sendMessage(lang.get(sender, "essc.version", Map.of("version", version)));
                plugin.debug("Version checked by " + sender.getName());
            }

            case "debug" -> {
                boolean current = plugin.getConfigManager().isDebug();
                boolean newState = !current;

                plugin.getConfigManager().setDebug(newState);

                String state = newState ? "enabled" : "disabled";
                sender.sendMessage(lang.get(sender, "essc.debug.toggled", Map.of("state", state)));
                Bukkit.getLogger().info("Debug mode " + state + " by " + sender.getName());
            }

            case "placeholders" -> {
                sender.sendMessage(lang.get(sender, "essc.placeholders.header"));
                sender.sendMessage("");

                List<String> placeholders = PlaceholderHook.getAllPlaceholders();
                for (String placeholder : placeholders) {
                    sender.sendMessage("§7• §f" + placeholder);
                }

                sender.sendMessage("");
                sender.sendMessage(lang.get(sender, "essc.placeholders.footer",
                        Map.of("count", String.valueOf(placeholders.size()))));
                plugin.debug("Placeholders listed by " + sender.getName());
            }

            case "help" -> showHelp(sender);



            default -> {
                sender.sendMessage(lang.get(sender, "essc.error.unknown_arg"));
                plugin.debug("Unknown subcommand: " + args[0] + " by " + sender.getName());
            }


        }

        return true;
    }

    private void executeBackup(CommandSender sender, String[] args) {

        String sub = args.length >= 2 ? args[1].toLowerCase() : "create";

        switch (sub) {
            case "list" -> {
                var backups = plugin.getBackupManager().listBackups();

                if (backups.isEmpty()) {
                    sender.sendMessage(lang.get(sender, "essc.backup.list.empty"));
                    return;
                }

                sender.sendMessage(lang.get(sender, "essc.backup.list.header",
                        Map.of("count", String.valueOf(backups.size()))));

                for (File backup : backups) {
                    String size = BackupManager.formatSize(backup.length());
                    String date = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(backup.lastModified()),
                            ZoneId.systemDefault()
                    ).format(DISPLAY_FORMAT);

                    sender.sendMessage(lang.get(sender, "essc.backup.list.entry", Map.of(
                            "name", backup.getName(),
                            "size", size,
                            "date", date
                    )));
                }
            }

            case "delete" -> {
                if (args.length < 3) {
                    sender.sendMessage(lang.get(sender, "essc.backup.delete.usage"));
                    return;
                }

                String fileName = args[2];
                if (plugin.getBackupManager().delete(fileName)) {
                    sender.sendMessage(lang.get(sender, "essc.backup.delete.success",
                            Map.of("name", fileName)));
                    Bukkit.getLogger().info("Backup deleted: " + fileName + " by " + sender.getName());
                } else {
                    sender.sendMessage(lang.get(sender, "essc.backup.delete.not_found",
                            Map.of("name", fileName)));
                }
            }

            default -> {
                sender.sendMessage(lang.get(sender, "essc.backup.create.starting"));

                plugin.getBackupManager().createAsync(
                        fileName -> sender.sendMessage(lang.get(sender, "essc.backup.create.success",
                                Map.of("name", fileName))),
                        error -> sender.sendMessage(lang.get(sender, "essc.backup.create.failed",
                                Map.of("error", error)))
                );
            }
        }
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(lang.get(sender, "essc.help.header"));
        sender.sendMessage(lang.get(sender, "essc.help.reload"));
        sender.sendMessage(lang.get(sender, "essc.help.backup"));
        sender.sendMessage(lang.get(sender, "essc.help.version"));
        sender.sendMessage(lang.get(sender, "essc.help.debug"));
        sender.sendMessage(lang.get(sender, "essc.help.placeholders"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return filter(List.of("reload", "backup", "version", "debug", "help", "placeholders", "dump"), args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("backup")) {
            return filter(List.of("list", "delete"), args[1]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("backup") && args[1].equalsIgnoreCase("delete")) {
            return plugin.getBackupManager().listBackups().stream()
                    .map(File::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[2].toLowerCase()))
                    .toList();
        }
        return List.of();
    }

    private List<String> filter(List<String> options, String input) {
        return options.stream()
                .filter(o -> o.toLowerCase().startsWith(input.toLowerCase()))
                .toList();
    }
}