package reobf.proghatches.item.patterngen;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stable, content-derived identifiers for recipes.
 * <p>
 * The id is a truncated SHA-256 over the recipe map, circuit, inputs and outputs. Ingredient
 * order is normalized so a harmless array reorder does not change the id. Object identity and
 * numeric item ids are never used.
 */
public final class PatternRecipeId {

    private static final char FIELD = '\u001f';
    private static final int HEX_LENGTH = 20;

    private PatternRecipeId() {}

    public static String of(String mapId, int circuit, List<String> inputs, List<String> outputs) {
        StringBuilder canonical = new StringBuilder();
        canonical.append(mapId == null ? "" : mapId)
            .append(FIELD)
            .append(circuit)
            .append(FIELD);
        append(canonical, inputs);
        canonical.append(FIELD);
        append(canonical, outputs);
        return sha256(canonical.toString(), HEX_LENGTH);
    }

    static String sha256(String value, int hexLength) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xf, 16));
                hex.append(Character.forDigit(b & 0xf, 16));
            }
            return hex.substring(0, Math.min(hexLength, hex.length()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private static void append(StringBuilder builder, List<String> values) {
        if (values == null || values.isEmpty()) return;
        List<String> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        for (String value : sorted) {
            if (value == null) continue;
            builder.append(value.length())
                .append(':')
                .append(value)
                .append(';');
        }
    }
}
