package net.godlycow.org.essc.nick;

import java.util.UUID;

public interface NicknameSyncHook {

    void onNicknameSet(UUID uuid, String nickname);

    void onNicknameCleared(UUID uuid);
}