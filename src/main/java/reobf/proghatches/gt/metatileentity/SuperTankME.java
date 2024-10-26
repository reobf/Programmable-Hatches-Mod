package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.MEMonitorIFluidHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.MEMonitorHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.client.texture.ExtraBlockTextures;
import appeng.helpers.IPriorityHost;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.IInventoryDestination;
import appeng.util.inv.IMEAdaptor;
import appeng.util.inv.IMEAdaptorIterator;
import appeng.util.inv.ItemSlot;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.FluidList;
import appeng.util.item.ItemList;
import appeng.util.prioitylist.IPartitionList;
import appeng.util.prioitylist.PrecisePriorityList;
import gregtech.api.GregTech_API;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_Utility;
import li.cil.oc.server.machine.Machine;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.gt.metatileentity.util.MappingFluidTank;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.IIconTexture;
import reobf.proghatches.util.ProghatchesUtil;

public class SuperTankME extends GT_MetaTileEntity_Hatch implements ICellContainer, IGridProxyable
,IPriorityHost,IStoageCellUpdate
{

	public SuperTankME(String aName, int aTier, int aInvSlotCount, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
		content.setCapacity(commonSizeCompute(aTier));
		
	}
	public SuperTankME(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount
			) {
		super(aID, aName, aNameRegional, aTier, aInvSlotCount,  reobf.proghatches.main.Config.get("STME", 
				ImmutableMap.of(
						"fluid",commonSizeCompute(aTier)
						)
				
				), new ITexture[0]);
		
		content.setCapacity(commonSizeCompute(aTier));
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}
	
	
	/*@MENetworkEventSubscribe
    public void channel(final MENetworkChannelsChanged c) {
		post();
		
    }*/	 private AEFluidStack last;
	private void post(){
		 
		 try {
			 
				this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
				this.getProxy().getGrid().postEvent(new MENetworkStorageEvent(handler0, StorageChannel.FLUIDS));
				try {
	                
					if(last!=null){
					this.getProxy().getStorage()
	                        .postAlterationOfStoredItems(StorageChannel.FLUIDS, 
	                        		ImmutableList.of(last.copy().setStackSize(-last.getStackSize()))
	                        		,new MachineSource(this));
					}
	                last=AEFluidStack.create(content.getFluid());
	                if(last!=null){
	    				this.getProxy().getStorage()
	                            .postAlterationOfStoredItems(StorageChannel.FLUIDS, 
	                            		ImmutableList.of(last.copy().setStackSize(last.getStackSize()))
	                            		,new MachineSource(this));
	    				}
	                
	                
	            } catch (final GridAccessException ignore) {}
		
				
				
				
		} catch (GridAccessException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	   
	}/*
	@MENetworkEventSubscribe
	    public void power(final MENetworkPowerStatusChange c) {
		post();
		
	 }*/
	@Override
	public IGridNode getActionableNode() {
		
		return getProxy().getNode();
	}
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		
		return getProxy().getNode();
	}
	@Override
	public void securityBreak() {
		
	}
	@Override
	public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
		if(channel==StorageChannel.FLUIDS)
		return ImmutableList.of(handler0);
		else
			return ImmutableList.of();
			
	}
	
	
	@Override
	public int getPriority() {
	
		return piority;
	}
	private ItemStack visualStack() {
		return new ItemStack(GregTech_API.sBlockMachines,1, getBaseMetaTileEntity().getMetaTileID());
	}
	AENetworkProxy gridProxy;
	private void updateValidGridProxySides() {
		/*if (disabled) {
			getProxy().setValidSides(EnumSet.noneOf(ForgeDirection.class));
			return;
		}*/
		getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(getBaseMetaTileEntity().getFrontFacing())));

	}
	@Override
	public AENetworkProxy getProxy() {

		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy", visualStack(), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			updateValidGridProxySides();
			if (getBaseMetaTileEntity().getWorld() != null)
				gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
						.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
		}

		return this.gridProxy;
	}
	@Override
	public void saveChanges(IMEInventory cellInventory) {
		
		markDirty();
	}
	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		
		return new SuperTankME(mName, mTier, mInventory.length, mDescriptionArray, mTextures);
	}
	@Override
	public void blinkCell(int slot) {
	
		post();
	}
	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		
		return new ITexture[]{aBaseTexture
				, new IIconTexture
				(ExtraBlockTextures.MEChest.getIcon(),
						0xD7BBEC)
				, new IIconTexture
				(FCPartsTexture.PartFluidTerminal_Bright.getIcon(),
						0xffffff)
		
		};
	}
	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return new ITexture[]{aBaseTexture
				, new IIconTexture
				(ExtraBlockTextures.MEChest.getIcon(),
						0xD7BBEC)
				/*, new IIconTexture
				(ExtraBlockTextures.BlockMEChestItems_Light.getIcon(),
						0xffffff)*/
		
		};
	}
	@Override
	public DimensionalCoord getLocation() {
		
		return new DimensionalCoord((TileEntity)this.getBaseMetaTileEntity());
	}
	
	
	//final FluidList filterList=new FluidList();//new FluidStack(FluidRegistry.WATER,1);
	public Consumer<IItemList> updateFilter;
	FluidStack cachedFilter;
	public void updateFilter(FluidStack fs){
		cachedFilter=fs;
		FluidList fl = new FluidList();
		fl.add(AEFluidStack.create(fs));
		updateFilter.accept(fl);
		post();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	IMEInventoryHandler<AEFluidStack> handler
	=new MEInventoryHandler(new UnlimitedWrapper()
	, StorageChannel .FLUIDS){
		public boolean getSticky() {return sticky&&!suppressSticky;};
		public int getPriority() {return piority;};
		{
			updateFilter=s->
		this.setPartitionList(new PrecisePriorityList(s));
		}
	};	
IMEMonitor handler0= new MEMonitorHandler(handler
		);

	
	
	boolean sticky;
	int piority;
	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
		onColorChangeServer(aBaseMetaTileEntity.getColorization());
		post();
	}
 final FluidTank content=new FluidTank(10000){
	 
	 
	 public FluidStack drain(int maxDrain, boolean doDrain) {if (fluid == null)
     {
         return null;
     }

     int drained = maxDrain;
     if (fluid.amount < drained)
     {
         drained = fluid.amount;
     }

     FluidStack stack = new FluidStack(fluid, drained);
     if (doDrain)
     {
         fluid.amount -= drained;
         if (fluid.amount <= 0)
         {
             fluid=null;
         }

         if (tile != null)
         {
             FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, this, drained));
         }
     }
     return stack;};
 };
	public class UnlimitedWrapper implements IMEInventory<IAEFluidStack> {

	 

	    public UnlimitedWrapper() {
	     
	    }

		@Override
		public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
			int acc=content.fill(input.getFluidStack(), type==Actionable.MODULATE);
			IAEFluidStack  ret = input.copy();
			ret.decStackSize(acc);
			if(voidOverflow&&(content.getFluidAmount()>0&&content.getFluid().getFluid()==input.getFluid())
					){return null;}
			if(ret.getStackSize()==0)return null;
			return ret;
		}

		@Override
		public IAEFluidStack extractItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
		if(content.getFluid()!=null&&content.getFluid().getFluid()!=input.getFluid()
				){return null;}
			FluidStack suck=	content.drain((int) Math.min(Integer.MAX_VALUE,input.getStackSize()),  type==Actionable.MODULATE);
			if(suck!=null&&suck.amount==0)return null;
			return AEFluidStack.create(suck);
		}

		@Override
		public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
		out.addStorage(AEFluidStack.create(content.getFluid()));
			return out;
		}

		@Override
		public StorageChannel getChannel() {
			
			return StorageChannel.FLUIDS;
		}
	} 
	@Override
	public boolean isValidSlot(int aIndex) {
		
		return true;
	}
	 public int getMaxItemCount() {
		
		 return super.getMaxItemCount();
	    }

	@Override
    public boolean isFacingValid(ForgeDirection facing) {
     
		return true;
    }
	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {
	
		return true;
	}
	@Override
	public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {
		return
				
				(aStack.getItem() instanceof ItemFluidPacket)||
				
				GT_Utility.getFluidForFilledItem(aStack, true)!=null
				;
				
				
				
	}
	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
	
		return true;
	}
	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		
		return isItemValidForSlot(aIndex, aStack);
				
	}
	@Override
	public boolean isFluidInputAllowed(FluidStack aFluid) {
		
		return true;
	}
	
	@Override
	public boolean canTankBeFilled() {
	
		return true;
	}
	@Override
	public boolean canTankBeEmptied() {
	
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection side) {
	
		return new FluidTankInfo[]{new FluidTankInfo(this.content)};
	}
	@Override
	public boolean canDrain(ForgeDirection side, Fluid aFluid) {
		//if(side!=this.getBaseMetaTileEntity().getFrontFacing())return false;
		return super.canDrain(side, aFluid);
	}
	@Override
	public int getCapacity() {
	
		return content.getCapacity();
	}
	@Override
	public boolean canFill(ForgeDirection side, Fluid aFluid) {
		//if(side!=this.getBaseMetaTileEntity().getFrontFacing())return false;
		return super.canFill(side, aFluid);
	}
	@Override
	public int fill(FluidStack aFluid, boolean doFill) {
		
		try{
		return content.fill(aFluid, doFill);
		}finally{
			post();
			if(voidOverflow&&(content.getFluidAmount()>0&&content.getFluid().getFluid()==aFluid.getFluid())
			){return aFluid.amount;}
		}
	}
	@Override
	public int fill(ForgeDirection side, FluidStack aFluid, boolean doFill) {
	try{
		return content.fill(aFluid, doFill);
		}finally{
			post();
			if(voidOverflow&&(content.getFluidAmount()>0&&content.getFluid().getFluid()==aFluid.getFluid())
					){return aFluid.amount;}
		}
	}	
	@Override
	public FluidStack getFluid() {
		
		return content.getFluid();
	}
	 public boolean isDrainableStackSeparate() {
	     return true;
	 }
	@Override
	public FluidStack getDrainableStack() {
		
		return content.getFluid();
	}
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		try{
		return content.drain(maxDrain, doDrain);
		}finally{
			post();
		}
	}
	@Override
	public FluidStack drain(ForgeDirection side, FluidStack aFluid, boolean doDrain) {
		try{
		if (content.getFluid() != null && aFluid != null && content.getFluid().isFluidEqual(aFluid))
	         return drain(aFluid.amount, doDrain);
		return null;
		}finally{
			post();
		}
		
	}
	@Override
	public FluidStack drain(ForgeDirection side, int maxDrain, boolean doDrain) {
		try{
		return content.drain(maxDrain, doDrain);
		}finally{
			post();
		}
	}
	
	boolean autoUnlock;
	boolean suppressSticky;
	boolean wasActive;
	int rep;
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		
		if(!aBaseMetaTileEntity.getWorld().isRemote)
		{	if(rep>0){rep--;update=true;}
		if(this.getBaseMetaTileEntity().hasInventoryBeenModified()){
			update=true;
			rep=1;
		};
			if(update){update=false;updateStatus();}
			if(wasActive!=this.getProxy().isActive()){
				wasActive=this.getProxy().isActive();
				post();
			}
			
			
			if (voidFull ) {
			 voidOverflow = false;
			 content.setFluid(null);
         }
		if(content.getFluidAmount()>0){
			
			if(content.getFluidAmount()>0)
				content.getFluid().amount=Math.min
				(commonSizeCompute(mTier),content.getFluid().amount);
		}
		}
		
		if(!aBaseMetaTileEntity.getWorld().isRemote&&(aTick&16)!=0){
			this.getBaseMetaTileEntity().setActive(
			this.getProxy().isPowered()&&this.getProxy().isActive()
			)
			;
			
		}

		if((!suppressSticky)&&((content.getFluidAmount()==0)&&autoUnlock)){
			
				suppressSticky=true;	
				post();
			
		}
		if(suppressSticky&&( (content.getFluidAmount()>0)||(!autoUnlock) )){
			
				suppressSticky=false;	
				post();
			
		}
		super.onPostTick(aBaseMetaTileEntity, aTick);
		boolean needToSort=false;
		for(int i=0;i<mInventory.length;i++){
			if(mInventory[i]==null)continue;
			
			if(mInventory[i]!=null&&mInventory[i].getItem() instanceof ItemFluidPacket){
				needToSort=true;
				FluidStack fs = ItemFluidPacket.getFluidStack(mInventory[i]);
				if(fs==null){continue;}
				if(fill(fs, false)!=fs.amount){continue;}
			 fill(fs, true);mInventory[i]=null;
			 continue;
			}
			
			if(
			FluidContainerRegistry.isFilledContainer(mInventory[i])){
				FluidStack f = GT_Utility.getFluidForFilledItem(mInventory[i], true);
				ItemStack it = GT_Utility.getContainerForFilledItem(mInventory[i], true);
				f.amount*=mInventory[i].stackSize;
				if(f!=null&&content.fill(f, false)==f.amount){
				content.fill(f, true);
				it.stackSize=mInventory[i].stackSize;
				mInventory[i]=it;
				needToSort=true;
			}
			}
			if(mInventory[i]==null)continue;
			
			if(FluidContainerRegistry.isEmptyContainer(mInventory[i])
					||(mInventory[i].getItem() instanceof IFluidContainerItem
				&&((IFluidContainerItem)mInventory[i].getItem()).getFluid(mInventory[i])==null
							)
				){
				needToSort=true;
				try {
					IAEItemStack notadd = this.getProxy().getStorage().getItemInventory().injectItems(
					        AEApi.instance().storage().createItemStack(mInventory[i]),
					        Actionable.MODULATE,
					        new MachineSource(this));
					if(notadd!=null){
						
						mInventory[i].stackSize=(int) notadd.getStackSize();
					}else{
						mInventory[i]=null;
					}
				} catch (GridAccessException e) {
				
				}
				
				
			}
			
			
		}
		if(needToSort)fillStacksIntoFirstSlots();
	}
	   @Override
	    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		   if(ProghatchesUtil.handleUse(aPlayer,  (MetaTileEntity) aBaseMetaTileEntity.getMetaTileEntity())){
		    	 return true;
		     }
		   GT_UIInfos.openGTTileEntityUI(aBaseMetaTileEntity, aPlayer);
	        return true;
	    }
@Override
public boolean useModularUI() {
	return true;
}
@Override
public boolean isUseableByPlayer(EntityPlayer entityplayer) {
	
	return true;
}
ItemStackHandler uihandler=new ItemStackHandler(mInventory){
	
	public boolean isItemValid(int slot, ItemStack stack) {
		return isItemValidForSlot(slot, stack);
	};
	
};
@Override
public ItemStackHandler getInventoryHandler() {
	
	return uihandler;
}
 @Override
public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
builder.widget(new FluidSlotWidget(content)
			 .setPos(3, 3)
			 );

	 builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_LEFT)
			 
			 .setPos(3+18, 3).setSize(18,18));
	 builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 0))
			 .setPos(3+18*2, 3)
			 );
	 builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 1))
			 .setPos(3+18*3, 3)
			 );
	 
	 Widget w;
     builder.widget(w=new DrawableWidget().setDrawable(ModularUITextures.ICON_INFO)
			 
			 .setPos(3+18*4+1, 3+1).setSize(16,16)
			// .addTooltip("xxxxxxx")
    		 );
     
     
    
     
    		 
 IntStream
		.range(0,
				Integer.valueOf(StatCollector.translateToLocal(
						"programmable_hatches.gt.metank.tooltip")))
		.forEach(s -> w.addTooltip(LangManager.translateToLocal(
				"programmable_hatches.gt.metank.tooltip." +  + s)));
 
 
 builder.widget(createButton(() -> 
	sticky
			, val -> {
				sticky = val;post();
		//updateSlots();
	}, 
			new ItemDrawable(new ItemStack(Items.slime_ball)), 
	ImmutableList.of(
			StatCollector.translateToLocal("programmable_hatches.gt.sticky")
		
			)

	
	, 0)
			.setPos( 3,3+18*2));

 builder.widget(createButton(() -> 
	autoUnlock
			, val -> {
				autoUnlock = val;
				cachedFilter=null;
				updateFilter(cachedFilter);post();
		//updateSlots();
	}, GT_UITextures.OVERLAY_BUTTON_RECIPE_UNLOCKED,
			
			//new ItemDrawable(new ItemStack(Items.slime_ball)), 
	ImmutableList.of(
			StatCollector.translateToLocal("programmable_hatches.gt.sticky.autounlock")
		
			)

	
	, 0)
			.setPos( 3+18,3+18*2));
 
 
 builder.widget(FluidSlotWidget.phantom(new MappingFluidTank(s->{cachedFilter=s==null?null:s.copy();if(s!=null){autoUnlock=false;}updateFilter(cachedFilter);}, ()->cachedFilter), 
		 false
		 ).setPos( 3+18*2,3+18*2).addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.phantom.filter"))
		
		 );
 
 
 
 builder.widget(new TextFieldWidget()	
		 .setPattern(BaseTextFieldWidget.NATURAL_NUMS)
		.setGetter(()->piority+"")
		.setSetter(s->
		{try{piority=Integer.parseInt(s);}catch(Exception e){piority=0;};post();})
		 .setSynced(true,true)
		
		 .setFocusOnGuiOpen(true).setTextColor(Color.WHITE.dark(1))

			.setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
			.addTooltip(StatCollector.translateToLocal("programmable_hatches.gt.piority"))
			.setPos(3+2,18*3+3+1).setSize(16*8,16))
 .widget(new CycleButtonWidget().setToggle(() -> voidFull, val -> {
	 voidFull = val;
  
     if (!voidFull) {
         GT_Utility.sendChatToPlayer(
             buildContext.getPlayer(),
             GT_Utility.trans("269", "Void Full Mode Disabled"));
     } else {
         GT_Utility.sendChatToPlayer(
             buildContext.getPlayer(),
             GT_Utility.trans("270", "Void Full Mode Enabled"));
     }
 })
     .setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE)
     .setStaticTexture(GT_UITextures.OVERLAY_BUTTON_TANK_VOID_ALL)
     .setGTTooltip(() -> mTooltipCache.getData("GT5U.machines.digitaltank.voidfull.tooltip"))
     .setTooltipShowUpDelay(TOOLTIP_DELAY)
     .setPos(3+18*3,3+18*2)
     .setSize(18, 18))
 
 .widget(new CycleButtonWidget().setToggle(() -> voidOverflow, val -> {
	 voidOverflow = val;
	
     if (!voidOverflow) {
         GT_Utility.sendChatToPlayer(
             buildContext.getPlayer(),
             GT_Utility.trans("267", "Overflow Voiding Mode Disabled"));
     } else {
         GT_Utility.sendChatToPlayer(
             buildContext.getPlayer(),
             GT_Utility.trans("268", "Overflow Voiding Mode Enabled"));
     }
 })
     .setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE)
     .setStaticTexture(GT_UITextures.OVERLAY_BUTTON_TANK_VOID_EXCESS)
     .setGTTooltip(() -> mTooltipCache.getData("GT5U.machines.digitaltank.voidoverflow.tooltip"))
     .setTooltipShowUpDelay(TOOLTIP_DELAY)
     .setPos(3+18*4,3+18*2)
     .setSize(18, 18))
 ;
 ;
 
 
 
}
 @Override
public void onFacingChange() {
	updateValidGridProxySides();
}
 private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, IDrawable picture,
		List<String> tooltip, int offset) {
	return new CycleButtonWidget().setToggle(getter, setter).setTextureGetter(__->picture)
			.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
			.setPos(7 + offset * 18, 62).setSize(18, 18).addTooltips(tooltip);
}
 @Override
public void loadNBTData(NBTTagCompound aNBT) {
	 if(aNBT.hasKey("proxy"))getProxy().readFromNBT(aNBT);
	super.loadNBTData(aNBT);
	piority=aNBT.getInteger("piority");
	sticky=	aNBT.getBoolean("sticky");
	autoUnlock=aNBT.getBoolean("autoUnlock");
	suppressSticky=aNBT.getBoolean("suppressSticky");
	content.readFromNBT(aNBT);
	
	NBTTagCompound tag=(NBTTagCompound) aNBT.getTag("cahcedFilter");
	if(tag!=null){
		cachedFilter=FluidStack.loadFluidStackFromNBT(tag);
		updateFilter(cachedFilter);	
	}
	voidFull=aNBT.getBoolean("voidFull" );
	voidOverflow= aNBT.getBoolean("voidOverflow" );
	last=AEFluidStack.create(content.getFluid());
}
 protected static int commonSizeCompute(int tier) {
     switch (tier) {
         case 1 : return 4000000;
         case 2 : return 8000000;
         case 3 : return 16000000;
         case 4 : return 32000000;
         case 5 : return 64000000;
         case 6 : return 128000000;
         case 7 : return 256000000;
         case 8 : return 512000000;
         case 9 : return 1024000000;
         case 10 : return 2147483640;
         default : return 0;
     }
 }
@Override
public void saveNBTData(NBTTagCompound aNBT) {
	 getProxy().writeToNBT(aNBT);
	super.saveNBTData(aNBT);
	
	content.writeToNBT(aNBT);
	aNBT.setInteger("piority", piority);
	aNBT.setBoolean("sticky", sticky);
	aNBT.setBoolean("autoUnlock",autoUnlock);
	aNBT.setBoolean("suppressSticky",suppressSticky);
	if(cachedFilter!=null){
		NBTTagCompound tag=new NBTTagCompound();
		cachedFilter.writeToNBT(tag);
		aNBT.setTag("cahcedFilter", tag);
	}
	
	aNBT.setBoolean("voidFull", voidFull);
	 aNBT.setBoolean("voidOverflow", voidOverflow);
	
	
}
@Override
public void setItemNBT(NBTTagCompound aNBT) {
	content.writeToNBT(aNBT);
    if(piority!=0)aNBT.setInteger("piority", piority);
    if(sticky)aNBT.setBoolean("sticky", sticky);
    if(voidFull)aNBT.setBoolean("voidFull", voidFull);
    if(voidOverflow)aNBT.setBoolean("voidOverflow", voidOverflow);
}
@Override
public boolean shouldDropItemAt(int index) {

	return true;
}

public static String name(int t){
	
	return StatCollector.translateToLocalFormatted("mesupertank.name."+(t>=6), suffix[t-1]);
}
public static String[] suffix={"I","II","III","IV","V","I","II","III","IV","V"};
@Override
public void onColorChangeServer(byte aColor) {
	
	super.onColorChangeServer(aColor);
	AEColor c;
	if(aColor==-1){
		c=(AEColor.Transparent);
	}else
	c=(AEColor.values()[15-aColor]);

try{
getProxy().setColor(c);
getGridNode(null).updateState();
}catch(Exception e){}

}

protected void fillStacksIntoFirstSlots() {
    final int L = mInventory.length;
    HashMap<GT_Utility.ItemId, Integer> slots = new HashMap<>(L);
    HashMap<GT_Utility.ItemId, ItemStack> stacks = new HashMap<>(L);
    List<GT_Utility.ItemId> order = new ArrayList<>(L);
    List<Integer> validSlots = new ArrayList<>(L);
    for (int i = 0; i < L; i++) {
        if (!isValidSlot(i)) continue;
        validSlots.add(i);
        ItemStack s = mInventory[i];
        if (s == null) continue;
        GT_Utility.ItemId sID = GT_Utility.ItemId.createNoCopy(s);
        slots.merge(sID, s.stackSize, Integer::sum);
        if (!stacks.containsKey(sID)) stacks.put(sID, s);
        order.add(sID);
        mInventory[i] = null;
    }
    int slotindex = 0;
    for (GT_Utility.ItemId sID : order) {
        int toSet = slots.get(sID);
        if (toSet == 0) continue;
        int slot = validSlots.get(slotindex);
        slotindex++;
        mInventory[slot] = stacks.get(sID)
            .copy();
        toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
        mInventory[slot].stackSize = toSet;
        slots.merge(sID, toSet, (a, b) -> a - b);
    }
}
@Override
public void setPriority(int newValue) {
this.piority=newValue;

	
}
/*
@MENetworkEventSubscribe
public void powerRender(final MENetworkPowerStatusChange c) {
    this.updateStatus();
}*/

/*
@MENetworkEventSubscribe
public void chanRender(final MENetworkChannelsChanged changedChannels) {
    this.updateStatus();
}
@MENetworkEventSubscribe
public void updateChannels(final MENetworkChannelsChanged changedChannels) {
    this.updateStatus();
}*/
static MENetworkCellArrayUpdate event=new MENetworkCellArrayUpdate();
public void updateStatus() {
   
	 post();
       
}
boolean voidFull;
boolean voidOverflow;
private boolean update;
@Override
public void cellUpdate() {
	update=true;
	
}

}
