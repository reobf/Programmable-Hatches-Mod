package reobf.proghatches.ae;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.block.INameAndTooltips;

public class BlockCyclicPatternSubmitter extends BlockContainer implements INameAndTooltips {

    public BlockCyclicPatternSubmitter(Material p_i45386_1_) {
        super(p_i45386_1_);

        setHardness(1);
        setHarvestLevel("pickaxe", 1);
        setBlockName("proghatch.submitter");
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {

        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        if (worldIn.isRemote == false) if (placer instanceof EntityPlayer) {
            if (!(placer instanceof FakePlayer))
                ((TileCyclicPatternSubmitter) worldIn.getTileEntity(x, y, z)).mark((EntityPlayer) placer);
        }
        int l = determineOrientation(worldIn, x, y, z, placer);
        worldIn.setBlockMetadataWithNotify(x, y, z, l, 2);

    }

    public static int determineOrientation(World p_150071_0_, int p_150071_1_, int p_150071_2_, int p_150071_3_,
        EntityLivingBase p_150071_4_) {
        if (MathHelper.abs((float) p_150071_4_.posX - (float) p_150071_1_) < 2.0F
            && MathHelper.abs((float) p_150071_4_.posZ - (float) p_150071_3_) < 2.0F) {
            double d0 = p_150071_4_.posY + 1.82D - (double) p_150071_4_.yOffset;

            if (d0 - (double) p_150071_2_ > 2.0D) {
                return 1;
            }

            if ((double) p_150071_2_ - d0 > 0.0D) {
                return 0;
            }
        }

        int l = MathHelper.floor_double((double) (p_150071_4_.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        return l == 0 ? 2 : (l == 1 ? 5 : (l == 2 ? 3 : (l == 3 ? 4 : 0)));
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        b: {
            if (NetworkUtils.isClient()) break b;
            UIInfos.TILE_MODULAR_UI.open(player, worldIn, x, y, z);
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileCyclicPatternSubmitter();
    }

    @SideOnly(value = Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {

        if (side == 4) return top;
        return this.side;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess worldIn, int x, int y, int z, int side) {

        if (side == (worldIn.getBlockMetadata(x, y, z) & 0b111)) {
            return

            ((worldIn.getBlockMetadata(x, y, z) & 0b1000) == 0) ? top_off : top;

        }

        return this.side;
    }

    @SideOnly(value = Side.CLIENT)
    IIcon side;
    @SideOnly(value = Side.CLIENT)
    IIcon top;
    @SideOnly(value = Side.CLIENT)
    IIcon top_off;

    @SideOnly(value = Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        side = reg.registerIcon("ae2fc:level_maintainer");
        top = reg.registerIcon("proghatches:submitter");
        top_off = reg.registerIcon("proghatches:submitter_off");
        super.registerBlockIcons(reg);
    }

    /*
     * (non-Javadoc)
     * @see net.minecraft.block.Block#rotateBlock(net.minecraft.world.World, int, int, int,
     * net.minecraftforge.common.util.ForgeDirection)
     */
    @Override
    public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis) {
        int old = worldObj.getBlockMetadata(x, y, z);

        worldObj.setBlockMetadataWithNotify(
            x,
            y,
            z,
            ForgeDirection.values()[old & 0b111].getRotation(axis)
                .ordinal() | (old & 0b1000),
            3);
        return true;

    }

    @Override
    public boolean hasComparatorInputOverride() {

        return true;
    }

    @Override
    public int getComparatorInputOverride(World worldIn, int x, int y, int z, int side) {
        TileEntity te = worldIn.getTileEntity(x, y, z);
        TileCyclicPatternSubmitter ts = (TileCyclicPatternSubmitter) te;

        return (ts.index * 16) / ts.inv.length;
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, List l) {
        l.add(StatCollector.translateToLocal("proghatch.submitter.tooltip.0"));

    }

    @Override
    public String getName(ItemStack p_77624_1_) {
      
        return null;
    }

    Random field_149955_b = new Random();

    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileCyclicPatternSubmitter tileentitychest = (TileCyclicPatternSubmitter) worldIn.getTileEntity(x, y, z);

        if (tileentitychest != null) {
            for (int i1 = 0; i1 < tileentitychest.upgrade.length; ++i1) {
                ItemStack itemstack = tileentitychest.upgrade[i1];

                if (itemstack != null) {
                    float f = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    float f1 = this.field_149955_b.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = this.field_149955_b.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; worldIn
                        .spawnEntityInWorld(entityitem)) {
                        int j1 = this.field_149955_b.nextInt(21) + 10;

                        if (j1 > itemstack.stackSize) {
                            j1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= j1;
                        entityitem = new EntityItem(
                            worldIn,
                            (double) ((float) x + f),
                            (double) ((float) y + f1),
                            (double) ((float) z + f2),
                            new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double) ((float) this.field_149955_b.nextGaussian() * f3);
                        entityitem.motionY = (double) ((float) this.field_149955_b.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double) ((float) this.field_149955_b.nextGaussian() * f3);

                        if (itemstack.hasTagCompound()) {
                            entityitem.getEntityItem()
                                .setTagCompound(
                                    (NBTTagCompound) itemstack.getTagCompound()
                                        .copy());
                        }
                    }
                }
            }

            worldIn.func_147453_f(x, y, z, blockBroken);
        }

        super.breakBlock(worldIn, x, y, z, blockBroken, meta);
    }
}
