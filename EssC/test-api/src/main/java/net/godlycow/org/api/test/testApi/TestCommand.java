package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.EssentialsCAPI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class TestCommand implements CommandExecutor {

    private final ChatColor g = ChatColor.GRAY;
    private final ChatColor w = ChatColor.WHITE;
    private final ChatColor d = ChatColor.DARK_GRAY;
    private final ChatColor r = ChatColor.RED;
    private final ChatColor gr = ChatColor.GREEN;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(g + "Console cannot run tests");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        EssentialsCAPI api = EssentialsCAPI.getInstance();
        if (api == null || !api.isReady()) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "EssentialsC API not available");
            return true;
        }

        TestRunner.reset();
        TestRunner runner = new TestRunner();

        switch (args[0].toLowerCase()) {
            case "create":
                testCreate(player, api, runner);
                break;
            case "delete":
                testDelete(player, api, runner);
                break;
            case "teleport":
                testTeleport(player, api, runner);
                break;
            case "instant":
                testInstantTeleport(player, api, runner);
                break;
            case "cancel":
                testCancel(player, api, runner);
                break;
            case "limit":
                testLimit(player, api, runner);
                break;
            case "all":
                runAllTests(player, api, runner);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(g + "=== " + w + "TestApi" + g + " ===");
        player.sendMessage(g + "/testapi create " + d + "- Test home creation");
        player.sendMessage(g + "/testapi delete " + d + "- Test home deletion");
        player.sendMessage(g + "/testapi teleport " + d + "- Test teleport with warmup");
        player.sendMessage(g + "/testapi instant " + d + "- Test instant teleport");
        player.sendMessage(g + "/testapi cancel " + d + "- Test cancel teleport");
        player.sendMessage(g + "/testapi limit " + d + "- Test home limit check");
        player.sendMessage(g + "/testapi all " + d + "- Run all tests");
    }

    private void testCreate(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing home creation...");
        Location loc = player.getLocation();
        api.getHomeManager().setHome(player, "testapi", loc).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runner.reportResult(player, "Home Creation", success, "API returned " + success);
                    runner.reportResult(player, "HomeCreateEvent", TestRunner.wasEventFired("HomeCreateEvent"), "Event fired check");
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testDelete(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing home deletion...");
        api.getHomeManager().deleteHome(player.getUniqueId(), "testapi").thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runner.reportResult(player, "Home Deletion", success, "API returned " + success);
                    runner.reportResult(player, "HomeDeleteEvent", TestRunner.wasEventFired("HomeDeleteEvent"), "Event fired check");
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testTeleport(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing teleport with warmup...");
        api.getHomeManager().setHome(player, "testapi", player.getLocation()).thenCompose(v ->
                api.getHomeManager().teleport(player, "testapi")
        ).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runner.reportResult(player, "Teleport Start", success, "API returned " + success);
                    runner.reportResult(player, "HomeTeleportEvent", TestRunner.wasEventFired("HomeTeleportEvent"), "Event fired check");
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testInstantTeleport(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing instant teleport...");
        api.getHomeManager().getHome(player.getUniqueId(), "testapi").thenCompose(opt -> {
            if (opt.isPresent()) {
                return api.getHomeManager().teleportInstantly(player, opt.get());
            }
            return java.util.concurrent.CompletableFuture.completedFuture(false);
        }).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    runner.reportResult(player, "Instant Teleport", success, "API returned " + success);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testCancel(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing cancel...");
        boolean cancelled = api.getHomeManager().cancelTeleport(player);
        runner.reportResult(player, "Cancel Teleport", true, "Returned " + cancelled);
    }

    private void testLimit(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing limit check...");
        int max = api.getHomeManager().getMaxHomes(player);
        boolean eventFired = TestRunner.wasEventFired("HomeLimitCheckEvent");
        runner.reportResult(player, "HomeLimitCheckEvent", eventFired, "Max homes: " + max);
        api.getHomeManager().getRemainingHomes(player).thenAccept(remaining -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Remaining: " + d + remaining);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void runAllTests(Player player, EssentialsCAPI api, TestRunner runner) {
        player.sendMessage(g + "=== " + w + "Running All Tests" + g + " ===");
        testCreate(player, api, runner);
        testLimit(player, api, runner);
        testTeleport(player, api, runner);

        new BukkitRunnable() {
            @Override
            public void run() {
                runner.printSummary(player);
            }
        }.runTaskLater(Main.getInstance(), 60L);
    }
}