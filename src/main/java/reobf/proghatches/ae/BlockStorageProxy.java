package reobf.proghatches.ae;

import java.util.List;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.common.blocks.GT_Material_Machines;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import reobf.proghatches.block.INameAndTooltips;
import reobf.proghatches.block.TileIOHub;
import reobf.proghatches.lang.LangManager;

public class BlockStorageProxy  extends BlockContainer implements INameAndTooltips {
	@SideOnly(Side.CLIENT)
	private IIcon blockIconAlt;
	@SideOnly(Side.CLIENT)
	private IIcon blockIconAlt2;
	public BlockStorageProxy() {
		
		super(new GT_Material_Machines());
		
		setHardness(1);
		setHarvestLevel("pickaxe", 1);
		setBlockName("proghatch.storageproxy");
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {

		TileStorageProxy te=new TileStorageProxy();
		 if(meta==1)te.fluid=true;
		 if(meta==2)te.noAdvConfig=true;
		 return te;
	}

	@Override
	@SideOnly(Side.CLIENT)
	protected String getTextureName() {
		return this.textureName = "proghatches:proxy_item";
	}
	@SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg)
    {super.registerBlockIcons(reg);
        this.blockIconAlt = reg.registerIcon("proghatches:proxy_fluid");
        this.blockIconAlt2 = reg.registerIcon("proghatches:proxy_item_adv");
    }
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int meta) {
		if(meta==1)return blockIconAlt;
		if(meta==0)return blockIconAlt2;
		return super.getIcon(side, meta);
	}

	@Override
	public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

		super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
		try{
		((TileStorageProxy)worldIn.getTileEntity(x, y, z)
		).getProxy().setOwner((EntityPlayer) placer);
		}catch(Exception e){e.printStackTrace();}
	}
	@Override
	public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
			float subY, float subZ) {
	
	
			UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
		

		return true;
	}  
	 @SideOnly(Side.CLIENT)
	    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list)
	    {
	        list.add(new ItemStack(itemIn, 1, 0));
	        list.add(new ItemStack(itemIn, 1, 1));
	        list.add(new ItemStack(itemIn, 1, 2));
	    }

	@Override
	public void addInformation(ItemStack p_77624_1_, List l) {
		int i = 0;
		
		
		while (true) {
			
			String k ="tile.proghatches.proxy.tooltip"+"."+p_77624_1_.getItemDamage();

			if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
				break;
			}
			String key = k + "." + i;
			String trans = LangManager.translateToLocal(key);

			l.add(trans);
			i++;

		}
		
		
	}
	public int damageDropped(int meta)
    {
        return meta;
    }
@Override
public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
	ItemStack is=super.getPickBlock(target, world, x, y, z, player);
	is.setItemDamage(world.getBlockMetadata(x, y, z));
	
			return is;
}
	@Override
	public String getName(ItemStack p_77624_1_) {
		
		return super.getUnlocalizedName()+"."+p_77624_1_.getItemDamage();
	}
}

