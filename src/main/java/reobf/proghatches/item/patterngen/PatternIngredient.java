package reobf.proghatches.item.patterngen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

/**
 * One item or fluid ingredient shown in the preview and used by the filters and recipe id.
 */
public final class PatternIngredient {

    private static final String TYPE_ITEM = "item";
    private static final String TYPE_FLUID = "fluid";

    public final ItemStack item;
    public final FluidStack fluid;
    public final long amount;
    private List<String> oreNames;

    private PatternIngredient(ItemStack item, FluidStack fluid, long amount) {
        this.item = item;
        this.fluid = fluid;
        this.amount = amount;
    }

    public static PatternIngredient ofItem(ItemStack stack, long amount) {
        if (stack == null || stack.getItem() == null) return null;
        ItemStack copy = stack.copy();
        copy.stackSize = (int) Math.max(1, Math.min(Integer.MAX_VALUE, amount));
        return new PatternIngredient(copy, null, amount);
    }

    public static PatternIngredient ofFluid(FluidStack stack, long amount) {
        if (stack == null || stack.getFluid() == null || amount <= 0) return null;
        FluidStack copy = stack.copy();
        copy.amount = (int) Math.min(Integer.MAX_VALUE, amount);
        return new PatternIngredient(null, copy, amount);
    }

    public boolean isFluid() {
        return this.fluid != null;
    }

    public String displayName() {
        if (this.item != null) return displayName(this.item);
        if (this.fluid != null) return fluidDisplayName(this.fluid);
        return "";
    }

    public List<String> nameCandidates() {
        if (this.item != null) return Collections.singletonList(displayName(this.item));
        List<String> names = new ArrayList<>(2);
        String localized = fluidDisplayName(this.fluid);
        if (!localized.isEmpty()) names.add(localized);
        String registry = this.fluid.getFluid()
            .getName();
        if (registry != null && !registry.isEmpty() && !names.contains(registry)) names.add(registry);
        return names;
    }

    public List<String> oreCandidates() {
        if (this.item == null) return nameCandidates();
        if (this.oreNames == null) {
            Set<String> names = new LinkedHashSet<>();
            try {
                ItemStack lookup = this.item.copy();
                if (lookup.getItemDamage() == OreDictionary.WILDCARD_VALUE) lookup.setItemDamage(0);
                for (int id : OreDictionary.getOreIDs(lookup)) {
                    String name = OreDictionary.getOreName(id);
                    if (name != null && !name.isEmpty() && !"Unknown".equals(name)) names.add(name);
                }
            } catch (Throwable ignored) {}
            List<String> sorted = new ArrayList<>(names);
            Collections.sort(sorted);
            this.oreNames = sorted;
        }
        return this.oreNames;
    }

    public List<String> candidates(boolean oreDict) {
        return oreDict ? oreCandidates() : nameCandidates();
    }

    public String canonicalKey() {
        if (this.item != null) return itemKey(this.item, this.amount);
        return fluidKey(this.fluid, this.amount);
    }

    /** Output identity without the amount, used for conflict grouping. */
    public String typeKey() {
        if (this.item != null) return itemTypeKey(this.item);
        return fluidTypeKey(this.fluid);
    }

    public static String itemTypeKey(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";
        return "I|" + itemRegistryName(stack) + '|' + stack.getItemDamage() + '|'
            + canonicalNbt(stack.getTagCompound());
    }

    public static String fluidTypeKey(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return "";
        return "F|" + stack.getFluid()
            .getName() + '|' + canonicalNbt(stack.tag);
    }

    public NBTTagCompound toTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setLong("amount", this.amount);
        if (this.item != null) {
            tag.setString("type", TYPE_ITEM);
            NBTTagCompound stackTag = new NBTTagCompound();
            ItemStack copy = this.item.copy();
            copy.stackSize = 1;
            copy.writeToNBT(stackTag);
            tag.setTag("stack", stackTag);
        } else {
            tag.setString("type", TYPE_FLUID);
            tag.setString(
                "fluid",
                this.fluid.getFluid()
                    .getName());
        }
        return tag;
    }

    public static PatternIngredient fromTag(NBTTagCompound tag) {
        if (tag == null) return null;
        long amount = Math.max(1, tag.getLong("amount"));
        String type = tag.getString("type");
        if (TYPE_ITEM.equals(type)) {
            ItemStack stack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("stack"));
            return ofItem(stack, amount);
        }
        if (TYPE_FLUID.equals(type)) {
            net.minecraftforge.fluids.Fluid fluid = net.minecraftforge.fluids.FluidRegistry
                .getFluid(tag.getString("fluid"));
            if (fluid == null) return null;
            return ofFluid(new FluidStack(fluid, 1), amount);
        }
        return null;
    }

    public static String itemKey(ItemStack stack, long amount) {
        if (stack == null || stack.getItem() == null) return "";
        return "I|" + itemRegistryName(stack) + '|' + stack.getItemDamage() + '|' + amount + '|'
            + canonicalNbt(stack.getTagCompound());
    }

    private static String itemRegistryName(ItemStack stack) {
        String registry;
        try {
            registry = Item.itemRegistry.getNameForObject(stack.getItem());
        } catch (Throwable ignored) {
            registry = null;
        }
        if (registry == null || registry.isEmpty()) {
            registry = String.valueOf(stack.getItem().getUnlocalizedName());
        }
        return registry;
    }

    public static String fluidKey(FluidStack stack, long amount) {
        if (stack == null || stack.getFluid() == null) return "";
        return "F|" + stack.getFluid()
            .getName() + '|' + amount + '|' + canonicalNbt(stack.tag);
    }

    public static String displayName(ItemStack stack) {
        if (stack == null || stack.getItem() == null) return "";
        ItemStack copy = stack.copy();
        if (copy.getItemDamage() == OreDictionary.WILDCARD_VALUE) copy.setItemDamage(0);
        try {
            String name = copy.getDisplayName();
            return name == null ? "" : name;
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String fluidDisplayName(FluidStack stack) {
        if (stack == null || stack.getFluid() == null) return "";
        try {
            String name = stack.getLocalizedName();
            if (name != null && !name.isEmpty()) return name;
        } catch (Throwable ignored) {}
        return stack.getFluid()
            .getName();
    }

    /**
     * Deterministic NBT rendering: compound keys are sorted and nested compounds are visited in
     * that order. Primitives fall back to their own stable string form.
     */
    static String canonicalNbt(NBTBase base) {
        if (base == null) return "";
        if (base instanceof NBTTagCompound compound) {
            List<String> keys = new ArrayList<>(compound.func_150296_c());
            Collections.sort(keys);
            StringBuilder builder = new StringBuilder("{");
            for (String key : keys) {
                builder.append(key)
                    .append('=')
                    .append(canonicalNbt(compound.getTag(key)))
                    .append(';');
            }
            return builder.append('}').toString();
        }
        if (base instanceof NBTTagList list) {
            if (list.func_150303_d() != 10) return list.toString();
            StringBuilder builder = new StringBuilder("[");
            for (int i = 0; i < list.tagCount(); i++) {
                builder.append(canonicalNbt(list.getCompoundTagAt(i)))
                    .append(';');
            }
            return builder.append(']').toString();
        }
        if (base instanceof NBTTagByteArray array) return Arrays.toString(array.func_150292_c());
        if (base instanceof NBTTagIntArray array) return Arrays.toString(array.func_150302_c());
        return base.toString();
    }
}
