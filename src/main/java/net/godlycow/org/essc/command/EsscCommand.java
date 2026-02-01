package net.godlycow.org.essc.command;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public class EsscCommand extends Command {

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
            case "reload" -> {
                plugin.debug("Reload requested by " + sender.getName());

                boolean wasEconomyEnabled = plugin.getConfigManager().isEconomyEnabled();

                plugin.getConfigManager().reload();
                plugin.getLanguageManager().reload();

                boolean isEconomyEnabled = plugin.getConfigManager().isEconomyEnabled();

                if (wasEconomyEnabled && !isEconomyEnabled) {
                    plugin.debug("Disabling economy due to config change");
                    plugin.disableEconomy();
                    sender.sendMessage(lang.get(sender, "essc.reload.economy_disabled"));
                } else if (!wasEconomyEnabled && isEconomyEnabled) {
                    plugin.debug("Enabling economy due to config change");
                    plugin.enableEconomy();
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
                    sender.sendMessage(lang.get(sender, "essc.reload.spawn"));
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
                    plugin.debug("Back Manager reloaded");
                }

                if (plugin.getScoreboardManager() != null) {
                    plugin.getScoreboardManager().reload();
                    plugin.debug("Scoreboard System reloaded");
                }



                sender.sendMessage(lang.get(sender, "essc.reload.success"));
                plugin.debug("Reload completed");
            }

            case "version" -> {
                String version = plugin.getDescription().getVersion();
                sender.sendMessage(lang.get(sender, "essc.version", Map.of("version", version)));
                plugin.debug("Version checked by " + sender.getName());
            }

            case "debug" -> {
                boolean current = plugin.getConfigManager().isDebug();
                plugin.getConfigManager().setDebug(!current);
                String state = !current ? "enabled" : "disabled";
                sender.sendMessage(lang.get(sender, "essc.debug.toggled", Map.of("state", state)));
                plugin.getLogger().info("Debug mode " + state + " by " + sender.getName());
            }

            case "help" -> showHelp(sender);

            default -> {
                sender.sendMessage(lang.get(sender, "essc.error.unknown_arg"));
                plugin.debug("Unknown subcommand: " + args[0]);
            }
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(lang.get(sender, "essc.help.header"));
        sender.sendMessage(lang.get(sender, "essc.help.reload"));
        sender.sendMessage(lang.get(sender, "essc.help.version"));
        sender.sendMessage(lang.get(sender, "essc.help.debug"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "version", "debug", "help");
        }
        return super.tabComplete(sender, args);
    }
}