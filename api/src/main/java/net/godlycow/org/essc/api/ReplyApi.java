package net.godlycow.org.essc.api;

import java.util.UUID;

/**
 * API interface for interacting with EssentialsC's private message reply system.
 *
 * <p>The reply system tracks the last player each player messaged so that
 * {@code /r} can send a follow-up without specifying a target. External plugins
 * that implement custom messaging (e.g. staff chat, party chat) can integrate
 * with this to keep the reply target in sync.</p>
 *
 * <p>Retrieve an instance via {@link EssentialsCAPI#getReplyApi()}.</p>
 *
 * <pre>{@code
 * ReplyApi reply = APIProvider.getAPI().getReplyApi();
 *
 * // after sending a custom message, register the reply target
 * reply.setReplyTarget(sender.getUniqueId(), recipient.getUniqueId());
 *
 * // check who the player would reply to
 * UUID target = reply.getReplyTarget(player.getUniqueId());
 * }</pre>
 *
 * @see EssentialsCAPI
 * @see APIProvider
 */
public interface ReplyApi {

    /**
     * Sets the reply target for the given player.
     *
     * <p>Pass {@code null} as the target to clear the reply target, which is
     * equivalent to calling {@link #removeReplyTarget(UUID)}.</p>
     *
     * @param player the UUID of the player whose reply target to set; must not be {@code null}
     * @param target the UUID of the player to reply to, or {@code null} to clear
     */
    void setReplyTarget(UUID player, UUID target);

    /**
     * Returns the UUID of the player that the given player would reply to,
     * or {@code null} if no reply target is set.
     *
     * <p>Note that the returned UUID is not validated against online players —
     * the target may have gone offline since the target was recorded.</p>
     *
     * @param player the UUID of the player to look up; must not be {@code null}
     * @return the reply target UUID, or {@code null} if none is set
     */
    UUID getReplyTarget(UUID player);

    /**
     * Removes the reply target for the given player.
     *
     * <p>Has no effect if the player has no reply target set.</p>
     *
     * @param player the UUID of the player whose reply target to clear; must not be {@code null}
     */
    void removeReplyTarget(UUID player);
}