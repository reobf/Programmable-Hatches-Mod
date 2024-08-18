package reobf.proghatches.gt.metatileentity;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.objects.XSTR.XSTR_INSTANCE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;

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
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IAETagCompound;
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
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.item.FluidList;
import appeng.util.item.ItemList;
import appeng.util.prioitylist.PrecisePriorityList;
import gregtech.api.GregTech_API;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_Utility;
import io.netty.buffer.ByteBuf;
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
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.gt.metatileentity.util.MappingFluidTank;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.IIconTexture;
import reobf.proghatches.util.ProghatchesUtil;

public class SuperChestME extends GT_MetaTileEntity_Hatch implements ICellContainer, IGridProxyable
,IPriorityHost
{

	public SuperChestME(String aName, int aTier, int aInvSlotCount, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
	
	}
	public SuperChestME(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount
			) {
		super(aID, aName, aNameRegional, aTier, aInvSlotCount,  reobf.proghatches.main.Config.get("SCME", 
				ImmutableMap.of(
						"items",commonSizeCompute(aTier)
						)
				
				), new ITexture[0]);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}
	@Override
	public int getInventoryStackLimit() {
	
		return cap();
	}
	public int cap(){
		
		return commonSizeCompute(mTier);
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
	@MENetworkEventSubscribe
    public void channel(final MENetworkChannelsChanged c) {
		post();
    }
	private void post(){
		 
		 try {
			 
			this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
			
			//System.out.println(getGridNode(null).isActive());
		} catch (GridAccessException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	   
	}
	@MENetworkEventSubscribe
	    public void power(final MENetworkPowerStatusChange c) {
		post();
		
	 }
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
		if(channel==StorageChannel.ITEMS)
		return ImmutableList.of(handler);
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
		
		return new SuperChestME(mName, mTier, mInventory.length, mDescriptionArray, mTextures);
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
				(ExtraBlockTextures.BlockMEChestItems_Light.getIcon(),
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
	public Consumer<IItemList> updateFilter;
	ItemStack[] cachedFilter=new ItemStack[1];
	public void updateFilter(ItemStack fs){
		cachedFilter[0]=fs;
		ItemList fl = new ItemList();
		fl.add(AEItemStack.create(fs));
		updateFilter.accept(fl);
		post();
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	IMEInventoryHandler<AEItemStack> handler
	=new MEInventoryHandler(new UnlimitedWrapper()
	, StorageChannel .ITEMS){
		public boolean getSticky() {return sticky&&!suppressSticky;};
		public int getPriority() {return piority;};
		{
			updateFilter=s->
		this.setPartitionList(new PrecisePriorityList(s));
		}
	};
	boolean sticky;
	int piority;
	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
		onColorChangeServer(aBaseMetaTileEntity.getColorization());
	}

	public class UnlimitedWrapper implements IMEInventory<IAEItemStack> {

	 

	    public UnlimitedWrapper() {
	     
	    }

		@Override
		public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
			long l=input.getStackSize();
			long compl=0;
			if(l>Integer.MAX_VALUE){compl=l-Integer.MAX_VALUE;}
			ItemStack in=input.getItemStack();
			ItemStack thiz=mInventory[0];
			if(thiz!=null&&!Platform.isSameItem(in, thiz))return input;
			if(thiz==null){thiz=in.copy();thiz.stackSize=0; }
			int space=Math.max(0, cap()-thiz.stackSize);
			int transfer=Math.min(space,in.stackSize);
			if(type==Actionable.SIMULATE){
				in.stackSize-=transfer;
				if(in.stackSize<=0&&compl==0)in=null;
				AEItemStack ret= AEItemStack.create(in);
				if(ret!=null)ret.incStackSize(compl);
				return ret;
			}
			if(type==Actionable.MODULATE){
				thiz.stackSize+=transfer;
				mInventory[0]=thiz;
				in.stackSize-=transfer;
				if(in.stackSize<=0&&compl==0)in=null;
				AEItemStack ret= AEItemStack.create(in);
				if(ret!=null)ret.incStackSize(compl);
				return ret;
				
			}
			
			
			return null;
		}

		@Override
		public IAEItemStack extractItems(IAEItemStack input, Actionable type, BaseActionSource src) {
		
			ItemStack in=input.getItemStack();
			ItemStack thiz=mInventory[0];
			if(thiz!=null&&!Platform.isSameItem(in, thiz))return null;
			if(thiz==null){return null;}//thiz=in.copy(); }
			int transfer=Math.min(in.stackSize,thiz.stackSize);
			if(transfer==0)return null;
			if(type==Actionable.SIMULATE){
				in.stackSize=transfer;
				return AEItemStack.create(in);
				
			}
			if(type==Actionable.MODULATE){
				thiz.stackSize-=transfer;
				if(thiz.stackSize<=0)thiz=null;
				mInventory[0]=thiz;
				in.stackSize=transfer;
				return AEItemStack.create(in);
			}
			
			
			return null;
		}
		@Override
		public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
		out.addStorage(AEItemStack.create(mInventory[0]));
			return out;
		}

		@Override
		public StorageChannel getChannel() {
			
			return StorageChannel.ITEMS;
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
		if(aIndex==0)return true;
		if(mInventory[0]==null||mInventory[0].stackSize==0){
			return true;
		}
		return Platform.isSameItem(mInventory[0],aStack);
	}
	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
	
		return true
				;
	}
	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		
		return true
				;
	}
	protected void fillStacksIntoFirstSlots() {
        final int L = mInventory.length;
        HashMap<GT_Utility.ItemId, Integer> slots = new HashMap<>(L);
        HashMap<GT_Utility.ItemId, ItemStack> stacks = new HashMap<>(L);
        List<GT_Utility.ItemId> order = new ArrayList<>(L);
        List<Integer> validSlots = new ArrayList<>(L);
        for (int i = 1; i < L; i++) {
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
	boolean autoUnlock;
	boolean suppressSticky;
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		if(!aBaseMetaTileEntity.getWorld().isRemote&&(aTick&16)!=0){
			this.getBaseMetaTileEntity().setActive(
			this.getProxy().isPowered()&&this.getProxy().isActive()
			)
			;
			
		}
		
			if((!suppressSticky)&&((mInventory[0]==null)&&autoUnlock)){
				
					suppressSticky=true;	
					post();
				
			}
			if(suppressSticky&&( (mInventory[0]!=null)||(!autoUnlock) )){
				
					suppressSticky=false;	
					post();
				
			}
		
		
		
		
		super.onPostTick(aBaseMetaTileEntity, aTick);
		boolean needToSort=false;
		for(int i=1;i<mInventory.length;i++){
			ItemStack is = mInventory[i];
			if(is==null)continue;
			markDirty();
			if(mInventory[0]==null){
				mInventory[0]=is.copy();
				mInventory[i]=null;
			}
			else
			if(cap()-is.stackSize>=mInventory[0].stackSize){
				mInventory[0].stackSize+=is.stackSize;
				mInventory[i]=null;
			}
			else{
				int to=Math.min(cap()-mInventory[0].stackSize,is.stackSize);
				mInventory[0].stackSize+=to;
				mInventory[i].stackSize-=to;
				needToSort=true;
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
	 builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 0))
			 
			 .setPos(3, 3)
			 );
	 builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ARROW_LEFT)
			 
			 .setPos(3+18, 3).setSize(18,18));
	 builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 1))
			 .setPos(3+18*2, 3)
			 );
	 builder.widget(new SlotWidget(new BaseSlotPatched(this.getInventoryHandler(), 2))
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
						"programmable_hatches.gt.mechest.tooltip")))
		.forEach(s -> w.addTooltip(LangManager.translateToLocal(
				"programmable_hatches.gt.mechest.tooltip." +  + s)));
 
 
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
			
			cachedFilter[0]=null;
			updateFilter(cachedFilter[0]);
				autoUnlock = val;post();
		//updateSlots();
	}, GT_UITextures.OVERLAY_BUTTON_RECIPE_UNLOCKED,
			
			//new ItemDrawable(new ItemStack(Items.slime_ball)), 
	ImmutableList.of(
			StatCollector.translateToLocal("programmable_hatches.gt.sticky.autounlock")
		
			)

	
	, 0)
			.setPos( 3+18,3+18*2));
 builder.widget(SlotWidget.phantom(
		 
		 new ItemStackHandler(cachedFilter){
			 public void setStackInSlot(int slot, ItemStack stack) {
				 super.setStackInSlot(slot, stack);
				 updateFilter(cachedFilter[0]);
				 autoUnlock=false;
				 post();
				 };
			 }, 0
		
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
 
 ;
 
 
 
}
 private Widget createButton(Supplier<Boolean> getter, Consumer<Boolean> setter, IDrawable picture,
		List<String> tooltip, int offset) {
	return new CycleButtonWidget()
	
		.setToggle(getter, setter).setTextureGetter(__->picture)
			.setVariableBackground(GT_UITextures.BUTTON_STANDARD_TOGGLE).setTooltipShowUpDelay(TOOLTIP_DELAY)
			.setPos(7 + offset * 18, 62).setSize(18, 18).addTooltips(tooltip);
}
 @Override
public void loadNBTData(NBTTagCompound aNBT) {
	
	 getProxy().readFromNBT(aNBT);
	super.loadNBTData(aNBT);
	piority=aNBT.getInteger("piority");
	sticky=	aNBT.getBoolean("sticky");
	autoUnlock=aNBT.getBoolean("autoUnlock");
	suppressSticky=aNBT.getBoolean("suppressSticky");
	NBTTagCompound tag=(NBTTagCompound) aNBT.getTag("cahcedFilter");
	if(tag!=null){
		cachedFilter[0]=ItemStack.loadItemStackFromNBT(tag);
		updateFilter(cachedFilter[0]);	
	}
}
 
@Override
public void saveNBTData(NBTTagCompound aNBT) {
	 getProxy().writeToNBT(aNBT);
	super.saveNBTData(aNBT);
	NBTTagList greggy=aNBT.getTagList("Inventory", 10);
	for(int i=0;i<mInventory.length;i++){
	
		if( mInventory[i]!=null){	
			NBTTagCompound t;
			t=((NBTTagCompound)greggy.getCompoundTagAt(i));
			if(t!=null)t.setInteger("Count", mInventory[i].stackSize);}
		
	}
	aNBT.setInteger("piority", piority);
	aNBT.setBoolean("sticky", sticky);
	aNBT.setBoolean("autoUnlock",autoUnlock);
	aNBT.setBoolean("suppressSticky",suppressSticky);
	if(cachedFilter[0]!=null){
		NBTTagCompound tag=new NBTTagCompound();
		cachedFilter[0].writeToNBT(tag);
		aNBT.setTag("cahcedFilter", tag);
	}
	
}@Override
public void onFacingChange() {
	updateValidGridProxySides();
}
@Override
public void setItemNBT(NBTTagCompound aNBT) {
	
	final NBTTagList tItemList = new NBTTagList();
    for (int i = 0; i < getRealInventory().length; i++) {
        final ItemStack tStack = getRealInventory()[i];
        if (tStack != null) {
            final NBTTagCompound tTag = new NBTTagCompound();
            tTag.setInteger("IntSlot", i);
            tStack.writeToNBT(tTag);
            tTag.setInteger("Count", tStack.stackSize);
            tItemList.appendTag(tTag);
        }
    }
    aNBT.setTag("Inventory", tItemList);
    if(piority!=0)aNBT.setInteger("piority", piority);
    if(sticky)aNBT.setBoolean("sticky", sticky);
}
@Override
public boolean shouldDropItemAt(int index) {

	return false;
}
public static String name(int t){
	
	return StatCollector.translateToLocalFormatted("mesuperchest.name."+(t>=6), suffix[t-1]);
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
@Override
public void setPriority(int newValue) {
this.piority=newValue;
	
}



@MENetworkEventSubscribe
public void powerRender(final MENetworkPowerStatusChange c) {
    this.updateStatus();
}


@MENetworkEventSubscribe
public void chanRender(final MENetworkChannelsChanged changedChannels) {
    this.updateStatus();
}
@MENetworkEventSubscribe
public void updateChannels(final MENetworkChannelsChanged changedChannels) {
    this.updateStatus();
}
protected void updateStatus() {
   
            try {
				this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
			} catch (GridAccessException e) {
			
				e.printStackTrace();
			}
       
}

}
