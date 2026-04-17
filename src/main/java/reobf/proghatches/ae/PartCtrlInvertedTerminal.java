package reobf.proghatches.ae;

import java.util.List;

import com.glodblock.github.client.textures.FCPartsTexture;

import appeng.api.AEApi;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.ITerminalHost;
import appeng.client.texture.CableBusTextures;
import appeng.parts.reporting.PartCraftingTerminal;
import appeng.parts.reporting.PartPanel;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

public class PartCtrlInvertedTerminal extends PartCraftingTerminal implements ICtrlInverted,ITerminalHost{

	
	static ItemStack part=AEApi.instance().parts().partTerminal.stack(1);
	
	public PartCtrlInvertedTerminal(ItemStack is) {
		super(is);
		
	}

	@Override
	public boolean invert() {
		return true;
	}
	 private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidTerminal_Bright;
	    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidTerminal_Colored;
	    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidTerminal_Dark;



	    @Override
	    @SideOnly(Side.CLIENT)
	    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
	        rh.setBounds(2, 2, 14, 14, 14, 16);

	        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
	        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

	        rh.setTexture(
	                sideTexture,
	                sideTexture,
	                backTexture,
	                part.getIconIndex(),
	                sideTexture,
	                sideTexture);
	        rh.renderInventoryBox(renderer);

	        rh.setInvColor(this.getColor().whiteVariant);
	        rh.renderInventoryFace(FRONT_BRIGHT_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        rh.setInvColor(this.getColor().mediumVariant);
	        rh.renderInventoryFace(FRONT_DARK_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        rh.setInvColor(this.getColor().blackVariant);
	        rh.renderInventoryFace(FRONT_COLORED_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        rh.setBounds(4, 4, 13, 12, 12, 14);
	        rh.renderInventoryBox(renderer);
	    }

	    @Override
	    @SideOnly(Side.CLIENT)
	    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
	            final RenderBlocks renderer) {
	        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));

	        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
	        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

	        rh.setTexture(
	                sideTexture,
	                sideTexture,
	                backTexture,
	                part.getIconIndex(),
	                sideTexture,
	                sideTexture);

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderBlock(x, y, z, renderer);

	        final Tessellator tess = Tessellator.instance;
	        if (this.getLightLevel() > 0) {
	            final int l = 13;
	            tess.setBrightness(l << 20 | l << 4);
	        }

	        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this
	                .getSpin();

	        tess.setColorOpaque_I(this.getColor().whiteVariant);
	        rh.renderFace(x, y, z, FRONT_BRIGHT_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        tess.setColorOpaque_I(this.getColor().mediumVariant);
	        rh.renderFace(x, y, z, FRONT_DARK_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        tess.setColorOpaque_I(this.getColor().blackVariant);
	        rh.renderFace(x, y, z, FRONT_COLORED_ICON.getIcon(), ForgeDirection.SOUTH, renderer);

	        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

	        final IIcon sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

	        rh.setTexture(
	                sideStatusTexture,
	                sideStatusTexture,
	                backTexture,
	                part.getIconIndex(),
	                sideStatusTexture,
	                sideStatusTexture);

	        rh.setBounds(4, 4, 13, 12, 12, 14);
	        rh.renderBlock(x, y, z, renderer);

	        final boolean hasChan = (this.getClientFlags() & (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG))
	                == (PartPanel.POWERED_FLAG | PartPanel.CHANNEL_FLAG);
	        final boolean hasPower = (this.getClientFlags() & PartPanel.POWERED_FLAG) == PartPanel.POWERED_FLAG;

	        if (hasChan) {
	            final int l = 14;
	            tess.setBrightness(l << 20 | l << 4);
	            tess.setColorOpaque_I(this.getColor().blackVariant);
	        } else if (hasPower) {
	            final int l = 9;
	            tess.setBrightness(l << 20 | l << 4);
	            tess.setColorOpaque_I(this.getColor().whiteVariant);
	        } else {
	            tess.setBrightness(0);
	            tess.setColorOpaque_I(0x000000);
	        }

	        final IIcon sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.EAST, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.WEST, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.UP, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.DOWN, renderer);
	    } 
	    
	   
}
