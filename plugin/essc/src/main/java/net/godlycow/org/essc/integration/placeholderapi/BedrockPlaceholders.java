package net.godlycow.org.essc.integration.placeholderapi;

import net.godlycow.org.essc.EssentialsC;
import net.godlycow.org.essc.integration.bedrock.BedrockUtil;
import org.bukkit.entity.Player;

import java.util.List;

public class BedrockPlaceholders {

    private final EssentialsC plugin;

    public BedrockPlaceholders(EssentialsC plugin) {
        this.plugin = plugin;
    }

    public String onRequest(Player player, String identifier) {
        if (!identifier.startsWith("bedrock_")) {
            return null;
        }

        BedrockUtil bedrock = plugin.getBedrockUtil();
        boolean isBedrock = bedrock != null && bedrock.isBedrockPlayer(player);

        return switch (identifier.toLowerCase()) {
            case "bedrock_is" -> String.valueOf(isBedrock);
            case "bedrock_platform" -> isBedrock ? "Bedrock" : "Java";
            case "bedrock_gamertag" -> isBedrock
                    ? orDefault(bedrock.getHook().getGamertag(player.getUniqueId()), player.getName())
                    : player.getName();
            case "bedrock_device" -> isBedrock
                    ? orDefault(bedrock.getHook().getDeviceOs(player.getUniqueId()), "Unknown")
                    : "";
            case "bedrock_input" -> isBedrock
                    ? orDefault(bedrock.getHook().getInputMode(player.getUniqueId()), "Unknown")
                    : "";
            case "bedrock_count" -> String.valueOf(bedrock == null ? 0 : bedrock.getHook().getBedrockPlayerCount());
            default -> null;
        };
    }

    private static String orDefault(String value, String fallback) {
        return value == null || value.isEmpty() ? fallback : value;
    }

    public static List<String> getPlaceholderList() {
        return List.of(
                "%essc_bedrock_is%        — 'true' if the player joined through Geyser/Floodgate",
                "%essc_bedrock_platform%  — 'Bedrock' or 'Java'",
                "%essc_bedrock_gamertag%  — Xbox gamertag without the Floodgate prefix (Java name for Java players)",
                "%essc_bedrock_device%    — Bedrock device OS, e.g. 'Android' or 'Xbox One' (empty for Java)",
                "%essc_bedrock_input%     — Bedrock input mode: KEYBOARD_MOUSE, TOUCH, CONTROLLER (empty for Java)",
                "%essc_bedrock_count%     — number of Bedrock players online"
        );
    }
}
