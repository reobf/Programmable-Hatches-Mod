package reobf.proghatches.gt.metatileentity.util.polyfill;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

/**
 * A specialization of {@link NumberFormat} for ModularUI. Provides methods for formatting and parsing of numbers.
 * Mirrors most of the functionality of {@link NumberFormat}, but is always based on the locale specified in ModularUI's
 * (By default, this is the player's system locale.) Also provides some GTNH-specific formatting options.
 */
public class NumberFormatMUI extends NumberFormat {

    /*
     * Basic idea: We keep baseFormat as a DecimalFormat obtained from the locale set by the user. Delegate most method
     * calls to this baseFormat, but when calling code changes any of the parameters (e.g., number of decimal places),
     * we store the changes in this instance of NumberFormatMUI. If the player changes their locale in the config, we
     * create a new baseFormat from the new locale, and use the stored parameters to also update the new baseFormat.
     * This means that the calling code can keep only one instance of NumberFormatMUI, configure its parameters, and
     * this one instance will automatically be updated if the player's locale changes.
     */
    protected DecimalFormat baseFormat;
    protected Locale currentLocale;

    public NumberFormatMUI() {
        refreshBaseFormat();
    }

    public static Locale locale = Locale.getDefault();

    /**
     * Updates the base format based on the locale from ModularUI's config, and then modifies all properties that have
     * been previously changed through this instance.
     */
    protected void refreshBaseFormat() {
        currentLocale = locale;
        NumberFormat nf = NumberFormat.getNumberInstance(currentLocale);

        if (nf instanceof DecimalFormat) {
            baseFormat = (DecimalFormat) nf;
        } else {
            // Current locale does not provide a DecimalFormat.
            baseFormat = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        }

        // Workaround: The grouping separator in French locales is the non-breakable space '\u00A0',
        // which does not display well in the Minecraft font. Replace it with a plain space.
        DecimalFormatSymbols dfs = baseFormat.getDecimalFormatSymbols();
        if (dfs.getGroupingSeparator() == '\u00A0' || dfs.getGroupingSeparator() == '\u202F') {
            dfs.setGroupingSeparator(' ');
            baseFormat.setDecimalFormatSymbols(dfs);
        }

        // Update any fields that have been previously changed via NumberFormatMUI.
        if (minimumIntegerDigitsChanged) baseFormat.setMinimumIntegerDigits(getMinimumIntegerDigits());
        if (maximumIntegerDigitsChanged) baseFormat.setMaximumIntegerDigits(getMaximumIntegerDigits());
        if (minimumFractionDigitsChanged) baseFormat.setMinimumFractionDigits(getMinimumFractionDigits());
        if (maximumFractionDigitsChanged) baseFormat.setMaximumFractionDigits(getMaximumFractionDigits());

        if (groupingUsedChanged) baseFormat.setGroupingUsed(isGroupingUsed());
        if (parseIntegerOnlyChanged) baseFormat.setParseIntegerOnly(isParseIntegerOnly());
        if (roundingModeChanged) baseFormat.setRoundingMode(getRoundingMode());
    }

    /* Formatting / Parsing */

    protected static final FieldPosition unusedFieldPosition = new FieldPosition(0);

    public StringBuffer format(double number, StringBuffer toAppendTo) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, unusedFieldPosition);
    }

    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, pos);
    }

    public StringBuffer format(long number, StringBuffer toAppendTo) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, unusedFieldPosition);
    }

    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, pos);
    }

    public StringBuffer format(Object number, StringBuffer toAppendTo) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, unusedFieldPosition);
    }

    @Override
    public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.format(number, toAppendTo, pos);
    }

    public static final char[] SUFFIXES = { 'k', 'M', 'G', 'T', 'P', 'E' };

    /**
     * Formats a number using a size-appropriate suffix: k, M, G, etc. Closely inspired by
     * <code>appeng.util.ReadableNumberConverter</code>.
     */
    public String formatWithSuffix(long number) {
        return formatWithSuffix(number, new StringBuffer()).toString();
    }

    /**
     * Formats a number using a size-appropriate suffix: k, M, G, etc. Closely inspired by
     * <code>appeng.util.ReadableNumberConverter</code>.
     */
    public StringBuffer formatWithSuffix(long number, StringBuffer toAppendTo) {
        if (-10_000 < number && number < 10_000) {
            // Display the full number.
            return format(number, toAppendTo, unusedFieldPosition);
        }

        for (int order = 0; order < SUFFIXES.length; ++order) {
            if (-10_000 < number && number < 10_000) {
                // Display as 1.2M
                return format((number / 100L) / 10d, toAppendTo, unusedFieldPosition).append(SUFFIXES[order]);
            }
            if (-1_000_000 < number && number < 1_000_000) {
                // Display as 34M
                return format(number / 1000L, toAppendTo, unusedFieldPosition).append(SUFFIXES[order]);
            }
            number /= 1000;
        }
        return format(number / 1000L, toAppendTo, unusedFieldPosition).append(SUFFIXES[SUFFIXES.length - 1]);
    }

    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        if (currentLocale != locale) refreshBaseFormat();
        return baseFormat.parse(source, parsePosition);
    }

    /* Configuring the format. */

    private boolean minimumIntegerDigitsChanged = false;

    @Override
    public void setMinimumIntegerDigits(int newValue) {
        super.setMinimumIntegerDigits(newValue);
        baseFormat.setMinimumIntegerDigits(getMinimumIntegerDigits());
        minimumIntegerDigitsChanged = true;
    }

    private boolean maximumIntegerDigitsChanged = false;

    @Override
    public void setMaximumIntegerDigits(int newValue) {
        super.setMaximumIntegerDigits(newValue);
        baseFormat.setMaximumIntegerDigits(getMaximumIntegerDigits());
        maximumIntegerDigitsChanged = true;
    }

    private boolean minimumFractionDigitsChanged = false;

    @Override
    public void setMinimumFractionDigits(int newValue) {
        super.setMinimumFractionDigits(newValue);
        baseFormat.setMinimumFractionDigits(getMinimumFractionDigits());
        minimumFractionDigitsChanged = true;
    }

    private boolean maximumFractionDigitsChanged = false;

    @Override
    public void setMaximumFractionDigits(int newValue) {
        super.setMaximumFractionDigits(newValue);
        baseFormat.setMaximumFractionDigits(getMaximumFractionDigits());
        maximumFractionDigitsChanged = true;
    }

    private boolean groupingUsedChanged = false;

    @Override
    public void setGroupingUsed(boolean newValue) {
        super.setGroupingUsed(newValue);
        baseFormat.setGroupingUsed(isGroupingUsed());
        groupingUsedChanged = true;
    }

    private boolean parseIntegerOnlyChanged = false;

    @Override
    public void setParseIntegerOnly(boolean value) {
        super.setParseIntegerOnly(value);
        baseFormat.setParseIntegerOnly(isParseIntegerOnly());
        parseIntegerOnlyChanged = true;
    }

    // NumberFormat.setRoundingMode() is not defined.
    protected RoundingMode roundingMode;

    private boolean roundingModeChanged = false;

    @Override
    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
        baseFormat.setRoundingMode(roundingMode);
        roundingModeChanged = true;
    }

    @Override
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }

    /* interface Cloneable */

    @Override
    public Object clone() {
        // Need to make a deep copy of baseFormat; other fields are okay to shallow copy.
        NumberFormatMUI other = (NumberFormatMUI) super.clone();
        other.baseFormat = (DecimalFormat) baseFormat.clone();
        return other;
    }

}
