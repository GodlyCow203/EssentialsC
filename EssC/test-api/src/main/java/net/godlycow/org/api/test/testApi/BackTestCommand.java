package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.event.back.Back;
import net.godlycow.org.essc.api.event.back.BackManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BackTestCommand implements CommandExecutor {

    private final ChatColor g = ChatColor.GRAY;
    private final ChatColor w = ChatColor.WHITE;
    private final ChatColor d = ChatColor.DARK_GRAY;
    private final ChatColor r = ChatColor.RED;
    private final ChatColor gr = ChatColor.GREEN;
    private final ChatColor y = ChatColor.YELLOW;
    private final ChatColor a = ChatColor.AQUA;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(g + "Console cannot run back tests");
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

        BackManager backManager = api.getBackManager();
        if (!backManager.isEnabled()) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Back system is disabled");
            return true;
        }

        TestRunner.reset();
        TestRunner runner = new TestRunner();

        switch (args[0].toLowerCase()) {
            case "info":
                testInfo(player, backManager, runner);
                break;
            case "set":
                testSetBack(player, backManager, runner);
                break;
            case "check":
                testCheckBack(player, backManager, runner);
                break;
            case "has":
                testHasBack(player, backManager, runner);
                break;
            case "remove":
                testRemoveBack(player, backManager, runner);
                break;
            case "teleport":
            case "tp":
                testTeleport(player, backManager, runner);
                break;
            case "instant":
            case "instanttp":
                testInstantTeleport(player, backManager, runner);
                break;
            case "cancel":
                testCancelTeleport(player, backManager, runner);
                break;
            case "pending":
                testPendingTeleport(player, backManager, runner);
                break;
            case "cooldown":
                testCooldown(player, backManager, runner);
                break;
            case "remaining":
                testRemainingCooldown(player, backManager, runner);
                break;
            case "limits":
                testLimits(player, backManager, runner);
                break;
            case "reload":
                testReload(player, backManager, runner);
                break;
            case "full":
                runFullTest(player, backManager, runner);
                break;
            case "all":
                runAllTests(player, backManager, runner);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(g + "=== " + w + "BackTest" + g + " ===");
        player.sendMessage(g + "/testback info " + d + "- Back system info");
        player.sendMessage(g + "/testback set " + d + "- Set current location as back");
        player.sendMessage(g + "/testback check " + d + "- Check your back location");
        player.sendMessage(g + "/testback has " + d + "- Check if you have a back location");
        player.sendMessage(g + "/testback remove " + d + "- Remove your back location");
        player.sendMessage(g + "/testback teleport " + d + "- Teleport back (with warmup)");
        player.sendMessage(g + "/testback instant " + d + "- Teleport back instantly");
        player.sendMessage(g + "/testback cancel " + d + "- Cancel pending teleport");
        player.sendMessage(g + "/testback pending " + d + "- Check if teleport is pending");
        player.sendMessage(g + "/testback cooldown " + d + "- Check if on cooldown");
        player.sendMessage(g + "/testback remaining " + d + "- Get remaining cooldown");
        player.sendMessage(g + "/testback limits " + d + "- Show warmup/cooldown limits");
        player.sendMessage(g + "/testback reload " + d + "- Reload back config");
        player.sendMessage(g + "/testback full " + d + "- Run full flow test");
        player.sendMessage(g + "/testback all " + d + "- Run all tests");
    }

    private void testInfo(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing back info...");

        boolean enabled = backManager.isEnabled();
        long warmup = backManager.getWarmupSeconds();
        long cooldown = backManager.getCooldownSeconds();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Enabled: " + (enabled ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Warmup: " + a + warmup + "s");
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Cooldown: " + a + cooldown + "s");

        runner.reportResult(player, "Back Info", true, "Warmup: " + warmup + "s, Cooldown: " + cooldown + "s");
    }

    private void testSetBack(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Setting back location...");

        Location loc = player.getLocation();
        backManager.setBackLocation(player, loc);

        String coords = String.format("%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Set to: " + a + coords + g + " in " + a + loc.getWorld().getName());

        boolean hasBack = backManager.hasBackLocation(player);
        runner.reportResult(player, "Set Back Location", hasBack, "Coords: " + coords);
    }

    private void testCheckBack(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking back location...");

        Optional<Back> backOpt = backManager.getBackLocation(player);

        if (backOpt.isPresent()) {
            Back back = backOpt.get();
            player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "World: " + a + back.getWorld());
            player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Coords: " + a +
                    String.format("%.1f, %.1f, %.1f", back.getX(), back.getY(), back.getZ()));
            player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Yaw/Pitch: " + a +
                    String.format("%.1f, %.1f", back.getYaw(), back.getPitch()));
            player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Set at: " + a + back.getTimestamp());

            boolean worldLoaded = back.toLocation() != null;
            runner.reportResult(player, "Check Back Location", worldLoaded, "World: " + back.getWorld());
        } else {
            player.sendMessage(g + "[" + r + "INFO" + g + "] " + w + "No back location set");
            runner.reportResult(player, "Check Back Location", false, "No location stored");
        }
    }

    private void testHasBack(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking if has back...");

        boolean hasBack = backManager.hasBackLocation(player);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Has back: " + (hasBack ? gr + "YES" : r + "NO"));

        runner.reportResult(player, "Has Back Check", true, "Result: " + hasBack);
    }

    private void testRemoveBack(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Removing back location...");

        boolean hadBack = backManager.hasBackLocation(player);
        backManager.removeBackLocation(player);
        boolean hasBackNow = backManager.hasBackLocation(player);

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Had back: " + (hadBack ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Has back now: " + (hasBackNow ? r + "YES" : gr + "NO"));

        runner.reportResult(player, "Remove Back Location", hadBack && !hasBackNow, "Removed: " + hadBack);
    }

    private void testTeleport(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing teleport back...");

        if (!backManager.hasBackLocation(player)) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "No back location set! Use /testback set first");
            runner.reportResult(player, "Teleport Back", false, "No back location");
            return;
        }

        if (backManager.isOnCooldown(player)) {
            long remaining = backManager.getRemainingCooldown(player);
            player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "On cooldown! Remaining: " + a + remaining + "s");
        }

        TestRunner.reset();

        Location before = player.getLocation();
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Starting teleport from: " + a +
                String.format("%.1f, %.1f, %.1f", before.getX(), before.getY(), before.getZ()));

        CompletableFuture<Boolean> future = backManager.teleport(player);

        future.thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    boolean eventFired = TestRunner.wasEventFired("BackTeleportEvent");
                    runner.reportResult(player, "Teleport Back", success, "Success: " + success);
                    runner.reportResult(player, "BackTeleportEvent", eventFired, "Event fired: " + eventFired);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testInstantTeleport(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing instant teleport...");

        if (!backManager.hasBackLocation(player)) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "No back location set! Use /testback set first");
            runner.reportResult(player, "Instant Teleport", false, "No back location");
            return;
        }

        TestRunner.reset();

        Location before = player.getLocation();
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Instant teleport from: " + a +
                String.format("%.1f, %.1f, %.1f", before.getX(), before.getY(), before.getZ()));

        CompletableFuture<Boolean> future = backManager.teleportInstantly(player);

        future.thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location after = player.getLocation();
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);
                    player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Now at: " + a +
                            String.format("%.1f, %.1f, %.1f", after.getX(), after.getY(), after.getZ()));

                    boolean moved = !before.getWorld().equals(after.getWorld()) ||
                            before.distance(after) > 0.5;

                    runner.reportResult(player, "Instant Teleport", success && moved, "Teleported: " + moved);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testCancelTeleport(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing cancel teleport...");

        boolean hadPending = backManager.hasPendingTeleport(player);
        boolean cancelled = backManager.cancelTeleport(player);
        boolean stillPending = backManager.hasPendingTeleport(player);

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Had pending: " + (hadPending ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Cancelled: " + (cancelled ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Still pending: " + (stillPending ? r + "YES" : gr + "NO"));

        runner.reportResult(player, "Cancel Teleport", true, "Had pending: " + hadPending + ", Cancelled: " + cancelled);
    }

    private void testPendingTeleport(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking pending teleport...");

        boolean pending = backManager.hasPendingTeleport(player);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Pending teleport: " + (pending ? gr + "YES" : r + "NO"));

        runner.reportResult(player, "Pending Check", true, "Pending: " + pending);
    }

    private void testCooldown(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking cooldown...");

        boolean onCooldown = backManager.isOnCooldown(player);
        long remaining = backManager.getRemainingCooldown(player);

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "On cooldown: " + (onCooldown ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Remaining: " + a + remaining + "s");

        runner.reportResult(player, "Cooldown Check", true, "On cooldown: " + onCooldown);
    }

    private void testRemainingCooldown(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Getting remaining cooldown...");

        long remaining = backManager.getRemainingCooldown(player);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Remaining cooldown: " + a + remaining + "s");

        boolean valid = remaining >= 0;
        runner.reportResult(player, "Remaining Cooldown", valid, remaining + "s");
    }

    private void testLimits(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing limits...");

        long warmup = backManager.getWarmupSeconds();
        long cooldown = backManager.getCooldownSeconds();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Warmup: " + a + warmup + "s");
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Cooldown: " + a + cooldown + "s");

        runner.reportResult(player, "Limits Check", true, "Warmup: " + warmup + "s, Cooldown: " + cooldown + "s");
    }

    private void testReload(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Reloading back config...");

        try {
            backManager.reload();
            player.sendMessage(g + "[" + gr + "SUCCESS" + g + "] " + w + "Config reloaded");
            runner.reportResult(player, "Reload Config", true, "Reloaded successfully");
        } catch (Exception e) {
            player.sendMessage(g + "[" + r + "FAILED" + g + "] " + w + e.getMessage());
            runner.reportResult(player, "Reload Config", false, "Error: " + e.getMessage());
        }
    }

    private void runFullTest(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "=== " + w + "Running Full Back Flow Test" + g + " ===");

        player.sendMessage(g + "[" + d + "STEP 1" + g + "] " + w + "Setting back location...");
        Location originalLoc = player.getLocation();
        backManager.setBackLocation(player, originalLoc);

        player.sendMessage(g + "[" + d + "STEP 2" + g + "] " + w + "Move 10 blocks away, then test will continue...");

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                ticks++;

                if (player.getLocation().distance(originalLoc) > 5) {
                    this.cancel();

                    player.sendMessage(g + "[" + d + "STEP 3" + g + "] " + w + "Checking back location...");
                    boolean hasBack = backManager.hasBackLocation(player);
                    player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Has back: " + (hasBack ? gr + "YES" : r + "NO"));

                    player.sendMessage(g + "[" + d + "STEP 4" + g + "] " + w + "Teleporting back...");
                    backManager.teleportInstantly(player).thenAccept(success -> {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                Location after = player.getLocation();
                                boolean returned = after.getWorld().equals(originalLoc.getWorld()) &&
                                        after.distance(originalLoc) < 2;

                                player.sendMessage(g + "[" + d + "RESULT" + g + "] " +
                                        (returned ? gr + "SUCCESSFULLY RETURNED" : r + "FAILED TO RETURN"));

                                runner.reportResult(player, "Full Flow Test", returned, "Returned to start: " + returned);
                                runner.printSummary(player);
                            }
                        }.runTask(Main.getInstance());
                    });
                }

                if (ticks > 200) {
                    this.cancel();
                    player.sendMessage(g + "[" + r + "TIMEOUT" + g + "] " + w + "You didn't move far enough!");
                    runner.reportResult(player, "Full Flow Test", false, "Timeout - didn't move");
                    runner.printSummary(player);
                }
            }
        }.runTaskTimer(Main.getInstance(), 20L, 1L);
    }

    private void runAllTests(Player player, BackManager backManager, TestRunner runner) {
        player.sendMessage(g + "=== " + w + "Running All Back Tests" + g + " ===");

        testInfo(player, backManager, runner);
        testSetBack(player, backManager, runner);
        testCheckBack(player, backManager, runner);
        testHasBack(player, backManager, runner);
        testLimits(player, backManager, runner);
        testCooldown(player, backManager, runner);
        testRemainingCooldown(player, backManager, runner);
        testPendingTeleport(player, backManager, runner);

        new BukkitRunnable() {
            @Override
            public void run() {
                runner.printSummary(player);
            }
        }.runTaskLater(Main.getInstance(), 40L);
    }
}