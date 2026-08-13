package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.NumberFormatMUI;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TabButton;
import com.gtnewhorizons.modularui.common.widget.TabContainer;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.misc.ItemEncodedPattern;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.misc.TileInterface;
import appeng.util.PatternMultiplierHelper;
import codechicken.nei.ItemStackMap;
import codechicken.nei.ItemStackSet;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.modularui2.GTGuiTextures;
import com.cleanroommc.modularui.api.IPanelHandler;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.GuiTextures;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.InteractionSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.utils.item.ItemStackHandler;
import com.cleanroommc.modularui.widget.ParentWidget;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import gregtech.api.modularui2.GTGuis;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputHatch;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.ExConfig;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.ExConfigEntry;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch.DA;
import reobf.proghatches.gt.metatileentity.bufferutil.ItemStackG;
import reobf.proghatches.gt.metatileentity.bufferutil.LongWrapper;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.ISpecialOptimize;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.MasterSetMessage;
import reobf.proghatches.net.TryOpenPatternCIRBMessage;

@gregtech.api.interfaces.metatileentity.IMetaTileEntity.SkipGenerateDescription
public class PatternDualInputHatchInventoryMappingSlave<T extends DualInputHatch & IDualInputHatch & IMetaTileEntity>
    extends MTETieredMachineBlock
    implements IAddUIWidgets, ICraftingMedium, ICustomNameObject, IGridProxyable, IInterfaceViewable,
    IPowerChannelState, IActionHost, ICraftingProvider,IAddGregtechLogo, IMultiplePatternPushable, IDataCopyablePlaceHolder,ISpecialOptimize {
	public static IntSyncValue accessorI(Supplier<Boolean> object, Consumer<Boolean> object2) {
		
		return (IntSyncValue) new IntSyncValue(()->object.get()?1:0, s->object2.accept(s==1)).allowC2S();
	}
	public static abstract class ExConfigEntry{
		public CycleButtonWidget asMUI1(int x,int y) {
			return (CycleButtonWidget) new CycleButtonWidget().setToggle(this::get, this::set).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
				.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
				.setPos(3 + 18 * x, 3 + 18 * y).setSize(18, 18)
				.addTooltips((List<String>)Stream.of(tips()).map(s->StatCollector.translateToLocal(s)).collect(Collectors.toList()));
		}
		public com.cleanroommc.modularui.widgets.CycleButtonWidget asMUI2(int x,int y) {
			return new com.cleanroommc.modularui.widgets.CycleButtonWidget()
					.stateCount(2)
					.value(accessorI(this::get, this::set))
					
					.stateBackground(1, GTGuiTextures.BUTTON_STANDARD_PRESSED)
					.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
				      .stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
                     .stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_CHECKMARK)
					
					
				
					.pos(3 + 18 * x, 3 + 18 * y).size(18, 18)
					.tooltipBuilder(sx->{Stream.of(tips()).map(s->StatCollector.translateToLocal(s)).forEach(sx::addLine);});

		}		
		
		public abstract boolean shouldApply();
		public abstract boolean get();
		public abstract void set(boolean b);
		public abstract String[] tips();
		public static ExConfigEntry create(Supplier<Boolean> get,Consumer<Boolean> set,String... tips) {
		return create(()->true, get, set, tips);
		}
		public static ExConfigEntry create(Supplier<Boolean> apply,Supplier<Boolean> get,Consumer<Boolean> set,String... tips) {
			
			return new ExConfigEntry() {

				@Override
				public boolean shouldApply() {
				
					return apply.get();
				}

				@Override
				public boolean get() {
					return get.get();
				}

				@Override
				public void set(boolean b) {
					 set.accept(b);
					
				}

				@Override
				public String[] tips() {
					
					return tips;
				}};
		}
		
	}
	public static class ExConfig{
		Table<Integer, Integer, ExConfigEntry> table = HashBasedTable.create();
		public void reg(int x,int y,ExConfigEntry xx) {
			ExConfigEntry old = table.put(x, y, xx);
			if(old !=null) {throw new AssertionError("NO");}
		}
	}
	public ExConfig exconfig=initExConfig();
	
	public ExConfig initExConfig() {
		ExConfig ret=new ExConfig();
		///////////
   /* 	builder.widget(new CycleButtonWidget().setToggle(() -> allowopt, (s) -> {
    		allowopt = s;

    	}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
    			.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
    			.setPos(3 + 18 * 1, 3 + 18 * 1).setSize(18, 18)
    			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.allowopt.0"))
    			//.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.allowopt.1"))
    		);	
    	
     	builder.widget(new CycleButtonWidget().setToggle(() -> inherit, (s) -> {
     		inherit = s;

    	}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
    			.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
    			.setPos(3 + 18 * 2, 3 + 18 * 1).setSize(18, 18)
    			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.inherit.0"))
    			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.inherit.1"))
    		);	
     	builder.widget(new CycleButtonWidget().setToggle(() -> normalopt, (s) -> {
     		normalopt = s;

    	}).setStaticTexture(GTUITextures.OVERLAY_BUTTON_CHECKMARK)
    			.setVariableBackground(GTUITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
    			.setPos(3 + 18 * 3, 3 + 18 * 1).setSize(18, 18)
    			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.normalopt.0"))
    			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.normalopt.1"))
    		);	    	*/
		/// ////
		ret.reg(1, 1, ExConfigEntry.create(
				() -> allowopt, 
				(s) -> {allowopt = s;},
				"programmable_hatches.gt.allowopt.0",
				"programmable_hatches.gt.allowopt.1"));
		ret.reg(2, 1, ExConfigEntry.create(
				() -> inherit, 
				(s) -> {inherit = s;},
				"programmable_hatches.gt.inherit.0",
				"programmable_hatches.gt.inherit.1"
				));

		ret.reg(3, 1, ExConfigEntry.create(
				() -> normalopt, 
				(s) -> {normalopt = s;},
				"programmable_hatches.gt.normalopt.0",
				"programmable_hatches.gt.normalopt.1"
				));
		
		
		return ret;
	}
    private T master; // use getMaster() to access
    public int masterX, masterY, masterZ;
    public boolean masterSet = false; // indicate if values of masterX,
                                      // masterY, masterZ are valid

    public PatternDualInputHatchInventoryMappingSlave(int aID, String aName, String aNameRegional, int aTier) {
        super(
            aID,
            aName,
            aNameRegional,
            aTier,
            0,

            Config.get("PDIHIMS", ImmutableMap.of()));
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
    }

    public PatternDualInputHatchInventoryMappingSlave(String aName, int aTier, int aInvSlotCount, String[] aDescription,
        ITexture[][][] aTextures) {
        super(aName, aTier, aInvSlotCount, aDescription, aTextures);

    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new PatternDualInputHatchInventoryMappingSlave<>(mName, mTier, 0, mDescriptionArray, mTextures);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setBoolean("additionalConnection", additionalConnection);
        NBTTagCompound tag = new NBTTagCompound();// aNBT.getCompoundTag("patternSlots");

        for (int i = 0; i < pattern.length; i++) {
            final int ii = i;
            Optional.ofNullable(pattern[i])
                .map(s -> s.writeToNBT(new NBTTagCompound()))
                .ifPresent(s -> tag.setTag("i" + ii, s));
        }
        aNBT.setTag("patternSlots", tag);
        Optional.ofNullable(customName)
            .ifPresent(s -> aNBT.setString("customName", s));
        getProxy().writeToNBT(aNBT);

        if (masterSet) {
            NBTTagCompound masterNBT = new NBTTagCompound();
            masterNBT.setInteger("x", masterX);
            masterNBT.setInteger("y", masterY);
            masterNBT.setInteger("z", masterZ);
            aNBT.setTag("master", masterNBT);
        }

        aNBT.setIntArray("multiplier", multiplier);
        aNBT.setBoolean("allowopt", allowopt);
        aNBT.setBoolean("inherit", inherit);
        aNBT.setBoolean("normalopt", normalopt);
        
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        additionalConnection = aNBT.getBoolean("additionalConnection");
        NBTTagCompound tag = aNBT.getCompoundTag("patternSlots");
        if (tag != null) for (int i = 0; i < pattern.length; i++) {
            pattern[i] = Optional.ofNullable(tag.getCompoundTag("i" + i))
                .map(ItemStack::loadItemStackFromNBT)
                .orElse(null);
        }
        customName = aNBT.getString("customName");

        getProxy().readFromNBT(aNBT);

        updateValidGridProxySides();

        if (aNBT.hasKey("master")) {
            NBTTagCompound masterNBT = aNBT.getCompoundTag("master");
            masterX = masterNBT.getInteger("x");
            masterY = masterNBT.getInteger("y");
            masterZ = masterNBT.getInteger("z");
            masterSet = true;
        }
        multiplier = aNBT.getIntArray("multiplier");
        if (multiplier.length < 36) multiplier = new int[36];
        for (int i = 0; i < multiplier.length; i++) {
            multiplier[i] = Math.max(multiplier[i], 1);
        }
        allowopt=aNBT.getBoolean("allowopt");
        if(!aNBT.hasKey("inherit"))aNBT.setBoolean("inherit", true);
        inherit=aNBT.getBoolean("inherit");
        normalopt=aNBT.getBoolean("normalopt");
    }

    @Override
    public boolean shouldDropItemAt(int index) {
        return false;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {

        return true;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
        int colorIndex, boolean active, boolean redstoneLevel) {

        if (side != facing)

            return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1] };
        else return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1],
            TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_inv_me_slave) };

    }

    @Override
    public ITexture[][][] getTextureSet(ITexture[] aTextures) {

        return new ITexture[0][0][0];
    }

    long lastSync;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
            trySetMasterFromCoord(masterX, masterY, masterZ);
        }
        if (getBaseMetaTileEntity().isServerSide()) {
            if (needPatternSync /*&& aTimer > lastSync + 100*/) {
                needPatternSync = !postMEPatternChange();
                lastSync = aTimer;
            }
            if (aTimer % 20 == 0) {
                getBaseMetaTileEntity().setActive(isActive());
            }
        }

    }

    @SuppressWarnings("unchecked")
    public IDualInputHatch trySetMasterFromCoord(int x, int y, int z) {
        TileEntity tileEntity = getBaseMetaTileEntity().getWorld()
            .getTileEntity(x, y, z);
        if (tileEntity == null) return null;
        if (!(tileEntity instanceof IGregTechTileEntity)) return null;
        IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
        if (!(metaTileEntity instanceof IDualInputHatch)) return null;
        // if (!(metaTileEntity instanceof IPHDual)){}
        if (!(metaTileEntity instanceof reobf.proghatches.gt.metatileentity.DualInputHatch)) return null;

        masterX = x;
        masterY = y;
        masterZ = z;
        masterSet = true;
        master = (T) metaTileEntity;
        return master;
    }

    private boolean tryLinkDataStick(EntityPlayer aPlayer) {
        ItemStack dataStick = aPlayer.inventory.getCurrentItem();

        if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) {
            return false;
        }
        if (dataStick.hasTagCompound() && dataStick.stackTagCompound.getString("type")
            .equals("CraftingInputBuffer")) {
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.slave.compat"));
            return false;
        }
        if (!dataStick.hasTagCompound() || !dataStick.stackTagCompound.getString("type")
            .equals("ProgHatchesDualInput")) {
            return false;
        }

        NBTTagCompound nbt = dataStick.stackTagCompound;
        int x = nbt.getInteger("x");
        int y = nbt.getInteger("y");
        int z = nbt.getInteger("z");
        if (trySetMasterFromCoord(x, y, z) != null) {
            aPlayer.addChatMessage(new ChatComponentText("Link successful"));
            return true;
        }
        aPlayer.addChatMessage(new ChatComponentText("Link failed"));
        return true;
    }

    /*
     * (non-Javadoc)
     * @see
     * gregtech.api.metatileentity.MetaTileEntity#onRightclick(gregtech.api.
     * interfaces.tileentity.IGregTechTileEntity,
     * net.minecraft.entity.player.EntityPlayer,
     * net.minecraftforge.common.util.ForgeDirection, float, float, float)
     */
    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
       /* if (!(aPlayer instanceof EntityPlayerMP)) {
            return false;
        }*/
        final ItemStack is = aPlayer.inventory.getCurrentItem();
        if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
            if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0) return false;
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }
        if (tryLinkDataStick(aPlayer)) {
            return true;
        }

        // MUI2: open our own GUI directly on the server. The old client->server
        // TryOpenPatternCIRB round-trip existed only for the (disabled) host-UI-proxy logic.
        if (aBaseMetaTileEntity.isClientSide()) return true;
        openGui(aPlayer);
        return true;

    }

    public T getMaster() {
        if (master == null) return null;
        if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master
                                                                          // disappeared
            master = null;
        }
        return master;
    }

    public ForgeDirection getMasterFront() {
        if (master == null) return this.getBaseMetaTileEntity()
            .getFrontFacing();// throw new
                              // RuntimeExcpetion()?
        // do not check master, becasue it's always called after getMaster()
        return master.getBaseMetaTileEntity()
            .getFrontFacing();
    }

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        currenttip.add((tag.getBoolean("linked") ? "Linked" : "Not linked/Not loaded"));

        if (tag.hasKey("masterX")) {
            currenttip.add(
                "Bound to " + tag
                    .getInteger("masterX") + ", " + tag.getInteger("masterY") + ", " + tag.getInteger("masterZ"));
        }

        if (tag.hasKey("masterName")) {
            currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName") + EnumChatFormatting.RESET);
        }

        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
        int z) {

        tag.setBoolean("linked", getMaster() != null);
        if (masterSet) {
            tag.setInteger("masterX", masterX);
            tag.setInteger("masterY", masterY);
            tag.setInteger("masterZ", masterZ);
        }
        if (getMaster() != null) tag.setString("masterName", getNameOf(getMaster()));
        /*
         * if (getMaster() != null) tag.setString("masterName",
         * getMaster().getnam);
         */

        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }

    public String getNameOf(T tg) {

        if (tg instanceof ICustomNameObject) {
            ICustomNameObject iv = (ICustomNameObject) tg;
            if (iv.hasCustomName()) return iv.getCustomName();

        }

        StringBuilder name = new StringBuilder();
        if (tg instanceof ICraftingMedium && ((ICraftingMedium) tg).getCrafterIcon() != null) {
            name.append(
                ((ICraftingMedium) tg).getCrafterIcon()
                    .getDisplayName());
        } else {
            name.append(tg.getLocalName());
        }

        return name.toString();
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {

        return true;
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
	static int EX_CONFIG = 0x985211;
    public static boolean enclose;
    ButtonWidget createPowerSwitchButton(IWidgetBuilder<?> builder) {
		IGregTechTileEntity thiz = this.getBaseMetaTileEntity();
		Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {
			if (clickData.shift == true) {
				if (widget.getContext().isClient() == false)
					widget.getContext().openSyncedWindow(EX_CONFIG);
				return;

			}
			/*if (thiz.isAllowedToWork()) {
				thiz.disableWorking();
			} else {
				thiz.enableWorking();
				// BufferedDualInputHatch bff =(BufferedDualInputHatch)
				// (thiz).getMetaTileEntity();
				
			}*/
		}).setPlayClickSoundResource(() -> thiz.isAllowedToWork() ? SoundResource.GUI_BUTTON_UP.resourceLocation
				: SoundResource.GUI_BUTTON_DOWN.resourceLocation).setBackground(() -> {
					if (thiz.isAllowedToWork()) {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
					} else {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
					}
				}).attachSyncer(new FakeSyncWidget.BooleanSyncer(thiz::isAllowedToWork, val -> {
					if (val)
						thiz.enableWorking();
					else
						thiz.disableWorking();
				}), builder).addTooltip(LangManager.translateToLocal("GT5U.gui.button.power_switch"))
				.addTooltip(LangManager.translateToLocal("proghatch.gui.button.power_switch.ex"))
				.setTooltipShowUpDelay(TOOLTIP_DELAY)
				 .setPos(new Pos2d(getGUIWidth() - 18 - 3-18, 5 + 16 + 2 + 16 + 2 + 18 + 24))
				//.setPos(new Pos2d(getGUIWidth() - 18 - 3, 5))
				
				.setSize(16, 16);
		return (ButtonWidget) button;
	}

    

    // @Override
    public boolean useModularUI() {

        return true;
    }

    private AENetworkProxy gridProxy;

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            gridProxy = new AENetworkProxy(
                this,
                "proxy",
                new ItemStack(
                    GregTechAPI.sBlockMachines,
                    1,
                    this.getBaseMetaTileEntity()
                        .getMetaTileID()),
                true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return getProxy().getNode();
    }

    @Override
    public void securityBreak() {

    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord(getTileEntity());
    }

    boolean additionalConnection;

    private void updateValidGridProxySides() {
        if (additionalConnection) {
            getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(ForgeDirection.UNKNOWN)));
        } else {
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        }
        // getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

    }

    @Override
    public IGridNode getActionableNode() {

        return this.getGridNode(ForgeDirection.UP);
    }

    @Override
    public boolean isPowered() {
        return getProxy() != null && getProxy().isPowered();
    }

    @Override
    public boolean isActive() {
        return getProxy() != null && getProxy().isActive();
    }

    @Override
    public int rows() {
        return 4;
    }

    @Override
    public int rowSize() {
        return 9;
    }

    @Override
    public IInventory getPatterns() {

        return patternMapper;
    }

    @Override
    public String getName() {
        if (hasCustomName()) {
            return getCustomName();
        }

        T m = getMaster();
        if (m == null) {
            return getLocalName();
        }
        StringBuilder name = new StringBuilder();
        String masterName;
        if (m instanceof IInterfaceViewable) {
            masterName = ((IInterfaceViewable) m).getName();
        } else {
            masterName = Optional.ofNullable(m.getMachineCraftingIcon())
                .map(ItemStack::getDisplayName)
                .orElse(m.getLocalName() == null ? getLocalName() : m.getLocalName());
        }
        name.append(masterName);
        name.append("(Mapped)");
        if (m.mInventory[m.getCircuitSlot()] != null) {
            name.append(" - ");
            ItemStack is = m.mInventory[m.getCircuitSlot()];
            if (is.getItem() != GTUtility.getIntegratedCircuit(0)
                .getItem()) {
                name.append(is.getDisplayName());
                if (is.getItemDamage() > 0) {
                    name.append("@")
                        .append(is.getItemDamage());
                }
            } else {
                name.append(is.getItemDamage());
            }
        }

        return name.toString();
    }

    @Override
    public TileEntity getTileEntity() {
        return (TileEntity) getBaseMetaTileEntity();
    }

    @Override
    public boolean shouldDisplay() {

        return true;
    }

    int n;
    ItemStack[] pattern = new ItemStack[36];
    IInventory patternMapper = new IInventory() {

        @Override
        public int getSizeInventory() {

            return pattern.length;
        }

        @Override
        public ItemStack getStackInSlot(int slotIn) {

            return pattern[slotIn];
        }

        @Override
        public ItemStack decrStackSize(int index, int count) {

            try {
                if (pattern[index] != null) {
                    ItemStack itemstack;

                    if (pattern[index].stackSize <= count) {
                        itemstack = pattern[index];
                        pattern[index] = null;
                        this.markDirty();
                        return itemstack;
                    } else {
                        itemstack = pattern[index].splitStack(count);

                        if (pattern[index].stackSize == 0) {
                            pattern[index] = null;
                        }

                        this.markDirty();
                        return itemstack;
                    }
                } else {
                    return null;
                }
            } finally {

                onPatternChange();
            }
        }

        @Override
        public ItemStack getStackInSlotOnClosing(int index) {

            return null;
        }

        @Override
        public void setInventorySlotContents(int index, ItemStack stack) {
            pattern[index] = stack;
            onPatternChange();
        }

        @Override
        public String getInventoryName() {

            return "";
        }

        @Override
        public boolean hasCustomInventoryName() {

            return false;
        }

        // @Override
        // public int stack

        @Override
        public void markDirty() {

        }

        @Override
        public boolean isUseableByPlayer(EntityPlayer player) {

            return true;
        }

        @Override
        public void openInventory() {}

        @Override
        public void closeInventory() {}

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {

            return true;
        }

        @Override
        public int getInventoryStackLimit() {

            return 1;
        }
    };
    boolean needPatternSync;
    private String customName;

    private void onPatternChange() {
        if (!getBaseMetaTileEntity().isServerSide()) return;

        needPatternSync = true;
    }

    @Override
    public void gridChanged() {
        needPatternSync = true;
    }

    @Override
    public String getCustomName() {

        return customName;
    }

    @Override
    public void setCustomName(String name) {
        customName = name;

    }

    @Override
    public boolean hasCustomName() {

        return customName != null && (!customName.isEmpty());
    }

    public boolean isInputEmpty(T master) {

        for (FluidTank f : master.mStoredFluid) {
            if (f.getFluidAmount() > 0) {
                return false;
            }
        }
        for (ItemStack i : master.mInventory) {

            if (i != null && i.stackSize > 0) {
                return false;
            }
        }
        return true;
    }

    public void clearInv(T master) {

        for (FluidTank f : master.mStoredFluid) {
            f.setFluid(null);
        }
        for (int i = 0; i < master.mInventory.length; i++) {

            if (master.isValidSlot(i)) {
                master.mInventory[i] = null;
            }
        }

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
        T master = getMaster();
        if (getMaster() instanceof PatternDualInputHatch) {
            PatternDualInputHatch dih = ((PatternDualInputHatch) getMaster());
            try {
                dih.skipActiveCheck = true;
                return dih.pushPattern(patternDetails, table);
            } finally {
                dih.skipActiveCheck = false;
            }
        }
        if (master != null) {
            if (!isInputEmpty(master)) {
                return false;
            }

            int i = 0;
            int f = 0;
            int ilimit = master.getInventoryStackLimit();
            int flimit = master.getInventoryFluidLimit();
            boolean isplit = master.disableLimited;
            boolean fsplit = master.fluidLimit == 0;
            for (int index = 0; index < table.getSizeInventory(); index++) {
                ItemStack is = (table.getStackInSlot(index));
                if (is == null) continue;
                is = is.copy();
                if (is.getItem() instanceof ItemFluidPacket) {
                    FluidStack fs = ItemFluidPacket.getFluidStack(is);
                    if (fs == null) {
                        continue;
                    }
                    while (fs.amount > 0) {
                        if (f >= master.mStoredFluid.length) {
                            clearInv(master);
                            return false;
                        }
                        int tosplit = Math.min(fs.amount, flimit);
                        fs.amount -= tosplit;
                        if ((!fsplit) && fs.amount > 0) {
                            clearInv(master);
                            return false;
                        }
                        FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
                        master.mStoredFluid[f].setFluidDirect(splitted);
                        f++;
                    }

                } else {
                    while (is.stackSize > 0) {
                        if (master.isValidSlot(i) == false) {
                            clearInv(master);
                            return false;
                        }
                        ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
                        if ((!isplit) && is.stackSize > 0) {
                            clearInv(master);
                            return false;
                        }
                        master.mInventory[i] = splitted;
                        i++;
                    }
                }

            }
            if (master instanceof BufferedDualInputHatch) {
                BufferedDualInputHatch m = (BufferedDualInputHatch) master;

                DualInvBuffer theBuffer = ((BufferedDualInputHatch) master).classifyForce();
                if (theBuffer != null) {
                    theBuffer.onChange();
                }
                // ((BufferedDualInputHatch) master).classifyForce();
            }
            // the writes above bypass setInventorySlotContents/fill, so raise the wake flags manually
            if (master instanceof BufferedDualInputHatch) {
                ((BufferedDualInputHatch) master).justHadNewItems = true;
            }
            if (master.getBaseMetaTileEntity() instanceof gregtech.api.interfaces.tileentity.IHasInventory inv) {
                inv.markInventoryBeenModified();
            }
            return true;// hoo ray
        }

        return false;
    }

    @Override
    public boolean isBusy() {
        T master = getMaster();
        if (master instanceof PatternDualInputHatch) {
            return ((PatternDualInputHatch) getMaster()).isBusy();
        }
        if (master != null) {

            if (!isInputEmpty(master)) {
                return true;
            }

        }

        return false;
    }

    private boolean postMEPatternChange() {
        // don't post until it's active
        if (!getProxy().isActive()) return false;
        try {
            getProxy().getGrid()
                .postEvent(new MENetworkCraftingPatternChange(this, getProxy().getNode()));
        } catch (GridAccessException ignored) {
            return false;
        }
        return true;
    }

    ItemStack[] patternItemCache = new ItemStack[36];
    ICraftingPatternDetails[] patternDetailCache = new ICraftingPatternDetails[36];

    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (!isActive()) return;

        for (int index = 0; index < pattern.length; index++) {
            ItemStack slot = pattern[index];

            if (slot == null) {
                patternItemCache[index] = null;
                patternDetailCache[index] = null;
                continue;
            }

            if (patternItemCache[index] == pattern[index]) {// just compare object id
                craftingTracker.addCraftingOption(this, patternDetailCache[index]);
                continue;
            }

            ICraftingPatternDetails details = null;
            try {
                details = ((ICraftingPatternItem) slot.getItem()).getPatternForItem(
                    slot,
                    this.getBaseMetaTileEntity()
                        .getWorld());
            } catch (Exception e) {}
            if (details == null) {
                GTMod.GT_FML_LOGGER.warn(
                    "Found an invalid pattern at " + getBaseMetaTileEntity().getCoords()
                        + " in dim "
                        + getBaseMetaTileEntity().getWorld().provider.dimensionId);
                continue;
            }
            patternItemCache[index] = pattern[index];
            patternDetailCache[index] = multiplier[index] == 1 ? details : new DA(details, multiplier[index]);
            craftingTracker.addCraftingOption(this, patternDetailCache[index]);
        }

    }

    int[] multiplier = new int[36];
    {
        Arrays.fill(multiplier, 1);
    }

    public void refresh() {
        for (int i = 0; i < patternItemCache.length; i++) {
            patternItemCache[i] = null;
        }
        postMEPatternChange();

    }

    

    private static String ps(int amount) {
        return numberFormatx.formatWithSuffix(amount);

    }

    private static final NumberFormatMUI numberFormatx = new NumberFormatMUI();

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
        return isOutputFacing(forgeDirection) ? AECableType.SMART : AECableType.NONE;
    }

    @Override
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    @Override
    public ItemStack getCrafterIcon() {
        ItemStack is = this.getMachineCraftingIcon();
        return is == null ? new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()) : is;
    }

    @Override
    public int getGUIHeight() {

        return super.getGUIHeight() + 20;
    }

    @Override
    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
        float aX, float aY, float aZ, ItemStack is) {
        additionalConnection = !additionalConnection;
        updateValidGridProxySides();
        aPlayer.addChatComponentMessage(
            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + additionalConnection));
        return true;
    }

    static int[] AZERO = { 0 };
    @Override
    public boolean allowsPatternOptimization() {
       if(getMaster() instanceof IInterfaceViewable){
    	   IInterfaceViewable mast=(IInterfaceViewable) getMaster();
    	   if(inherit)return mast.allowsPatternOptimization();
       }
        return allowopt;
    }
    boolean inherit;
    boolean allowopt=true;
public boolean shouldDisplayMaster;



public Map<EntityPlayer/*EntityPlayer*/,Boolean> playerConfig=new WeakHashMap<>();

public boolean playerConfigClient;

    @Override
    public int[] pushPatternMulti(ICraftingPatternDetails patternDetails, InventoryCrafting table, int maxTodo) {
        if (Config.fastPatternDualInput == false) return AZERO;

        if (getMaster() instanceof PatternDualInputHatch) {
            PatternDualInputHatch dih = ((PatternDualInputHatch) getMaster());
            try {
                dih.skipActiveCheck = true;
                return dih.pushPatternMulti(patternDetails, table, maxTodo);
            } finally {
                dih.skipActiveCheck = false;
            }
        }
        int suc = 0;
        if (master != null) {
            if (!isInputEmpty(master)) {
                return AZERO;
            }

            int i = 0;
            int f = 0;
            int ilimit = master.getInventoryStackLimit();
            int flimit = master.getInventoryFluidLimit();
            boolean isplit = master.disableLimited;
            boolean fsplit = master.fluidLimit == 0;
            for (int index = 0; index < table.getSizeInventory(); index++) {
                ItemStack is = (table.getStackInSlot(index));
                if (is == null) continue;
                is = is.copy();
                if (is.getItem() instanceof ItemFluidPacket) {
                    FluidStack fs = ItemFluidPacket.getFluidStack(is);
                    if (fs == null) {
                        continue;
                    }
                    while (fs.amount > 0) {
                        if (f >= master.mStoredFluid.length) {
                            clearInv(master);
                            return AZERO;
                        }
                        int tosplit = Math.min(fs.amount, flimit);
                        fs.amount -= tosplit;
                        if ((!fsplit) && fs.amount > 0) {
                            clearInv(master);
                            return AZERO;
                        }
                        FluidStack splitted = new FluidStack(fs.getFluid(), tosplit);
                        master.mStoredFluid[f].setFluidDirect(splitted);
                        f++;
                    }

                } else {
                    while (is.stackSize > 0) {
                        if (master.isValidSlot(i) == false) {
                            clearInv(master);
                            return AZERO;
                        }
                        ItemStack splitted = is.splitStack(Math.min(is.stackSize, ilimit));
                        if ((!isplit) && is.stackSize > 0) {
                            clearInv(master);
                            return AZERO;
                        }
                        master.mInventory[i] = splitted;
                        i++;
                    }
                }

            }
            suc++;
            maxTodo--;

            if (master instanceof BufferedDualInputHatch) {

                BufferedDualInputHatch m = (BufferedDualInputHatch) master;
                /*
                 * Integer check = m.detailmap.get(patternDetails);
                 * if(check==null){
                 * m.currentID++;
                 * m.detailmap.put(patternDetails,m.currentID );
                 * check=m.currentID;
                 * }
                 */

                DualInvBuffer theBuffer = ((BufferedDualInputHatch) master).classifyForce();
                // DualInvBuffer theBuffer=((BufferedDualInputHatch) master).classifyForce();

                if (theBuffer != null) {
                    int todo = Math.min(theBuffer.space(), maxTodo);

                    if (todo > 0) {
                        for (int ix = 0; ix < theBuffer.i; ix++) {
                            if (theBuffer.mStoredItemInternalSingle[ix] != null) {
                                if (theBuffer.mStoredItemInternal[ix] == null) {
                                    theBuffer.mStoredItemInternal[ix] = ItemStackG
                                        .neo(theBuffer.mStoredItemInternalSingle[ix].copy());
                                    theBuffer.mStoredItemInternal[ix].stackSize(0);// circuit?
                                }
                                theBuffer.mStoredItemInternal[ix]
                                    .stackSizeInc(new LongWrapper(theBuffer.mStoredItemInternalSingle[ix].stackSize *1L* todo));
                            }
                        }

                        for (int ix = 0; ix < theBuffer.f; ix++) {
                            if (theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() > 0) {
                                if (theBuffer.mStoredFluidInternal[ix].getFluidAmount() <= 0) {
                                    FluidStack zerof = theBuffer.mStoredFluidInternalSingle[ix].getFluid()
                                        .copy();
                                    zerof.amount = 0;
                                    theBuffer.mStoredFluidInternal[ix].setFluid(zerof);

                                }
                                theBuffer.mStoredFluidInternal[ix]
                                    .amountAcc(theBuffer.mStoredFluidInternalSingle[ix].getFluidAmount() * 1l * todo);
                            }
                        }
                        theBuffer.nonempty=true;
                        suc += todo;
                    }
                    theBuffer.onChange();
                }

            }

            if (master instanceof BufferedDualInputHatch) {
                ((BufferedDualInputHatch) master).justHadNewItems = true;
            }
            if (master.getBaseMetaTileEntity() instanceof gregtech.api.interfaces.tileentity.IHasInventory inv) {
                inv.markInventoryBeenModified();
            }
            if (master instanceof PatternDualInputHatch) {
                ((PatternDualInputHatch) master).saved += suc;
            }

        }

        return new int[] { suc };

    }

    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        ret.setInteger("masterX", masterX);
        ret.setInteger("masterY", masterY);
        ret.setInteger("masterZ", masterZ);
        ret.setBoolean("masterSet", masterSet);

        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        if (nbt.hasKey("masterX")) masterX = nbt.getInteger("masterX");
        if (nbt.hasKey("masterY")) masterY = nbt.getInteger("masterY");
        if (nbt.hasKey("masterZ")) masterZ = nbt.getInteger("masterZ");
        if (nbt.hasKey("masterSet")) masterSet = nbt.getBoolean("masterSet");
        master = null;
        return true;
    }

    @Override
    public void onBlockDestroyed() {

        super.onBlockDestroyed();

        IGregTechTileEntity te = this.getBaseMetaTileEntity();
        World aWorld = te.getWorld();
        int aX = te.getXCoord();
        short aY = te.getYCoord();
        int aZ = te.getZCoord();

        for (int i = 0; i < pattern.length; i++) {
            final ItemStack tItem = pattern[i];
            if ((tItem != null) && (tItem.stackSize > 0)) {
                final EntityItem tItemEntity = new EntityItem(
                    aWorld,
                    aX + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aY + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    aZ + XSTR_INSTANCE.nextFloat() * 0.8F + 0.1F,
                    new ItemStack(tItem.getItem(), tItem.stackSize, tItem.getItemDamage()));
                if (tItem.hasTagCompound()) {
                    tItemEntity.getEntityItem()
                        .setTagCompound(
                            (NBTTagCompound) tItem.getTagCompound()
                                .copy());
                }
                tItemEntity.motionX = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                tItemEntity.motionY = (XSTR_INSTANCE.nextGaussian() * 0.25D);
                tItemEntity.motionZ = (XSTR_INSTANCE.nextGaussian() * 0.05D);
                aWorld.spawnEntityInWorld(tItemEntity);
                tItem.stackSize = 0;
                pattern[i] = null;
            }
        }
    }
    
    boolean normalopt;
    @Override
	public void optimize(ItemStackMap<Pair<Object, Integer>> lookupMap) {
		IInventory patternInv = this.getPatterns();

		for (int i = 0; i < patternInv.getSizeInventory(); i++) {
			ItemStack stack = patternInv.getStackInSlot(i);

			if (stack == null) {
				continue;
			}
			if(multiplier[i]>1){
				stack=new PatternDualInputHatch.DA(
						((ICraftingPatternItem)stack.getItem()).getPatternForItem(stack,this.getBaseMetaTileEntity().getWorld())
						, multiplier[i]).getPattern();
			}
			/*if ((stack.getItem() instanceof ItemFakePattern) && (stack.hasTagCompound() == true)
					&& (3 == stack.getTagCompound().getInteger("type"))) {
				PatternDualInputHatch.DA parent = (DA) ((ItemFakePattern) stack.getItem()).getPatternForItem(stack,
						null);
				ICraftingPatternDetails wrapped = parent.p;

				stack = wrapped.getPattern();
			}*/
			
			

			Pair<Object, Integer> pair = lookupMap.get(stack);
			if (pair == null)
				continue;
			Integer bitMultiplier = pair.getValue();
			if (bitMultiplier == 0)
				return;
			boolean isDividing = false;
			if (bitMultiplier < 0) {
				isDividing = true;
				bitMultiplier = -bitMultiplier;
			}
			
			if(normalopt) {
				 PatternMultiplierHelper.applyModification(stack, bitMultiplier);
				 patternInv.setInventorySlotContents(i, stack);
			}
			else {
			multiplier[i] = isDividing ? multiplier[i] >> bitMultiplier : multiplier[i] << bitMultiplier;
			if (multiplier[i] <= 0) {
				multiplier[i] = 1;
			}
			}
			markDirty();
			onPatternChange(); 
			refresh();
			/*
			 * ItemStack sCopy = sdtack.copy();
			 * pair.getKey().applyModification(sCopy, pair.getValue());
			 * patternInv.setInventorySlotContents(i, sCopy);
			 */

		}

	}
    @Override
	public void blacklist(ItemStackSet black) {
		for(ICraftingPatternDetails a:patternDetailCache){
			if(a!=null)black.add(a.getPattern());
		}
	}
    @Override
    public void addGregTechLogo(Builder builder) {
 //no-op
    }

	// ===================== MUI2 =====================
	// This MTE extends MTETieredMachineBlock, whose useMui2() returns true in GT 5.09.54.x, so the
	// legacy MUI1 UI stopped being called and the pattern "+" button vanished. Rebuilt for MUI2.
	// The host's UI is deliberately NOT proxied into this panel (that logic is disabled: the host
	// may be far away / unloaded). Instead: left-click the block, or use the button below, to open
	// the host GUI via the same forwarding the plain slaves use (GTNH's mixin handles remote hosts).

	@Override
	public ModularPanel buildUI(PosGuiData data, PanelSyncManager syncManager, UISettings uiSettings) {
		ModularPanel builder = GTGuis.mteTemplatePanelBuilder(this, data, syncManager, uiSettings)
			.doesAddGregTechLogo(false)
			.build();

		if (masterSet) trySetMasterFromCoord(masterX, masterY, masterZ);

		// Host info is decided on the server and synced as "<mName>|x,y,z" (empty = no host); only the
		// UNLOCALIZED machine name is sent, so the client localizes it with its own language via the
		// "gt.blockmachines.<mName>.name" key that GT registers on both sides.
		com.cleanroommc.modularui.value.sync.StringSyncValue hostInfo =
			new com.cleanroommc.modularui.value.sync.StringSyncValue(() -> {
				T m = getMaster();
				if (m == null || m.getBaseMetaTileEntity() == null) return "";
				IGregTechTileEntity te = m.getBaseMetaTileEntity();
				return m.mName + "|" + te.getXCoord() + "," + te.getYCoord() + "," + te.getZCoord();
			});
		syncManager.syncValue("host_info", hostInfo);

		builder.child(IKey.dynamic(() -> {
			String v = hostInfo.getStringValue();
			if (v == null || v.isEmpty())
				return LangManager.translateToLocal("hatch.dualinput.slave.inv.mapping.me.missing");
			int sep = v.indexOf('|');
			String name = LangManager.translateToLocal("gt.blockmachines." + v.substring(0, sep) + ".name");
			return LangManager.translateToLocalFormatted(
				"hatch.dualinput.slave.inv.mapping.me.hostinfo", name, v.substring(sep + 1));
		})
			.asWidget()
			.pos(26, 9)
			.size(getGUIWidth() - 33, 12));

		// host rendered as its pick-block item; hover = server-evaluated WAILA tooltip
		reobf.proghatches.util.TargetBlockInfoSync hostItem = new reobf.proghatches.util.TargetBlockInfoSync(
			() -> {
				T m = getMaster();
				return m == null || m.getBaseMetaTileEntity() == null ? null
					: m.getBaseMetaTileEntity()
						.getWorld();
			},
			() -> {
				T m = getMaster();
				if (m == null || m.getBaseMetaTileEntity() == null) return null;
				IGregTechTileEntity te = m.getBaseMetaTileEntity();
				return new int[] { te.getXCoord(), te.getYCoord(), te.getZCoord() };
			});
		syncManager.syncValue("host_item", hostItem);
		builder.child(
			hostItem.createWidget()
				.pos(7, 6));

		builder.child(IKey.lang("hatch.dualinput.slave.inv.mapping.me.hint")
			.asWidget()
			.pos(7, 24)
			.size(getGUIWidth() - 14 - 20, 52));

		// EX config window, opened by Shift+clicking the power button (same UX as BufferedDualInputHatch)
		syncManager.syncedPanel("EX_Config", true, (manager, handler) -> createWindowEx2(manager));
		builder.child(createPowerSwitchButton2(syncManager));

		IPanelHandler patternPanel = syncManager
			.syncedPanel("pattern_panel", true, (manager, handler) -> createPatternWindow2(manager));

		builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onMousePressed(mouseButton -> {
			patternPanel.openPanel();
			return patternPanel.isPanelOpen();
		})
			.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE)
			.tooltip(t -> t.addLine(
				IKey.str(LangManager.translateToLocalFormatted("programmable_hatches.gt.pattern.mapping"))))
			.size(16, 16)
			.pos(getGUIWidth() - 18 - 3, 5 + 16 + 2));

		// Open-host button: the click is synced to the server, which validates (host set & alive)
		// and then opens the host GUI, replacing this one. One-way by design.
		InteractionSyncHandler openHost = new InteractionSyncHandler().setOnMousePressed(d -> {
			if (getBaseMetaTileEntity() == null || getBaseMetaTileEntity().isClientSide()) return;
			T m = getMaster();
			if (m == null) return;
			m.onRightclick(m.getBaseMetaTileEntity(), data.getPlayer());
		});
		syncManager.syncValue("open_host", openHost);
		builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().syncHandler(openHost)
			.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_EXPORT)
			.tooltip(t -> t.addLine(IKey.lang("hatch.dualinput.slave.inv.mapping.me.openhost")))
			.size(16, 16)
			.pos(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2));

		// Highlight-host button: purely client-side, reuses AE2's interface-terminal highlighter
		// (pulsing blue box + chat message with the coords). Coords come from the synced host info.
		builder.child(new com.cleanroommc.modularui.widgets.ButtonWidget<>().onMousePressed(mouseButton -> {
			highlightHostClient(hostInfo.getStringValue());
			return true;
		})
			.background(GTGuiTextures.BUTTON_STANDARD, GTGuiTextures.OVERLAY_BUTTON_HIGHLIGHT_BLOCK)
			.tooltip(t -> t.addLine(IKey.lang("hatch.dualinput.slave.inv.mapping.me.highlight")))
			.size(16, 16)
			.pos(getGUIWidth() - 18 - 3, 5 + 16 + 2 + 16 + 2 + 16 + 2));

		return builder;
	}

	/**
	 * Client-only (button callbacks never run on the server; AE client classes are resolved lazily).
	 * Highlights the synced host position with AE2's block highlighter and closes the screen so the
	 * player can see the pulsing blue box, exactly like the interface terminal's highlight action.
	 */
	private void highlightHostClient(String hostInfo) {
		if (hostInfo == null || hostInfo.isEmpty()) return;
		try {
			int sep = hostInfo.indexOf('|');
			String[] c = hostInfo.substring(sep + 1).split(",");
			net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
			appeng.client.render.highlighter.BlockPosHighlighter.highlightBlocks(
				mc.thePlayer,
				java.util.Collections.singletonList(
					new appeng.api.util.DimensionalCoord(
						mc.theWorld,
						Integer.parseInt(c[0]),
						Integer.parseInt(c[1]),
						Integer.parseInt(c[2]))),
				appeng.core.localization.PlayerMessages.MachineHighlighted.getUnlocalized(),
				appeng.core.localization.PlayerMessages.MachineInOtherDim.getUnlocalized());
			mc.thePlayer.closeScreen();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Power switch; Shift+click opens the EX config window (same as BufferedDualInputHatch). */
	com.cleanroommc.modularui.widgets.CycleButtonWidget createPowerSwitchButton2(PanelSyncManager syncManager) {
		return new com.cleanroommc.modularui.widgets.CycleButtonWidget() {
			@Override
			public com.cleanroommc.modularui.api.widget.Interactable.Result onMousePressed(int mouseButton) {
				if (org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_LSHIFT)) {
					syncManager.findPanelHandlerNullable("EX_Config").openPanel();
					return com.cleanroommc.modularui.api.widget.Interactable.Result.ACCEPT;
				}
				return super.onMousePressed(mouseButton);
			}
		}.stateCount(2)
			.value(accessorI(() -> getBaseMetaTileEntity().isAllowedToWork(), s -> {
				if (s) getBaseMetaTileEntity().enableWorking();
				else getBaseMetaTileEntity().disableWorking();
			}))
			.stateBackground(1, GTGuiTextures.BUTTON_STANDARD)
			.stateBackground(0, GTGuiTextures.BUTTON_STANDARD)
			.stateOverlay(1, GTGuiTextures.OVERLAY_BUTTON_POWER_SWITCH_ON)
			.stateOverlay(0, GTGuiTextures.OVERLAY_BUTTON_POWER_SWITCH_OFF)
			.pos(getGUIWidth() - 18 - 3, 5)
			.size(16, 16);
	}

	/** MUI2 port of the EX config window (entries come from the shared ExConfig table). */
	public final ModularPanel createWindowEx2(PanelSyncManager manager) {
		final int WIDTH = 18 * 6 + 6;
		final int HEIGHT = 18 * 4 + 6;
		ModularPanel builder = new ModularPanel("EX_Config");
		builder.size(WIDTH, HEIGHT);
		exconfig.table.cellSet().stream().map(s -> s.getValue().asMUI2(s.getRowKey(), s.getColumnKey()))
			.forEach(builder::child);
		return builder;
	}

	/**
	 * Builds a 16x16 button whose action runs on the server (see PatternDualInputHatch#makeBatchButton).
	 */
	private com.cleanroommc.modularui.widgets.ButtonWidget<?> makeBatchButton(PanelSyncManager syncManager,
		String key, Runnable action) {
		InteractionSyncHandler handler = new InteractionSyncHandler().setOnMousePressed(data -> action.run());
		syncManager.syncValue(key, handler);
		return (com.cleanroommc.modularui.widgets.ButtonWidget<?>) new com.cleanroommc.modularui.widgets.ButtonWidget<>()
			.syncHandler(handler)
			.size(16, 16)
			.background(GTGuiTextures.BUTTON_STANDARD);
	}

	protected ModularPanel createPatternWindow2(PanelSyncManager syncManager) {
		final int WIDTH = 18 * 4 + 6;     // page content width (4 slots wide)
		final int HEIGHT = 18 * 9 + 6;
		// Panel is content-width only; the right-side tabs protrude past its right edge.
		// (Widening the panel area to wrap the tabs was rejected: the extra area is visually
		// transparent but still part of the panel, so clicking that empty corner triggered a
		// "ghost" window drag. So the area stays at content size.)
		// Consequence of a content-width panel: a protruding child sits OUTSIDE the panel
		// area, so NEI fills that strip and the panel's built-in drag does not cover it.
		final int PANEL_W = WIDTH;
		final int TAB_W = 32;             // GuiTextures.TAB_RIGHT width (drag-tab size)
		// open the popup docked to the right edge of the main panel (top-aligned) instead
		// of the default centered position. The main panel's screen area is only known at
		// open time, so the position is set in onOpen (before super lays the panel out).
		ModularPanel builder = new ModularPanel("pattern_window") {
			@Override
			public void onOpen(com.cleanroommc.modularui.screen.ModularScreen screen) {
				Area main = screen.getMainPanel().getArea();
				// left edge flush against the main panel's right edge; tops aligned.
				// left()/top() override the center() set in the ModularPanel constructor.
				this.left(main.x() + main.w()).top(main.y());
				super.onOpen(screen);
			}
		};
		builder.size(PANEL_W, HEIGHT);

		com.cleanroommc.modularui.api.drawable.IDrawable tab1 = new com.cleanroommc.modularui.drawable.ItemDrawable(
			Api.INSTANCE.definitions()
				.items()
				.encodedPattern()
				.maybeStack(1)
				.get()).asIcon()
					.size(18, 18);
		com.cleanroommc.modularui.api.drawable.IDrawable tab2 = GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_OFF.asIcon()
			.size(18, 18);
		com.cleanroommc.modularui.api.drawable.IDrawable tab3 = GTGuiTextures.OVERLAY_BUTTON_BATCH_MODE_ON.asIcon()
			.size(18, 18);
		// Icon for the drag tab: GT++ (miscutils) heat-protection bauble ("insulated gloves").
		// The registry name really does include the trailing ".name" - that's a GT++ quirk,
		// not the lang-key suffix - so it is kept verbatim. Falls back to a plus overlay if
		// the item isn't found, so it never crashes.
		net.minecraft.item.Item gloveItem = cpw.mods.fml.common.registry.GameRegistry
			.findItem("miscutils", "GTPP.bauble.fireprotection.0.name");
		com.cleanroommc.modularui.api.drawable.IDrawable dragIcon = gloveItem != null
			? new com.cleanroommc.modularui.drawable.ItemDrawable(new ItemStack(gloveItem, 1, 0)).asIcon().size(18, 18)
			: GTGuiTextures.OVERLAY_BUTTON_PLUS_LARGE.asIcon().size(18, 18);

		PagedWidget.Controller tabController = new PagedWidget.Controller();

		ParentWidget<?> page1 = new ParentWidget<>().coverChildren()
			.name("patterns");
		ParentWidget<?> page2 = new ParentWidget<>().coverChildren()
			.name("individual_multiplier");
		ParentWidget<?> page3 = new ParentWidget<>().coverChildren()
			.name("batch_multiplier");

		// ---- page 3: batch multiplier op. ----
		// These buttons mutate multiplier[] (saved to NBT), so the mutation must run
		// server-side. Route each click through an InteractionSyncHandler (the click is
		// synced to the server, where the action runs), mirroring the legacy MUI1
		// com.cleanroommc.modularui.widgets.ButtonWidget.setOnClick behaviour which auto-synced clicks to the server.
		page3.child(makeBatchButton(syncManager, "batch_x2", () -> {
			for (int i = 0; i < 36; i++) {
				multiplier[i] *= 2;
				multiplier[i] = Math.max(multiplier[i], 1);
			}
			refresh();
		}).pos(3, 3)
			.tooltip(t -> t.addLine(IKey.str("x2"))));
		page3.child(IKey.str("x2")
			.asWidget()
			.pos(3 + 3, 3));
		page3.child(makeBatchButton(syncManager, "batch_set1", () -> {
			for (int i = 0; i < 36; i++) multiplier[i] = 1;
			refresh();
		}).pos(3 + 16, 3)
			.tooltip(t -> t.addLine(IKey.str("=1"))));
		page3.child(IKey.str("=1")
			.asWidget()
			.pos(3 + 3 + 16, 3));
		page3.child(makeBatchButton(syncManager, "batch_xn", () -> {
			for (int i = 0; i < 36; i++) {
				multiplier[i] *= n;
				multiplier[i] = Math.max(multiplier[i], 1);
			}
			refresh();
		}).pos(3, 3 + 32)
			.tooltip(t -> t.addLine(IKey.str("xN"))));
		page3.child(IKey.dynamic(() -> "x" + n)
			.asWidget()
			.pos(3 + 3, 3 + 32));
		page3.child(makeBatchButton(syncManager, "batch_setn", () -> {
			for (int i = 0; i < 36; i++) multiplier[i] = n;
			refresh();
		}).pos(3 + 16, 3 + 32)
			.tooltip(t -> t.addLine(IKey.str("=N"))));
		page3.child(IKey.dynamic(() -> "=" + n)
			.asWidget()
			.pos(3 + 3 + 16, 3 + 32));

		com.cleanroommc.modularui.value.sync.IntSyncValue nValue = new com.cleanroommc.modularui.value.sync.IntSyncValue(
			() -> n, s -> {
				n = s;
				refresh();
			}).allowC2S();
		page3.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(nValue)
			.formatAsInteger(true)
			.numbersInt(Integer.MIN_VALUE, Integer.MAX_VALUE)
			.setTextColor(com.cleanroommc.modularui.utils.Color.WHITE.main)
			.tooltip(t -> t.addLine(IKey.str("N=")))
			.size(60, 18)
			.pos(3, 3 + 32 + 18)
			.background(GTGuiTextures.BACKGROUND_TEXT_FIELD));


		// shared handler: use one handler for pattern + display slots so shift-clicking
		// a pattern doesn't transfer it between pattern slots instead of to player inv.
		// MUI2 ItemStackHandler(ItemStack[]) is backed by Arrays.asList(pattern), so
		// writes propagate directly to the pattern[] array (same as the legacy handler).
		ItemStackHandler shared_handler = new ItemStackHandler(pattern);

		// register the slot group before the slots reference it (rowSize 4 == grid width)
		// rowSize 4 (grid width); shift-click priority -1 so shift-clicking from the player
		// inventory targets other slot groups before these pattern slots (matches the legacy
		// MUI1 setShiftClickPriority(-1) behaviour).
		syncManager.registerSlotGroup("pattern_inv", 4, -1);

		for (int i = 0; i < 36; i++) {
			final int ii = i;

			// ---- page 2: display-only slot + per-slot multiplier field ----
			page2.child(new ItemSlot() {
				@Override
				@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
				protected ItemStack getItemStackForRendering(ItemStack itemstack, boolean dragging) {
					if (itemstack == null || !(itemstack.getItem() instanceof ItemEncodedPattern)) {
						return itemstack;
					}
					ItemStack output = ((ItemEncodedPattern) itemstack.getItem()).getOutput(itemstack);
					return output != null ? output : itemstack;
				}
			}.slot(new ModularSlot(shared_handler, i).accessibility(false, false))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.background(GTGuiTextures.SLOT_ITEM_STANDARD, GTGuiTextures.OVERLAY_SLOT_PATTERN_ME));

			com.cleanroommc.modularui.value.sync.IntSyncValue mulValue = new com.cleanroommc.modularui.value.sync.IntSyncValue(
				() -> multiplier[ii], s -> {
					multiplier[ii] = s;
					refresh();
				}).allowC2S();
			page2.child(new com.cleanroommc.modularui.widgets.textfield.TextFieldWidget().value(mulValue)
				.formatAsInteger(true)
				.numbersInt(Integer.MIN_VALUE, Integer.MAX_VALUE)
				.setMaxLength(999)
				.setTextColor(com.cleanroommc.modularui.utils.Color.RED.main)
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.size(18, 18));

			// ---- page 1: interactive pattern slot + multiplier text overlay ----
			page1.child(new ItemSlot() {
				@Override
				@cpw.mods.fml.relauncher.SideOnly(cpw.mods.fml.relauncher.Side.CLIENT)
				protected ItemStack getItemStackForRendering(ItemStack itemstack, boolean dragging) {
					if (itemstack == null || !(itemstack.getItem() instanceof ItemEncodedPattern)) {
						return itemstack;
					}
					ItemStack output = ((ItemEncodedPattern) itemstack.getItem()).getOutput(itemstack);
					return output != null ? output : itemstack;
				}
			}.slot(new ModularSlot(shared_handler, i).slotGroup("pattern_inv")
				.filter(itemStack -> itemStack.getItem() instanceof ICraftingPatternItem)
				.changeListener((newItem, onlyAmountChanged, client, init) -> onPatternChange()))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.background(GTGuiTextures.SLOT_ITEM_STANDARD, GTGuiTextures.OVERLAY_SLOT_PATTERN_ME));

			// Multiplier label drawn on top of the slot. It must NOT take part in hit-testing:
			// layered over the slot, a normal TextWidget sits above the slot in the hovered
			// list and swallows the slot's click/release, so a click on the slot is treated as
			// a drop "outside" and throws the held item out. NonInteractiveText returns false
			// from isInside, so it is skipped in hit-testing (clicks/drags reach the slot) while
			// still rendering the number.
			page1.child(new PatternDualInputHatch.NonInteractiveText(IKey.dynamic(() -> {
				String s = multiplier[ii] == 1 ? "" : (ps(multiplier[ii]) + "");
				if (pattern[ii] == null) return "§7" + s;
				return s;
			}))
				.pos((i % 4) * 18 + 3, (i / 4) * 18 + 3)
				.size(18, 18));
		}

		// Invisible backing behind the (protruding) tab strip, marked as an NEI/recipe-viewer
		// exclusion area. The strip sits outside the panel's own area, so the panel's auto
		// NEI exclusion doesn't cover it; this widget does. It draws nothing and clicks pass
		// through to the tabs rendered on top of it.
		builder.child(new com.cleanroommc.modularui.widget.Widget<>()
			.pos(WIDTH - 3, -1)
			.size(TAB_W, 28 * 4)
			.excludeAreaInRecipeViewer());

		builder.child(new com.cleanroommc.modularui.widgets.layout.Column().coverChildren()
			.pos(WIDTH - 3, -1)
			// First tab slot = GT++ heat-protection glove, used as a drag handle. It is a
			// DragTab (forwards the drag to the panel by repositioning it live each frame, so
			// no jump even though the tab protrudes outside the panel area), NOT a PageButton,
			// so it moves the window and does not switch pages.
			.child(new PatternDualInputHatch.DragTab()
				.background(GuiTextures.TAB_RIGHT.get(-1, false), dragIcon)
				.size(TAB_W, 28)
				.tooltip(t -> t.addLine(IKey.str("Hold to drag"))))
			.child(new PageButton(0, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab1)
				.tooltip(t -> t.addLine(IKey.str("Patterns"))))
			.child(new PageButton(1, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab2)
				.tooltip(t -> t.addLine(IKey.str("Individual Multiplier Op."))))
			.child(new PageButton(2, tabController).tab(GuiTextures.TAB_RIGHT, 0)
				.overlay(tab3)
				.tooltip(t -> t.addLine(IKey.str("Batch Multiplier Op.")))));

		builder.child(new PagedWidget<>().controller(tabController)
			.pos(0, 0)
			.size(WIDTH, HEIGHT)
			.addPage(page1)
			.addPage(page2)
			.addPage(page3));

		return builder;
	}



    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        // Left-click opens the linked host's GUI, using the same forwarding the plain slaves use for
        // right-click (GTNH's mixin copes with far-away/unloaded hosts). Sneak keeps normal breaking.
        if (aBaseMetaTileEntity.isServerSide() && !aPlayer.isSneaking()) {
            T m = getMaster();
            if (m != null) {
                m.onRightclick(m.getBaseMetaTileEntity(), aPlayer);
                return;
            }
        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

}