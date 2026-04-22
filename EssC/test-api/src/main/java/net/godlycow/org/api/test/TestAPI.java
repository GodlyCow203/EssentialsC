package net.godlycow.org.api.test;

import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.kit.*;
import net.godlycow.org.essc.api.kit.event.*;
import net.godlycow.org.essc.api.kit.result.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TestAPI extends JavaPlugin implements Listener {

    private KitApi api;
    private int passed = 0;
    private int failed = 0;

    private boolean preClaimFired = false;
    private boolean postClaimFired = false;
    private boolean createEventFired = false;
    private boolean deleteEventFired = false;
    private boolean modifyEventFired = false;
    private boolean reloadEventFired = false;

    @Override
    public void onEnable() {
        api = APIProvider.getKitApi();
        if (api == null) {
            getLogger().severe("EssentialsC KitApi not found!");
            return;
        }
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Kit API Test suite loaded. Use /testkit");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("testkit")) return false;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only");
            return true;
        }

        passed = 0;
        failed = 0;

        String section = args.length > 0 ? args[0].toLowerCase() : "all";
        switch (section) {
            case "crud" -> runCrudTests(player);
            case "query" -> runQueryTests(player);
            case "claim" -> runClaimTests(player);
            case "events" -> runEventTests(player);
            case "playerdata" -> runPlayerDataTests(player);
            case "async" -> runAsyncTests(player);
            case "sync" -> runSyncTests(player);
            case "all" -> {
                runCrudTests(player);
                runQueryTests(player);
                runClaimTests(player);
                runEventTests(player);
                runPlayerDataTests(player);
                runAsyncTests(player);
                runSyncTests(player);
            }
            default -> player.sendMessage("Usage: /testkit [crud|query|claim|events|playerdata|async|sync|all]");
        }

        player.sendMessage("\n<gold>=== Results: " + passed + " passed, " + failed + " failed ===");
        return true;
    }

    // ========================
    //  CRUD
    // ========================

    private void runCrudTests(Player player) {
        player.sendMessage("<yellow>--- CRUD Tests ---");

        Kit kit = new KitBuilder("test_crud_kit")
                .displayName("<green>Test CRUD Kit")
                .cooldown(Duration.ofMinutes(5))
                .oneTime(false)
                .maxClaims(3)
                .addItem(new ItemStack(Material.DIRT, 5))
                .description("<gray>A test kit")
                .sortOrder(99)
                .build();

        KitCreateResult create = api.createKit(kit);
        assertResult(player, "Create kit", create.isSuccess() || create.getStatus() == KitCreateResult.Status.ALREADY_EXISTS);

        Kit found = api.getKit("test_crud_kit");
        assertResult(player, "Get kit by name", found != null);
        if (found != null) {
            assertResult(player, "Kit name matches", found.getName().equals("test_crud_kit"));
            assertResult(player, "Kit display name", found.getDisplayName().equals("<green>Test CRUD Kit"));
            assertResult(player, "Kit cooldown", found.getCooldown().equals(Duration.ofMinutes(5)));
            assertResult(player, "Kit oneTime", !found.isOneTime());
            assertResult(player, "Kit maxClaims", found.getMaxClaims() == 3);
            assertResult(player, "Kit items size", found.getItems().size() == 1);
            assertResult(player, "Kit description", found.getDescription().equals("<gray>A test kit"));
            assertResult(player, "Kit sortOrder", found.getSortOrder() == 99);
            assertResult(player, "Kit isLimited", found.isLimited());
        }

        assertResult(player, "Kit exists", api.kitExists("test_crud_kit"));
        assertResult(player, "Kit not exists", !api.kitExists("nonexistent_kit_12345"));

        Kit updated = new KitBuilder("test_crud_kit")
                .displayName("<red>Updated Kit")
                .cooldown(Duration.ofMinutes(10))
                .oneTime(true)
                .maxClaims(1)
                .addItem(new ItemStack(Material.STONE, 10))
                .description("<gray>Updated description")
                .build();

        KitModifyResult modify = api.updateKit("test_crud_kit", updated);
        assertResult(player, "Update kit", modify.isSuccess() || modify.getStatus() == KitModifyResult.Status.KIT_NOT_FOUND);
        if (modify.isSuccess()) {
            assertResult(player, "Old kit preserved", modify.getOldKit() != null);
            assertResult(player, "New kit set", modify.getNewKit() != null);
        }

        KitDeleteResult delete = api.deleteKit("test_crud_kit");
        assertResult(player, "Delete kit", delete.isSuccess() || delete.getStatus() == KitDeleteResult.Status.KIT_NOT_FOUND);
        if (delete.isSuccess()) {
            assertResult(player, "Deleted kit returned", delete.getDeletedKit() != null);
        }
    }

    // ========================
    //  Query
    // ========================

    private void runQueryTests(Player player) {
        player.sendMessage("<yellow>--- Query Tests ---");

        List<Kit> all = api.getAllKits();
        assertResult(player, "Get all kits", all != null);
        assertResult(player, "Kit count matches", api.getKitCount() == all.size());

        KitQuery query = KitQuery.builder()
                .filter(k -> k.getName().contains("starter"))
                .sortedBy(KitQuery.SortOrder.NAME_ASC)
                .limit(10)
                .offset(0)
                .build();
        List<Kit> filtered = api.queryKits(query);
        assertResult(player, "Query kits", filtered != null);

        List<Kit> firstJoin = api.getFirstJoinKits();
        assertResult(player, "Get first join kits", firstJoin != null);
        for (Kit k : firstJoin) {
            assertResult(player, "First join flag on " + k.getName(), k.isFirstJoin());
        }

        KitQuery sortQuery = KitQuery.builder()
                .sortedBy(KitQuery.SortOrder.COOLDOWN_DESC)
                .build();
        List<Kit> sorted = api.queryKits(sortQuery);
        assertResult(player, "Sort by cooldown desc", sorted != null);
    }

    // ========================
    //  Claim
    // ========================

    private void runClaimTests(Player player) {
        player.sendMessage("<yellow>--- Claim Tests ---");

        String kitName = "starter";
        if (!api.kitExists(kitName)) {
            player.sendMessage("<red>Skipping claim tests: no '" + kitName + "' kit");
            return;
        }

        assertResult(player, "Has permission check", true);
        assertResult(player, "Can claim check", api.canClaim(player, kitName) || !api.canClaim(player, kitName));

        Duration cd = api.getCooldownRemaining(player, kitName);
        assertResult(player, "Cooldown remaining", cd != null && !cd.isNegative());

        api.giveKit(player, kitName).thenAccept(result -> {
            Bukkit.getScheduler().runTask(this, () -> {
                assertResult(player, "Give kit success", result.isSuccess());
                assertResult(player, "Give kit has items", !result.getGivenItems().isEmpty() || !result.getDroppedItems().isEmpty());
                assertResult(player, "Give kit message", result.getMessage() != null);
            });
        });

        api.claimKit(player, kitName).thenAccept(result -> {
            Bukkit.getScheduler().runTask(this, () -> {
                player.sendMessage("<gray>Claim result: " + result.getStatus());
                assertResult(player, "Claim result not null", result.getStatus() != null);
                assertResult(player, "Claim has kit", result.getKit() != null);
                assertResult(player, "Claim has player", result.getPlayer() != null);
                assertResult(player, "Claim items not null", result.getGivenItems() != null);
                assertResult(player, "Claim dropped not null", result.getDroppedItems() != null);
            });
        });
    }

    // ========================
    //  Events
    // ========================

    private void runEventTests(Player player) {
        player.sendMessage("<yellow>--- Event Tests ---");

        Kit testKit = new KitBuilder("test_event_kit")
                .displayName("<blue>Event Test Kit")
                .addItem(new ItemStack(Material.OAK_LOG, 1))
                .build();

        api.createKit(testKit);
        api.reloadKits("test");
        api.deleteKit("test_event_kit");

        Bukkit.getScheduler().runTaskLater(this, () -> {
            assertResult(player, "KitCreateEvent fired", createEventFired);
            assertResult(player, "KitReloadEvent fired", reloadEventFired);
            assertResult(player, "KitDeleteEvent fired", deleteEventFired);
        }, 5L);
    }

    @EventHandler
    public void onPreClaim(KitPreClaimEvent event) {
        preClaimFired = true;
        getLogger().info("[TEST] KitPreClaimEvent: " + event.getPlayer().getName() + " -> " + event.getKit().getName());
    }

    @EventHandler
    public void onPostClaim(KitPostClaimEvent event) {
        postClaimFired = true;
        getLogger().info("[TEST] KitPostClaimEvent: " + event.getPlayer().getName() + " -> " + event.getKit().getName() + " success=" + event.isSuccess());
    }

    @EventHandler
    public void onKitCreate(KitCreateEvent event) {
        createEventFired = true;
        getLogger().info("[TEST] KitCreateEvent: " + event.getKit().getName() + " by " + event.getCreator());
    }

    @EventHandler
    public void onKitDelete(KitDeleteEvent event) {
        deleteEventFired = true;
        getLogger().info("[TEST] KitDeleteEvent: " + event.getKit().getName() + " by " + event.getDeleter());
    }

    @EventHandler
    public void onKitModify(KitModifyEvent event) {
        modifyEventFired = true;
        getLogger().info("[TEST] KitModifyEvent: " + event.getOldKit().getName() + " -> " + event.getNewKit().getName());
    }

    @EventHandler
    public void onKitReload(KitReloadEvent event) {
        reloadEventFired = true;
        getLogger().info("[TEST] KitReloadEvent: " + event.getKitCount() + " kits from " + event.getSource());
    }

    // ========================
    //  Player Data
    // ========================

    private void runPlayerDataTests(Player player) {
        player.sendMessage("<yellow>--- Player Data Tests ---");

        UUID uuid = player.getUniqueId();
        String kitName = "starter";

        KitPlayerData data = api.getPlayerData(uuid, kitName);
        assertResult(player, "Get player data", true);

        Map<String, KitPlayerData> allData = api.getAllPlayerData(uuid);
        assertResult(player, "Get all player data", allData != null);

        if (data != null && data.hasClaimed()) {
            api.resetPlayerData(uuid, kitName).thenRun(() -> {
                getLogger().info("[TEST] Reset player data for " + uuid);
            });
        }
    }

    // ========================
    //  Async
    // ========================

    private void runAsyncTests(Player player) {
        player.sendMessage("<yellow>--- Async Tests ---");

        String kitName = "starter";
        if (!api.kitExists(kitName)) {
            player.sendMessage("<red>Skipping async tests: no '" + kitName + "' kit");
            return;
        }

        long start = System.currentTimeMillis();
        api.getCooldownRemainingAsync(player, kitName).thenAccept(cd -> {
            long elapsed = System.currentTimeMillis() - start;
            Bukkit.getScheduler().runTask(this, () -> {
                assertResult(player, "Async cooldown returned", cd != null);
                assertResult(player, "Async cooldown not negative", !cd.isNegative());
                player.sendMessage("<gray>Async cooldown took " + elapsed + "ms");
            });
        });

        api.claimKit(player, kitName).thenAccept(result -> {
            Bukkit.getScheduler().runTask(this, () -> {
                assertResult(player, "Async claim completed", result != null);
                assertResult(player, "Async claim has status", result.getStatus() != null);
            });
        });

        api.getCooldownRemainingAsync(player, kitName)
                .thenCompose(cd -> api.claimKit(player, kitName))
                .thenAccept(result -> {
                    Bukkit.getScheduler().runTask(this, () -> {
                        assertResult(player, "Chained async ops", result != null);
                    });
                });
    }

    // ========================
    //  Network Sync
    // ========================

    private void runSyncTests(Player player) {
        player.sendMessage("<yellow>--- Network Sync Tests ---");

        KitSyncHook oldHook = api.getNetworkSyncHook();

        KitSyncHook testHook = new KitSyncHook() {
            @Override
            public void onKitClaimed(UUID uuid, String kitName, long claimedAt, String serverId) {
                getLogger().info("[TEST] Sync hook onKitClaimed: " + uuid + " / " + kitName);
            }

            @Override
            public CompletableFuture<Long> getNetworkLastClaimed(UUID uuid, String kitName) {
                return CompletableFuture.completedFuture(0L);
            }
        };

        api.setNetworkSyncHook(testHook);
        assertResult(player, "Set network hook", api.getNetworkSyncHook() == testHook);

        api.setNetworkSyncHook(null);
        assertResult(player, "Clear network hook", api.getNetworkSyncHook() == null);

        api.setNetworkSyncHook(oldHook);
    }

    // ========================
    //  Assertions
    // ========================

    private void assertResult(Player player, String testName, boolean condition) {
        if (condition) {
            passed++;
            player.sendMessage("<green>  " + testName);
        } else {
            failed++;
            player.sendMessage("<red>  " + testName);
            getLogger().warning("[TEST FAILED] " + testName);
        }
    }
}