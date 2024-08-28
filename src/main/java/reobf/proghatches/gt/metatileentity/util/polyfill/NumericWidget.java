package reobf.proghatches.gt.metatileentity.util.polyfill;

import java.awt.Point;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.regex.Pattern;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.MathHelper;
import reobf.proghatches.gt.metatileentity.util.polyfill.MathExpressionParser.Context;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.GlStateManager;

import com.gtnewhorizons.modularui.api.drawable.GuiHelper;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;

import cpw.mods.fml.common.Loader;

/**
 * A widget that allows the user to enter a numeric value. Synced between client and server. Automatically handles
 * number parsing and formatting. Only the numeric value (of type <code>double</code>) is exposed to the calling code.
 * <p>
 * If GTNHLib is present, also allows entering values as mathematical expressions.
 */
public class NumericWidget extends BaseTextFieldWidget implements ISyncedWidget {

    private double value = 0;
    private DoubleSupplier getter;
    private DoubleConsumer setter;
    private DoubleUnaryOperator validator;
    public static final boolean isGTNHLibLoaded = Loader.isModLoaded("gtnhlib");
    private double minValue = 0;
    private double maxValue = Double.POSITIVE_INFINITY;
    private double defaultValue = 0;
    private double scrollStep = 1;
    private double scrollStepCtrl = 0.1;
    private double scrollStepShift = 100;
    private boolean integerOnly = true;
    private NumberFormat numberFormat;

    private Context ctx;
    private static final int MAX_FRACTION_DIGITS = 4;
    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9., \u202F_’]*");
    // Character '\u202F' (non-breaking space) to support French locale thousands separator.

    public NumericWidget() {
        setTextAlignment(Alignment.CenterLeft);
        handler.setMaxLines(1);

        numberFormat = new NumberFormatMUI();
        numberFormat.setMaximumFractionDigits(MAX_FRACTION_DIGITS);

        if (isGTNHLibLoaded) {
            handler.setPattern(MathExpressionParser.EXPRESSION_PATTERN);
            ctx = new MathExpressionParser.Context();
            ctx.setNumberFormat(numberFormat);
        } else {
            handler.setPattern(NUMBER_PATTERN);
        }
    }

    @Override
    public void draw(float partialTicks) {
        Point draggableTranslate = getDraggableTranslate();
        GuiHelper
                .useScissor(pos.x + draggableTranslate.x, pos.y + draggableTranslate.y, size.width, size.height, () -> {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(1, 1, 0);
                    renderer.setSimulate(false);
                    renderer.setScale(scale);
                    renderer.setAlignment(textAlignment, size.width - 2, size.height - 2);
                    renderer.draw(handler.getText());
                    GlStateManager.popMatrix();
                });
    }

    public double getValue() {
        return value;
    }

    public void setValue(double newValue) {
        value = newValue;

        String displayValue = numberFormat.format(value);

        if (handler.getText().isEmpty()) {
            handler.getText().add(displayValue);
        } else {
            handler.getText().set(0, displayValue);
        }
    }

    /**
     * @return true if the value has changed.
     */
    private boolean validateAndSetValue(double newValue) {
        newValue = MathHelper.clamp_double(newValue, minValue, maxValue);
        if (integerOnly) {
            newValue = Math.round(newValue);
        }
        if (validator != null) {
            newValue = validator.applyAsDouble(newValue);
        }

        // We want to call setValue even if the value has not changed.
        // The text field might contain an expression which evaluates to the old value,
        // we still want to replace this expression with an actual number.
        boolean changed = newValue != value;
        setValue(newValue);
        return changed;
    }

    private double parseValueFromTextField() {
        if (handler.getText().isEmpty()) {
            handler.getText().add("");
        }
        if (handler.getText().size() > 1) {
            throw new IllegalStateException("NumericWidget can only have one line!");
        }

        if (isGTNHLibLoaded) {
            double newValue = MathExpressionParser.parse(handler.getText().get(0), ctx);
            return ctx.wasSuccessful() ? newValue : value;
        } else {
            if (handler.getText().get(0) == null || handler.getText().get(0).isEmpty()) {
                return defaultValue;
            }
            try {
                return numberFormat.parse(handler.getText().get(0)).doubleValue();
            } catch (ParseException ignore) {
                return value;
            }
        }
    }

    /* Configure widget properties. */

    /**
     * Sets the minimum allowed input value. Can be negative.
     * <p>
     * Default: 0
     */
    public NumericWidget setMinValue(double minValue) {
        this.minValue = minValue;
        return this;
    }

    /**
     * Sets the maximum allowed input value. This is also used to evaluate expressions that refer to a percentage of the
     * maximum.
     * <p>
     * Default: Double.POSITIVE_INFINITY.
     */
    public NumericWidget setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        if (isGTNHLibLoaded) {
            ctx.setHundredPercent(maxValue);
        }
        return this;
    }

    /**
     * Convenience method to set both minimum and maximum value at the same time.
     *
     * @see #setMinValue
     * @see #setMaxValue
     */
    public NumericWidget setBounds(double minValue, double maxValue) {
        this.minValue = minValue;
        this.maxValue = maxValue;
        if (isGTNHLibLoaded) {
            ctx.setHundredPercent(maxValue);
        }
        return this;
    }

    /**
     * Sets the default input value to be used when the text field is empty.
     * <p>
     * Default: 0.
     */
    public NumericWidget setDefaultValue(double defaultValue) {
        this.defaultValue = defaultValue;
        if (isGTNHLibLoaded) {
            ctx.setDefaultValue(defaultValue);
        }
        return this;
    }

    /**
     * Sets the values by which to increment the value when the player uses the scroll wheel. Scrolling up increases the
     * value, scrolling down decreases. The typical convention is for ctrl to be a smaller step than the base, and shift
     * to be a larger step; but this can be changed if there is a good reason.
     * <p>
     * Default values: 1, 0.1, 100 in order.
     *
     * @param baseStep  By how much to change the value when no modifier key is held.
     * @param ctrlStep  By how much to change the value when the ctrl key is held.
     * @param shiftStep By how much to change the value when the shift key is held.
     */
    public NumericWidget setScrollValues(double baseStep, double ctrlStep, double shiftStep) {
        this.scrollStep = baseStep;
        this.scrollStepCtrl = ctrlStep;
        this.scrollStepShift = shiftStep;
        return this;
    }

    /**
     * If this is set to true, the widget will always round the entered value to the nearest integer. Otherwise, the
     * value is returned with full precision.
     * <p>
     * Default: true.
     */
    public NumericWidget setIntegerOnly(boolean integerOnly) {
        this.integerOnly = integerOnly;
        return this;
    }

    /**
     * If this is set to true, the widget will only accept a single number, and will not try to evaluate mathematical
     * expressions. Note that expression parsing requires GTNHLib.
     * <p>
     * Default: false.
     */
    public NumericWidget setPlainOnly(boolean plainOnly) {
        if (isGTNHLibLoaded) {
            ctx.setPlainOnly(plainOnly);
            handler.setPattern(plainOnly ? NUMBER_PATTERN : MathExpressionParser.EXPRESSION_PATTERN);
        }
        return this;
    }

    /**
     * Returns the {@link NumberFormat} used by this widget. This is a more direct method of modifying this format, such
     * as number of decimal spaces, than calling {@link #setNumberFormat(NumberFormat)} with a completely new format.
     */
    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    /**
     * Convenience method for chaining. Use: <code>
     *     new NumericWidget().modifyNumberFormat(format -> format.setMaximumFractionDigits(10)).setBounds(0, 10)...
     * </code>
     */
    public NumericWidget modifyNumberFormat(Consumer<NumberFormat> consumer) {
        consumer.accept(numberFormat);
        return this;
    }

    /**
     * Sets a {@link NumberFormat} to be used for formatting the value in the input field. Modifying the formatter
     * returned from {@link #getNumberFormat()} should be sufficient in most cases, call this only when you need to use
     * a completely different formatter.
     */
    public NumericWidget setNumberFormat(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
        if (isGTNHLibLoaded) {
            ctx.setNumberFormat(numberFormat);
        }
        return this;
    }

    /**
     * Sets a supplier of numeric values to display in the input field.
     */
    public NumericWidget setGetter(DoubleSupplier getter) {
        this.getter = getter;
        return this;
    }

    /**
     * Sets a consumer of values entered by the player.
     */
    public NumericWidget setSetter(DoubleConsumer setter) {
        this.setter = setter;
        return this;
    }

    /**
     * Sets a validator for entered values. For simply restricting the value to a certain range, use
     * {@link #setMinValue(double)} and {@link #setMaxValue(double)}.
     */
    public NumericWidget setValidator(DoubleUnaryOperator validator) {
        this.validator = validator;
        return this;
    }

    /* Event handlers. */

    @Override
    public void onRemoveFocus() {
        super.onRemoveFocus();

        double newValue = parseValueFromTextField();
        if (validateAndSetValue(newValue)) {
            if (setter != null) {
                setter.accept(value);
            }
            if (syncsToServer()) {
                syncToServer(1, buffer -> buffer.writeDouble(value));
            }
        }
    }

    @Override
    public boolean onMouseScroll(int direction) {
        if (!isFocused()) return false;

        double newValue = parseValueFromTextField();

        if (Interactable.hasControlDown()) newValue += direction * scrollStepCtrl;
        else if (Interactable.hasShiftDown()) newValue += direction * scrollStepShift;
        else newValue += direction * scrollStep;

        if (validateAndSetValue(newValue)) {
            if (setter != null) {
                setter.accept(value);
            }
            if (syncsToServer()) {
                syncToServer(1, buffer -> buffer.writeDouble(value));
            }
        }
        return true;
    }

    /* ISyncedWidget implementation. */

    private boolean needsUpdate;
    private boolean syncsToServer = true;
    private boolean syncsToClient = true;

    /**
     * @return if this widget should operate on the server side. For example detecting and sending changes to client.
     */
    public boolean syncsToClient() {
        return syncsToClient;
    }

    /**
     * @return if this widget should operate on the client side. For example, sending a changed value to the server.
     */
    public boolean syncsToServer() {
        return syncsToServer;
    }

    /**
     * Determines how this widget should sync values
     *
     * @param syncsToClient if this widget should sync changes to the server
     * @param syncsToServer if this widget should detect changes on server and sync them to client
     */
    public NumericWidget setSynced(boolean syncsToClient, boolean syncsToServer) {
        this.syncsToClient = syncsToClient;
        this.syncsToServer = syncsToServer;
        return this;
    }

    @Override
    public void detectAndSendChanges(boolean init) {
        if (syncsToClient() && getter != null) {
            double newValue = getter.getAsDouble();

            // Order matters here, validateAndSetValue() has side effects, it needs to be evaluated first so that it
            // does not get short-circuited.
            if (validateAndSetValue(newValue) || init) {
                syncToClient(1, buffer -> {
                    buffer.writeBoolean(init);
                    buffer.writeDouble(value);
                });
                markForUpdate();
            }
        }
    }

    @Override
    public void readOnClient(int id, PacketBuffer buf) {
        if (id == 1) {
            boolean init = buf.readBoolean();
            if (init || !isFocused()) {
                validateAndSetValue(buf.readDouble());
                if (init) {
                    lastText = new ArrayList<>(handler.getText());
                    if (focusOnGuiOpen) {
                        forceFocus();
                    }
                }
                if (this.setter != null && (this.getter == null || this.getter.getAsDouble() != value)) {
                    this.setter.accept(value);
                }
            }
        }
    }

    @Override
    public void readOnServer(int id, PacketBuffer buf) {
        if (id == 1) {
            if (validateAndSetValue(buf.readDouble())) {
                if (this.setter != null) {
                    this.setter.accept(value);
                }
                markForUpdate();
            }
        }
    }

    @Override
    public void markForUpdate() {
        needsUpdate = true;
    }

    @Override
    public void unMarkForUpdate() {
        needsUpdate = false;
    }

    @Override
    public boolean isMarkedForUpdate() {
        return needsUpdate;
    }

}
