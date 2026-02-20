package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.event.afk.AFKManager;
import net.godlycow.org.essc.api.event.auction.AuctionManager;
import net.godlycow.org.essc.api.event.back.BackManager;
import net.godlycow.org.essc.api.event.home.HomeManager;
import net.godlycow.org.essc.api.event.shop.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* * Note: These comments were written by AI to keep the code clear and easy to understand for everyone.
 */
public interface EssentialsCAPI {

    // Get the main instance of the API to start using its features
    @Nullable
    static EssentialsCAPI getInstance() {
        return APIProvider.getInstance();
    }

    // Get the manager that handles player homes and teleports
    @NotNull
    HomeManager getHomeManager();

    // Get the manager that handles the shop system and economy
    @NotNull
    ShopManager getShopManager();

    // Get the manager that tracks if players are away from their keyboard
    @NotNull
    AFKManager getAFKManager();

    // Get the manager for the auction house and player listings
    @NotNull
    AuctionManager getAuctionManager();

    // Get the manager that handles player back teleports
    @NotNull
    BackManager getBackManager();

    // Check if the API has finished loading and is ready for use
    boolean isReady();

    // Get the JavaPlugin instance of EssentialsC
    @NotNull
    JavaPlugin getPlugin();
}