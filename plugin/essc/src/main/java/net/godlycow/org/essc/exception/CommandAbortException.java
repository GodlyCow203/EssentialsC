package net.godlycow.org.essc.exception;

import java.util.Map;

/**
 * Stops a command and tells the sender why
 */
public class CommandAbortException extends RuntimeException {

    private final String langKey;
    private final Map<String, String> placeholders;

    public CommandAbortException(String langKey) {
        this(langKey, Map.of());
    }

    public CommandAbortException(String langKey, Map<String, String> placeholders) {
        //a stack trace is useless here, this is an expected outcome, not a bug
        super(langKey, null, false, false);
        this.langKey = langKey;
        this.placeholders = placeholders;
    }

    public static CommandAbortException usage() {
        return new CommandAbortException( null, Map.of() );
    }

    public boolean isUsage() {
        return langKey == null;
    }

    public String getLangKey() {
        return langKey;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }
}
