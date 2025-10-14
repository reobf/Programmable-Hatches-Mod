package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

public interface IDataCopyablePlaceHolderSuper extends IDataCopyablePlaceHolder {

  
    default NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = impl_getCopiedData(player, super_getCopiedData(player));

        ret.setString("type", getCopiedDataIdentifier(player));
        return ret;
    }

    default boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        nbt = (NBTTagCompound) nbt.copy();
        impl_pasteCopiedData(player, nbt);
        // conver to super id
        nbt.setString("type", super_getCopiedDataIdentifier(player));

        return super_pasteCopiedData(player, nbt);
    }

    boolean impl_pasteCopiedData(EntityPlayer player, NBTTagCompound tag);

    NBTTagCompound impl_getCopiedData(EntityPlayer player, NBTTagCompound tag);

  
    abstract NBTTagCompound super_getCopiedData(
        EntityPlayer player);
    abstract String super_getCopiedDataIdentifier(
        EntityPlayer player);

    abstract boolean super_pasteCopiedData(EntityPlayer player,
        NBTTagCompound nbt);
}
