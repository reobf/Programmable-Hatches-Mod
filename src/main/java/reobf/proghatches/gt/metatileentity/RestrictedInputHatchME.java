package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.GT_Values.TIER_COLORS;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_FLUID_HATCH;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_FLUID_HATCH_ACTIVE;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.fluid.FluidStackTank;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.core.localization.WailaText;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEFluidStack;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_Input_ME;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class RestrictedInputHatchME extends GT_MetaTileEntity_Hatch_Input_ME
{
	
	public RestrictedInputHatchME(int aID, boolean autoPullAvailable, String aName, String aNameRegional) {
		super(aID, autoPullAvailable, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
		desc=reobf.proghatches.main.Config.get("RIHME", ImmutableMap.of());
	}
	public RestrictedInputHatchME(String aName, boolean autoPullAvailable, int aTier, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, autoPullAvailable, aTier, aDescription, aTextures);
		
	}  
	String[] desc;
	  @Override
	public String[] getDescription() {
		
		return desc;
	}
	  private static final int SLOT_COUNT = 16;
	public void updateInformationSlot(int index) {
		 if(getBaseMetaTileEntity().isAllowedToWork()==false){
			 storedInformationFluids[index] = null;
        	
        }
		if (index < 0 || index >= SLOT_COUNT) {
            return;
        }

        FluidStack fluidStack = storedFluids[index];
        if (fluidStack == null) {
            storedInformationFluids[index] = null;
            return;
        }

        AENetworkProxy proxy = getProxy();
        if (proxy == null || !proxy.isActive()) {
            storedInformationFluids[index] = null;
            return;
        }

        try {
            IMEMonitor<IAEFluidStack> sg = proxy.getStorage()
                .getFluidInventory();
            IAEFluidStack request = AEFluidStack.create(fluidStack);
            request.setStackSize(Integer.MAX_VALUE);
            IAEFluidStack result = sg.extractItems(request, Actionable.SIMULATE, getRequestSource());
            FluidStack resultFluid = (result != null) ? result.getFluidStack() : null;
            if(resultFluid!=null&&resultFluid.amount>restrict){
            	resultFluid.amount=restrict;
            }
            if(resultFluid!=null&&resultFluid.amount<restrict_lowbound){
            	resultFluid=null;
            }
            if(resultFluid!=null&&restrict_lowbound>0&&multiples){
            	resultFluid.amount=(resultFluid.amount/restrict_lowbound)*restrict_lowbound;
            }
             storedInformationFluids[index] = resultFluid;
        } catch (final GridAccessException ignored) {}
    }
@Override
public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
	try{
	return super.endRecipeProcessing(controller);}finally{
		for (int index = 0; index < SLOT_COUNT; index++) {
		updateInformationSlot(index);
	}
	}
	
}BaseActionSource  requestSource;
    private BaseActionSource getRequestSource() {
        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }
    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
    
    	return   new RestrictedInputHatchME(mName, false, mTier, mDescriptionArray, mTextures);
    	 
    }
    private static final int CONFIG_WINDOW_ID=123456;
    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
       
        buildContext.addSyncedWindow(CONFIG_WINDOW_ID, this::createStackSizeConfigurationWindow);
        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
             if ( !widget.isClient()) {
                widget.getContext()
                    .openSyncedWindow(CONFIG_WINDOW_ID);
            }
        })
            .setPlayClickSound(true)
            .setBackground(() -> {
                if (autoPullFluidList) {
                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
                        GT_UITextures.OVERLAY_BUTTON_AUTOPULL_ME };
                } else {
                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
                        GT_UITextures.OVERLAY_BUTTON_AUTOPULL_ME_DISABLED };
                }
            })
            .addTooltips(
                Arrays.asList(
                    StatCollector.translateToLocal("proghatches.restricted.configure")
                    ))
            .setSize(16, 16)
            .setPos(80, 10));
           // .widget(new FakeSyncWidget.BooleanSyncer(() -> autoPullFluidList, this::setAutoPullFluidList));

        
        super.addUIWidgets(builder, buildContext);
        
    }
    boolean multiples;
    ButtonWidget createMultiplesModeButton(IWidgetBuilder<?> builder,int HEIGHT) {
		
		Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {

			multiples=!multiples;
		}).attachSyncer(new FakeSyncWidget.BooleanSyncer(()->multiples, val -> {
			multiples=val;
				}), builder)
				.setBackground(() -> {
	                if (multiples) {
	                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
	                        GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
	                } else {
	                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
	                        GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
	                }
	            })
				.addTooltip(LangManager.translateToLocal("proghatches.restricted.multiples"))
				.setTooltipShowUpDelay(TOOLTIP_DELAY)
				
				.setPos(new Pos2d(3, HEIGHT-3-16)).setSize(16, 16);
		return (ButtonWidget) button;
	}
    protected ModularWindow createStackSizeConfigurationWindow(final EntityPlayer player) {
        final int WIDTH = 78;
        final int HEIGHT = 80+18+18;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();
        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);
        builder.setPos(
            (size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
                .add(
                    Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))
                        .add(WIDTH - 3, 0)));
        builder.widget(
            TextWidget.localised("proghatches.restricted.bound.down")
                .setPos(3, 2)
                .setSize(74, 14+18))
            .widget(
                new NumericWidget().setSetter(val -> restrict_lowbound = (int) val)
                    .setGetter(() -> restrict_lowbound)
                    .setBounds(1, Integer.MAX_VALUE)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(3, 18+18)
                    .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD));
        builder.widget(
            TextWidget.localised("proghatches.restricted.bound.up")
                .setPos(3, 42+18)
                .setSize(74, 14))
            .widget(
                new NumericWidget().setSetter(val -> restrict = (int) val)
                    .setGetter(() -> restrict)
                    .setBounds(1, Integer.MAX_VALUE)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(3, 58+18)
                    .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD));   
        builder.widget(createMultiplesModeButton(builder,HEIGHT));
        return builder.build();
    }
int restrict=Integer.MAX_VALUE;
int restrict_lowbound=1;
@Override
public void saveNBTData(NBTTagCompound aNBT) {
    super.saveNBTData(aNBT);
    aNBT.setInteger("restrict", restrict);
    aNBT.setInteger("restrict_l", restrict_lowbound);
    aNBT.setBoolean("multiples", multiples);
    }
@Override
public void loadNBTData(NBTTagCompound aNBT) {
	if(aNBT.hasKey("x")==false)return;
	super.loadNBTData(aNBT);
	 restrict=aNBT.getInteger( "restrict");
	 restrict_lowbound=aNBT.getInteger( "restrict_l");
	 multiples=aNBT.getBoolean("multiples");
}


}