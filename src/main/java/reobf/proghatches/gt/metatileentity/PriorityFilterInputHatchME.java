package reobf.proghatches.gt.metatileentity;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeMultimap;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AELog;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.MeaningfulItemIterator;
import gregtech.api.GregTech_API;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_InputBus_ME;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_Input_ME;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolderSuper;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

public class PriorityFilterInputHatchME extends GT_MetaTileEntity_Hatch_Input_ME implements IMEHatchOverrided,IDataCopyablePlaceHolderSuper,
IPriorityHost,IActionHost{

	public PriorityFilterInputHatchME(int aID, /*boolean autoPullAvailable,*/ String aName, String aNameRegional) {
		super(aID, /*autoPullAvailable*/true, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
		desc=reobf.proghatches.main.Config.get("PFIHME", ImmutableMap.of());
	} 
	private static Field f;

	static {
		try {
			f = GridStorageCache.class.getDeclaredField("activeCellProviders");
		} catch (Exception e) {
			e.printStackTrace();
			throw new AssertionError(e);

		}
		f.setAccessible(true);
	}
	String[] desc;
	  @Override
	public String[] getDescription() {
		
		return desc;
	}
	public PriorityFilterInputHatchME(String aName, /*boolean autoPullAvailable,*/ int aTier, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, true, aTier, aDescription, aTextures);
		
	}	
	private static Set<ICellProvider> get(GridStorageCache thiz) {

		try {
			return (Set<ICellProvider>) f.get(thiz);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
int filter;
	@Override
	public void overridedBehoviour(int minPull) {
		
		AENetworkProxy proxy = getProxy();

		try {
			GridStorageCache st = (GridStorageCache) proxy.getStorage();
			LinkedList<IMEInventoryHandler> all=new LinkedList<>();
			for (final ICellProvider cc : get(st)) {
			
			List<IMEInventoryHandler> list = cc
				.getCellArray(StorageChannel.FLUIDS);
			if(cc.getPriority()==filter)
			for(IMEInventoryHandler l:list){
				 if(!(l instanceof MEInventoryHandler))continue;
				 all.add( l);
			}
			
			}
			IItemList list=StorageChannel.FLUIDS.createList();
			
			for (IMEInventoryHandler ent : all) {
				if(list.size()>=16)break;
				ent.getAvailableItems(list);
			}
			
			
			int index = 0;
			Iterator iterator = list.iterator();
			while (iterator.hasNext() && index < 16) {
				IAEFluidStack currItem = (IAEFluidStack) iterator.next();
				// if(all.findPrecise(currItem)!=null){continue;}
				if (currItem.getStackSize() >= minPull) {
					FluidStack itemstack = GT_Utility.copyAmount(1, currItem.getFluidStack());
					this.storedFluids[index] = itemstack;
					index++;
				}
			}
			for (int i = index; i < 16; i++) {
				storedFluids[i] = null;
			}

		} catch (final GridAccessException ignored) {
		}

	

	}
	@SuppressWarnings("unchecked")
	@Override
	public IAEStack overridedExtract(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src) {
		AENetworkProxy proxy = getProxy();
		long size=request.getStackSize();
		request=request.copy();
		try {
			GridStorageCache st = (GridStorageCache) proxy.getStorage();
			LinkedList<IMEInventoryHandler> all=new LinkedList<>();
			lab:for (final ICellProvider cc : get(st)) {
			
			List<IMEInventoryHandler> list = cc
				.getCellArray(StorageChannel.FLUIDS);
			if(cc.getPriority()==filter)
			for(IMEInventoryHandler l:list){
				 if(!(l instanceof MEInventoryHandler))continue;
				 IAEStack ext = l.extractItems(request, mode, src);
				 if(ext!=null)
				 request.decStackSize(ext.getStackSize());
				 if(request.getStackSize()<=0)break lab;
			}
			
			}
			
		} catch (final GridAccessException ignored) {}
		
		if(size-request.getStackSize()>0){
			request.setStackSize(size-request.getStackSize());
			return request;
		}
		return null;
	}
	
	
	@Override
	public PriorityFilterInputHatchME newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new PriorityFilterInputHatchME(mName, mTier, mDescriptionArray, mTextures);
	}
	
	/*
	@Override
	public NBTTagCompound getCopiedData(EntityPlayer player) {
	 NBTTagCompound ret=super_getCopiedData(player,()->MethodHandles.lookup());
	 ret.setInteger("filter", filter);

	return ret;
	}
	@Override
	public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
	boolean suc=super_pasteCopiedData(player, nbt,()->MethodHandles.lookup());
	if(suc){
		if(nbt.hasKey("filter"))filter=nbt.getInteger("filter");
	
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
		if(nbt.hasKey("filter"))filter=nbt.getInteger("filter");
		return true;
	}

	@Override
	public NBTTagCompound impl_getCopiedData(EntityPlayer player, NBTTagCompound tag) {
		 tag.setInteger("filter", filter);
		return tag;
	}
	protected ModularWindow createStackSizeConfigurationWindow(final EntityPlayer player) {
		ModularWindow parent= super.createStackSizeConfigurationWindow(player);
		
		
		
		
		
		final int WIDTH = 78;
        final int HEIGHT = parent.getSize().height+18+18+3;
        final int PARENT_WIDTH = getGUIWidth();
        final int PARENT_HEIGHT = getGUIHeight();
        ModularWindow.Builder builder = ModularWindow.builder(WIDTH, HEIGHT);
        builder.setBackground(GT_UITextures.BACKGROUND_SINGLEBLOCK_DEFAULT);
        builder.setGuiTint(getGUIColorization());
        builder.setDraggable(true);
        
        
        builder.widgets(parent.getChildren());
        
        
        builder.setPos(
            (size, window) -> Alignment.Center.getAlignedPos(size, new Size(PARENT_WIDTH, PARENT_HEIGHT))
                .add(
                    Alignment.TopRight.getAlignedPos(new Size(PARENT_WIDTH, PARENT_HEIGHT), new Size(WIDTH, HEIGHT))
                        .add(WIDTH - 3, 0)));
        builder.widget(
            TextWidget.localised("proghatches.priority.filter")
                .setPos(3, parent.getSize().height)
                .setSize(74, 18))
            .widget(
                new NumericWidget().setSetter(val -> filter = (int) val)
                    .setGetter(() -> filter)
                    .setBounds(Integer.MIN_VALUE, Integer.MAX_VALUE)
                    .setScrollValues(1, 4, 64)
                    .setTextAlignment(Alignment.Center)
                    .setTextColor(Color.WHITE.normal)
                    .setSize(70, 18)
                    .setPos(3, parent.getSize().height+18)
                    .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD));
  
        
       // builder.widget(createMultiplesModeButton(builder,HEIGHT));
        
        
        
        
        return builder.build();
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
	               
	            	  for (int index = 0; index < 16; index++) {
	                      updateInformationSlot(index);
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
	        
	    }	  @Override
		public void saveNBTData(NBTTagCompound aNBT) {
			  aNBT.setInteger("filter", filter);
			super.saveNBTData(aNBT);
		}
		  @Override
		public void loadNBTData(NBTTagCompound aNBT) {
			filter  =aNBT.getInteger("filter" );
			super.loadNBTData(aNBT);
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
		@Override
		public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		
		
		if(ProghatchesUtil.handleUse(aPlayer, this))return true;
			
		
		return super.onRightclick(aBaseMetaTileEntity, aPlayer);
		}
		
		
		private static boolean isEditMode(ItemStack stack) {
		       
		        NBTTagCompound tagCompound = Platform.openNbtData(stack);
		        try {
		            if (tagCompound.hasKey("PRIORITY_CARD_MODE")) {
		                return (tagCompound.getString("PRIORITY_CARD_MODE")).equals("EDIT");
		            }
		        } catch (final IllegalArgumentException e) {
		            AELog.debug(e);
		        }
		        return true;
		  }
		@Override
		public int getPriority() {
			
			return filter;
		}
		@Override
		public void setPriority(int newValue) {
			filter=newValue;
			markDirty();
		}
		@Override
		public IGridNode getGridNode(ForgeDirection dir) {
		
			return this.getProxy().getNode();
		}
		@Override
		public void securityBreak() {
		
			
		}
		@Override
		public IGridNode getActionableNode() {
		
			return this.getProxy().getNode();
		}
		
}
