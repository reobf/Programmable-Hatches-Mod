package reobf.proghatches.oc;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockGTHatchConfigurator extends BlockContainer {

    public BlockGTHatchConfigurator(Material p_i45386_1_) {
        super(p_i45386_1_);
        setBlockName("proghatches.configurator");

    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {

        return new TileGTHatchConfigurator();
    }

    /*
     * @SideOnly(Side.CLIENT) IIcon icon;
     * @SideOnly(Side.CLIENT)
     * @Override
     * public IIcon getIcon(int side, int meta) {
     * if(icon!=null)return icon;
     * Block b=GameRegistry.findBlock("OpenComputers", "raid");
     * return icon=b.getIcon(ForgeDirection.UP.ordinal(), 0);
     * }
     */
}
