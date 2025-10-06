package reobf.proghatches.main.mixin.mixins;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

//import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.llamalad7.mixinextras.sugar.Local;

import appeng.util.InventoryAdaptor;
import gregtech.api.interfaces.tileentity.ICoverable;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.CoverToMachineAdaptor;
import reobf.proghatches.eucrafting.ISer;

//@Mixin(FluidConvertingInventoryAdaptor.class)
public class MixinAE2FCCompat {

    /*
     * @Inject(method = "wrap", remap = false, at = { @At("HEAD") }, cancellable = true)
     * private static void wrap(TileEntity capProvider, ForgeDirection face,
     * CallbackInfoReturnable<InventoryAdaptor> ret) {
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
    /*@Inject(
        method = "wrap",
        remap = true,
        at = { @At(
            value = "INVOKE_ASSIGN",
            target = "Lnet/minecraft/world/World;getTileEntity(III)Lnet/minecraft/tileentity/TileEntity;") },
        locals = LocalCapture.CAPTURE_FAILHARD,
        cancellable = true,

        require = 1)*/
    private static void wrap(TileEntity capProvider, ForgeDirection face, CallbackInfoReturnable<InventoryAdaptor> ret,
        @Local(ordinal = 1) TileEntity inter) {

        // System.out.println(face);
        // Thread.dumpStack();
        if (face == ForgeDirection.UNKNOWN) {
            return;
        }

        boolean ispart = false;
        /*
         * ispart = Util.getPart(inter, face.getOpposite()) instanceof PartEUP2PInterface;
         * if ( ispart) {
         * BlockPos pos = new BlockPos(capProvider.xCoord + face.offsetX, capProvider.yCoord + face.offsetY,
         * capProvider.zCoord + face.offsetZ);
         * InventoryAdaptor item = InventoryAdaptor.getAdaptor(capProvider, face);
         * IFluidHandler fluid = capProvider instanceof IFluidHandler ? (IFluidHandler) capProvider : null;
         * boolean onmi = false;
         * ret.setReturnValue(new FluidConvertingInventoryAdaptor(capProvider, item, fluid, face, pos, onmi));
         * }
         */
        boolean iscover = check(capProvider, face);
        if (iscover) {
            InventoryAdaptor item = InventoryAdaptor.getAdaptor(capProvider, face);
            IFluidHandler fluid = capProvider instanceof IFluidHandler ? (IFluidHandler) capProvider : null;

            ret.setReturnValue(new CoverToMachineAdaptor(capProvider, item, fluid, face));
        } ;
    }

    private static boolean check(TileEntity te, ForgeDirection face) {

        if (te instanceof ICoverable) {
            ICoverable c = (ICoverable) te;
            ISer data = (ISer) AECover.getCoverData(c.getCoverAtSide(face));
            if (data instanceof AECover.Data) {
                return ((AECover.Data) data).supportFluid();
            }

            /*
             * Optional<ISerializableObject> op = Optional.ofNullable(c.getComplexCoverDataAtSide(face));
             * return op.filter(s -> (s instanceof AECover.Data) && ((AECover.Data) s).supportFluid())
             * .isPresent();
             */

        }

        return false;
    }

}
