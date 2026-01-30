package net.godlycow.org.essc.config;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;

public class ConfigManager {
    private final EssentialsC plugin;
    private FileConfiguration config;

    public ConfigManager(EssentialsC plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        plugin.debug("Configuration file reloaded");
    }

    public String getDefaultLanguage() {
        return config.getString("default-language", "en_US");
    }

    public boolean isDebug() {
        return config.getBoolean("debug", false);
    }

    public void setDebug(boolean debug) {
        config.set("debug", debug);
        plugin.saveConfig();
    }

    public boolean isEconomyEnabled() {
        return config.getBoolean("economy.enabled", true);
    }

    public String getCurrencySingular() {
        return config.getString("economy.currency.singular", "Dollar");
    }

    public String getCurrencyPlural() {
        return config.getString("economy.currency.plural", "Dollars");
    }

    public BigDecimal getStartingBalance() {
        return new BigDecimal(config.getString("economy.starting-balance", "100.00"));
    }

    public BigDecimal getMinTransaction() {
        return new BigDecimal(config.getString("economy.minimum-transaction", "0.01"));
    }

    public BigDecimal getMaxBalance() {
        String val = config.getString("economy.max-balance", "-1");
        return val.equals("-1") ? null : new BigDecimal(val);
    }

    public String getEconomyFormat() {
        return config.getString("economy.format", "#,##0.00");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}