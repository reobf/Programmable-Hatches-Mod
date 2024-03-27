package reobf.proghatches.util;

import static gregtech.api.util.GT_Utility.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import gregtech.api.GregTech_API;
import gregtech.api.util.GT_Log;
import gregtech.api.util.GT_Utility;
import gregtech.common.covers.redstone.GT_Cover_AdvancedRedstoneReceiverBase;

/*
 * copied from GT_Cover_AdvancedRedstoneTransmitterBase and GT_Cover_AdvancedRedstoneReceiverBase
 */
public class ProghatchesUtil {

    public static void removeAllSignalAt(UUID uuid, long hash) {
        Map<Integer, Map<Long, Byte>> frequencies = GregTech_API.sAdvancedWirelessRedstone.get(String.valueOf(uuid));
        if (frequencies == null) return;
        frequencies.keySet()
            .forEach(frequency ->

            frequencies.computeIfPresent(frequency, (freq, longByteMap) -> {
                longByteMap.remove(hash);

                return longByteMap.isEmpty() ? null : longByteMap;
            }));
    }

    public static void removeSignalAt(UUID uuid, int frequency, long hash) {
        Map<Integer, Map<Long, Byte>> frequencies = GregTech_API.sAdvancedWirelessRedstone.get(String.valueOf(uuid));
        if (frequencies == null) return;
        frequencies.computeIfPresent(frequency, (freq, longByteMap) -> {
            longByteMap.remove(hash);
            return longByteMap.isEmpty() ? null : longByteMap;
        });
    }

    public static void setSignalAt(UUID uuid, int frequency, long hash, byte value) {
        Map<Integer, Map<Long, Byte>> frequencies = GregTech_API.sAdvancedWirelessRedstone
            .computeIfAbsent(String.valueOf(uuid), k -> new ConcurrentHashMap<>());
        Map<Long, Byte> signals = frequencies.computeIfAbsent(frequency, k -> new ConcurrentHashMap<>());
        signals.put(hash, value);
    }

    public static Byte getSignalAt(UUID uuid, int frequency, GT_Cover_AdvancedRedstoneReceiverBase.GateMode mode) {
        Map<Integer, Map<Long, Byte>> frequencies = GregTech_API.sAdvancedWirelessRedstone.get(String.valueOf(uuid));
        if (frequencies == null) return 0;

        Map<Long, Byte> signals = frequencies.get(frequency);
        if (signals == null) signals = new ConcurrentHashMap<>();

        switch (mode) {
            case AND: {
                return (byte) (signals.values()
                    .stream()
                    .map(signal -> signal > 0)
                    .reduce(true, (signalA, signalB) -> signalA && signalB) ? 15 : 0);
            }
            case NAND: {
                return (byte) (signals.values()
                    .stream()
                    .map(signal -> signal > 0)
                    .reduce(true, (signalA, signalB) -> signalA && signalB) ? 0 : 15);
            }
            case OR: {
                return (byte) (signals.values()
                    .stream()
                    .map(signal -> signal > 0)
                    .reduce(false, (signalA, signalB) -> signalA || signalB) ? 15 : 0);
            }
            case NOR: {
                return (byte) (signals.values()
                    .stream()
                    .map(signal -> signal > 0)
                    .reduce(false, (signalA, signalB) -> signalA || signalB) ? 0 : 15);
            }
            case SINGLE_SOURCE: {
                if (signals.values()
                    .isEmpty()) {
                    return 0;
                }
                return signals.values()
                    .iterator()
                    .next();
            }
            default: {
                return 0;
            }
        }
    }

    public static int moveFromSlotToSlotSafe(IInventory fromInv, IInventory toInv, int aGrabFrom, int aPutTo,
        List<ItemStack> aFilter, boolean aInvertFilter, byte aMaxTargetStackSize, byte aMinTargetStackSize,
        byte aMaxMoveAtOnce, byte aMinMoveAtOnce) {

        if (fromInv.getSizeInventory() <= aGrabFrom || 0 > aGrabFrom
            || toInv.getSizeInventory() <= aPutTo
            || fromInv.getStackInSlot(aGrabFrom) == null) {
            return 0;
        }

        return GT_Utility.moveFromSlotToSlot(
            fromInv,
            toInv,
            aGrabFrom,
            aPutTo,
            aFilter,
            aInvertFilter,
            aMaxTargetStackSize,
            aMinTargetStackSize,
            aMaxMoveAtOnce,
            aMinMoveAtOnce);

    }

    public static ItemStack getWrittenBook(String aMapping, String aTitle, String aAuthor, String... aPages) {
        if (isStringInvalid(aMapping)) return null;
        ItemStack rStack = null;// GregTech_API.sBookList.get(aMapping);
        if (rStack != null) return copyAmount(1, rStack);
        if (isStringInvalid(aTitle) || isStringInvalid(aAuthor) || aPages.length == 0) return null;
        // sBookCount++;
        rStack = new ItemStack(Items.written_book, 1);
        NBTTagCompound tNBT = new NBTTagCompound();
        tNBT.setString("title", aTitle);
        tNBT.setString("author", aAuthor);
        NBTTagList tNBTList = new NBTTagList();
        for (byte i = 0; i < aPages.length; i++) {
            // aPages[i] = GT_LanguageManager
            // .addStringLocalization("Book." + aTitle + ".Page" + ((i < 10) ? "0" + i : i), aPages[i]);
            if (i < 48) {
                if (aPages[i].length() < 256) tNBTList.appendTag(new NBTTagString(aPages[i]));
                else GT_Log.err.println("WARNING: String for written Book too long! -> " + aPages[i]);
            } else {
                GT_Log.err.println("WARNING: Too much Pages for written Book! -> " + aTitle);
                break;
            }
        }
        /*
         * tNBTList.appendTag(
         * new NBTTagString(
         * "Credits to " + aAuthor
         * + " for writing this Book. This was Book Nr. "
         * + sBookCount
         * + " at its creation. Gotta get 'em all!"));
         */
        tNBT.setTag("pages", tNBTList);
        rStack.setTagCompound(tNBT);
        /*
         * GT_Log.out.println(
         * "GT_Mod: Added Book to Book List  -  Mapping: '" + aMapping
         * + "'  -  Name: '"
         * + aTitle
         * + "'  -  Author: '"
         * + aAuthor
         * + "'");
         * GregTech_API.sBookList.put(aMapping, rStack);
         */
        return copyOrNull(rStack);
    }
    
    /*
     * prevent negative stacksize dupe bug
     */
    
    public static  void attachZeroSizedStackRemover(Builder builder, UIBuildContext buildContext){
    	builder.widget(new SyncedWidget(){  
        	Consumer<Widget> ticker=ss->{
        		//if held stack is 0-sized, remove it
        		Optional.ofNullable(buildContext.getPlayer().inventory.getItemStack())
        		.filter(s->s.stackSize<=0)
        		.ifPresent(s->buildContext.getPlayer().inventory.setItemStack(null));
        		;};
        		{//tick on client side
        			this.setTicker(ticker);
        		}
        	    public  void detectAndSendChanges(boolean init) {
        		//tick on server side, don't really detect changes
        		ticker.accept(this);
        	};	
        	
    		@Override public void readOnClient(int id, PacketBuffer buf) throws IOException {}
    		@Override public void readOnServer(int id, PacketBuffer buf) throws IOException {}});
    }
    
    
    
    public static UUID deser(NBTTagCompound tag,String name){
    	return new UUID(tag.getLong(name+"_UUID_M"), tag.getLong(name+"_UUID_L"));
    }
    public static void ser(NBTTagCompound tag, UUID id,String name){
    	tag.setLong(name+"_UUID_M", id.getMostSignificantBits());
    	tag.setLong(name+"_UUID_L", id.getLeastSignificantBits());
    }
    
    
}
