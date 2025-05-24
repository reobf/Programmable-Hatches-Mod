package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.interfaces.IDataCopyable;

public interface IDataCopyablePlaceHolder extends IDataCopyable {

   // NBTTagCompound getCopiedData(EntityPlayer player);

    default NBTTagCompound writeType(NBTTagCompound t, EntityPlayer player) {
        t.setString("type", this.getCopiedDataIdentifier(player));
        return t;
    }

    //boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt);

    default String getCopiedDataIdentifier(EntityPlayer player) {
        return this.getClass()
            .getName();

    };

}
