package reobf.proghatches.gt.metatileentity.meinput;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import appeng.me.GridAccessException;
import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;

public class MEHatchRefactor {
	

	
	
	

	public static void updateInformationSlot(MTEHatchInputBusME thiz, int index/*,ItemStack[] mInv*/){
		try {
			thiz.updateInformationSlot(index);
		} catch (GridAccessException e1) {
		}
		
	}
	

	
	
	
	

	public static void setConfigFluid(MTEHatchInputME thiz,int index,FluidStack fs,FluidStack o){
		
			
		((IMEHatchOverrided)thiz).setConfigFluid(thiz, index, fs, o);
			

		
		
		
	}

	public static void setConfigItem(MTEHatchInputBusME thiz, int index, ItemStack bruh,
			ItemStack itemStack2) {
		
	
			
		
		((IMEHatchOverrided)thiz).setConfigItem(thiz, index, bruh, itemStack2);
			

		
		
		
	
		
	}
	
	
	
	
}
