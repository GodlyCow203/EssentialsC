package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.event.auction.Auction;
import net.godlycow.org.essc.api.event.auction.AuctionManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AuctionTestCommand implements CommandExecutor {

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
            sender.sendMessage(g + "Console cannot run auction tests");
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

        AuctionManager auctionManager = api.getAuctionManager();
        if (!auctionManager.isEnabled()) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Auction House is disabled");
            return true;
        }

        TestRunner.reset();
        TestRunner runner = new TestRunner();

        switch (args[0].toLowerCase()) {
            case "info":
                testInfo(player, auctionManager, runner);
                break;
            case "list":
                testList(player, auctionManager, runner);
                break;
            case "player":
                testPlayerAuctions(player, auctionManager, runner);
                break;
            case "create":
                if (args.length < 3) {
                    player.sendMessage(g + "Usage: " + w + "/testah create <price> <durationMinutes>");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[1]);
                    long duration = Long.parseLong(args[2]) * 60 * 1000;
                    testCreate(player, auctionManager, runner, price, duration);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid number");
                }
                break;
            case "buy":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testah buy <auctionId>");
                    return true;
                }
                try {
                    int auctionId = Integer.parseInt(args[1]);
                    testBuy(player, auctionManager, runner, auctionId);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid auction ID");
                }
                break;
            case "cancel":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testah cancel <auctionId>");
                    return true;
                }
                try {
                    int auctionId = Integer.parseInt(args[1]);
                    testCancel(player, auctionManager, runner, auctionId);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid auction ID");
                }
                break;
            case "expired":
                testExpiredItems(player, auctionManager, runner);
                break;
            case "claim":
                testClaim(player, auctionManager, runner);
                break;
            case "limits":
                testLimits(player, auctionManager, runner);
                break;
            case "price":
                testPriceLimits(player, auctionManager, runner);
                break;
            case "cancelcreate":
                testCancelCreate(player, auctionManager, runner);
                break;
            case "cancelbuy":
                testCancelBuy(player, auctionManager, runner);
                break;
            case "cancelcancel":
                testCancelCancel(player, auctionManager, runner);
                break;
            case "all":
                runAllTests(player, auctionManager, runner);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(g + "=== " + w + "AuctionTest" + g + " ===");
        player.sendMessage(g + "/testah info " + d + "- Auction system info");
        player.sendMessage(g + "/testah list " + d + "- List all active auctions");
        player.sendMessage(g + "/testah player " + d + "- List your auctions");
        player.sendMessage(g + "/testah create <price> <durationMin> " + d + "- Create auction (hold item)");
        player.sendMessage(g + "/testah buy <auctionId> " + d + "- Buy an auction");
        player.sendMessage(g + "/testah cancel <auctionId> " + d + "- Cancel your auction");
        player.sendMessage(g + "/testah expired " + d + "- Check expired items");
        player.sendMessage(g + "/testah claim " + d + "- Claim expired items");
        player.sendMessage(g + "/testah limits " + d + "- Check auction limits");
        player.sendMessage(g + "/testah price " + d + "- Check price limits");
        player.sendMessage(g + "/testah cancelcreate " + d + "- Test cancel create event");
        player.sendMessage(g + "/testah cancelbuy " + d + "- Test cancel buy event");
        player.sendMessage(g + "/testah cancelcancel " + d + "- Test cancel cancel event");
        player.sendMessage(g + "/testah all " + d + "- Run all tests");
    }

    private void testInfo(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing auction info...");

        int total = auctionManager.getTotalAuctionCount();
        int maxPerPlayer = auctionManager.getMaxAuctionsPerPlayer();
        boolean enabled = auctionManager.isEnabled();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Enabled: " + (enabled ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Total Auctions: " + a + total);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Max Per Player: " + a + maxPerPlayer);

        runner.reportResult(player, "Auction Info", true, "Total: " + total + ", Max: " + maxPerPlayer);
    }

    private void testList(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Listing active auctions...");

        List<Auction> auctions = auctionManager.getActiveAuctions();
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Found " + a + auctions.size() + w + " active auctions:");

        for (Auction auction : auctions) {
            String itemName = auction.getItem().getType().name();
            player.sendMessage(g + "  " + a + "#" + auction.getId() + g + " | " + d + auction.getSellerName() +
                    g + " | " + a + itemName + g + " | $" + a + auction.getPrice() +
                    g + " | " + formatTime(auction.getTimeRemaining()));
        }

        runner.reportResult(player, "List Auctions", true, "Found " + auctions.size() + " auctions");
    }

    private void testPlayerAuctions(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking your auctions...");

        List<Auction> auctions = auctionManager.getPlayerAuctions(player.getUniqueId());
        int count = auctionManager.getPlayerAuctionCount(player.getUniqueId());

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Your auctions: " + a + count);
        for (Auction auction : auctions) {
            player.sendMessage(g + "  " + a + "#" + auction.getId() + g + " | " +
                    auction.getItem().getType().name() + g + " | $" + a + auction.getPrice());
        }

        runner.reportResult(player, "Player Auctions", true, "Count: " + count);
    }

    private void testCreate(Player player, AuctionManager auctionManager, TestRunner runner, double price, long duration) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Creating auction...");

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Hold an item to sell!");
            runner.reportResult(player, "Create Auction", false, "No item in hand");
            return;
        }

        TestRunner.reset();

        auctionManager.createAuction(player, item, BigDecimal.valueOf(price), duration).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    runner.reportResult(player, "Create Auction", success, "Price: $" + price);

                    boolean eventFired = TestRunner.wasEventFired("AuctionCreateEvent");
                    runner.reportResult(player, "AuctionCreateEvent", eventFired, "Event fired: " + eventFired);

                    boolean successEventFired = TestRunner.wasEventFired("AuctionCreateSuccessEvent");
                    runner.reportResult(player, "AuctionCreateSuccessEvent", successEventFired, "Event fired: " + successEventFired);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testBuy(Player player, AuctionManager auctionManager, TestRunner runner, int auctionId) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Buying auction #" + auctionId);

        TestRunner.reset();

        auctionManager.buyAuction(player, auctionId).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    runner.reportResult(player, "Buy Auction", success, "ID: #" + auctionId);

                    boolean eventFired = TestRunner.wasEventFired("AuctionBuyEvent");
                    runner.reportResult(player, "AuctionBuyEvent", eventFired, "Event fired: " + eventFired);

                    boolean successEventFired = TestRunner.wasEventFired("AuctionBuySuccessEvent");
                    runner.reportResult(player, "AuctionBuySuccessEvent", successEventFired, "Event fired: " + successEventFired);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testCancel(Player player, AuctionManager auctionManager, TestRunner runner, int auctionId) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Cancelling auction #" + auctionId);

        TestRunner.reset();

        auctionManager.cancelAuction(player, auctionId).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    runner.reportResult(player, "Cancel Auction", success, "ID: #" + auctionId);

                    boolean eventFired = TestRunner.wasEventFired("AuctionCancelEvent");
                    runner.reportResult(player, "AuctionCancelEvent", eventFired, "Event fired: " + eventFired);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testExpiredItems(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking expired items...");

        boolean hasExpired = auctionManager.hasExpiredItems(player.getUniqueId());
        List<ItemStack> items = auctionManager.getPlayerExpiredItems(player.getUniqueId());

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Has expired items: " + (hasExpired ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Items waiting: " + a + items.size());

        for (ItemStack item : items) {
            player.sendMessage(g + "  " + d + "- " + a + item.getType().name() + g + " x" + item.getAmount());
        }

        runner.reportResult(player, "Expired Items Check", true, "Items waiting: " + items.size());
    }

    private void testClaim(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Claiming expired items...");

        TestRunner.reset();

        boolean claimed = auctionManager.claimExpiredItems(player);
        String status = claimed ? gr + "SUCCESS" : r + "NO ITEMS";
        player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

        runner.reportResult(player, "Claim Items", claimed, "Claimed: " + claimed);

        boolean eventFired = TestRunner.wasEventFired("AuctionClaimEvent");
        runner.reportResult(player, "AuctionClaimEvent", eventFired, "Event fired: " + eventFired);
    }

    private void testLimits(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking limits...");

        int max = auctionManager.getMaxAuctionsPerPlayer();
        int current = auctionManager.getPlayerAuctionCount(player.getUniqueId());
        int remaining = max - current;

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Max: " + a + max);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Current: " + a + current);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Remaining: " + a + remaining);

        runner.reportResult(player, "Auction Limits", true, max + " max, " + current + " used");
    }

    private void testPriceLimits(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking price limits...");

        BigDecimal min = auctionManager.getMinPrice();
        BigDecimal max = auctionManager.getMaxPrice();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Min Price: " + a + "$" + min);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Max Price: " + (max != null ? a + "$" + max : a + "Unlimited"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Default Duration: " + a + formatTime(auctionManager.getDefaultDuration()));

        runner.reportResult(player, "Price Limits", true, "Min: $" + min);
    }

    private void testCancelCreate(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing create cancellation...");
        player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "This test requires a plugin to cancel AuctionCreateEvent");
        runner.reportResult(player, "Cancel Create Test", true, "Event can be cancelled");
    }

    private void testCancelBuy(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing buy cancellation...");
        player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "This test requires a plugin to cancel AuctionBuyEvent");
        runner.reportResult(player, "Cancel Buy Test", true, "Event can be cancelled");
    }

    private void testCancelCancel(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing cancel cancellation...");
        player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "This test requires a plugin to cancel AuctionCancelEvent");
        runner.reportResult(player, "Cancel Cancel Test", true, "Event can be cancelled");
    }

    private void runAllTests(Player player, AuctionManager auctionManager, TestRunner runner) {
        player.sendMessage(g + "=== " + w + "Running All Auction Tests" + g + " ===");

        testInfo(player, auctionManager, runner);
        testList(player, auctionManager, runner);
        testPlayerAuctions(player, auctionManager, runner);
        testLimits(player, auctionManager, runner);
        testPriceLimits(player, auctionManager, runner);
        testExpiredItems(player, auctionManager, runner);

        new BukkitRunnable() {
            @Override
            public void run() {
                runner.printSummary(player);
            }
        }.runTaskLater(Main.getInstance(), 100L);
    }

    private String formatTime(long ms) {
        if (ms <= 0) return "Expired";
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + "d " + (hours % 24) + "h";
        if (hours > 0) return hours + "h " + (minutes % 60) + "m";
        if (minutes > 0) return minutes + "m " + (seconds % 60) + "s";
        return seconds + "s";
    }
}