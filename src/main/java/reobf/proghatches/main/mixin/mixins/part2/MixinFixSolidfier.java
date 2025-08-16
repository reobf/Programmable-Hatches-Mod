package reobf.proghatches.main.mixin.mixins.part2;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import appeng.crafting.v2.resolvers.ExtractItemResolver;
import gregtech.api.logic.ProcessingLogic;
import gregtech.common.tileentities.machines.IDualInputInventoryWithPattern;
import gregtech.common.tileentities.machines.multi.MTEMultiSolidifier;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import reobf.proghatches.eucrafting.AECover;

@Mixin(targets="gregtech.common.tileentities.machines.multi.MTEMultiSolidifier$1", remap = false)
public class MixinFixSolidfier extends ProcessingLogic{
	   
	
	@Inject(require = 1, cancellable=true,at = { @At("HEAD") }, method = "tryCachePossibleRecipesFromPattern", remap = false)
	    public void tryCachePossibleRecipesFromPattern(IDualInputInventoryWithPattern inv, CallbackInfoReturnable r) {
		 if (!inv.shouldBeCached()) {
	          r.setReturnValue(true);
	        }
	    }


}
