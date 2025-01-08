package reobf.proghatches.ae;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;

import com.glodblock.github.inventory.MEMonitorIFluidHandler;
import com.glodblock.github.util.BlockPos;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.shapes.Rectangle;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Settings;
import appeng.api.config.StorageFilter;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IExternalStorageHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.IConfigManager;
import appeng.client.texture.CableBusTextures;
import appeng.core.Api;
import appeng.core.api.definitions.ApiItems;
import appeng.crafting.CraftingLink;
import appeng.items.tools.ToolMemoryCard;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEMonitorIInventory;
import appeng.parts.AEBasePart;
import appeng.parts.PartBasicState;
import appeng.parts.p2p.PartP2PRedstone;
import appeng.parts.p2p.PartP2PTunnel;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.PrecisePriorityList;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.util.GTUtil;
import gregtech.api.util.GTUtility;
import mrtjp.projectred.core.ItemPart;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.eucrafting.EUUtil;
import reobf.proghatches.eucrafting.IGuiProvidingPart;
import reobf.proghatches.gt.metatileentity.util.polyfill.NumericWidget;
import reobf.proghatches.util.ProghatchesUtil;

public class PartAmountMaintainer  extends PartBasicState implements IGuiProvidingPart,IGridTickable,IPowerChannelState, ICraftingRequester{

	 @Override
	public IConfigManager getConfigManager() {
		
		return new IConfigManager() {
			
			@Override
			public void writeToNBT(NBTTagCompound data) {
				data.setLong("freq", freq);
				data.setInteger("mode", mode);
				data.setInteger("rsmode", rsmode);
				data.setInteger("redstone", redstone);
				data.setLong("amount", amount);
				//data.setBoolean("lastredstone", lastredstone);
				if(mark[0]!=null)data.setTag("mark", mark[0].writeToNBT(new NBTTagCompound()));
				//if(upgrade[0]!=null)data.setTag("upgrade", upgrade[0].writeToNBT(new NBTTagCompound()));
				//if(upgrade[1]!=null)data.setTag("upgrade1", upgrade[1].writeToNBT(new NBTTagCompound()));
				
				
				
			}
			
			@Override
			public void registerSetting(Settings settingName, Enum<?> defaultValue) {
			
				
			}
			
			@Override
			public void readFromNBT(NBTTagCompound data) {
				freq=data.getLong("freq");
				mode=data.getInteger("mode");
				rsmode=data.getInteger("rsmode");
				redstone=data.getInteger("redstone");
				amount=data.getLong("amount");
				//lastredstone=data.getBoolean("lastredstone" );
				if(data.getCompoundTag("mark").hasNoTags()==false)
				mark[0]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("mark"));
				//upgrade[0]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("upgrade"));
				//upgrade[1]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("upgrade1"));
			
				
				
				
				
			}
			
			@Override
			public Enum<?> putSetting(Settings settingName, Enum<?> newValue) {
			
				return null;
			}
			
			@Override
			public Set<Settings> getSettings() {
				
				return null;
			}
			
			@Override
			public Enum<?> getSetting(Settings settingName) {
			
				return null;
			}
		};
	}
	
	
	
	
	private int mode;
	private int rsmode;
	
	//bit0 ->when offline,clear or maintain
	//bit1 ->when online,invert redstone
	private int redstone;
	
	public PartAmountMaintainer(ItemStack is) {
		super(is);
		 this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		
		return new TickingRequest(1,1,false,false);
	}
	
	//AEItemStack empty=AEItemStack.create(new ItemStack(Items.apple,0));
	//AEFluidStack emptyf=AEFluidStack.create(new FluidStack(FluidRegistry.WATER,0));
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static private Map<StorageChannel,IAEStack> EMPTY=(Map<StorageChannel, IAEStack>)(Object)ImmutableMap.of(
			StorageChannel.FLUIDS,AEFluidStack.create(new FluidStack(FluidRegistry.WATER,0)),
			StorageChannel.ITEMS,AEItemStack.create(new ItemStack(Items.apple,0))
			);
	
	boolean lastredstone;
	
	public boolean shouldProceed(boolean red, boolean lastredstone){
	switch (rsmode) {
	case 0:return true;
	case 1:return false;
	case 2:return red;
	case 3:return !red;
	case 4:return red&&(!lastredstone);
	case 5:return (!red)&&lastredstone;
	
	}	
		
		
	return true;}
	
	
	Future<ICraftingJob>  job;
	ICraftingLink link;
	int reqcooldown;
	public void requestForMissing(IAEStack primitive){
		if(upgrade[1]==null)return;
		
	IAEItemStack iaeStack=(primitive instanceof IAEItemStack)?(IAEItemStack) primitive:
		ItemFluidDrop.newAeStack((AEFluidStack) primitive);
		
		try {
		if(link==null){
			if(job==null){
			if(reqcooldown>0){reqcooldown--;return;}
			reqcooldown=80;
			job=getProxy().getCrafting().beginCraftingJob(this.getTile().getWorldObj(), getProxy().getGrid(), source, iaeStack, null);
			}
			else{
				if(job.isDone()){
				link = getProxy().getCrafting().submitJob(job.get(), this, null, false, source);
				job=null;}
				if(job.isCancelled()){job=null;}
			}
		}else{
			if(link.isCanceled()||link.isDone()){link=null;}
			
		}
	
	
	} catch (Exception e1) {}
	}
	
	
	
	@SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
	      
      //  validateLink();
		
		if(upgrade[0]==null)rsmode=0;
		boolean red=this.getHost().hasRedstone(this.getSide());
		boolean should=shouldProceed(red,lastredstone);
		lastredstone=red;
		if(!should){
			handlerHash.clear();
			inv.clear();
			return TickRateModulation.SAME;}
		if(getProxy().isActive()==false)return TickRateModulation.SAME;
		for(StorageChannel ch:new StorageChannel[]{StorageChannel.FLUIDS,StorageChannel.ITEMS})
		{
		IMEInventory inv = getInv(ch);
		if(inv!=null){
			
			IItemList list;
			if(inv instanceof MEMonitorIFluidHandler){
			//	inv.injectItems(EMPTY.get(ch),  Actionable.MODULATE, source);//dirty hack, trigger update
				// cache is invalid! just use the deprecated way to get the inv
				((MEMonitorIFluidHandler) inv).onTick();
				list = ((MEMonitorIFluidHandler) inv).getAvailableItems(ch.createList());
				
			}else if(inv instanceof MEMonitorIInventory){
				((MEMonitorIInventory) inv).onTick();
				list = ((MEMonitorIInventory) inv).getAvailableItems(ch.createList());
			
			}
			else{
				list=inv.getAvailableItems(ch.createList());
			}
			
			end:{
				IAEStack opt=maybe(ch);
				if(opt==null)break end;
				IAEStack  is=	 list.findPrecise(
						//AEItemStack.create(new ItemStack(Items.apple))
						opt
						);
				
				long amount;
				if(is==null){
					amount=0;
					is=opt.copy();
					is.setStackSize(0);
				}else{
					amount=is.getStackSize();
				}
			
				
				
				long expected=opt.getStackSize();
				sc:if(amount>expected){
					try{
					IAEStack take = inv.extractItems(is.copy().setStackSize(amount-expected), Actionable.SIMULATE, source);
					if(take==null){break sc;}
					IAEStack notadd =getStorage(getProxy().getStorage(),ch).injectItems( take, Actionable.SIMULATE, source);
					if(notadd==null||notadd.getStackSize()==0){
						IAEStack realtake = inv.extractItems(is.copy().setStackSize(amount-expected), Actionable.MODULATE, source);
						/*IAEStack notadd =*/getStorage(getProxy().getStorage(),ch).injectItems( realtake, Actionable.MODULATE, source);
					}
					
					}catch(GridAccessException e){}
				}
				sc:if(amount<expected){
					try{
						IAEStack take = getStorage(getProxy().getStorage(),ch).extractItems(is.copy().setStackSize(-amount+expected), Actionable.SIMULATE, source);
						long missing=-amount+expected-(take==null?0:take.getStackSize());
						if(missing>0)requestForMissing(is.copy().setStackSize(missing));
						if(take==null){break sc;}//ae2fc fluid inv disallow injecting null
						IAEStack notadd =inv.injectItems( take, Actionable.SIMULATE, source);
						if(notadd==null||notadd.getStackSize()==0){
							IAEStack realtake = getStorage(getProxy().getStorage(),ch).extractItems(is.copy().setStackSize(-amount+expected), Actionable.MODULATE, source);
							/*IAEStack notadd =*/inv.injectItems( realtake, Actionable.MODULATE, source);
						}
						
						}catch(GridAccessException e){}
				}	
			}		
				
				
				
				
				
				
				
				
				
			
			
		/*list.forEach(s->{
		System.out.println(s);
			
		});*/
		
			
			
			
			
		
		}
		
		
		}
		
		
		
		return TickRateModulation.SAME;
	}
	
	
	 @Override
	    public void getBoxes(final IPartCollisionHelper bch) {
	        bch.addBox(2, 2, 14, 14, 14, 16);
	        bch.addBox(5, 5, 12, 11, 11, 14);
	    }  @Override
	    @SideOnly(Side.CLIENT)
	    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
	        rh.setTexture(
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorBack.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon());

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderInventoryBox(renderer);

	        rh.setBounds(5, 5, 12, 11, 11, 13);
	        rh.renderInventoryBox(renderer);

	        rh.setBounds(5, 5, 13, 11, 11, 14);
	        rh.renderInventoryBox(renderer);
	    } @Override
	    @SideOnly(Side.CLIENT)
	    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
	            final RenderBlocks renderer) {
	        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
	        rh.setTexture(
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorBack.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon());

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setTexture(
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorBack.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartMonitorSides.getIcon(),
	                CableBusTextures.PartMonitorSides.getIcon());

	        rh.setBounds(5, 5, 12, 11, 11, 13);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setTexture(
	                CableBusTextures.PartMonitorSidesStatus.getIcon(),
	                CableBusTextures.PartMonitorSidesStatus.getIcon(),
	                CableBusTextures.PartMonitorBack.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartMonitorSidesStatus.getIcon(),
	                CableBusTextures.PartMonitorSidesStatus.getIcon());

	        rh.setBounds(5, 5, 13, 11, 11, 14);
	        rh.renderBlock(x, y, z, renderer);

	        //this.renderLights(x, y, z, rh, renderer);
	    }
		
	    
	    
	    
	    @SuppressWarnings("deprecation")
		@Override
		public boolean onPartActivate(EntityPlayer player, Vec3 pos) {
/*if(player.getHeldItem()!=null&&player.getHeldItem().getItem()instanceof ToolMemoryCard){
	
	NBTTagCompound a = ((ToolMemoryCard)player.getHeldItem().getItem()).getData(player.getHeldItem());
    if (a.hasKey("freq")) {
        final long freq = a.getLong("freq");
        final ItemStack newType = ItemStack.loadItemStackFromNBT(a);

		if (newType != null) {
			
				if(Api.INSTANCE.definitions().parts().p2PTunnelRedstone().isSameAs(newType)){
					
					System.out.println(freq);
					
					
					
				};
				
			
		}
    }
	
}*/
			// System.out.println(this.getTile().getWorldObj().isRemote);
			if (player.isSneaking())
				return false;
			TileEntity t = this.getTile();
			// System.out.println(getSide());
			EUUtil.open(player, player.getEntityWorld(), t.xCoord, t.yCoord, t.zCoord, getSide());
			//System.out.println(player.getHeldItem());
			return true;
		}

		
		@Override
		public ModularWindow createWindow(UIBuildContext buildContext) {
			ModularWindow.Builder builder = ModularWindow.builder(176, 107+20);
			
			
			builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
			builder.bindPlayerInventory(buildContext.getPlayer());
		
			   String freqTooltip = String.format("%X", freq).replaceAll("(.{4})", "$0 ").trim();
			ItemStackHandler is=new ItemStackHandler();
			
			if(freq!=0){
			ItemStack stack = Api.INSTANCE.definitions().parts().p2PTunnelRedstone().maybeStack(1).get();
			
			stack.setStackDisplayName("freq:"+freqTooltip);
			is.setStackInSlot(0, stack);
			}
			
			
			
			
			
			
			
			builder.widget( TextWidget.dynamicString(()->{
				try{
				PartP2PTunnel p2p =freq==0?null: getProxy().getP2P().getInput(freq);
			if(p2p.isActive()&&p2p.isPowered()){
				return StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.online");
			}}catch(Exception e){}
			
			
			return StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.offline");
				
			}).setPos(30,10));
			builder.widget( new SlotWidget(new BaseSlot(is, 0,true)){
				@Override
				protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
					if(cursorStack==null){
						freq=0;
						is.setStackInSlot(0, null);
					}
					if(cursorStack!=null&&cursorStack.getItem()instanceof ToolMemoryCard){
						
						NBTTagCompound a = ((ToolMemoryCard)cursorStack.getItem()).getData(cursorStack);
					    if (a.hasKey("freq")) {
					        final long freqx = a.getLong("freq");
					        final ItemStack newType = ItemStack.loadItemStackFromNBT(a);

							if (newType != null) {
								
									if(Api.INSTANCE.definitions().parts().p2PTunnelRedstone().isSameAs(newType)){
										
										
										freq=freqx;
										String freqTooltip = String.format("%X", freq).replaceAll("(.{4})", "$0 ").trim();
										ItemStack stack = Api.INSTANCE.definitions().parts().p2PTunnelRedstone().maybeStack(1).get();
										stack.setStackDisplayName("freq:"+freqTooltip);
										is.setStackInSlot(0, stack);
										
										
									};
									
								
							}
					    }}
					
					
					
				}
				
				
			}.disableShiftInsert().setPos(3+4, 3)
					.addTooltip( StatCollector.translateToLocal("proghatches.amountmaintainer.memorycard"))
			           );
			
			
			
			
			
			ItemStackHandler iss=new ItemStackHandler(mark);
			builder.widget( new SlotWidget(new BaseSlot(iss, 0,true)){
				@Override
				protected void phantomClick(ClickData clickData, ItemStack cursorStack) {
					if(cursorStack==null){
						mark[0]=null;
						
					}else{
						ItemStack fis = mode==0?null:tryConvertToFluid(cursorStack);
						
						mark[0]=fis==null?cursorStack:fis;
						mark[0]=mark[0].copy();
						mark[0].stackSize=1;
					}
		
				}
				
				
			}.setPos(60, 3).addTooltip(StatCollector.translateToLocal("proghatches.amountmaintainer.phantomslot")));
			
			
			builder.widget(
	                new NumericWidget().setSetter(val -> amount =  (long) val)
	                    .setGetter(() -> amount)
	                    .setBounds(1, 9_007_199_254_740_991D)
	                    .setScrollValues(1, 4, 64)
	                    .setTextAlignment(Alignment.Center)
	                    .setTextColor(Color.WHITE.normal)
	                    .setSize(60, 18)
	                    .setPos(60+18, 3)
	                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD)
	                    .addTooltips(Arrays.asList(
	                    		StatCollector.translateToLocal("proghatches.amountmaintainer.amount.0"),
	                    		StatCollector.translateToLocal("proghatches.amountmaintainer.amount.1"),
	                    		StatCollector.translateToLocal("proghatches.amountmaintainer.amount.2"),
	                    		StatCollector.translateToLocal("proghatches.amountmaintainer.amount.3"))
	                    		
	                    		)
					);
			
			builder.widget(new CycleButtonWidget().setGetter(()->mode)
					.setSetter(s->mode=s).setLength(2)
	           .setTextureGetter(s->{
	        	   if(s==0)return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ITEM;
	        	   if(s==1)return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_FLUID;
	        			   return GTUITextures.OVERLAY_BUTTON_VOID_EXCESS_ALL;
	           })
	           .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.0"))
	           .addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.phantomclick.mode.1"))
					.setBackground(() -> {
	               {
	                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
	                       };
	                }
	            })
	            
	            .setSize(18, 18)
	            .setPos(120+20, 3));
			
			
			builder.widget(new CycleButtonWidget().setGetter(()->redstone)
					.setSetter(s->redstone=s).setLength(4)
	           .setTextureGetter(s->{
	        	
	        			   return GTUITextures.OVERLAY_BUTTON_REDSTONE_ON;
	           })
	           .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.mode.0"))
	           .addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.mode.1"))
	           .addTooltip(2, StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.mode.2"))
	           .addTooltip(3, StatCollector.translateToLocal("proghatches.amountmaintainer.redstone.mode.3"))
	           
					.setBackground(() -> {
	               {
	                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
	                       };
	                }
	            })
	            
	            .setSize(18, 18)
	            .setPos(3+4, 3+18));
			
			
			builder.widget(new DrawableWidget().setDrawable(GTUITextures.OVERLAY_BUTTON_REDSTONE_ON).setPos(3+4+20, 3+18).setSize(18,18).setEnabled(s->{return on;})
					.addTooltip(StatCollector.translateToLocalFormatted("proghatches.amountmaintainer.redstone.state.on",amount))
					)
			;
			builder.widget(new DrawableWidget().setDrawable(GTUITextures.OVERLAY_BUTTON_REDSTONE_OFF).setPos(3+4+20, 3+18).setSize(18,18).setEnabled(s->!on)
					.addTooltip(StatCollector.translateToLocalFormatted("proghatches.amountmaintainer.redstone.state.off"))
					)
			;
			builder.widget(new FakeSyncWidget.BooleanSyncer(()->{on=isOn();  return on;}, s->on=s)
					.setSynced(true, false)
					);
			
			
			ItemStackHandler iss0=new ItemStackHandler(upgrade){
				
				public boolean isItemValid(int slot, ItemStack stack) {
				return	Api.INSTANCE.definitions().materials().cardRedstone().isSameAs(stack);
					
				};
			public int getSlotLimit(int slot) {
				return 1;};
			};
			
			builder.widget( new SlotWidget(new BaseSlot(iss0, 0)){
				
			
			}
			.setPos(60, 3+20).addTooltip(StatCollector.translateToLocal("proghatches.amountmaintainer.rscard")));
			
		
			builder.widget(new CycleButtonWidget().setGetter(()->rsmode)
					.setSetter(s->rsmode=s).setLength(6)
	           .setTextureGetter(s->{
	        	   if(s==0)return new ItemDrawable(new ItemStack(Items.redstone));
	        	   if(s==1)return new ItemDrawable(new ItemStack(Items.gunpowder));
	        	   if(s==2)return GTUITextures.OVERLAY_BUTTON_REDSTONE_ON;
	        	   if(s==3)return GTUITextures.OVERLAY_BUTTON_REDSTONE_OFF;
	        	   if(s==4)return GTUITextures.OVERLAY_BUTTON_ARROW_GREEN_UP;
	        	   return GTUITextures.OVERLAY_BUTTON_ARROW_GREEN_DOWN;
	           })
	           .addTooltip(0, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.0"))
	           .addTooltip(1, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.1"))
	           .addTooltip(2, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.2"))
	           .addTooltip(3, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.3"))
	           .addTooltip(4, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.4"))
	           .addTooltip(5, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.5"))
	           
	           
	           
					.setBackground(() -> {
	               {
	                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
	                       };
	                }
	            })
	            .setEnabled((a)->(upgrade[0]!=null))
	            .setSize(18, 18)
	            .setPos(60+20, 3+20));
	
			ItemStackHandler iss1=new ItemStackHandler(upgrade){
				
				public boolean isItemValid(int slot, ItemStack stack) {
				return	Api.INSTANCE.definitions().materials().cardCrafting().isSameAs(stack);
					
				};
			public int getSlotLimit(int slot) {
				return 1;};
			};
			
			builder.widget( new SlotWidget(new BaseSlot(iss1, 1)){
				
			
			}
			.setPos(60+40, 3+20).addTooltip(StatCollector.translateToLocal("proghatches.amountmaintainer.craftcard")));
			
			
			
			return builder.build();
		}
		private boolean on;
		static Field f ;
		static{try {
			f=PartP2PRedstone.class.getDeclaredField("power");
		f.setAccessible(true);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}}
		
		public Boolean getSignal(){
			
			try {
				PartP2PTunnel p2p =freq==0?null: getProxy().getP2P().getInput(freq);
			if(p2p!=null){
				if(p2p.isActive()&&p2p.isPowered()){
				
					
					return f.getInt(((PartP2PRedstone)p2p))>0;
					
				}
				
			}
			
			} catch (GridAccessException e) {
				
			}
			
			 catch (Exception e) {e.printStackTrace();
		
			}
				return null;
			
			
		}
		long freq;
		private BaseActionSource source=new MachineSource(this);
		ItemStack[] upgrade=new ItemStack[2];
		private boolean danglingLink;
		
		@Override
		public void readFromNBT(NBTTagCompound data) {
			freq=data.getLong("freq");
			mode=data.getInteger("mode");
			rsmode=data.getInteger("rsmode");
			redstone=data.getInteger("redstone");
			amount=data.getLong("amount");
			lastredstone=data.getBoolean("lastredstone" );
			mark[0]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("mark"));
			
			exit:{
			FluidStack fs = GTUtility.getFluidFromDisplayStack(mark[0]);
			if(fs==null){break exit;}
			String name=   data.getString("fluid_ID_string");
			if(name.isEmpty()){break exit;}
			Fluid f=FluidRegistry.getFluid(name);
			if(f==null){break exit;}
			if(f==fs.getFluid()){break exit;}
			fs=new FluidStack(f, fs.amount);
			}
			
			upgrade[0]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("upgrade"));
			upgrade[1]=ItemStack.loadItemStackFromNBT(data.getCompoundTag("upgrade1"));
			super.readFromNBT(data);
			NBTTagCompound tag=data.getCompoundTag("link");
			
			if(tag.hasNoTags()==false){
					tag.setBoolean("req", true);
					link=new CraftingLink(tag,this);
			    	danglingLink=true;
			    }
		}
		@Override
		public void writeToNBT(NBTTagCompound data) {
			data.setLong("freq", freq);
			data.setInteger("mode", mode);
			data.setInteger("rsmode", rsmode);
			data.setInteger("redstone", redstone);
			data.setLong("amount", amount);
			data.setBoolean("lastredstone", lastredstone);
			if(mark[0]!=null){
				
				data.setTag("mark", mark[0].writeToNBT(new NBTTagCompound()));
				FluidStack fs = GTUtility.getFluidFromDisplayStack(mark[0]);
			   if(fs!=null){String name=FluidRegistry.getFluidName(fs);
			   data.setString("fluid_ID_string", name);}
			   
			}
			if(upgrade[0]!=null)data.setTag("upgrade", upgrade[0].writeToNBT(new NBTTagCompound()));
			if(upgrade[1]!=null)data.setTag("upgrade1", upgrade[1].writeToNBT(new NBTTagCompound()));
			super.writeToNBT(data);
			if(link!=null){
		    	NBTTagCompound tag=new NBTTagCompound();
		    	link.writeToNBT(tag);
		    	data.setTag("link", tag);
		    }
		}
		long amount=64;
		
		HashMap<StorageChannel,IMEInventory> inv=new HashMap();
		HashMap<StorageChannel,Integer> handlerHash=new HashMap();
		public ItemStack[] mark=new ItemStack[1];
	public IMEInventory getInv(StorageChannel ch){
		  final TileEntity self = this.getHost().getTile();
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
	
	public IAEStack maybe(StorageChannel c){
		
		
		
		if(c==StorageChannel.ITEMS)return maybeItem();
		return maybeFluid();
	}

	
	public AEItemStack maybeItem(){
		if(mark[0]==null)return null;
		
		FluidStack fs = GTUtility.getFluidFromDisplayStack(mark[0]);
		if(fs==null){
			AEItemStack is=AEItemStack.create(mark[0]);
			is.setStackSize(requestedAmount());
		return is;
		}
		return null;
	}
	
	
	public long requestedAmount(){return isOn()?amount:0;}
	public boolean isOn(){
		
		Boolean b=getSignal();
		if(b==null){return (redstone&0b1)==0;}
		return ((redstone&0b10)!=0)^b;
	}
	
	
	
	public AEFluidStack maybeFluid(){
		if(mark[0]==null)return null;
		FluidStack fs = GTUtility.getFluidFromDisplayStack(mark[0]);
		if(fs!=null){
			AEFluidStack is=AEFluidStack.create(fs);
			is.setStackSize(requestedAmount());
		return is;
		}
		return null;
	}
	private ItemStack tryConvertToFluid(ItemStack is){
		
		FluidStack fs = GTUtility.getFluidForFilledItem(is, true);
		if(fs!=null){
		return GTUtility.getFluidDisplayStack(fs, false);
		}
		
		return null;
	}
	private IMEMonitor getStorage(IStorageGrid g,StorageChannel c){
		if(c==StorageChannel.ITEMS)return g.getItemInventory();
		return g.getFluidInventory();
		
		
	}

	   @Override
	    public void getDrops(final List<ItemStack> drops, final boolean wrenched) {
		  if(upgrade[0]!=null)
		   drops.add(upgrade[0]);
		  if(upgrade[1]!=null)
			   drops.add(upgrade[1]);
	    }

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
	
		return link==null?ImmutableSet.of():ImmutableSet.of(link);
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
	
		return items;
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
	
		
	}

public void validateLink(){
	try {
	if(link!=null)
	if(danglingLink)
	{
		 getProxy().getCrafting().getCpus().stream().filter(s->{
			if(s instanceof CraftingCPUCluster){
				CraftingCPUCluster c=(CraftingCPUCluster) s;
				boolean b=c.isBusy();
				if(b){
				
					if(Objects.equals(c.getLastCraftingLink().getCraftingID(),link.getCraftingID())){
						link=c.getLastCraftingLink();
						danglingLink=false;
					return true;
				    }
				}
			}
			return false;
		}).findFirst().orElse(null);
	}
		
			
	} catch (GridAccessException e) {
		
		e.printStackTrace();
	}

	
	
}
/*
@Override
public Set<Settings> getSettings() {
	
	return null;
}

@Override
public void registerSetting(Settings settingName, Enum<?> defaultValue) {
	
	
}

@Override
public Enum<?> getSetting(Settings settingName) {
	
	return null;
}

@Override
public Enum<?> putSetting(Settings settingName, Enum<?> newValue) {
	
	return null;
}*/
}
