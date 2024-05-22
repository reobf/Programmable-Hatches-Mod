package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.parts.AEBasePart;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.InterfaceData;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinCoverInsertion {
	//spotless:off
	@ModifyVariable(require=0,expect=0,at = @At(value="INVOKE"
			,shift=Shift.BY,by=-3,
			target="getAdaptor(Ljava/lang/Object;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;"
			
			),method="*",remap=false)
	public ForgeDirection  a(ForgeDirection  old){
		return correct(old);
	}
	
	@ModifyVariable(require=0,expect=0,at = @At(value="INVOKE"
			,shift=Shift.BY,by=-3,
			target="wrapInventory(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;"
			
			),method="*",remap=false)
	public ForgeDirection  b(ForgeDirection  old){
		return correct(old);
	}
	//spotless:on
	@Shadow
	private IInterfaceHost iHost;

	private ForgeDirection correct(ForgeDirection f) {
		if (f != ForgeDirection.UNKNOWN)
			return f;

		if (this instanceof InterfaceData.IActualSideProvider) {
			return ((InterfaceData.IActualSideProvider) this).getActualSide().getOpposite();
		}

		if (AEBasePart.class.isInstance(iHost)) {
			AEBasePart host = (AEBasePart) iHost;
			if (host.getHost() instanceof InterfaceData.IActualSideProvider) {

				return ((InterfaceData.IActualSideProvider) host.getHost()).getActualSide().getOpposite();

			}

		}
		return f;

	}

}
