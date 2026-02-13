package net.godlycow.org.essc.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.ServicePriority;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class VaultHook {
    private final EconomyManager economyManager;
    private boolean hooked = false;

    public VaultHook(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public boolean hook() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        Bukkit.getServicesManager().register(Economy.class, new VaultEconomy(),
                net.godlycow.org.essc.EssentialsC.getInstance(), ServicePriority.High);
        hooked = true;
        return true;
    }

    public boolean isHooked() {
        return hooked;
    }

    private class VaultEconomy implements Economy {

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public String getName() {
            return "EssentialsCEconomy";
        }

        @Override
        public boolean hasBankSupport() {
            return false;
        }

        @Override
        public int fractionalDigits() {
            return 2;
        }

        @Override
        public String format(double amount) {
            return economyManager.format(BigDecimal.valueOf(amount));
        }

        @Override
        public String currencyNamePlural() {
            return economyManager.currencyNamePlural();
        }

        @Override
        public String currencyNameSingular() {
            return economyManager.currencyNameSingular();
        }

        @Override
        public boolean hasAccount(String playerName) {
            return false;
        }

        @Override
        public boolean hasAccount(OfflinePlayer player) {
            try {
                return economyManager.hasAccount(player.getUniqueId()).get();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean hasAccount(String playerName, String worldName) {
            return hasAccount(playerName);
        }

        @Override
        public boolean hasAccount(OfflinePlayer player, String worldName) {
            return hasAccount(player);
        }

        @Override
        public double getBalance(String playerName) {
            return 0;
        }

        @Override
        public double getBalance(OfflinePlayer player) {
            try {
                return economyManager.getBalance(player.getUniqueId()).get().doubleValue();
            } catch (Exception e) {
                return 0;
            }
        }

        @Override
        public double getBalance(String playerName, String world) {
            return getBalance(playerName);
        }

        @Override
        public double getBalance(OfflinePlayer player, String world) {
            return getBalance(player);
        }

        @Override
        public boolean has(String playerName, double amount) {
            return false;
        }

        @Override
        public boolean has(OfflinePlayer player, double amount) {
            try {
                return economyManager.has(player.getUniqueId(), BigDecimal.valueOf(amount)).get();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean has(String playerName, String worldName, double amount) {
            return has(playerName, amount);
        }

        @Override
        public boolean has(OfflinePlayer player, String worldName, double amount) {
            return has(player, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, double amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Deprecated");
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
            try {
                boolean success = economyManager.withdraw(player.getUniqueId(), BigDecimal.valueOf(amount)).get();
                double newBal = success ? getBalance(player) : getBalance(player) + amount;
                return new EconomyResponse(amount, newBal,
                        success ? EconomyResponse.ResponseType.SUCCESS : EconomyResponse.ResponseType.FAILURE,
                        success ? null : "Insufficient funds");
            } catch (Exception e) {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.getMessage());
            }
        }

        @Override
        public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
            return withdrawPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
            return withdrawPlayer(player, amount);
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, double amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Deprecated");
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
            try {
                economyManager.deposit(player.getUniqueId(), BigDecimal.valueOf(amount)).get();
                return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
            } catch (Exception e) {
                return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, e.getMessage());
            }
        }

        @Override
        public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
            return depositPlayer(playerName, amount);
        }

        @Override
        public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
            return depositPlayer(player, amount);
        }

        @Override
        public EconomyResponse createBank(String name, String player) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse createBank(String name, OfflinePlayer player) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse deleteBank(String name) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse bankBalance(String name) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse bankHas(String name, double amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse bankWithdraw(String name, double amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse bankDeposit(String name, double amount) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse isBankOwner(String name, String playerName) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse isBankMember(String name, String playerName) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public EconomyResponse isBankMember(String name, OfflinePlayer player) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "No bank support");
        }

        @Override
        public List<String> getBanks() {
            return new ArrayList<>();
        }

        @Override
        public boolean createPlayerAccount(String playerName) {
            return false;
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player) {
            try {
                return economyManager.createAccount(player.getUniqueId(), player.getName()).get();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        public boolean createPlayerAccount(String playerName, String worldName) {
            return createPlayerAccount(playerName);
        }

        @Override
        public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
            return createPlayerAccount(player);
        }
    }
}