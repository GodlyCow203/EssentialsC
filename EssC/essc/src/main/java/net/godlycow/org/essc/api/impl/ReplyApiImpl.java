package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.ReplyApi;
import net.godlycow.org.essc.msg.ReplyManager;

import java.util.UUID;

public class ReplyApiImpl implements ReplyApi {

    private final ReplyManager manager;

    public ReplyApiImpl(ReplyManager manager) {
        this.manager = manager;
    }

    @Override
    public void setReplyTarget(UUID player, UUID target) {
        manager.setReplyTarget(player, target);
    }

    @Override
    public UUID getReplyTarget(UUID player) {
        return manager.getReplyTarget(player);
    }

    @Override
    public void removeReplyTarget(UUID player) {
        manager.removeReplyTarget(player);
    }
}