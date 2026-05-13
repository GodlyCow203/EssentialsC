package net.godlycow.org.essc.modules.schedule;

import net.godlycow.org.essc.EssentialsC;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

public interface ScheduleAction {

    void execute(EssentialsC plugin, Collection<Player> targets);

    static ScheduleAction from(ConfigurationSection section) {
        String type = section.getString("type", "").toLowerCase();
        return switch (type) {
            case "broadcast"  -> new Broadcast(section);
            case "title"      -> new TitleAction(section);
            case "actionbar"  -> new ActionBar(section);
            case "bossbar"    -> new BossBarAction(section);
            case "sound"      -> new SoundAction(section);
            case "command"    -> new CommandAction(section);
            default           -> new UnknownActionType(type);
        };
    }

    class Broadcast implements ScheduleAction {
        private final String message;

        Broadcast(ConfigurationSection s) {
            this.message = s.getString("message", "");
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            MiniMessage mm = plugin.getMiniMessage();
            for (Player p : targets) {
                p.sendMessage(mm.deserialize(applyPlaceholders(message, p)));
            }
        }
    }

    class TitleAction implements ScheduleAction {
        private final String title;
        private final String subtitle;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        TitleAction(ConfigurationSection s) {
            this.title    = s.getString("title", "");
            this.subtitle = s.getString("subtitle", "");
            this.fadeIn   = s.getInt("fade-in", 10);
            this.stay     = s.getInt("stay", 70);

            this.fadeOut  = s.getInt("fade-out", 20);
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            MiniMessage mm = plugin.getMiniMessage();
            for (Player p : targets) {
                Component t  = mm.deserialize(applyPlaceholders(title, p));
                Component st = mm.deserialize(applyPlaceholders(subtitle, p));
                Title times  = Title.title(t, st, Title.Times.times(
                        Duration.ofMillis(fadeIn  * 50L),
                        Duration.ofMillis(stay    * 50L),
                        Duration.ofMillis(fadeOut * 50L)
                ));
                p.showTitle(times);
            }
        }
    }

    class ActionBar implements ScheduleAction {
        private final String message;

        ActionBar(ConfigurationSection s) {
            this.message = s.getString("message", "");
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            MiniMessage mm = plugin.getMiniMessage();
            for (Player p : targets) {
                p.sendActionBar(mm.deserialize(applyPlaceholders(message, p)));
            }
        }
    }

    class BossBarAction implements ScheduleAction {
        private final String message;
        private final BossBar.Color color;
        private final BossBar.Overlay overlay;
        private final int duration;

        BossBarAction(ConfigurationSection s) {
            this.message  = s.getString("message", "");
            this.duration = s.getInt("duration", 5);

            BossBar.Color parsedColor;
            try {
                parsedColor = BossBar.Color.valueOf(s.getString("color", "YELLOW").toUpperCase());
            } catch (IllegalArgumentException e) {
                parsedColor = BossBar.Color.YELLOW;
            }
            this.color = parsedColor;

            BossBar.Overlay parsedOverlay;
            try {
                parsedOverlay = BossBar.Overlay.valueOf(s.getString("style", "PROGRESS").toUpperCase());
            } catch (IllegalArgumentException e) {
                parsedOverlay = BossBar.Overlay.PROGRESS;
            }
            this.overlay = parsedOverlay;
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            MiniMessage mm = plugin.getMiniMessage();
            for (Player p : targets) {
                Component name = mm.deserialize(applyPlaceholders(message, p));
                BossBar bar = BossBar.bossBar(name, 1.0f, color, overlay);
                p.showBossBar(bar);

                plugin.getEssScheduler().runGlobalLater(() -> p.hideBossBar(bar), duration * 20L);
            }
        }
    }

    class SoundAction implements ScheduleAction {
        private final String soundName;
        private final float volume;
        private final float pitch;

        SoundAction(ConfigurationSection s) {
            this.soundName = s.getString("sound", "ENTITY_PLAYER_LEVELUP");
            this.volume    = (float) s.getDouble("volume", 1.0);
            this.pitch     = (float) s.getDouble("pitch", 1.0);
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            Sound sound;
            try {
                sound = Sound.valueOf(soundName.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[Schedules] Unknown sound: '" + soundName + "'");
                return;
            }
            for (Player p : targets) {
                p.playSound(p.getLocation(), sound, volume, pitch);
            }
        }
    }

    class CommandAction implements ScheduleAction {
        private final String runAs;
        private final String command;

        CommandAction(ConfigurationSection s) {
            this.runAs   = s.getString("run-as", "console").toLowerCase();
            this.command = s.getString("command", "");
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            if (runAs.equals("player")) {
                for (Player p : targets) {
                    String cmd = applyPlaceholders(command, p);
                    p.performCommand(cmd);
                }
            } else {
                if (command.contains("{player}")) {
                    for (Player p : targets) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), applyPlaceholders(command, p));
                    }
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                }
            }
        }
    }

    class UnknownActionType implements ScheduleAction {
        private final String type;

        UnknownActionType(String type) {
            this.type = type;
        }

        @Override
        public void execute(EssentialsC plugin, Collection<Player> targets) {
            plugin.getLogger().warning("[Schedules] Unknown action type: '" + type + "'");
        }
    }

    static String applyPlaceholders(String input, Player player) {
        double tps = Bukkit.getTPS()[0];
        String tpsFormatted = String.format("%.1f", Math.min(tps, 20.0));

        return input
                .replace("{player}", player.displayName() != null
                        ? net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
                        .legacySection().serialize(player.displayName())
                        : player.getName())
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{world}",  player.getWorld().getName())
                .replace("{tps}",    tpsFormatted)
                .replace("{time}",   java.time.LocalTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
    }
}