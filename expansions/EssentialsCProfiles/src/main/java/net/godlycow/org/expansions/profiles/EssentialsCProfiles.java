package net.godlycow.org.expansions.profiles;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.expansions.profiles.chat.ChatProfileListener;
import net.godlycow.org.expansions.profiles.command.ProfileCommand;
import net.godlycow.org.expansions.profiles.gui.ProfileGui;
import net.godlycow.org.expansions.profiles.messages.MessagesManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EssentialsCProfiles extends JavaPlugin {

    private static EssentialsCProfiles instance;
    private EssentialsC essc;
    private MessagesManager messages;
    private ProfileGui profileGui;

    @Override
    public void onEnable() {
        instance = this;

        essc = (EssentialsC) getServer().getPluginManager().getPlugin("EssentialsC");
        if (essc == null) {
            getLogger().severe("EssentialsC not found! Disabling EssentialsCProfiles.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        messages = new MessagesManager(this);
        profileGui = new ProfileGui(this, essc, messages);
        getCommand("profile").setExecutor(
                new ProfileCommand(this, essc, profileGui, messages));
        getServer().getPluginManager().registerEvents(
                new ChatProfileListener(this, essc, messages), this);

        getLogger().info("EssentialsCProfiles enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("EssentialsCProfiles disabled.");
    }

    public static EssentialsCProfiles getInstance(){
        return instance;
    }

    public EssentialsC getEssc(){
        return essc;
    }

    public MessagesManager getMessages(){
        return messages;
    }

    public ProfileGui getProfileGui() {
        return profileGui;
    }
}
