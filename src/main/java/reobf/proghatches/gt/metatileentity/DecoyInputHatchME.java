package reobf.proghatches.gt.metatileentity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import org.spongepowered.asm.mixin.Unique;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.me.cache.GridStorageCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.storage.MEInventoryHandler;
import appeng.me.storage.MEPassThrough;
import appeng.util.SortedArrayList;
import appeng.util.item.AEFluidStack;
import appeng.util.item.MeaningfulFluidIterator;
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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.IMEHatchOverrided;
import reobf.proghatches.main.registration.Registration;

public class DecoyInputHatchME  extends GT_MetaTileEntity_Hatch_Input_ME implements IMEHatchOverrided{

	public DecoyInputHatchME(int aID, /*boolean autoPullAvailable,*/ String aName, String aNameRegional) {
		super(aID, /*autoPullAvailable*/true, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
		desc=reobf.proghatches.main.Config.get("DIHME", ImmutableMap.of());
	} 
	String[] desc;
	  @Override
	public String[] getDescription() {
		
		return desc;
	}
	public DecoyInputHatchME(String aName, /*boolean autoPullAvailable,*/ int aTier, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, true, aTier, aDescription, aTextures);
		
	}
	@Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
    return   new DecoyInputHatchME(mName, mTier, mDescriptionArray, mTextures);
    }
	
	@Unique
	private static Field f;
	
	 static {
		try {
			f=GridStorageCache.class.getDeclaredField("activeCellProviders");
		} catch (Exception e) {e.printStackTrace();
			throw new AssertionError(e);
			
		}
		f.setAccessible(true);
	}
	@Unique
	private static Set<ICellProvider> get(GridStorageCache thiz){
		
		try {
			return (Set<ICellProvider>) f.get(thiz);
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}
	
	@Override
	public void overridedBehoviour(int minPull) {
		AENetworkProxy proxy = getProxy();

		try {
			GridStorageCache st = (GridStorageCache) proxy.getStorage();
			
			TreeMultimap<Integer, IMEInventoryHandler<IAEFluidStack>> orderMap = TreeMultimap.create((a, b) -> -a + b,
					(a, b) -> a.hashCode() - b.hashCode());
			for (final ICellProvider cc : get(st)) {
			
			List<IMEInventoryHandler> list = cc
				.getCellArray(StorageChannel.FLUIDS);
			
			for(IMEInventoryHandler l:list){
				 if(!(l instanceof MEInventoryHandler))continue;
				/* if(l instanceof MEInventoryHandler){
				 MEInventoryHandler hh=(MEInventoryHandler) l; 
					if((hh.getInternal() instanceof MEPassThrough)) continue;
				}*/
				 
				orderMap.put(cc.getPriority(), l);
			}
			
			}
			
			boolean keepFirstEmpty=false;
			if(reserveFirst&&orderMap.isEmpty()==false)
			{
				if(orderMap.get(orderMap.keySet().first()).stream()
						.filter(s->s.getAvailableItems(StorageChannel.FLUIDS.createList()).getFirstItem()!=null)
						.findAny().isPresent()==false
						){
					keepFirstEmpty=true;
				}
		}
			
			
			ArrayList<IAEFluidStack> added = new ArrayList<>();
			IItemList<IAEFluidStack> all = new IItemList<IAEFluidStack>() {

				private final NavigableMap<IAEFluidStack, IAEFluidStack> records = new ConcurrentSkipListMap<>();

				@Override
				public void add(final IAEFluidStack option) {
					if (option == null) {
						return;
					}

					final IAEFluidStack st = this.records.get(option);

					if (st != null) {
						st.add(option);
						return;
					}
					added.add(option.copy());
					final IAEFluidStack opt = option.copy();

					this.putItemRecord(opt);
				}

				@Override
				public IAEFluidStack findPrecise(final IAEFluidStack itemStack) {
					if (itemStack == null) {
						return null;
					}

					return this.records.get(itemStack);
				}

				@Override
				public Collection<IAEFluidStack> findFuzzy(final IAEFluidStack filter, final FuzzyMode fuzzy) {
					throw new Error();
				}

				@Override
				public boolean isEmpty() {
					return !this.iterator().hasNext();
				}

				@Override
				public void addStorage(final IAEFluidStack option) {
					if (option == null) {
						return;
					}

					final IAEFluidStack st = this.records.get(option);

					if (st != null) {
						st.incStackSize(option.getStackSize());
						return;
					}
					added.add(option.copy());
					final IAEFluidStack opt = option.copy();

					this.putItemRecord(opt);
				}

				/*
				 * public void clean() { Iterator<StackType> i = iterator();
				 * while (i.hasNext()) { StackType AEI = i.next(); if (
				 * !AEI.isMeaningful() ) i.remove(); } }
				 */

				@Override
				public void addCrafting(final IAEFluidStack option) {
					if (option == null) {
						return;
					}

					final IAEFluidStack st = this.records.get(option);

					if (st != null) {
						st.setCraftable(true);
						return;
					}

					final IAEFluidStack opt = option.copy();
					opt.setStackSize(0);
					opt.setCraftable(true);

					this.putItemRecord(opt);
				}

				@Override
				public void addRequestable(final IAEFluidStack option) {
					if (option == null) {
						return;
					}

					final IAEFluidStack st = this.records.get(option);

					if (st != null) {
						st.setCountRequestable(st.getCountRequestable() + option.getCountRequestable());
						st.setCountRequestableCrafts(
								st.getCountRequestableCrafts() + option.getCountRequestableCrafts());
						return;
					}

					final IAEFluidStack opt = option.copy();
					opt.setStackSize(0);
					opt.setCraftable(false);
					opt.setCountRequestable(option.getCountRequestable());
					opt.setCountRequestableCrafts(option.getCountRequestableCrafts());

					this.putItemRecord(opt);
				}

				@Override
				public IAEFluidStack getFirstItem() {
					for (final IAEFluidStack stackType : this) {
						return stackType;
					}

					return null;
				}

				@Override
				public int size() {
					return this.records.size();
				}

				@Override
				public Iterator<IAEFluidStack> iterator() {
					return new MeaningfulFluidIterator<>(this.records.values().iterator());
				}

				@Override
				public void resetStatus() {
					for (final IAEFluidStack i : this) {
						i.reset();
					}
				}

				public void clear() {
					this.records.clear();
				}

				private IAEFluidStack putItemRecord(final IAEFluidStack itemStack) {
					return this.records.put(itemStack, itemStack);
				}

				private Collection<IAEFluidStack> findFuzzyDamage(final AEFluidStack filter, final FuzzyMode fuzzy,
						final boolean ignoreMeta) {
					throw new Error();
				}
			};

			for (Entry<Integer, IMEInventoryHandler<IAEFluidStack>> ent : orderMap.entries()) {
				ent.getValue().getAvailableItems(all);
				if (added.size() > 16) {
					break;
				}
			}
			int index = 0;
			if(keepFirstEmpty){
				this.storedFluids[0] = null;
				index++;
				
			}
			Iterator<IAEFluidStack> iterator = added.iterator();
			while (iterator.hasNext() && index < 16) {
				IAEFluidStack currItem = iterator.next();
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
		 /* AENetworkProxy proxy = getProxy();
	        try {
	        	GridStorageCache st = (GridStorageCache) proxy.getStorage();
	        	IItemList<IAEFluidStack> all=StorageChannel.FLUIDS.createList();
			
			for (final ICellProvider cc : get(st)) {
				for (final IMEInventoryHandler<IAEFluidStack> h : cc.getCellArray(StorageChannel.FLUIDS)) {
					if(!(h instanceof IMEInventoryHandler))continue;
					if(h instanceof MEInventoryHandler){
						MEInventoryHandler hh=(MEInventoryHandler) h;
						if((hh.getInternal() instanceof MEPassThrough))
						continue;
						}
					h.getAvailableItems(all);
				}
			}
			
			 int index = 0;
			 Iterator<IAEFluidStack> it = all.iterator();
		               
			 while (it.hasNext() && index < 16) {
				 IAEFluidStack currItem = it.next();
	                if (currItem.getStackSize() >= minPull) {
	                    FluidStack itemstack = GT_Utility.copyAmount(1, currItem.getFluidStack());
	                    this.storedFluids[index] = itemstack;
	                    index++;
	                }
			 }
			 if(index==16)return;
			 if(reserveFirst&&index==0){
				 storedFluids[0] = null;
				 index++;
				 
			 }
			 IMEMonitor<IAEFluidStack> sg = proxy.getStorage()
	                .getFluidInventory();
	            Iterator<IAEFluidStack> iterator = sg.getStorageList()
	                .iterator();
	          
	            while (iterator.hasNext() && index < 16) {
	            	IAEFluidStack currItem = iterator.next();
	                if(all.findPrecise(currItem)!=null){continue;}
	                if (currItem.getStackSize() >= minPull) {
	                	FluidStack itemstack = GT_Utility.copyAmount(1, currItem.getFluidStack());
	                    this.storedFluids[index] = itemstack;
	                    index++;
	                }
	            }
	            for (int i = index; i < 16; i++) {
	            	storedFluids[i] = null;
	            }

	        } catch (final GridAccessException ignored) {}*/
		
	}
	
	 @Override
	    public void addUIWidgets(ModularWindow.Builder builder, UIBuildContext buildContext) {
	       
	       
	        
	        
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
	        
	    } 
	 boolean reserveFirst;
@Override
public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
	if(aPlayer.isSneaking()){
		reserveFirst=!reserveFirst;
		 aPlayer.addChatMessage(
		            new ChatComponentTranslation(
		                "proghatches.decoy.reservefirst." + (reserveFirst ? "enabled" : "disabled")));

		return;
	}
	super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ);
}@Override
public IAEStack overridedExtract(IMEMonitor thiz, IAEStack request, Actionable mode, BaseActionSource src) {
	
	long requested = request.getStackSize();
	long num = request.getStackSize();
	try {
		AENetworkProxy proxy = getProxy();
		GridStorageCache st;
		ArrayList<IMEInventoryHandler> ordered = new ArrayList();
				

		st = (GridStorageCache) proxy.getStorage();
		for (final ICellProvider cc : get(st)) {
			ordered.addAll(cc.getCellArray(StorageChannel.FLUIDS));
		}
		ordered.sort((IMEInventoryHandler a, IMEInventoryHandler b) -> a.getPriority() - b.getPriority());
		
		boolean somethingChanged=true;
		done: while (true) {
			if(somethingChanged){
				somethingChanged=false;
			}else{break done;}
			if(num<=0){break done;}
			ArrayList<IMEInventoryHandler> most = new ArrayList<>();
			long most_ = -1;
			long sec_ = 0;
			for (IMEInventoryHandler o : ordered) {
				IAEStack ex = o.extractItems(request.copy().setStackSize(Long.MAX_VALUE), Actionable.SIMULATE, src);
				if (ex != null) {
					if (most_ == -1) {
						most_ = ex.getStackSize();
						most.add(o);
					} else if (most_ == ex.getStackSize()) {
						most.add(o);
					} else if (most_ < ex.getStackSize()) {
						sec_ = most_;
						most = new ArrayList<>();most.add(o);
						most_ = ex.getStackSize();
					}else if(sec_==0)sec_=ex.getStackSize();
				}
			}
			if (most_ == -1)
				break done;
			
			
			most.sort((a,b)->a.getPriority()-b.getPriority());
			{
				long eachmax = most_ - sec_;
				int nums = most.size();

				if (num >= eachmax * nums) {

					for (IMEInventoryHandler s : most) {
						IAEStack ret = s.extractItems(request.copy().setStackSize(eachmax), Actionable.MODULATE,
								src);
						if (ret != null){somethingChanged=true;
							num -= ret.getStackSize();}
					}
				} else if (num > nums) {
					for (IMEInventoryHandler s : most) {
						IAEStack ret = s.extractItems(request.copy().setStackSize(num / nums), Actionable.MODULATE,
								src);
						if (ret != null){somethingChanged=true;
							num -= ret.getStackSize();}
					}
				} else {
					for (IMEInventoryHandler s : most) {
						IAEStack ret = s.extractItems(request.copy().setStackSize(1), Actionable.MODULATE, src);
						if (ret != null) {
							num -= ret.getStackSize();
							if (num <= 0){somethingChanged=true;
								break done;}
						}
					}

				}

			}

		}

	} catch (GridAccessException e) {

	}
	if(requested==num)return null;
	return request.copy().setStackSize(requested-num);
}
}
