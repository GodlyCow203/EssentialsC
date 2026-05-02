package net.godlycow.org.essc;

import net.godlycow.org.essc.afk.AFKManager;
import net.godlycow.org.essc.api.impl.EssentialsCAPIImpl;
import net.godlycow.org.essc.auction.AuctionManager;
import net.godlycow.org.essc.auction.gui.AhGuiManager;
import net.godlycow.org.essc.back.BackManager;
import net.godlycow.org.essc.backup.BackupManager;
import net.godlycow.org.essc.bedrock.BedrockUtil;
import net.godlycow.org.essc.bootstrap.EconomyRegistrar;
import net.godlycow.org.essc.chat.ChatManager;
import net.godlycow.org.essc.command.item.HatCommand;
import net.godlycow.org.essc.command.player.RenameCommand;
import net.godlycow.org.essc.config.CommandsConfig;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.data.LogoutDataManager;
import net.godlycow.org.essc.discord.DiscordSRVHook;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.fly.FlyManager;
import net.godlycow.org.essc.home.HomeManager;
import net.godlycow.org.essc.home.HomeNotificationManager;
import net.godlycow.org.essc.home.gui.GuiManager;
import net.godlycow.org.essc.ignore.IgnoreManager;
import net.godlycow.org.essc.kit.KitManager;
import net.godlycow.org.essc.language.HelpManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.listener.JoinLeaveListener;
import net.godlycow.org.essc.motd.MOTDManager;
import net.godlycow.org.essc.msg.ReplyManager;
import net.godlycow.org.essc.nick.NickManager;
import net.godlycow.org.essc.plugin.PluginLoader;
import net.godlycow.org.essc.plugin.PluginShutdown;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.rtp.RTPGuiManager;
import net.godlycow.org.essc.rtp.RTPManager;
import net.godlycow.org.essc.rules.RulesManager;
import net.godlycow.org.essc.schedule.ScheduleManager;
import net.godlycow.org.essc.scoreboard.ScoreboardManager;
import net.godlycow.org.essc.shop.ShopManager;
import net.godlycow.org.essc.shop.sell.SellManager;
import net.godlycow.org.essc.softwares.EssScheduler;
import net.godlycow.org.essc.spawn.SpawnManager;
import net.godlycow.org.essc.tab.TabManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.util.EssLog;
import net.godlycow.org.essc.vanish.VanishManager;
import net.godlycow.org.essc.warp.WarpManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsC extends JavaPlugin implements Listener {

    private static EssentialsC instance;

    private EssScheduler essScheduler;
    private ConfigManager configManager;
    private CommandsConfig commandsConfig;
    private LanguageManager languageManager;
    private HelpManager helpManager;
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
    private ShopManager shopManager;
    private NickManager nickManager;
    private PunishmentManager punishmentManager;
    private IgnoreManager ignoreManager;
    private ReplyManager replyManager;
    private AuctionManager auctionManager;
    private WarpManager warpManager;
    private AFKManager afkManager;
    private ChatManager chatManager;
    private DiscordSRVHook discordSRVHook;
    private RTPManager rtpManager;
    private RTPGuiManager rtpGuiManager;
    private TabManager tabManager;
    private FlyManager flyManager;
    private BedrockUtil bedrockUtil;
    private RulesManager rulesManager;
    private ScheduleManager scheduleManager;
    private MOTDManager motdManager;
    private BackupManager backupManager;
    private LogoutDataManager logoutDataManager;
    private SellManager sellManager;
    private AhGuiManager ahGuiManager;
    private EssentialsCAPIImpl apiImplementation;
    private HatCommand hatCommand;
    public JoinLeaveListener joinLeaveListener;;
    private RenameCommand renameCommand;

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public void onLoad() {
        instance = this;
        essScheduler = new EssScheduler(this);
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.migrate();

        commandsConfig = new CommandsConfig(this);
        commandsConfig.load();

        EssLog.init(getLogger(), configManager.isDebug());

        if (configManager.isEconomyEnabled()) {
            debug("Economy is enabled, initializing EconomyManager...");
            economyManager = new EconomyManager(this);
            vaultHook = new VaultHook(economyManager);
            if (vaultHook.hook()) {
                getLogger().info("Successfully hooked into Vault.");
            } else {
                getLogger().warning("Vault not found, skipping Vault economy registration.");
            }
        } else {
            debug("Economy is disabled, skipping initialization.");
        }
    }

    @Override
    public void onEnable() {
        new PluginLoader(this).start();
    }

    @Override
    public void onDisable() {
        new PluginShutdown(this).stop();
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

    public CommandsConfig getCommandsConfig() {
        return commandsConfig;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public void setLanguageManager(LanguageManager languageManager) {
        this.languageManager = languageManager;
    }

    public HelpManager getHelpManager() {
        return helpManager;
    }

    public void setHelpManager(HelpManager helpManager) {
        this.helpManager = helpManager;
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

    public void setTpaManager(TPAManager tpaManager) {
        this.tpaManager = tpaManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public void setHomeManager(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    public HomeNotificationManager getHomeNotificationManager() {
        return homeNotificationManager;
    }

    public void setHomeNotificationManager(HomeNotificationManager homeNotificationManager) {
        this.homeNotificationManager = homeNotificationManager;
    }

    public GuiManager getHomeGuiManager() {
        return homeGuiManager;
    }

    public void setHomeGuiManager(GuiManager homeGuiManager) {
        this.homeGuiManager = homeGuiManager;
    }

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public void setSpawnManager(SpawnManager spawnManager) {
        this.spawnManager = spawnManager;
    }

    public BackManager getBackManager() {
        return backManager;
    }

    public void setBackManager(BackManager backManager) {
        this.backManager = backManager;
    }

    public KitManager getKitManager() {
        return kitManager;
    }

    public void setKitManager(KitManager kitManager) {
        this.kitManager = kitManager;
    }

    public VanishManager getVanishManager() {
        return vanishManager;
    }

    public void setVanishManager(VanishManager vanishManager) {
        this.vanishManager = vanishManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public void setScoreboardManager(ScoreboardManager scoreboardManager) {
        this.scoreboardManager = scoreboardManager;
    }

    public EconomyRegistrar getEconomyRegistrar() {
        return economyRegistrar;
    }

    public NickManager getNickManager() {
        return nickManager;
    }

    public void setNickManager(NickManager nickManager) {
        this.nickManager = nickManager;
    }

    public ShopManager getShopManager() {
        return shopManager;
    }

    public void setShopManager(ShopManager shopManager) {
        this.shopManager = shopManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public void setPunishmentManager(PunishmentManager punishmentManager) {
        this.punishmentManager = punishmentManager;
    }

    public IgnoreManager getIgnoreManager() {
        return ignoreManager;
    }

    public void setIgnoreManager(IgnoreManager ignoreManager) {
        this.ignoreManager = ignoreManager;
    }

    public ReplyManager getReplyManager() {
        return replyManager;
    }

    public void setReplyManager(ReplyManager replyManager) {
        this.replyManager = replyManager;
    }

    public AuctionManager getAuctionManager() {
        return auctionManager;
    }

    public void setAuctionManager(AuctionManager auctionManager) {
        this.auctionManager = auctionManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public void setWarpManager(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    public AFKManager getAfkManager() {
        return afkManager;
    }

    public void setAfkManager(AFKManager afkManager) {
        this.afkManager = afkManager;
    }

    public ChatManager getChatManager() {
        return chatManager;
    }

    public void setChatManager(ChatManager chatManager) {
        this.chatManager = chatManager;
    }

    public DiscordSRVHook getDiscordSRVHook() {
        return discordSRVHook;
    }

    public void setDiscordSRVHook(DiscordSRVHook discordSRVHook) {
        this.discordSRVHook = discordSRVHook;
    }

    public RTPManager getRtpManager() {
        return rtpManager;
    }

    public void setRtpManager(RTPManager rtpManager) {
        this.rtpManager = rtpManager;
    }

    public RTPGuiManager getRtpGuiManager() {
        return rtpGuiManager;
    }

    public void setRtpGuiManager(RTPGuiManager rtpGuiManager) {
        this.rtpGuiManager = rtpGuiManager;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public void setTabManager(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public FlyManager getFlyManager() {
        return flyManager;
    }

    public void setFlyManager(FlyManager flyManager) {
        this.flyManager = flyManager;
    }

    public BedrockUtil getBedrockUtil() {
        return bedrockUtil;
    }

    public void setBedrockUtil(BedrockUtil bedrockUtil) {
        this.bedrockUtil = bedrockUtil;
    }

    public RulesManager getRulesManager() {
        return rulesManager;
    }

    public void setRulesManager(RulesManager rulesManager) {
        this.rulesManager = rulesManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    public void setScheduleManager(ScheduleManager scheduleManager) {
        this.scheduleManager = scheduleManager;
    }

    public MOTDManager getMotdManager() {
        return motdManager;
    }

    public void setMotdManager(MOTDManager motdManager) {
        this.motdManager = motdManager;
    }

    public BackupManager getBackupManager() {
        return backupManager;
    }

    public void setBackupManager(BackupManager backupManager) {
        this.backupManager = backupManager;
    }

    public LogoutDataManager getLogoutDataManager() {
        return logoutDataManager;
    }

    public void setLogoutDataManager(LogoutDataManager logoutDataManager) {
        this.logoutDataManager = logoutDataManager;
    }

    public SellManager getSellManager() {
        return sellManager;
    }

    public void setSellManager(SellManager sellManager) {
        this.sellManager = sellManager;
    }

    public AhGuiManager getAhGuiManager() {
        return ahGuiManager;
    }

    public void setAhGuiManager(AhGuiManager ahGuiManager) {
        this.ahGuiManager = ahGuiManager;
    }

    public EssentialsCAPIImpl getApiImplementation() {
        return apiImplementation;
    }

    public void setApiImplementation(EssentialsCAPIImpl apiImplementation) {
        this.apiImplementation = apiImplementation;
    }

    public  HatCommand getHatCommand(){
        return hatCommand;
    }

    public JoinLeaveListener getJoinLeaveListener(){
        return joinLeaveListener;
    }

    public RenameCommand getRenameCommand(){
        return renameCommand;
    }

}