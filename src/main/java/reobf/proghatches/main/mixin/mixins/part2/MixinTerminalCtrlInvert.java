package reobf.proghatches.main.mixin.mixins.part2;

import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.implementations.GuiMEMonitorable;
import appeng.client.gui.slots.VirtualMEMonitorableSlot;
import appeng.client.gui.slots.VirtualMEPinSlot;
import appeng.client.gui.slots.VirtualMESlot;
import appeng.container.implementations.ContainerMEMonitorable;
import appeng.core.AEConfig;
import appeng.helpers.MonitorableAction;
import appeng.me.GridConnection;
import appeng.util.FluidUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import reobf.proghatches.ae.ICtrlInverted;
@Mixin(value = GuiMEMonitorable.class, remap = false)
public abstract class MixinTerminalCtrlInvert extends AEBaseGui{

	public MixinTerminalCtrlInvert(Container container) {
		super(container);
		
	}



public abstract void drawHoveringText(List textLines, int x, int y, FontRenderer font);


@WrapOperation(method = "handleMonitorableSlotClick", at = @At(
	    value = "INVOKE",
	    //target = "Lappeng/client/gui/AEBaseGui;isCtrlKeyDown()Z"
	    target = "Lappeng/client/gui/implementations/GuiMEMonitorable;isCtrlKeyDown()Z"
	))
	private boolean handleMonitorableSlotClick(Operation<Boolean> original,@Local VirtualMEMonitorableSlot t,@Local(argsOnly = true,ordinal = 0) LocalIntRef  mouseButton) {
	  
	
	if(((this.host instanceof ICtrlInverted ta) ? ta.invert() : false)) {
	ItemStack hand = Minecraft.getMinecraft().thePlayer.inventory.getItemStack();
	FluidStack fs;
	if(hand!=null&&(fs = FluidUtils.getFluidFromContainer(hand))!=null&&fs.amount>0) {
		/*if(t.getAEStack()==null&&original.call()==false) {
			if(mouseButton.get()==1)
			mouseButton.set(0);
		}*/

		return original.call()^true ;
		
	}else if(t.getAEStack()instanceof IAEFluidStack){
		return original.call()^true ;
	}	
	
	
	}
	
	
	
	/*if(Minecraft.getMinecraft().thePlayer.inventory.getItemStack()!=null
			&&t.getAEStack()instanceof IAEFluidStack)
	{return original.call()^((this.host instanceof ICtrlInverted ta) ? ta.invert() : false) ; }else 
	if(Minecraft.getMinecraft().thePlayer.inventory.getItemStack()!=null&&t.getAEStack()==null)
	{
		
		
		
		
	}
	  */
	  
	    return original.call(); 
	}

@Shadow
private  ITerminalHost host;


@Inject(method = "drawFG", at = { @At("HEAD") })
public void A(CallbackInfo a,@Share(value = "showContainerInteractionTooltips") LocalRef<Boolean> tmp) {
	if(((this.host instanceof ICtrlInverted ta) ? ta.invert() : false)) {
	tmp.set(AEConfig.instance.showContainerInteractionTooltips);
	AEConfig.instance.showContainerInteractionTooltips=false;}
}

@Inject(method = "drawFG", at = { @At("RETURN") })
public void B(CallbackInfo a,@Share(value = "showContainerInteractionTooltips") LocalRef<Boolean> tmp) {
	if(((this.host instanceof ICtrlInverted ta) ? ta.invert() : false)) {
	AEConfig.instance.showContainerInteractionTooltips=tmp.get();}
}





}
