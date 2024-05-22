package reobf.proghatches.block;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.common.blocks.GT_Material_Machines;

import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockIOHub extends BlockContainer {
	public BlockIOHub() {
		super(new GT_Material_Machines());
		

		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.iohub");
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {

		return new TileIOHub();
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getTextureName() {
		return this.textureName = "proghatches:iohub";
	}

	@Override
	public void registerBlockIcons(IIconRegister reg) {
		me_overlay = reg.registerIcon("proghatches:me_overlay");
		provider_overlay = reg.registerIcon("proghatches:provider");
		provider_active_overlay = reg.registerIcon("proghatches:provider_active");
		provider_in_overlay = reg.registerIcon("proghatches:provider_in");
		provider_in_active_overlay = reg.registerIcon("proghatches:provider_in_active");
		super.registerBlockIcons(reg);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
		// TODO Auto-generated method stub
		super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
	}

	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
			float subY, float subZ) {
		TileEntity te = worldIn.getTileEntity(x, y, z);
		/*
		 * boolean suc= ((TileIOHub) te) .onRightclick(player,
		 * ForgeDirection.getOrientation(side), subX, subY, subZ); if(suc)return
		 * suc;
		 */
		final ItemStack is = player.inventory.getCurrentItem();
		if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
			if (ForgeEventFactory.onItemUseStart(player, is, 1) <= 0)
				return false;

			// System.out.println(te);
			player.openGui(AppEng.instance(), GuiBridge.GUI_RENAMER.ordinal() << 5 | (side), te.getWorldObj(),
					te.xCoord, te.yCoord, te.zCoord);
			return true;
		}
		b: {
			if (NetworkUtils.isClient())
				break b;
			UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
		}

		return true;
	}

	// dirty hack: load all textures in this Block
	public static IIcon me_overlay;
	public static IIcon provider_overlay;
	public static IIcon provider_active_overlay;
	public static IIcon provider_in_overlay;
	public static IIcon provider_in_active_overlay;
	static public int magicNO_provider_overlay = 0x7e;
	static public int magicNO_provider_active_overlay = 0x7d;
	static public int magicNO_provider_in_overlay = 0x7c;
	static public int magicNO_provider_in_active_overlay = 0x7b;

	@SideOnly(value = Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int meta) {
		//spotless:off
		if(meta==0x7f)return me_overlay;
		if(meta==0x7e)return provider_overlay;
		if(meta==0x7d)return provider_active_overlay;
		if(meta==0x7c)return provider_in_overlay;
		if(meta==0x7b)return provider_in_active_overlay;
		//spotless:on
		return super.getIcon(side, meta);
	}
}
