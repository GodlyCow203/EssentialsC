package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.api.*;
import net.godlycow.org.essc.api.impl.*;
import org.jetbrains.annotations.NotNull;

public class EssentialsCAPIImpl implements EssentialsCAPI {

    private final EssentialsC plugin;

    private final AFKApi afkApi;
    private final AuctionApi auctionApi;
    private final BackApi backApi;
    private final ChatApi chatApi;
    private final DiscordApi discordApi;
    private final EconomyApi economyApi;
    private final FlyApi flyApi;
    private final HomeApi homeApi;
    private final RtpApi rtpApi;
    private final KitApi kitApi;
    private final LanguageApi languageApi;
    private final ReplyApi replyApi;
    private final NickApi nickApi;
    private final PunishmentApi punishmentApi;
    private final RulesApi rulesApi;
    private final SchedulesApi schedulesApi;
    private final ScoreboardApi scoreboardApi;
    private final ShopApi shopApi;
    private final SpawnApi spawnApi;
    private final TabApi tabApi;
    private final TpaApi tpaApi;
    private final VanishApi vanishApi;
    private final WarpApi warpApi;

    public EssentialsCAPIImpl(EssentialsC plugin) {
        this.plugin = plugin;
        this.afkApi        = new AFKApiImpl(plugin.getAfkManager());
        this.auctionApi    = new AuctionApiImpl(plugin.getAuctionManager());
        this.backApi       = new BackApiImpl(plugin.getBackManager());
        this.chatApi       = new ChatApiImpl(plugin.getChatManager());
        this.discordApi    = new DiscordApiImpl(plugin.getDiscordSRVHook());
        this.economyApi    = new EconomyApiImpl(plugin.getEconomyManager(), plugin.getVaultHook());
        this.flyApi        = new FlyApiImpl(plugin.getFlyManager());
        this.homeApi       = new HomeApiImpl(plugin.getHomeManager());
        this.kitApi        = new KitApiImpl(plugin.getKitManager());
        this.languageApi   = new LanguageApiImpl(plugin.getLanguageManager());
        this.replyApi      = new ReplyApiImpl(plugin.getReplyManager());
        this.nickApi       = new NickApiImpl(plugin.getNickManager());
        this.punishmentApi = new PunishmentApiImpl(plugin.getPunishmentManager());
        this.rtpApi        = new RtpApiImpl(plugin.getRtpManager());
        this.rulesApi = new RulesApiImpl(plugin.getRulesManager());
        this.schedulesApi = new SchedulesApiImpl(plugin.getScheduleManager());
        this.scoreboardApi = new ScoreboardApiImpl(plugin.getScoreboardManager());
        this.shopApi = new ShopApiImpl(plugin.getShopManager());
        this.spawnApi = new SpawnApiImpl(plugin.getSpawnManager());
        this.tabApi = new TabApiImpl(plugin.getTabManager());
        this.tpaApi = new TpaApiImpl(plugin.getTpaManager());
        this.vanishApi = new VanishApiImpl(plugin.getVanishManager());
        this.warpApi = new WarpApiImpl(plugin.getWarpManager());
    }

    public void enable() {
        APIProvider.register(this);
        plugin.getLogger().info("EssentialsC API enabled");
    }

    public void disable() {
        APIProvider.unregister();
    }

    @Override public @NotNull AFKApi getAFKApi()           { return afkApi; }
    @Override public @NotNull AuctionApi getAuctionApi()   { return auctionApi; }
    @Override public @NotNull BackApi getBackApi()         { return backApi; }
    @Override public @NotNull ChatApi getChatApi()         { return chatApi; }
    @Override public @NotNull DiscordApi getDiscordApi()   { return discordApi; }
    @Override public @NotNull EconomyApi getEconomyApi()   { return economyApi; }
    @Override public @NotNull FlyApi getFlyApi()           { return flyApi; }
    @Override public @NotNull HomeApi getHomeApi()         { return homeApi; }
    @Override public @NotNull KitApi getKitApi()           { return kitApi; }
    @Override public @NotNull LanguageApi getLanguageApi() { return languageApi; }
    @Override public @NotNull ReplyApi getReplyApi()       { return replyApi; }
    @Override public @NotNull NickApi getNickApi()               { return nickApi; }
    @Override public @NotNull PunishmentApi getPunishmentApi()   { return punishmentApi; }
    @Override public @NotNull RtpApi getRtpApi()           { return  rtpApi;}
    @Override public @NotNull RulesApi getRulesApi()       { return rulesApi; }
    @Override public @NotNull SchedulesApi getSchedulesApi() { return schedulesApi; }
    @Override public @NotNull ScoreboardApi getScoreboardApi() { return scoreboardApi; }
    @Override public @NotNull ShopApi getShopApi() { return shopApi; }
    @Override public @NotNull SpawnApi getSpawnApi() { return spawnApi; }
    @Override public @NotNull TabApi getTabApi() { return tabApi; }
    @Override public @NotNull TpaApi getTpaApi() { return tpaApi; }
    @Override public @NotNull VanishApi getVanishApi() { return vanishApi; }
    @Override public @NotNull WarpApi getWarpApi() { return warpApi; }
}