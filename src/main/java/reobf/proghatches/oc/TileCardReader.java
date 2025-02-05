package reobf.proghatches.oc;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import com.google.common.base.Joiner;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import shedar.mods.ic2.nuclearcontrol.api.IPanelDataSource;
import shedar.mods.ic2.nuclearcontrol.api.PanelString;
import shedar.mods.ic2.nuclearcontrol.panel.CardWrapperImpl;

public class TileCardReader extends TileEntity
    implements ITileWithModularUI, IInventory, ISidedInventory, li.cil.oc.api.network.Environment {

    ItemStack[] inv = new ItemStack[27];

    // ItemCardBase
    @Override
    public int getSizeInventory() {

        return inv.length;
    }

    @Override
    public ItemStack getStackInSlot(int slotIn) {

        return inv[slotIn];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inv[index] == null) return null;
        return inv[index].splitStack(count);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {

        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {

        inv[index] = stack;
    }

    @Override
    public String getInventoryName() {

        return "reader";
    }

    @Override
    public boolean hasCustomInventoryName() {

        return false;
    }

    @Override
    public int getInventoryStackLimit() {

        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {

        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {

        return stack.getItem() instanceof IPanelDataSource;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_) {

        return new int[0];
    }

    @Override
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
        return false;
    }

    @Override
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
        return false;
    }

    @Callback(
        doc = "function(index:number,line:number[,showLabel:boolean=true,conact:boolean=false]):string -- Get the #line text of the #index(0 is the first slot!) Sensor Card."
            + " If conact is false, return 3 values(left, center and right text, might be nil if missing!). Otherwise concat those 3 texts with space and return only 1 value.")
    public Object[] getText(final Context context, final Arguments args) {
        return get(args.checkInteger(0), args.checkInteger(1), args.optBoolean(2, true), args.optBoolean(2, false));
    }

    public Object[] get(int index, int line, boolean lab, boolean concat) {
        ItemStack is;
        try {
            is = inv[index];
            if (is == null) throw new Exception();
        } catch (Exception e) {

            return new Object[] { null, "invalid slot or no item" };
        }
        CardWrapperImpl w = new CardWrapperImpl(is, index);

        ((IPanelDataSource) is.getItem()).update(worldObj, w, Integer.MAX_VALUE);
        PanelString str;
        try {
            str = ((IPanelDataSource) is.getItem()).getStringData(-1, w, lab)
                .get(line);
        } catch (IndexOutOfBoundsException e) {
            return new Object[] { null, "invalid line" };
        }
        ArrayList<String> arr = new ArrayList();
        arr.add(str.textLeft);
        arr.add(str.textCenter);
        arr.add(str.textRight);

        if (!concat) return arr.toArray();
        arr.removeIf(s -> s == null);
        return new Object[] { Joiner.on(" ")
            .join(arr) };
    }

    @Callback(doc = "function(index:number):string -- As its name suggests. Note that index 0 is the first slot.")
    public Object[] getTitle(final Context context, final Arguments args) {
        ItemStack is;
        try {
            is = inv[args.checkInteger(0)];
            if (is == null) throw new Exception();
        } catch (Exception e) {

            return new Object[] { null, "invalid slot or no item" };
        }
        CardWrapperImpl w = new CardWrapperImpl(is, args.checkInteger(0));

        ((IPanelDataSource) is.getItem()).update(worldObj, w, 999);

        return new Object[] { w.getTitle() };

    }

    Node node = li.cil.oc.api.Network.newNode(this, Visibility.Network)
        .withComponent("cardreader")
        .create();

    @Override
    public Node node() {

        return node;
    }

    @Override
    public void onConnect(Node node) {

    }

    @Override
    public void onDisconnect(Node node) {

    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public ModularWindow createWindow(UIBuildContext buildContext) {
        ModularWindow.Builder builder = ModularWindow.builder(176, 107 + 18 * 3 + 18);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        IDrawable slot = new AdaptableUITexture(
            new ResourceLocation("nuclearcontrol", "textures/gui/GUIAdvancedInfoPanel.png"),
            7 / 256.0f,
            41 / 256.0f,
            (7 + 18) / 256.0f,
            (41 + 18) / 256.0f

            ,
            18,
            18,
            0,
            0);
        final IDrawable[] background = new IDrawable[] { slot/* GUITextureSet.DEFAULT.getItemSlot() */ };
        builder.bindPlayerInventory(buildContext.getPlayer());

        builder.widget(
            SlotGroup.ofItemHandler(new MappingItemHandler(inv, 0, 27), 9)

                .startFromSlot(0)
                .endAtSlot(26)
                .background(background)
                .widgetCreator((h) -> (SlotWidget) new SlotWidget(h) {

                    public IDrawable[] getBackground() {

                        return background;
                    };
                }.setFilter(s -> s.getItem() instanceof IPanelDataSource))

                .build()
                .setPos(3 + 4, 3 + 4)
                .setSize(18 * 9, 18 * 3)

        );

        return builder.build();

    }

    boolean init;

    @Override
    public void updateEntity() {
        if (init == false) if (node != null) {
            init = true;
            li.cil.oc.api.Network.joinOrCreateNetwork(this);
        }
        super.updateEntity();
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        if (node() != null) {
            final NBTTagCompound nd = new NBTTagCompound();
            compound.setTag("node", nd);
            node().save(nd);
        }
        NBTTagList nbttaglist = new NBTTagList();
        for (int i = 0; i < this.inv.length; ++i) {
            if (this.inv[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                this.inv[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
        super.writeToNBT(compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound nd = (NBTTagCompound) compound.getTag("node");
        if (nd.hasNoTags() != false && node != null) node().load(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.inv.length) {
                this.inv[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
        super.readFromNBT(compound);
    }

}
