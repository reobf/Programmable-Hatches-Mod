package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.security.IActionHost;
import appeng.api.util.IInterfaceViewable;
import appeng.container.implementations.ContainerInterfaceTerminal;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.parts.AEBasePart;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.IActualSideProvider;
import reobf.proghatches.main.mixin.MixinCallback;

@Mixin(
		targets="appeng.container.implementations.ContainerInterfaceTerminal$InvTracker",
		//value = ContainerInterfaceTerminal.InvTracker.class,
		remap = false, priority = 1)
public class MixinInvTracker {
	
	@Mutable// @Final
	@Shadow  private  ForgeDirection side;
	@Mutable// @Final
	@Shadow private  long id;
	@Inject(method = "<init>", at = @At(value = "RETURN"), require = 1)
	public void check(long idx, IInterfaceViewable machine, boolean online,CallbackInfo c) 
	{
		check(machine);
		MixinCallback.aa(machine);
	if(machine instanceof AEBasePart){
		check(((AEBasePart) machine).getHost());
		
	}    
	
	
	}	
	public void check(Object machine){
		
if(machine instanceof IActualSideProvider){
			
			side=((IActualSideProvider) machine).getActualSide();
			id=id|Long.MIN_VALUE;
		}
		
	}
	
	
	
	
}
