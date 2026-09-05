package net.godlycow.org.essc.expansion.mysql.sync;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.expansion.mysql.MySQLExpansion;
import net.godlycow.org.essc.expansion.mysql.config.ExpansionConfig;
import net.godlycow.org.essc.expansion.mysql.storage.ConnectionPool;
import net.godlycow.org.essc.expansion.mysql.storage.SchemaManager;
import net.godlycow.org.essc.plugin.economy.EconomyManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


public class EconomySyncService {

    private final MySQLExpansion plugin;
    private final EssentialsC essentialsC;
    private final ConnectionPool pool;

    private final SchemaManager schema;
    private final ExpansionConfig config;

    private final String serverId;

    private final Map<UUID,BigDecimal> lastPushed = new ConcurrentHashMap<>();
    private ScheduledTask pushTask;

    private final Map<UUID, BigDecimal> lastNetwork = new ConcurrentHashMap<>();
    private boolean enabled = false;
    private ScheduledTask mirrorTask;

    public EconomySyncService( MySQLExpansion plugin, EssentialsC essentialsC,  ConnectionPool pool, SchemaManager schema, ExpansionConfig config, String serverId) {

        this.plugin = plugin;
        this.essentialsC = essentialsC;
        this.pool = pool;
        this.config = config;
        this.serverId = serverId;
        this.schema = schema;


    }

    public void init() {
        if (!config.isEconomyEnabled()) {
            enabled = false;

            return;
        }
        if (essentialsC.getEconomyManager() == null) {
            enabled = false;
            plugin.getLogger().warning("Economy module is disabled in EssentialsC, economy sync will not run !");
            return;
        }
        enabled = true;
        plugin.getLogger().info("Economy network sync initialised (server-id: " + serverId + ")");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void start() {
        if (!enabled) {

            return;
        }
        int pushInterval = Math.max(1, config.getPushIntervalSeconds());

        pushTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> pushOnline(),
                20L * pushInterval, 20L * pushInterval, TimeUnit.MILLISECONDS);

        plugin.debug("Scheduled economy push (on change) every " + pushInterval + "s");

        if (config.isSyncLocal()) {

            int mirrorInterval = Math.max(1, config.getMirrorIntervalSeconds());

            mirrorTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, task -> mirror(),
                    20L * mirrorInterval, 20L * mirrorInterval, TimeUnit.MILLISECONDS);
            plugin.debug("Scheduled local  network mirror every " + mirrorInterval + "s");
        }
    }

    public void stop() {

        if (pushTask != null) {
            pushTask.cancel();
            pushTask = null;
        }
        if (mirrorTask != null) {
            mirrorTask.cancel();
            mirrorTask = null;
        }
    }

    public void onJoin( Player player) {

        if (!enabled || !config.isPullOnJoin()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        fetchNetworkBalance(uuid).thenAccept(balance -> {
            if (balance != null) {
                lastNetwork.put(uuid, balance);
                applyBalance(player, uuid, balance); //apply balance for a joinin player
            } else { // or else keep local value
                lastPushed.put(uuid, essentialsC.getEconomyManager().getCachedBalance(uuid));
                plugin.debug("no network balance for " + player.getName() + "; keeping local value");
            }
        }).exceptionally(error -> {
            plugin.getLogger().warning("Failed to pull network balance for " +  player.getName() + ": " + error.getMessage());
            return null;
        });
    }

    public void onQuit(Player player) {
        if (!enabled || !config.isPushOnQuit()) {
            return;
        }

        push(player.getUniqueId(), player.getName(),
                essentialsC.getEconomyManager().getCachedBalance(player.getUniqueId()));
        lastPushed.remove(player.getUniqueId());
    }

    private void applyBalance(Player player, UUID uuuid,  BigDecimal balance) {

        EconomyManager manager = essentialsC.getEconomyManager();
        if (manager == null) {
            return;
        }
        lastNetwork.put(uuuid, balance);

        manager.createAccount(uuuid, player.getName()).thenCompose(ignored ->
                manager.setBalance(uuuid, balance)).thenAccept(success -> {
            if (success) {
                lastPushed.put(uuuid, balance);
                plugin.debug("Applied network balance " + balance + " to " + player.getName());
            } else {
                plugin.getLogger().warning("Could not apply network balance to " +  player.getName()
                        + " (server balance cap????), Network value: " + balance);
            }
        });
    }

    public CompletableFuture<BigDecimal> pullSingle(Player player) {
        UUID uuid = player.getUniqueId();

        return  fetchNetworkBalance(uuid).thenCompose( balance -> {

            if (balance == null) {
                return CompletableFuture.completedFuture((BigDecimal) null);
            }
            CompletableFuture<BigDecimal> result = new CompletableFuture<>();

            applyBalance(player, uuid, balance);
            lastPushed.put(uuid, balance);
            result.complete(balance);


            return result;
        });
    }

    private CompletableFuture<BigDecimal> fetchNetworkBalance(UUID  uuid) {
        String sql = "SELECT balance FROM " + schema.balancesTable() + " WHERE uuid = ?";

        return pool.query(sql, rs -> {
            if (rs.next()) {
                return rs.getBigDecimal("balance");
            }
            return null;
        }, uuid.toString()).exceptionally(error -> null);
    }

    public CompletableFuture <Void> push(UUID uuid, String name, BigDecimal balance) {

        if (!enabled) {
            return CompletableFuture.completedFuture(null);
        }

        String sql = "INSERT INTO " + schema.balancesTable()
                + " (uuid, username, balance, server_id, last_updated) VALUES (?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE balance = VALUES(balance), username = VALUES(username), "
                + "server_id = VALUES(server_id), last_updated = VALUES(last_updated)";

        return pool.update(sql, uuid.toString(), name, balance, serverId,  System.currentTimeMillis())
                .thenRun(() -> {
                    lastPushed.put(uuid, balance);
                    lastNetwork.put(uuid, balance);
                })
                .exceptionally(error -> {
                    plugin.getLogger().warning("Failed to push balance for " + name + ": " + error.getMessage());
                    return null;
                });
    }

    private void pushOnline() {

        if (!enabled) {
            return;
        }

        EconomyManager manager = essentialsC.getEconomyManager();
        if (manager == null) {


            return;
        }
        for (Player player :  Bukkit.getOnlinePlayers()) {

            UUID uuid = player.getUniqueId();

            BigDecimal current = manager.getCachedBalance(uuid);
            BigDecimal previous = lastPushed.get(uuid);
            if (previous == null || previous.compareTo(current) != 0) {
                push(uuid, player.getName(), current);
            }
        }
    }


    private void mirror() {
        if (!enabled || !config.isSyncLocal()) {
            return;
        }
        EconomyManager manager = essentialsC.getEconomyManager();
        if (manager == null) {
            return;
        }
        fetchAllNetworkBalances().thenAccept(rows -> {
            for (NetworkRow row : rows) {
                UUID uuid = row.uuid();
                if (Bukkit.getPlayer(uuid) != null) {
                    //owned locally, pushOnline handles it,jst  keep the network snapshot fresh
                    lastNetwork.put(uuid, row.balance());

                    continue;
                }
                BigDecimal previousNetwork = lastNetwork.get(uuid);
                BigDecimal local = manager.getCachedBalance(uuid);
                if (local.compareTo(row.balance()) != 0) {

                    if (previousNetwork != null && previousNetwork.compareTo(row.balance()) == 0) {
                        push(uuid, row.name(), local);
                    } else {
                        manager.createAccount(uuid, row.name())
                                .thenCompose(ignored -> manager.setBalance(uuid, row.balance()))
                                .exceptionally(error -> {
                                    plugin.getLogger().warning("Failed to mirror network balance for "
                                            + row.name() + ": " + error.getMessage());

                                    return null;
                                });
                    }
                }

                lastNetwork.put(uuid, row.balance());
            }
        }) .exceptionally(error -> {
            plugin.getLogger().warning("Failed to mirror network balances: " + error.getMessage());
            return null;
        });
    }

    private CompletableFuture<List<NetworkRow>> fetchAllNetworkBalances() {
        String sql = "SELECT uuid, username, balance FROM " + schema.balancesTable();

        return pool.query(sql, rs -> {
            List<NetworkRow> rows = new ArrayList<>();

            while (rs.next()) {
                try {
                    rows.add(new NetworkRow(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getBigDecimal("balance")));
                } catch (IllegalArgumentException ignored) {
                    //  skip malformed uuid rows
                }
            }
            return rows;
        }).exceptionally(error -> new ArrayList<>());
    }

    private record NetworkRow(UUID uuid, String name, BigDecimal balance) {
    }

    public  CompletableFuture<Integer> forcePushAll() {
        if (!enabled) {
            return CompletableFuture.completedFuture(0);
        }


        EconomyManager manager = essentialsC.getEconomyManager();
        if (manager == null) {
            return CompletableFuture.completedFuture(0);
        }
        int[] count = {0};

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {

            UUID uuid = player.getUniqueId();
            BigDecimal current = manager.getCachedBalance(uuid);
            futures.add(push(uuid, player.getName(), current).thenRun(() -> count[0]++));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> count[0]);
    }

    public CompletableFuture<Integer> forcePullAll() {

        if (!enabled) {
            return CompletableFuture.completedFuture(0);
        }
        int[] count = {0};
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            CompletableFuture<Void> future = fetchNetworkBalance(uuid).thenAccept(balance -> {
                if (balance != null) {
                    applyBalance(player, uuid, balance);
                    count[0]++;
                }
            });

            futures.add(future);
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> count[0]);
    }

    public CompletableFuture<List<BalanceEntry>>  getNetworkTop( int limit) {
        String sql = "SELECT uuid, username, balance FROM " + schema.balancesTable()
                + " ORDER BY balance DESC LIMIT ?";
        return pool.query(sql, rs -> {

            List<BalanceEntry> entries = new ArrayList<>();
            while (rs.next()) {
                try {
                    entries.add(new BalanceEntry(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("username"),
                            rs.getBigDecimal("balance")));

                } catch (IllegalArgumentException ignored) {
                }
            }
            return entries;

        }, limit).thenApply(entries -> {
            EconomyManager manager = essentialsC.getEconomyManager();
            if (manager != null) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BigDecimal live = manager.getCachedBalance(player.getUniqueId());

                    boolean replaced = false;
                    for (int i = 0; i < entries.size(); i++) {
                        if (entries.get(i).uuid().equals(player.getUniqueId())) {
                            entries.set(i, new BalanceEntry(player.getUniqueId(), player.getName(), live));

                            replaced = true;



                            break;
                        }
                    }

                    if (!replaced) {
                        entries.add(new BalanceEntry(player.getUniqueId(), player.getName(), live));
                    }
                }
            }
            entries.sort(Comparator.comparing(BalanceEntry::balance).reversed());
            return entries;

        }).exceptionally(error -> {
            plugin.getLogger().warning("Failed to create network baltop: " + error.getMessage());
            return new ArrayList<>();
        });
    }

    public void flush() {
        if (!enabled) {
            return;
        }

        EconomyManager manager = essentialsC.getEconomyManager();
        if (manager == null) {
            return;
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            try {

                push(uuid, player.getName(), manager.getCachedBalance(uuid)).join();
            } catch (Exception ignored) {
            }
        }
    }

    public record BalanceEntry(UUID uuid, String  name, BigDecimal balance)
    {
    }
}
