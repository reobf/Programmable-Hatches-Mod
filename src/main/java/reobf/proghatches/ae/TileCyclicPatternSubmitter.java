package reobf.proghatches.ae;


import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.crafting.CraftingLink;
import appeng.crafting.CraftingLinkNexus;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;



public class TileCyclicPatternSubmitter extends TileEntity implements IGridProxyable, ICraftingRequester, ITileWithModularUI{
	public static final int ALL = 0;
	public static final int DIM = 1;
	public static final int OWNER = 2;
	int SLOT_SIZE=32;
	ItemStack[] inv=new ItemStack[SLOT_SIZE];
	/**
	 * 
	 */
	public static IWailaDataProvider provider=new IWailaDataProvider(){

		@Override
		public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
			
			return null;
		}

		@Override
		public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
		
			return currenttip;
		}

		@Override
		public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
			
		
			
			return currenttip;
		}

		@Override
		public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {
		
			return currenttip;
		}

		@Override
		public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity t, NBTTagCompound tag, World world, int x,
				int y, int z) {
			/*TileCyclicPatternSubmitter te=(TileCyclicPatternSubmitter) t;
			tag.setBoolean("", te.task==null);
		*/
			
			return tag;
		}};
	
	
	
	
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
	
		return createProxy().getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
	
	}

	@Override
	public AENetworkProxy getProxy() {
		
		return createProxy();
	}
	AENetworkProxy proxy;
    protected AENetworkProxy createProxy() {
    	if(proxy!=null)return proxy;
    	
    	proxy=new AENetworkProxy(this, "proxy", new ItemStack(MyMod.alert), true);
    	
    	proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
    	proxy.setValidSides(EnumSet.range(ForgeDirection.DOWN, ForgeDirection.EAST));
        return proxy; }
	@Override
	public DimensionalCoord getLocation() {
	
		return new DimensionalCoord(this);
	}

	@Override
	public void gridChanged() {
		
		
	}
	UUID owner;
    
    
	public void mark(EntityPlayer placer) {
	createProxy().setOwner((EntityPlayer) placer);
	owner=placer.getUniqueID();
	}
	
	boolean danglingLink;
	@Override
	public void readFromNBT(NBTTagCompound compound) {	
		//mode=compound.getInteger("m");
		owner=ProghatchesUtil.deser(compound, "OWNER_UUID");
		if(owner.getLeastSignificantBits()==0&&owner.getMostSignificantBits()==0)owner=null;
		createProxy().readFromNBT(compound);
		
		NBTTagCompound tag=compound.getCompoundTag("link");
		
		if(tag.hasNoTags()==false){
				tag.setBoolean("req", true);
		    	last=new CraftingLink(tag, this);
		    	danglingLink=true;
		    }
		 for(int i=0;i<inv.length;++i)
		 if(compound.hasKey("inv"+i))
			 inv[i]=ItemStack.loadItemStackFromNBT((NBTTagCompound) compound.getTag("inv"+i));
		 else
			 inv[i]=null;
		 asManyAsPossible = compound.getBoolean("asManyAsPossible");
		 submitfail = compound.getBoolean("submitfail");
		 index=compound.getInteger("index");
		 abortingMode =compound.getBoolean("abortingMode");
		 on =compound.getBoolean("on");
		 lastredstone =compound.getBoolean("lastredstone"); 
		 forceForward=compound.getBoolean("forceForward" );
		 skipIfFail=compound.getBoolean("skipIfFail" );
		 rsmode=compound.getInteger("rsmode");
		upgrade[0]=ItemStack.loadItemStackFromNBT(compound.getCompoundTag("upgrade"));
			
		super.readFromNBT(compound);
	}
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		//compound.setInteger("m", mode);
		if(owner!=null)
		ProghatchesUtil.ser(compound, owner, "OWNER_UUID");
	    createProxy().writeToNBT(compound);
	    
	    if(last!=null){
	    	NBTTagCompound tag=new NBTTagCompound();
	    	last.writeToNBT(tag);
	    	compound.setTag("link", tag);
	    }
	   for(int i=0;i<inv.length;++i)
	    if(inv[i]!=null)compound.setTag("inv"+i, inv[i].writeToNBT(new NBTTagCompound()));
	   compound.setInteger("index", index);
	   compound.setBoolean("abortingMode", abortingMode);
	   compound.setBoolean("on", on);
	   compound.setBoolean("asManyAsPossible",asManyAsPossible);
	   compound.setBoolean("submitfail",submitfail);
	   compound.setBoolean("lastredstone", lastredstone);
	   compound.setBoolean("forceForward", forceForward);
	   compound.setBoolean("skipIfFail", skipIfFail);
	   compound.setInteger("rsmode", rsmode);
	   if(upgrade[0]!=null)compound.setTag("upgrade", upgrade[0].writeToNBT(new NBTTagCompound()));
		
	   super.writeToNBT(compound);
	}
	MachineSource source=new  MachineSource(this);
	
	ICraftingLink  last;
	boolean abortingMode;
	boolean submitfail;

int state;
public int state(){
	if(submitfail)return 1;
	if(last!=null&&(!last.isCanceled())&&(!last.isDone()))return 2;
	return 0;
}
boolean asManyAsPossible;
boolean skipIfFail;
private boolean forceForward;
int cd;
int cdmax;
@Override
public void updateEntity() {
	ticksSinceLoaded++;
	cd++;
	if(upgrade[0]==null)rsmode=0;
	boolean red=this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
	boolean should=shouldProceed(red,lastredstone);
	lastredstone=red;
	if(!should){return ;}
	
	
	
	super.updateEntity();
	if(!getProxy().isReady())
	getProxy().onReady();
	
	if(!getWorldObj().isRemote&&ticksSinceLoaded%20==12){
		
		int old = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 
				((this.getProxy().isActive()&&this.getProxy().isPowered())
				?0b1000:0)|(old&0b111)
				, 6);
		
	}
	
	
	if(!getWorldObj().isRemote&&(
			ticksSinceLoaded%20==2
			)&&on&&(this.getProxy().isActive()&&this.getProxy().isPowered()))
	end:try {CraftingCPUCluster cpu=null;
	
		if(last!=null){
		if(last.isDone()||last.isCanceled()){
			last=null;
			index=(index+1);
			index=index%SLOT_SIZE;
			updateComparator();
			}else{
		cpu = getCpu(last);}
		}
		if(last==null){
				int i=0;
				if(forceForward){
				i++;index++;
				forceForward=false;
				}
				while(inv[index]==null&&i<SLOT_SIZE){
					i++;
					index=(index+1)%SLOT_SIZE;}
				
			ICraftingPatternDetails pat=null;
			if(inv[index] !=null&&inv[index].getItem() instanceof ICraftingPatternItem ){
				pat=((ICraftingPatternItem )inv[index].getItem()).getPatternForItem(inv[index], worldObj);
			}
			
			if(pat==null)break end;
			PatternCraftingJob job=new PatternCraftingJob(pat,getProxy().getStorage());
			
			
			int howmany;
			if((howmany=job.canBeDone(getProxy(), source))>0){
				if(asManyAsPossible){
					job.times=howmany;
				}
				last=getProxy().getCrafting().submitJob(job, this, null, true, source);
				
			}
			submitfail=last==null;
			if(skipIfFail)forceForward=submitfail;
		}else{
			
			if(abortingMode){
				if(last.isDone()==false){
				if(cpu!=null){
					try {
						Map c=(Map) task.get(cpu);
					if(c.isEmpty()){
						cpu.cancel();
						//last.cancel();
						
					}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				}
			}
		}
	
	
	
	} catch (GridAccessException e) {
		
		e.printStackTrace();
	};

	
	
	
	
	
}
private void updateComparator() {
	this.getWorldObj().notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
	
}
Field task;
{
	
	try {
		 task=	CraftingCPUCluster.class.getDeclaredField("tasks");
		task.setAccessible(true);
	} catch (Exception e) {
	
		e.printStackTrace();
	}


}
static Field nexus;
static Field cpuside;
static Field cpu;
static{try {
	nexus=CraftingLink.class.getDeclaredField("tie");
	nexus.setAccessible(true);
	cpuside=CraftingLinkNexus.class.getDeclaredField("cpu");
	cpuside.setAccessible(true);
	cpu=CraftingLink.class.getDeclaredField("cpu");
	cpu.setAccessible(true);
} catch (Exception e) {
	e.printStackTrace();
}}
static boolean broken;
public CraftingCPUCluster getCpu(ICraftingLink cid){
	try {
		end:if(broken==false)
		try {
			CraftingLinkNexus nex=(CraftingLinkNexus) nexus.get(cid);
			if(nex==null)break end;
			ICraftingLink other=(ICraftingLink) cpuside.get(nex);
			if(other==null)break end;
			ICraftingCPU ret=(ICraftingCPU) cpu.get(other);
			if(ret instanceof CraftingCPUCluster){return (CraftingCPUCluster) ret;}
		} catch (Exception e) {
			broken=true;
			e.printStackTrace();
		}
		
		CraftingCPUCluster found=null;
		if(found==null){
			found=(CraftingCPUCluster) getProxy().getCrafting().getCpus().stream().filter(s->{
					if(s instanceof CraftingCPUCluster){
						CraftingCPUCluster c=(CraftingCPUCluster) s;
						boolean b=c.isBusy();
						if(b){
						
							if(Objects.equals(c.getLastCraftingLink().getCraftingID(),(cid).getCraftingID())){
							//last=c.getLastCraftingLink();
						
							return true;
						    }
							
						}
						
						
					}
					return false;
				}).findFirst().orElse(null);
		}
		
			return found;
	} catch (GridAccessException e) {
		
		e.printStackTrace();
	}
	return null;
	
	
}


public CraftingCPUCluster getCpu(String cid){
	try {
		return (CraftingCPUCluster) getProxy().getCrafting().getCpus().stream().filter(s->{
			if(s instanceof CraftingCPUCluster){
				CraftingCPUCluster c=(CraftingCPUCluster) s;
				if(c.isBusy()){
				if(c.getLastCraftingLink().getCraftingID().equals(cid)){
					return true;
					
				}
				}
				
				
			}
			return false;
		}).findFirst().orElse(null);
	} catch (GridAccessException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return null;
	
}
public void onChunkUnload() {
	
	this.getProxy().onChunkUnload();
	super.onChunkUnload();
}

public void invalidate() {

	this.getProxy().invalidate();
	super.invalidate();
}

@Override
public void validate() {
	this.getProxy().validate();
	super.validate();
}
int ticksSinceLoaded;

@Override
public IGridNode getActionableNode() {
	
	return getProxy().getNode();
}

@Override
public ImmutableSet<ICraftingLink> getRequestedJobs() {
	
	return last==null?ImmutableSet.of():ImmutableSet.of(last);
}

@Override
public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
	
	return items;
}

@Override
public void jobStateChange(ICraftingLink link) {
/*if(last==link){
	last=null;
	
}*/
	
}



 int index=0;

protected class UIFactory {

	private final UIBuildContext uiBuildContext;

	public UIFactory(UIBuildContext buildContext) {
		this.uiBuildContext = buildContext;
	}

	public ModularWindow createWindow() {
		ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
		// builder.setGuiTint(getUIBuildContext().getGuiColorization());
		if (doesBindPlayerInventory()) {
			builder.bindPlayerInventory(getUIBuildContext().getPlayer());
		}
		// builder.bindPlayerInventory(builder.getPlayer(), 7,
		// getGUITextureSet().getItemSlot());

		addTitleToUI(builder);
		addUIWidgets(builder);
		/*
		 * if (getUIBuildContext().isAnotherWindow()) { builder.widget(
		 * ButtonWidget.closeWindowButton(true) .setPos(getGUIWidth() - 15,
		 * 3)); }
		 */

		/*
		 * final CoverInfo coverInfo = uiBuildContext.getTile()
		 * .getCoverInfoAtSide(uiBuildContext.getCoverSide()); final
		 * CoverBehaviorBase<?> behavior = coverInfo.getCoverBehavior();
		 * if (coverInfo.getMinimumTickRate() > 0 &&
		 * behavior.allowsTickRateAddition()) { builder.widget( new
		 * GT_CoverTickRateButton(coverInfo, builder).setPos(getGUIWidth() -
		 * 24, getGUIHeight() - 24)); }
		 */
		return builder.build();
	}

	/**
	 * Override this to add widgets for your UI.
	 */

	// IItemHandlerModifiable fakeInv=new ItemHandlerModifiable();

	protected void addUIWidgets(ModularWindow.Builder builder) {
		
		builder.widget(new FakeSyncWidget.IntegerSyncer(()->state(), s->state=s));
		builder.widget(
				new DynamicTextWidget(()->new Text(StatCollector.translateToLocal("proghatches.submitter.state."+state)))
				.setSynced(false)
				.setPos(5, 3));
		final IItemHandlerModifiable inventoryHandler = new MappingItemHandler(inv, 0, inv.length){
			public boolean isItemValid(int slot, ItemStack stack) {
				return	stack.getItem() instanceof ICraftingPatternItem;
				};
			
		};
		Scrollable sc = new Scrollable().setVerticalScroll();
		builder.widget(new FakeSyncWidget.IntegerSyncer(() -> index, s ->{ index = s;updateComparator();}));
		builder.widget(new FakeSyncWidget.BooleanSyncer(() -> on, s -> on = s));
		//builder.widget(new FakeSyncWidget.IntegerSyncer(() -> tankselected, s -> tankselected = s));
		final IDrawable[] background = new IDrawable[] { GUITextureSet.DEFAULT.getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME};
		final IDrawable[] special = new IDrawable[] { GUITextureSet.DEFAULT.getItemSlot(), GTUITextures.OVERLAY_SLOT_PATTERN_ME,
				new ItemDrawable(new ItemStack(MyMod.progcircuit))
				//GTUITextures.OVERLAY_BUTTON_CROSS
				
		};
	
	
		
		
	     for (int row = 0; row * 4 < inventoryHandler.getSlots() - 1; row++) {
	            int columnsToMake = Math.min(inventoryHandler.getSlots() - row * 4, 4);
	            for (int column = 0; column < columnsToMake; column++) {
	            	int indexl= row * 4 + column;
	                sc.widget(new SlotWidget(new BaseSlot(inventoryHandler, indexl,true)) {
						
	                	public boolean onMouseScroll(int direction) {
							return false;};
						public IDrawable[] getBackground() {
							
							if (indexl == index) {
								return special;
							}
							;
							return background;
						}}.setPos(column * 18, row * 18)
	                       );
	            }
	        }
	            
		
		/*
		 * SlotGroup do not respect Scrollable!!!
		 * sc.widget(SlotGroup.ofItemHandler(inventoryHandler, 4)

				.startFromSlot(0).endAtSlot(inv.length-1).background(background)
				.slotCreator(s->new BaseSlot(inventoryHandler, s,true))
				.widgetCreator((h) -> (SlotWidget) new SlotWidget(h) {
					

					public IDrawable[] getBackground() {
						// System.out.println(h.getSlotIndex()+"
						// "+(slotselected-1));
						if (h.getSlotIndex() == index) {
							return special;
						}
						;
						return background;
					};
				})

				.build()

		);*/
	
		builder.widget(sc.setPos(3 + 4, 3 + 8).setSize(18 * 4+1, 18 * 4));
		
		
		
		builder.widget(new CycleButtonWidget().setToggle(()->abortingMode, s->abortingMode=s)
           .setTextureGetter(s->{
        	   if(s==0)return GTUITextures.OVERLAY_BUTTON_CROSS;
        	
        			   return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
           })
           .addTooltip(0, StatCollector.translateToLocal("proghatches.submitter.mode.0"))
           .addTooltip(1, StatCollector.translateToLocal("proghatches.submitter.mode.1"))
				.setBackground(() -> {
               {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                       };
                }
            })
            
            .setSize(18, 18)
            .setPos(120+20, 3));
		
		builder.widget(new ButtonWidget()
		        .setOnClick((a,b)->{
		        	if(a.mouseButton==0){
		        	//if(on)last=null;
		        	on=!on;
		        			
		        	}
		        	if(a.mouseButton==1){
		        		on=false;
		        		last=null;
		        		index=(index+1)%SLOT_SIZE;
		        		
		        	}
		        	
		        })
		        	
		        	
		        	.addTooltips(Arrays.asList(
		        			StatCollector.translateToLocal("proghatches.submitter.power.0"),
		        			StatCollector.translateToLocal("proghatches.submitter.power.1"))
		        			)		
		        .setBackground(() -> {
					if (on) {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD_PRESSED,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
					} else {
						return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
								GTUITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
					}
		            })
		            
		            .setSize(18, 18)
		            .setPos(120+20, 3+20));
		
		
		
		ItemStackHandler iss0=new ItemStackHandler(upgrade){
			
			public boolean isItemValid(int slot, ItemStack stack) {
			return	Api.INSTANCE.definitions().materials().cardRedstone().isSameAs(stack);
				
			};
		public int getSlotLimit(int slot) {
			return 1;};
		};
		
		builder.widget( new SlotWidget(new BaseSlot(iss0, 0)){
			
		
		}
		.setPos(60+40, 3+20).addTooltip(StatCollector.translateToLocal("proghatches.amountmaintainer.rscard")));
		
		builder.widget(new CycleButtonWidget().setGetter(()->rsmode)
				.setSetter(s->rsmode=s).setLength(4)
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
          // .addTooltip(4, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.4"))
          // .addTooltip(5, StatCollector.translateToLocal("proghatches.amountmaintainer.rscard.mode.5"))
           
           
           
				.setBackground(() -> {
               {
                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
                       };
                }
            })
            .setEnabled((a)->(upgrade[0]!=null))
            .setSize(18, 18)
            .setPos(60+20+40, 3+20));
		
		/*sc = new Scrollable().setVerticalScroll();

		final IDrawable[] background0 = new IDrawable[] { GUITextureSet.DEFAULT.getFluidSlot() };
		final IDrawable[] special0 = new IDrawable[] { GUITextureSet.DEFAULT.getFluidSlot(),
				GTUITextures.OVERLAY_SLOT_ARROW_ME };

		

		builder.widget(sc.setPos(3 + 18 * 4 + 4, 3 + 8).setSize(18, 18 * 4));
		*/
		builder.widget(new CycleButtonWidget().setToggle(()->asManyAsPossible, s->asManyAsPossible=s)
		           .setTextureGetter(s->{
		        	   if(s==0)return GTUITextures.OVERLAY_BUTTON_CROSS;
		        	
		        			   return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
		           })
		           .addTooltip(0, StatCollector.translateToLocal("proghatches.submitter.number.mode.0"))
		           .addTooltip(1, StatCollector.translateToLocal("proghatches.submitter.number.mode.1"))
						.setBackground(() -> {
		               {
		                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
		                       };
		                }
		            })
		            
		            .setSize(18, 18)
		            .setPos(120+20, 3+20+20));
		builder.widget(new CycleButtonWidget().setToggle(()->skipIfFail, s->skipIfFail=s)
		           .setTextureGetter(s->{
		        	   if(s==0)return GTUITextures.OVERLAY_BUTTON_CROSS;
		        	
		        			   return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
		           })
		           .addTooltip(0, StatCollector.translateToLocal("proghatches.submitter.skip.mode.0"))
		           .addTooltip(1, StatCollector.translateToLocal("proghatches.submitter.skip.mode.1"))
						.setBackground(() -> {
		               {
		                    return new IDrawable[] { GTUITextures.BUTTON_STANDARD,
		                       };
		                }
		            })
		            
		            .setSize(18, 18)
		            .setPos(120+20, 3+20+20+20));
	}

	public UIBuildContext getUIBuildContext() {
		return uiBuildContext;
	}

	/*
	 * public boolean isCoverValid() { return !getUIBuildContext().getTile()
	 * .isDead() && getUIBuildContext().getTile()
	 * .getCoverBehaviorAtSideNew(getUIBuildContext().getCoverSide()) !=
	 * GregTechAPI.sNoBehavior; }
	 */

	protected void addTitleToUI(ModularWindow.Builder builder) {
		/*
		 * ItemStack coverItem =
		 * GTUtility.intToStack(getUIBuildContext().getCoverID()); if
		 * (coverItem != null) { builder.widget( new
		 * ItemDrawable(coverItem).asWidget() .setPos(5, 5) .setSize(16,
		 * 16)) .widget( new
		 * TextWidget(coverItem.getDisplayName()).setDefaultColor(
		 * COLOR_TITLE.get()) .setPos(25, 9)); }
		 */
	}

	protected int getGUIWidth() {
		return 176;
	}

	protected int getGUIHeight() {
		return 107 + 18 * 3 + 18;
	}

	protected boolean doesBindPlayerInventory() {
		return true;
	}

	protected int getTextColorOrDefault(String textType, int defaultColor) {
		return defaultColor;
	}

	protected final Supplier<Integer> COLOR_TITLE = () -> getTextColorOrDefault("title", 0x222222);
	protected final Supplier<Integer> COLOR_TEXT_GRAY = () -> getTextColorOrDefault("text_gray", 0x555555);
	protected final Supplier<Integer> COLOR_TEXT_WARN = () -> getTextColorOrDefault("text_warn", 0xff0000);
}	int rsmode;
ItemStack upgrade[]=new ItemStack[1];
@Override
public ModularWindow createWindow(UIBuildContext buildContext) {
	
	return new UIFactory(buildContext).createWindow();
}
boolean on=true;

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

}
