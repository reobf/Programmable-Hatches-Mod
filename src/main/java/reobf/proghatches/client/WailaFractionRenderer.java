package reobf.proghatches.client;

import java.awt.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcp.mobius.waila.api.IWailaCommonAccessor;
import mcp.mobius.waila.api.IWailaRegistrar;
import mcp.mobius.waila.api.IWailaTooltipRenderer;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.Copies;

/**
 * WAILA tooltip renderer that draws a mixed number with a real stacked fraction: the whole part as
 * normal text, then the remainder's numerator over a bar over the denominator, both at half scale so
 * the stack fits a tooltip line. Requested for issue #332 so "1 and 1/4 copies" reads as 1¼ rather
 * than 1.25.
 * <p>
 * Params, all decimal strings: {@code [whole, numerator, denominator]}; a whole of "0" is omitted.
 * The line fragment is produced by {@link Copies#waila()} through
 * {@code SpecialChars.getRenderString}, the same mechanism WAILA uses for its own item-icon and
 * progress-bar renderers. WAILA translates to the fragment's position before calling {@link #draw},
 * so everything here is drawn relative to (0, 0).
 * <p>
 * Top-level and client-only on purpose: nested {@code @SideOnly} classes get listed in the
 * jvmDowngrader NestMembers annotation of their outer class and blow up a dedicated server (see the
 * note on StockingHookHolder). Registration is behind a physical-side check in CommonProxy.
 */
@SideOnly(Side.CLIENT)
public final class WailaFractionRenderer implements IWailaTooltipRenderer {

    private static final float SCALE = 0.5f;
    private static final int COLOR = 0xFFFFFF;
    /** Gap between the whole part and the fraction, and padding either side of the bar. */
    private static final int GAP = 1;

    public static void register(IWailaRegistrar registrar) {
        registrar.registerTooltipRenderer(Copies.WAILA_RENDERER, new WailaFractionRenderer());
    }

    private static FontRenderer font() {
        return Minecraft.getMinecraft().fontRenderer;
    }

    /** Full-scale pixel width of the stacked part: the wider of numerator/denominator at half scale, plus bar padding. */
    private static int fractionWidth(FontRenderer fr, String num, String den) {
        int w = Math.max(fr.getStringWidth(num), fr.getStringWidth(den));
        return (int) Math.ceil(w * SCALE) + 2 * GAP;
    }

    @Override
    public Dimension getSize(String[] params, IWailaCommonAccessor accessor) {
        FontRenderer fr = font();
        String whole = params.length > 0 ? params[0] : "0";
        String num = params.length > 1 ? params[1] : "0";
        String den = params.length > 2 ? params[2] : "1";
        int w = 0;
        if (!"0".equals(whole)) w += fr.getStringWidth(whole) + GAP;
        w += fractionWidth(fr, num, den);
        // numerator (half a line) + bar + denominator (half a line): a touch taller than one line
        int h = (int) Math.ceil(fr.FONT_HEIGHT * SCALE) * 2 + 1;
        return new Dimension(w, h);
    }

    @Override
    public void draw(String[] params, IWailaCommonAccessor accessor) {
        FontRenderer fr = font();
        String whole = params.length > 0 ? params[0] : "0";
        String num = params.length > 1 ? params[1] : "0";
        String den = params.length > 2 ? params[2] : "1";

        int x = 0;
        int half = (int) Math.ceil(fr.FONT_HEIGHT * SCALE); // height of one half-scale line
        if (!"0".equals(whole)) {
            // Same baseline as the rest of the line: WAILA draws plain text segments at y = 0, so the
            // whole part must sit at y = 0 too. Centring it on the stack put it 2px lower than the
            // neighbouring text, which the forced-unicode font made obvious. The stack then hangs from
            // the top of the digit: numerator level with it, bar through its middle, denominator below
            // the baseline - the usual look of a mixed number.
            fr.drawStringWithShadow(whole, x, 0, COLOR);
            x += fr.getStringWidth(whole) + GAP;
        }

        int fw = fractionWidth(fr, num, den);
        int nw = (int) Math.ceil(fr.getStringWidth(num) * SCALE);
        int dw = (int) Math.ceil(fr.getStringWidth(den) * SCALE);

        GL11.glPushMatrix();
        GL11.glTranslatef(x, 0, 0);
        GL11.glScalef(SCALE, SCALE, 1f);
        // positions are in the scaled frame, hence the division by SCALE
        fr.drawStringWithShadow(num, (int) ((fw - nw) / 2 / SCALE), 0, COLOR);
        fr.drawStringWithShadow(den, (int) ((fw - dw) / 2 / SCALE), (int) ((half + 1) / SCALE), COLOR);
        GL11.glPopMatrix();

        // the fraction bar, full-scale, one pixel tall, between the two halves
        Gui.drawRect(x, half, x + fw, half + 1, 0xFF000000 | COLOR);
        GL11.glColor4f(1f, 1f, 1f, 1f); // drawRect leaves the colour set; the tooltip text after us must not inherit it
    }
}
