package net.godlycow.org.essc.bstats;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.economy.EconomyManager;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class EconomyCharts {

    private static final AtomicInteger depositCount = new AtomicInteger(0);
    private static final AtomicInteger withdrawCount = new AtomicInteger(0);

    private static final AtomicLong cachedOnlineBalances = new AtomicLong(0);
    private static final AtomicInteger cachedActiveAccounts = new AtomicInteger(0);

    private EconomyCharts() {}

    public static void trackDeposit() {
        depositCount.incrementAndGet();
    }

    public static void trackWithdraw() {
        withdrawCount.incrementAndGet();
    }

    public static void register(EssentialsC plugin, Metrics metrics) {
        startCacheUpdater(plugin);

        registerOnlineBalancesChart(metrics);
        registerActiveAccountsChart(metrics);
        registerTransactionPieChart(metrics);
    }

    private static void startCacheUpdater(EssentialsC plugin) {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            EconomyManager em = plugin.getEconomyManager();
            if (em == null) {
                cachedOnlineBalances.set(0);
                cachedActiveAccounts.set(0);
                return;
            }

            AtomicLong total = new AtomicLong(0);
            AtomicInteger count = new AtomicInteger(0);
            AtomicInteger processed = new AtomicInteger(0);
            int onlineCount = Bukkit.getOnlinePlayers().size();

            if (onlineCount == 0) {
                cachedOnlineBalances.set(0);
                cachedActiveAccounts.set(0);
                return;
            }

            for (var player : Bukkit.getOnlinePlayers()) {
                em.getBalance(player.getUniqueId()).thenAccept(balance -> {
                    total.addAndGet(balance.longValue());
                    if (balance.compareTo(BigDecimal.ZERO) > 0) {
                        count.incrementAndGet();
                    }
                    processed.incrementAndGet();
                });
            }

            int attempts = 0;
            while (processed.get() < onlineCount && attempts < 50) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
                attempts++;
            }

            cachedOnlineBalances.set(total.get());
            cachedActiveAccounts.set(count.get());
        }, 20L, 6000L);
    }

    private static void registerOnlineBalancesChart(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("economy_online_balances", () -> {
            return (int) Math.min(cachedOnlineBalances.get(), Integer.MAX_VALUE);
        }));
    }

    private static void registerActiveAccountsChart(Metrics metrics) {
        metrics.addCustomChart(new SingleLineChart("economy_active_accounts", () -> {
            return cachedActiveAccounts.get();
        }));
    }

    private static void registerTransactionPieChart(Metrics metrics) {
        metrics.addCustomChart(new SimplePie("economy_transaction_type", () -> {
            int deposits = depositCount.getAndSet(0);
            int withdrawals = withdrawCount.getAndSet(0);

            if (deposits == 0 && withdrawals == 0) return "None";
            if (deposits > withdrawals) return "Deposits";
            if (withdrawals > deposits) return "Withdrawals";
            return "Equal";
        }));
    }
}