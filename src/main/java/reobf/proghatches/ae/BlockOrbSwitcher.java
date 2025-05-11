package reobf.proghatches.ae;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import gregtech.api.enums.SoundResource;
import gregtech.api.util.GTUtility;
import li.cil.oc.integration.util.Wrench;
import li.cil.oc.util.BlockPosition;
import reobf.proghatches.block.INameAndTooltips;
import scala.Some;

public class BlockOrbSwitcher extends BlockContainer implements INameAndTooltips {

    public BlockOrbSwitcher() {
        super(Material.rock);
        this.setHardness(2.2F);
        this.setHarvestLevel("pickaxe", 0);
        setBlockName("proghatches.orb_switcher");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileOrbSwitcher();
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, List l) {
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.0"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.1"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.2"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.3"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.4"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.5"));
        l.add(StatCollector.translateToLocal("proghatch.orb_switcher.tooltip.6"));
    }

    @Override
    public String getName(ItemStack p_77624_1_) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == 1) {
            return orb;
        }
        if (side == 0) {
            return stocking;
        }
        if (side == 3) {
            return cirucit;
        }
        return blockIcon;
    }

    IIcon cirucit;
    IIcon stocking;
    IIcon orb;

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        cirucit = reg.registerIcon("proghatches:switch_circuit");
        stocking = reg.registerIcon("proghatches:switch_stocking");
        orb = reg.registerIcon("proghatches:switch_orb");
        blockIcon = reg.registerIcon("proghatches:switch");
    }

    @Override
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {

        TileOrbSwitcher tile = (TileOrbSwitcher) worldIn.getTileEntity(x, y, z);
        if (side == tile.dirorb.ordinal()) {
            return orb;
        }
        if (side == tile.dirstocking.ordinal()) {
            return stocking;
        }
        if (side == tile.facing.ordinal()) {
            return cirucit;
        }
        return blockIcon;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        int l = BlockPistonBase.determineOrientation(worldIn, x, y, z, placer);
        // worldIn.setBlockMetadataWithNotify(x, y, z, l, 2);
        TileOrbSwitcher tile = (TileOrbSwitcher) worldIn.getTileEntity(x, y, z);
        tile.facing = ForgeDirection.VALID_DIRECTIONS[l];
        if (l <= 1) {
            tile.dirorb = ForgeDirection.EAST;
            tile.dirstocking = ForgeDirection.WEST;

        }

        if (tile != null && placer instanceof EntityPlayer) tile.mark((EntityPlayer) placer);
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {

        TileOrbSwitcher tile = (TileOrbSwitcher) worldIn.getTileEntity(x, y, z);
        if (tile != null) {
            if (Wrench.holdsApplicableWrench(player, new BlockPosition(x, y, z, Some.apply(worldIn)))) {
                if (!worldIn.isRemote) {
                    GTUtility.sendSoundToPlayers(worldIn, SoundResource.IC2_TOOLS_WRENCH, 1.0F, -1.0F, x, y, z);
                    Wrench.wrenchUsed(player, new BlockPosition(x, y, z, Some.apply(worldIn)));

                    tile.dirorb = tile.dirorb.getRotation(ForgeDirection.VALID_DIRECTIONS[side]);
                    tile.dirstocking = tile.dirstocking.getRotation(ForgeDirection.VALID_DIRECTIONS[side]);
                    tile.facing = tile.facing.getRotation(ForgeDirection.VALID_DIRECTIONS[side]);

                    tile.updateConnection();

                    /*
                     * worldIn.setBlockMetadataWithNotify(x, y, z,
                     * ForgeDirection.VALID_DIRECTIONS[ worldIn.getBlockMetadata(x, y,
                     * z)].getRotation(ForgeDirection.VALID_DIRECTIONS[side])
                     * .ordinal(), 2);
                     */

                    tile.sendPacket();

                }
                return true;
            }
        }

        TileEntity te = worldIn.getTileEntity(x, y, z);
        final ItemStack is = player.inventory.getCurrentItem();

        b: {
            if (NetworkUtils.isClient()) break b;
            UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
        }

        return true;
    }

}
