package reobf.proghatches.ae;

import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

import javax.annotation.Nonnull;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.InvWrapper;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.helpers.ICustomNameObject;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEMonitorPassThrough;
import appeng.me.storage.NullInventory;
import appeng.util.prioitylist.OreFilteredList;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.GT_Utility.ItemId;
import li.cil.oc.api.network.SidedEnvironment;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.gt.metatileentity.util.MappingFluidTank;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.MyMod;

public class TileStorageProxy extends TileEntity implements 
 IGridProxyable, IActionHost, IStorageMonitorable, ITileStorageMonitorable,ITileWithModularUI
 
 {
	boolean fluid;
	public  AENetworkProxy gridProxy;

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		
		return getProxy().getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
		
		
	}

	@Override
	public IMEMonitor<IAEItemStack> getItemInventory() {
	
		return items;
	}

	@Override
	public IMEMonitor<IAEFluidStack> getFluidInventory() {
	
		return fluids;
	}
	  
	private final MEMonitorPassThrough<IAEItemStack> items = new MEMonitorPassThrough<IAEItemStack>(
	            new NullInventory<IAEItemStack>(),
	            StorageChannel.ITEMS){
		  
		 
		  
	  };
	    private final MEMonitorPassThrough<IAEFluidStack> fluids = new MEMonitorPassThrough<IAEFluidStack>(
	            new NullInventory<IAEFluidStack>(),
	            StorageChannel.FLUIDS){
	   
	    	
	    	
	    };
		private boolean init;
	@Override
	public IGridNode getActionableNode() {
		
		return getProxy().getNode();
	}

	@Override
	public AENetworkProxy getProxy() {
		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy", new ItemStack(MyMod.iohub, 1), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			gridProxy.setValidSides(EnumSet.range(ForgeDirection.DOWN, ForgeDirection.EAST));

		}

		

		return this.gridProxy;
	}

	@Override
	public DimensionalCoord getLocation() {
	
		return new DimensionalCoord(this);
	}
	
ItemStack[] is=new ItemStack[36];

int fuzzmode=1;
//0 disabled
//1 strict
//4 ignore nbt
//3 oredict
//2 passthrough
String dict="";
String lastdict="";
Predicate<IAEItemStack> dictfilter;
FluidStack[] fs=new FluidStack[36];
public boolean noAdvConfig;
public boolean isProxyExternal;	
	
public Predicate<IAEItemStack> itemFilter(){
	return 
		
		s->fluid==false&&s!=null&&checkItem(s)
	;}

private boolean checkItem(@Nonnull IAEItemStack s){
	if(fuzzmode==0)return false;
	if(fuzzmode==1){
		for(ItemStack stack:is){
			if(stack==null)continue;
			if(	s.isSameType(stack))return true;
		}
	}
	if(fuzzmode==4){
		for(ItemStack stack:is){
			if(stack==null)continue;
			if(	s.getItem()==stack.getItem()&&s.getItemDamage()==stack.getItemDamage())return true;
		}
	}
	if(fuzzmode==3){
		if(dictfilter==null||lastdict.equals(dict)==false){
		 dictfilter = OreFilteredList.makeFilter(dict);
		 if(dictfilter==null)dictfilter=__->false;
		 lastdict=dict;
		}
		return dictfilter.test(s);
	}
	if(fuzzmode==2)return true;
	return false;
}


public Predicate<IAEFluidStack> fluidFilter(){
	
	
	return 
		
		s->fluid&&s!=null&&checkFluid(s);
		}

private boolean checkFluid(IAEFluidStack s) {

	for(FluidStack stack:fs){
		if(stack==null)continue;
		if(	s.getFluid()==stack.getFluid())return true;
	}
	return false;
}

	@Override
	public void gridChanged() {
		  try {
	          
			  IMEMonitor<IAEItemStack> items=  this.gridProxy.getStorage().getItemInventory();
	           IMEMonitor<IAEItemStack> wrappeditem=  new IMEMonitor<IAEItemStack>(){

					@Override
					public AccessRestriction getAccess() {
						
						return items.getAccess();
					}

					@Override
					public boolean isPrioritized(IAEItemStack input) {
						
						return items.isPrioritized(input);
					}

					@Override
					public boolean canAccept(IAEItemStack input) {
						
						return items.canAccept(input);
					}

					@Override
					public int getPriority() {
						
						return items.getPriority();
					}

					@Override
					public int getSlot() {
						
						return items.getSlot();
					}

					@Override
					public boolean validForPass(int i) {
				
						return items.validForPass(i);
					}

					@Override
					public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src) {
						
						return items.injectItems(input, type, src);
					}

					@Override
					public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src) {
						if(!itemFilter().test(request))return null;
						return items.extractItems(request, mode, src);
					}

					@Override
					public StorageChannel getChannel() {
						
						return items.getChannel();
					}

					@Override
					public void addListener(IMEMonitorHandlerReceiver<IAEItemStack> l, Object verificationToken) {
						items.addListener(l, verificationToken);
						
					}

					@Override
					public void removeListener(IMEMonitorHandlerReceiver<IAEItemStack> l) {
						items.removeListener(l);
						
					}

					@SuppressWarnings({ "deprecation", "rawtypes" })
					@Override
					public IItemList<IAEItemStack> getAvailableItems(IItemList out) {
					
						items.getAvailableItems(new IItemList<IAEItemStack>() {

							@Override
							public void add(IAEItemStack option) {
								if(!itemFilter().test(option))return;
								 out.add(option);
							}

							
							@Override
							public IAEItemStack findPrecise(IAEItemStack i) {
							
								return (IAEItemStack) out.findPrecise(i);
							}

							@Override
							public Collection<IAEItemStack> findFuzzy(IAEItemStack input, FuzzyMode fuzzy) {
							
								return out.findFuzzy(input, fuzzy);
							}

							@Override
							public boolean isEmpty() {
								
								return out.isEmpty();
							}

							@Override
							public void addStorage(IAEItemStack option) {
								if(!itemFilter().test(option))return;
								 out.addStorage(option);
							}

							@Override
							public void addCrafting(IAEItemStack option) {
								out.addCrafting(option);
							}

							@Override
							public void addRequestable(IAEItemStack option) {
							
								out.addRequestable(option);
							}

							@Override
							public IAEItemStack getFirstItem() {
							
								return (IAEItemStack) out.getFirstItem();
							}

							@Override
							public int size() {
								
								return out.size();
							}

							@Override
							public Iterator<IAEItemStack> iterator() {
								
								return out.iterator();
							}

							@Override
							public void resetStatus() {
							out.resetStatus();
								
							}
						});
						
						return out;
					}

					@Override
					public IItemList<IAEItemStack> getStorageList() {
					IItemList<IAEItemStack> ls = items.getStorageList();
						return new IItemList<IAEItemStack>(){

							@Override
							public void add(IAEItemStack option) {
								ls.add(option);
								
							}

							@Override
							public IAEItemStack findPrecise(IAEItemStack i) {
								IAEItemStack ret = ls.findPrecise(i);
								if(!itemFilter().test(ret))return null;
								return ret;
							}

							@Override
							public Collection<IAEItemStack> findFuzzy(IAEItemStack input, FuzzyMode fuzzy) {
								
								Collection<IAEItemStack> ret = ls.findFuzzy(input,fuzzy);
								ret.removeIf(itemFilter().negate());
								return ret;
							}

							@Override
							public boolean isEmpty() {
							
								return ls.isEmpty();
							}

							@Override
							public void addStorage(IAEItemStack option) {
								ls.addStorage(option);
								
							}

							@Override
							public void addCrafting(IAEItemStack option) {
							ls.addCrafting(option);
								
							}

							@Override
							public void addRequestable(IAEItemStack option) {
								ls.addRequestable(option);
								
							}

							@Override
							public IAEItemStack getFirstItem() {
								 Iterator<IAEItemStack> itr = iterator();
								 IAEItemStack ret;
								for(;itr.hasNext();){
									ret=itr.next();
									if(itemFilter().test(ret))return ret;
								}
								return null;
							}

							@Override
							public int size() {
							
								return ls.size();
							}

							@Override
							public Iterator<IAEItemStack> iterator() {
								Iterator<IAEItemStack> itr = ls.iterator();
								
								return new Iterator<IAEItemStack>() {
									IAEItemStack peek;
									boolean peekvalid;
									boolean done;
									@Override
									public IAEItemStack next() {
										if(done)throw new NoSuchElementException();
									if(peekvalid){
										peekvalid=false;
										return peek;
									}
									if(find()==false)throw new NoSuchElementException();
									return peek;
									}
									
									@Override
									public boolean hasNext() {
										if(done)return false;
										if(peekvalid){return true;}
										if(find()==false)return false;
										peekvalid=true;
										return true;
									}
									
									public boolean find(){
										while(itr.hasNext()){
											peek=itr.next();
											if(itemFilter().test(peek)){return true;}
										}
										done=true;
										return false;
									}
									
									
									
								};
							}

							@Override
							public void resetStatus() {
								ls.resetStatus();
								
							}};
					}
					
					
					
	            	};
	            
	            this.items.setInternal(wrappeditem);
	            
	            
	            
	            
	            
	            
	            IMEMonitor<IAEFluidStack> fluids=  this.gridProxy.getStorage().getFluidInventory();
		           IMEMonitor<IAEFluidStack> wrappedfluid=  new IMEMonitor<IAEFluidStack>(){

						@Override
						public AccessRestriction getAccess() {
							
							return fluids.getAccess();
						}

						@Override
						public boolean isPrioritized(IAEFluidStack input) {
							
							return fluids.isPrioritized(input);
						}

						@Override
						public boolean canAccept(IAEFluidStack input) {
							
							return fluids.canAccept(input);
						}

						@Override
						public int getPriority() {
							
							return fluids.getPriority();
						}

						@Override
						public int getSlot() {
							
							return fluids.getSlot();
						}

						@Override
						public boolean validForPass(int i) {
					
							return fluids.validForPass(i);
						}

						@Override
						public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src) {
							
							return fluids.injectItems(input, type, src);
						}

						@Override
						public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
							if(!fluidFilter().test(request))return null;
							return fluids.extractItems(request, mode, src);
						}

						@Override
						public StorageChannel getChannel() {
							
							return fluids.getChannel();
						}

						@Override
						public void addListener(IMEMonitorHandlerReceiver<IAEFluidStack> l, Object verificationToken) {
							fluids.addListener(l, verificationToken);
							
						}

						@Override
						public void removeListener(IMEMonitorHandlerReceiver<IAEFluidStack> l) {
							fluids.removeListener(l);
							
						}

						@SuppressWarnings({ "deprecation", "rawtypes" })
						@Override
						public IItemList<IAEFluidStack> getAvailableItems(IItemList out) {
						
							fluids.getAvailableItems(new IItemList<IAEFluidStack>() {

								@Override
								public void add(IAEFluidStack option) {
									if(!fluidFilter().test(option))return;
									 out.add(option);
								}

								
								@Override
								public IAEFluidStack findPrecise(IAEFluidStack i) {
								
									return (IAEFluidStack) out.findPrecise(i);
								}

								@Override
								public Collection<IAEFluidStack> findFuzzy(IAEFluidStack input, FuzzyMode fuzzy) {
								
									return out.findFuzzy(input, fuzzy);
								}

								@Override
								public boolean isEmpty() {
									
									return out.isEmpty();
								}

								@Override
								public void addStorage(IAEFluidStack option) {
									if(!fluidFilter().test(option))return;
									 out.addStorage(option);
								}

								@Override
								public void addCrafting(IAEFluidStack option) {
									out.addCrafting(option);
								}

								@Override
								public void addRequestable(IAEFluidStack option) {
								
									out.addRequestable(option);
								}

								@Override
								public IAEFluidStack getFirstItem() {
								
									return (IAEFluidStack) out.getFirstItem();
								}

								@Override
								public int size() {
									
									return out.size();
								}

								@Override
								public Iterator<IAEFluidStack> iterator() {
									
									return out.iterator();
								}

								@Override
								public void resetStatus() {
								out.resetStatus();
									
								}
							});
							
							return out;
						}

						@Override
						public IItemList<IAEFluidStack> getStorageList() {
						IItemList<IAEFluidStack> ls = fluids.getStorageList();
							return new IItemList<IAEFluidStack>(){

								@Override
								public void add(IAEFluidStack option) {
									ls.add(option);
									
								}

								@Override
								public IAEFluidStack findPrecise(IAEFluidStack i) {
									IAEFluidStack ret = ls.findPrecise(i);
									if(!fluidFilter().test(ret))return null;
									return ret;
								}

								@Override
								public Collection<IAEFluidStack> findFuzzy(IAEFluidStack input, FuzzyMode fuzzy) {
									
									Collection<IAEFluidStack> ret = ls.findFuzzy(input,fuzzy);
									ret.removeIf(fluidFilter().negate());
									return ret;
								}

								@Override
								public boolean isEmpty() {
								
									return ls.isEmpty();
								}

								@Override
								public void addStorage(IAEFluidStack option) {
									ls.addStorage(option);
									
								}

								@Override
								public void addCrafting(IAEFluidStack option) {
								ls.addCrafting(option);
									
								}

								@Override
								public void addRequestable(IAEFluidStack option) {
									ls.addRequestable(option);
									
								}

								@Override
								public IAEFluidStack getFirstItem() {
									 Iterator<IAEFluidStack> itr = iterator();
									 IAEFluidStack ret;
									for(;itr.hasNext();){
										ret=itr.next();
										if(fluidFilter().test(ret))return ret;
									}
									return null;
								}

								@Override
								public int size() {
								
									return ls.size();
								}

								@Override
								public Iterator<IAEFluidStack> iterator() {
									Iterator<IAEFluidStack> itr = ls.iterator();
									
									return new Iterator<IAEFluidStack>() {
										IAEFluidStack peek;
										boolean peekvalid;
										boolean done;
										@Override
										public IAEFluidStack next() {
											if(done)throw new NoSuchElementException();
										if(peekvalid){
											peekvalid=false;
											return peek;
										}
										if(find()==false)throw new NoSuchElementException();
										return peek;
										}
										
										@Override
										public boolean hasNext() {
											if(done)return false;
											if(peekvalid){return true;}
											if(find()==false)return false;
											peekvalid=true;
											return true;
										}
										
										public boolean find(){
											while(itr.hasNext()){
												peek=itr.next();
												if(fluidFilter().test(peek)){return true;}
											}
											done=true;
											return false;
										}
										
										
										
									};
								}

								@Override
								public void resetStatus() {
									ls.resetStatus();
									
								}};
						}
						
						
						
		            	};
		            
		            this.fluids.setInternal(wrappedfluid);
	      
		  
		  
		  } catch (final GridAccessException gae) {
	            this.items.setInternal(new NullInventory<>());
	            this.fluids.setInternal(new NullInventory<>());
	        }
		
	}
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(!isProxyExternal)getProxy().readFromNBT(compound);
		dict=	compound.getString("dict" );
		fuzzmode=compound.getInteger("fuzzmode");
		fluid=compound.getBoolean("fluid");
		noAdvConfig=compound.getBoolean("noAdvConfig");
		isProxyExternal=compound.getBoolean("isProxyExternal");
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
	        Arrays.fill(this.is,null);

	       

	        for (int i = 0; i < nbttaglist.tagCount(); ++i)
	        {
	            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
	            int j = nbttagcompound1.getByte("Slot") & 255;

	            if (j >= 0 && j < this.is.length)
	            {
	                this.is[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
	            }
	        }
	        
	        NBTTagList nbttaglist0 = compound.getTagList("Fluids", 10);
	        Arrays.fill(this.fs,null);

	       

	        for (int i = 0; i < nbttaglist0.tagCount(); ++i)
	        {
	            NBTTagCompound nbttagcompound1 = nbttaglist0.getCompoundTagAt(i);
	            int j = nbttagcompound1.getByte("Slot") & 255;

	            if (j >= 0 && j < this.fs.length)
	            {
	                this.fs[j] = FluidStack.loadFluidStackFromNBT(nbttagcompound1);
	            }
	        }   
	        
	        
		super.readFromNBT(compound);
	}
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		if(!isProxyExternal)getProxy().writeToNBT(compound);
		compound.setString("dict", dict);
		compound.setInteger("fuzzmode", fuzzmode);
		compound.setBoolean("fluid", fluid);
		compound.setBoolean("noAdvConfig",noAdvConfig);
		compound.setBoolean("isProxyExternal",isProxyExternal);
		NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.is.length; ++i)
        {
            if (this.is[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.is[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
         nbttaglist = new NBTTagList();

        for (int i = 0; i < this.fs.length; ++i)
        {
            if (this.fs[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.fs[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Fluids", nbttaglist);
		super.writeToNBT(compound);
	}
	
	public void updateEntity() {

		
		super.updateEntity();

		if (init == false) {
			getProxy().onReady();
			init = true;

			
		}
		

	}

	

	public void onChunkUnload() {
		
		super.onChunkUnload();
		
		this.getProxy().onChunkUnload();
	}
	@Override
	public void validate() {
		super.validate();
		this.getProxy().validate();
	}

	public void invalidate() {
		super.invalidate();
		
		this.getProxy().invalidate();
	}

	@Override
	public IStorageMonitorable getMonitorable(ForgeDirection side, BaseActionSource src) {
	
		return this;
	}
	protected int getGUIWidth() {
		return 176;
	}

	protected int getGUIHeight() {
		return 107 + 18 * 3 + 18;
	}

	@Override
	public ModularWindow createWindow(UIBuildContext buildContext) {
		
		ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
		builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
		builder.bindPlayerInventory(buildContext.getPlayer());
		if(fluid){
			for( int ii=0;ii<36;ii++){
				final int i=ii;
			 builder.widget(FluidSlotWidget.phantom(new MappingFluidTank(s->{fs[i]=s==null?null:s.copy();gridChanged();}, ()->fs[i]), 
					 false
					 ).setPos(3+(i%9)*18, 3+(i/9)*18));
					
					 ;}
		}
		if(!fluid)
		{
		MappingItemHandler handler=new MappingItemHandler(is,0,36);
		handler.update=()->gridChanged();
		for(int i=0;i<36;i++)
			builder.widget(
		SlotWidget.phantom(handler, i)
		.setPos(3+(i%9)*18, 3+(i/9)*18));
		
		;

		if(!noAdvConfig)
		builder.widget(
		new CycleButtonWidget().setLength(5)
		.setSetter(s->{this.fuzzmode=s;gridChanged();})
		.setGetter(()->this.fuzzmode)
		.setTextureGetter(s->{
			if(s==0)return GT_UITextures.OVERLAY_BUTTON_CROSS;
			
			
			return GT_UITextures.OVERLAY_BUTTON_CHECKMARK;
		})
		.addTooltip(0, StatCollector.translateToLocal("proghatch.proxy.disable"))
		.addTooltip(1, StatCollector.translateToLocal("proghatch.proxy.Strict"))
		.addTooltip(4, StatCollector.translateToLocal("proghatch.proxy.ignoreNBT"))
		.addTooltip(3, StatCollector.translateToLocal("proghatch.proxy.oredict"))
		.addTooltip(2, StatCollector.translateToLocal("proghatch.proxy.passthrough"))
		.setBackground(GT_UITextures.BUTTON_STANDARD)
			.setTooltipShowUpDelay(TOOLTIP_DELAY)
		     .setPos(3,3+18*4)
		     .setSize(18, 18)
		);
		
		if(!noAdvConfig)
		builder.widget(new TextFieldWidget()	
				
				.setGetter(()->dict)
				.setSetter(s->
				{dict=s;gridChanged();})
				 .setSynced(true,true)
				
				 .setFocusOnGuiOpen(true).setTextColor(Color.WHITE.dark(1))

					.setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
					.setPos(3+20,3+18*4+1).setSize(16*8,16));
		
		
	}
		
		
		
		return builder.build();
	}

	

}
