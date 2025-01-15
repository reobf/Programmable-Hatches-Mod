package reobf.proghatches.ae;

import java.util.List;

import javax.annotation.Nullable;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.tile.TileFluidAutoFiller;
import com.glodblock.github.inventory.AeItemStackHandler;
import com.glodblock.github.inventory.AeStackInventory;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.inventory.slot.ISlotFluid;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.slot.AppEngSlot;
import appeng.container.slot.SlotFake;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import reobf.proghatches.block.INameAndTooltips;

public class BlockAutoFillerMKII extends BlockContainer implements INameAndTooltips{

	public BlockAutoFillerMKII() {
		super(Material.rock);
		 this.setLightOpacity(255);
	        this.setLightLevel(0);
	        this.setHardness(2.2F);
	        this.setHarvestLevel("pickaxe", 0);
	        setBlockName("proghatches.autofillerMKII");
			setBlockTextureName(FluidCraft.MODID + ":" + NameConst.BLOCK_FLUID_AUTO_FILLER);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
	
		return new TileAutoFillerMKII();
	}
@Override
public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
        float hitY, float hitZ) {
    if (player.isSneaking()) {
        return false;
    }
    TileAutoFillerMKII tile =(TileAutoFillerMKII) world. getTileEntity( x, y, z);
    if (tile != null) {
        if (Platform.isServer()) {
            if (Util.hasPermission(player, SecurityPermissions.INJECT, tile)) {
                InventoryHandler.openGui(
                        player,
                        world,
                        new BlockPos(x, y, z),
                        ForgeDirection.getOrientation(facing),
                        GuiType.FLUID_AUTO_FILLER);
            } else {
                player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
            }
        }
        return true;
    }
    return false;
}

@Override
public void addInformation(ItemStack p_77624_1_, List l) {
	l.add(StatCollector.translateToLocal("tile.proghatches.autofillerMKII.tooltip.0"));
	l.add(StatCollector.translateToLocal("tile.proghatches.autofillerMKII.tooltip.1"));
}

@Override
public String getName(ItemStack p_77624_1_) {
	// TODO Auto-generated method stub
	return null;
}


}
