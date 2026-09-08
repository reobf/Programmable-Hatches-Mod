package reobf.proghatches.ae;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import reobf.proghatches.eucrafting.IInputMightBeEmptyPattern;
import reobf.proghatches.gt.metatileentity.util.IDisallowOptimize;

public class EmitterPattern implements ICraftingPatternDetails, IInputMightBeEmptyPattern, IDisallowOptimize {

    private final ItemStack patternStack;
    private final IAEItemStack outputs;
    /**
     * The stack-typed view of the same output.
     * <p>
     * The emitter GUI stores its target in NBT as an ItemStack, so a fluid arrives here as a legacy
     * ItemFluidDrop wrapped in an AEItemStack. That item view is still what the deprecated
     * getOutputs()/getCondensedOutputs() must return, but AE2 decides whether this craftable can be
     * requested at all from the AE-typed pair: CraftingGridCache.craftableItems is keyed on the
     * getAEOutputs() stack verbatim, while beginCraftingJob puts a fluid request through
     * Platform.convertStack first. Without this the emitter registered a fluid under an item key that
     * no request could match. Platform.convertStack is a no-op for a genuine item.
     */
    private final IAEStack<?> aeOutputs;
    private int priority = 0;

    public EmitterPattern(ItemStack patternStack, IAEItemStack outputs) {
        this.patternStack = patternStack;
        this.outputs = outputs;
        this.aeOutputs = Platform.convertStack(outputs);

    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof EmitterPattern) {

            return outputs.equals(((EmitterPattern) obj).outputs);
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {

        return ((AEItemStack) outputs).hashCode();
    }

    @Override
    public ItemStack getPattern() {

        return patternStack;
    }

    @Override
    public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {

        return false;
    }

    @Override
    public boolean isCraftable() {

        return false;
    }

    @Override
    public IAEItemStack[] getInputs() {
        return new IAEItemStack[] { AEApi.instance()
            .storage()
            .createItemStack(new ItemStack(Items.apple, 0)) };

    }

    @Override
    public IAEItemStack[] getCondensedInputs() {

        return getInputs();
    }

    @Override
    public IAEItemStack[] getCondensedOutputs() {

        return new IAEItemStack[] { outputs };
    }

    @Override
    public IAEItemStack[] getOutputs() {
        return new IAEItemStack[] { outputs };
    }

    @Override
    public IAEStack<?>[] getCondensedAEOutputs() {

        return new IAEStack<?>[] { aeOutputs };
    }

    @Override
    public IAEStack<?>[] getAEOutputs() {

        return getCondensedAEOutputs();
    }

    @Override
    public boolean canSubstitute() {

        return false;
    }

    @Override
    public ItemStack getOutput(InventoryCrafting craftingInv, World world) {

        return outputs.getItemStack();
    }

    @Override
    public int getPriority() {

        return priority;
    }

    @Override
    public void setPriority(int priority) {
        this.priority = priority;

    }
}
