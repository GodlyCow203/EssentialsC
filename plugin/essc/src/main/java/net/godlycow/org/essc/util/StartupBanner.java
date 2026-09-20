package net.godlycow.org.essc.util;

import net.godlycow.org.essc.server.software.ServerSoftware;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class StartupBanner {

    private static final boolean ansi = ansiSupported();

    private StartupBanner() {}

    public static void print(JavaPlugin plugin, Logger logger) {
        String version = plugin.getDescription().getVersion();

        String yellow = fg("#FFD900");
        String gray = fg("#9B9B9B");
        String white = fg("#FFFFFF");
        String green = fg("#00D97E");
        String spigot = fg( "#FFA100");
        String blue = fg("#009DFF");


        String swColor;
        String swLabel;

        switch (ServerSoftware.get()) {

            case FOLIA  -> {
                swColor = green;
                swLabel = "Folia region-threaded";
            }

            case PAPER -> {
                swColor = blue;
                swLabel = "Paper";
            }

            default -> {
                swColor = spigot;
                swLabel = "Spigot (limited support)";
            }
        }

        String[] lines = {
                "",
                paint(yellow, "  ███████╗███████╗███████╗ ██████╗"),
                paint(yellow, "  ██╔════╝██╔════╝██╔════╝██╔════╝") + paint(gray,"  EssentialsC"),
                paint(yellow, "  █████╗  ███████╗███████╗██║     ") + paint(white,"  v" + version),
                paint(yellow, "  ██╔══╝  ╚════██║╚════██║██║     ") + paint(gray, "  by _GodlyCow"),
                paint(yellow, "  ███████╗███████║███████║╚██████╗"),
                paint(yellow, "  ╚══════╝╚══════╝╚══════╝ ╚═════╝"),
                "",
                paint(gray, "  Platform  ") + paint(bold() + swColor, swLabel),
                paint(gray, "  ANSI      ") + paint(ansi ? green : green, ansi ? "supported" : "not supported"),
                "",
        };

        for (String line : lines) {
            logger.info(line);
        }
    }

    private static String paint( String prefix,  String text) {
        return ansi ? prefix + text + "\033[0m" : text;
    }


    private static String fg(String hex) {
        return String.format("\033[38;2;%d;%d;%dm",
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16));
    }

    private static String bold() {
        return ansi ? "\033[1m" : "";
    }

    private static boolean ansiSupported() {
        return switch (System.getProperty("ansi.enabled", "").toLowerCase())  {
            case "false" -> false;
            case "true" -> true;
            default -> detect();
        };
    }

    private static boolean detect() {
        if ("dumb".equalsIgnoreCase(System.getenv("TERM")))
            return false;

        if ( System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null)
            return true;

        if (System.console() != null)
            return true;

        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("nix") || os.contains("nux") || os.contains("mac") || os.contains("win");
    }
}