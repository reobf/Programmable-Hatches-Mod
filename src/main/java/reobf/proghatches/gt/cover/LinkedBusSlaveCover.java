package reobf.proghatches.gt.cover;

import java.util.Optional;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import ggfab.mte.MTELinkedInputBus;
import gregtech.api.covers.CoverContext;
import gregtech.api.covers.CoverPlacer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.covers.Cover;

public class LinkedBusSlaveCover extends Cover implements IProgrammer {

    public LinkedBusSlaveCover(CoverContext context, ITexture tex) {
        super(context, tex);
    }

    @Override
    public int getDefaultTickRate() {

        return 1;
    }

    @Override
    public void impl(ICoverable aTileEntity) {
        if (aTileEntity instanceof IGregTechTileEntity) {
            IMetaTileEntity x = ((IGregTechTileEntity) aTileEntity).getMetaTileEntity();
            if (x instanceof MTELinkedInputBus) {
                MTELinkedInputBus bus = (MTELinkedInputBus) x;
                try {
                    ItemStack is = ProgrammingCover.sync(bus);
                    bus.setInventorySlotContents(bus.getCircuitSlot(), is);
                } catch (RuntimeException e) {
                    // expected, do nothing
                }
            }

        }

    }

    public static CoverPlacer placer() {
        return CoverPlacer.builder()

            .onlyPlaceIf(LinkedBusSlaveCover::isCoverPlaceable)
            .build();

    }

    public static boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
        if (!Optional.of(aTileEntity)
            .filter(s -> s instanceof IGregTechTileEntity)
            .map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
            .filter(s -> s instanceof MTELinkedInputBus)
            .isPresent()) return false;
        for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
            Cover beh = aTileEntity.getCoverAtSide(d);
            if (beh != null && beh.getClass() == ProgrammingCover.class) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void doCoverThings(byte x, long aTimer) {
        impl(getTile());

        /* return */ super.doCoverThings(x, aTimer);
    }

}
