package reobf.proghatches.main.mixin.mixins.part2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.TileConduitBundle;
import reobf.proghatches.eio.ICraftingMachineConduit;

@Mixin(value = TileConduitBundle.class, remap = false)
public abstract class MixinEIOBundle implements ICraftingMachine {

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {

        ICraftingMachineConduit gc = (ICraftingMachineConduit) getConduit(ICraftingMachineConduit.class);
        if (gc != null) {
            return gc.pushPattern(patternDetails, table, ejectionDirection);

        }

        return false;
    }

    @Shadow
    public abstract IConduit getConduit(Class type);

    @Override
    public boolean acceptsPlans() {
        ICraftingMachineConduit gc = (ICraftingMachineConduit) getConduit(ICraftingMachineConduit.class);
        if (gc != null) {
            return gc.acceptsPlans();

        }

        return false;
    }

}
