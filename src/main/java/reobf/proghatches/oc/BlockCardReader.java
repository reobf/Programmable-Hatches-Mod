package reobf.proghatches.oc;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.UIInfos;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCardReader extends BlockContainer {

    public BlockCardReader(Material p_i45386_1_) {
        super(p_i45386_1_);
        setBlockName("proghatches.card_reader");

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

        return new TileCardReader();
    }

    @SideOnly(Side.CLIENT)
    IIcon icon;
    
    @Override
    @SideOnly(Side.CLIENT)
    protected String getTextureName() {
        return this.textureName = "proghatches:cardreader";
    }

    
   /* @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (icon != null) return icon;
        Block b = GameRegistry.findBlock("OpenComputers", "raid");
        return icon = b.getIcon(ForgeDirection.UP.ordinal(), 0);
    }*/

    Random field_149955_b = new Random();

    public void breakBlock(World worldIn, int x, int y, int z, Block blockBroken, int meta) {
        TileCardReader tileentitychest = (TileCardReader) worldIn.getTileEntity(x, y, z);

        if (tileentitychest != null) {
            for (int i1 = 0; i1 < tileentitychest.getSizeInventory(); ++i1) {
                ItemStack itemstack = tileentitychest.getStackInSlot(i1);

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
