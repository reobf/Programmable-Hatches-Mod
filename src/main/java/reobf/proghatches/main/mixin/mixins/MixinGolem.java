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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import reobf.proghatches.thaum.AIFix;
import thaumcraft.common.entities.ai.interact.AIFish;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.ItemGolemCore;

@Mixin(value=EntityGolemBase.class,remap=false)
public abstract class MixinGolem extends EntityGolem{
	public MixinGolem(World p_i1686_1_) {
		super(p_i1686_1_);
		
	}
	@Inject(method = "setupGolem", at = @At("RETURN"),remap=false, require = 1)
 public void setupGolem(CallbackInfoReturnable c) {
	    
	if(getCore()==120){
		
		  tasks.addTask(2, new AIFix(this));
	}
	   } 
	
	@Shadow
	 public abstract byte getCore() ;
}
