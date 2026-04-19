package net.godlycow.org.essc;

import net.godlycow.org.essc.afk.*;
import net.godlycow.org.essc.api.*;
import net.godlycow.org.essc.api.impl.*;
import net.godlycow.org.essc.auction.*;
import net.godlycow.org.essc.auction.gui.*;
import net.godlycow.org.essc.back.*;
import net.godlycow.org.essc.backup.*;
import net.godlycow.org.essc.bedrock.*;
import net.godlycow.org.essc.bootstrap.*;
import net.godlycow.org.essc.bstats.*;
import net.godlycow.org.essc.chat.*;
import net.godlycow.org.essc.command.auction.*;
import net.godlycow.org.essc.command.item.*;
import net.godlycow.org.essc.command.player.*;
import net.godlycow.org.essc.config.*;
import net.godlycow.org.essc.data.*;
import net.godlycow.org.essc.discord.*;
import net.godlycow.org.essc.economy.*;
import net.godlycow.org.essc.faststats.*;
import net.godlycow.org.essc.fly.*;
import net.godlycow.org.essc.home.*;
import net.godlycow.org.essc.home.gui.*;
import net.godlycow.org.essc.ignore.*;
import net.godlycow.org.essc.kit.*;
import net.godlycow.org.essc.language.*;
import net.godlycow.org.essc.listener.*;
import net.godlycow.org.essc.motd.*;
import net.godlycow.org.essc.msg.*;
import net.godlycow.org.essc.nick.*;
import net.godlycow.org.essc.placeholderapi.*;
import net.godlycow.org.essc.punishment.*;
import net.godlycow.org.essc.rtp.*;
import net.godlycow.org.essc.rules.*;
import net.godlycow.org.essc.schedule.*;
import net.godlycow.org.essc.scoreboard.*;
import net.godlycow.org.essc.setup.*;
import net.godlycow.org.essc.shop.*;
import net.godlycow.org.essc.shop.sell.*;
import net.godlycow.org.essc.spawn.*;
import net.godlycow.org.essc.tab.*;
import net.godlycow.org.essc.teleport.*;
import net.godlycow.org.essc.util.*;
import net.godlycow.org.essc.softwares.*;
import net.godlycow.org.essc.vanish.*;
import net.godlycow.org.essc.warp.*;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsC extends JavaPlugin {

    private static EssentialsC instance;

    private EssScheduler essScheduler;
    private ConfigManager configManager;
    private LanguageManager languageManager;

    private EconomyManager economyManager;
    private VaultHook vaultHook;

    private TPAManager tpaManager;
    private HomeManager homeManager;
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
    private EssentialsCAPIImpl apiImpl;
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
        homeGuiManager = new GuiManager(this);
        spawnManager = new SpawnManager(this);
        backManager = new BackManager(this);
        kitManager = new KitManager(this);
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
        getLogger().info("bStats Metrics initialized successfully!");

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
                getLogger().warning("Scoreboard feature is not supported on Folia (getNewScoreboard() is unavailable). Disabling scoreboard.");
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
        new CommandRegistrar(this).registerAll();

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
            auctionManager = new AuctionManager(this);
            ahGuiManager = new AhGuiManager(this, new AhSoundManager(this));
            AhCommand ahCommand = new AhCommand(this);
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
        getServer().getPluginManager().registerEvents(new BanListener(this, punishmentManager), this);

        apiImpl = new EssentialsCAPIImpl(this);
        apiImpl.enable();

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
        if (apiImpl != null) {
            apiImpl.disable();
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

    public EssentialsCAPI getAPI() {
        return apiImpl;
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

    public TPAManager getTpaManager() {
        return tpaManager;
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