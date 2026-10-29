package reobf.proghatches.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.storage.data.IAEStack;
import appeng.items.misc.ItemEncodedPattern;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.gt.metatileentity.chance.ChanceMark;

/** Replaces AE2's encoded pattern renderer: same shift-to-show-output behaviour, plus a % badge on chance patterns. */
@SideOnly(Side.CLIENT)
public class ChancePatternRenderer implements IItemRenderer {

    private final RenderItem ri = new RenderItem();
    private boolean recursive;

    private static boolean shift() {
        return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
    }

    private static IAEStack<?> output(ItemStack item) {
        return item.getItem() instanceof ItemEncodedPattern iep ? iep.getOutputAE(item) : null;
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type) {
        if (recursive || type != ItemRenderType.INVENTORY) return false;
        return ChanceMark.isMarked(item) || shift() && output(item) != null;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
        recursive = true;
        try {
            Minecraft mc = Minecraft.getMinecraft();
            IAEStack<?> out = shift() ? output(item) : null;
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
            RenderHelper.enableGUIStandardItemLighting();
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            if (out != null) out.drawInGui(mc, 0, 0);
            else {
                ItemStack plain = item.copy();
                plain.setTagCompound(null);
                ri.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), plain, 0, 0);
            }
            if (ChanceMark.isMarked(item)) {
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                GL11.glColor4f(1, 1, 1, 1);
                mc.fontRenderer.drawStringWithShadow("%", 16 - mc.fontRenderer.getStringWidth("%"), 0, 0xFFE63C);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
            GL11.glPopAttrib();
        } finally {
            recursive = false;
        }
    }
}
