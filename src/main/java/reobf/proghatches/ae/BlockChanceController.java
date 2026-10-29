package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.cleanroommc.modularui.factory.GuiFactories;

import reobf.proghatches.block.INameAndTooltips;

public class BlockChanceController extends BlockContainer implements INameAndTooltips {

    /** The data of the controller broken last, for the drop that follows its removal. */
    private static final ThreadLocal<Object[]> broken = new ThreadLocal<>();

    public BlockChanceController() {
        super(Material.rock);
        setHardness(1);
        setHarvestLevel("pickaxe", 1);
        setBlockName("proghatches.chance_controller");
        setBlockTextureName("proghatches:chance_controller");
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (!worldIn.isRemote && worldIn.getTileEntity(x, y, z) instanceof TileChanceController)
            GuiFactories.tileEntity()
                .open(player, x, y, z);
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileChanceController();
    }

    @Override
    public void addInformation(ItemStack stack, List l) {
        for (int i = 0; i < 7; i++) l.add(StatCollector.translateToLocal("proghatch.chance_controller.tooltip." + i));
        if (stack.hasTagCompound() && stack.getTagCompound()
            .hasKey("phDeposits"))
            l.add(
                StatCollector.translateToLocalFormatted(
                    "proghatch.chance_controller.tooltip.stored",
                    stack.getTagCompound()
                        .getTagList("phDeposits", 10)
                        .tagCount()));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, int x, int y, int z, EntityLivingBase placer, ItemStack itemIn) {
        super.onBlockPlacedBy(worldIn, x, y, z, placer, itemIn);
        if (!(worldIn.getTileEntity(x, y, z) instanceof TileChanceController t)) return;
        if (placer instanceof EntityPlayer p) t.getProxy()
            .setOwner(p);
        if (itemIn.hasTagCompound()) t.loadDropTag(itemIn.getTagCompound());
    }

    @Override
    public void breakBlock(World worldIn, int x, int y, int z, Block block, int meta) {
        if (worldIn.getTileEntity(x, y, z) instanceof TileChanceController t)
            broken.set(new Object[] { worldIn, x, y, z, t.dropTag() });
        super.breakBlock(worldIn, x, y, z, block, meta);
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        NBTTagCompound tag = null;
        if (world.getTileEntity(x, y, z) instanceof TileChanceController t) tag = t.dropTag();
        else {
            Object[] b = broken.get();
            if (b != null && b[0] == world && (int) b[1] == x && (int) b[2] == y && (int) b[3] == z)
                tag = (NBTTagCompound) b[4];
        }
        broken.remove();
        ItemStack s = new ItemStack(this);
        if (tag != null && !tag.hasNoTags()) s.setTagCompound(tag);
        ArrayList<ItemStack> list = new ArrayList<>();
        list.add(s);
        return list;
    }

    @Override
    public String getName(ItemStack stack) {
        return null;
    }
}
