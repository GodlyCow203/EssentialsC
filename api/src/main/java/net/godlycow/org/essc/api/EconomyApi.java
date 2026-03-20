package net.godlycow.org.essc.api;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * API interface for interacting with EssentialsC's economy system.
 *
 * <p>The economy is backed by SQLite and maintains an in-memory cache of recently
 * accessed balances. All methods that touch the database return
 * {@link CompletableFuture} — do not block the main thread waiting on them.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getEconomyApi()}.</p>
 *
 * <pre>{@code
 * EconomyApi eco = APIProvider.getAPI().getEconomyApi();
 *
 * eco.getBalance(player.getUniqueId()).thenAccept(balance -> {
 *     player.sendMessage("Balance: " + eco.format(balance));
 * });
 *
 * eco.withdraw(player.getUniqueId(), BigDecimal.valueOf(100))
 *    .thenAccept(success -> {
 *        if (!success) player.sendMessage("Insufficient funds.");
 *    });
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface EconomyApi {

    /**
     * Returns whether an economy account exists for the given player.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the player to check; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if an account exists
     */
    CompletableFuture<Boolean> hasAccount(UUID uuid);

    /**
     * Creates an economy account for the given player if one does not already exist.
     *
     * <p>The account is initialised with the configured starting balance. If an account
     * already exists this is a no-op and the future resolves {@code false}.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @param name the player's current name, used for display purposes; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if the account was
     *         newly created, {@code false} if it already existed
     */
    CompletableFuture<Boolean> createAccount(UUID uuid, String name);


    /**
     * Returns the current balance of the given player.
     *
     * <p>Results are served from an in-memory cache when available. Returns
     * {@link BigDecimal#ZERO} if no account exists for the UUID.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid the UUID of the player; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to the player's balance; never {@code null}
     */
    CompletableFuture<BigDecimal> getBalance(UUID uuid);

    /**
     * Returns whether the given player has at least the specified amount.
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid   the UUID of the player; must not be {@code null}
     * @param amount the amount to check against; must not be {@code null}
     * @return a {@link CompletableFuture} resolving to {@code true} if the player's
     *         balance is greater than or equal to {@code amount}
     */
    CompletableFuture<Boolean> has(UUID uuid, BigDecimal amount);

    /**
     * Withdraws the given amount from the player's account.
     *
     * <p>The future resolves {@code false} if:</p>
     * <ul>
     *   <li>The amount is below the configured minimum transaction threshold.</li>
     *   <li>The player does not have sufficient funds.</li>
     *   <li>No account exists for the UUID.</li>
     * </ul>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid   the UUID of the player; must not be {@code null}
     * @param amount the amount to withdraw; must be positive
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> withdraw(UUID uuid, BigDecimal amount);

    /**
     * Deposits the given amount into the player's account.
     *
     * <p>The future resolves {@code false} if:</p>
     * <ul>
     *   <li>The amount is below the configured minimum transaction threshold.</li>
     *   <li>The deposit would cause the balance to exceed the configured maximum
     *       (when a maximum balance is set).</li>
     * </ul>
     *
     * <p>If no account exists for the UUID, one is created automatically.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid   the UUID of the player; must not be {@code null}
     * @param amount the amount to deposit; must be positive
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> deposit(UUID uuid, BigDecimal amount);

    /**
     * Sets the player's balance to exactly the given amount.
     *
     * <p>The future resolves {@code false} if:</p>
     * <ul>
     *   <li>The amount is negative.</li>
     *   <li>The amount exceeds the configured maximum balance (when one is set).</li>
     *   <li>No account exists for the UUID.</li>
     * </ul>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param uuid   the UUID of the player; must not be {@code null}
     * @param amount the exact balance to set; must be zero or positive
     * @return a {@link CompletableFuture} resolving to {@code true} on success
     */
    CompletableFuture<Boolean> setBalance(UUID uuid, BigDecimal amount);

    /**
     * Returns the top balances across all players, ordered descending.
     *
     * <p>The returned map preserves insertion order (highest balance first).
     * Keys are player UUIDs, values are their balances.</p>
     *
     * <p><strong>Do not block the main thread waiting on this future.</strong></p>
     *
     * @param limit the maximum number of entries to return; must be positive
     * @return a {@link CompletableFuture} resolving to a linked map of UUID to balance;
     *         never {@code null}, may be empty if no accounts exist
     */
    CompletableFuture<Map<UUID, BigDecimal>> getTopBalances(int limit);

    /**
     * Formats the given amount as a full currency string including the currency name
     * (e.g. {@code "1,234.56 Dollars"} or {@code "1.00 Dollar"}).
     *
     * <p>Singular vs. plural currency name is chosen automatically based on the amount.</p>
     *
     * @param amount the amount to format; must not be {@code null}
     * @return the formatted currency string; never {@code null}
     */
    String format(BigDecimal amount);

    /**
     * Formats the given amount as a plain number string with no currency name appended
     * (e.g. {@code "1,234.56"}).
     *
     * @param amount the amount to format; must not be {@code null}
     * @return the formatted number string; never {@code null}
     */
    String formatPlain(BigDecimal amount);

    /**
     * Returns the singular form of the configured currency name (e.g. {@code "Dollar"}).
     *
     * @return the singular currency name; never {@code null}
     */
    String currencyNameSingular();

    /**
     * Returns the plural form of the configured currency name (e.g. {@code "Dollars"}).
     *
     * @return the plural currency name; never {@code null}
     */
    String currencyNamePlural();

    /**
     * Returns the minimum amount allowed in a single transaction.
     *
     * <p>Withdraw and deposit calls with an amount below this threshold will
     * resolve {@code false} without touching the database.</p>
     *
     * @return the minimum transaction amount; never {@code null}, always positive
     */
    BigDecimal getMinTransaction();

    /**
     * Returns the maximum balance a player may hold, or {@code null} if there is no cap.
     *
     * @return the maximum balance, or {@code null} if uncapped
     */
    BigDecimal getMaxBalance();

    /**
     * Returns whether a maximum balance cap is configured.
     *
     * <p>Equivalent to {@code getMaxBalance() != null}.</p>
     *
     * @return {@code true} if a maximum balance limit is active
     */
    boolean hasMaxBalance();

    /**
     * Returns the starting balance given to newly created accounts.
     *
     * @return the starting balance; never {@code null}, always non-negative
     */
    BigDecimal getStartingBalance();

    /**
     * Returns whether EssentialsC has successfully registered itself as a Vault
     * economy provider.
     *
     * <p>When {@code true}, other plugins using the Vault API will route economy
     * calls through EssentialsC's economy. When {@code false}, Vault is either
     * not installed or the hook failed to register.</p>
     *
     * @return {@code true} if the Vault economy hook is active
     */
    boolean isVaultHooked();
}