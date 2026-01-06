package reobf.proghatches.ae.cpu;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.lazy;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static gregtech.api.enums.HatchElement.Energy;
import static gregtech.api.enums.HatchElement.Maintenance;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE;
import static gregtech.api.util.GTStructureUtility.buildHatchAdder;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;

import com.google.common.collect.HashMultimap;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.IAlignmentLimits;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.IStructureElementChain;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.AEApi;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.events.MENetworkCraftingCpuChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.WorldCoord;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.sync.GuiBridge;
import appeng.helpers.ICustomNameObject;
import appeng.items.tools.quartz.ToolQuartzCuttingKnife;
import appeng.me.GridAccessException;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.crafting.TileCraftingStorageTile;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.grid.AENetworkTile;
import appeng.util.Platform;
import appeng.util.item.AEItemDef;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.common.FMLCommonHandler;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.HatchElement;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTECubicMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEEnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTStructureUtility;
import gregtech.api.util.MultiblockTooltipBuilder;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.SIDItemStack;

@SuppressWarnings({ "deprecation", "unchecked" })
public class TileCPU extends MTEEnhancedMultiBlockBase<TileCPU>
		implements ISurvivalConstructable, IExternalManager, IGridProxyable, IActionHost,ICustomNameObject {
	private AENetworkProxy gridProxy;

	public TileCPU(String aName) {
		super(aName);

	}
	
	public  CraftingCPUCluster newCCC(){
		
		CraftingCPUCluster c = new CraftingCPUCluster(new WorldCoord(0, 0, 0), new WorldCoord(0, 0, 0));
		FMLCommonHandler.instance().bus().register(c);
		((IExternalManagerHolder) (Object) c).acceptIExternalManager(this);
		//cluster.add(c);
		clusterData.put(c, new  Data());
	return c;
		
	}
	
	
	public TileCPU(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));

	}

	
	private appeng.util.item.ItemList readList(final NBTTagList tag) {
        final appeng.util.item.ItemList out = new appeng.util.item.ItemList();

        if (tag == null) {
            return out;
        }

        for (int x = 0; x < tag.tagCount(); x++) {
            final IAEItemStack ais = AEItemStack.loadItemStackFromNBT(tag.getCompoundTagAt(x));
            if (ais != null) {
                out.add(ais);
            }
        }

        return out;
    }
	private NBTTagCompound writeItem(final IAEItemStack finalOutput2) {
        final NBTTagCompound out = new NBTTagCompound();

        if (finalOutput2 != null) {
            finalOutput2.writeToNBT(out);
        }

        return out;
    }
    private NBTTagList writeList(final IItemList<IAEItemStack> myList) {
        final NBTTagList out = new NBTTagList();

        for (final IAEItemStack ais : myList) {
            out.appendTag(this.writeItem(ais));
        }

        return out;
    }
	public void saveNBTData(net.minecraft.nbt.NBTTagCompound aNBT) {
		NBTTagList clusters = new NBTTagList();
		clusterData.entrySet().forEach(s -> {
			NBTTagCompound a = new NBTTagCompound();
			s.getKey().writeToNBT(a);
			a.setInteger("state",(s).getValue().state);
			a.setLong("storage", (s).getValue().storage);
			
			a.setTag("list",writeList((s).getValue().usedStorage));
			clusters.appendTag(a);
		});

		aNBT.setTag("clusters", clusters);
        getProxy().writeToNBT(aNBT);
        aNBT.setTag("refunds", writeList(refunds));;
        aNBT.setTag("acc", writeList(acc));;
        aNBT.setTag("accCondenser", writeList(accCondenser));;
        aNBT.setString("myName", myName);
		super.saveNBTData(aNBT);
	};

	public void loadNBTData(net.minecraft.nbt.NBTTagCompound aNBT) {
		//cluster.clear();
		clusterData.clear();
		NBTTagList clusters = (NBTTagList) aNBT.getTag("clusters");
		for (int i = 0; i < clusters.tagCount(); i++) {
			CraftingCPUCluster c = newCCC();
			NBTTagCompound a = clusters.getCompoundTagAt(i);
			c.readFromNBT(a);
			int state=a.getInteger("state");
			long l=a.getLong("storage");
			 appeng.util.item.ItemList it = readList((NBTTagList) a.getTag("list"));
			 Data d;
			 clusterData.put(c, d=new Data());
			d.state=state;
			d.storage=l;
			d.usedStorage=it;
			((IExternalManagerHolder)(Object)c).setStorage(l);
			
		}
		getProxy().readFromNBT(aNBT);
		refunds=readList((NBTTagList) aNBT.getTag("refunds"));
		acc=readList((NBTTagList) aNBT.getTag("acc"));
		accCondenser=readList((NBTTagList) aNBT.getTag("accCondenser"));
		updateAccCache();
		updateCondenserCache();
		myName=aNBT.getString("myName");
		super.loadNBTData(aNBT);

	};

	//List<CraftingCPUCluster> cluster = new ArrayList<>();
	LinkedHashMap<CraftingCPUCluster,Data> clusterData=new LinkedHashMap<>();
	public class Data{
		
		
		//0->available
		//1->crafting
		public int state;
		public appeng.util.item.ItemList usedStorage=new appeng.util.item.ItemList();
		public long storage;
	}
	public TileCraftingTile fakeCPU;
	
	private Consumer<AENetworkProxy> setter=s->{};
	public Collection<CraftingCPUCluster> getClusters() {
		return clusterData.keySet();
	}

	/*@Override
	public boolean isNewExtendedFacingValid(ForgeDirection direction, Rotation rotation, Flip flip) {

		return true;
	}*/

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new TileCPU(mName);
	}

	@Override
	public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
			int colorIndex, boolean active, boolean redstoneLevel) {
		if (side != facing) {
			return new ITexture[] { BlockIcons.getCasingTextureForId(CASING_INDEX) };
		}
		return active ? getTexturesActive(BlockIcons.getCasingTextureForId(CASING_INDEX))
				: getTexturesInactive(BlockIcons.getCasingTextureForId(CASING_INDEX));
	}

	protected static final int CASING_INDEX = 210;

	@Override
	protected MultiblockTooltipBuilder createTooltip() {

		final MultiblockTooltipBuilder tt = new MultiblockTooltipBuilder();
		Config.get(tt, "M_CPU");
		return tt;
	}

	@Override
	public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
		mCasingAmount=0;
		return checkPiece("M", 1, 1, 0);
	}
	  private static final IIconContainer textureFont = new Textures.BlockIcons.CustomIcon("icons/YOTTAHatch");
	  
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture,
				TextureFactory.builder().addIcon((textureFont)).extFacing().build()
				// TextureFactory.builder()
				// .setFromBlock(MyMod.iohub,
				// BlockIOHub.magicNO_provider_active_overlay).glow().build()
		};
	}

	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture,
				TextureFactory.builder().addIcon((textureFont)).extFacing().build()

		};
	}
	private static IStructureDefinition<TileCPU> STRUCTURE_DEFINITION(){
		if(STRUCTURE_DEFINITION==null)STRUCTURE_DEFINITION = StructureDefinition.<TileCPU> builder()
				.addShape("M", /* transpose */
						
						
						
						(new String[][] { { "bbb", "b~b", "bbb" },{ "bbb", "b b", "bbb" },{ "bbb", "bbb", "bbb" } }))
						
						/*transpose(
		                        new String[][] { { "hhh", "hhh", "hhh" }, { "h~h", "h-h", "hhh" }, { "hhh", "hhh", "hhh" }})
						
						)*/
				
				  .addElement(
		                    'b',
		                    ofChain(
		                        lazy(
		                            t -> GTStructureUtility.<TileCPU>buildHatchAdder()
		                                .atLeast(Energy, Maintenance, HatchElement.InputBus, HatchElement.OutputBus)
		                                .casingIndex(CASING_INDEX)
		                                .dot(1)
		                                .build()),
		                        onElementPass(
		                        		TileCPU ::onCorrectCasingAdded,
		                            lazy(TileCPU::getCasingElement))))
				
				/*.addElement('h',
						ofChain(buildHatchAdder(TileCPU.class)
								.atLeast(Energy, Maintenance, HatchElement.InputBus, HatchElement.OutputBus)
								.casingIndex(CASING_INDEX).dot(1)

								.build(),

								onElementPass(s -> {
								}, ofBlock(GregTechAPI.sBlockReinforced, 2)

						))

				)*/
			
				
				
				.build();
		
		return STRUCTURE_DEFINITION;
	}
	private static IStructureDefinition<TileCPU> STRUCTURE_DEFINITION;

	@Override
	public IStructureDefinition<TileCPU> getStructureDefinition() {
		
		return STRUCTURE_DEFINITION();
	}
	
@Override
public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
	 if (mMachine) return -1;
	 
	 
	
	 return survivalBuildPiece(
	            "M",
	            stackSize,
	            1,
	            1,
	           0,

	            elementBudget,
	            env,
	            false,
	            true);
}

	@Override
	public void construct(ItemStack stackSize, boolean hintsOnly) {

		buildPiece("M", stackSize, hintsOnly, 1, 1, 0);
	}

	@Override
	public AENetworkProxy getProxy() {

		if (gridProxy == null) {
			gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), true);
			gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
			gridProxy.setValidSides(EnumSet.allOf(ForgeDirection.class));
			updateValidGridProxySides();
			if (getBaseMetaTileEntity().getWorld() != null)
				gridProxy.setOwner(getBaseMetaTileEntity().getWorld()
						.getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
			setter.accept(gridProxy);
		}

		return this.gridProxy;
	}   
	
	private void updateValidGridProxySides() {
       
            getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
        
    }

    @Override
    public void onFacingChange() {
     
        super.onFacingChange();  
        updateValidGridProxySides();
    }

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		return getProxy().getNode();
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public DimensionalCoord getLocation() {

		return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
	}

	@Override
	public IGridNode getActionableNode() {
		
		return getProxy().getNode();
	}
@Override
public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
	getProxy().onReady();
	fakeCPU().getWorldObj();
	super.onFirstTick(aBaseMetaTileEntity);
}

public TileCraftingTile fakeCPU() {
	if(fakeCPU==null)
	fakeCPU=new TileCraftingTile(){
		AENetworkProxy proxy;
		{
		if(gridProxy!=null)proxy=gridProxy;//if ready, set it now
		setter=s->{proxy=s;
		//setWorldObj(((TileCPU)s.getMachine()).getBaseMetaTileEntity().getWorld());
		};//if not, set when ready
		//setWorldObj(TileCPU.this.getBaseMetaTileEntity().getWorld());
		}
		@Override
		public World getWorldObj() {
			if(worldObj==null){
				worldObj=TileCPU.this.getBaseMetaTileEntity().getWorld();
			}
			return worldObj;
		}
		public AENetworkProxy getProxy() {if(proxy!=null)return proxy;return super.getProxy();};
		public boolean isActive() {return getProxy().isActive();};
		public boolean isPowered() {return getProxy().isPowered();};
		
		
	};
	return fakeCPU;
}




long used[]=new long[3];
public int getRemOP() {
	long all= totalAcc()-used[0]-used[1]-used[2];
	return (int) Math.min(all, Integer.MAX_VALUE);
}

appeng.util.item.ItemList accCondenser=new appeng.util.item.ItemList();
appeng.util.item.ItemList acc=new appeng.util.item.ItemList();
long accCache=0;
private void updateAccCache(){
	accCache=0;
	acc.forEach(s->{
		
		Long get = accMap0.get(new SIDItemStack(s.getItemStack()));
		if(get!=null){
			accCache=accCache+get*s.getStackSize();
		}
	});
	
	
}
long accCacheCondenser=0;
private void updateCondenserCache(){
	accCacheCondenser=0;
	accCondenser.forEach(s->{
		
		Long get = accMap1.get(new SIDItemStack(s.getItemStack()));
		if(get!=null){
			accCacheCondenser=accCacheCondenser+get*s.getStackSize();
		}
	});
	
	
}
public static Map<SIDItemStack,Long> accMap0=new HashMap<>();


public static Map<SIDItemStack,Long> accMap1=new HashMap<>();


public long totalAcc() {
	
	return 1+accCache;
}

public void repRemOP(int remainingOperations, int old) {
	int usedt=old-remainingOperations;
	
	used[0]+=usedt;
	
}
static public Method addOutputPartial;
static public BiConsumer<TileCPU,ItemStack> addOutputPartialX;
static{
try{
	addOutputPartial=MTEMultiBlockBase.class.getMethod("addOutputPartial", ItemStack.class,boolean.class);

		addOutputPartialX=(a,b)->{try {
			addOutputPartial.invoke(a, b,false);
		} catch (Exception e) {
			
			e.printStackTrace();
		}};
	
}catch(Exception e){}

try{
addOutputPartial=MTEMultiBlockBase.class.getMethod("addOutputPartial", ItemStack.class);
addOutputPartialX=(a,b)->{try {
	addOutputPartial.invoke(a, b);
} catch (Exception e) {
	
	e.printStackTrace();
}};
}catch(Exception e){}
}



@Override
public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	super.onPostTick(aBaseMetaTileEntity, aTick);
	
	if(aBaseMetaTileEntity.isServerSide()==false)return;
	boolean changes=false;
	startRecipeProcessing();
	
	for(ItemStack is:getStoredInputs()){
		
		if(accMap0.get(new SIDItemStack(is))!=null){
			acc.add(AEItemStack.create(is));
			is.stackSize=0;
			updateAccCache();
			changes=true;
			//updateSlots();
		}
		
		if(accMap1.get(new SIDItemStack(is))!=null){
			accCondenser.add(AEItemStack.create(is));
			is.stackSize=0;
			updateCondenserCache();
			changes=true;
			//updateSlots();
		}
		
	}
	
	endRecipeProcessing();
	if(changes)updateSlots();
	
	refunds.forEach(s->{
		ItemStack get = s.getItemStack();
		int old=get.stackSize;
		//this.addOutputPartial(get, false);
		addOutputPartialX.accept(this, get);
		
		
		
		int eaten=old-get.stackSize;
		s.decStackSize(eaten);
		
	});
	
	this.usedAccCache=used[0]+used[1]+used[2];
	
	//System.out.println(used[0]);
	this.used[2] = this.used[1];
     this.used[1] = this.used[0];
     this.used[0]=0;
     
     //this.used[0] = started - this.remainingOperations;
	try {
		
		/*if(cluster.size()==0){
			
			
		CraftingCPUCluster c = newCCC();
		((IExternalManagerHolder)(Object)c).setStorage(64000);
		
		this.getProxy().getGrid().postEvent(new MENetworkCraftingCpuChange(this.getProxy().getNode()));
		}*/
		
		k:{
			boolean anynew=false;
			Iterator<Entry<CraftingCPUCluster, Data>> it = clusterData.entrySet().iterator();
			for(;it.hasNext();){
				Entry<CraftingCPUCluster, Data> set = it.next();

					if (set.getValue().state == 0) {
						
						if (set.getKey().isBusy()) {
							set.getValue().state = 1;
						} else
						{
							if(set.getValue().usedStorage.isEmpty()==false){
								refund(set.getValue().usedStorage);
								set.getValue().usedStorage.clear();
							}
							
							long siz=qureyStorage();
							if(set.getValue().storage!=siz){
								
								set.getValue().storage=siz;
								((IExternalManagerHolder)(Object)set.getKey()).setStorage(siz);
							}
							
							
							anynew = true;}
					}
				
				
				if(set.getValue().state==1){
					if(!set.getKey().isBusy()){
						if(set.getKey().getInventory().getAvailableItems(new appeng.util.item.ItemList()).isEmpty()){
					
						//cluster.remove(set.getKey());
						refund(set.getValue().usedStorage);
						set.getValue().usedStorage.clear();
						it.remove();
						set.getKey().destroy();
						this.getProxy().getGrid().postEvent(new MENetworkCraftingCpuChange(this.getProxy().getNode()));
						}
					}
					
				}
				
			}	
			
			if(anynew)break k;
			long get=qureyStorage();
			CraftingCPUCluster c = newCCC();
			((IExternalManagerHolder)(Object)c).setStorage(get);
			Data d;
			clusterData.put(c, d=new Data());
			d.storage=get;
			this.getProxy().getGrid().postEvent(new MENetworkCraftingCpuChange(this.getProxy().getNode()));
			
		}
		
		
		
		
	} catch (GridAccessException e) {

	}

	
}
appeng.util.item.ItemList refunds=new appeng.util.item.ItemList();
private void refund(appeng.util.item.ItemList usedStorage) {
	usedStorage.forEach(s->refunds.add(s));
}


public static Map<SIDItemStack,Long> accMap=new HashMap<>();
static {init();}
public static synchronized void init(){
	accMap.clear();
	accMap0.clear();
	accMap1.clear();
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage1k().maybeStack(1).get()), 1024l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage4k().maybeStack(1).get()), 4096l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage16k().maybeStack(1).get()), 4096*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage64k().maybeStack(1).get()), 4096*4*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage256k().maybeStack(1).get()), 4096*4*4*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage1024k().maybeStack(1).get()), 4096*4*4*4*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage4096k().maybeStack(1).get()), 4096*4*4*4*4*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorage16384k().maybeStack(1).get()), 4096*4*4*4*4*4*4l);
	accMap.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingStorageSingularity().maybeStack(1).get()), Long.MAX_VALUE*1l-1);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator().maybeStack(1).get()), 1*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator4x().maybeStack(1).get()), 4*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator16x().maybeStack(1).get()), 16*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator64x().maybeStack(1).get()), 64*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator256x().maybeStack(1).get()), 256*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator1024x().maybeStack(1).get()), 1024*1L);
	accMap0.put(new SIDItemStack(AEApi.instance().definitions().blocks().craftingAccelerator4096x().maybeStack(1).get()), 4096*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[0])), 1*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[1])), 4*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[2])), 16*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[3])), 64*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[4])), 256*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[5])), 1024*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[6])), 4096*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[7])), 16384*1L);
	accMap1.put(new SIDItemStack(new ItemStack(MyMod.condensers[8])), Integer.MAX_VALUE*1L);
}
public static long safeFMA(long original,long a,long b){
	
	try {
        long add= Math.multiplyExact(a, b);
        
        return Math.min(Math.addExact(add, original),Long.MAX_VALUE-1);
    } catch (ArithmeticException e) {
        return Long.MAX_VALUE-1;
    }
	
}
public long qureyStorage() {
	long all=0;
	startRecipeProcessing();
	
	ArrayList<ItemStack> in = getStoredInputs();
	
	for(ItemStack item:in){
		
		Long acc=accMap.get(new SIDItemStack(item));
		if(acc!=null){
			//all+=acc*item.stackSize;
			all=safeFMA(all,acc,item.stackSize);
		}
	}
	endRecipeProcessing();
	
	
	return all;
}

public boolean  useStorage(CraftingCPUCluster craftingCPUCluster, long given, long job) {
	startRecipeProcessing();
	ArrayList<ItemStack> in = getStoredInputs();
	boolean hasSingular=false;
	HashMultimap<Long,ItemStack> m=HashMultimap.create();
	Map<Long,Integer> mm=new HashMap<>();
	for(ItemStack item:in){
		Long acc=accMap.get(new reobf.proghatches.util.SIDItemStack(item));
		if(acc!=null){
			if(acc>=Long.MAX_VALUE-1)hasSingular=true;
			m.put(acc, item);
			mm.put(acc, item.stackSize+mm.getOrDefault(acc, 0));
		}
	}
	
	Map<Long, Integer> result = select0(mm,job);
	if(result==null)return false;
	
	for( Entry<Long, Integer> used:result.entrySet()){
		
		int toremove = used.getValue();
		for(ItemStack is:m.get(used.getKey())){
			int toremove_thistime=Math.min(is.stackSize, toremove);
			
			if(!hasSingular)
			clusterData.get(craftingCPUCluster).usedStorage.add(AEItemStack.create(
					is
					).setStackSize(toremove_thistime)
					);
			
			toremove-=toremove_thistime;
			if(!hasSingular)
			is.stackSize-=toremove_thistime;
			if(toremove<=0)break;
		}
		
		
		/*for(ItemStack item:m.get(used.getKey()))
		clusterData.get(craftingCPUCluster).usedStorage.add(AEItemStack.create(
				item.copy()//m.get(used.getKey()).copy()
				).setStackSize(used.getValue())
				);
		*/
		
		
	}	
	
	
	long get=result.entrySet().stream()
			.mapToLong(s->s.getKey()*s.getValue()).sum();
	
	((IExternalManagerHolder)(Object)craftingCPUCluster).setStorage(get
			);
	clusterData.get(craftingCPUCluster).storage=get;
	endRecipeProcessing();	
	updateSlots();
	
	
	
	
	
	return true;
}
public static Map<Long, Integer> select0(Map<Long, Integer> map, long goal) {
    if (goal < 0) {
        goal = 0;
    }
    
    Map<Long, Integer> result = new HashMap<>();
    Map<Long, Integer> remaining = new HashMap<>(map);
    
    if (remaining.isEmpty()) {
        return null;
    }
    
    List<Long> descCoins = new ArrayList<>(remaining.keySet());
    Collections.sort(descCoins, Collections.reverseOrder());
    
    long currentTotal = 0;
    
    for (Long coin : descCoins) {
        int count = remaining.getOrDefault(coin, 0);
        if (count == 0) {
            continue;
        }
        long maxTake = (goal - currentTotal) / coin;
        if (maxTake > 0) {
            int take = (int) Math.min(count, maxTake);
            result.put(coin, result.getOrDefault(coin, 0) + take);
            remaining.put(coin, count - take);
            currentTotal += take * coin;
        }
    }
    
    if (currentTotal < goal) {
        List<Long> ascCoins = new ArrayList<>(remaining.keySet());
        Collections.sort(ascCoins);
        for (Long coin : ascCoins) {
            int count = remaining.getOrDefault(coin, 0);
            while (count > 0 && currentTotal < goal) {
                result.put(coin, result.getOrDefault(coin, 0) + 1);
                remaining.put(coin, count - 1);
                currentTotal += coin;
                if(currentTotal<0)currentTotal=Long.MAX_VALUE;
                count--;
                if (currentTotal >= goal) {
                    break;
                }
            }
            if (currentTotal >= goal) {
                break;
            }
        }
    }
    
    if (currentTotal < goal) {
        return null;
    }
    
    return result;
}

public boolean isOn() {

	return this.isAllowedToWork();
}
@Override
public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
		ItemStack aTool) {
	
	
	if(clusterData.size()==1&&clusterData.keySet().iterator().next().isBusy()==false){
		
		
		boolean any=false;
	if(!acc.iterator().hasNext())
	{}
	else{
		any=true;
	
	refund(acc);
	acc.clear();
	}
		
	if(!accCondenser.iterator().hasNext())
	{}
	else{
		any=true;
	
	refund(accCondenser);
	accCondenser.clear();
	}
	
	if(!any)
	aPlayer.addChatComponentMessage(new ChatComponentText("Nothing to refund."));
	else
	aPlayer.addChatComponentMessage(new ChatComponentText("Refunded."));
	
	updateAccCache();
	updateCondenserCache();
		}
		
		
	
	
	
	
	super.onScrewdriverRightClick(side, aPlayer, aX, aY, aZ, aTool);
}

@Override
public void addUIWidgets(com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder builder,
		UIBuildContext buildContext) {
	
	super.addUIWidgets(builder, buildContext);
	builder.widgets(new FakeSyncWidget.LongSyncer(()->this.accCache, s->this.accCache=s).setSynced(true, false));
	builder.widgets(new FakeSyncWidget.LongSyncer(()->this.usedAccCache, s->this.usedAccCache=s).setSynced(true, false));
	builder.widgets(new FakeSyncWidget.LongSyncer(()->this.accCacheCondenser, s->this.accCacheCondenser=s).setSynced(true, false));

}
long usedAccCache;
protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {

    super.drawTexts(screenElements, inventorySlot);
    screenElements.setSpace(0);
    screenElements.setPos(0, 0);
    // make it look same on 2.7.2-
    // 2.7.2- set it to a non zero value
    screenElements.widget(new TextWidget().setStringSupplier(
    		() -> "Accelerators in use/total:"+usedAccCache+"/"+accCache

    )
        .setDefaultColor(COLOR_TEXT_WHITE.get()));
    screenElements.widget(new TextWidget().setStringSupplier(
    		() -> "Dumper:"+accCacheCondenser

    )
        .setDefaultColor(COLOR_TEXT_WHITE.get()).setEnabled(s->accCacheCondenser>0));
  

}

public long getCondenser() {
	
	return accCacheCondenser;
}



String myName="Auto CPU";
public String getName() {
	return myName;
}


@Override
public String getCustomName() {
	
	return myName;
}

@Override
public boolean hasCustomName() {
	
	return !myName.isEmpty();
}

@Override
public void setCustomName(String name) {
	myName=name;
	
}

@Override
public void onBlockDestroyed() {

	clusterData.keySet().forEach(s->{
		 final IItemList list;
	        s.getListOfItem(list = AEApi.instance().storage().createItemList(), CraftingItemList.ALL);
	      
		
	        list.forEach(ss->{drop((IAEItemStack) ss);});
	});	
	
	
	
	super.onBlockDestroyed();
}


private void drop(IAEItemStack ss) {

	
}
@Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
        float aX, float aY, float aZ) {
        final ItemStack is = aPlayer.inventory.getCurrentItem();
        if (is != null && is.getItem() instanceof ToolQuartzCuttingKnife) {
            if (ForgeEventFactory.onItemUseStart(aPlayer, is, 1) <= 0) return false;
            IGregTechTileEntity te = getBaseMetaTileEntity();
            aPlayer.openGui(
                AppEng.instance(),
                GuiBridge.GUI_RENAMER.ordinal() << 5 | (side.ordinal()),
                te.getWorld(),
                te.getXCoord(),
                te.getYCoord(),
                te.getZCoord());
            return true;
        }
        return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
    } 
@Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
       
        return (d, r, f) -> true;//d.offsetY == 0 && r.isNotRotated() && !f.isVerticallyFliped();
    }


protected void onCorrectCasingAdded() {
        
		mCasingAmount++;
    }int mCasingAmount;   
    protected IStructureElement getCasingElement() {
		return ofBlock(GregTechAPI.sBlockReinforced, 2);
	}
    @Override
    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
    		int z) {
    	
    	NBTTagList l=new NBTTagList(); 
    	tag.setTag("cpu", l);
    	for(Entry<CraftingCPUCluster, Data> a:clusterData.entrySet()){
    		NBTTagCompound neo=new NBTTagCompound();
    		neo.setLong("storage", a.getValue().storage);
    		neo.setInteger("isFree", a.getValue().state);
    		
    		if(a.getKey().getFinalOutput()!=null){
	    		NBTTagCompound t;
	    		a.getKey().getFinalOutput().writeToNBT(t=new NBTTagCompound());
	    		neo.setTag("item",t );
    		}
    		CraftingCPUCluster cpu = a.getKey();
    		 double craftingPercentage = (double) (cpu.getStartItemCount() - Math.max(cpu.getRemainingItemCount(), 0))
                     / (double) cpu.getStartItemCount();
    		neo.setDouble("percentage", craftingPercentage);
    		
    		
    		
    		//neo.setLong("t", a.getKey().getElapsedTime());
    		//neo.setLong("s", a.getKey().getStartItemCount());
    		//neo.setLong("r", a.getKey().getRemainingItemCount());
    		l.appendTag(neo);
    	}
    	
    	
    	super.getWailaNBTData(player, tile, tag, world, x, y, z);
    } static DecimalFormat df = new DecimalFormat("0.0%");
    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
    		IWailaConfigHandler config) {
 
    	
    	NBTTagList l=(NBTTagList) accessor.getNBTData().getTag("cpu");
    	if(l!=null){
	    	int index=0;
	    	for(int i=0;i<l.tagCount();i++){
	    		NBTTagCompound get=l.getCompoundTagAt(i);
	    		if(get.getInteger("isFree")==0)
	    	currentTip.add("Free"+":"+Platform.formatByteDouble(get.getLong("storage")));
	    		
	    			else
	    			{index++;
	    		IAEItemStack is = AEItemStack.loadItemStackFromNBT(
	    		get.getCompoundTag("item"));
	    		String ex=is==null?"":" crafting:"+is.getItemStack().getDisplayName()+(is.getStackSize()==1?"":("x"+is.getStackSize()+""));	
	    		
	    		String ex2=" "+df.format(get.getDouble("percentage"));
	    		
	    		currentTip.add("CPU"+index+":"+Platform.formatByteDouble(get.getLong("storage"))+" "+ex+ex2);
	    			
	    		
	    				
	    		
	    			
	    			
	    			
	    			}
	    	}
    	}
    	
    	
    	
    	
    	super.getWailaBody(itemStack, currentTip, accessor, config);
    }  
  
}
