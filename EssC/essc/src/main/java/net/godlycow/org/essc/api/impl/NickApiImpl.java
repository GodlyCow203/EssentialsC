package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.NickApi;
import net.godlycow.org.essc.nick.NickManager;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NickApiImpl implements NickApi {

    private final NickManager manager;

    public NickApiImpl(NickManager manager) {
        this.manager = manager;
    }

    @Override
    public CompletableFuture<Optional<String>> getNickname(UUID uuid) {
        return manager.getNickname(uuid);
    }

    @Override
    public String getCachedNickname(UUID uuid) {
        return manager.getCachedNickname(uuid);
    }

    @Override
    public CompletableFuture<Optional<UUID>> getUUIDByNickname(String nickname) {
        return manager.getUUIDByNickname(nickname);
    }

    @Override
    public CompletableFuture<Boolean> isNicknameTaken(String nickname, UUID excludeUuid) {
        return manager.isNicknameTaken(nickname, excludeUuid);
    }

    @Override
    public CompletableFuture<Boolean> setNickname(UUID uuid, String nickname) {
        return manager.setNickname(uuid, nickname);
    }

    @Override
    public CompletableFuture<Boolean> removeNickname(UUID uuid) {
        return manager.removeNickname(uuid);
    }

    @Override
    public void applyNickname(Player player) {
        manager.applyNickname(player);
    }

    @Override
    public void clearNickname(Player player) {
        manager.clearNickname(player);
    }
}