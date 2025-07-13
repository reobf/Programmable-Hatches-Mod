package reobf.proghatches.ae;

import static net.minecraft.client.gui.GuiScreen.isShiftKeyDown;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.INameAndTooltips;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.block.FCBaseBlock;
import com.glodblock.github.common.item.FCBaseItemBlock;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.common.tile.TileFluidDiscretizer;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.RenderUtil;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.block.AEBaseTileBlock;
import appeng.util.Platform;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFluidDiscretizerMKII  extends BlockContainer implements INameAndTooltips {

    public BlockFluidDiscretizerMKII() {
        super(Material.rock);
        this.setLightOpacity(255);
        this.setLightLevel(0);
        this.setHardness(2.2F);
        this.setHarvestLevel("pickaxe", 0);
        setBlockName("proghatches.fluidDiscretizerMKII");
        setBlockTextureName(FluidCraft.MODID + ":" + NameConst.BLOCK_FLUID_AUTO_FILLER);

    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

    
        return new TileFluidDiscretizerMKII();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int facing, float hitX,
        float hitY, float hitZ) {
       /* if (player.isSneaking()) {
            return false;
        }
        TileAutoFillerMKII tile = (TileAutoFillerMKII) world.getTileEntity(x, y, z);
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
        }*/
        return false;
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, List l) {
        l.add(StatCollector.translateToLocal("tile.proghatches.fluidDiscretizerMKII.tooltip.0"));
        l.add(StatCollector.translateToLocal("tile.proghatches.fluidDiscretizerMKII.tooltip.1"));
        l.add(StatCollector.translateToLocal("tile.proghatches.fluidDiscretizerMKII.tooltip.2"));
    }

    @Override
    public String getName(ItemStack p_77624_1_) {
        return null;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        if (worldIn.isRemote == false) if (placer instanceof EntityPlayer) {
            if (!(placer instanceof FakePlayer))
                ((TileFluidDiscretizerMKII) worldIn.getTileEntity(x, y, z)).mark((EntityPlayer) placer);
        }

    }
}
