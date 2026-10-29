package reobf.proghatches.gt.metatileentity.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;

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

    /**
     * The Matter Manipulator round-trips copied data through JSON and re-types every number by its magnitude,
     * which turns an int array into a byte array, or throws for an array mixing magnitudes. One key per entry
     * survives that.
     */
    static NBTTagCompound intsToTag(int[] values) {
        NBTTagCompound t = new NBTTagCompound();
        for (int i = 0; i < values.length; i++) t.setInteger(Integer.toString(i), values[i]);
        return t;
    }

    /** Reads {@link #intsToTag}, a plain int array, or what the JSON round trip makes of one. */
    static int[] intsFromTag(NBTBase tag) {
        if (tag instanceof NBTTagIntArray a) return a.func_150302_c()
            .clone();
        if (tag instanceof NBTTagByteArray a) {
            byte[] b = a.func_150292_c();
            int[] r = new int[b.length];
            for (int i = 0; i < b.length; i++) r[i] = b[i];
            return r;
        }
        if (tag instanceof NBTTagList l0) {
            NBTTagList l = (NBTTagList) l0.copy();
            int[] r = new int[l.tagCount()];
            for (int i = 0; i < r.length; i++) {
                NBTBase e = l.removeTag(0);
                if (e instanceof NBTBase.NBTPrimitive p) r[i] = p.func_150287_d();
            }
            return r;
        }
        if (tag instanceof NBTTagCompound c) {
            int n = 0;
            while (c.hasKey(Integer.toString(n))) n++;
            int[] r = new int[n];
            for (int i = 0; i < n; i++) r[i] = c.getInteger(Integer.toString(i));
            return r;
        }
        return new int[0];
    }

}
