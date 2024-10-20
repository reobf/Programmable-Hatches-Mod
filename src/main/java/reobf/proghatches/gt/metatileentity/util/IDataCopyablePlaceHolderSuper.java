package reobf.proghatches.gt.metatileentity.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataCopyablePlaceHolderSuper extends IDataCopyablePlaceHolder{
	Supplier<Lookup> lookup();
	default NBTTagCompound getCopiedData(EntityPlayer player){
		NBTTagCompound ret= impl_getCopiedData(player, super_getCopiedData(player, lookup()));
		
		ret.setString("type", getCopiedDataIdentifier(player));
		return ret;
	}
	default boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt){
		if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
		nbt=(NBTTagCompound) nbt.copy();
		impl_pasteCopiedData(player, nbt);
		//conver to super id
		nbt.setString("type",super_getCopiedDataIdentifier(player, lookup()));
		
		return super_pasteCopiedData(player, nbt, lookup());
	}
	
	
	boolean  impl_pasteCopiedData(EntityPlayer player,NBTTagCompound tag);
	NBTTagCompound  impl_getCopiedData(EntityPlayer player,NBTTagCompound tag);
	//String  impl_getCopiedDataIdentifier(EntityPlayer player);
//////////
	default NBTTagCompound super_getCopiedData(EntityPlayer player,Supplier<Lookup>  lp){
		MethodHandle mh = getMH(2);
		if(mh==null){
		try {
			mh=lp.get().findSpecial(getClass().getSuperclass(), "getCopiedData", 
					MethodType.methodType(NBTTagCompound.class,EntityPlayer.class)
					, getClass());
			
			
		} catch (Exception e) {e.printStackTrace();
			throw new AssertionError(e);
		}
		setMH(mh,2);
		}
		
	try {
		return	(NBTTagCompound) mh.invoke(this,player);
	} catch (Throwable e) {e.printStackTrace();
		throw new AssertionError(e);
	}
	};	
	default String super_getCopiedDataIdentifier(EntityPlayer player,Supplier<Lookup>  lp){
		MethodHandle mh = getMH(1);
		if(mh==null){
		try {
			mh=lp.get().findSpecial(getClass().getSuperclass(), "getCopiedDataIdentifier", 
					MethodType.methodType(String.class,EntityPlayer.class)
					, getClass());
		
			
		} catch (Exception e) {e.printStackTrace();
			throw new AssertionError(e);
		}
		setMH(mh,1);
		}
		
	try {
		return	(String) mh.invoke(this,player);
	} catch (Throwable e) {e.printStackTrace();
		throw new AssertionError(e);
	}
	};	
		
	default boolean super_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt,Supplier<Lookup>  lp){
		MethodHandle mh = getMH(3);
		if(mh==null){
		try {
			mh=lp.get().findSpecial(getClass().getSuperclass(), "pasteCopiedData", 
					MethodType.methodType(boolean.class,EntityPlayer.class,NBTTagCompound.class)
					, getClass());
		
			
		} catch (Exception e) {e.printStackTrace();
			throw new AssertionError(e);
		}
		setMH(mh,3);
		}
		
	try {
		return	(boolean) mh.invoke(this,player,nbt);
	} catch (Throwable e) {e.printStackTrace();
		throw new AssertionError(e);
	}
	};	
		
		
		
	default public void setMH(MethodHandle MH,int index){
		
		cache.put(getClass(), index, MH);
	};
	default public MethodHandle  getMH( int index){
		
		return cache.get(getClass(), index);
	};
	
		
static  Table<Class, Integer, MethodHandle> cache=HashBasedTable.create();
}
