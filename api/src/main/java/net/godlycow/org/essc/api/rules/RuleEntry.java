package net.godlycow.org.essc.api.rules;

/**
 * An immutable snapshot of a single server rule.
 *
 * <p>Instances are returned by {@link net.godlycow.org.essc.api.RulesApi}.
 * Rules are parsed from rules.txt using MiniMessage formatting.</p>
 *
 * @see net.godlycow.org.essc.api.RulesApi#getRules()
 */
public record RuleEntry(

        /**
         * The zero-based index of this rule in the rules list.
         */
        int index,

        /**
         * The raw MiniMessage-formatted string of the rule.
         * May contain color tags, decorations, and other MiniMessage syntax.
         */
        String content
) {
}