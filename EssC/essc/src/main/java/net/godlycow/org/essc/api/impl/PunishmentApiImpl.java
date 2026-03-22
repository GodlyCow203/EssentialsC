package net.godlycow.org.essc.api.impl;

import net.godlycow.org.essc.api.PunishmentApi;
import net.godlycow.org.essc.api.punishment.BanEntry;
import net.godlycow.org.essc.api.punishment.MuteEntry;
import net.godlycow.org.essc.punishment.PunishmentManager;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PunishmentApiImpl implements PunishmentApi {

    private final PunishmentManager manager;

    public PunishmentApiImpl(PunishmentManager manager) {
        this.manager = manager;
    }

    @Override
    public void banPlayer(UUID uuid, String name, String reason, String banner, long expires) {
        manager.banPlayer(uuid, name, reason, banner, expires);
    }

    @Override
    public void unbanPlayer(UUID uuid) {
        manager.unbanPlayer(uuid);
    }

    @Override
    public boolean isBanned(UUID uuid) {
        return manager.isBanned(uuid);
    }

    @Override
    public BanEntry getBanEntry(UUID uuid) {
        PunishmentManager.BanEntry e = manager.getBanEntry(uuid);
        if (e == null) return null;
        return new BanEntry(e.uuid(), e.name(), e.reason(), e.banner(), e.time(), e.expires());
    }

    @Override
    public List<BanEntry> getAllBans() {
        return manager.getAllBans().stream()
                .map(e -> new BanEntry(e.uuid(), e.name(), e.reason(), e.banner(), e.time(), e.expires()))
                .collect(Collectors.toList());
    }

    @Override
    public void mutePlayer(UUID uuid, String name, String reason, String muter, long expires) {
        manager.mutePlayer(uuid, name, reason, muter, expires);
    }

    @Override
    public void unmutePlayer(UUID uuid) {
        manager.unmutePlayer(uuid);
    }

    @Override
    public boolean isMuted(UUID uuid) {
        return manager.isMuted(uuid);
    }

    @Override
    public MuteEntry getMuteEntry(UUID uuid) {
        PunishmentManager.MuteEntry e = manager.getMuteEntry(uuid);
        if (e == null) return null;
        return new MuteEntry(e.uuid(), e.name(), e.reason(), e.muter(), e.time(), e.expires());
    }

    @Override
    public List<MuteEntry> getAllMutes() {
        return manager.getAllMutes().stream()
                .map(e -> new MuteEntry(e.uuid(), e.name(), e.reason(), e.muter(), e.time(), e.expires()))
                .collect(Collectors.toList());
    }
}