package net.godlycow.org.essc.api;

/**
 * Root API interface for EssentialsC.
 *
 * <p>Provides access to each sub-system API. Retrieve the live instance via
 * {@link APIProvider#getAPI()}.</p>
 *
 * <pre>{@code
 * EssentialsCAPI api = APIProvider.getAPI();
 * if (api != null) {
 *     boolean afk = api.getAFKApi().isAFK(player);
 * }
 * }</pre>
 *
 * @see APIProvider
 */
public interface EssentialsCAPI {

    /**
     * Returns the AFK sub-system API.
     *
     * @return the {@link AFKApi} instance; never {@code null}
     */
    AFKApi getAFKApi();

    /**
     * Returns the Auction House sub-system API.
     *
     * @return the {@link AuctionApi} instance; never {@code null}
     */
    AuctionApi getAuctionApi();

    /**
     * Returns the Back teleport sub-system API.
     *
     * @return the {@link BackApi} instance; never {@code null}
     */
    BackApi getBackApi();

    /**
     * Returns the chat formatting sub-system API.
     *
     * @return the {@link ChatApi} instance; never {@code null}
     */
    ChatApi getChatApi();

    /**
     * Returns the DiscordSRV integration API.
     *
     * @return the {@link DiscordApi} instance; never {@code null}
     */
    DiscordApi getDiscordApi();

    /**
     * Returns the economy sub-system API.
     *
     * @return the {@link EconomyApi} instance; never {@code null}
     */
    EconomyApi getEconomyApi();

    /**
     * Returns the kit sub-system API.
     *
     * @return the {@link KitApi} instance; never {@code null}
     */
    KitApi getKitApi();

    /**
     * Returns the fly sub-system API.
     *
     * @return the {@link FlyApi} instance; never {@code null}
     */
    FlyApi getFlyApi();

    /**
     * Returns the language sub-system API.
     *
     * @return the {@link LanguageApi} instance; never {@code null}
     */
    LanguageApi getLanguageApi();

    /**
     * Returns the private message reply sub-system API.
     *
     * @return the {@link ReplyApi} instance; never {@code null}
     */
    ReplyApi getReplyApi();

    /**
     * Returns the nickname sub-system API.
     *
     * @return the {@link NickApi} instance; never {@code null}
     */
    NickApi getNickApi();

    /**
     * Returns the punishment sub-system API.
     *
     * @return the {@link PunishmentApi} instance; never {@code null}
     */
    PunishmentApi getPunishmentApi();

    /**
     * Returns the rtp sub-system API.
     *
     * @return the {@link RtpApi} instance; never {@code null}
     */
    RtpApi getRtpApi();

    /**
     * Returns the rules sub-system API.
     *
     * @return the {@link RulesApi} instance; never {@code null}
     */
    RulesApi getRulesApi();

    /**
     * Returns the schedules sub-system API.
     *
     * @return the {@link SchedulesApi} instance; never {@code null}
     */
    SchedulesApi getSchedulesApi();

    /**
     * Returns the scoreboard sub-system API.
     *
     * @return the {@link ScoreboardApi} instance; never {@code null}
     */
    ScoreboardApi getScoreboardApi();

    /**
     * Returns the shop sub-system API.
     *
     * @return the {@link ShopApi} instance; never {@code null}
     */
    ShopApi getShopApi();

    /**
     * Returns the spawn sub-system API.
     *
     * @return the {@link SpawnApi} instance; never {@code null}
     */
    SpawnApi getSpawnApi();

    /**
     * Returns the tab list sub-system API.
     *
     * @return the {@link TabApi} instance; never {@code null}
     */
    TabApi getTabApi();

    /**
     * Returns the TPA sub-system API.
     *
     * @return the {@link TpaApi} instance; never {@code null}
     */
    TpaApi getTpaApi();

    /**
     * Returns the vanish sub-system API.
     *
     * @return the {@link VanishApi} instance; never {@code null}
     */
    VanishApi getVanishApi();

    /**
     * Returns the warp sub-system API.
     *
     * @return the {@link WarpApi} instance; never {@code null}
     */
    WarpApi getWarpApi();

    /**
     * Returns the home sub-system API.
     *
     * @return the {@link HomeApi} instance; never {@code null}
     */
    HomeApi getHomeApi();

}
