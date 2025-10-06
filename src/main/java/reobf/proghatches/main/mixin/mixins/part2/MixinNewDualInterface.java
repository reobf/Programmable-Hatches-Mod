package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.main.MyMod;

@Mixin(value = DualityInterface.class, remap = false)
public class MixinNewDualInterface {
	  @Shadow boolean isFluidInterface;
	@Inject(require = 1, method = "<init>", remap = false, at = { @At("RETURN") })
	    private  void ctr(final AENetworkProxy networkProxy, final IInterfaceHost ih, CallbackInfo c) {
if(ih instanceof Data){
	if(((Data) ih).supportFluid())
		isFluidInterface=true;
		}
	    }
}
