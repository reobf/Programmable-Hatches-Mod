package reobf.proghatches.gt.metatileentity.meinput;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;

import gregtech.common.tileentities.machines.MTEHatchInputBusME;
import gregtech.common.tileentities.machines.MTEHatchInputME;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class MEHatchRefactor {
	

	
	
	static Boolean  cache;
	public static boolean isRefactor(){
		
		if(cache==null){
			
			try {
				Class.forName("gregtech.common.tileentities.machines.MTEHatchInputBusME$Slot");
				cache=true;System.out.println("ME Input Refactor detected.");
			} catch (ClassNotFoundException e) {
				cache=false;System.out.println("Good O'L ME Input.");
			}
			
			
			
			
			
		}
		
		return cache;
		
	}
	static BiConsumer<MTEHatchInputBusME,Integer> updateInformationSlot;
	public static void updateInformationSlot(MTEHatchInputBusME thiz, int index/*,ItemStack[] mInv*/){
		
		if(updateInformationSlot==null){
			
		try{
			Method m=MTEHatchInputBusME.class.getDeclaredMethod("updateInformationSlot", int.class);
			updateInformationSlot=(a,b)->{try {
				m.invoke(a,b);
			} catch (Exception e) {
				throw new AssertionError("updateInformationSlot not found",e);
			}};
				
		}catch(Exception e){}
		
		try{
			Method m=MTEHatchInputBusME.class.getDeclaredMethod("updateInformationSlot", int.class,ItemStack.class);
			updateInformationSlot=(a,b)->{try {
				m.invoke(a,b,a.mInventory[b]);
			} catch (Exception e) {
				throw new AssertionError("updateInformationSlot not found",e);
			}};
		
		}catch(Exception e){}
		
		if(updateInformationSlot==null)throw new AssertionError("updateInformationSlot not found");
		
		updateInformationSlot.accept(thiz,  index);

	}	
	}
	
	static Method setSlotConfig;
	
	
	
	static Field extracted,extractedA;
	static Field extractedF,extractedAF;
	static {
		
		try {
			extracted=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputBusME$Slot").getDeclaredField("extracted");
			extractedA=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputBusME$Slot").getDeclaredField("extractedAmount");
			extractedF=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputME$Slot").getDeclaredField("extracted");
			extractedAF=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputME$Slot").getDeclaredField("extractedAmount");
			
			extracted.setAccessible(true);
			extractedA.setAccessible(true);
			extractedF.setAccessible(true);
			extractedAF.setAccessible(true);
		
		} catch (Exception e) {
		}
		
		
		
		
	}
	
	
	public static void setConfigItem0(MTEHatchInputBusME thiz,int index,ItemStack bruh){
		if(!isRefactor()){
			if(index<0||index>=16){
				System.out.println(thiz);
				System.out.println(index);
				System.out.println(bruh);
				throw new AssertionError("should not happen");
				
			}
			thiz.mInventory[index]=bruh;
			
			
		}else{
			
			if(setSlotConfig==null){
				
				try {
					setSlotConfig=MTEHatchInputBusME.class.getDeclaredMethod("setSlotConfig", int.class,ItemStack.class);
					setSlotConfig.setAccessible(true);
				} catch (Exception e) {
				}
				
			}
			if(setSlotConfig==null)throw new AssertionError();
			
			try {
				setSlotConfig.invoke(thiz, index, bruh);
			
				
				
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			
			//thiz.setSlotConfig(index, bruh);
			
			
			
		}
		
		
	}
	
	
	
static Field f1,f2;
static Field f3,f4;
	static{
		
		try {
			f1=MTEHatchInputBusME.class.getDeclaredField("slots");
			f1.setAccessible(true);
			
			f3=MTEHatchInputME.class.getDeclaredField("slots");
			f3.setAccessible(true);
			
		} catch (Exception e) {
		}
		
		
	}
	
	public static ItemStack getConfigItem(MTEHatchInputBusME thiz,int index){
		if(!isRefactor()){
			if(index<0||index>=16){
				System.out.println(thiz);
				System.out.println(index);
				
				throw new AssertionError("should not happen");
				
			}
			return thiz.mInventory[index];
			
			
			
		}else{
			
			
			if(f1==null&&f2==null){
				try {
					f1=MTEHatchInputBusME.class.getDeclaredField("slots");
					f1.setAccessible(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
				
				try {
					f2=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputBusME$Slot").getDeclaredField("config");
					f2.setAccessible(true);
					} catch (Exception e) {
					throw new AssertionError(e);
				}
				
				
				
			}
			if(f1==null||f2==null){throw new AssertionError();}
			try {
				Object obj = ((Object[])f1.get(thiz))[index];
				if(obj==null){return null;}
				return (ItemStack) f2.get(obj);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			
			
			
			
			//thiz.getSlotConfig(index);
			
			
			
		}
		
		
	}
	static Field mStoredFluids;
	static Method setSlotConfigF;
	public static void setConfigFluid(MTEHatchInputME thiz,int index,FluidStack fs,FluidStack o){
		if(!isRefactor()){
			
			if(mStoredFluids==null){
				
				try {
				mStoredFluids=MTEHatchInputME.class.getDeclaredField("mStoredFluids");
				mStoredFluids.setAccessible(true);
				} catch (Exception e) {throw new AssertionError(e);
				}
				
				
				
			}
			
			if(mStoredFluids==null){
				throw new AssertionError();
			}
			
			try {
				FluidStack[] marks=(FluidStack[]) mStoredFluids.get(thiz);
				marks[index]=fs;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			//thiz.mStoredFluids[index]=fs;
			
		}else{
			
			
				if(setSlotConfigF==null){
				
				try {
					setSlotConfigF=MTEHatchInputME.class.getDeclaredMethod("setSlotConfig", int.class,FluidStack.class);
					setSlotConfigF.setAccessible(true);
				} catch (Exception e) {
				}
				
			}
			if(setSlotConfigF==null)throw new AssertionError();
			
			try {
				setSlotConfigF.invoke(thiz, index, fs);
				if(o!=null){
					Object ww=((Object[])f3.get(thiz))[index];
				extractedF.set(ww,o);
				extractedAF.set(ww,o.amount);}
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			
			
			
		}
		
		
		
	}
	
	
	public static FluidStack getConfigFluid(MTEHatchInputME thiz,int index){
		if(!isRefactor()){
			
			if(mStoredFluids==null){
				
				try {
				mStoredFluids=MTEHatchInputME.class.getDeclaredField("mStoredFluids");
				mStoredFluids.setAccessible(true);
				} catch (Exception e) {throw new AssertionError(e);
				}
				
				
				
			}
			
			if(mStoredFluids==null){
				throw new AssertionError();
			}
			
			try {
				FluidStack[] marks=(FluidStack[]) mStoredFluids.get(thiz);
				return marks[index];
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			//thiz.mStoredFluids[index]=fs;
			
		}else{
			
			if(f3==null&&f4==null){
				try {
					f3=MTEHatchInputBusME.class.getDeclaredField("slots");
					f3.setAccessible(true);
				} catch (Exception e) {
					throw new AssertionError(e);
				}
				
				try {
					f4=Class.forName("gregtech.common.tileentities.machines.MTEHatchInputME$Slot").getDeclaredField("config");
					f4.setAccessible(true);
					} catch (Exception e) {
					throw new AssertionError(e);
				}
				
				
				
			}
			if(f3==null||f4==null){throw new AssertionError();}
			try {
				Object obj = ((Object[])f3.get(thiz))[index];
				if(obj==null){return null;}
				return (FluidStack) f4.get(obj);
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			
			
			
			
			
			
		}
		
		
		
	}
	static Field storedInformationFluids;
	public static void setConfigItem(MTEHatchInputBusME thiz, int index, ItemStack bruh,
			ItemStack itemStack2) {
		
		if(!isRefactor()){
			if(index<0||index>=16){
				System.out.println(thiz);
				System.out.println(index);
				System.out.println(bruh);
				throw new AssertionError("should not happen");
				
			}
			thiz.mInventory[index]=bruh;
			thiz.mInventory[index+16]=itemStack2;
			
		}else{
			
			if(setSlotConfig==null){
				
				try {
					setSlotConfig=MTEHatchInputBusME.class.getDeclaredMethod("setSlotConfig", int.class,ItemStack.class);
					setSlotConfig.setAccessible(true);
				} catch (Exception e) {
				}
				
			}
			if(setSlotConfig==null)throw new AssertionError();
			
			try {
				setSlotConfig.invoke(thiz, index, bruh);
				if(itemStack2!=null){
					Object ww= ((Object[])f1.get(thiz))[index];
				extracted.set(ww,itemStack2);
				extractedA.set(ww,itemStack2.stackSize);}
			} catch (Exception e) {
				throw new AssertionError(e);
			}
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			
			//thiz.setSlotConfig(index, bruh);
			
			
			
		}
		
		
		
	
		
	}
	
	
	
	
}
