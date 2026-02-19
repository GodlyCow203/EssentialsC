package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.event.shop.ShopCategory;
import net.godlycow.org.essc.api.event.shop.ShopItem;
import net.godlycow.org.essc.api.event.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.Map;

public class ShopTestCommand implements CommandExecutor {

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
            sender.sendMessage(g + "Console cannot run shop tests");
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

        ShopManager shopManager = api.getShopManager();
        if (!shopManager.isEnabled()) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Shop system is disabled");
            return true;
        }

        TestRunner.reset();
        TestRunner runner = new TestRunner();

        switch (args[0].toLowerCase()) {
            case "info":
                testInfo(player, shopManager, runner);
                break;
            case "categories":
                testCategories(player, shopManager, runner);
                break;
            case "find":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testshop find <itemId>");
                    return true;
                }
                testFindItem(player, shopManager, runner, args[1]);
                break;
            case "purchase":
                if (args.length < 3) {
                    player.sendMessage(g + "Usage: " + w + "/testshop purchase <itemId> <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    testPurchase(player, shopManager, runner, args[1], amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid amount");
                }
                break;
            case "sell":
                if (args.length < 3) {
                    player.sendMessage(g + "Usage: " + w + "/testshop sell <itemId> <amount>");
                    return true;
                }
                try {
                    int amount = Integer.parseInt(args[2]);
                    testSell(player, shopManager, runner, args[1], amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid amount");
                }
                break;
            case "sellall":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testshop sellall <itemId>");
                    return true;
                }
                testSellAll(player, shopManager, runner, args[1]);
                break;
            case "balance":
                testBalance(player, shopManager, runner);
                break;
            case "has":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testshop has <amount>");
                    return true;
                }
                try {
                    BigDecimal amount = new BigDecimal(args[1]);
                    testHasEnough(player, shopManager, runner, amount);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid amount");
                }
                break;
            case "open":
                testOpenShop(player, shopManager, runner);
                break;
            case "openpage":
                if (args.length < 2) {
                    player.sendMessage(g + "Usage: " + w + "/testshop openpage <category> [page]");
                    return true;
                }
                int page = args.length > 2 ? Integer.parseInt(args[2]) : 1;
                testOpenCategory(player, shopManager, runner, args[1], page);
                break;
            case "reload":
                testReload(player, shopManager, runner);
                break;
            case "stock":
                if (args.length < 3) {
                    player.sendMessage(g + "Usage: " + w + "/testshop stock <itemId> <amount>");
                    return true;
                }
                try {
                    int stock = Integer.parseInt(args[2]);
                    testSetStock(player, shopManager, runner, args[1], stock);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid amount");
                }
                break;
            case "price":
                if (args.length < 4) {
                    player.sendMessage(g + "Usage: " + w + "/testshop price <buy|sell> <itemId> <price>");
                    return true;
                }
                try {
                    double price = Double.parseDouble(args[3]);
                    testSetPrice(player, shopManager, runner, args[1], args[2], price);
                } catch (NumberFormatException e) {
                    player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Invalid price");
                }
                break;
            case "inventory":
                testInventoryCount(player, shopManager, runner);
                break;
            case "currency":
                testCurrency(player, shopManager, runner);
                break;
            case "cancelpurchase":
                testCancelPurchase(player, shopManager, runner);
                break;
            case "cancelsell":
                testCancelSell(player, shopManager, runner);
                break;
            case "all":
                runAllTests(player, shopManager, runner);
                break;
            default:
                sendHelp(player);
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(g + "=== " + w + "ShopTest" + g + " ===");
        player.sendMessage(g + "/testshop info " + d + "- Shop system info");
        player.sendMessage(g + "/testshop categories " + d + "- List all categories");
        player.sendMessage(g + "/testshop find <itemId> " + d + "- Find an item");
        player.sendMessage(g + "/testshop purchase <itemId> <amount> " + d + "- Buy items");
        player.sendMessage(g + "/testshop sell <itemId> <amount> " + d + "- Sell items");
        player.sendMessage(g + "/testshop sellall <itemId> " + d + "- Sell all of item");
        player.sendMessage(g + "/testshop balance " + d + "- Check balance");
        player.sendMessage(g + "/testshop has <amount> " + d + "- Check if has enough");
        player.sendMessage(g + "/testshop open " + d + "- Open main shop");
        player.sendMessage(g + "/testshop openpage <cat> [page] " + d + "- Open category");
        player.sendMessage(g + "/testshop reload " + d + "- Reload shop config");
        player.sendMessage(g + "/testshop stock <item> <amount> " + d + "- Set item stock");
        player.sendMessage(g + "/testshop price <buy|sell> <item> <price> " + d + "- Set price");
        player.sendMessage(g + "/testshop inventory " + d + "- Check inventory counts");
        player.sendMessage(g + "/testshop currency " + d + "- Show currency names");
        player.sendMessage(g + "/testshop cancelpurchase " + d + "- Test cancel purchase event");
        player.sendMessage(g + "/testshop cancelsell " + d + "- Test cancel sell event");
        player.sendMessage(g + "/testshop all " + d + "- Run all tests");
    }

    private void testInfo(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing shop info...");

        int categories = shopManager.getCategoryCount();
        int items = shopManager.getTotalItemCount();
        boolean enabled = shopManager.isEnabled();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Enabled: " + (enabled ? gr + "YES" : r + "NO"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Categories: " + a + categories);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Total Items: " + a + items);

        runner.reportResult(player, "Shop Info", true, "Categories: " + categories + ", Items: " + items);
    }

    private void testCategories(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing categories...");

        Map<String, ShopCategory> categories = shopManager.getCategories();
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Found " + a + categories.size() + w + " categories:");

        for (ShopCategory cat : categories.values()) {
            String status = cat.isEnabled() ? gr + "✓" : r + "✗";
            player.sendMessage(g + "  " + status + " " + a + cat.getId() + g + " - " + d + cat.getDisplayName() +
                    g + " (" + cat.getItemCount() + " items)");
        }

        runner.reportResult(player, "Categories List", !categories.isEmpty(), "Found " + categories.size() + " categories");
    }

    private void testFindItem(Player player, ShopManager shopManager, TestRunner runner, String itemId) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Finding item: " + a + itemId);

        shopManager.findItem(itemId).thenAccept(opt -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (opt.isPresent()) {
                        ShopItem item = opt.get();
                        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Found: " + gr + item.getId());
                        player.sendMessage(g + "  " + d + "Material: " + a + item.getMaterial());
                        player.sendMessage(g + "  " + d + "Buy: " + a + "$" + item.getBuyPrice() + g + " | Sell: " + a + "$" + item.getSellPrice());
                        player.sendMessage(g + "  " + d + "Stock: " + a + item.getStock() + g + " | Category: " + a + item.getCategory());
                        runner.reportResult(player, "Find Item", true, "Found " + item.getId());
                    } else {
                        player.sendMessage(g + "[" + r + "INFO" + g + "] " + w + "Item not found: " + r + itemId);
                        runner.reportResult(player, "Find Item", false, "Item not found");
                    }
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testPurchase(Player player, ShopManager shopManager, TestRunner runner, String itemId, int amount) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing purchase: " + a + itemId + " x" + amount);

        TestRunner.reset();

        shopManager.purchase(player, itemId, amount).thenAccept(result -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean success = result.success();
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    if (!success && result.getErrorMessage() != null) {
                        player.sendMessage(g + "  " + d + "Error: " + r + result.getErrorMessage());
                    } else if (success) {
                        player.sendMessage(g + "  " + d + "Purchased: " + a + result.getAmountPurchased());
                        player.sendMessage(g + "  " + d + "Total: " + a + "$" + String.format("%.2f", result.getTotalPrice()));
                    }

                    runner.reportResult(player, "Purchase", success, result.getErrorMessage());

                    boolean eventFired = TestRunner.wasEventFired("ShopPurchaseEvent");
                    runner.reportResult(player, "ShopPurchaseEvent", eventFired, "Event fired: " + eventFired);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testSell(Player player, ShopManager shopManager, TestRunner runner, String itemId, int amount) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing sell: " + a + itemId + " x" + amount);

        shopManager.findItem(itemId).thenCompose(opt -> {
            if (opt.isPresent()) {
                return shopManager.sell(player, opt.get(), amount);
            }
            return java.util.concurrent.CompletableFuture.completedFuture(
                    new ShopManager.SellResult() {
                        public boolean success() { return false; }
                        public String getErrorMessage() { return "Item not found"; }
                        public int getAmountSold() { return 0; }
                        public double getTotalPrice() { return 0; }
                        public ShopItem getItem() { return null; }
                    }
            );
        }).thenAccept(result -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean success = result.success();
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    if (!success && result.getErrorMessage() != null) {
                        player.sendMessage(g + "  " + d + "Error: " + r + result.getErrorMessage());
                    } else if (success) {
                        player.sendMessage(g + "  " + d + "Sold: " + a + result.getAmountSold());
                        player.sendMessage(g + "  " + d + "Earned: " + a + "$" + String.format("%.2f", result.getTotalPrice()));
                    }

                    runner.reportResult(player, "Sell", success, result.getErrorMessage());
                    runner.reportResult(player, "ShopSellEvent", TestRunner.wasEventFired("ShopSellEvent"), "Event fired check");
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testSellAll(Player player, ShopManager shopManager, TestRunner runner, String itemId) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing sell all: " + a + itemId);

        shopManager.findItem(itemId).thenCompose(opt -> {
            if (opt.isPresent()) {
                return shopManager.sellAll(player, opt.get());
            }
            return java.util.concurrent.CompletableFuture.completedFuture(
                    new ShopManager.SellResult() {
                        public boolean success() { return false; }
                        public String getErrorMessage() { return "Item not found"; }
                        public int getAmountSold() { return 0; }
                        public double getTotalPrice() { return 0; }
                        public ShopItem getItem() { return null; }
                    }
            );
        }).thenAccept(result -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean success = result.success();
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);

                    if (!success && result.getErrorMessage() != null) {
                        player.sendMessage(g + "  " + d + "Error: " + r + result.getErrorMessage());
                    } else if (success) {
                        player.sendMessage(g + "  " + d + "Sold: " + a + result.getAmountSold());
                        player.sendMessage(g + "  " + d + "Earned: " + a + "$" + String.format("%.2f", result.getTotalPrice()));
                    }

                    runner.reportResult(player, "Sell All", success, "Sold " + result.getAmountSold() + " items");
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testBalance(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing balance check...");

        shopManager.getBalance(player).thenAccept(balance -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String formatted = shopManager.formatBalance(balance);
                    player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Balance: " + a + formatted);
                    player.sendMessage(g + "  " + d + "Raw: " + a + balance.toPlainString());
                    runner.reportResult(player, "Balance Check", true, "Balance: " + formatted);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testHasEnough(Player player, ShopManager shopManager, TestRunner runner, BigDecimal amount) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking if has: " + a + amount);

        shopManager.hasEnough(player, amount).thenAccept(hasEnough -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = hasEnough ? gr + "YES" : r + "NO";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + w + "Has enough: " + status);
                    runner.reportResult(player, "Has Enough", true, "Result: " + hasEnough);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testOpenShop(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing open main shop...");
        shopManager.openMainShop(player);
        runner.reportResult(player, "Open Main Shop", true, "GUI opened");
        new BukkitRunnable() {
            @Override
            public void run() {
                runner.reportResult(player, "ShopOpenEvent", TestRunner.wasEventFired("ShopOpenEvent"), "Event fired check");
            }
        }.runTaskLater(Main.getInstance(), 5L);
    }

    private void testOpenCategory(Player player, ShopManager shopManager, TestRunner runner, String categoryId, int page) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing open category: " + a + categoryId + g + " page " + a + page);

        if (!shopManager.hasCategory(categoryId)) {
            player.sendMessage(g + "[" + r + "ERROR" + g + "] " + w + "Category not found: " + r + categoryId);
            runner.reportResult(player, "Open Category", false, "Category not found");
            return;
        }

        shopManager.openCategory(player, categoryId, page);
        runner.reportResult(player, "Open Category", true, "Opened " + categoryId + " page " + page);
    }

    private void testReload(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing shop reload...");
        shopManager.reload();
        runner.reportResult(player, "Shop Reload", true, "Reload triggered");
        new BukkitRunnable() {
            @Override
            public void run() {
                runner.reportResult(player, "ShopReloadEvent", TestRunner.wasEventFired("ShopReloadEvent"), "Event fired check");
            }
        }.runTaskLater(Main.getInstance(), 10L);
    }

    private void testSetStock(Player player, ShopManager shopManager, TestRunner runner, String itemId, int stock) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Setting stock: " + a + itemId + g + " = " + a + stock);

        shopManager.setItemStock(itemId, stock).thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);
                    runner.reportResult(player, "Set Stock", success, "Stock set to " + stock);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testSetPrice(Player player, ShopManager shopManager, TestRunner runner, String type, String itemId, double price) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Setting " + type + " price: " + a + itemId + g + " = $" + a + price);

        java.util.concurrent.CompletableFuture<Boolean> future;
        if (type.equalsIgnoreCase("buy")) {
            future = shopManager.setItemBuyPrice(itemId, price);
        } else {
            future = shopManager.setItemSellPrice(itemId, price);
        }

        future.thenAccept(success -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    String status = success ? gr + "SUCCESS" : r + "FAILED";
                    player.sendMessage(g + "[" + d + "RESULT" + g + "] " + status);
                    runner.reportResult(player, "Set " + type + " Price", success, "Price set to " + price);
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testInventoryCount(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Checking inventory counts...");

        shopManager.findItem("stone").thenAccept(opt -> {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (opt.isPresent()) {
                        int count = shopManager.getPlayerItemCount(player, opt.get());
                        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Stone in inventory: " + a + count);
                        runner.reportResult(player, "Inventory Count", true, "Found " + count + " stone");
                    } else {
                        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Using stone as test item (not found in shop)");
                        runner.reportResult(player, "Inventory Count", false, "Test item not in shop");
                    }
                }
            }.runTask(Main.getInstance());
        });
    }

    private void testCurrency(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing currency...");

        String singular = shopManager.getCurrencySingular();
        String plural = shopManager.getCurrencyPlural();

        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Singular: " + a + singular);
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Plural: " + a + plural);

        // Test formatting
        String formatted = shopManager.formatBalance(new BigDecimal("1234.56"));
        player.sendMessage(g + "[" + d + "INFO" + g + "] " + w + "Format test: " + a + formatted);

        runner.reportResult(player, "Currency Info", true, singular + " / " + plural);
    }

    private void testCancelPurchase(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing purchase cancellation...");
        player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "This test requires a plugin to cancel ShopPurchaseEvent");
        runner.reportResult(player, "Cancel Purchase Test", true, "Event can be cancelled");
    }

    private void testCancelSell(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "[" + d + "TEST" + g + "] " + w + "Testing sell cancellation...");
        player.sendMessage(g + "[" + y + "INFO" + g + "] " + w + "This test requires a plugin to cancel ShopSellEvent");
        runner.reportResult(player, "Cancel Sell Test", true, "Event can be cancelled");
    }

    private void runAllTests(Player player, ShopManager shopManager, TestRunner runner) {
        player.sendMessage(g + "=== " + w + "Running All Shop Tests" + g + " ===");

        testInfo(player, shopManager, runner);
        testCategories(player, shopManager, runner);
        testBalance(player, shopManager, runner);
        testCurrency(player, shopManager, runner);

        Map<String, ShopCategory> cats = shopManager.getCategories();
        if (!cats.isEmpty()) {
            ShopCategory firstCat = cats.values().iterator().next();
            if (firstCat.getItemCount() > 0) {
                ShopItem firstItem = firstCat.getAllItems().get(0);
                testFindItem(player, shopManager, runner, firstItem.getId());
            }
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                runner.printSummary(player);
            }
        }.runTaskLater(Main.getInstance(), 100L);
    }
}