package reobf.proghatches.oc;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockGTHatchConfigurator  extends BlockContainer{


	public BlockGTHatchConfigurator(Material p_i45386_1_) {
		super(p_i45386_1_);
		setBlockName("proghatches.configurator");
	
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		return new TileGTHatchConfigurator();
	}
	
	/*
@SideOnly(Side.CLIENT)	IIcon icon;
	
@SideOnly(Side.CLIENT)
@Override
public IIcon getIcon(int side, int meta) {
	if(icon!=null)return icon;
	Block b=GameRegistry.findBlock("OpenComputers", "raid");
	return icon=b.getIcon(ForgeDirection.UP.ordinal(), 0);
}*/
}

