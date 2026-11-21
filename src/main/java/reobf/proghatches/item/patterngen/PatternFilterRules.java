package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Multiline regex rule handling for the pattern generator filters.
 * <p>
 * Every non-empty line is one rule and the rules in a section are ORed. Regexes are compiled
 * case-insensitively by default; Java inline flags such as {@code (?-i)} still take precedence.
 */
public final class PatternFilterRules {

    public static final int DEFAULT_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final String REGEX_META = "\\^$|?*+()[]{}.";

    private PatternFilterRules() {}

    /**
     * Escapes regex metacharacters so the result matches the input literally. Unlike
     * {@link Pattern#quote(String)} this does not wrap the text in visible {@code \Q...\E}.
     */
    public static String quoteLiteral(String value) {
        if (value == null) return "";
        StringBuilder builder = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (REGEX_META.indexOf(c) >= 0) builder.append('\\');
            builder.append(c);
        }
        return builder.toString();
    }

    public static final class Compiled {

        private final List<Pattern> patterns;
        private final String invalidPattern;
        private final int ruleCount;

        private Compiled(List<Pattern> patterns, String invalidPattern, int ruleCount) {
            this.patterns = patterns;
            this.invalidPattern = invalidPattern;
            this.ruleCount = ruleCount;
        }

        public boolean isEmpty() {
            return this.patterns.isEmpty();
        }

        public boolean hasInvalid() {
            return this.invalidPattern != null;
        }

        public String invalidPattern() {
            return this.invalidPattern;
        }

        public int ruleCount() {
            return this.ruleCount;
        }

        public boolean matchesAny(Iterable<String> candidates) {
            if (this.patterns.isEmpty() || candidates == null) return false;
            for (String candidate : candidates) {
                if (candidate == null) continue;
                for (Pattern pattern : this.patterns) {
                    if (pattern.matcher(candidate)
                        .find()) return true;
                }
            }
            return false;
        }
    }

    public static Compiled compile(String text) {
        return compile(text, DEFAULT_FLAGS);
    }

    public static Compiled compile(String text, int flags) {
        List<String> lines = ruleLines(text);
        List<Pattern> patterns = new ArrayList<>(lines.size());
        String invalid = null;
        for (String line : lines) {
            try {
                patterns.add(Pattern.compile(line, flags));
            } catch (PatternSyntaxException e) {
                invalid = line;
                patterns.clear();
                break;
            }
        }
        return new Compiled(patterns, invalid, lines.size());
    }

    public static List<String> ruleLines(String text) {
        if (text == null || text.isEmpty()) return Collections.emptyList();
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\\r?\\n")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) lines.add(trimmed);
        }
        return lines;
    }

    /**
     * Applies the four sections to the candidate names of one recipe. An empty include section
     * imposes no restriction; an exclude section rejects on the first match.
     */
    public static boolean accepts(Compiled inputInclude, Compiled inputExclude, Compiled outputInclude,
        Compiled outputExclude, List<String> inputNames, List<String> outputNames) {
        if (inputInclude != null && !inputInclude.isEmpty() && !inputInclude.matchesAny(inputNames)) return false;
        if (outputInclude != null && !outputInclude.isEmpty() && !outputInclude.matchesAny(outputNames)) return false;
        if (inputExclude != null && !inputExclude.isEmpty() && inputExclude.matchesAny(inputNames)) return false;
        return outputExclude == null || outputExclude.isEmpty() || !outputExclude.matchesAny(outputNames);
    }
}
