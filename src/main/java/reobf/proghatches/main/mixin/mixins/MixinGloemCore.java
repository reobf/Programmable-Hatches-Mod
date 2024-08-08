package reobf.proghatches.main.mixin.mixins;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
public class MixinGloemCore {
	@Shadow
	  public IIcon[] icon;
	 
	
	
	
	@Inject(method = "registerIcons", at = @At("HEAD"),remap=false)
	@SideOnly(Side.CLIENT)
	   public void registerIcons(IIconRegister ir,CallbackInfo c) {
		icon=new IIcon[icon.length+1];
	    this.icon[12] = ir.registerIcon("thaumcraft:golem_core_fish");
	      
	   }

	@Inject(method = "getSubItems", at = @At("HEAD"),remap=false)

	   @SideOnly(Side.CLIENT)
	   public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List,CallbackInfo c) {
	    

	   }

	


}
