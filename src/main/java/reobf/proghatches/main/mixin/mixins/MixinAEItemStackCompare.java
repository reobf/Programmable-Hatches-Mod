package reobf.proghatches.main.mixin.mixins;

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.Inject;

import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;

import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAETagCompound;
import appeng.util.item.AEItemDef;
import appeng.util.item.AEItemStack;
import appeng.util.item.AEStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

@Mixin(remap=false,value=AEItemStack.class)
public abstract class MixinAEItemStackCompare extends AEStack<IAEItemStack>{
@Inject(remap=false,method="compareTo", cancellable = true,require = 1,at = { @At(ordinal=0,shift=Shift.BEFORE,value="INVOKE",target = 
//"getAdaptor(Ljava/lang/Object;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;"
"getTagCompound"

,remap=false) })
public void a(AEItemStack other,CallbackInfoReturnable<Integer> r){
	if(error)return;
	try{
	if(other.getItem()==MyMod.progcircuit&&this.getItem()==MyMod.progcircuit){
		
	Optional<ItemStack> a = ItemProgrammingCircuit.getCircuit(other.getItemStack());
	Optional<ItemStack> b = ItemProgrammingCircuit.getCircuit(this.getItemStack());
	
		if(a.isPresent()&&b.isPresent()){
			int result=AEItemStack.create(b.get()).compareTo(AEItemStack.create(a.get()));
			if(result!=0)r.setReturnValue(result);
			return;
		}	
		else if(!a.isPresent()&&b.isPresent()){r.setReturnValue(-1);}
		else if(a.isPresent()&&!b.isPresent()){r.setReturnValue(1);}
		else{
			boolean aa=ItemProgrammingCircuit.isNew(other.getItemStack());
			boolean bb=ItemProgrammingCircuit.isNew(this.getItemStack());
			if(aa!=bb){
				r.setReturnValue(aa?1:-1);return;
			}
			return;//r.setReturnValue(0);
		}
		
	};
	
	}catch(Exception e){
		MyMod.LOG.fatal("ERROR:Excpeion in Mixin!!");
		e.printStackTrace();
		error=true;
	}
}
private static boolean error;
@Shadow
public abstract Item getItem() ;
@Shadow
public abstract ItemStack getItemStack() ;
}
