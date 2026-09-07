package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import org.joml.Vector4i;

import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.PhantomItemSlotSH;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.ScrollWidget;
import com.cleanroommc.modularui.widget.scroll.VerticalScrollData;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.modularui2.GTGuis;
import gregtech.api.util.StringUtils;
import gregtech.api.util.GTTooltipDataCache.TooltipData;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.registration.Registration;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class PhantomInputBus extends MTEHatchInputBus implements IDataCopyablePlaceHolder {
    /**
     * GT 290's hatch base classes override getDescription() with their own hardcoded
     * "input bus / output hatch / ..." text, which shadowed every PH machine's own tooltip
     * (the Config.get(...) template passed to the constructor). Hand it back.
     */
    @Override
    public String[] getDescription() {
        return mDescriptionArray;
    }

@Override
protected boolean useMui2() {
	
	return true;
	
}












    public PhantomInputBus(int id, String name, String nameRegional, int tier) {
        super(id, name, nameRegional, tier, 64, Config.get("PIB", ImmutableMap.of()));
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id

        ));

    }

    public PhantomInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 64, aDescription, aTextures);
    }

    @Override
    public void updateSlots() {

    }


    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        // TODO Auto-generated method stub
        super.onPostTick(aBaseMetaTileEntity, aTimer);
    }

@Override
	public int getOffsetX() {

		return 0;
	}@Override
	public int getOffsetY() {

		return 0;
	}
    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new PhantomInputBus(this.mName, this.mTier, mDescriptionArray, this.mTextures);
    }
    @Override
    public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
		ModularPanel builder = GTGuis.mteTemplatePanelBuilder(this, data, syncManager,uiSettings)
				.doesAddGregTechLogo(false).doesAddGhostCircuitSlot(allowSelectCircuit()).build();
		


	
		        syncManager.registerSlotGroup("item_inv", 4);

		        String[] matrix = new String[16];
		        String repeat = StringUtils.getRepetitionOf('s', 4);
		        Arrays.fill(matrix, repeat);
		        SlotGroupWidget all = SlotGroupWidget.builder()
		            .matrix(matrix)
		            .key(
		                's',
		                // ClearableMarkSlotSH: marks are stored with stackSize 0, and MUI2's default
		                // left-click path (incrementStackCount(-1) -> max(0, 0-1) == 0) is then a
		                // no-op, so a mark could never be removed by clicking it
		                index -> new PhantomItemSlot()
		                    .syncHandler(new reobf.proghatches.gt.metatileentity.util.ClearableMarkSlotSH(
		                        new ModularSlot(inventoryHandler, index) {

		                	@Override
		                	public void putStack(ItemStack stack) {
		                		if(stack!=null) {stack=stack.copy();stack.stackSize=0;}
		                		super.putStack(stack);
		                		};
		                }.slotGroup("item_inv"))))
		            .build()
		            .pos(0, 0).size(18*4, 18*16)
		           ;
		    
				ScrollWidget<?> list = new ScrollWidget<>(new VerticalScrollData()).size(18)/*.keepScrollBarInArea(true)*/;
				list.getScrollArea().getScrollY().setScrollSize(18 * 16);
				list.child(all);
				list.size(18 * 4+4, 18 * 4) .horizontalCenter()
				.top(5)
				;
				
		
				builder.child(list);
		
		
		
		
		
		
		
		
		
		return builder;
    }
    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        final Scrollable scrollable = (Scrollable) new Scrollable().setVerticalScroll()
            .setSize(6, 40);
        for (int row = 0; row * 4 < inventoryHandler.getSlots() - 1; row++) {
            int columnsToMake = Math.min(inventoryHandler.getSlots() - row * 4, 4);
            for (int column = 0; column < columnsToMake; column++) {
                scrollable.widget(

                    /*
                     * new SlotWidget(inventoryHandler, row * 4 + column).setPos(column * 18, row * 18)
                     * .setSize(18, 18)
                     */

                    new SlotWidget(new BaseSlot(inventoryHandler, row * 4 + column) {

                        public int getSlotStackLimit() {
                            return 0;
                        };

                    }

                    ) {

                        @Override
                        public List<String> getExtraTooltip() {
                            return Arrays
                                .asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
                        }
                    }.disableShiftInsert()
                        .setHandlePhantomActionClient(true)
                        .setGTTooltip(
                            () -> new TooltipData(
                                Arrays.asList(
                                    LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
                                    LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
                                Arrays.asList(
                                    LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
                                    LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"))))
                        .setSize(18, 18)
                        .setPos(column * 18, row * 18)

                );
            }
        }
        builder.widget(
            scrollable.setSize(18 * 4 + 4, 18 * 4)
                .setPos(52, 7));
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack itemStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {

        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {

        return false;
    }

    @Override
    public boolean allowSelectCircuit() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (!(aPlayer instanceof EntityPlayerMP)) return;

        ItemStack dataStick = aPlayer.inventory.getCurrentItem();
        if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) return;

        if (dataStick.hasTagCompound() == false) {
            dataStick.setTagCompound(new NBTTagCompound());
            dataStick.getTagCompound()
                .setString("type", "PhantomInput");
            dataStick.setStackDisplayName("Phantom Input Bus Link");
        }
        if (dataStick.getTagCompound()
            .hasKey("phantom_pos") == false) {
            dataStick.getTagCompound()
                .setTag("phantom_pos", new NBTTagIntArray(new int[0]));
        } ;

        /*
         * if(dataStick.getTagCompound().hasKey("dim")==false){
         * dataStick.getTagCompound().setInteger("dim", aPlayer.getEntityWorld().provider.dimensionId);}
         * if(dataStick.getTagCompound().getInteger("dim")!=aPlayer.getEntityWorld().provider.dimensionId){
         * aPlayer.addChatMessage(new ChatComponentText("Cannot link"));
         * return;
         * }
         */

        NBTTagIntArray tag = (NBTTagIntArray) dataStick.getTagCompound()
            .getTag("phantom_pos");

        List<Vector4i> a = p(tag);
        Vector4i v;
        if (a.size() >= 1024) {
            aPlayer.addChatMessage(new ChatComponentTranslation("proghatch.phantombus.chat.link.exceed"));
        } else

            if (!a.contains(
                v = new Vector4i(
                    this.getBaseMetaTileEntity()
                        .getXCoord(),
                    this.getBaseMetaTileEntity()
                        .getYCoord(),
                    this.getBaseMetaTileEntity()
                        .getZCoord(),
                    this.getBaseMetaTileEntity()
                        .getWorld().provider.dimensionId))) {
                            a.add(v);
                            aPlayer.addChatMessage(
                                new ChatComponentTranslation("proghatch.phantombus.chat.link.success", a.size()));

                        } else {
                            aPlayer.addChatMessage(
                                new ChatComponentTranslation("proghatch.phantombus.chat.link.noop", a.size()));

                        }
        dataStick.getTagCompound()
            .setTag("phantom_pos", p(a));
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

        ds: {
            if (!(aPlayer instanceof EntityPlayerMP)) break ds;

            ItemStack dataStick = aPlayer.inventory.getCurrentItem();
            if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) break ds;
            if (dataStick.hasTagCompound() == false) {
                break ds;
            }
            if (dataStick.getTagCompound()
                .hasKey("phantom_pos") == false) {
                break ds;
            } ;
            if (dataStick.getTagCompound()
                .getString("type")
                .equals("PhantomInput") == false) {
                break ds;
            } ;
            if (aPlayer.getEntityWorld().isRemote) return true;
            NBTTagIntArray tag = (NBTTagIntArray) dataStick.getTagCompound()
                .getTag("phantom_pos");
            List<Vector4i> a = p(tag);
            int[] count = new int[1];
            a.removeIf((aw) -> {
                World w = DimensionManager.getWorld(aw.w);
                if (w == null) {
                    aPlayer
                        .addChatMessage(new ChatComponentTranslation("proghatch.phantombus.chat.world.missing", aw.w));
                    return false;
                }
                if (w.blockExists(aw.x, aw.y, aw.z) == false) {
                    aPlayer.addChatMessage(
                        new ChatComponentTranslation(
                            "proghatch.phantombus.chat.chunk.missing",
                            aw.w,
                            aw.x,
                            aw.y,
                            aw.z));
                    return false;
                } ;

                TileEntity te = w.getTileEntity(aw.x, aw.y, aw.z);
                if (te == null) {
                    aPlayer.addChatMessage(
                        new ChatComponentTranslation("proghatch.phantombus.chat.te.missing", aw.w, aw.x, aw.y, aw.z));

                    return true;
                }
                if (!(te instanceof IGregTechTileEntity)) {
                    aPlayer.addChatMessage(
                        new ChatComponentTranslation("proghatch.phantombus.chat.te.incorrect", aw.w, aw.x, aw.y, aw.z));
                    return true;
                }

                if (!(((IGregTechTileEntity) te).getMetaTileEntity() instanceof PhantomInputBus)) {
                    aPlayer.addChatMessage(
                        new ChatComponentTranslation("proghatch.phantombus.chat.te.incorrect", aw.w, aw.x, aw.y, aw.z));
                    return true;
                }
                PhantomInputBus buz = ((PhantomInputBus) (((IGregTechTileEntity) te).getMetaTileEntity()));
                if (buz != this) {
                    for (int i = 0; i < mInventory.length; i++) {
                        buz.mInventory[i] = mInventory[i];
                        if (buz.mInventory[i] != null) buz.mInventory[i] = buz.mInventory[i].copy();
                    }
                    count[0]++;
                }

                return false;
            });

            aPlayer.addChatMessage(new ChatComponentTranslation("proghatch.phantombus.chat.success", count[0]));

            dataStick.getTagCompound()
                .setTag("phantom_pos", p(a));
            return true;
        }

        return super.onRightclick(aBaseMetaTileEntity, aPlayer);
    }

    /*
     * Matter Manipulator support. The only user configuration of this bus is the set of phantom item
     * marks in mInventory (ghost stacks kept at stackSize 0) - exactly what the bus-to-bus data stick
     * copy above transfers. Deliberately NOT copied: no real stock exists here (allowPutStack/
     * canInsertItem always return false), there is no ghost circuit (allowSelectCircuit() is false),
     * the inherited disableSort/disableFilter/disableLimited flags are dead on this machine
     * (updateSlots() is a no-op and nothing goes through limitedAllowPutStack), and the "phantom_pos"
     * link list belongs to the data stick, not to the bus. The copy identifier is the class name
     * (IDataCopyablePlaceHolder default), never the "PhantomInput" string used by the link stick, so
     * an MM tag and a link stick can never be confused for one another.
     */
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        NBTTagList marks = new NBTTagList();
        for (int i = 0; i < mInventory.length; i++) {
            if (mInventory[i] == null) continue;
            NBTTagCompound mark = new NBTTagCompound();
            mark.setInteger("slot", i);
            // copy() first: writeToNBT would otherwise hand out the live stack's own tag compound
            mInventory[i].copy()
                .writeToNBT(mark);
            marks.appendTag(mark);
        }
        ret.setTag("marks", marks);
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player)
            .equals(nbt.getString("type"))) return false;
        // a copy taken from an unmarked bus is still a valid paste - returning false here would abort
        // the Matter Manipulator's whole block placement
        if (nbt.hasKey("marks")) {
            NBTTagList marks = nbt.getTagList("marks", 10);
            for (int i = 0; i < mInventory.length; i++) {
                mInventory[i] = null;
            }
            for (int i = 0; i < marks.tagCount(); i++) {
                NBTTagCompound mark = marks.getCompoundTagAt(i);
                int slot = mark.getInteger("slot");
                // the source bus may have had more slots than this one
                if (slot < 0 || slot >= mInventory.length) continue;
                ItemStack stack = ItemStack.loadItemStackFromNBT(mark);
                // marks are stored with stackSize 0, exactly as the GUI slot's putStack forces
                if (stack != null) stack.stackSize = 0;
                mInventory[slot] = stack;
            }
        }
        // nothing to rebuild afterwards: updateSlots() is overridden to do nothing and the marks are
        // read straight out of mInventory, which is also all the bus-to-bus copy above does
        return true;
    }

    public NBTTagIntArray p(List<Vector4i> c) {
        int[] l = new int[c.size() * 4];
        for (int i = 0; i < c.size(); i++) {
            Vector4i xx = c.get(i);
            l[i * 4] = xx.x;
            l[i * 4 + 1] = xx.y;
            l[i * 4 + 2] = xx.z;
            l[i * 4 + 3] = xx.w;
        }

        return new NBTTagIntArray(l);
    }

    public List<Vector4i> p(NBTTagIntArray tag) {
        int[] arr = tag.func_150302_c();
        List<Vector4i> l = new ArrayList<Vector4i>(arr.length / 4);
        for (int i = 0; i < arr.length / 4; i++) {

            l.add(new Vector4i(arr[i * 4], arr[i * 4 + 1], arr[i * 4 + 2], arr[i * 4 + 3]));

        }
        return l;
    }

}
