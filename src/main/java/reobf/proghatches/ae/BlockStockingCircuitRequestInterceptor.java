package reobf.proghatches.ae;

import java.util.List;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import reobf.proghatches.block.INameAndTooltips;

public class BlockStockingCircuitRequestInterceptor extends BlockContainer implements INameAndTooltips {

    public BlockStockingCircuitRequestInterceptor() {

        super(Material.rock);
        setHardness(1);
        setHarvestLevel("pickaxe", 1);
        setBlockName("proghatches.circuit_interceptor");
        setBlockTextureName("proghatches:circuit_interceptor");
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (worldIn.getBlockMetadata(x, y, z) == 1) {
            if (!worldIn.isRemote) {
                TileStockingCircuitRequestInterceptor te = (TileStockingCircuitRequestInterceptor) worldIn
                    .getTileEntity(x, y, z);
                te.mark[0] = player.getCurrentEquippedItem();
                if (te.mark[0] != null) te.mark[0] = te.mark[0].copy();
            } else {
                if (player.getCurrentEquippedItem() != null) player.addChatMessage(
                    new ChatComponentTranslation(
                        "tile.proghatches.circuit_interceptor.change",
                        player.getCurrentEquippedItem()
                            .getDisplayName()));
                else {
                    player.addChatMessage(
                        new ChatComponentTranslation("tile.proghatches.circuit_interceptor.change", "None"));

                }

            }

            return true;
        }

        return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
    }

    @Override
    public int damageDropped(int meta) {

        return (meta);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileStockingCircuitRequestInterceptor(meta);
    }

    @Override
    public void addInformation(ItemStack p_77624_1_, List l) {

        if (p_77624_1_.getItemDamage() == 1) {
            l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.1.0"));
            l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.1.1"));

        } else {
            l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.0"));
            l.add(StatCollector.translateToLocal("proghatch.circuit_interceptor.tooltip.1"));
        }

    }

    @Override
    public String getName(ItemStack p_77624_1_) {
        if (p_77624_1_.getItemDamage() == 1) {
            return "tile.proghatches.circuit_interceptor.1";
        }
        return null;
    }

}
