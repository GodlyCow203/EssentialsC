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
     * Returns the home sub-system API.
     *
     * @return the {@link HomeApi} instance; never {@code null}
     */
    HomeApi getHomeApi();

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

}
