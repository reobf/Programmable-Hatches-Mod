package reobf.proghatches.gt.metatileentity.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.Supplier;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import gregtech.api.interfaces.IDataCopyable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataCopyablePlaceHolder extends IDataCopyable{
	NBTTagCompound getCopiedData(EntityPlayer player);
	default NBTTagCompound writeType(NBTTagCompound t,EntityPlayer player){
		t.setString("type", this.getCopiedDataIdentifier(player));
		return t;
	}
	boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt);
	
	default String getCopiedDataIdentifier(EntityPlayer player){
		return this.getClass().getName();
		
		
	};
	

		
}
