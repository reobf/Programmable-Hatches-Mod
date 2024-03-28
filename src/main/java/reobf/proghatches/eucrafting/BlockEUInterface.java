package reobf.proghatches.eucrafting;

import java.util.EnumSet;
import java.util.List;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.render.RenderBlockFluidInterface;
import com.glodblock.github.common.block.BlockFluidInterface;
import com.glodblock.github.common.block.FCBaseBlock;
import com.glodblock.github.common.tile.TileFluidInterface;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import cpw.mods.fml.common.Optional.Interface;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.block.BlockFluidInterface;
import com.glodblock.github.common.tile.TileFluidInterface;

public class BlockEUInterface extends AEBaseTileBlock{



	 private IIcon back;
	private IIcon arr;



	@Override
	    protected boolean hasCustomRotation() {
	        return true;
	    }

	    @Override
	    protected void customRotateBlock(final IOrientable rotatable, final ForgeDirection axis) {
	        if (rotatable instanceof TileInterface) {
	            ((TileInterface) rotatable).setSide(axis);
	        }
	    }
@Override@SideOnly(Side.CLIENT)
public void registerBlockIcons(IIconRegister i) {
	super.registerBlockIcons(i);
	this.blockIcon = i.registerIcon("proghatches:eu_interface");
	this.back = i.registerIcon("proghatches:eu_interface_a");
	this.arr = i.registerIcon("proghatches:eu_interface_arrow");
	
}@Override
public String getTextureName() {
	
	return "proghatches:eu_interface";
}

	    @Override
	    @SideOnly(Side.CLIENT)
	    protected BaseBlockRender<BlockEUInterface, TileFluidInterface_EU> getRenderer() {
	        return new BaseBlockRender<BlockEUInterface, TileFluidInterface_EU>(false, 20){
	        	
	        	

	        	   

	        	    @Override
	        	    public boolean renderInWorld(final BlockEUInterface block, final IBlockAccess world, final int x, final int y,
	        	            final int z, final RenderBlocks renderer) {
	        	        final TileInterface ti = block.getTileEntity(world, x, y, z);
	        	        final BlockRenderInfo info = block.getRendererInstance();

	        	        if (ti != null && ti.getForward() != ForgeDirection.UNKNOWN) {
	        	            final IIcon side = arr;
	        	            info.setTemporaryRenderIcons(
	        	                   back,
	        	                    block.getIcon(0, 0),
	        	                    side,
	        	                    side,
	        	                    side,
	        	                    side);
	        	        }

	        	        final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);

	        	        info.setTemporaryRenderIcon(null);

	        	        return fz;
	        	    }
	        	};

	        };
	    

	    @Override
	    public boolean onActivated(final World world, final int x, final int y, final int z, final EntityPlayer player,
	            final int facing, final float hitX, final float hitY, final float hitZ) {
	        if (player.isSneaking()) {
	           
	        	
	        	b:{
	        		if (NetworkUtils.isClient()) break b;
	        		UIInfos.TILE_MODULAR_UI.open(player, world, x, y, z);
	  	        }return true;
	        }
	        final TileInterface tg = this.getTileEntity(world, x, y, z);
	        if (tg != null) {
	            if (Platform.isServer()) {
	                InventoryHandler.openGui(
	                        player,
	                        world,
	                        new BlockPos(x, y, z),
	                        ForgeDirection.getOrientation(facing),
	                        GuiType.DUAL_INTERFACE);
	            }
	            return true;
	        }
	        return false;
	    }
	  public BlockEUInterface(Material mat, String name) {
	        super(mat);
	        this.setBlockName(name);
	        setFullBlock(true);
	        setOpaque(true);
	        setTileEntity(TileFluidInterface_EU.class);
	        setFeature(EnumSet.of(AEFeature.Core));
	      //  this.setBlockTextureName(FluidCraft.MODID + ":" + name);
	    }

	    @Override
	    public void setTileEntity(final Class<? extends TileEntity> clazz) {
	        AEBaseTile.registerTileItem(clazz, new BlockStackSrc(this, 0, ActivityState.Enabled));
	        super.setTileEntity(clazz);
	    }

	    public void setOpaque(boolean opaque) {
	        this.isOpaque = opaque;
	    }

	    public void setFullBlock(boolean full) {
	        this.isFullSize = full;
	    }

	    @Override
	    public void setFeature(final EnumSet<AEFeature> f) {
	        super.setFeature(f);
	    }

	    @SideOnly(Side.CLIENT)
	    public void addInformation(final ItemStack itemStack, final EntityPlayer player, final List<String> toolTip,
	            final boolean advancedToolTips) {}

	    public void addCheckedInformation(ItemStack itemStack, EntityPlayer player, List<String> toolTip,
	            boolean advancedToolTips) {
	        this.addInformation(itemStack, player, toolTip, advancedToolTips);
	    }

	    public ItemStack stack(int size) {
	        return new ItemStack(this, size);
	    }

	    public ItemStack stack() {
	        return new ItemStack(this, 1);
	    }
	

	
@Override
	public void addCollisionBoxesToList(World w, int x, int y, int z, AxisAlignedBB bb, List out,
			Entity e) {
		// TODO Auto-generated method stub
		super.addCollisionBoxesToList(w, x, y, z, bb, out, e);
	}


}
