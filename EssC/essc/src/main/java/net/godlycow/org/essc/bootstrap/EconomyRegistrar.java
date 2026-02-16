package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.command.economy.*;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.util.CommandRegistrationUtil;
import org.bukkit.command.PluginCommand;

import java.util.List;

public class EconomyRegistrar {

    private static final List<String> ECONOMY_COMMANDS =
            List.of("balance", "bal", "pay", "eco", "baltop", "balancetop");

    private final EssentialsC plugin;

    public EconomyRegistrar(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        EconomyManager economyManager = new EconomyManager(plugin);
        plugin.setEconomyManager(economyManager);

        VaultHook vaultHook = new VaultHook(economyManager);
        plugin.setVaultHook(vaultHook);

        if (vaultHook.hook()) {
            plugin.getLogger().info("Successfully hooked into Vault!");
        } else {
            plugin.getLogger().info("Using built-in economy system");
        }

        register("balance", new BalanceCommand(plugin));
        register("bal", new BalanceCommand(plugin));
        register("pay", new PayCommand(plugin));
        register("eco", new EcoCommand(plugin));
        register("baltop", new BaltopCommand(plugin));
        register("balancetop", new BaltopCommand(plugin));

        CommandRegistrationUtil.syncCommands();
    }

    public void disable() {
        if (plugin.getEconomyManager() != null) {
            plugin.getEconomyManager().shutdown();
            plugin.setEconomyManager(null);
            plugin.setVaultHook(null);
        }

        unregisterCommands();
    }

    private void unregisterCommands() {
        for (String cmd : ECONOMY_COMMANDS) {
            PluginCommand command = plugin.getCommand(cmd);
            if (command != null) {
                command.setExecutor(null);
                command.setTabCompleter(null);
            }
        }

        CommandRegistrationUtil.unregisterCommands(ECONOMY_COMMANDS);
        CommandRegistrationUtil.syncCommands();
    }

    private void register(String name, net.godlycow.org.essc.command.Command command) {
        PluginCommand pluginCommand = plugin.getCommand(name);
        if (pluginCommand == null) {
            plugin.getLogger().warning("Command '" + name + "' not found in plugin.yml");
            return;
        }

        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }
}
