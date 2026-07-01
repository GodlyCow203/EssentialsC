package net.godlycow.org.essc.storage.user;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserRepo {
    CompletableFuture<UserProfile> findOrCreate(UUID uuid, String username);
    CompletableFuture<UserProfile> findByUuid(UUID uuid);
    CompletableFuture<Boolean> save(UserProfile profile);
    CompletableFuture<Boolean> recordIp(UUID uuid, String ip);
    CompletableFuture<Set<UUID>> getIgnoredPlayers(UUID uuid);
    CompletableFuture<Boolean> addIgnoredPlayer(UUID uuid, UUID ignoredUuid, String ignoredName);
    CompletableFuture<Boolean> removeIgnoredPlayer(UUID uuid, UUID ignoredUuid);
    CompletableFuture<java.util.List<String>> getIpHistory(UUID uuid);
}
