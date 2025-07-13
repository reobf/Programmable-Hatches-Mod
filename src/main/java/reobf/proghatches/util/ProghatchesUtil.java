package reobf.proghatches.util;

import static gregtech.api.util.GTUtility.*;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;

import appeng.api.networking.security.IActionHost;
import appeng.helpers.IPriorityHost;
import appeng.items.tools.ToolPriorityCard;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.CoverPosition;
import gregtech.common.covers.redstone.CoverAdvancedRedstoneReceiverBase;
import reobf.proghatches.main.MyMod;

/*
 * copied from GT_Cover_AdvancedRedstoneTransmitterBase and CoverAdvancedRedstoneReceiverBase
 */
public class ProghatchesUtil {

    public static void removeAllSignalAt(UUID uuid, CoverPosition hash) {
        Map<String, Map<CoverPosition, Byte>> frequencies = GregTechAPI.sAdvancedWirelessRedstone
            .get(String.valueOf(uuid));
        if (frequencies == null) return;
        frequencies.keySet()
            .forEach(frequency ->

            frequencies.computeIfPresent(frequency, (freq, longByteMap) -> {
                longByteMap.remove(hash);

                return longByteMap.isEmpty() ? null : longByteMap;
            }));
    }

    public static void removeSignalAt(UUID uuid, int frequency, CoverPosition hash) {
        Map<String, Map<CoverPosition, Byte>> frequencies = GregTechAPI.sAdvancedWirelessRedstone
            .get(String.valueOf(uuid));
        if (frequencies == null) return;
        frequencies.computeIfPresent(frequency + "", (freq, longByteMap) -> {
            longByteMap.remove(hash);
            return longByteMap.isEmpty() ? null : longByteMap;
        });
    }/*
      * public static CoverPosition getCoverKey(@NotNull ICoverable tile, @NotNull ForgeDirection side) {
      * return new CoverPosition(tile.getCoords(), "ph_redstone_card", tile.getWorld().provider.dimensionId,
      * side.ordinal());
      * }
      */

    public static void setSignalAt(UUID uuid, int frequency, CoverPosition hash, byte value) {
        Map<String, Map<CoverPosition, Byte>> frequencies = GregTechAPI.sAdvancedWirelessRedstone
            .computeIfAbsent(String.valueOf(uuid), k -> new ConcurrentHashMap<>());
        Map<CoverPosition, Byte> signals = frequencies.computeIfAbsent(frequency + "", k -> new ConcurrentHashMap<>());
        signals.put(hash, value);
    }

    public static Byte getSignalAt(UUID uuid, int frequency, CoverAdvancedRedstoneReceiverBase.GateMode mode) {
        return getSignalAt(uuid, frequency, mode, true);
    }

    public static Byte getSignalAt(UUID uuid, int frequency, CoverAdvancedRedstoneReceiverBase.GateMode mode,
        boolean missingAsFalse) {
        Map<String, Map<CoverPosition, Byte>> frequencies = GregTechAPI.sAdvancedWirelessRedstone
            .get(String.valueOf(uuid));
        Map<CoverPosition, Byte> signals;
        if (frequencies == null) {
            if (missingAsFalse) return 0;
            signals = ImmutableMap.of();
        } else {
            signals = frequencies.get(frequency + "");
        }

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

        return GTUtility.moveFromSlotToSlot(
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

    public static ItemStack getWrittenBook(Item it, String aMapping, String aTitle, String aAuthor, String... aPages) {
        if (isStringInvalid(aMapping)) return null;
        ItemStack rStack = null;// GregTechAPI.sBookList.get(aMapping);
        if (rStack != null) return copyAmount(1, rStack);
       // if (isStringInvalid(aTitle) || isStringInvalid(aAuthor) || aPages.length == 0) return null;
        // sBookCount++;
        rStack = new ItemStack(it, 1);
        NBTTagCompound tNBT = new NBTTagCompound();
        tNBT.setString("title", aTitle);
        tNBT.setString("author", aAuthor);
        NBTTagList tNBTList = new NBTTagList();
        for (byte i = 0; i < aPages.length; i++) {
            // aPages[i] = GT_LanguageManager
            // .addStringLocalization("Book." + aTitle + ".Page" + ((i < 10) ?
            // "0" + i : i), aPages[i]);
            if (i < 48) {
                if (aPages[i].length() < 256) tNBTList.appendTag(new NBTTagString(aPages[i]));
                else MyMod.LOG.warn("WARNING: String for written Book too long! -> " + aPages[i]);
            } else {
                MyMod.LOG.warn("WARNING: Too much Pages for written Book! -> " + aTitle);
                break;
            }
        }

        tNBT.setTag("pages", tNBTList);
        rStack.setTagCompound(tNBT);

        return copyOrNull(rStack);
    }

    /*
     * public static void removeMultiCache(Builder builder, DualInputHatch buildContext) {
     * removeMultiCache(builder, buildContext::resetMulti);
     * }
     * public static void removeMultiCache(Builder builder, Runnable buildContext) {
     * builder.widget(new SyncedWidget() {
     * public void detectAndSendChanges(boolean init) {
     * buildContext.run();
     * }
     * @Override
     * public void readOnClient(int id, PacketBuffer buf) throws IOException {
     * // TODO Auto-generated method stub
     * }
     * @Override
     * public void readOnServer(int id, PacketBuffer buf) throws IOException {
     * // TODO Auto-generated method stub
     * }});
     * }
     */
    /*
     * prevent negative stacksize dupe bug
     */
    public static void attachZeroSizedStackRemover2(PanelSyncManager syncManager,ModularPanel p) {
    	Runnable ticker = () -> {
            // if held stack is 0-sized, remove it
            Optional.ofNullable(syncManager.getPlayer().inventory.getItemStack())
                .filter(s -> s.stackSize <= 0)
                .ifPresent(s -> syncManager.getPlayer().inventory.setItemStack(null));
            ItemStack[] inv = syncManager.getPlayer().inventory.mainInventory;
            for (int i = 0; i < inv.length; i++) {
                if (inv[i] != null && inv[i].stackSize <= 0) {
                    inv[i] = null;
                }

            }
        };
        p.child(new com.cleanroommc.modularui.widget.Widget(){
        	
        	public void onUpdate() {
        		//Thread.dumpStack();
        		ticker.run();
        		super.onUpdate();};
        }
        		);
    	syncManager.syncValue("ZeroSizedStackRemover", new SyncHandler() {
			
    		@Override
    		public void detectAndSendChanges(boolean init) {
    			
    			ticker.run();
    		}
			@Override
			public void readOnServer(int id, PacketBuffer buf) throws IOException {
			
				
			}
			
			@Override
			public void readOnClient(int id, PacketBuffer buf) throws IOException {
			
				
			}
		});
    	
    	
    }
    public static void attachZeroSizedStackRemover(Builder builder, UIBuildContext buildContext) {
        builder.widget(new SyncedWidget() {

            Consumer<Widget> ticker = ss -> {
                // if held stack is 0-sized, remove it
                Optional.ofNullable(buildContext.getPlayer().inventory.getItemStack())
                    .filter(s -> s.stackSize <= 0)
                    .ifPresent(s -> buildContext.getPlayer().inventory.setItemStack(null));
                ItemStack[] inv = buildContext.getPlayer().inventory.mainInventory;
                for (int i = 0; i < inv.length; i++) {
                    if (inv[i] != null && inv[i].stackSize <= 0) {
                        inv[i] = null;
                    }

                }
            };

            {// tick on client side
                this.setTicker(ticker);
            }

            public void detectAndSendChanges(boolean init) {
                // tick on server side, don't really detect changes
                ticker.accept(this);
            };

            @Override
            public void readOnClient(int id, PacketBuffer buf) throws IOException {}

            @Override
            public void readOnServer(int id, PacketBuffer buf) throws IOException {}
        });
    }

    public static UUID deser(NBTTagCompound tag, String name) {
        return new UUID(tag.getLong(name + "_UUID_M"), tag.getLong(name + "_UUID_L"));
    }

    public static NBTTagCompound ser(NBTTagCompound tag, UUID id, String name) {
        tag.setLong(name + "_UUID_M", id.getMostSignificantBits());
        tag.setLong(name + "_UUID_L", id.getLeastSignificantBits());
        return tag;
    }

    public static NBTTagCompound ser(NBTTagCompound tag, ItemStack[] is, String name) {
        for (int i = 0; i < is.length; i++) {

            tag.setTag(name + i, is[i].writeToNBT(new NBTTagCompound()));

        }
        tag.setInteger(name + "len", is.length);
        return tag;

    }

    public static ItemStack[] deseri(NBTTagCompound tag, String name) {
        ItemStack[] is = new ItemStack[tag.getInteger(name + "len")];

        for (int i = 0; i < is.length; i++) {
            is[i] = ItemStack.loadItemStackFromNBT(tag.getCompoundTag(name + i));
        }
        return is;

    }

    private static Method m;

    public static void handleUse(Object player, Object tile, Object priorityHost, Object actionHost, Object stack,
        Object side) {
        // dummy, for version that has no piority card
    }

    public static boolean handleUse(EntityPlayer player, /* SuperChestME */MetaTileEntity te) {
        if (player.getHeldItem() == null) {
            return false;
        }
        if (!(player.getHeldItem()
            .getItem() instanceof ToolPriorityCard)) {
            return false;
        }

        handleUse(
            player,
            (TileEntity) te.getBaseMetaTileEntity(),
            (IPriorityHost) te,
            (IActionHost) te,
            player.getHeldItem(),
            ForgeDirection.UP);
        return true;

    }

    public static void handleUse(EntityPlayer player, TileEntity tile, IPriorityHost priorityHost,
        IActionHost actionHost, ItemStack stack, ForgeDirection side) {

        if (m == null) try {
            Class<?> z = Class.forName("appeng.items.tools.ToolPriorityCard");
            m = z.getDeclaredMethod(
                "handleUse",
                EntityPlayer.class,
                TileEntity.class,
                IPriorityHost.class,
                IActionHost.class,
                ItemStack.class,
                ForgeDirection.class);
            m.setAccessible(true);

        } catch (Exception e) {
            MyMod.LOG.warn("PriorityCard not found, this is normal and safe to ignore if you're using pack v2.5.1.");
            MyMod.LOG.catching(e);
            // e.printStackTrace();
            try {
                m = ProghatchesUtil.class.getDeclaredMethod(
                    "handleUse",
                    Object.class,
                    Object.class,
                    Object.class,
                    Object.class,
                    Object.class,
                    Object.class);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }

        try {
            m.invoke(null, player, tile, priorityHost, actionHost, stack, side);
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    static List<ItemStack> allc = null;

    public static List<ItemStack> allCircuits() {

        if (allc == null) {
            try {
                allc = (List<ItemStack>) GTUtility.class.getDeclaredMethod("getAllIntegratedCircuits")
                    .invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (allc == null) {
            try {
                allc = (List<ItemStack>) GregTechAPI.class.getDeclaredMethod("getConfigurationCircuitList", int.class)
                    .invoke(null, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return allc;

    }
}
