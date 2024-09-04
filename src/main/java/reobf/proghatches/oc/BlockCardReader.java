package reobf.proghatches.oc;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

public class BlockCardReader extends BlockContainer{

	public BlockCardReader(Material p_i45386_1_) {
		super(p_i45386_1_);
		setBlockName("proghatches.card_reader");
	
	}
	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
			float subY, float subZ) {
	
	
		b: {
			if (NetworkUtils.isClient())
				break b;
			UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
		}

		return true;
	}
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		return new TileCardReader();
	}
@SideOnly(Side.CLIENT)	IIcon icon;
	
@SideOnly(Side.CLIENT)
@Override
public IIcon getIcon(int side, int meta) {
	if(icon!=null)return icon;
	Block b=GameRegistry.findBlock("OpenComputers", "raid");
	return icon=b.getIcon(ForgeDirection.UP.ordinal(), 0);
}
}
