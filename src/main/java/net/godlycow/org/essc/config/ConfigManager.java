package net.godlycow.org.essc.config;

import net.godlycow.org.essc.EssentialsC;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.util.List;

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

    public long getTPACooldown() {
        return config.getLong("teleport.tpa.cooldown", 60);
    }

    public long getTPAWarmup() {
        return config.getLong("teleport.tpa.warmup", 3);
    }

    public long getTPATimeout() {
        return config.getLong("teleport.tpa.timeout", 60);
    }

    public int getTPAMaxPending() {
        return config.getInt("teleport.tpa.max-pending", 5);
    }

    public int getTPAMaxOutgoing() {
        return config.getInt("teleport.tpa.max-outgoing", 1);
    }

    public double getTPACost() {
        return config.getDouble("teleport.tpa.cost", 0.0);
    }

    public boolean isTPADenyMovement() {
        return config.getBoolean("teleport.tpa.deny-movement", true);
    }

    public boolean isTPAParticles() {
        return config.getBoolean("teleport.tpa.particles", true);
    }

    public boolean isTPASounds() {
        return config.getBoolean("teleport.tpa.sounds", true);
    }

    public List<String> getTPABlockedWorlds() {
        return config.getStringList("teleport.tpa.blocked-worlds");
    }

    public int getMaxHomes() {
        return config.getInt("home.max-homes", 3);
    }

    public long getHomeCooldown() {
        return config.getLong("home.cooldown", 5);
    }

    public long getHomeWarmup() {
        return config.getLong("home.warmup", 3);
    }

    public boolean isHomeCancelOnMovement() {
        return config.getBoolean("home.cancel-on-movement", true);
    }

    public boolean isHomeParticles() {
        return config.getBoolean("home.particles", true);
    }

    public boolean isHomeSounds() {
        return config.getBoolean("home.sounds", true);
    }

    public List<String> getHomeBlockedWorlds() {
        return config.getStringList("home.blocked-worlds");
    }

    public String getDefaultHomeName() {
        return config.getString("home.default-name", "home");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}