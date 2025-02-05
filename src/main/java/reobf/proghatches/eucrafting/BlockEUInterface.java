package reobf.proghatches.eucrafting;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.core.features.AEFeature;
import appeng.core.features.ActivityState;
import appeng.core.features.BlockStackSrc;
import appeng.tile.AEBaseTile;
import appeng.tile.misc.TileInterface;
import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTModHandler;
import gregtech.api.util.GTUtility;

/*
 * Eclipse says:
 * Duplicate methods named getSubBlocks with the parameters (Item, CreativeTabs, List<ItemStack>) and (Item,
 * CreativeTabs, List<ItemStack>) are defined by the type AEBaseBlock
 * replace actual superclass in coremod
 */
public class BlockEUInterface extends DummySuper
// appeng.block.AEBaseTileBlock
{

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

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister i) {
        super.registerBlockIcons(i);
        this.blockIcon = i.registerIcon("proghatches:eu_interface");
        this.back = i.registerIcon("proghatches:eu_interface_a");
        this.arr = i.registerIcon("proghatches:eu_interface_arrow");

    }

    @Override
    public String getTextureName() {

        return "proghatches:eu_interface";
    }

    @SideOnly(Side.CLIENT)
    protected BaseBlockRender<AEBaseBlock, TileFluidInterface_EU> getRenderer() {
        return new BaseBlockRender<AEBaseBlock, TileFluidInterface_EU>(false, 20) {

            @Override
            public boolean renderInWorld(final AEBaseBlock block, final IBlockAccess world, final int x, final int y,
                final int z, final RenderBlocks renderer) {
                final TileInterface ti = (TileInterface) ((BlockEUInterface) (Object) block)
                    .getTileEntity(world, x, y, z);
                final BlockRenderInfo info = block.getRendererInstance();

                if (ti != null && ti.getForward() != ForgeDirection.UNKNOWN) {
                    final IIcon side = arr;
                    info.setTemporaryRenderIcons(back, block.getIcon(0, 0), side, side, side, side);
                }

                final boolean fz = super.renderInWorld(block, world, x, y, z, renderer);

                info.setTemporaryRenderIcon(null);

                return fz;
            }
        };

    };

    public boolean onActivated(final World world, final int x, final int y, final int z, final EntityPlayer player,
        final int facing, final float hitX, final float hitY, final float hitZ) {
        if (player.getHeldItem() != null
            && (GTUtility.isStackInList(player.getHeldItem(), GregTechAPI.sScrewdriverList))
            && (GTModHandler.damageOrDechargeItem(player.getHeldItem(), 1, 200, player))) {

            b: {
                if (NetworkUtils.isClient()) break b;
                UIInfos.TILE_MODULAR_UI.open(player, world, x, y, z);
            }
            return true;
        }
        final TileInterface tg = (TileInterface) this.getTileEntity(world, x, y, z);
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
        super.setBlockName(name);
        // setFullBlock(true);
        // setOpaque(true);
        setTileEntity(TileFluidInterface_EU.class);
        setFeature(EnumSet.of(AEFeature.Core));
        // this.setBlockTextureName(FluidCraft.MODID + ":" + name);
    }

    public void setTileEntity(final Class<? extends TileEntity> clazz) {
        AEBaseTile.registerTileItem(clazz, new BlockStackSrc((Block) (Object) this, 0, ActivityState.Enabled));
        super.setTileEntity(clazz);
    }

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
        return new ItemStack((Block) (Object) this, size);
    }

    public ItemStack stack() {
        return new ItemStack((Block) (Object) this, 1);
    }

}
