package reobf.proghatches.ae;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import reobf.proghatches.eucrafting.IInputMightBeEmptyPattern;
import reobf.proghatches.gt.metatileentity.util.IDisallowOptimize;

public class EmitterPattern implements ICraftingPatternDetails, IInputMightBeEmptyPattern, IDisallowOptimize {

    private final ItemStack patternStack;
    private final IAEItemStack outputs;
    private int priority = 0;

    public EmitterPattern(ItemStack patternStack, IAEItemStack outputs) {
        this.patternStack = patternStack;
        this.outputs = outputs;

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
