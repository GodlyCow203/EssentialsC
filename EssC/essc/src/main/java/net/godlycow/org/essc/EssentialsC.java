package net.godlycow.org.essc;

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
import net.godlycow.org.essc.command.item.HatCommand;
import net.godlycow.org.essc.command.player.RenameCommand;
import net.godlycow.org.essc.config.CommandsConfig;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.data.LogoutDataManager;
import net.godlycow.org.essc.discord.DiscordSRVHook;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.faststats.FastStatsManager;
import net.godlycow.org.essc.fly.FlyManager;
import net.godlycow.org.essc.gui.GuiFramework;
import net.godlycow.org.essc.home.HomeManager;
import net.godlycow.org.essc.home.HomeNotificationManager;
import net.godlycow.org.essc.home.gui.GuiManager;
import net.godlycow.org.essc.ignore.IgnoreManager;
import net.godlycow.org.essc.kit.KitManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.listener.AhListener;
import net.godlycow.org.essc.listener.BanListener;
import net.godlycow.org.essc.listener.JoinLeaveListener;
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
import net.godlycow.org.essc.shop.ShopListener;
import net.godlycow.org.essc.shop.ShopManager;
import net.godlycow.org.essc.shop.sell.SellListener;
import net.godlycow.org.essc.shop.sell.SellManager;
import net.godlycow.org.essc.softwares.EssScheduler;
import net.godlycow.org.essc.softwares.ServerSoftware;
import net.godlycow.org.essc.spawn.SpawnManager;
import net.godlycow.org.essc.tab.TabManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.util.EssLog;
import net.godlycow.org.essc.util.StartupBanner;
import net.godlycow.org.essc.vanish.VanishManager;
import net.godlycow.org.essc.warp.WarpManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsC extends JavaPlugin {

    private static EssentialsC instance;

    private EssScheduler essScheduler;
    private ConfigManager configManager;
    private LanguageManager languageManager;
    private net.godlycow.org.essc.language.HelpManager helpManager;

    private EconomyManager economyManager;
    private VaultHook vaultHook;

    private TPAManager tpaManager;
    private HomeManager homeManager;
    private HomeNotificationManager homeNotificationManager;
    private GuiManager homeGuiManager;
    private SpawnManager spawnManager;
    private BackManager backManager;
    private KitManager kitManager;
    private VanishManager vanishManager;
    private ScoreboardManager scoreboardManager;
    private EconomyRegistrar economyRegistrar;
    private JoinLeaveListener joinLeaveListener;
    private RenameCommand renameCommand;
    private ShopManager shopManager;
    private NickManager nickManager;
    private HatCommand hatCommand;
    private PunishmentManager punishmentManager;
    private IgnoreManager ignoreManager;
    private ReplyManager replyManager;
    private AuctionManager auctionManager;
    private WarpManager warpManager;
    private AFKManager afkManager;
    private PlaceholderHook placeholderHook;
    private FastStatsManager fastStats;
    private ChatManager chatManager;
    private DiscordSRVHook discordSRVHook;
    private RTPManager rtpManager;
    private RTPGuiManager rtpGuiManager;
    private TabManager tabManager;
    private FlyManager flyManager;
    private FloodgateHook floodgateHook;
    private BedrockUtil bedrockUtil;
    private RulesManager rulesManager;
    private CommandsConfig commandsConfig;
    private ScheduleManager scheduleManager;
    private MOTDManager motdManager;
    private BackupManager backupManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private LogoutDataManager logoutDataManager;
    private SellManager sellManager;
    private SellListener sellListener;
    private AhGuiManager ahGuiManager;
    private EssentialsCAPIImpl apiImplementation;

    @Override
    public void onLoad() {
        instance = this;
        essScheduler = new EssScheduler(this);
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        commandsConfig = new CommandsConfig(this);
        commandsConfig.load();
        configManager.migrate();
        EssLog.init(getLogger(), configManager.isDebug());


        if (configManager.isEconomyEnabled()) {
            debug("Economy is enabled, initializing EconomyManager...");
            economyManager = new EconomyManager(this);
            vaultHook = new VaultHook(economyManager);
            if (vaultHook.hook()) {
                getLogger().info("Successfully hooked into Vault!");
            } else {
                getLogger().warning("Vault not found, skipping Vault economy registration.");
            }
        } else {
            debug("Economy is disabled, skipping initialization.");
        }
    }

    @Override
    public void onEnable() {
        StartupBanner.print(this, getLogger());

        saveResource("lang/en_US.json", false);
        saveResource("lang/de_DE.json", false);

        languageManager = new LanguageManager(this);
        languageManager.load(configManager.getDefaultLanguage());

        helpManager = new net.godlycow.org.essc.language.HelpManager(this);
        helpManager.load(configManager.getDefaultLanguage());
        scheduleManager = new ScheduleManager(this);
        scheduleManager.load();

        if (configManager.isBackupEnabled()) {
            backupManager = new BackupManager(this);
        }

        if (configManager.isMotdEnabled()) {
            motdManager = new MOTDManager(this);
        }

        tpaManager = new TPAManager(this);
        homeManager = new HomeManager(this);
        homeNotificationManager = new HomeNotificationManager(this);
        homeGuiManager = new GuiManager(this);
        spawnManager = new SpawnManager(this);
        backManager = new BackManager(this);
        kitManager = new KitManager(this);

        apiImplementation = new EssentialsCAPIImpl(this);
        APIProvider.register(apiImplementation);

        vanishManager = new VanishManager(this);
        punishmentManager = new PunishmentManager(this);
        ignoreManager = new IgnoreManager(this);
        replyManager = new ReplyManager();
        chatManager = new ChatManager(this);
        logoutDataManager = new LogoutDataManager(this);


        int pluginId = 29401;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("bStats Metrics initialized successfully!");

        if (configManager.isEconomyEnabled()) {
            EconomyCharts.register(this, metrics);
        }

        registerPlaceholderAPI();

        fastStats = new FastStatsManager();
        fastStats.init(this);

        floodgateHook = new FloodgateHook(this);
        bedrockUtil = new BedrockUtil(this, floodgateHook);

        new FirstRunHandler(this);

        rulesManager = new RulesManager(this);
        rulesManager.load();


        flyManager = new FlyManager(this);

        if (configManager.isScoreboardEnabled()) {
            if (ServerSoftware.isFolia()) {
                getLogger().warning("Scoreboard feature is not *yet* supported by EssentialsC");
            } else {
                scoreboardManager = new ScoreboardManager(this);
            }
        }

        if (getConfigManager().isLuckPermsTabEnabled() || configManager.isNickEnabled()) {
            this.tabManager = new TabManager(this);
        }

        if (configManager.isNickEnabled()) {
            nickManager = new NickManager(this);
        }

        if (configManager.isRTPEnabled()) {
            rtpManager = new RTPManager(this);
            rtpGuiManager = new RTPGuiManager(this, rtpManager);
        }

        new ListenerRegistrar(this);

        if (configManager.isEconomyEnabled()) {
            debug("economyManager was null in onEnable, running EconomyRegistrar fallback.");
            new EconomyRegistrar(this).enable();
        }

        if (economyManager != null) {
            getServer().getPluginManager().registerEvents(economyManager, this);
            debug("EconomyManager event listener registered.");
        }

        if (configManager.isShopEnabled()) {
            shopManager = new ShopManager(this);
            ShopListener shopListener = new ShopListener(this, shopManager);
            shopManager.setShopListener(shopListener);
            getServer().getPluginManager().registerEvents(shopListener, this);
        }

        if (configManager.isAHEnabled()) {
            GuiFramework guiFramework = new GuiFramework(this);
            guiFramework.loadTemplates();

            auctionManager = new AuctionManager(this);
            ahGuiManager = new AhGuiManager(this, guiFramework, new AhSoundManager(this));
            AhCommand ahCommand = new AhCommand(this, ahGuiManager);
            new AhListener(this, ahCommand);
        }

        if (configManager.isWarpEnabled()) {
            warpManager = new WarpManager(this);
            getServer().getPluginManager().registerEvents(new net.godlycow.org.essc.listener.WarpListener(this), this);
        }

        if (configManager.isAfkEnabled()) {
            afkManager = new AFKManager(this);
        }

        if (configManager.isDiscordSRVEnabled()) {
            discordSRVHook = new DiscordSRVHook(this);
            discordSRVHook.init();
        }

        if (configManager.isSellEnabled()) {
            sellListener = new SellListener(this);
            sellManager = new SellManager(this, sellListener);
            sellListener.setSellManager(sellManager);
            getServer().getPluginManager().registerEvents(sellListener, this);
        }

        new CommandRegistrar(this).registerAll();

        getServer().getPluginManager().registerEvents(new BanListener(this, punishmentManager), this);


        getLogger().info("EssentialsC enabled");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.shutdown();
        }
        if (afkManager != null) {
            afkManager.shutdown();
        }
        if (homeManager != null) {
            homeManager.shutdown();
        }
        if (auctionManager != null) {
            auctionManager.shutdown();
        }
        if (shopManager != null) {
            shopManager.shutdown();
        }
        if (kitManager != null) {
            kitManager.shutdown();
        }
        if (nickManager != null) {
            nickManager.shutdown();
        }
        if (scoreboardManager != null) {
            scoreboardManager.shutdown();
        }

        if (backManager != null) {
            backManager.shutdown();
        }
        if (discordSRVHook != null) {
            discordSRVHook.shutdown();
        }
        if (rtpManager != null) {
            rtpManager.shutdown();
        }
        if (rtpGuiManager != null) {
            rtpGuiManager.shutdown();
        }

        if (scheduleManager != null) {
            scheduleManager.shutdown();
        }

        if (configManager.isBackupOnShutdown()) {
            getLogger().info("[Backup] Creating shutdown backup...");
            try {
                backupManager.createAsync(
                        name -> getLogger().info("[Backup] Shutdown backup created: " + name),
                        err  -> getLogger().warning("[Backup] Shutdown backup failed: " + err)
                );
            } catch (Exception e) {
                getLogger().warning("[Backup] Shutdown backup error: " + e.getMessage());
            }
        }

        if (apiImplementation != null) {
            APIProvider.unregister();
        }

        getLogger().info("EssentialsC disabled");
    }

    private void registerPlaceholderAPI() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().info("PlaceholderAPI not found, skipping placeholder registration.");
            return;
        }

        placeholderHook = new PlaceholderHook(this);

        if (placeholderHook.register()) {
            getLogger().info("PlaceholderAPI hook registered successfully!");
        } else {
            getLogger().warning("Failed to register PlaceholderAPI hook!");
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getHomeManager().getHomes(player.getUniqueId());
        if (homeNotificationManager != null) {
            homeNotificationManager.deliverPending(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        getHomeManager().clearCache(event.getPlayer().getUniqueId());
    }

    public void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public static EssentialsC getInstance() {
        return instance;
    }

    public EssScheduler getEssScheduler() {
        return essScheduler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public net.godlycow.org.essc.language.HelpManager getHelpManager() {
        return helpManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public void setEconomyManager(EconomyManager economyManager) {
        this.economyManager = economyManager;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public void setVaultHook(VaultHook vaultHook) {
        this.vaultHook = vaultHook;
    }

    public boolean isVaultHooked() {
        return vaultHook != null && vaultHook.isHooked();
    }

    public MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public TPAManager getTPAManager() {
        return tpaManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public HomeNotificationManager getHomeNotificationManager() {
        return homeNotificationManager;
    }

    public GuiManager getHomeGuiManager() {
        return homeGuiManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public BackManager getBackManager() {
        return backManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public EconomyRegistrar getEconomyRegistrar() {
        return economyRegistrar;
    }

    public JoinLeaveListener getJoinLeaveListener() {
        return joinLeaveListener;
    }

    public RenameCommand getRenameCommand() {
        return renameCommand;
    }

    public NickManager getNickManager() {
        return nickManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public HatCommand getHatCommand() {
        return hatCommand;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public ReplyManager getReplyManager() {
        return replyManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public AFKManager getAfkManager() {
        return afkManager;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public DiscordSRVHook getDiscordSRVHook() {
        return discordSRVHook;
    }

    public RTPManager getRtpManager() {
        return rtpManager;
    }

    public RTPGuiManager getRtpGuiManager() {
        return rtpGuiManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public BedrockUtil getBedrockUtil() {
        return bedrockUtil;
    }

    public RulesManager getRulesManager() {
        return rulesManager;
    }

    public CommandsConfig getCommandsConfig() {
        return commandsConfig;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public MOTDManager getMotdManager(){
        return motdManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public LogoutDataManager getLogoutDataManager() {
        return logoutDataManager;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public AhGuiManager getAhGuiManager() {
        return ahGuiManager;
    }

}