package reobf.proghatches.main.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import thaumcraft.common.entities.golems.ItemGolemCore;

@Mixin(value=ItemGolemCore.class,remap=false)
public class MixinGolemCore extends Item{
	
	  public IIcon iconEx;
	 private static int damage=120;
	
	
	
	@Inject(method = "registerIcons", at = @At("HEAD"),remap=false)
	@SideOnly(Side.CLIENT)
	   public void registerIcons(IIconRegister ir,CallbackInfo c) {
	    
		iconEx = ir.registerIcon("proghatches:core");
	      
	   }

	@Inject(method = "addInformation", at = @At("TAIL"),remap=false)
	public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List list, boolean par4,CallbackInfo c) {
		  if(stack.getItemDamage()==damage) {
			  list.add(StatCollector.translateToLocal("proghatches.golemcore.120.hint.0"));
			  list.add(StatCollector.translateToLocal("proghatches.golemcore.120.hint.1"));
			  list.add(StatCollector.translateToLocal("programmable_hatches.addedby"));
		  }}
	@Inject(method = "getSubItems", at = @At("HEAD"),remap=false)

	   @SideOnly(Side.CLIENT)
	   public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List,CallbackInfo c) {
	    
		 par3List.add(new ItemStack(this, 1, damage));
	   }
	
	@Inject(method = "getIconFromDamage", at = @At("HEAD"),remap=false,cancellable=true)
	@SideOnly(Side.CLIENT)
	   public void getIconFromDamage(int d,CallbackInfoReturnable c) {
	     if(d==damage)c.setReturnValue((Object)iconEx);
	  
	   }
/*
	@Inject(method = "hasGUI", at = @At("HEAD"),remap=false,cancellable=true)
	 private static void hasGUI(int core,CallbackInfoReturnable a) {
		if(core==damage)a.setReturnValue(true);
	}
	    
	@Inject(method = "hasInventory", at = @At("HEAD"),remap=false,cancellable=true)

	private static void hasInventory(int core,CallbackInfoReturnable a) {
		if(core==damage)a.setReturnValue(true);
	}
	  */

	

	  
	


}
