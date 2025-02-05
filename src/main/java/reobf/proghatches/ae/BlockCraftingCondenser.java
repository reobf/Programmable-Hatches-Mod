package reobf.proghatches.ae;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import appeng.block.crafting.BlockCraftingUnit;
import appeng.block.crafting.ItemCraftingStorage;
import appeng.client.texture.ExtraBlockTextures;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCraftingCondenser extends BlockCraftingUnit {

    public BlockCraftingCondenser(int tier) {
        this.setTileEntity(TileCraftingCondenser.class);
        setBlockName("proghatches.craftingdumper." + tier);
        setBlockTextureName("?");
        setHardness(1);
        this.tier = tier;
    }

    @Override
    public Class<ItemCraftingStorage> getItemBlockClass() {
        return ItemCraftingStorage.class;
    }

    ExtraBlockTextures[] nfit = { ExtraBlockTextures.BlockCraftingStorage1k, ExtraBlockTextures.BlockCraftingStorage4k,
        ExtraBlockTextures.BlockCraftingStorage16k, ExtraBlockTextures.BlockCraftingStorage64k,
        ExtraBlockTextures.BlockCraftingStorage256k, ExtraBlockTextures.BlockCraftingStorage1024k,
        ExtraBlockTextures.BlockCraftingStorage4096k, ExtraBlockTextures.BlockCraftingStorage16384k,
        ExtraBlockTextures.BlockCraftingStorageSingularity };
    ExtraBlockTextures[] fit = { ExtraBlockTextures.BlockCraftingStorage1kFit,
        ExtraBlockTextures.BlockCraftingStorage4kFit, ExtraBlockTextures.BlockCraftingStorage16kFit,
        ExtraBlockTextures.BlockCraftingStorage64kFit, ExtraBlockTextures.BlockCraftingStorage256kFit,
        ExtraBlockTextures.BlockCraftingStorage1024kFit, ExtraBlockTextures.BlockCraftingStorage4096kFit,
        ExtraBlockTextures.BlockCraftingStorage16384kFit, ExtraBlockTextures.BlockCraftingStorageSingularityFit };
    int[] num = { 1, 4, 16, 64, 256, 1024, 4096, 16384, Integer.MAX_VALUE };

    @Override
    public IIcon getIcon(final int direction, final int metadata) {
        if ((metadata & 8) > 0) {

            return fit[tier].getIcon();

        }

        return nfit[tier].getIcon();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getCheckedSubBlocks(final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks) {
        itemStacks.add(new ItemStack(this, 1, 0));
    }

    int tier;

    public int getSkips() {
        return num[tier];
    }

    public void addTips(List<String> toolTip) {
        toolTip.add(StatCollector.translateToLocalFormatted("proghatches.craftingdumper.tooltip.0", num[tier]));

    }

}
