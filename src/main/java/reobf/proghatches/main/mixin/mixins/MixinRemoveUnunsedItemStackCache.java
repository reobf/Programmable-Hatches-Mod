package reobf.proghatches.main.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.Container;

@Mixin(value = ModularUIContainer.class, remap = false)
public abstract class MixinRemoveUnunsedItemStackCache extends Container{

	
	/*
	addSlotToContainer() in Container.java will add to both inventorySlots and inventoryItemStacks
	removeSlot() in ModularUIContainer.java only remove inventorySlots
	that means open and close a child window with slotwidgets will lengthen inventoryItemStacks
	this mixin will fix this potential leak
	*/
	
	  private int length;
	  @Inject(method = "removeSlot", at = @At(value = "HEAD"), require = 1, cancellable = false)
	  private void removeSlot0(net.minecraft.inventory.Slot slot, CallbackInfo c) 
	  {
		 length=this.inventoryItemStacks.size();
	  }
	  
	  @Inject(method = "removeSlot", at = @At(value = "TAIL"), require = 1, cancellable = false)

	    private void removeSlot1(net.minecraft.inventory.Slot slot, CallbackInfo c) 
	  {
		  if(length!=this.inventoryItemStacks.size())return;//just in case it's fixed in future version
		  this.inventoryItemStacks.remove(slot.slotNumber);
		 // System.out.println( this.inventoryItemStacks);
	  }
	
	
	
}
