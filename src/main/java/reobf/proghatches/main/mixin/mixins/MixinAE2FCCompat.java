package reobf.proghatches.main.mixin.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;

import appeng.util.InventoryAdaptor;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.util.ISerializableObject;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.PartEUP2PInterface;

@Mixin(FluidConvertingInventoryAdaptor.class)
public class MixinAE2FCCompat {

	@Inject(method = "wrap", remap = false, at = { @At("HEAD") }, cancellable = true)
	private static void wrap(TileEntity capProvider, ForgeDirection face,
			CallbackInfoReturnable<InventoryAdaptor> ret) {
		if (face == ForgeDirection.UNKNOWN) {
			return;
		}
		boolean iscover = (check(capProvider, face));
		boolean ispart = false;
		if (iscover == false) {
			TileEntity inter = capProvider.getWorldObj().getTileEntity(capProvider.xCoord + face.offsetX,
					capProvider.yCoord + face.offsetY, capProvider.zCoord + face.offsetZ);
			ispart = Util.getPart(inter, face.getOpposite()) instanceof PartEUP2PInterface;
		}

		/*
		 * System.out.println(inter);
		 */
		// System.out.println(face);
		if (iscover || ispart) {

			BlockPos pos = new BlockPos(capProvider.xCoord + face.offsetX, capProvider.yCoord + face.offsetY,
					capProvider.zCoord + face.offsetZ);

			InventoryAdaptor item = InventoryAdaptor.getAdaptor(capProvider, face);
			IFluidHandler fluid = capProvider instanceof IFluidHandler ? (IFluidHandler) capProvider : null;
			boolean onmi = false;

			ret.setReturnValue(new FluidConvertingInventoryAdaptor(capProvider, item, fluid, face, pos, onmi));

		}

	}

	private static boolean check(TileEntity te, ForgeDirection face) {

		// TileEntity te=b.world.getTileEntity(b.x, b.y, b.z);
		if (te instanceof ICoverable) {
			ICoverable c = (ICoverable) te;
			{

				Optional<ISerializableObject> op = Optional.ofNullable(c.getComplexCoverDataAtSide(face));
				// System.out.println(op); System.out.println(face);
				return op.filter(s -> (s instanceof AECover.Data) && ((AECover.Data) s).supportFluid())

						.isPresent();

			}
		}

		return false;
	}

}
