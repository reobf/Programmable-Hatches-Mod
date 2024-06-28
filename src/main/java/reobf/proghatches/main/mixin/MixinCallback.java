package reobf.proghatches.main.mixin;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import reobf.proghatches.gt.metatileentity.DualInputHatch;

public class MixinCallback {

	public static boolean encodingSpecialBehaviour = true;
	public static Function<Object, Long> getter;
	public static BiConsumer<Object, Long> setter;
	//spotless:off
	static {
		try {
			Field n = Class.forName("appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress")
					.getDeclaredField("value");
			n.setAccessible(true);
			setter = (s, b) -> {
				try {n.set(s, b);} catch (Exception e) {e.printStackTrace();}
			};
			getter = s -> {
				try {return (Long) n.get(s);} catch (Exception e) {e.printStackTrace();}
				return null;
			};
		} catch (Exception e) {e.printStackTrace();}

	}
	//spotless:on

	public static void handleAddedToMachineList(IGregTechTileEntity aTileEntity, Object o) {
		GT_MetaTileEntity_MultiBlockBase thiz = (GT_MetaTileEntity_MultiBlockBase) o;
		try {
			if (aTileEntity == null)
				return;
			IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
			if (aMetaTileEntity != null && aMetaTileEntity instanceof DualInputHatch) {
				((DualInputHatch) aMetaTileEntity).setFilter(thiz.getRecipeMap());
			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}
public static void aa(Object t){
	//System.out.println(t);
	
}

public static boolean fixCircuitTag(NBTTagCompound stackTagCompound){
	if(stackTagCompound!=null){
		try{
	
		boolean hasID=	
				stackTagCompound.getCompoundTag("targetCircuit").hasKey("id");
		boolean hasSTR=	
				stackTagCompound.getCompoundTag("targetCircuit").hasKey("string_id");	
	
		//V1->V2 
		//V2->V2
		//V3->V3
		try{
	if(hasID&&!hasSTR){	
		int id = stackTagCompound.getCompoundTag("targetCircuit").getInteger("id");
		stackTagCompound.getCompoundTag("targetCircuit").setString("string_id",
				Item.itemRegistry.getNameForObject(
				Item.itemRegistry.getObjectById(id)
				)
				);
		hasSTR=true;
	}		}catch(Exception e){}

	//V1->X 
	//V2->V3
	//V3->V3		
		try{
	if(hasSTR&&hasID){	
	stackTagCompound.getCompoundTag("targetCircuit").removeTag("id");	
	return true;
	}	}catch(Exception e){}
	
	
	
	
	//System.out.println(this);
		}catch(Exception e){}
	}
	return false;
}

}
