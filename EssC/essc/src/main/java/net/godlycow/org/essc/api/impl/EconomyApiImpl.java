package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.EconomyApi;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EconomyApiImpl implements EconomyApi {

    private final EconomyManager manager;
    private final VaultHook vaultHook;

    public EconomyApiImpl(EconomyManager manager, VaultHook vaultHook) {
        this.manager = manager;
        this.vaultHook = vaultHook;
    }

    @Override
    public CompletableFuture<Boolean> hasAccount(UUID uuid) {
        return manager.hasAccount(uuid);
    }

    @Override
    public CompletableFuture<Boolean> createAccount(UUID uuid, String name) {
        return manager.createAccount(uuid, name);
    }

    @Override
    public CompletableFuture<BigDecimal> getBalance(UUID uuid) {
        return manager.getBalance(uuid);
    }

    @Override
    public CompletableFuture<Boolean> has(UUID uuid, BigDecimal amount) {
        return manager.has(uuid, amount);
    }

    @Override
    public CompletableFuture<Boolean> withdraw(UUID uuid, BigDecimal amount) {
        return manager.withdraw(uuid, amount);
    }

    @Override
    public CompletableFuture<Boolean> deposit(UUID uuid, BigDecimal amount) {
        return manager.deposit(uuid, amount);
    }

    @Override
    public CompletableFuture<Boolean> setBalance(UUID uuid, BigDecimal amount) {
        return manager.setBalance(uuid, amount);
    }

    @Override
    public CompletableFuture<Map<UUID, BigDecimal>> getTopBalances(int limit) {
        return manager.getTopBalances(limit);
    }

    @Override
    public String format(BigDecimal amount) {
        return manager.format(amount);
    }

    @Override
    public String formatPlain(BigDecimal amount) {
        return manager.formatPlain(amount);
    }

    @Override
    public String currencyNameSingular() {
        return manager.currencyNameSingular();
    }

    @Override
    public String currencyNamePlural() {
        return manager.currencyNamePlural();
    }

    @Override
    public BigDecimal getMinTransaction() {
        return manager.getMinTransaction();
    }

    @Override
    public BigDecimal getMaxBalance() {
        return manager.getMaxBalance();
    }

    @Override
    public boolean hasMaxBalance() {
        return manager.hasMaxBalance();
    }

    @Override
    public BigDecimal getStartingBalance() {
        return manager.getStartingBalance();
    }

    @Override
    public boolean isVaultHooked() {
        return vaultHook != null && vaultHook.isHooked();
    }
}