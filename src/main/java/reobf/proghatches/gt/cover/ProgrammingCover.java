package reobf.proghatches.gt.cover;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import ggfab.mte.MTELinkedInputBus;
import gregtech.api.covers.CoverContext;
import gregtech.api.covers.CoverPlacer;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.covers.Cover;
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.gt.metatileentity.util.IProgrammingCoverBlacklisted;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

public class ProgrammingCover extends Cover implements IProgrammer {

    public ProgrammingCover(CoverContext context, ITexture tex) {
        super(context, tex);

    }

    @Override
    public int getDefaultTickRate() {
        return 1;
    }
	@Override
	public int getMinimumTickRate() {
		return 1;
	}
    public void impl(ICoverable aTileEntity) {

        /*
         * if ((((aTileEntity instanceof IMachineProgress)) && (!((IMachineProgress) aTileEntity).isAllowedToWork()))) {
         * return;
         * }
         */
        TileEntity tile = (TileEntity) aTileEntity;

        /*
         * if (!(tile instanceof ISidedInventory)) {
         * return;
         * }
         * if (!(tile instanceof IGregTechTileEntity)) {
         * return;
         * }
         */
        IMetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();
        if (meta instanceof IProgrammingCoverBlacklisted) {

            return;
        }

        if (!(meta instanceof IConfigurationCircuitSupport)) {
            return;
        }

        ArrayList<ItemStack> isa = new ArrayList<>();
        int[] slots = (aTileEntity).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
        for (int slot : slots) {
            ItemStack is = (aTileEntity).getStackInSlot(slot);
            if (is == null) continue;
            if (is.getItem() != MyMod.progcircuit) continue;

            if (((ISidedInventory) tile).decrStackSize(slot, 64).stackSize == 0) {
                continue;
            }
            isa.add(
                GTUtility.copyAmount(
                    0,
                    ItemProgrammingCircuit.getCircuit(is)
                        .orElse(null)));

        }
        if (isa.isEmpty() == false) {
            if (meta instanceof IMultiCircuitSupport) {
                int[] aslots = ((IMultiCircuitSupport) meta).getCircuitSlots();
                for (int i = 0; i < aslots.length; i++) {
                    if (i < isa.size()) {
                        ((IInventory) tile)
                            .setInventorySlotContents(((IMultiCircuitSupport) meta).getCircuitSlots()[i], isa.get(i));
                    } else {
                        ((IInventory) tile)
                            .setInventorySlotContents(((IMultiCircuitSupport) meta).getCircuitSlots()[i], null);
                    }

                }

            } else {

                ((IInventory) tile)
                    .setInventorySlotContents(((IConfigurationCircuitSupport) meta).getCircuitSlot(), isa.get(0));

            }
        }

    }

    public static CoverPlacer placer() {
        return CoverPlacer.builder()

            .onlyPlaceIf(ProgrammingCover::isCoverPlaceable)
            .build();

    }

    public static boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
        if (Optional.of(aTileEntity)
            .filter(s -> s instanceof IGregTechTileEntity)
            .map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
            .filter(s -> s instanceof IProgrammingCoverBlacklisted)
            .isPresent()) return false;

        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            Cover beh = aTileEntity.getCoverAtSide(d);
            if (beh != null && beh.getClass() == LinkedBusSlaveCover.class) {
                return false;
            }
        }

        return true;
    }
    /*
     * public static class Placer extends CoverPlacerBase{
     * @Override
     * public boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
     * if (Optional.of(aTileEntity)
     * .filter(s -> s instanceof IGregTechTileEntity)
     * .map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
     * .filter(s -> s instanceof IProgrammingCoverBlacklisted)
     * .isPresent()) return false;
     * for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
     * Cover beh = aTileEntity.getCoverAtSide(d);
     * if (beh != null && beh.getClass() == LinkedBusSlaveCover.class) {
     * return false;
     * }
     * }
     * return super.isCoverPlaceable(side, aStack, aTileEntity);
     * }
     * }
     */

    @Override
    public void doCoverThings(byte xs, long aTimer) {
        impl(this.getTile());
        if (getTile() instanceof IGregTechTileEntity) {
            IMetaTileEntity x = ((IGregTechTileEntity) getTile()).getMetaTileEntity();
            if (x instanceof MTELinkedInputBus) {
                markOrUpdate((MTELinkedInputBus) x);
            }

        }
        return;
    }

    public static class Data {

        String str;
        ItemStack circuit;

        public Data(String s, ItemStack i) {
            str = s;
            circuit = i;
        }
    }

    public static WeakHashMap<MTELinkedInputBus, Data> ggfabLinkedBus = new WeakHashMap<>();

    public static void markOrUpdate(MTELinkedInputBus host) {

        Data bus = ggfabLinkedBus.get(host);
        if (bus == null)
            ggfabLinkedBus.put(host, new Data(ggfabGetRealChannel(host), host.getStackInSlot(host.getCircuitSlot())));
        else {

            bus.str = ggfabGetRealChannel(host);
            if (!ItemStack.areItemStacksEqual(bus.circuit, host.getStackInSlot(host.getCircuitSlot())))
                bus.circuit = host.getStackInSlot(host.getCircuitSlot());
        }
    }

    private static RuntimeException RESUABLE_EXCEPTION = new RuntimeException("", null, false, false) {

        private static final long serialVersionUID = 1L;
    };

    public static ItemStack sync(MTELinkedInputBus host) {
        String chan = ggfabGetRealChannel(host);
        Data data = ggfabLinkedBus.values()
            .stream()
            .filter(s -> Objects.equals(s.str, chan))
            .findAny()
            .orElse(null);
        if (data == null) throw RESUABLE_EXCEPTION;
        return data.circuit.copy();
    }

    private static String ggfabGetRealChannel(MTELinkedInputBus thiz) {
        if (thiz.getChannel() == null) return null;
        if (thiz.isPrivate()) return thiz.getBaseMetaTileEntity()
            .getOwnerUuid() + thiz.getChannel();
        return new UUID(0, 0) + thiz.getChannel();
    }

}
