package net.godlycow.org.essc.plugin;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.afk.AFKManager;
import net.godlycow.org.essc.api.APIProvider;
import net.godlycow.org.essc.api.impl.EssentialsCAPIImpl;
import net.godlycow.org.essc.auction.AhSoundManager;
import net.godlycow.org.essc.auction.AuctionManager;
import net.godlycow.org.essc.auction.gui.AhGuiManager;
import net.godlycow.org.essc.back.BackManager;
import net.godlycow.org.essc.backup.BackupManager;
import net.godlycow.org.essc.bedrock.BedrockUtil;
import net.godlycow.org.essc.bedrock.FloodgateHook;
import net.godlycow.org.essc.bootstrap.CommandRegistrar;
import net.godlycow.org.essc.bootstrap.EconomyRegistrar;
import net.godlycow.org.essc.bootstrap.ListenerRegistrar;
import net.godlycow.org.essc.bstats.EconomyCharts;
import net.godlycow.org.essc.chat.ChatManager;
import net.godlycow.org.essc.command.auction.AhCommand;
import net.godlycow.org.essc.data.LogoutDataManager;
import net.godlycow.org.essc.discord.DiscordSRVHook;
import net.godlycow.org.essc.faststats.FastStatsManager;
import net.godlycow.org.essc.fly.FlyManager;
import net.godlycow.org.essc.gui.GuiFramework;
import net.godlycow.org.essc.home.HomeManager;
import net.godlycow.org.essc.home.HomeNotificationManager;
import net.godlycow.org.essc.home.gui.GuiManager;
import net.godlycow.org.essc.user.UserManager;
import net.godlycow.org.essc.ignore.IgnoreManager;
import net.godlycow.org.essc.kit.KitManager;
import net.godlycow.org.essc.language.HelpManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.listener.AhListener;
import net.godlycow.org.essc.listener.BanListener;
import net.godlycow.org.essc.listener.WarpListener;
import net.godlycow.org.essc.motd.MOTDManager;
import net.godlycow.org.essc.msg.ReplyManager;
import net.godlycow.org.essc.nick.NickManager;
import net.godlycow.org.essc.placeholderapi.PlaceholderHook;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.rtp.RTPGuiManager;
import net.godlycow.org.essc.rtp.RTPManager;
import net.godlycow.org.essc.rules.RulesManager;
import net.godlycow.org.essc.schedule.ScheduleManager;
import net.godlycow.org.essc.scoreboard.ScoreboardManager;
import net.godlycow.org.essc.setup.FirstRunHandler;
import net.godlycow.org.essc.shop.ShopGuiManager;
import net.godlycow.org.essc.shop.ShopListener;
import net.godlycow.org.essc.shop.ShopManager;
import net.godlycow.org.essc.shop.ShopSoundManager;
import net.godlycow.org.essc.shop.sell.SellListener;
import net.godlycow.org.essc.shop.sell.SellManager;
import net.godlycow.org.essc.softwares.ServerSoftware;
import net.godlycow.org.essc.spawn.SpawnManager;
import net.godlycow.org.essc.tab.TabManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.util.StartupBanner;
import net.godlycow.org.essc.vanish.VanishManager;
import net.godlycow.org.essc.warp.WarpManager;
import org.bstats.bukkit.Metrics;

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
            new CrashHandler(plugin).handle(ex);
        }
    }

    private void load() {
        timer.start();
        StartupBanner.print(plugin, plugin.getLogger());
        timer.mark("banner");
        loadLanguages();
        timer.mark("languages");
        loadScheduler();
        timer.mark("scheduler");
        registerAPI();
        timer.mark("api");
        startPlugin();
        timer.mark("plugin");
        startMetrics();
        timer.mark("metrics");
        registerPlaceholderAPI();
        timer.mark("placeholderapi");
        String timings = timer.finish();
        plugin.getLogger().info("EssentialsC enabled — " + timings);
    }

    private void loadLanguages() {
        plugin.saveResource("lang/en_US.json", false);
        plugin.saveResource("lang/de_DE.json", false);

        LanguageManager languageManager = new LanguageManager(plugin);
        languageManager.load(plugin.getConfigManager().getDefaultLanguage());
        plugin.setLanguageManager(languageManager);

        HelpManager helpManager = new HelpManager(plugin);
        helpManager.load(plugin.getConfigManager().getDefaultLanguage());
        plugin.setHelpManager(helpManager);
    }

    private void loadScheduler() {
        ScheduleManager scheduleManager = new ScheduleManager(plugin);
        scheduleManager.load();
        plugin.setScheduleManager(scheduleManager);
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

        plugin.setTpaManager(new TPAManager(plugin));
        plugin.setHomeManager(new HomeManager(plugin));
        plugin.setHomeNotificationManager(new HomeNotificationManager(plugin));
        plugin.setHomeGuiManager(new GuiManager(plugin));
        plugin.setSpawnManager(new SpawnManager(plugin));
        plugin.setBackManager(new BackManager(plugin));
        plugin.setKitManager(new KitManager(plugin));
        plugin.setVanishManager(new VanishManager(plugin));
        plugin.setPunishmentManager(new PunishmentManager(plugin));
        plugin.setIgnoreManager(new IgnoreManager(plugin));
        plugin.setReplyManager(new ReplyManager());
        plugin.setChatManager(new ChatManager(plugin));
        plugin.setLogoutDataManager(new LogoutDataManager(plugin));
        plugin.setUserManager(new UserManager(plugin));
        plugin.setFlyManager(new FlyManager(plugin));

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
        if (plugin.getConfigManager().isAHEnabled() || plugin.getConfigManager().isShopEnabled()) {
            guiFramework = new GuiFramework(plugin);
            guiFramework.loadTemplates();
        }

        if (plugin.getConfigManager().isAHEnabled()) {
            AuctionManager auctionManager = new AuctionManager(plugin);
            plugin.setAuctionManager(auctionManager);

            AhGuiManager ahGuiManager = new AhGuiManager(plugin, guiFramework, new AhSoundManager(plugin));
            plugin.setAhGuiManager(ahGuiManager);

            new AhListener(plugin, new AhCommand(plugin, ahGuiManager));
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
        plugin.getServer().getPluginManager().registerEvents(new BanListener(plugin, plugin.getPunishmentManager()), plugin);
        plugin.getServer().getPluginManager().registerEvents(plugin, plugin);

        new CommandRegistrar(plugin).registerAll();
    }

    private void startMetrics() {
        int pluginId = 29401;
        Metrics metrics = new Metrics(plugin, pluginId);
        plugin.getLogger().info("bStats Metrics initialized.");

        if (plugin.getConfigManager().isEconomyEnabled()) {
            EconomyCharts.register(plugin, metrics);
        }

        FastStatsManager fastStats = new FastStatsManager();
        fastStats.init(plugin);
    }

    private void registerPlaceholderAPI() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            plugin.getLogger().info("PlaceholderAPI not found, skipping placeholder registration.");
            return;
        }

        PlaceholderHook placeholderHook = new PlaceholderHook(plugin);
        if (placeholderHook.register()) {
            plugin.getLogger().info("PlaceholderAPI hook registered successfully.");
        } else {
            plugin.getLogger().warning("Failed to register PlaceholderAPI hook.");
        }
    }
}