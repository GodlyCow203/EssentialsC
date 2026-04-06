package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.TpaApi;
import net.godlycow.org.essc.api.teleport.TPARequestEntry;
import net.godlycow.org.essc.teleport.TPARequest;
import net.godlycow.org.essc.teleport.TPAManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class TpaApiImpl implements TpaApi {

    private final TPAManager manager;

    public TpaApiImpl(TPAManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean requestTeleport(Player requester, Player target, TPARequestEntry.Type type) {
        return manager.requestTeleport(requester, target, TPARequest.Type.valueOf(type.name()));
    }

    @Override
    public boolean acceptRequest(Player target, Player requester) {
        return manager.acceptRequest(target, requester);
    }

    @Override
    public boolean denyRequest(Player target, Player requester) {
        return manager.denyRequest(target, requester);
    }

    @Override
    public boolean cancelRequest(Player requester, Player target) {
        return manager.cancelRequest(requester, target);
    }

    @Override
    public void toggleTPA(Player player) {
        manager.toggleTPA(player);
    }

    @Override
    public void toggleIgnore(Player player, Player target) {
        manager.toggleIgnore(player, target);
    }

    @Override
    public List<TPARequestEntry> getIncomingRequests(Player player) {
        return manager.getIncomingRequests(player).stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public List<TPARequestEntry> getOutgoingRequests(Player player) {
        return manager.getOutgoingRequests(player).stream()
                .map(this::mapToEntry)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasIncomingRequests(Player player) {
        return manager.hasIncomingRequests(player);
    }

    @Override
    public boolean hasOutgoingRequests(Player player) {
        return manager.hasOutgoingRequests(player);
    }

    @Override
    public boolean isBlocking(Player player) {
        return manager.getBlockedPlayers().contains(player.getUniqueId());
    }

    @Override
    public void cancelTeleport(Player player, String reason) {
        manager.cancelTeleport(player, reason);
    }

    @Override
    public boolean isInTeleport(Player player) {
        return manager.hasOutgoingRequests(player);
    }

    @Override
    public void reload() {
        manager.reload();
    }

    private TPARequestEntry mapToEntry(TPARequest r) {
        return new TPARequestEntry(
                r.getRequester(),
                r.getTarget(),
                TPARequestEntry.Type.valueOf(r.getType().name()),
                r.getTimestamp(),
                r.isExpired()
        );
    }
}