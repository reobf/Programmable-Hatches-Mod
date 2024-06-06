package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
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
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEIInventoryWrapper;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import gregtech.api.GregTech_API;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.util.BaseSlotPatched;
import reobf.proghatches.main.registration.Registration;

public class SuperChestME extends GT_MetaTileEntity_Hatch implements ICellContainer, IGridProxyable{

	public SuperChestME(String aName, int aTier, int aInvSlotCount, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
	
	}
	public SuperChestME(int aID, String aName, String aNameRegional, int aTier, int aInvSlotCount
			) {
		super(aID, aName, aNameRegional, aTier, aInvSlotCount, new String[0], new ITexture[0]);
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
		
		return 0;
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
		
		return new ITexture[]{aBaseTexture};
	}
	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		// TODO Auto-generated method stub
		return new ITexture[]{aBaseTexture};
	}
	@Override
	public DimensionalCoord getLocation() {
		
		return new DimensionalCoord((TileEntity)this.getBaseMetaTileEntity());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	IMEInventoryHandler<AEItemStack> handler
	=new MEInventoryHandler(new UnlimitedWrapper()
	, StorageChannel .ITEMS);
	
	@Override
	public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {

		super.onFirstTick(aBaseMetaTileEntity);
		getProxy().onReady();
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
			if(thiz!=null&&!Platform.isSameItem(in, thiz))return input;
			if(thiz==null){thiz=in.copy(); }
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
	
		return aBaseMetaTileEntity.getFrontFacing()==side
				&&aIndex==0
				;
	}
	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		
		return aBaseMetaTileEntity.getFrontFacing()==side
				&&aIndex!=0
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
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
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
     builder.widget(new DrawableWidget().setDrawable(ModularUITextures.ICON_INFO)
			 
			 .setPos(3+18*4+1, 3+1).setSize(16,16)
			 .addTooltip("xxxxxxx")
    		 
    		 );

}
 @Override
public void loadNBTData(NBTTagCompound aNBT) {
	 getProxy().readFromNBT(aNBT);
	super.loadNBTData(aNBT);
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
	
	
}
public static String name(int t){
	
	return StatCollector.translateToLocalFormatted("mesuperchest.name."+(t>=5), suffix[t-1]);
}
public static String[] suffix={"I","II","III","IV","V","I","II","III","IV","V"};

}
