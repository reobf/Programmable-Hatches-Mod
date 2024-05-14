package reobf.proghatches.gt.metatileentity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.isAir;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofChain;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;
import static gregtech.api.enums.GT_HatchElement.Energy;
import static gregtech.api.enums.GT_HatchElement.InputBus;
import static gregtech.api.enums.GT_HatchElement.InputHatch;
import static gregtech.api.enums.GT_HatchElement.Maintenance;
import static gregtech.api.enums.GT_HatchElement.OutputBus;
import static gregtech.api.enums.GT_HatchElement.OutputHatch;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.IAlignment;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.alignment.enumerable.Flip;
import com.gtnewhorizon.structurelib.alignment.enumerable.Rotation;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElementChain;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.core.Api;
import appeng.core.api.definitions.ApiBlocks;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.TierEU;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.IGT_HatchAdder;
import gregtech.api.util.shutdown.ShutDownReason;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_DistillationTower;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.IInstantCompletable;
import reobf.proghatches.gt.metatileentity.ICircuitProvider;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.ProviderChainer;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class LargeProgrammingCircuitProvider extends GT_MetaTileEntity_EnhancedMultiBlockBase<LargeProgrammingCircuitProvider>  implements ISurvivalConstructable, IGridProxyable, ICraftingProvider,IInstantCompletable,ICircuitProvider{

	public LargeProgrammingCircuitProvider(String aName) {
		super(aName);
	
	}
	public LargeProgrammingCircuitProvider(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
		   Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
		
	}
	@Override
    public void onFacingChange() {
        updateValidGridProxySides();
        super.onFacingChange();
    }
	@Override
	public AECableType getCableConnectionType(ForgeDirection forgeDirection) {
		
		return AECableType.DENSE;
	}
	boolean forceUpdatePattern;
	@Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
		forceUpdatePattern=true;
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
    }
	public List<ICircuitProvider> providers=new ArrayList<>();
	
	protected static final int CASING_INDEX = 49;
	private static final IStructureDefinition<LargeProgrammingCircuitProvider> STRUCTURE_DEFINITION;
	   protected static final String STRUCTURE_PIECE_BASE = "base";
	    protected static final String STRUCTURE_PIECE_LAYER = "layer";
	    protected static final String STRUCTURE_PIECE_LAYER_HINT = "layerHint";
	    protected static final String STRUCTURE_PIECE_TOP_HINT = "topHint";
    static {
    	Function<IGT_HatchAdder<? super LargeProgrammingCircuitProvider>,IHatchElement<LargeProgrammingCircuitProvider>> provider=s->new IHatchElement<LargeProgrammingCircuitProvider>(){
    		@Override
			public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
				return ImmutableList.of(
						ProgrammingCircuitProvider.class,
						ProviderChainer.class);
			}
    		@Override
			public IGT_HatchAdder<? super LargeProgrammingCircuitProvider> adder() {
    			
				return s;
			}

			@Override
			public String name() {
			
				return "providerAdder";
			}

			@Override
			public long count(LargeProgrammingCircuitProvider t) {
			
				return t.providers.size();
			}};

			IHatchElement<LargeProgrammingCircuitProvider> providerSide=provider.apply(LargeProgrammingCircuitProvider::addProvider);
			IHatchElement<LargeProgrammingCircuitProvider> providerTop=provider.apply(LargeProgrammingCircuitProvider::addProviderTop);
			
			
        STRUCTURE_DEFINITION = StructureDefinition.<LargeProgrammingCircuitProvider>builder()
            .addShape(STRUCTURE_PIECE_BASE, /*transpose*/(new String[][] { { "bbb", "b~b", "bbb" }, }))
            .addShape(STRUCTURE_PIECE_LAYER, /*transpose*/(new String[][] { { "lhl", "hxh", "lhl" }, }))
            .addShape(STRUCTURE_PIECE_LAYER_HINT, /*transpose*/(new String[][] { { "lhl", "hXh", "lhl" }, }))
            .addShape(STRUCTURE_PIECE_TOP_HINT, /*transpose*/(new String[][] { { "lll", "lll", "lll" }, }))
            .addElement(
                'b',
                ofChain(
                    buildHatchAdder(LargeProgrammingCircuitProvider.class)
                        .atLeast(Energy, Maintenance)
                        .casingIndex(CASING_INDEX)
                        .dot(1)
                        
                        .build(),
                    //ofBlock(GregTech_API.sBlockCasings4, 1),
                    onElementPass(
                    		LargeProgrammingCircuitProvider::onCasingFound,
                        ofBlock(GregTech_API.sBlockCasings4, 1))
                    )
                
            		)
            .addElement(
            	'l',
            	ofBlock(GregTech_API.sBlockCasings4, 1))
            .addElement(
                'x',
               (IStructureElementChain<LargeProgrammingCircuitProvider>)
            ()->{
            	return
               	 buildHatchAdder(LargeProgrammingCircuitProvider.class).atLeast(providerTop)
                     .casingIndex(CASING_INDEX).dot(2)
                     .buildAndChain(
                    		 ofBlock(Api.INSTANCE.definitions().blocks().fluix().maybeBlock().get(), 0),
                         	StructureUtility.ofBlockAdder(LargeProgrammingCircuitProvider::onTopCenterFound,GregTech_API.sBlockCasings4, 1)
                     )    .fallbacks();
                    		 
            }
                
            		)
            .addElement(
                    'X',
                   ofBlock(Api.INSTANCE.definitions().blocks().fluix().maybeBlock().get(), 0)
               )
            .addElement(
                'h',
                   
                    		 buildHatchAdder(LargeProgrammingCircuitProvider.class).atLeast(providerSide)
                             .casingIndex(CASING_INDEX)
                             .dot(2)
                             //.disallowOnly(ForgeDirection.UP, ForgeDirection.DOWN)
                             .buildAndChain(GregTech_API.sBlockCasings4, 1)
                    
                )
            
            
            
            /*
            .addElement(
                'l',
                ofChain(
                    buildHatchAdder(LargeProgrammingCurcuitProvider.class).atLeast(layeredOutputHatch)
                        .casingIndex(CASING_INDEX)
                        .dot(2)
                        .disallowOnly(ForgeDirection.UP, ForgeDirection.DOWN)
                        .build(),
                    ofHatchAdder(LargeProgrammingCurcuitProvider::addEnergyInputToMachineList, CASING_INDEX, 2),
                    ofHatchAdder(LargeProgrammingCurcuitProvider::addLayerOutputHatch, CASING_INDEX, 2),
                    ofHatchAdder(LargeProgrammingCurcuitProvider::addMaintenanceToMachineList, CASING_INDEX, 2),
                    onElementPass(
                    		LargeProgrammingCurcuitProvider::onCasingFound,
                        ofBlock(GregTech_API.sBlockCasings4, 1))))
            // hint element only used in top layer
            .addElement(
                'L',
                buildHatchAdder(LargeProgrammingCurcuitProvider.class).atLeast(n)
                    .casingIndex(CASING_INDEX)
                    .dot(2)
                    .disallowOnly(ForgeDirection.UP)
                    .buildAndChain(GregTech_API.sBlockCasings4, 1))
            .addElement(
                'c',
                ofChain(
                    onElementPass(
                        t -> t.onTopLayerFound(false),
                        ofHatchAdder(LargeProgrammingCurcuitProvider::addOutputToMachineList, CASING_INDEX, 3)),
                    onElementPass(
                        t -> t.onTopLayerFound(false),
                        ofHatchAdder(
                            LargeProgrammingCurcuitProvider::addMaintenanceToMachineList,
                            CASING_INDEX,
                            3)),
                    onElementPass(t -> t.onTopLayerFound(true), ofBlock(GregTech_API.sBlockCasings4, 1)),
                    isAir()))*/
            .build();
    }  
    int mCasing;
    protected void onCasingFound() {
        
		mCasing++;
    }  
    boolean mTopLayerFound;
    protected void onTopLayerFound(boolean aIsCasing) {
    	mTopLayerFound= true;
        if (aIsCasing) onCasingFound();
    }
	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new LargeProgrammingCircuitProvider(mName);
	}

	

	@Override
	public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
			int colorIndex, boolean active, boolean redstoneLevel) {
		
		return new ITexture[] { BlockIcons.getCasingTextureForId(CASING_INDEX)};
	}
	
	@Override
	protected GT_Multiblock_Tooltip_Builder createTooltip() {
		   final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
	      /*  tt.addMachineType("Programming Circuit Provider")
	            .addInfo("Controller block for the Large Programming Circuit Provider")
	            .addInfo("The correct height equals the slot number in the NEI recipe")
	            .addSeparator()
	            .beginVariableStructureBlock(3, 3, 2, 12, 3, 3, false)
	            .addController("Front center")
	            .addOtherStructurePart("Clean Stainless Steel Machine Casing", "7 x h - 5 (minimum)")
	            .addOtherStructurePart("Fluix Block", "Center of each layer except the first and last one")
	            .addOtherStructurePart("Programming Circuit Provider", "Any casing that is adjacent to Fluix Block")
		        .addEnergyHatch("Any front layer casing", 1)
	            .addMaintenanceHatch("Any front layer casing", 1)
	            .toolTipFinisher("ProgrammableHatches");*/
	        Config.get(tt, "M_LPCP");
	       
	        return tt;
	}
	@Override
	public boolean isCorrectMachinePart(ItemStack aStack) {
		return true;
	}
	 @Override
	    public boolean isRotationChangeAllowed() {
	        return false;
	    }

	    @Override
	    public IStructureDefinition<LargeProgrammingCircuitProvider> getStructureDefinition() {
	        return STRUCTURE_DEFINITION;
	    }
	    int mHeight;
	    @Override
	    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
	        // reset
	       
	        mHeight = 1;
	        mTopLayerFound = false;
	        mCasing = 0;

	        // check base
	        if (!checkPiece(STRUCTURE_PIECE_BASE, 1, 1, 0)) return false;

	        // check each layer
	        while (mHeight < 12) {
	        	providerFoundThisLayer=false;
	            if (!checkPiece(STRUCTURE_PIECE_LAYER, 1,1, -mHeight)) {
	                return false;
	            }
	          
	            if (mTopLayerFound) {
	            	if(providerFoundThisLayer){return false;}//no providers allowed on top layer!
	                break;
	            }
	           
	            mHeight++;
	        }

	        // validate final invariants... (actual height is mHeight+1)
	        return// mCasing >= 7 * (mHeight + 1) - 5 && mHeight + 1 >= 3&&
	            mTopLayerFound
	            && mMaintenanceHatches.size() == 1;
	    }

	    
	    
	public int getMaxEfficiency(ItemStack aStack) {
		return 10000;
	}
	@Override
	public int getDamageToComponent(ItemStack aStack) {
		return 0;
	}
	@Override
	public boolean explodesOnComponentBreak(ItemStack aStack) {
		return false;
	}
	

	boolean providerFoundThisLayer;
	static boolean onTopCenterFound(LargeProgrammingCircuitProvider t, Block block, int meta) {
		if(block==GregTech_API.sBlockCasings4&&meta==1){
			t.mTopLayerFound=true;
			return true;
		}
		
		
		
		
		return false;
	}

	static boolean addProvider(LargeProgrammingCircuitProvider t,  IGregTechTileEntity aTileEntity, Short text) {
		 return addProviderWithUpdater(t, aTileEntity, text, tt->tt.providerFoundThisLayer=true);
	}
	
	static boolean addProviderTop(LargeProgrammingCircuitProvider t,  IGregTechTileEntity aTileEntity, Short text) {
		  return addProviderWithUpdater(t, aTileEntity, text, tt->tt.mTopLayerFound=true);
	}
	
	static private<T extends GT_MetaTileEntity_Hatch&ICircuitProvider> boolean addProviderWithUpdater(LargeProgrammingCircuitProvider t,  IGregTechTileEntity aTileEntity, Short text,Consumer<LargeProgrammingCircuitProvider> cb){
		
		  if (aTileEntity == null) return false;
	        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
	        if (aMetaTileEntity == null) return false;
	        if (aMetaTileEntity instanceof ICircuitProvider) {
	        	@SuppressWarnings("unchecked")
				T hatch=(T) aMetaTileEntity;
	            Optional.ofNullable(text).ifPresent(hatch::updateTexture);
	        	hatch.updateCraftingIcon(t.getMachineCraftingIcon());
	        	cb.accept(t);
	        	if(hatch instanceof ProgrammingCircuitProvider)
	        	((ProgrammingCircuitProvider) hatch).disable();
	            t.providers.add(hatch);
	           
	        	return true;
	        }
	        return false;
		
	}
	
	@Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_BASE, stackSize, hintsOnly, 1, 1, 0);
        int tTotalHeight = Math.min(12, stackSize.stackSize + 2); // min 2 output layer, so at least 1 + 2 height
        for (int i = 1; i < tTotalHeight - 1; i++) {
            buildPiece(STRUCTURE_PIECE_LAYER_HINT, stackSize, hintsOnly, 1, 1, -i);
        }
        buildPiece(STRUCTURE_PIECE_TOP_HINT, stackSize, hintsOnly, 1, 1, -(tTotalHeight - 1));
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        mHeight = 0;
        int built = survivialBuildPiece(STRUCTURE_PIECE_BASE, stackSize, 1, 1, 0, elementBudget, env, false, true);
        if (built >= 0) return built;
        int tTotalHeight = Math.min(12, stackSize.stackSize + 2); // min 2 output layer, so at least 1 + 2 height
        for (int i = 1; i < tTotalHeight - 1; i++) {
            mHeight = i;
            built = survivialBuildPiece(
                STRUCTURE_PIECE_LAYER_HINT,
                stackSize,
                1,
                1,
                -i,
                elementBudget,
                env,
                false,
                true);
            if (built >= 0) return built;
        }
        mHeight = tTotalHeight - 1;
        return survivialBuildPiece(
            STRUCTURE_PIECE_TOP_HINT,
            stackSize,
            1,
            1,
           -( tTotalHeight - 1),
           
            elementBudget,
            env,
            false,
            true);
    }

    @Nonnull
    @Override
    public CheckRecipeResult checkProcessing() {
        mEfficiencyIncrease = 100;
        final long inputVoltage = getMaxInputVoltage();

        // only allow LV+ energy hatches
        if (inputVoltage < TierEU.LV) {
            return CheckRecipeResultRegistry.insufficientPower(40);
        }

        // use the standard overclock mechanism to determine duration and estimate a maximum consumption
        // if the cleanroom is powered by an LV energy hatch, it will actually accept 2A instead of just 1A.
        calculateOverclockedNessMultiInternal(
            40,
            45 * Math.max(1, mHeight - 1),
            inputVoltage == TierEU.LV ? 2 : 1,
            inputVoltage,
            false);
        // negate it to trigger the special energy consumption function. divide by 10 to get the actual final
        // consumption.
        mEUt /= -10;
        return SimpleCheckRecipeResult.ofSuccess("proghatches.largepcp.running");
    }
    @Override
    public void clearHatches() {
    	this.providers.clear();
    	
    	super.clearHatches();
    }  
    AENetworkProxy gridProxy;
    @Override
    public AENetworkProxy getProxy() {

      
		if (gridProxy == null) {
            gridProxy = new AENetworkProxy(this, "proxy", ItemList.Hatch_CraftingInput_Bus_ME.get(1), true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }

        return this.gridProxy;
    }
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		 return getProxy().getNode();
	}
	@Override
	public void securityBreak() {
	
		
	}
	 private void updateValidGridProxySides() {

	        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));

	    }

	  @Override
	    public DimensionalCoord getLocation() {
	        return new DimensionalCoord(
	            getBaseMetaTileEntity().getWorld(),
	            getBaseMetaTileEntity().getXCoord(),
	            getBaseMetaTileEntity().getYCoord(),
	            getBaseMetaTileEntity().getZCoord());
	    }
	  @Override
	public void saveNBTData(NBTTagCompound aNBT) {
		  getProxy().writeToNBT(aNBT); int[] count = new int[1];
	        toReturn.forEach(s -> aNBT.setTag("toReturn" + (count[0]++), s.writeToNBT(new NBTTagCompound())));
	        aNBT.setInteger("cacheState", cacheState.ordinal());
		super.saveNBTData(aNBT);
	}
	  @Override
	public void loadNBTData(NBTTagCompound aNBT) {
		  getProxy().readFromNBT(aNBT);
		  toReturn.clear();
	        int[] count = new int[1];
	        NBTTagCompound c;
	        while ((c = (NBTTagCompound) aNBT.getTag("toReturn" + (count[0]++))) != null) {
	            toReturn.add(ItemStack.loadItemStackFromNBT(c));
	        }
	        cacheState=CacheState.values()[aNBT.getInteger("cacheState")];
		super.loadNBTData(aNBT);
	}
	  
	  
	  ArrayList<ItemStack> toReturn = new ArrayList<>();
	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		try {
            if (ItemProgrammingCircuit.getCircuit(patternDetails.getOutputs()[0].getItemStack())
                .map(ItemStack::getItem)
                .orElse(null) == MyMod.progcircuit) {
              this.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
                return false;
            }

        } catch (Exception e) {}

        try {
            this.getProxy()
                .getEnergy()
                .extractAEPower(10, Actionable.MODULATE, PowerMultiplier.ONE);
        } catch (GridAccessException e) {

        }
        ItemStack circuitItem = (patternDetails.getOutput(
            table,
            this.getBaseMetaTileEntity()
                .getWorld()));
        toReturn.add((circuitItem));

        return true;
	}
	@Override
	public boolean isBusy() {
		
		return false;
	} 
	private IStorageGrid getStorageGrid() {
        try {
            return this.getProxy()
                .getGrid()
                .getCache(IStorageGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }
	
	  final private  int ran = (int) (Math.random() * 20);
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.onPostTick(aBaseMetaTileEntity, aTick);
		returnItems();
		
	
		if( getBaseMetaTileEntity().isActive()){
			if(cacheState==CacheState.POWEROFF||cacheState==CacheState.CRASH){
				cacheState=CacheState.UPDATED;
				forceUpdatePattern=true;
			}
			if(cacheState.shouldCheck()&&mMachine){
				if(aTick%20==ran){
				boolean any=false;
				for(ICircuitProvider p:providers){
					if(p.patternDirty()){
						any=true;
						p.clearDirty();
					}
					
				}
				if(any||forceUpdatePattern){
					forceUpdatePattern=false;
					cacheState=CacheState.DIRTY;
					postEvent();}
				}
		     }
			
			
			
			
		}else{
			
			patternCache.clear();
			cacheState=CacheState.POWEROFF;
			postEvent();
			
		}
		
		
		
		
		
	}
	 public void returnItems(){
    	 toReturn.replaceAll(
       		  s -> Optional.ofNullable(
       		  getStorageGrid().getItemInventory()
                 .injectItems(
                     AEItemStack.create(s),
                     Actionable.MODULATE,
                     new MachineSource((IActionHost) getBaseMetaTileEntity()))
                 )
       		  .filter(ss->ss.getStackSize()<=0)
       		  .map(ss->ss.getItemStack())
       		  .orElse(null)
       		);
       toReturn.removeIf(Objects::isNull);
    	
    	
    }
	 List<ItemStack> patternCache=new ArrayList<>();
	 enum CacheState{
		 DIRTY(false),
		 FRESHLY_UPDATED(true),
		 UPDATED(true),
		 CRASH(false),
		 POWEROFF(false)
		
		 
		 ;  
		 boolean shouldCheck;
		 CacheState(boolean shouldCheck){
			 this.shouldCheck=shouldCheck;
		 }
		
		public boolean shouldCheck() {
			return shouldCheck;
		}
		 
	 }
	 CacheState cacheState=CacheState.POWEROFF;
	 
	 
	 private HashSet<Object> reusable=new HashSet<>();
	 public void updateCache(){
		 patternCache.clear();
		 
		 if(!checkLoop(reusable)){
			 cacheState=CacheState.CRASH;
			  this.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
			 reusable.clear();
			 return;};
		 
		 reusable.clear();
		 providers.forEach(s->patternCache.addAll(s.getCircuit()));
	 }
	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		if(cacheState==CacheState.DIRTY){
			updateCache();
			cacheState=CacheState.FRESHLY_UPDATED;
		}	
		if(cacheState==CacheState.FRESHLY_UPDATED||cacheState==CacheState.UPDATED)
		patternCache.forEach( s->craftingTracker
             .addCraftingOption(this, new CircuitProviderPatternDetial(s)));

		
	}
     public  boolean checkLoop(HashSet<Object> blacklist){
		for(ICircuitProvider p:providers){
			if(!p.checkLoop(blacklist)){return false;};
		}
		return blacklist.add(this);
		
	};
	@Override
	public void complete() {
		   returnItems();
		
	}
	 private boolean postEvent() {
	    	
	        try {
	            this.getProxy()
	                .getGrid()
	                .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
	            return true;
	        } catch (GridAccessException ignored) {
	            return false;
	        }
	    }
	
	@Override
	public void clearDirty() {
	if(cacheState==CacheState.FRESHLY_UPDATED)
		cacheState=CacheState.UPDATED;
	}
	@Override
	public boolean patternDirty() {
	
		return cacheState==CacheState.FRESHLY_UPDATED;
	}
	@Override
	public Collection<ItemStack> getCircuit() {
	
		return patternCache;
	}

}
