package net.godlycow.org.essc.api;

import net.godlycow.org.essc.api.rules.RuleEntry;

import java.util.List;

/**
 * API interface for interacting with EssentialsC's server rules system.
 *
 * <p>Rules are loaded from rules.txt on startup and reload, parsed with
 * MiniMessage formatting, and cached in memory. This API provides read-only
 * access to the current rule set.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getRulesApi()}.</p>
 *
 * <pre>{@code
 * RulesApi rules = APIProvider.getAPI().getRulesApi();
 *
 * for (RuleEntry rule : rules.getRules()) {
 *     player.sendMessage(miniMessage.deserialize(rule.content()));
 * }
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface RulesApi {

    /**
     * Returns a copy of the current rules list.
     *
     * <p>Each rule preserves its MiniMessage formatting. Returns an empty list
     * if no rules are loaded.</p>
     *
     * @return a list of {@link RuleEntry} objects; never {@code null}
     */
    List<RuleEntry> getRules();

    /**
     * Returns the number of loaded rules.
     *
     * @return the rule count, or {@code 0} if no rules are loaded
     */
    int getRuleCount();

    /**
     * Reloads the rules from disk.
     *
     * <p>Re-reads rules.txt and re-parses all MiniMessage formatting.
     * Must be called on the main thread.</p>
     */
    void reload();
}