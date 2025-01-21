package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.apache.commons.io.output.NullOutputStream;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import com.google.common.collect.ImmutableSet;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.GridFlags;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IOrientable;
import appeng.crafting.CraftingLink;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.util.GTUtility;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class TileRequestTunnel  extends AENetworkTile implements ICraftingMachine,ICraftingRequester,IOrientable
,ISidedInventory,IFluidHandler


{
	 public static IWailaDataProvider provider=new IWailaDataProvider(){

		@Override
		public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
			
			return null;
		}

		@Override
		public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
			
			return null;
		}

		@Override
		public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
			 try{
				if( accessor.getSide()==((TileRequestTunnel)accessor.getTileEntity()).getUp().getOpposite()){
					
					currenttip.add("Auto-output to this side");
					
				};
				 
				 
				 
				 
			NBTTagCompound data = accessor.getNBTData();
			
			ArrayList<ItemStack> cacheR=new ArrayList<>();
			 {	 NBTTagList t= (NBTTagList) data.getTag("cacheR");
				 for(int i=0;i<t.tagCount();i++){
					 cacheR.add(ItemStack.loadItemStackFromNBT(t.getCompoundTagAt(i))); 
					 
				}	 }
			 ArrayList<FluidStack> cacheFR=new ArrayList<>();
			 {	 NBTTagList t= (NBTTagList) data.getTag("cacheFR");
			 for(int i=0;i<t.tagCount();i++){
				 cacheFR.add(FluidStack.loadFluidStackFromNBT(t.getCompoundTagAt(i))); 
				 
			}	 }
			 
			 List<String> ret=currenttip;
			 
			
			 
			 if(cacheR.isEmpty()==false||cacheFR.isEmpty()==false)
				 ret.add("Sending:");
			 
			 cacheR.forEach(s->{
				 
				 ret.add(""+s.getDisplayName()+" x"+s.stackSize);
				 
			 });
			 
			 cacheFR.forEach(s->{
				 
				 ret.add(""+s.getFluid().getLocalizedName(s)+" x"+s.amount+"mB");
				 
			 });
			 NavigableMap<AEItemStack,Long> waiting=new TreeMap<>();
			 NBTTagList t= (NBTTagList) data.getTag("waiting");
			 for(int i=0;i<t.tagCount();i++){
				NBTTagCompound tag = t.getCompoundTagAt(i); 
				
			AEItemStack key = (AEItemStack) AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("key"));
			long value = (tag.getLong("value"));	
			waiting.put(key, value);	
			 }
			 ret.add("Waiting for:");
			 waiting.entrySet().forEach(s->{
				 
				 ret.add(""+s.getKey().getDisplayName()+" x"+s.getValue());
				 
				 
			 });
			 
			 
			 
			 
			return ret;}catch(Exception e){e.printStackTrace();}
			return currenttip;
		}

		@Override
		public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
			
			return null;
		}

		@Override
		public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound data, World world, int x,
				int y, int z) {
			
			
			TileRequestTunnel thiz= (TileRequestTunnel) te;
			{
			NBTTagList listR=new NBTTagList();
			thiz. cacheR.stream().map(s->
				s.writeToNBT(new NBTTagCompound()) 
				).forEach(s->listR.appendTag(s));
			 data.setTag("cacheR", listR);
			 NBTTagList  listFR=new NBTTagList();
			 thiz. cacheFR.stream().map(s->
				s.writeToNBT(new NBTTagCompound()) 
				).forEach(s->listFR.appendTag(s));
			 data.setTag("cacheFR", listFR);
		}
			 NBTTagList  listR=new NBTTagList();
			 thiz.waiting.entrySet().stream().map(s->
				{
					NBTTagCompound t=new NBTTagCompound();
					NBTTagCompound k=new NBTTagCompound();
					s.getKey().writeToNBT(k);
					t.setTag("key", k);
					t.setLong("value", s.getValue());
					
					return t;
				}
				).forEach(s->listR.appendTag(s)) ;
			 data.setTag("waiting", listR);;
			return data;
		}};
	public TileRequestTunnel() {
	        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	    }
	 ArrayList<ItemStack> cache=new ArrayList<>();
	 ArrayList<FluidStack> cacheF=new ArrayList<>();
	 HashMap<AEItemStack,Long> waiting=new HashMap<>();
	 ArrayList<ItemStack> cacheR=new ArrayList<>();
	 ArrayList<FluidStack> cacheFR=new ArrayList<>();
	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
	for(int i=0;i<table.getSizeInventory();i++){
		ItemStack is=table.getStackInSlot(i);
		if(is!=null){
			if(is.getItem() instanceof ItemFluidPacket){
				cacheF.add(ItemFluidPacket.getFluidStack(is));
			}else{
				cache.add((is));
			}
			
		}
	}
	dump();
	
	for(IAEItemStack item:patternDetails.getCondensedOutputs()){
		if(waiting.get(item)==null){
			waiting.put((AEItemStack) item, 0l);
		};
		waiting.put((AEItemStack) item,waiting.get(item)+item.getStackSize());
	}
	
	
	
	
	return true;
	}
 @TileEvent(TileEventType.WORLD_NBT_READ)
public void readFromNBT_AENetworkX(NBTTagCompound data) {
	 cache.clear();
	 {	 NBTTagList t= (NBTTagList) data.getTag("cache");
		 for(int i=0;i<t.tagCount();i++){
			 cache.add(ItemStack.loadItemStackFromNBT(t.getCompoundTagAt(i))); 
			 
		}	 }
	 cacheF.clear();
	 {	 NBTTagList t= (NBTTagList) data.getTag("cacheF");
	 for(int i=0;i<t.tagCount();i++){
		 cacheF.add(FluidStack.loadFluidStackFromNBT(t.getCompoundTagAt(i))); 
		 
	}	 }
	 
	 cacheR.clear();
	 {	 NBTTagList t= (NBTTagList) data.getTag("cacheR");
		 for(int i=0;i<t.tagCount();i++){
			 cacheR.add(ItemStack.loadItemStackFromNBT(t.getCompoundTagAt(i))); 
			 
		}	 }
	 cacheFR.clear();
	 {	 NBTTagList t= (NBTTagList) data.getTag("cacheFR");
	 for(int i=0;i<t.tagCount();i++){
		 cacheFR.add(FluidStack.loadFluidStackFromNBT(t.getCompoundTagAt(i))); 
		 
	}	 }
	 
	 waiting.clear();
	 
	 NBTTagList t= (NBTTagList) data.getTag("waiting");
	 for(int i=0;i<t.tagCount();i++){
		NBTTagCompound tag = t.getCompoundTagAt(i); 
		
	AEItemStack key = (AEItemStack) AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("key"));
	long value = (tag.getLong("value"));	
	waiting.put(key, value);	
	 }
	 NBTTagCompound tag=data.getCompoundTag("link");
	 if(tag.hasNoTags()==false){
			tag.setBoolean("req", true);
	    	last=new CraftingLink(tag, this);
	    	
	  }
	 
}
 @TileEvent(TileEventType.WORLD_NBT_WRITE)
 public void writeToNBT_AENetworkX( NBTTagCompound data) {
	 NBTTagList list=new NBTTagList();
	 cache.stream().map(s->
		s.writeToNBT(new NBTTagCompound()) 
		).forEach(s->list.appendTag(s));
	 data.setTag("cache", list);
	 NBTTagList  listF=new NBTTagList();
	 cacheF.stream().map(s->
		s.writeToNBT(new NBTTagCompound()) 
		).forEach(s->listF.appendTag(s));
	 data.setTag("cacheF", listF);
	 {
	 NBTTagList listR=new NBTTagList();
	 cacheR.stream().map(s->
		s.writeToNBT(new NBTTagCompound()) 
		).forEach(s->listR.appendTag(s));
	 data.setTag("cacheR", listR);
	 NBTTagList  listFR=new NBTTagList();
	 cacheFR.stream().map(s->
		s.writeToNBT(new NBTTagCompound()) 
		).forEach(s->listFR.appendTag(s));
	 data.setTag("cacheFR", listFR);
	 }
	 
	 
	 
	 
	 
	 NBTTagList  listR=new NBTTagList();
	 waiting.entrySet().stream().map(s->
		{
			NBTTagCompound t=new NBTTagCompound();
			NBTTagCompound k=new NBTTagCompound();
			s.getKey().writeToNBT(k);
			t.setTag("key", k);
			t.setLong("value", s.getValue());
			
			return t;
		}
		).forEach(s->listR.appendTag(s));
	 data.setTag("waiting", listR);
	 if(last!=null){
	    	NBTTagCompound tag=new NBTTagCompound();
	    	last.writeToNBT(tag);
	    	data.setTag("link", tag);
	    }
	 
 }
	public void dump(){
			try {
			IMEMonitor<IAEItemStack> i = getProxy().getStorage().getItemInventory();
			cache.removeIf(s->{
				IAEItemStack left = i.injectItems(AEItemStack.create(s), Actionable.MODULATE, new MachineSource(this));
			if(left==null||left.getStackSize()<=0){return true;}
				s.stackSize=(int) left.getStackSize();
			return false;
			});
			} catch (GridAccessException e) {
				
			}
		
			try {
				IMEMonitor<IAEFluidStack> i = getProxy().getStorage().getFluidInventory();
				cacheF.removeIf(s->{
					IAEFluidStack left = i.injectItems(AEFluidStack.create(s), Actionable.MODULATE, new MachineSource(this));
				if(left==null||left.getStackSize()<=0){return true;}
					s.amount=(int) left.getStackSize();
				return false;
				});
				} catch (GridAccessException e) {
					
				}
		
	}
	
	
	
	
	@Override
	public boolean acceptsPlans() {
		
		return true;
	}
	
	ICraftingLink  last;
	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		return last==null?ImmutableSet.of():ImmutableSet.of(last);
	}
	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
	if(mode==Actionable.SIMULATE){
		
		return null;
	}
		/*Long l=waiting.get(items);
		if(l!=null){
			long todo=Math.min(l, items.getStackSize());
			IAEItemStack t = items.copy().setStackSize(todo);
			t=complete(t);
			todo=todo-((t==null)?0:t.getStackSize());
			waiting.put((AEItemStack) items, l-todo);
			items.decStackSize(todo);
			if(l-todo<=0){
				waiting.remove(items);
			}
			if(items.getStackSize()<=0)items=null;
			
			
		}*/
	if(items.getItem() instanceof ItemFluidDrop){
		cacheFR.add(ItemFluidDrop.getAeFluidStack(items).getFluidStack());
	}else{
		cacheR.add(items.getItemStack());
		
	}
		return null;
	}
	
	HashMap<StorageChannel,IMEInventory> inv=new HashMap();
	HashMap<StorageChannel,Integer> handlerHash=new HashMap();
	public ItemStack[] mark=new ItemStack[1];
	private BaseActionSource source=new MachineSource(this);
	
	
	ForgeDirection prevDir;
public IMEInventory getInv(StorageChannel ch){
	if(prevDir!=getUp()){
		prevDir=getUp();
		  this.inv.clear();
	}
	if(getSide()==ForgeDirection.UNKNOWN)return null;
	  final TileEntity self = this;
        final TileEntity target = new BlockPos(self).getOffSet(this.getSide()).getTileEntity();
	
        final int newHandlerHash = Platform.generateTileHash(target);
        if (newHandlerHash != 0 && newHandlerHash == this.handlerHash.getOrDefault(ch, 0)) {
            return this.inv.get(ch);
        }
        
        final IExternalStorageHandler esh = AEApi.instance().registries().externalStorage()
          .getHandler(target, this.getSide().getOpposite(), ch, this.source);
  if (esh != null) {
      final IMEInventory<?> inv = esh
              .getInventory(target, this.getSide().getOpposite(), ch, this.source);
     this.inv.put(ch,inv);
     handlerHash.put(ch, newHandlerHash);
      return inv;
  }else{
	  
	  handlerHash.put(ch, 0);
  }
	return null;
	
}
	private ForgeDirection getSide() {
	
	return getUp().getOpposite();
}
/*	@SuppressWarnings("unchecked")
	public IAEItemStack complete(IAEItemStack todo){
		
		try{
			todo=(IAEItemStack) getInv(StorageChannel.ITEMS).injectItems(todo, Actionable.MODULATE, source);
			
		}catch(NullPointerException e){}
	
		
		return todo;
	}
	*/
	
	
	
	@Override
	public void jobStateChange(ICraftingLink link) {
	
		
	}
	int cd;
	int tick;
	Future<ICraftingJob> job;
	 @TileEvent(TileEventType.TICK)
	public void update(){tick++;
		 if(getWorldObj().isRemote)return;
		 
		 if(dirty){
			 dirty=false;
			 cacheR.removeIf(s->s.stackSize<=0);
		 }
		 if(dirtyF){
			 dirtyF=false;
			 cacheFR.removeIf(s->s.amount<=0);
		 }
	   boolean clear=false;
		
	  IMEInventory ch = getInv(StorageChannel.ITEMS);
	  if(ch!=null)
	   for(ItemStack todo:cacheR){
			AEItemStack itodo=(AEItemStack)ch .injectItems(AEItemStack.create(todo), Actionable.MODULATE, source);
			todo.stackSize=(int) (itodo==null?0:itodo.getStackSize());
			if(todo.stackSize==0)clear=true;
		}
		if(clear)cacheR.removeIf(s->s.stackSize==0);
		
		ch = getInv(StorageChannel.FLUIDS);
		clear=false;
		if(ch!=null)
		for(FluidStack todo:cacheFR){
			AEFluidStack itodo=(AEFluidStack) getInv(StorageChannel.FLUIDS).injectItems(AEFluidStack.create(todo), Actionable.MODULATE, source);
			todo.amount=(int) (itodo==null?0:itodo.getStackSize());
			if(todo.amount==0)clear=true;
		}
		if(clear)cacheFR.removeIf(s->s.amount==0);
		
		
		 
		 
		 
		 IAEItemStack req = null;
		 
		 if(waiting.isEmpty()==false){
			 
			
			 
			 
			 useExisting();
		 }
		  if(this.tick%40==2){
				 
				 fillStacksIntoFirstSlots(cacheR);
				 fillStacksIntoFirstSlotsF(cacheFR);
				 fillStacksIntoFirstSlots(cache);
				 fillStacksIntoFirstSlotsF(cacheF);
				 
			 }
		 if(waiting.isEmpty()==false&&job==null){
			 Entry<AEItemStack, Long> ent = waiting.entrySet().iterator().next();
			 req= ent.getKey().copy().setStackSize(ent.getValue());
		 }
		 
		 
		 
		 
		 
		 
		 
		 
		 try{
			if(last!=null){
			if(last.isDone()||last.isCanceled()){
				last=null;
				
				}
			}
			if(last==null){
				if(job==null){
					if(req!=null){
						if(cd--<=0){
					job = getProxy().getCrafting().beginCraftingJob(this.getTile().getWorldObj(), getProxy().getGrid(), new MachineSource(this), req, null);
					cd=40;
						}}
					}
				else if(job.isDone()&&!job.isCancelled()){
					last=getProxy().getCrafting().submitJob(job.get(), this, null, true, new MachineSource(this));
					job=null;
				}else if(job.isCancelled()){last=null;}
				
			}else{
				
			
			}
		 }catch(Exception e){}
		 
		 
	 }  
	 protected void fillStacksIntoFirstSlotsF( ArrayList<FluidStack>  mInventory) {
	        final int L = mInventory.size();
	        HashMap<Fluid, Integer> slots = new HashMap<>(L);
	        HashMap<Fluid, FluidStack> stacks = new HashMap<>(L);
	        List<Fluid> order = new ArrayList<>(L);
	        List<Integer> validSlots = new ArrayList<>(L);
	        for (int i = 0; i < L; i++) {
	        
	            validSlots.add(i);
	            FluidStack s = mInventory.get(i);
	            if (s == null) continue;
	            Fluid sID =s.getFluid();
	            slots.merge(sID, s.amount, Integer::sum);
	            if (!stacks.containsKey(sID)) stacks.put(sID, s);
	            order.add(sID);
	            mInventory.set(i, null);
	        }
	        int slotindex = 0;
	        for (Fluid sID : order) {
	            int toSet = slots.get(sID);
	            if (toSet == 0) continue;
	            int slot = validSlots.get(slotindex);
	            slotindex++;
	            mInventory.set( slot,stacks.get(sID).copy())
	                ;
	           // toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
	            mInventory.get(slot).amount = toSet;
	            slots.merge(sID, toSet, (a, b) -> a - b);
	        }
	        while(mInventory.size()>=1&&mInventory.get(mInventory.size()-1)==null){
	        	mInventory.remove(mInventory.size()-1);
	        }
	        
	    } 
	 protected void fillStacksIntoFirstSlots( ArrayList<ItemStack>  mInventory) {
	        final int L = mInventory.size() ;
	        HashMap<GTUtility.ItemId, Integer> slots = new HashMap<>(L);
	        HashMap<GTUtility.ItemId, ItemStack> stacks = new HashMap<>(L);
	        List<GTUtility.ItemId> order = new ArrayList<>(L);
	        List<Integer> validSlots = new ArrayList<>(L);
	        for (int i = 0; i < L; i++) {
	        
	            validSlots.add(i);
	            ItemStack s = mInventory.get(i);
	            if (s == null) continue;
	            GTUtility.ItemId sID = GTUtility.ItemId.createNoCopy(s);
	            slots.merge(sID, s.stackSize, Integer::sum);
	            if (!stacks.containsKey(sID)) stacks.put(sID, s);
	            order.add(sID);
	            mInventory.set(i, null);
	        }
	        int slotindex = 0;
	        for (GTUtility.ItemId sID : order) {
	            int toSet = slots.get(sID);
	            if (toSet == 0) continue;
	            int slot = validSlots.get(slotindex);
	            slotindex++;
	            mInventory.set( slot,stacks.get(sID).copy())
	                ;
	           // toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
	            mInventory.get(slot).stackSize = toSet;
	            slots.merge(sID, toSet, (a, b) -> a - b);
	        }
	        while(mInventory.size()>=1&&mInventory.get(mInventory.size()-1)==null){
	        	mInventory.remove(mInventory.size()-1);
	        }
	        
	    }
	private void useExisting() {
		for(Iterator<Entry<AEItemStack, Long>> itr = waiting.entrySet().iterator();itr.hasNext();){
			
			Entry<AEItemStack, Long> e=itr.next();
			
			
			IAEItemStack val = e.getKey().copy().setStackSize(e.getValue());
			
			if(val.getItem() instanceof ItemFluidDrop)
				try {
					IAEFluidStack ext = getProxy().getStorage().getFluidInventory().extractItems(ItemFluidDrop.getAeFluidStack(val), Actionable.MODULATE, source);
				if(ext!=null){
					e.setValue(e.getValue()-ext.getStackSize());
					cacheFR.add(ext.copy().getFluidStack());
					if(e.getValue()<=0){itr.remove();}
				}
				
				} catch (GridAccessException e1) {
				}
				
				
			else
				try {
					IAEItemStack ext = getProxy().getStorage().getItemInventory().extractItems(val, Actionable.MODULATE, source);
				if(ext!=null){
					e.setValue(e.getValue()-ext.getStackSize());
					cacheR.add(ext.copy().getItemStack());
					if(e.getValue()<=0){itr.remove();}
				}
				
				} catch (GridAccessException e1) {
				}
			
			
			
			
		}
		
		
		
		
		
	}
	 public boolean canBeRotated() {
		return true;
	}

	 public void setSide( ForgeDirection axis) {
	        if (Platform.isClient()) {
	            return;
	        }//axis=axis.getOpposite();
	        ForgeDirection pointAt = getUp().getOpposite();
	        if (pointAt == axis.getOpposite()) {
	            pointAt = axis;
	        } else if (pointAt == axis || pointAt == axis.getOpposite()) {
	            pointAt = ForgeDirection.UNKNOWN;
	        } else if (pointAt == ForgeDirection.UNKNOWN) {
	            pointAt = axis.getOpposite();
	        } else {
	            pointAt = Platform.rotateAround(pointAt, axis);
	        }

	        if (ForgeDirection.UNKNOWN == pointAt) {
	            this.setOrientation(pointAt, pointAt);
	        } else {
	            this.setOrientation(
	                    pointAt.offsetY != 0 ? ForgeDirection.SOUTH : ForgeDirection.UP,
	                    pointAt.getOpposite());
	        }

	        this.markForUpdate();
	        this.markDirty();
	    }
	 /*   ForgeDirection getForward() {
			return null;
		}

	
	    ForgeDirection getUp() {
			return null;
		}*/

	
	 public void setOrientation(ForgeDirection Forward, ForgeDirection Up) {
	    	super.setOrientation(Forward, Up); 
	    	ForgeDirection pointAt = Up.getOpposite();
	        this.getProxy().setValidSides(EnumSet.complementOf(EnumSet.of(pointAt)));
		}
	@Override
	public int getSizeInventory() {
		
		return cacheR.size()+1;
	}
	@Override
	public ItemStack getStackInSlot(int slotIn) {
		if(slotIn==cacheR.size())return null;
		dirty=true;
		return cacheR.get(slotIn);
	}
	@Override
	public ItemStack decrStackSize(int index, int count) {
		if(index==cacheR.size())return null;
		dirty=true;
		if(cacheR.get(index)==null){return null;}
		cacheR.get(index).stackSize-=count;
		ItemStack ret = cacheR.get(index).copy();
		ret.stackSize=count;
		return 	ret;
	}
	@Override
	public ItemStack getStackInSlotOnClosing(int index) {
		return null;
	}
	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if(index==cacheR.size())return ;
		dirty=true;
		if(stack==null||stack.stackSize==0){
			cacheR.get(index).stackSize=0;
		}else{
			cacheR.set(index,stack);
		}
		
	
	}
	@Override
	public String getInventoryName() {
		
		return "";
	}
	@Override
	public boolean hasCustomInventoryName() {
	
		return false;
	}
	@Override
	public int getInventoryStackLimit() {
		
		return Integer.MAX_VALUE;
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
	
		return false;
	}
	@Override
	public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
	//	ForgeDirection pointAt =getUp().getOpposite();
		//if(pointAt.ordinal()==p_94128_1_){
			return IntStream.range(0, cacheR.size()+1).toArray();
//}
	//	return new int[0];
	}
	@Override
	public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
		
		return false;
	}
	@Override
	public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_) {
		
		return true;
	}
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
		
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
		int all=cacheFR.stream().filter(s->s.getFluid()==resource.getFluid())
		.mapToInt(s->s.amount).sum();
		if(doDrain){
			dirtyF=true;
			int[] todo=new int[]{all};
			cacheFR.stream().filter(s->s.getFluid()==resource.getFluid()).forEach(s->{
				int real=Math.min(todo[0], s.amount);
				s.amount-=real;
				todo[0]-=real;
			});	
			
		}
		
		FluidStack cp = resource.copy();
		cp.amount=all;
		if(cp.amount==0)cp=null;
		return cp;
	}
	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
	if(cacheFR.size()==0){return null;}
	FluidStack cp = cacheFR.get(0).copy();
	cp.amount=maxDrain;
		return drain(from,cp , doDrain);
	}
	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid) {
		
		return false;
	}
	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid) {
		
		return true;
	}
	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from) {
		
		return cacheFR.stream().map(s->new FluidTank(s, 1)).toArray(FluidTankInfo[]::new);
	}
	boolean dirty,dirtyF;
}
