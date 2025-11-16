package reobf.proghatches.ae;

import java.util.HashSet;
import java.util.List;
import java.util.NavigableMap;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;

import appeng.api.config.SecurityPermissions;
import appeng.util.Platform;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import kekztech.common.tileentities.MTEHatchTFFT;
import reobf.proghatches.block.INameAndTooltips;

public class BlockAutoFillerMKII extends BlockContainer implements INameAndTooltips {

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
        return null;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        if (worldIn.isRemote == false) if (placer instanceof EntityPlayer) {
            if (!(placer instanceof FakePlayer))
                ((TileAutoFillerMKII) worldIn.getTileEntity(x, y, z)).mark((EntityPlayer) placer);
        }

    }
}
