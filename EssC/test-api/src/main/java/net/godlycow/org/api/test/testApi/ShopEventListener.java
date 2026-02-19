package net.godlycow.org.api.test.testApi;

import net.godlycow.org.essc.api.event.shop.open.ShopOpenEvent;
import net.godlycow.org.essc.api.event.shop.purchase.ShopPurchaseEvent;
import net.godlycow.org.essc.api.event.shop.reload.ShopReloadEvent;
import net.godlycow.org.essc.api.event.shop.sell.ShopSellEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ShopEventListener implements Listener {
    private final Main plugin;
    private final ChatColor g = ChatColor.GRAY;
    private final ChatColor w = ChatColor.WHITE;
    private final ChatColor d = ChatColor.DARK_GRAY;
    private final ChatColor y = ChatColor.YELLOW;
    private final ChatColor a = ChatColor.AQUA;

    public ShopEventListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onShopOpen(ShopOpenEvent event) {
        TestRunner.markEventFired("ShopOpenEvent");
        Player player = event.getPlayer();
        String context = event.getContext().name();
        String category = event.getCategory() != null ? event.getCategory().getId() : "MainMenu";

        String msg = g + "[" + d + "SHOP" + g + "] " + y + "Open" + g + " | " + d + player.getName() +
                g + " | " + a + context + g + " | " + a + category;
        broadcast(msg);
    }

    @EventHandler
    public void onShopPurchase(ShopPurchaseEvent event) {
        TestRunner.markEventFired("ShopPurchaseEvent");
        Player player = event.getPlayer();
        String item = event.getItem().getId();
        int amount = event.getAmount();
        double price = event.getTotalPrice();

        String msg = g + "[" + d + "SHOP" + g + "] " + y + "Purchase" + g + " | " + d + player.getName() +
                g + " | " + a + item + g + " x" + a + amount + g + " | $" + a + String.format("%.2f", price);
        broadcast(msg);
    }

    @EventHandler
    public void onShopSell(ShopSellEvent event) {
        TestRunner.markEventFired("ShopSellEvent");
        Player player = event.getPlayer();
        String item = event.getItem().getId();
        int amount = event.getAmount();
        double price = event.getTotalPrice();

        String msg = g + "[" + d + "SHOP" + g + "] " + y + "Sell" + g + " | " + d + player.getName() +
                g + " | " + a + item + g + " x" + a + amount + g + " | $" + a + String.format("%.2f", price);
        broadcast(msg);
    }

    @EventHandler
    public void onShopReload(ShopReloadEvent event) {
        TestRunner.markEventFired("ShopReloadEvent");
        String reloader = event.getReloader() != null ? event.getReloader().getName() : "Console";
        int cats = event.getCategoriesLoaded();
        int items = event.getItemsLoaded();

        String msg = g + "[" + d + "SHOP" + g + "] " + y + "Reload" + g + " | " + d + reloader +
                g + " | " + a + cats + " cats" + g + " | " + a + items + " items";
        broadcast(msg);
    }

    private void broadcast(String message) {
        plugin.getServer().broadcastMessage(message);
        plugin.getLogger().info(ChatColor.stripColor(message));
    }
}