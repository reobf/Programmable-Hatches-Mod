package reobf.proghatches.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizon.gtnhlib.chat.AbstractChatComponentCustom;
import com.gtnewhorizon.gtnhlib.chat.ChatComponentCustomRegistry;
import com.gtnewhorizon.gtnhlib.chat.customcomponents.AbstractChatComponentBuffer;

import cpw.mods.fml.common.network.ByteBufUtils;
import gregtech.api.util.GTUtility;
import reobf.proghatches.main.MyMod;

/**
 * The part of a pattern hatch's Interface Terminal name that lists its circuits, marked items and marked fluids.
 * The stacks travel to the client and are named there, so the text is in the viewing player's language.
 */
public class ChatComponentHatchNameSuffix extends AbstractChatComponentBuffer<ChatComponentHatchNameSuffix> {

    public List<ItemStack> items = new ArrayList<>();
    public List<FluidStack> fluids = new ArrayList<>();

    public ChatComponentHatchNameSuffix() {}

    public ChatComponentHatchNameSuffix(List<ItemStack> items, List<FluidStack> fluids) {
        this.items = items;
        this.fluids = fluids;
    }

    public static void register() {
        ChatComponentCustomRegistry.register(ChatComponentHatchNameSuffix::new);
    }

    /** Null when there is nothing to list. */
    public static ChatComponentHatchNameSuffix of(ItemStack[] items, FluidStack[] fluids) {
        List<ItemStack> is = new ArrayList<>();
        if (items != null) for (ItemStack s : items) if (s != null && s.getItem() != null) is.add(s);
        List<FluidStack> fs = new ArrayList<>();
        if (fluids != null) for (FluidStack s : fluids) if (s != null && s.getFluid() != null) fs.add(s);
        return is.isEmpty() && fs.isEmpty() ? null : new ChatComponentHatchNameSuffix(is, fs);
    }

    @Override
    public String getID() {
        return MyMod.MODID + ":ChatComponentHatchNameSuffix";
    }

    @Override
    protected AbstractChatComponentCustom copySelf() {
        List<ItemStack> is = new ArrayList<>(items.size());
        for (ItemStack s : items) is.add(s == null ? null : s.copy());
        List<FluidStack> fs = new ArrayList<>(fluids.size());
        for (FluidStack s : fluids) fs.add(s == null ? null : s.copy());
        return new ChatComponentHatchNameSuffix(is, fs);
    }

    @Override
    public String getUnformattedTextForChat() {
        StringBuilder text = new StringBuilder();
        for (ItemStack is : items) {
            if (is == null || is.getItem() == null) continue;
            text.append(" - ");
            if (is.getItem() == GTUtility.getIntegratedCircuit(0)
                .getItem()) {
                text.append(is.getItemDamage());
            } else {
                text.append(is.getDisplayName());
                if (is.getItemDamage() > 0) text.append("@")
                    .append(is.getItemDamage());
            }
        }
        for (FluidStack fs : fluids) {
            if (fs == null || fs.getFluid() == null) continue;
            text.append(" - ")
                .append(fs.getLocalizedName());
        }
        return text.toString();
    }

    @Override
    public void encode(PacketBuffer buf) {
        buf.writeVarIntToBuffer(items.size());
        for (ItemStack is : items) ByteBufUtils.writeItemStack(buf, is);
        buf.writeVarIntToBuffer(fluids.size());
        for (FluidStack fs : fluids) ByteBufUtils.writeTag(buf, fs == null ? null : fs.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void decode(PacketBuffer buf) {
        int n = buf.readVarIntFromBuffer();
        items = new ArrayList<>(n);
        for (int i = 0; i < n; i++) items.add(ByteBufUtils.readItemStack(buf));
        n = buf.readVarIntFromBuffer();
        fluids = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            NBTTagCompound tag = ByteBufUtils.readTag(buf);
            fluids.add(tag == null ? null : FluidStack.loadFluidStackFromNBT(tag));
        }
    }
}
