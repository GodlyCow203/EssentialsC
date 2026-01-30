package net.godlycow.org.essc;

import net.godlycow.org.essc.command.*;
import net.godlycow.org.essc.command.economy.BalanceCommand;
import net.godlycow.org.essc.command.economy.BaltopCommand;
import net.godlycow.org.essc.command.economy.EcoCommand;
import net.godlycow.org.essc.command.economy.PayCommand;
import net.godlycow.org.essc.command.player.HealCommand;
import net.godlycow.org.essc.command.tpa.*;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.util.CommandRegistrationUtil;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class EssentialsC extends JavaPlugin {
    private static EssentialsC instance;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private EconomyManager economyManager;
    private VaultHook vaultHook;
    private TPAManager tpaManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final List<String> economyCommands = List.of("balance", "bal", "pay", "eco", "baltop", "balancetop");

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("lang/en_US.json", false);
        saveResource("lang/de_DE.json", false);

        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        languageManager.load(configManager.getDefaultLanguage());

        registerCommand("heal", new HealCommand(this));
        registerCommand("essc", new EsscCommand(this));

        if (configManager.isEconomyEnabled()) {
            enableEconomy();
        } else {
            unregisterEconomyCommands();
        }

        tpaManager = new TPAManager(this);
        registerTPACommands();


        debug("Plugin enabled successfully");
        getLogger().info("EssentialsC enabled");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.shutdown();
        }
        getLogger().info("EssentialsC disabled");
    }

    public void enableEconomy() {
        economyManager = new EconomyManager(this);

        vaultHook = new VaultHook(economyManager);
        if (vaultHook.hook()) {
            getLogger().info("Successfully hooked into Vault!");
        } else {
            getLogger().info("Using built-in economy system (Vault not found)");
        }

        registerCommand("balance", new BalanceCommand(this));
        registerCommand("bal", new BalanceCommand(this));
        registerCommand("pay", new PayCommand(this));
        registerCommand("eco", new EcoCommand(this));
        registerCommand("baltop", new BaltopCommand(this));
        registerCommand("balancetop", new BaltopCommand(this));

        CommandRegistrationUtil.syncCommands();
    }

    public void disableEconomy() {
        if (economyManager != null) {
            economyManager.shutdown();
            economyManager = null;
            vaultHook = null;
        }

        unregisterEconomyCommands();
    }

    private void unregisterEconomyCommands() {
        for (String cmd : economyCommands) {
            PluginCommand command = getCommand(cmd);
            if (command != null) {
                command.setExecutor(null);
                command.setTabCompleter(null);
            }
        }

        CommandRegistrationUtil.unregisterCommands(economyCommands);
        CommandRegistrationUtil.syncCommands();

        getLogger().info("Economy commands unregistered");
    }

    private void registerCommand(String name, Command command) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand == null) {
            getLogger().warning("Command '" + name + "' not found in plugin.yml");
            return;
        }
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);

        for (String alias : command.getAliases()) {
            PluginCommand aliasCmd = getCommand(alias);
            if (aliasCmd != null && aliasCmd.getPlugin() == this) {
                aliasCmd.setExecutor(command);
                aliasCmd.setTabCompleter(command);
            }
        }

        debug("Registered command: " + name);
    }

    private void registerTPACommands() {
        registerCommand("tpa", new TPACommand(this));
        registerCommand("tpahere", new TPAHereCommand(this));
        registerCommand("tpaccept", new TPAcceptCommand(this));
        registerCommand("tpdeny", new TPADenyCommand(this));
        registerCommand("tpcancel", new TPACancelCommand(this));
        registerCommand("tpaignore", new TPAIgnoreCommand(this));
        registerCommand("tpatoggle", new TPAToggleCommand(this));
        registerCommand("tpaqueue", new TPAQueueCommand(this));
    }

    public void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public static EssentialsC getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public boolean isVaultHooked() {
        return vaultHook != null && vaultHook.isHooked();
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public List<String> getEconomyCommands() {
        return new ArrayList<>(economyCommands);
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }
}