package net.godlycow.org.essc;

import net.godlycow.org.essc.back.BackManager;
import net.godlycow.org.essc.bootstrap.CommandRegistrar;
import net.godlycow.org.essc.bootstrap.EconomyRegistrar;
import net.godlycow.org.essc.bootstrap.ListenerRegistrar;
import net.godlycow.org.essc.command.item.HatCommand;
import net.godlycow.org.essc.command.player.RenameCommand;
import net.godlycow.org.essc.command.player.ShopCommand;
import net.godlycow.org.essc.config.ConfigManager;
import net.godlycow.org.essc.economy.EconomyManager;
import net.godlycow.org.essc.economy.VaultHook;
import net.godlycow.org.essc.fly.FlyManager;
import net.godlycow.org.essc.home.HomeManager;
import net.godlycow.org.essc.ignore.IgnoreManager;
import net.godlycow.org.essc.kit.KitManager;
import net.godlycow.org.essc.language.LanguageManager;
import net.godlycow.org.essc.listener.JoinLeaveListener;
import net.godlycow.org.essc.msg.ReplyManager;
import net.godlycow.org.essc.nick.NickManager;
import net.godlycow.org.essc.punishment.PunishmentManager;
import net.godlycow.org.essc.scoreboard.ScoreboardManager;
import net.godlycow.org.essc.shop.ShopListener;
import net.godlycow.org.essc.shop.ShopManager;
import net.godlycow.org.essc.spawn.SpawnManager;
import net.godlycow.org.essc.teleport.TPAManager;
import net.godlycow.org.essc.vanish.VanishManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
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


        new FlyManager(this);

        if (configManager.isScoreboardEnabled()) {
            scoreboardManager = new ScoreboardManager(this);
        }

        if (configManager.isNickEnabled()) {
            nickManager = new NickManager(this);
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


        getLogger().info("EssentialsC enabled");
    }

    @Override
    public void onDisable() {
        if (economyManager != null) {
            economyManager.shutdown();
        }

        getLogger().info("EssentialsC disabled");
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
}
