package net.godlycow.org.essc.bootstrap;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.modules.afk.AFKManager;
import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.impl.EssentialsCAPIImpl;
import net.godlycow.org.essc.CommandRegistration;
import net.godlycow.org.essc.modules.auction.AhSoundManager;
import net.godlycow.org.essc.modules.auction.AuctionManager;
import net.godlycow.org.essc.modules.auction.gui.AhGuiHolder;
import net.godlycow.org.essc.modules.auction.gui.AhGuiManager;
import net.godlycow.org.essc.modules.back.BackManager;
import net.godlycow.org.essc.modules.backup.BackupManager;
import net.godlycow.org.essc.integration.bedrock.BedrockUtil;
import net.godlycow.org.essc.integration.bedrock.FloodgateHook;
import net.godlycow.org.essc.bootstrap.registrar.CommandRegistrar;
import net.godlycow.org.essc.bootstrap.registrar.EconomyRegistrar;
import net.godlycow.org.essc.bootstrap.registrar.ListenerRegistrar;
import net.godlycow.org.essc.integration.metrics.bstats.EconomyCharts;
import net.godlycow.org.essc.integration.metrics.bstats.UsageCharts;
import net.godlycow.org.essc.modules.chat.ChatManager;
import net.godlycow.org.essc.command.auction.AhCommand;
import net.godlycow.org.essc.integration.discord.DiscordSRVHook;
import net.godlycow.org.essc.integration.metrics.faststats.FastStatsManager;
import net.godlycow.org.essc.modules.fly.FlyManager;
import net.godlycow.org.essc.modules.fly.FlyMigration;
import net.godlycow.org.essc.modules.kit.gui.KitGuiManager;
import net.godlycow.org.essc.modules.punishment.IpHistoryMigration;
import net.godlycow.org.essc.plugin.gui.GuiFramework;
import net.godlycow.org.essc.modules.home.HomeManager;
import net.godlycow.org.essc.modules.home.HomeNotificationManager;
import net.godlycow.org.essc.storage.user.UserManager;
import net.godlycow.org.essc.modules.kit.KitManager;
import net.godlycow.org.essc.modules.kit.gui.KitGuiHolder;
import net.godlycow.org.essc.language.HelpManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.plugin.listener.AhListener;
import net.godlycow.org.essc.plugin.listener.BanListener;
import net.godlycow.org.essc.plugin.listener.WarpListener;
import net.godlycow.org.essc.modules.MOTDManager;
import net.godlycow.org.essc.modules.ReplyManager;
import net.godlycow.org.essc.modules.nick.NickManager;
import net.godlycow.org.essc.integration.placeholderapi.PlaceholderHook;
import net.godlycow.org.essc.modules.punishment.PunishmentManager;
import net.godlycow.org.essc.modules.rtp.RTPGuiManager;
import net.godlycow.org.essc.modules.rtp.RTPManager;
import net.godlycow.org.essc.modules.RulesManager;
import net.godlycow.org.essc.modules.scoreboard.ScoreboardManager;
import net.godlycow.org.essc.modules.shop.ShopGuiManager;
import net.godlycow.org.essc.modules.shop.ShopListener;
import net.godlycow.org.essc.modules.shop.ShopManager;
import net.godlycow.org.essc.modules.shop.ShopHolder;
import net.godlycow.org.essc.modules.shop.ShopSoundManager;
import net.godlycow.org.essc.modules.shop.sell.SellListener;
import net.godlycow.org.essc.modules.shop.sell.SellManager;
import net.godlycow.org.essc.server.software.ServerSoftware;
import net.godlycow.org.essc.modules.SpawnManager;
import net.godlycow.org.essc.modules.tab.TabManager;
import net.godlycow.org.essc.modules.teleport.TPAManager;
import net.godlycow.org.essc.util.ItemUtil;
import net.godlycow.org.essc.util.StartupBanner;
import net.godlycow.org.essc.modules.VanishManager;
import net.godlycow.org.essc.modules.warp.WarpManager;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredListener;

public final class PluginLoader {

    private final EssentialsC plugin;
    private final StartupTimer timer = new StartupTimer();

    public PluginLoader(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public void start() {
        try {
            load();
        } catch (Exception ex) {
            CrashHandler.handle(plugin, ex);
        }
    }

    private void load() {
        timer.start();
        StartupBanner.print(plugin, plugin.getLogger());
        timer.mark("banner");
        loadLanguages();
        timer.mark("languages");
        registerAPI();
        timer.mark("api");
        startPlugin();
        timer.mark("plugin");
        startMetrics();
        printthis();
        timer.mark("metrics");
        registerPlaceholderAPI();
        timer.mark("placeholderapi");
        String timings = timer.finish();
        plugin.debug("EssentialsC enabled — " + timings);
    }

    private void loadLanguages() {
        saveResourceIfAbsent("lang/en_US.json");
        saveResourceIfAbsent("lang/de_DE.json");

        LanguageManager languageManager = new LanguageManager(plugin);
        languageManager.load(plugin.getConfigManager().getDefaultLanguage());
        plugin.setLanguageManager(languageManager);

        HelpManager helpManager = new HelpManager(plugin);
        helpManager.load(plugin.getConfigManager().getDefaultLanguage());
        plugin.setHelpManager(helpManager);
    }


    private void registerAPI() {
        EssentialsCAPIImpl apiImpl = new EssentialsCAPIImpl(plugin);
        plugin.setApiImplementation(apiImpl);
        APIProvider.register(apiImpl);
    }

    private void startPlugin() {
        if (plugin.getConfigManager().isBackupEnabled()) {
            plugin.setBackupManager(new BackupManager(plugin));
        }

        if (plugin.getConfigManager().isMotdEnabled()) {
            plugin.setMotdManager(new MOTDManager(plugin));
        }

        if (plugin.getConfigManager().isTPAEnabled()) {
            plugin.setTpaManager(new TPAManager(plugin));
        } else {
            unloadTPA();
        }
        if (plugin.getConfigManager().isHomesEnabled()) {
            plugin.setHomeManager(new HomeManager(plugin));
            plugin.setHomeNotificationManager(new HomeNotificationManager(plugin));
        } else {
            unloadHomes();
        }
        if (plugin.getConfigManager().isSpawnEnabled()) {
            plugin.setSpawnManager(new SpawnManager(plugin));
        } else {
            unloadSpawn();
        }
        if (plugin.getConfigManager().isBackEnabled()) {
            plugin.setBackManager(new BackManager(plugin));
        } else {
            unloadBack();
        }
        if (plugin.getConfigManager().isKitsEnabled()) {
            plugin.setKitManager(new KitManager(plugin));
        } else {

            unloadKits();
        }
        if (plugin.getConfigManager().isVanishEnabled()) {
            plugin.setVanishManager(new VanishManager(plugin));
        } else {
            unloadVanish();
        }
        plugin.setReplyManager(new ReplyManager());
        if (plugin.getConfigManager().isChatSystemEnabled()) {
            plugin.setChatManager(new ChatManager(plugin));
        } else {
            plugin.getLogger().info("Chat system is disabled in config.");
        }
        plugin.setUserManager(new UserManager(plugin));
        if (plugin.getConfigManager().isPunishmentsEnabled()) {
            plugin.setPunishmentManager(new PunishmentManager(plugin));
        } else {
            unloadPunishments();
        }
        plugin.setFlyManager(new FlyManager(plugin));
        new FlyMigration(plugin).runIfNeeded();
        new IpHistoryMigration(plugin).runIfNeeded();

        RulesManager rulesManager = new RulesManager(plugin);
        rulesManager.load();
        plugin.setRulesManager(rulesManager);

        FloodgateHook floodgateHook = new FloodgateHook(plugin);
        plugin.setBedrockUtil(new BedrockUtil(plugin, floodgateHook));

        if (plugin.getConfigManager().isScoreboardEnabled()) {
            if (ServerSoftware.isFolia()) {
                plugin.getLogger().warning("Scoreboard feature is not *yet* supported on Folia.");
            } else {
                plugin.setScoreboardManager(new ScoreboardManager(plugin));
            }
        }

        if (plugin.getConfigManager().isLuckPermsTabEnabled() || plugin.getConfigManager().isNickEnabled()) {
            plugin.setTabManager(new TabManager(plugin));
        }

        if (plugin.getConfigManager().isNickEnabled()) {
            plugin.setNickManager(new NickManager(plugin));
        }

        if (plugin.getConfigManager().isRTPEnabled()) {
            RTPManager rtpManager = new RTPManager(plugin);
            plugin.setRtpManager(rtpManager);
            plugin.setRtpGuiManager(new RTPGuiManager(plugin, rtpManager));
        }

        if (plugin.getConfigManager().isEconomyEnabled()) {
            new EconomyRegistrar(plugin).enable();
        }

        if (plugin.getEconomyManager() != null) {
            plugin.getServer().getPluginManager().registerEvents(plugin.getEconomyManager(), plugin);
        }

        GuiFramework guiFramework = null;
        if (plugin.getConfigManager().isAHEnabled() || plugin.getConfigManager().isShopEnabled() || plugin.getConfigManager().isTrashEnabled() || isKitGuiAvailable()) {
            guiFramework = new GuiFramework(plugin);
            guiFramework.loadTemplates();
            plugin.setGuiFramework(guiFramework);
        }

        if (isKitGuiAvailable() && guiFramework != null) {
            plugin.setKitGuiManager(new KitGuiManager(plugin, guiFramework));
        }

        if (plugin.getConfigManager().isAHEnabled()) {

            AuctionManager auctionManager = new AuctionManager(plugin);
            plugin.setAuctionManager(auctionManager);

            AhGuiManager ahGuiManager = new AhGuiManager(plugin, guiFramework, new AhSoundManager(plugin));
            plugin.setAhGuiManager(ahGuiManager);

            new AhListener(plugin, new AhCommand(plugin, ahGuiManager));
        } else {
            unloadAuctionHouse();
        }

        if (plugin.getConfigManager().isShopEnabled()) {
            ShopManager shopManager = new ShopManager(plugin);
            plugin.setShopManager(shopManager);

            ShopSoundManager shopSounds = new ShopSoundManager(plugin);
            ShopListener shopListener = new ShopListener(plugin, shopManager, shopSounds);
            shopManager.setShopListener(shopListener);
            plugin.getServer().getPluginManager().registerEvents(shopListener, plugin);

            if (guiFramework != null) {
                shopManager.setShopGuiManager(new ShopGuiManager(plugin, guiFramework, shopManager, shopSounds));
            }
        } else {
            unloadShop();
        }

        if (plugin.getConfigManager().isWarpEnabled()) {
            plugin.setWarpManager(new WarpManager(plugin));
            plugin.getServer().getPluginManager().registerEvents(new WarpListener(plugin), plugin);
        }

        if (plugin.getConfigManager().isAfkEnabled()) {
            plugin.setAfkManager(new AFKManager(plugin));
        }

        if (plugin.getConfigManager().isDiscordSRVEnabled()) {
            DiscordSRVHook discordSRVHook = new DiscordSRVHook(plugin);
            discordSRVHook.init();
            plugin.setDiscordSRVHook(discordSRVHook);
        }

        if (plugin.getConfigManager().isSellEnabled()) {
            SellListener sellListener = new SellListener(plugin);
            SellManager sellManager = new SellManager(plugin, sellListener);
            sellListener.setSellManager(sellManager);
            plugin.setSellManager(sellManager);
            plugin.getServer().getPluginManager().registerEvents(sellListener, plugin);
        }

        new FirstRunHandler(plugin);

        new ListenerRegistrar(plugin);
        if (plugin.getConfigManager().isPunishmentsEnabled()) {
            plugin.getServer().getPluginManager().registerEvents(new BanListener(plugin, plugin.getPunishmentManager()), plugin);
        }
        plugin.getServer().getPluginManager().registerEvents(plugin, plugin);

        new CommandRegistrar(plugin).registerAll();
    }

    private void printthis(){
        ItemUtil itemUtil = ItemUtil.getInstance();
        plugin.getLogger().info("Loaded " + itemUtil.size() + " Items from items.json" );
    }

    private void startMetrics() {
        int pluginId = 29401;
        Metrics metrics = new Metrics(plugin, pluginId);
        plugin.debug("bStats Metrics initialized.");

        if (plugin.getConfigManager().isEconomyEnabled()) {
            EconomyCharts.register(plugin, metrics);
        }

        UsageCharts.register(plugin, metrics);

        plugin.getFastStatsManager().ready();
    }

    private void unloadAuctionHouse() {
        plugin.debug("Auction House is disabled in config – fully unloading.");

        CommandRegistration.unregisterCommand("ah");
        CommandRegistration.unregisterCommand("essentialsc:ah");
        CommandRegistration.unregisterCommand("auction");
        CommandRegistration.unregisterCommand("essentialsc:auction");
        plugin.debug("AH commands unregistered.");

        for (org.bukkit.entity.Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory() != null
                    && player.getOpenInventory().getTopInventory().getHolder() instanceof AhGuiHolder) {
                player.closeInventory();
            }
        }

        for (org.bukkit.event.HandlerList handlerList : org.bukkit.event.HandlerList.getHandlerLists()) {
            for (org.bukkit.plugin.RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Auction") || name.contains("Ah")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered AH listener: " + name);
                    }
                }
            }
        }

        if (plugin.getAuctionManager() != null) {
            plugin.getAuctionManager().shutdown();
            plugin.setAuctionManager(null);
        }

        plugin.setAhGuiManager(null);
        plugin.debug("Auction House fully unloaded.");
    }


    private boolean isKitGuiAvailable() {
        return plugin.getConfigManager().isKitsEnabled() && plugin.getConfigManager().isKitGuiMode();
    }

    private void unloadVanish() {
        plugin.debug("vanish is disabled in the config, unloading");

        CommandRegistration.unregisterCommand("vanish");
        CommandRegistration.unregisterCommand("essentialsc:vanish");

        plugin.debug("Unregistered All Vanish Commands");

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Vanish") || name.contains("ServerListPing")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Vanish listener: " + name);
                    }
                }
            }
        }

        plugin.setVanishManager(null);

        plugin.debug("Vanish fully unloaded");
    }

    private void unloadBack() {
        plugin.debug("Back is disabled in the config, fully unloading");

        CommandRegistration.unregisterCommand("back");
        CommandRegistration.unregisterCommand("essentialsc:back");
        CommandRegistration.unregisterCommand("dback");
        CommandRegistration.unregisterCommand("essentialsc:dback");


        plugin.debug("Unregistered Back Commands");

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Back")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Back listener: " + name);
                    }
                }
            }
        }

        if (plugin.getBackManager() != null) {
            plugin.getBackManager().shutdown();
            plugin.setBackManager(null);
        }

        plugin.debug("Back fully unloaded");
    }

    private void unloadPunishments() {

        plugin.debug("Punishments are disabled in the config, unloading");

        CommandRegistration.unregisterCommand("ban");
        CommandRegistration.unregisterCommand("essentialsc:ban");
        CommandRegistration.unregisterCommand("ban-ip");
        CommandRegistration.unregisterCommand("essentialsc:ban-ip");
        CommandRegistration.unregisterCommand("unban");
        CommandRegistration.unregisterCommand("essentialsc:unban");
        CommandRegistration.unregisterCommand("unban-ip");
        CommandRegistration.unregisterCommand("essentialsc:unban-ip");
        CommandRegistration.unregisterCommand("banlist");
        CommandRegistration.unregisterCommand("essentialsc:banlist");
        CommandRegistration.unregisterCommand("mute");
        CommandRegistration.unregisterCommand("essentialsc:mute");
        CommandRegistration.unregisterCommand("unmute");
        CommandRegistration.unregisterCommand("essentialsc:unmute");
        CommandRegistration.unregisterCommand("checkpunish");
        CommandRegistration.unregisterCommand("essentialsc:checkpunish");

        plugin.debug("Unregistered all Punishment Commands");


        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Ban") || name.contains("Mute") || name.contains("Punish")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Punishment listener: " + name);
                    }
                }
            }
        }

        if (plugin.getPunishmentManager() != null) {
            plugin.getPunishmentManager().shutdown();
            plugin.setPunishmentManager(null);
        }

        plugin.debug("Punishments fully unloaded");
    }

    private void unloadTPA() {
        plugin.debug("TPA is disabled in the config, unloading");

        CommandRegistration.unregisterCommand("tpa");
        CommandRegistration.unregisterCommand("essentialsc:tpa");
        CommandRegistration.unregisterCommand("tpahere");
        CommandRegistration.unregisterCommand("essentialsc:tpahere");
        CommandRegistration.unregisterCommand("tpaccept");
        CommandRegistration.unregisterCommand("essentialsc:tpaccept");
        CommandRegistration.unregisterCommand("tpdeny");
        CommandRegistration.unregisterCommand("essentialsc:tpdeny");
        CommandRegistration.unregisterCommand("tpcancel");
        CommandRegistration.unregisterCommand("essentialsc:tpcancel");
        CommandRegistration.unregisterCommand("tpaignore");
        CommandRegistration.unregisterCommand("essentialsc:tpaignore");
        CommandRegistration.unregisterCommand("tpatoggle");
        CommandRegistration.unregisterCommand("essentialsc:tpatoggle");
        CommandRegistration.unregisterCommand("tpaqueue");
        CommandRegistration.unregisterCommand("essentialsc:tpaqueue");

        plugin.debug("Unregistered all TPA Commands");

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("TPA")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered TPA listener: " + name);
                    }
                }
            }
        }

        if (plugin.getTPAManager() != null) {
            plugin.getTPAManager().shutdown();
            plugin.setTpaManager(null);
        }

        plugin.debug("TPA fully unloaded");
    }

    private void unloadHomes() {
        plugin.debug("Homes are disabled in config, fully unloading");

        CommandRegistration.unregisterCommand("home");
        CommandRegistration.unregisterCommand("essentialsc:home");
        CommandRegistration.unregisterCommand("sethome");
        CommandRegistration.unregisterCommand("essentialsc:sethome");
        CommandRegistration.unregisterCommand("delhome");
        CommandRegistration.unregisterCommand("essentialsc:delhome");
        CommandRegistration.unregisterCommand("homes");
        CommandRegistration.unregisterCommand("essentialsc:homes");
        plugin.debug("Unregistered Home Commands");

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Home") || name.contains("TeleportHandler")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Home listener: " + name);
                    }
                }
            }
        }

        if (plugin.getHomeManager() != null) {
            plugin.getHomeManager().shutdown();
            plugin.setHomeManager(null);
        }

        plugin.setHomeNotificationManager(null);

        plugin.debug("Homes fully unloaded ");
    }

    private void unloadSpawn() {
        plugin.debug("Spawn is disabled in the config, unloading...");

        CommandRegistration.unregisterCommand("spawn");
        CommandRegistration.unregisterCommand("essentialsc:spawn");
        CommandRegistration.unregisterCommand("setspawn");
        CommandRegistration.unregisterCommand("essentialsc:setspawn");

        plugin.debug("Unregistered Spawn");

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();
                    if (name.contains("Spawn")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Spawn listener: " + name);
                    }
                }
            }
        }



        if (plugin.getSpawnManager() != null) {
            plugin.getSpawnManager().shutdown();
            plugin.setSpawnManager(null);
        }

        plugin.debug("Spawn fully unloaded");
    }

    private void unloadKits() {
        plugin.debug("Kits are disabled in config,fully unloading");

        CommandRegistration.unregisterCommand("kit");
        CommandRegistration.unregisterCommand("essentialsc:kit");
        CommandRegistration.unregisterCommand("kits");
        CommandRegistration.unregisterCommand("essentialsc:kits");
        plugin.debug("Unregistered Kit Commands");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory() != null
                    && player.getOpenInventory().getTopInventory().getHolder() instanceof KitGuiHolder) {
                player.closeInventory();
            }
        }

        if (plugin.getKitGuiManager() != null) {
            plugin.getKitGuiManager().shutdown();
            plugin.setKitGuiManager(null);
        }


        if (plugin.getKitManager() != null) {
            HandlerList.unregisterAll(plugin.getKitManager());
            plugin.getKitManager().shutdown();
            plugin.setKitManager(null);
        }

        plugin.debug("Kits fully unloaded");
    }

    private void unloadShop() {
        plugin.debug("Shop system is disabled in config - umloading");

        CommandRegistration.unregisterCommand("shop");
        CommandRegistration.unregisterCommand("essentialsc:shop");

        plugin.debug("Shop commands unregistered");

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getOpenInventory().getTopInventory() != null
                    && player.getOpenInventory().getTopInventory().getHolder() instanceof ShopHolder) {
                player.closeInventory();
            }
        }

        for (HandlerList handlerList : HandlerList.getHandlerLists()) {
            for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
                if (rl.getPlugin().equals(plugin)) {
                    String name = rl.getListener().getClass().getSimpleName();

                    if (name.contains("Shop")) {
                        handlerList.unregister(rl);
                        plugin.debug("Unregistered Shop listener: " + name);
                    }
                    
                }
            }
        }

        if (plugin.getShopManager() != null) {
            plugin.getShopManager().shutdown();
            plugin.setShopManager(null);
        }

        plugin.debug("Shop system fully unloaded.");
    }

    private void registerPlaceholderAPI() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.debug("PlaceholderAPI not found, skipping placeholder registration.");
            return;
        }

        plugin.getServer().getGlobalRegionScheduler().execute(plugin, () -> {
            PlaceholderHook placeholderHook = new PlaceholderHook(plugin);
            if (placeholderHook.register()) {
                plugin.debug("PlaceholderAPI hook registered successfully.");
            } else {
                plugin.getLogger().warning("Failed to register PlaceholderAPI hook.");
            }
        });
    }

    private void saveResourceIfAbsent(String resourcePath) {
        java.io.File target = new java.io.File(plugin.getDataFolder(), resourcePath);
        if (!target.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }
}