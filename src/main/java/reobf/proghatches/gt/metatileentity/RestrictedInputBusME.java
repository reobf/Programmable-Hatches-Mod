package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.GT_Values.TIER_COLORS;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Mods.GregTech;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_FLUID_HATCH;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_INPUT_FLUID_HATCH_ACTIVE;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
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
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolderSuper;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import sun.misc.Unsafe;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
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
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
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
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.core.localization.WailaText;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.Optional.Interface;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.SoundResource;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.modularui.IAddGregtechLogo;
import gregtech.api.interfaces.modularui.IAddUIWidgets;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CommonMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_InputBus_ME;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_Input_ME;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;

public class RestrictedInputBusME extends GT_MetaTileEntity_Hatch_InputBus_ME implements IDataCopyablePlaceHolderSuper{
	
	public RestrictedInputBusME(int aID, boolean autoPullAvailable, String aName, String aNameRegional) {
		super(aID, autoPullAvailable, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
		desc=reobf.proghatches.main.Config.get("RIBME", ImmutableMap.of());
	}
	public RestrictedInputBusME(String aName, boolean autoPullAvailable, int aTier, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, autoPullAvailable, aTier, aDescription, aTextures);
		
	}
	
	  private static final int SLOT_COUNT = 16;
	
	  
	  String[] desc;
	  @Override
	public String[] getDescription() {
		
		return desc;
	}
	  
	  
	  static private int  _mDescriptionArray_offset;
	  static private int  _mDescription_offset;
	  
	  static Field f1,f2,f3;
	  
	  
	  static{
		  try {
			f1=GT_MetaTileEntity_Hatch_InputBus_ME.class.getDeclaredField("shadowInventory");  
			f2=GT_MetaTileEntity_Hatch_InputBus_ME.class.getDeclaredField("savedStackSizes");
			f3=GT_MetaTileEntity_Hatch_InputBus_ME.class.getDeclaredField("processingRecipe");
			f1.
	setAccessible(true);
	f2.
	setAccessible(true);
	f3.setAccessible(true);
		  } catch (Exception e) {
			e.printStackTrace();
		}
		
		  
		  
	  }
	  public ItemStack[] shadowInventory(){
		 try {
			return (ItemStack[]) f1.get(this);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		  
	  }
	  public int[] savedStackSizes(){
		  try {
				return (int[]) f2.get(this);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		  
	  }
	  public boolean  processingRecipe(){
		  try {
				return (boolean) f3.get(this);
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		  
		  
	  }
	  
	  @Override
	public ItemStack getStackInSlot(int aIndex) {
		
		  ItemStack s= super.getStackInSlot(aIndex);
	     if(!processingRecipe()){return s;}
		  if(s==null)return null;
		  if(aIndex==getCircuitSlot())return s;
		  
		   if(getBaseMetaTileEntity().isAllowedToWork()==false){
			   this.shadowInventory()[aIndex] = null;
	           this.savedStackSizes()[aIndex] = 0;
	           this.setInventorySlotContents(aIndex + SLOT_COUNT, null);
			   return null;
		   }
		  s=s.copy();
		  if(s!=null&&s.stackSize>restrict){
          	s.stackSize=restrict;
          }
          if(s!=null&&s.stackSize<restrict_lowbound){
          	s=null;
          }
          if(s!=null&&restrict_lowbound>0&&multiples==1){
          	s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;
          }
          if(s!=null&&restrict_lowbound>0&&multiples==2){
          //	s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;
          	s.stackSize=1<<(31-Integer.numberOfLeadingZeros(s.stackSize/restrict_lowbound));
          	s.stackSize*=restrict_lowbound;
          }
		  
		  //if(s.stackSize<0)s=null;
		  
		  if(s!=null){
		   this.shadowInventory()[aIndex] = s;
           this.savedStackSizes()[aIndex] = this.shadowInventory()[aIndex].stackSize;
           this.setInventorySlotContents(aIndex + SLOT_COUNT, this.shadowInventory()[aIndex]);
           return this.shadowInventory()[aIndex];
		  }else{
			  this.setInventorySlotContents(aIndex + SLOT_COUNT, null);
			  
		  }
		  return s;
	}
	  
	  
	  public ItemStack updateInformationSlot(int aIndex, ItemStack aStack) {
	        if(getBaseMetaTileEntity().isAllowedToWork()==false){
	        	 setInventorySlotContents(aIndex + SLOT_COUNT, null);
	        	return null;
	        }
		  
		  if (aIndex >= 0 && aIndex < SLOT_COUNT) {
	            if (aStack == null) {
	                super.setInventorySlotContents(aIndex + SLOT_COUNT, null);
	            } else {
	                AENetworkProxy proxy = getProxy();
	                if (!proxy.isActive()) {
	                    super.setInventorySlotContents(aIndex + SLOT_COUNT, null);
	                    return null;
	                }
	                try {
	                    IMEMonitor<IAEItemStack> sg = proxy.getStorage()
	                        .getItemInventory();
	                    IAEItemStack request = AEItemStack.create(mInventory[aIndex]);
	                    request.setStackSize(Integer.MAX_VALUE);
	                    IAEItemStack result = sg.extractItems(request, Actionable.SIMULATE, getRequestSource());
	                    ItemStack s = (result != null) ? result.getItemStack() : null;
	                    if(s!=null&&s.stackSize>restrict){
	                    	s.stackSize=restrict;
	                    }
	                    if(s!=null&&s.stackSize<restrict_lowbound){
	                    	s=null;
	                    }
	                    if(s!=null&&restrict_lowbound>0&&multiples==1){
	                    	s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;
	                    }
	                    if(s!=null&&restrict_lowbound>0&&multiples==2){
	                    //	s.stackSize=(s.stackSize/restrict_lowbound)*restrict_lowbound;
	                    	
	                    	s.stackSize=1<<(31-Integer.numberOfLeadingZeros(s.stackSize/restrict_lowbound));
	                    	s.stackSize*=restrict_lowbound;
	                    }
	                    
	                    
	                    
	                    setInventorySlotContents(aIndex + SLOT_COUNT, s);
	                    return s;
	                } catch (final GridAccessException ignored) {}
	            }
	        }
	        return null;
	    }
	  
	  
@Override
public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
	try{
	return super.endRecipeProcessing(controller);}finally{
		for (int index = 0; index < SLOT_COUNT; index++) {
		updateInformationSlot(index, mInventory[index]);
	}
	}
	
}BaseActionSource  requestSource;
    private BaseActionSource getRequestSource() {
        if (requestSource == null) requestSource = new MachineSource((IActionHost) getBaseMetaTileEntity());
        return requestSource;
    }
    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
    
    	return   new RestrictedInputBusME(mName, false, mTier, mDescriptionArray, mTextures);
    	 
    }
    private static final int CONFIG_WINDOW_ID=123456;
    @Override
    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
       
        buildContext.addSyncedWindow(CONFIG_WINDOW_ID, this::createStackSizeConfigurationWindow);
       
        
        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
            if (clickData.mouseButton == 0) {
              if (!widget.isClient()) {
                widget.getContext()
                    .openSyncedWindow(CONFIG_WINDOW_ID);
            }
        }})
            .setBackground(() -> {
               {
                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
                        GT_UITextures.OVERLAY_BUTTON_AUTOPULL_ME_DISABLED };
                }
            })
            .addTooltips(
                Arrays.asList(
                		StatCollector.translateToLocal("proghatches.restricted.configure")))
            .setSize(16, 16)
            .setPos(80, 10));
        
        
        builder.widget(new ButtonWidget().setOnClick((clickData, widget) -> {
            if (clickData.mouseButton == 0) {
              if (!widget.isClient()) {
               
            	  for (int index = 0; index < SLOT_COUNT; index++) {
                      updateInformationSlot(index, mInventory[index]);
                  }
            }
        }})
            .setBackground(() -> {
               {
                    return new IDrawable[] {
                    		GT_UITextures.BUTTON_STANDARD
                      };
                }
            })
            .addTooltips(
                Arrays.asList(
                		StatCollector.translateToLocal("proghatches.restricted.refresh.0"),
                		StatCollector.translateToLocal("proghatches.restricted.refresh.1")))
            .setSize(16, 16)
            .setPos(80, 10+18));
        
        
     // .widget(new FakeSyncWidget.BooleanSyncer(() -> autoPullFluidList, this::setAutoPullFluidList));

        
        super.addUIWidgets(builder, buildContext);
        
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
    
    

    
   int multiples;
    Widget createMultiplesModeButton(IWidgetBuilder<?> builder,int HEIGHT) {
		
		Widget button = new CycleButtonWidget()
				.setLength(3)
				.setTextureGetter((I)->UITexture.EMPTY)
				.addTooltip(
						0,LangManager.translateToLocal("proghatches.restricted.multiples.exact"))
				.addTooltip(
						1,LangManager.translateToLocal("proghatches.restricted.multiples"))
				.addTooltip(
						2,LangManager.translateToLocal("proghatches.restricted.multiples.alt"))	
				.setGetter(()->multiples)				
				.setSetter(s->multiples=s)
				
			.setBackground(() -> {
		                if (multiples==1) {
		                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
		                        mode0 };
		                } 
		                
		                else if (multiples==2) {
		                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
		                        mode1 };
		                } 
		                
		                else {
		                    return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
		                        GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
		                }
		            })
				
				
				
				.setTooltipShowUpDelay(TOOLTIP_DELAY)
				
				.setPos(new Pos2d(3, HEIGHT-3-16)).setSize(16, 16);
		return  button;
	}
int restrict=Integer.MAX_VALUE;
int restrict_lowbound=1;
@Override
public void saveNBTData(NBTTagCompound aNBT) {
    super.saveNBTData(aNBT);
    aNBT.setInteger("restrict", restrict);
    aNBT.setInteger("restrict_l", restrict_lowbound);
    aNBT.setInteger("multiples", multiples);
    }
@Override
public void loadNBTData(NBTTagCompound aNBT) {
	if(aNBT.hasKey("x")==false)return;
	super.loadNBTData(aNBT);
	 restrict=aNBT.getInteger( "restrict");
	 restrict_lowbound=aNBT.getInteger( "restrict_l");
	 multiples=aNBT.getInteger("multiples");
}

static AdaptableUITexture mode0= AdaptableUITexture
.of("proghatches", "restrict_mode0", 18, 18, 1);
static AdaptableUITexture mode1= AdaptableUITexture
.of("proghatches", "restrict_mode1", 18, 18, 1);

/*
@Override
public NBTTagCompound getCopiedData(EntityPlayer player) {
 NBTTagCompound ret=super_getCopiedData(player,()->MethodHandles.lookup());
 ret.setInteger("multiples", multiples);
 ret.setInteger("restrict", restrict);
 ret.setInteger("restrict_lowbound", restrict_lowbound);
return ret;
}
@Override
public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
boolean suc=super_pasteCopiedData(player, nbt,()->MethodHandles.lookup());
if(suc){
	if(nbt.hasKey("multiples"))multiples=nbt.getInteger("multiples");
	if(nbt.hasKey("restrict"))restrict=nbt.getInteger("restrict");
	if(nbt.hasKey("restrict_lowbound"))restrict_lowbound=nbt.getInteger("restrict_lowbound");
}
return suc;
}
@Override
public String getCopiedDataIdentifier(EntityPlayer player) {
	return super_getCopiedDataIdentifier(player,()->MethodHandles.lookup());
}*/

@Override
public Supplier<Lookup> lookup() {
	return ()->MethodHandles.lookup();
}

@Override
public boolean impl_pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
	if(nbt.hasKey("multiples"))multiples=nbt.getInteger("multiples");
	if(nbt.hasKey("restrict"))restrict=nbt.getInteger("restrict");
	if(nbt.hasKey("restrict_lowbound"))restrict_lowbound=nbt.getInteger("restrict_lowbound");
	return true;
}

@Override
public NBTTagCompound impl_getCopiedData(EntityPlayer player, NBTTagCompound ret) {
	 ret.setInteger("multiples", multiples);
	 ret.setInteger("restrict", restrict);
	 ret.setInteger("restrict_lowbound", restrict_lowbound);
	return ret;
}

@Override
public NBTTagCompound getCopiedData(EntityPlayer player) {
	return IDataCopyablePlaceHolderSuper.super.getCopiedData(player);
}
@Override
public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
	return IDataCopyablePlaceHolderSuper.super.pasteCopiedData(player, nbt);
}
@Override
public String getCopiedDataIdentifier(EntityPlayer player) {
	return IDataCopyablePlaceHolderSuper.super.getCopiedDataIdentifier(player);
}

}
