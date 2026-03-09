package net.godlycow.org.essc;

import net.godlycow.org.essc.afk.AFKManager;
import net.godlycow.org.essc.api.EssentialsCAPI;
import net.godlycow.org.essc.api.impl.EssentialsCAPIImpl;
import net.godlycow.org.essc.auction.AuctionManager;
import net.godlycow.org.essc.back.BackManager;
import net.godlycow.org.essc.bootstrap.CommandRegistrar;
import net.godlycow.org.essc.bootstrap.EconomyRegistrar;
import net.godlycow.org.essc.bootstrap.ListenerRegistrar;
import net.godlycow.org.essc.chat.luckperms.ChatManager;
import net.godlycow.org.essc.command.auction.AhCommand;
import net.godlycow.org.essc.command.item.HatCommand;
import net.godlycow.org.essc.command.player.RTPCommand;
import net.godlycow.org.essc.command.player.RenameCommand;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.discord.DiscordSRVHook;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.faststats.FastStatsManager;
import net.godlycow.org.essc.fly.FlyManager;
import net.godlycow.org.essc.home.HomeManager;
import net.godlycow.org.essc.ignore.IgnoreManager;
import net.godlycow.org.essc.kit.KitManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.listener.AhListener;
import net.godlycow.org.essc.listener.BanListener;
import net.godlycow.org.essc.listener.JoinLeaveListener;
import net.godlycow.org.essc.msg.ReplyManager;
import net.godlycow.org.essc.nick.NickManager;
import net.godlycow.org.essc.placeholderapi.PlaceholderHook;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.rtp.RTPGuiManager;
import net.godlycow.org.essc.rtp.RTPManager;
import net.godlycow.org.essc.scoreboard.ScoreboardManager;
import net.godlycow.org.essc.shop.ShopListener;
import net.godlycow.org.essc.shop.ShopManager;
import net.godlycow.org.essc.spawn.SpawnManager;
import net.godlycow.org.essc.tab.TabManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.vanish.VanishManager;
import net.godlycow.org.essc.warp.WarpManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsC extends JavaPlugin {

    private static EssentialsC instance;

    private ConfigManager configManager;
    private LanguageManager languageManager;

    private EconomyManager economyManager;
    private VaultHook vaultHook;

    private TPAManager tpaManager;
    private HomeManager homeManager;
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






    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        saveResource("lang/en_US.json", false);
        saveResource("lang/de_DE.json", false);

        configManager = new ConfigManager(this);
        languageManager = new LanguageManager(this);
        languageManager.load(configManager.getDefaultLanguage());

        tpaManager = new TPAManager(this);
        homeManager = new HomeManager(this);
        spawnManager = new SpawnManager(this);
        backManager = new BackManager(this);
        kitManager = new KitManager(this);
        vanishManager = new VanishManager(this);
        punishmentManager = new PunishmentManager(this);
        ignoreManager = new IgnoreManager(this);
        replyManager = new ReplyManager();
        chatManager = new ChatManager(this);

        int pluginId = 29401;
        Metrics metrics = new Metrics(this, pluginId);
        getLogger().info("bStats Metrics initialized successfully!");

        registerPlaceholderAPI();

        fastStats = new FastStatsManager();
        fastStats.init(this);



        new FlyManager(this);

        if (configManager.isScoreboardEnabled()) {
            scoreboardManager = new ScoreboardManager(this);
        }

        if (getConfigManager().isLuckPermsTabEnabled()) {
            this.tabManager = new TabManager(this);
        }

        if (configManager.isNickEnabled()) {
            nickManager = new NickManager(this);
        }

        if (configManager.isRTPEnabled()) {
            rtpManager = new RTPManager(this);
            rtpGuiManager = new RTPGuiManager(this, rtpManager);

            if (configManager.isRTPCommandRegistered()) {
                getCommand("rtp").setExecutor(new RTPCommand(this));
                getCommand("rtp").setTabCompleter(new RTPCommand(this));
            } else {
                RTPCommand.unregisterCommand();
            }
        }

        new ListenerRegistrar(this);
        new CommandRegistrar(this).registerAll();

        if (configManager.isEconomyEnabled()) {
            new EconomyRegistrar(this).enable();
        }

        if (configManager.isShopEnabled()) {
            shopManager = new ShopManager(this);
            ShopListener shopListener = new ShopListener(this, shopManager);
            shopManager.setShopListener(shopListener);
            getServer().getPluginManager().registerEvents(shopListener, this);
        }

        if (configManager.isAHEnabled()) {
            auctionManager = new AuctionManager(this);
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
        if (economyManager != null) {
            economyManager.shutdown();
        }
        if (backManager != null) {
            backManager.shutdown();
        }
        if (apiImpl != null) {
            apiImpl.disable();
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

    public void debug(String message) {
        if (configManager != null && configManager.isDebug()) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    public static EssentialsC getInstance() {
        return instance;
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

    public JoinLeaveListener getJoinLeaveListener(){
        return joinLeaveListener;
    }

    public RenameCommand getRenameCommand(){
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

    public WarpManager getWarpManager() { return warpManager;}

    public AFKManager getAfkManager() { return afkManager;}

    public PlaceholderHook getPlaceholderHook() { return placeholderHook;}

    public ChatManager getChatManager() { return chatManager;}

    public EssentialsCAPI getAPI() { return apiImpl;}

    public DiscordSRVHook getDiscordSRVHook() { return discordSRVHook;}

    public RTPManager getRtpManager() {return rtpManager;}

    public RTPGuiManager getRtpGuiManager() {return rtpGuiManager;}

    public TabManager getTabManager() {return tabManager;}

}

