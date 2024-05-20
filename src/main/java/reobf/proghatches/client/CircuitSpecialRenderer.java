package reobf.proghatches.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

@SideOnly(Side.CLIENT)
public class CircuitSpecialRenderer implements IItemRenderer {

	private final RenderItem ri = new RenderItem();

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		if (item.getTagCompound() == null)
			return false;
		return true;

	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType aType, ItemStack item, ItemRendererHelper helper) {

		return aType == IItemRenderer.ItemRenderType.ENTITY || aType == IItemRenderer.ItemRenderType.EQUIPPED;

	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		ItemStack is = ItemProgrammingCircuit.getCircuit(item).orElse(null);
		if (type == ItemRenderType.INVENTORY) {

			ri.zLevel -= 2f;
			Minecraft mc = Minecraft.getMinecraft();
			if (is != null) {
				GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
				RenderHelper.enableGUIStandardItemLighting();
				GL11.glPushMatrix();

				// GL11.glScaled(15/16f,15/16f, 1);
				// GL11.glTranslated(2/16f,2/16f, 0);

				this.ri.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), is, 0, 0);
				GL11.glPopMatrix();
				RenderHelper.disableStandardItemLighting();
				GL11.glPopAttrib();
			}

			ri.zLevel += 2f;

			ri.zLevel -= 1f;

			is = new ItemStack(MyMod.progcircuit);
			mc = Minecraft.getMinecraft();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
			RenderHelper.enableGUIStandardItemLighting();

			this.ri.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), is, 0, 0);

			RenderHelper.disableStandardItemLighting();
			GL11.glPopAttrib();

			ri.zLevel += 1f;
			return;
		}

		GL11.glPushMatrix();
		if (type == ItemRenderType.ENTITY) {
			GL11.glRotated(90, 0, 1, 0);
			GL11.glTranslated(-0.5D, -0.42D, 0.0D);

		}

		IIcon tIcon;

		if (is != null) {

			// int i=GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

			// GL11.glBindTexture(GL11.GL_TEXTURE_2D, i);
			is = new ItemStack(MyMod.progcircuit);
			tIcon = ((ItemProgrammingCircuit) is.getItem()).def;
			ItemRenderer.renderItemIn2D(Tessellator.instance, tIcon.getMaxU(), tIcon.getMinV(), tIcon.getMinU(),
					tIcon.getMaxV(), tIcon.getIconWidth(), tIcon.getIconHeight(), 0.0625F);

		}

		is = new ItemStack(MyMod.progcircuit);
		tIcon = is.getItem().getIcon(is, 0);

		ItemRenderer.renderItemIn2D(Tessellator.instance, tIcon.getMaxU(), tIcon.getMinV(), tIcon.getMinU(),
				tIcon.getMaxV(), tIcon.getIconWidth(), tIcon.getIconHeight(), 0.0625F);

		GL11.glPopMatrix();

	}

}
