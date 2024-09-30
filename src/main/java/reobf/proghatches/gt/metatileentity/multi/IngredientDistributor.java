package reobf.proghatches.gt.metatileentity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.AutoPlaceEnvironment;
import com.gtnewhorizon.structurelib.structure.IItemSource;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.IStructureElement;
import com.gtnewhorizon.structurelib.structure.IStructureElementChain;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureUtility;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.IWidgetBuilder;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_HatchElement;
import gregtech.api.enums.Materials;
import gregtech.api.enums.SoundResource;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.enums.VoidingMode;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.IHatchElement;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.fluid.IFluidStore;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.SimpleCheckRecipeResult;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_StructureUtility;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.IGT_HatchAdder;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_OutputBus_ME;
import gregtech.common.tileentities.machines.GT_MetaTileEntity_Hatch_Output_ME;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.CommunicationPortHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;


public class IngredientDistributor extends GT_MetaTileEntity_EnhancedMultiBlockBase<IngredientDistributor>
implements ISurvivalConstructable {
	public IngredientDistributor(String aName) {
		super(aName);
	
	}
	 public static <T> IStructureElement<T> ofFrameAdder(Materials aFrameMaterial,Consumer<T> onadded) {
		IStructureElement<Object> frame = GT_StructureUtility.ofFrame(aFrameMaterial); 
		 
		 return new IStructureElement<T>() {

			@Override
			public boolean check(T t, World world, int x, int y, int z) {
			boolean b=frame.check(t, world, x, y, z);
			if(b)onadded.accept(t);
				return b;
			}
			@Override
			public com.gtnewhorizon.structurelib.structure.IStructureElement.BlocksToPlace getBlocksToPlace(T t,
					World world, int x, int y, int z, ItemStack trigger, AutoPlaceEnvironment env) {
			
				return frame.getBlocksToPlace(t, world, x, y, z, trigger, env);
			}
			@Override
			public com.gtnewhorizon.structurelib.structure.IStructureElement.PlaceResult survivalPlaceBlock(T t,
					World world, int x, int y, int z, ItemStack trigger, AutoPlaceEnvironment env) {
			
				return frame.survivalPlaceBlock(t, world, x, y, z, trigger, env);
			}
			@SuppressWarnings("deprecation")
			@Override
			public com.gtnewhorizon.structurelib.structure.IStructureElement.PlaceResult survivalPlaceBlock(T t,
					World world, int x, int y, int z, ItemStack trigger, IItemSource s, EntityPlayerMP actor,
					Consumer<IChatComponent> chatter) {
			
				return frame.survivalPlaceBlock(t, world, x, y, z, trigger, s, actor, chatter);
			}
			@Override
			public boolean spawnHint(T t, World world, int x, int y, int z, ItemStack trigger) {
				
				return frame.spawnHint(aFrameMaterial, world, x, y, z, trigger);
			}

			@Override
			public boolean placeBlock(T t, World world, int x, int y, int z, ItemStack trigger) {
				
				return frame.placeBlock(aFrameMaterial, world, x, y, z, trigger);
			}
		};
		 
	 }
	public static boolean addDual(IngredientDistributor thiz,IGregTechTileEntity aTileEntity, short aBaseCasingIndex){
		 if (aTileEntity == null) return false;
	        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
	        if (aMetaTileEntity == null) return false;
	        if (aMetaTileEntity instanceof IDualInputHatch ) {
	        	IDualInputHatch hatch=(IDualInputHatch) aMetaTileEntity;
	            hatch.updateTexture(aBaseCasingIndex);
	            hatch.updateCraftingIcon(thiz.getMachineCraftingIcon());
	            if(hatch instanceof DualInputHatch){
	            	((DualInputHatch) hatch).setFilter(null);
	            }
	          
	            return thiz.mDualInputHatches.add(hatch);
	        }
	        return false;
		
		
	}
	CommunicationPortHatch port;
	boolean allMEHatch=true;
	boolean hasHatchThisLayer;
	boolean hasBusThisLayer;
	@Override
	public void clearHatches() {
		allMEHatch=true;
		super.clearHatches();
	}
	@Override
	public boolean addOutputBusToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
	boolean ok= super.addOutputBusToMachineList(aTileEntity, aBaseCasingIndex);
	if(ok&&!hasBusThisLayer){hasBusThisLayer=true;}else{return false;}
	if(ok&&
	!(aTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_Hatch_OutputBus_ME)){
		allMEHatch=false;
	}
	return ok;	
	};
	@Override
	public boolean addOutputHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
	boolean ok= super.addOutputHatchToMachineList(aTileEntity, aBaseCasingIndex);
	if(ok&&!hasHatchThisLayer){hasHatchThisLayer=true;}else{return false;}
	if(ok&&
	!(aTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_Hatch_Output_ME)){
		allMEHatch=false;
	}
	
	return ok;	};
	
	boolean isLiteVersion;
	
	
	@SuppressWarnings("unchecked")
	IStructureDefinition<IngredientDistributor> STRUCTURE_DEFINITION = StructureDefinition.<IngredientDistributor> builder()
		.addShape("first", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "m","f","c"}, { "m","f","c"}}  ))
		.addShape("second", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "~","c","c"}, { "m","c","c"} } ))	
		.addShape("third", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "m","c","c"}, { "◎","c","c"}  }))	
		.addShape("piece", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { "h","h","h"}  }))	
		.addShape("piece_survival", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { "h","※","h"}  }))	
		
		.addShape("last", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { " ","f"," "}  }))	
		
		.addElement('c',
				
				
				
				
				
						ofBlock(GregTech_API.sBlockCasings4, 12)
						
				
				
				
				)
		.addElement('f',StructureUtility.ofChain(
				GT_StructureUtility.ofFrame(Materials.Terbium),
				ofFrameAdder(Materials.Yttrium,s->((IngredientDistributor) s).onCheapFrameFound())
			
				
				
				
				))
		.addElement('h',
				buildHatchAdder(IngredientDistributor.class)
				.atLeast(GT_HatchElement.OutputBus.withAdder(IngredientDistributor::addBus)
						.withCount(s->s.hasBusThisLayer?1:0)
						,GT_HatchElement.OutputHatch.withAdder(IngredientDistributor::addHatch)
						.withCount(s->s.hasHatchThisLayer?1:0)
						)
				//.shouldSkip((a,b)->a.hasBusThisLayer)
				.casingIndex(CASING_INDEX).dot(1)
				.buildAndChain(GregTech_API.sBlockCasings4, 0)
				)
		.addElement('※',
				buildHatchAdder(IngredientDistributor.class)
				.atLeast(GT_HatchElement.OutputBus.withAdder(IngredientDistributor::addBus)
						.withCount(s->s.hasBusThisLayer?1:0)
						,GT_HatchElement.OutputHatch.withAdder(IngredientDistributor::addHatch)
						.withCount(s->s.hasHatchThisLayer?1:0)
						).allowOnly(ForgeDirection.DOWN)
				//.shouldSkip((a,b)->a.hasBusThisLayer)
				.casingIndex(CASING_INDEX).dot(1)
				.buildAndChain(GregTech_API.sBlockCasings4, 0)
				)
		
		
		
		.addElement('m', buildHatchAdder(IngredientDistributor.class)
				.atLeast(GT_HatchElement.Maintenance,GT_HatchElement.Energy)
				.casingIndex(CASING_INDEX).dot(3)
				.buildAndChain(
						buildHatchAdder(IngredientDistributor.class)
						.atLeast(
								new IHatchElement<IngredientDistributor>(){

							@Override
							public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
							
								return ImmutableList.of(CommunicationPortHatch.class);
							}

							@Override
							public IGT_HatchAdder<? super IngredientDistributor> adder() {
							
								return (s,w,u)->{
									if(s==null)return false;
									if(s.port!=null)return false;
									s.port=(CommunicationPortHatch) w.getMetaTileEntity();
									if(s.port==null)return false;
									s.port.updateTexture(CASING_INDEX);
									  return true;
									
								};
							}

							@Override
							public String name() {
							
								return "commport";
							}

							@Override
							public long count(IngredientDistributor t) {
							
								return port==null?0:1;
							}}
								)
						
								.hatchItemFilter(s->(
								//have to return this or will this not be displayed in NEI 'availale hatches' list
								is->is.getItemDamage()==Registration.CommunicationPortHatchOffset+Config.metaTileEntityOffset
								))
						
						
						
						.casingIndex(CASING_INDEX).dot(3).build(),
						
						ofBlock(GregTech_API.sBlockCasings4, 12)
						)
				)
		
		.addElement('◎', buildHatchAdder(IngredientDistributor.class)
				
				.atLeast(
						
						new IHatchElement<IngredientDistributor>(){

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public List<? extends Class<? extends IMetaTileEntity>> mteClasses() {
				return (List) ImmutableList.of(IDualInputHatch.class);
			}

			@Override
			public IGT_HatchAdder<? super IngredientDistributor> adder() {
				return IngredientDistributor::addDual;
			}

			@Override
			public String name() {
				return "dualadder";
			}

			@Override
			public long count(IngredientDistributor t) {
				
				return t.mDualInputHatches.size();
			}}

			
						,GT_HatchElement.InputBus
						,GT_HatchElement.InputHatch
		
				).casingIndex(CASING_INDEX).dot(2).build()
						
						
				)	
		.build();
	
	public IngredientDistributor(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));

	}

	

	
	

	private void onCheapFrameFound() {
		isLiteVersion=true;
		return ;
	}
	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		
		return new IngredientDistributor(mName);
	}
	protected static final int CASING_INDEX = 49+11-12;
	@Override
	public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
			int colorIndex, boolean active, boolean redstoneLevel) {
		if (side != facing) {
			return new ITexture[] { BlockIcons.getCasingTextureForId(CASING_INDEX) };
		}
		return active ? getTexturesActive(BlockIcons.getCasingTextureForId(CASING_INDEX))
				: getTexturesInactive(BlockIcons.getCasingTextureForId(CASING_INDEX));
	}

	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture,
				 TextureFactory.builder()
                 .addIcon(OVERLAY_FRONT_VACUUM_FREEZER_ACTIVE)
                 .extFacing()
                 .build()
				//TextureFactory.builder()
				//.setFromBlock(MyMod.iohub, BlockIOHub.magicNO_provider_active_overlay).glow().build() 
				};
	}

	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return new ITexture[] { aBaseTexture, 
				 TextureFactory.builder()
                 .addIcon(OVERLAY_FRONT_VACUUM_FREEZER)
                 .extFacing()
                 .build()
				
				 };
	}

	@Override
	public IStructureDefinition<IngredientDistributor> getStructureDefinition() {
	
		return STRUCTURE_DEFINITION;
	}

	@Override
	protected GT_Multiblock_Tooltip_Builder createTooltip() {
		final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
		Config.get(tt, "M_ID");
		return tt;
	}

	@Override
	public boolean isCorrectMachinePart(ItemStack aStack) {
	
		return true;
	}

	@Override
	public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env){
		
		if (mMachine)
			return -1;
	int b=	survivialBuildPiece("first", stackSize, 1, 1, 0,elementBudget, env, false,true);
	if(b>0)return b;
	b=survivialBuildPiece("second", stackSize,  0, 1, 0,elementBudget, env, false,true);
	if(b>0)return b;
	b=survivialBuildPiece("third", stackSize, -1, 1, 0,elementBudget, env, false,true);
	if(b>0)return b;
	
	
	int size=stackSize.stackSize+1;
	int index=-2;
	
	while(true){
		b=survivialBuildPiece("piece_survival", stackSize,index, 1, 0,elementBudget, env, false,true);
		if(b>0)return b;
		index--;if(--size==0)break;
		
	};
	return survivialBuildPiece("last", stackSize, index, 1, 0,elementBudget, env, false,true);

	
	
	}
	@Override
	public void construct(ItemStack stackSize, boolean hintsOnly) {
		buildPiece("first", stackSize, hintsOnly, 1, 1, 0);
		buildPiece("second", stackSize, hintsOnly, 0, 1, 0);
		buildPiece("third", stackSize, hintsOnly, -1, 1, 0);
		int size=stackSize.stackSize+1;
		int index=-2;
		while(buildPiece("piece", stackSize, hintsOnly, index, 1, 0)){
			
			index--;if(--size==0)break;
		};
		buildPiece("last", stackSize, hintsOnly, index, 1, 0);
	}
	@Override
	public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
		isLiteVersion=false;
		port=null;
		if(checkPiece("first", 1, 1, 0)&&
		checkPiece("second", 0, 1, 0)&&
		checkPiece("third", -1, 1, 0)
		){}else{return false;}
		int index=-2;
		
		//if(mDualInputHatches.size()!=1)return false;
		hasHatchThisLayer=hasBusThisLayer=false;
		while(checkPiece("piece", index, 1, 0)){
			hasHatchThisLayer=hasBusThisLayer=false;
			index--;
		};
		
		if(port!=null&&allMEHatch==false){return false;}
		return checkPiece("last", index, 1, 0)&&this.mEnergyHatches.size()>0;
				
				
				
				
				
		
		
		
	
	}

	@Override
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
int ready;

long storedEU;

/*
protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
	
	super.drawTexts(screenElements, inventorySlot);

    screenElements.widget(
    		 TextWidget.dynamicString(()->
    		 
    		StatCollector.translateToLocalFormatted("proghatches.ingdistr.eu", cost(),getMaxInputEu())
    		 
    				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
            .setEnabled(widget -> {
                return (getBaseMetaTileEntity().isAllowedToWork());
            }));

    
    
    
    
}

*/

boolean yield;
int count;
int cd;
int cdmax=1;
@Override
	protected void runMachine(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
		super.runMachine(aBaseMetaTileEntity, aTick);
		if (!mMachine)
			return;

		
		if (ready > 0 && aBaseMetaTileEntity.isAllowedToWork()) {
			boolean trylock = false;
			boolean porton = false;
			if (port != null && !lockRecipe && isAllMEOutputEmpty() == true
					&& (!port.getBaseMetaTileEntity().getRedstone() || count > 0) && !isInputEmpty()) {
				count++;
				porton = true;
				if (count > 100) {
					count = 0;
					trylock = true;
				}
			} else {
				count = 0;
			}
			if(port!=null){
			yield=(!porton&&count==0&&port.getBaseMetaTileEntity().getRedstone()&&!lockRecipe);
			
			
			port.setRS(porton);}
			if(trylock){
				
				 cd=0;
				 cdmax=1;
			}
			if ((port != null && lockRecipe && isAllMEOutputEmpty() == false) || port == null || trylock)
				if(++cd>=cdmax){
					cd=0;
					if (distribute()) {
						ready--;
						if (port != null) {
							if (trylock)
								lockRecipe = true;
	
						}
						lastfail = null;
						cd=0;
						cdmax=1;
					}else{
						if(cdmax<16)cdmax*=2;
					}	
				}

			if (port != null) {
				if (isAllMEOutputEmpty()) {
					lockRecipe = false;
				}
			}

		}
}
/*
@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
private Optional<IRecipeProcessingAwareHatch> getInput(){
	if(mDualInputHatches.size()==1){
		return (Optional)Optional.ofNullable(mDualInputHatches.get(0)).filter(s->s instanceof IRecipeProcessingAwareDualHatch)
				.map(s->new IRecipeProcessingAwareHatch(){
					public void startRecipeProcessing() {
					((IRecipeProcessingAwareDualHatch)s).startRecipeProcessing();
						
					}
					public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
						return ((IRecipeProcessingAwareDualHatch)s).endRecipeProcessing(controller);
					}})
				;
	}
	if(mInputBusses.size()==1){
		return (Optional)Optional.ofNullable(mInputBusses.get(0)).filter(s->s instanceof IRecipeProcessingAwareHatch);
	}
	if(mInputHatches.size()==1){
		return (Optional)Optional.ofNullable(mInputHatches.get(0)).filter(s->s instanceof IRecipeProcessingAwareHatch);
	}
	return Optional.empty();
	
}
*/

public boolean isInputEmpty(){
	startRecipeProcessing();
	
	
	try{
	if(mDualInputHatches.size()==1){
	return	!mDualInputHatches.get(0).getFirstNonEmptyInventory().isPresent();
	}
	if(mInputBusses.size()==1){
	GT_MetaTileEntity_Hatch_InputBus bus = mInputBusses.get(0);
		for(int i=0;i<bus.getSizeInventory();i++){
			if(bus.getStackInSlot(i)!=null||bus.getStackInSlot(i).stackSize>0){return false;}
		}
	}
	if(mInputHatches.size()==1){
		ArrayList<FluidStack> bus =getStoredFluids();
			for(int i=0;i<bus.size();i++){
				if(bus.get(i)!=null||bus.get(i).amount>0){return false;}
			}
		}
	return true;
	}finally{
		endRecipeProcessing();
		
	}
}
@SuppressWarnings("unchecked")
private boolean distribute() {
	
	startRecipeProcessing();
	Iterator<?> possibleSource=null;
	try{
	if(mDualInputHatches.size()==1){
	IDualInputHatch input = mDualInputHatches.get(0);
	possibleSource = (Iterator<IDualInputInventory>) input.inventories();
	
	}
	if(possibleSource==null&&mInputHatches.size()==1){
			ArrayList<FluidStack> fluid = getStoredFluids();
			fluid.removeIf(s->s==null||s.amount<=0);
			if(fluid.size()>0){
				possibleSource=ImmutableList.of(
						new IDualInputInventory(){
							@Override
							public ItemStack[] getItemInputs() {
							return new ItemStack[0];
							}
							FluidStack[] obj=null;
							@Override
							public FluidStack[] getFluidInputs() {
								if(obj!=null)return obj;
								fluid.removeIf(s->s==null||s.amount<=0);
								return obj=fluid.toArray(new FluidStack[fluid.size()]);
							}}
						
						).iterator();
				
			}
		}
	
		if(possibleSource==null&&mInputBusses.size()==1){
			ArrayList<ItemStack> items =getStoredInputs();
			items.removeIf(s->s==null||s.stackSize<=0);
			if(items.size()>0){
				possibleSource=ImmutableList.of(
						new IDualInputInventory(){
							@Override
							public ItemStack[] getItemInputs() {
								if(obj!=null)return obj;
								items.removeIf(s->s==null||s.stackSize<=0);
								return obj=items.toArray(new ItemStack[items.size()]);
							}
							ItemStack[] obj=null;
							@Override
							public FluidStack[] getFluidInputs() {
								return new FluidStack[0];
							}}
						
						).iterator();
				
			}
		}
	}finally{
	//boolean fail=in.map(s->s.endRecipeProcessing(this)).filter(s->!s.wasSuccessful()).isPresent();
		fail=false;
		endRecipeProcessing();
		if(fail) return false;
	}
	if(possibleSource==null)return false;
	Iterator<IDualInputInventory > itr = (Iterator<IDualInputInventory>) possibleSource;
	if(allMEHatch&&blocking){
		while(itr.hasNext()){
			if(moveToOutpusME(itr.next())){
				return true;
			};
			
		}
		return false;
	}
	while(itr.hasNext()){
		if(moveToOutpus(itr.next(),true)){
			return true;
		};
		
	}
	return false;
}

boolean fail;
public void setResultIfFailure(CheckRecipeResult result) {
   if(result.wasSuccessful()==false)fail=true;
	super.setResultIfFailure(result);
}

TransferCheckResult lastfail;
boolean blocking;
private boolean moveToOutpusME(IDualInputInventory opt) {
	ItemStack[] i = opt.getItemInputs();
	FluidStack[] f = opt.getFluidInputs();
	
	if(i.length>mOutputBusses.size())return false;
	if(f.length>mOutputHatches.size())return false;

	for(int index=0;index<mOutputBusses.size();++index){
		if(!(mOutputBusses.get(index) instanceof GT_MetaTileEntity_Hatch_OutputBus_ME)){return false;}
		TransferCheckResult result=checkMEBus(((GT_MetaTileEntity_Hatch_OutputBus_ME)mOutputBusses.get(index)),
				index<i.length?i[index]:null,index);
		if(!result.isSuccess){
			lastfail=result;
			return false;};
	}
	
	for(int index=0;index<mOutputHatches.size();++index){
		if(!(mOutputHatches.get(index) instanceof GT_MetaTileEntity_Hatch_Output_ME)){return false;}
		TransferCheckResult result=checkMEHatch(((GT_MetaTileEntity_Hatch_Output_ME)mOutputHatches.get(index)),
				index<f.length?f[index]:null,index
						);
		if(!result.isSuccess){
			lastfail=result;return false;};
	}
	
	
	for(int index=0;index<i.length;++index){
				try {
					IAEItemStack notadded = ((GT_MetaTileEntity_Hatch_OutputBus_ME)mOutputBusses.get(index))
					.getProxy().getStorage().getItemInventory().injectItems(AEApi.instance()
					.storage()
					.createItemStack(i[index]), Actionable.MODULATE, getActionSourceFor(mOutputBusses.get(index)));
					i[index].stackSize=Optional.ofNullable(notadded).map(s->s.getStackSize()).orElse(0l).intValue();
				} catch (GridAccessException e) {}
				
					
				
	}
	
	for(int index=0;index<f.length;++index){
		try {
			IAEFluidStack notadded = ((GT_MetaTileEntity_Hatch_Output_ME)mOutputHatches.get(index))
			.getProxy().getStorage().getFluidInventory().injectItems(AEApi.instance()
			.storage()
			.createFluidStack(f[index]), Actionable.MODULATE, getActionSourceFor(mOutputHatches.get(index)));
			f[index].amount=Optional.ofNullable(notadded).map(s->s.getStackSize()).orElse(0l).intValue();
		} catch (GridAccessException e) {}
		}
	mInputBusses.forEach(s->s.updateSlots());
	mInputHatches.forEach(s->s.updateSlots());
	
	
	return true;
}
public static class TransferCheckResultSyncer extends FakeSyncWidget<TransferCheckResult> {

    public TransferCheckResultSyncer(Supplier<TransferCheckResult> getter, Consumer<TransferCheckResult> setter) {
        super(getter, setter, TransferCheckResult::ser,TransferCheckResult::deser);
    }
}
static class TransferCheckResult{
	boolean isSuccess;
	String reason="";
	public String format(){
		if(isSuccess)	return "";//StatCollector.translateToLocalFormatted("proghatch.ingbuf.success");
		
		for(int i=0;i<args.length;i++){
			if(args[i]instanceof AEFluidStack){
				args[i]=((AEFluidStack)args[i]).getFluidStack();
			}
			if(args[i]instanceof FluidStack){
				args[i]=((FluidStack)args[i]).getFluid().getName()+" "+((FluidStack)args[i]).amount+"L";
			}
			
			if(args[i]instanceof AEItemStack){
				args[i]=((AEItemStack)args[i]).getItemStack();
			}
			if(args[i]instanceof ItemStack){
				args[i]=((ItemStack)args[i]).getDisplayName()+"x"+((ItemStack)args[i]).stackSize;
			}
			
			
			
		}//only called on CLIENT, so it's safe to do like this
		
		return StatCollector.translateToLocalFormatted("proghatch.ingbuf.fail."+reason, args);
		
	}
	Object[] args=new Object[0];
	static TransferCheckResult ofSuccess(){
		return new TransferCheckResult(){{isSuccess=true;}};}
	static TransferCheckResult ofFail(String key,Object... fmt){
		return new TransferCheckResult(){{reason=key;args=fmt;}};}
	
	static TransferCheckResult deser(PacketBuffer pb){
		try {
			NBTTagCompound tag = pb.readNBTTagCompoundFromBuffer();
			if(tag.hasNoTags())return null;
			TransferCheckResult ret=new TransferCheckResult();
			ret. isSuccess=tag.getBoolean("s");
			ret. reason=tag.getString("r");
			int len=tag.getInteger("l");
			Object[] arg=new Object[len];
			for(int i=0;i<len;++i){
				Object r=null;
				int type=tag.getInteger("t"+i);
				
				if(type==0)
				r=tag.getString("o"+i);
				if(type==1)
				r=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("o"+i));
				if(type==2)
				r=AEItemStack.loadItemStackFromNBT(tag.getCompoundTag("o"+i));
				if(type==3)
				r=FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("o"+i));
				if(type==4)
				r=AEFluidStack.loadFluidStackFromNBT(tag.getCompoundTag("o"+i));
				
				
				
				arg[i]=r;
			}
			ret.args=arg;
			return ret;
		} catch (IOException e) {
	
			e.printStackTrace();return
					null;
		}
		
	}
	
	static void ser(PacketBuffer pb,TransferCheckResult thiz){
		
		NBTTagCompound tag=new NBTTagCompound();
		if(thiz==null){try {
			pb.writeNBTTagCompoundToBuffer(tag);
		} catch (IOException e) {
			e.printStackTrace();
		}return;}
		try {
			tag.setBoolean("s", thiz.isSuccess);
			tag.setString("r", thiz.reason);
			tag.setInteger("l", thiz.args.length);
			for(int i=0;i<thiz.args.length;i++){
				Object o=thiz.args[i];
				if(o instanceof String){
					tag.setString("o"+i, o.toString());
					tag.setInteger("t"+i, 0);
				}
				if(o instanceof ItemStack){
					tag.setTag("o"+i, ((ItemStack) o).writeToNBT(new NBTTagCompound()));
					tag.setInteger("t"+i, 1);
				}
				if(o instanceof AEItemStack){
					NBTTagCompound tmp = new NBTTagCompound();
					((AEItemStack) o).writeToNBT(tmp);
					tag.setTag("o"+i, tmp);
					tag.setInteger("t"+i, 2);
				}
				if(o instanceof FluidStack){
					tag.setTag("o"+i, ((FluidStack) o).writeToNBT(new NBTTagCompound()));
					tag.setInteger("t"+i, 3);
				}
				if(o instanceof AEFluidStack){
					NBTTagCompound tmp = new NBTTagCompound();
					((AEFluidStack) o).writeToNBT(tmp);
					tag.setTag("o"+i, tmp);
					tag.setInteger("t"+i, 4);
				}
				if(o instanceof Number){
				
					tag.setString("o"+i, o.toString());
					tag.setInteger("t"+i, 0);
				}
				
			}
			
			pb.writeNBTTagCompoundToBuffer(tag);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}
	
	
}
private ItemStack cp(ItemStack c){
	if(c!=null)return c.copy();
	return null;
}
private IAEItemStack cp(IAEItemStack c){
	if(c!=null)return c.copy();
	return null;
}
private boolean isAllMEOutputEmpty(){
	
	for(GT_MetaTileEntity_Hatch_OutputBus o:mOutputBusses){
		if(o instanceof GT_MetaTileEntity_Hatch_OutputBus_ME){
		
			IItemList<IAEItemStack> itemCache;
			try {
			itemCache = (IItemList<IAEItemStack>) f.get(o);
			if(itemCache.isEmpty()==false){return false;}
			itemCache=o.getProxy().getStorage().getItemInventory().getStorageList();
			if(itemCache.isEmpty()==false){return false;}
			
			} catch (Exception e) {e.printStackTrace();
			}
		}else return false;
	}

	for(GT_MetaTileEntity_Hatch_Output o:mOutputHatches){
		if(o instanceof GT_MetaTileEntity_Hatch_Output_ME){
		
			IItemList<IAEFluidStack> itemCache;
			try {
			itemCache = (IItemList<IAEFluidStack>) f2.get(o);
			if(itemCache.isEmpty()==false){return false;}
			itemCache=o.getProxy().getStorage().getFluidInventory().getStorageList();
			if(itemCache.isEmpty()==false){return false;}
			} catch (Exception e) {e.printStackTrace();
			}
		}else return false;
	}
	return true;
}

static Field f,f2;
static{
if(f==null)
	try {
		f=GT_MetaTileEntity_Hatch_OutputBus_ME.class.getDeclaredField("itemCache");
     f.setAccessible(true);
	} catch (Exception e) {
		e.printStackTrace();
	}
if(f2==null)
	try {
		f2=GT_MetaTileEntity_Hatch_Output_ME.class.getDeclaredField("fluidCache");
     f2.setAccessible(true);
	} catch (Exception e) {
		e.printStackTrace();
	}}
@SuppressWarnings({ "unchecked", "unused" })
private TransferCheckResult checkMEBus(GT_MetaTileEntity_Hatch_OutputBus_ME bus,ItemStack check,int index){
	
	try {
		IItemList<IAEItemStack> itemCache =(IItemList<IAEItemStack>) f.get(bus);
		Iterator<IAEItemStack> itr = itemCache.iterator();
		//if(check!=null)
		while(itr.hasNext()){IAEItemStack next;
			if((next=itr.next()).isSameType(check)==false&&next.getStackSize()>0){
				if(check==null)
					return TransferCheckResult.ofFail("cache.diff.bus.null",index,cp(next));
				return TransferCheckResult.ofFail("cache.diff.bus",index,cp(next),cp(check));
			}
		}
		itr = bus.getProxy().getStorage().getItemInventory().getStorageList().iterator();
		//if(check!=null)
		while(itr.hasNext()){IAEItemStack next;
			if((next=itr.next()).isSameType(check)==false&&next.getStackSize()>0){
				if(check==null)
					return TransferCheckResult.ofFail("net.diff.bus.null",index,cp(next));
				return TransferCheckResult.ofFail("net.diff.bus",index,cp(next),cp(check));
			}
		}
		if(check!=null){
		
		IAEItemStack notadded = bus.getProxy().getStorage().getItemInventory().injectItems(
				AEItemStack.create(check),
				Actionable.SIMULATE, 
				getActionSourceFor(bus)
				);
		
		if(notadded!=null&&notadded.getStackSize()>0)return TransferCheckResult.ofFail("inject.failure."+(bus.getProxy().isPowered()&&bus.getProxy().isActive())+".bus",index,check.copy(),notadded.copy());
		}
	} catch (Exception e) {
		e.printStackTrace();
		return  TransferCheckResult.ofFail("crash",e.getClass()+" "+e.getMessage());
	}
	
	
	return TransferCheckResult.ofSuccess();
}
@SuppressWarnings({ "unchecked", "unused" })
private TransferCheckResult checkMEHatch(GT_MetaTileEntity_Hatch_Output_ME bus,FluidStack check,int index){
	
	try {
		IItemList<IAEFluidStack> itemCache =(IItemList<IAEFluidStack>) f2.get(bus);
		Iterator<IAEFluidStack> itr = itemCache.iterator();
		//if(check!=null)
		while(itr.hasNext()){IAEFluidStack next;
			if(!sameType(next=itr.next(),(check))&&next.getStackSize()>0){
				if(check==null)
					return TransferCheckResult.ofFail("net.diff.hatch.null",index,cp(next));
				return TransferCheckResult.ofFail("cache.diff.hatch",index,cp(next),cp(check));
			}
		}
			
		itr = bus.getProxy().getStorage().getFluidInventory().getStorageList().iterator();
		//if(check!=null)
		while(itr.hasNext()){IAEFluidStack next;
			if(!sameType(next=itr.next(),(check))&&next.getStackSize()>0){
				if(check==null)
					return TransferCheckResult.ofFail("net.diff.hatch.null",index,cp(next));
				return TransferCheckResult.ofFail("net.diff.hatch",index,cp(next),cp(check));
			}
		}if(check!=null){
		IAEFluidStack notadded = bus.getProxy().getStorage().getFluidInventory().injectItems(
				AEFluidStack.create(check),
				Actionable.SIMULATE, 
				getActionSourceFor(bus)
				);
		if(notadded!=null&&notadded.getStackSize()>0)return TransferCheckResult.ofFail("inject.failure."+(bus.getProxy().isPowered()&&bus.getProxy().isActive())+".hatch",index,check.copy(),notadded.copy());
		}
	} catch (Exception e) {
		e.printStackTrace();
		return  TransferCheckResult.ofFail("crash",e.getClass()+" "+e.getMessage());
	}
	
	
	return TransferCheckResult.ofSuccess();
}
private IAEFluidStack cp(IAEFluidStack c) {

	if(c!=null)return c.copy();
	return null;
}

private FluidStack cp(FluidStack c) {
	
	if(c!=null)return c.copy();
	return null;
}
static BaseActionSource fakeSource=new  BaseActionSource();

static Method[] cache;
static BaseActionSource getActionSourceFor(Object o){
	if(cache==null)try {
		cache=new Method[2];
		cache[0]=GT_MetaTileEntity_Hatch_OutputBus_ME.class.getDeclaredMethod("getRequest");
		cache[1]=GT_MetaTileEntity_Hatch_Output_ME.class.getDeclaredMethod("getRequest");
		cache[0].setAccessible(true);
		cache[1].setAccessible(true);
	} catch (NoSuchMethodException e) {
		cache=new Method[0];;
		e.printStackTrace();
	}
	if(cache.length==0)return fakeSource;
	
		try {
			if(o instanceof GT_MetaTileEntity_Hatch_OutputBus_ME)
			return (BaseActionSource) cache[0].invoke(o);
			if(o instanceof GT_MetaTileEntity_Hatch_Output_ME)
			return (BaseActionSource) cache[1].invoke(o);
		} catch (Exception e) {
			e.printStackTrace();
		}
	 
	
	throw new RuntimeException("err");
}

private static boolean sameType(IAEFluidStack  a,FluidStack b){
	if(b==null)return false;
	if(a.getFluid()!=b.getFluid())return false;
	if(a.getTagCompound()==null){
		if(b.tag==null)return true;
		else return false;
	}else{
		if(b.tag==null)return false;
	}
	return a.getTagCompound().getNBTTagCompoundCopy().equals(b.tag);
}


private boolean moveToOutpus(IDualInputInventory opt,boolean checkSpace) {
	ItemStack[] i = opt.getItemInputs();
	FluidStack[] f = opt.getFluidInputs();
	boolean anyDiff=false;
	if(i.length>mOutputBusses.size()){
		lastfail=TransferCheckResult.ofFail("insufficient.length.bus", i.length,mOutputBusses.size());
		return false;}
	if(f.length>mOutputHatches.size()){
		lastfail=TransferCheckResult.ofFail("insufficient.length.hatch", f.length,mOutputHatches.size());
		return false;}
	
	if(checkSpace){
	for(int index=0;index<i.length;++index){
		if(mOutputBusses.get(index) instanceof GT_MetaTileEntity_Hatch_OutputBus_ME){
			if(i[index]!=null){
				GT_MetaTileEntity_Hatch_OutputBus_ME bus=(GT_MetaTileEntity_Hatch_OutputBus_ME) mOutputBusses.get(index);
				try {
					IAEItemStack notadded=null;
					notadded = (bus).getProxy().getStorage().getItemInventory().injectItems(
							AEItemStack.create(i[index]),
							Actionable.SIMULATE, 
							getActionSourceFor(bus)
							);
				
				
				if(notadded!=null&&notadded.getStackSize()>0){
					TransferCheckResult ret=TransferCheckResult.ofFail("inject.failure."+(bus.getProxy().isPowered()&&bus.getProxy().isActive())+".bus",index,i[index].copy(),notadded.copy());
				 if(!ret.isSuccess){
					 lastfail=ret;
					return false;}
				 }
				} catch (GridAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			
			continue;}
		GT_MetaTileEntity_Hatch_OutputBus bus = mOutputBusses.get(index);
		int acc=0;
		for(int x=0;x<bus.getSizeInventory();x++){
			if(bus.isValidSlot(x))
			acc+=space(i[index],bus.getStackInSlot(x));
		}
		if(acc<i[index].stackSize){
			
			lastfail=TransferCheckResult.ofFail("insufficient.space.bus", index,i[index].copy(),acc);
			return false;}
		
	}
	for(int index=0;index<f.length;++index){
		if(mOutputHatches.get(index) instanceof GT_MetaTileEntity_Hatch_Output_ME){
			if(i[index]!=null){
				GT_MetaTileEntity_Hatch_Output_ME bus=(GT_MetaTileEntity_Hatch_Output_ME) mOutputHatches.get(index);
				try {
					IAEFluidStack notadded=null;
					notadded = (bus).getProxy().getStorage().getFluidInventory().injectItems(
							AEFluidStack.create(f[index]),
							Actionable.SIMULATE, 
							getActionSourceFor(bus)
							);
				
				
				if(notadded!=null&&notadded.getStackSize()>0){
					TransferCheckResult ret=TransferCheckResult.ofFail("inject.failure."+(bus.getProxy().isPowered()&&bus.getProxy().isActive())+".hatch",index,i[index].copy(),notadded.copy());
				 if(!ret.isSuccess){
					 lastfail=ret;
					return false;}
				 }
				} catch (GridAccessException e) {
					e.printStackTrace();
				}
				}
			continue;}
		GT_MetaTileEntity_Hatch_Output hatch = mOutputHatches.get(index);
		int acc=0;
		acc+=space(f[index],hatch);
		if(acc<f[index].amount){
			lastfail=TransferCheckResult.ofFail("insufficient.space.hatch", index,f[index].copy(),acc);
			return false;}
	}
	}
	
	
	
	
	for(int index=0;index<i.length;++index){
		if(mOutputBusses.get(index) instanceof GT_MetaTileEntity_Hatch_OutputBus_ME){
			int before=i[index].stackSize;
			i[index].stackSize=((GT_MetaTileEntity_Hatch_OutputBus_ME)mOutputBusses.get(index)).store(i[index]);
	    	if(i[index].stackSize!=before)anyDiff=true;	
			
			continue;
		}
		GT_MetaTileEntity_Hatch_OutputBus bus = mOutputBusses.get(index);
		int diff=storeAll(bus,i[index].copy());
		if(diff>0)anyDiff=true;
		i[index].stackSize-=diff;
	}
	
	for(int index=0;index<f.length;++index){
		if(mOutputHatches.get(index) instanceof GT_MetaTileEntity_Hatch_Output_ME){
			int before=f[index].amount;
			f[index].amount-=((GT_MetaTileEntity_Hatch_Output_ME)mOutputHatches.get(index)).tryFillAE(f[index]);
			if(f[index].amount!=before)anyDiff=true;	
			
			continue;
		}
		GT_MetaTileEntity_Hatch_Output hatch = mOutputHatches.get(index);
		int diff=hatch.fill(f[index], true);
		if(diff>0)anyDiff=true;	
		f[index].amount-=diff;
		
	}
	mInputBusses.forEach(s->s.updateSlots());
	mInputHatches.forEach(s->s.updateSlots());
	
	
	return anyDiff;
}
private static int storeAll(GT_MetaTileEntity_Hatch_OutputBus bus,ItemStack aStack) {
	bus.markDirty();
	int consumed=0;
    for (int i = 0, mInventoryLength = bus.mInventory.length; i < mInventoryLength && aStack.stackSize > 0; i++) {
        ItemStack tSlot = bus.mInventory[i];
        if (GT_Utility.isStackInvalid(tSlot)) {
            int tRealStackLimit = Math.min(bus.getInventoryStackLimit(), aStack.getMaxStackSize());
            if (aStack.stackSize <= tRealStackLimit) {
            	bus.mInventory[i] = aStack;
            	consumed+=aStack.stackSize;
                return consumed;
            }
            bus.mInventory[i] = aStack.splitStack(tRealStackLimit);
            consumed+=tRealStackLimit;
        } else {
            int tRealStackLimit = Math.min(bus.getInventoryStackLimit(), tSlot.getMaxStackSize());
            if (tSlot.stackSize < tRealStackLimit && tSlot.isItemEqual(aStack)
                && ItemStack.areItemStackTagsEqual(tSlot, aStack)) {
                if (aStack.stackSize + tSlot.stackSize <= tRealStackLimit) {
                	bus.mInventory[i].stackSize += aStack.stackSize;
                	consumed+=aStack.stackSize;
                    return consumed;
                } else {
                    // more to serve 
                	consumed+=tRealStackLimit - tSlot.stackSize;
                    aStack.stackSize -= tRealStackLimit - tSlot.stackSize;
                    bus.mInventory[i].stackSize = tRealStackLimit;
                   
                }
            }
        }
    }
    return consumed;
}

private static int space(FluidStack in,IFluidStore store){
	return store.fill(in, false);
	
}
private static int space(ItemStack in,ItemStack store){
	if(store==null){return in.getMaxStackSize();}
	if(in.getItem()==store.getItem()&&in.getItemDamage()==store.getItemDamage()&&
			ItemStack.areItemStackTagsEqual(in, store)){
		
		return in.getMaxStackSize()-store.stackSize;
	}
	
	return 0;
	
}
@Override
public void loadNBTData(NBTTagCompound aNBT) {
	ready=aNBT.getInteger("ready");
	blocking=aNBT.getBoolean("blocking");
	emptyRun=aNBT.getBoolean("emptyRun");
	allMEHatch=aNBT.getBoolean("allMEHatch");
	count=aNBT.getInteger("count");
	cd=aNBT.getInteger("cd");
	cdmax=aNBT.getInteger("cdmax");
	lockRecipe=aNBT.getBoolean("lockRecipe");
	super.loadNBTData(aNBT);
}
@Override
public void saveNBTData(NBTTagCompound aNBT) {
	aNBT.setInteger("ready", ready);
	aNBT.setBoolean("blocking", blocking);
	aNBT.setBoolean("emptyRun", emptyRun);
	aNBT.setBoolean("allMEHatch", allMEHatch);
	
	aNBT.setInteger("count", count);
	aNBT.setInteger("cd", cd);
	aNBT.setInteger("cdmax", cdmax);
	aNBT.setBoolean("lockRecipe", lockRecipe);
	super.saveNBTData(aNBT);
}

boolean lockRecipe;
@Override
public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	if(isLiteVersion)blocking=false;
	if(blocking==false&&port!=null){
		
		LargeProgrammingCircuitProvider.shut(this,"proghatch.commport");
	}
	if( !aBaseMetaTileEntity.getWorld().isRemote&&mMachine){
	if (!allMEHatch) {
		blocking=false;
	}
	if (mMaxProgresstime > 0 && mProgresstime+1 >= mMaxProgresstime) {
		  if(ready<=0&&!emptyRun)ready++;
		  
	  }
	 }
	super.onPostTick(aBaseMetaTileEntity, aTick);
}
boolean emptyRun;

@Override
public CheckRecipeResult checkProcessing() {
    mEfficiency= 10000;
    final long inputVoltage = getMaxInputVoltage();
    calculateOverclockedNessMultiInternal(
        120,
        100,
        1,
        inputVoltage,
        false);
  
    mEUt /= -1; 
    if(ready>0){
    	mEUt=0;
    	emptyRun=true;
    	return SimpleCheckRecipeResult.ofSuccess("proghatches.ingredientdistr.ready");
    	}
    emptyRun=false;
    return SimpleCheckRecipeResult.ofSuccess("proghatches.ingredientdistr.charging");
}
@Override
public void addUIWidgets(com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder builder,
		UIBuildContext buildContext) {
	builder.widget(new TransferCheckResultSyncer(()->this.lastfail, s->this.lastfail=s));
	  builder.widget(
	            new DrawableWidget().setDrawable(GT_UITextures.PICTURE_SCREEN_BLACK)
	                .setPos(4, 4)
	                .setSize(190, 85));
	        final SlotWidget inventorySlot = new SlotWidget(inventoryHandler, 1);
	        builder.widget(
	            inventorySlot.setPos(173, 167)
	                .setBackground(GT_UITextures.SLOT_DARK_GRAY));

	        final DynamicPositionedColumn screenElements = new DynamicPositionedColumn();
	        drawTexts(screenElements, inventorySlot);
	        builder.widget(screenElements);

	        builder.widget(createPowerSwitchButton(builder))
	            .widget(createBlockingModeButton(builder))
	           .widget(new FakeSyncWidget.BooleanSyncer(
        		 ()->blocking,
        		 val -> {
        	 blocking=val;
                   }).setSynced(true, true))
	           .widget(new FakeSyncWidget.BooleanSyncer(
	          		 ()->lockRecipe,
	          		 val -> {
	          			lockRecipe=val;
	                     }).setSynced(true, false))
	           .widget(new FakeSyncWidget.BooleanSyncer(
		          		 ()->port!=null,
		          		 val -> {
		          			clientport=val;
		                     }).setSynced(true, false))
	           
	           .widget(new FakeSyncWidget.IntegerSyncer(
		          		 ()->count,
		          		 val -> {
		          			count=val;
		                     }).setSynced(true, false))
	           .widget(new FakeSyncWidget.IntegerSyncer(
		          		 ()->cd,
		          		 val -> {
		          			cd=val;
		                     }).setSynced(true, false))
	           .widget(new FakeSyncWidget.IntegerSyncer(
		          		 ()->cdmax,
		          		 val -> {
		          			cdmax=val;
		                     }).setSynced(true, false))
	           .widget(new FakeSyncWidget.BooleanSyncer(
		          		 ()->yield,
		          		 val -> {
		          			yield=val;
		                     }).setSynced(true, false))
	           ;
	        
	        
}

ButtonWidget createBlockingModeButton(IWidgetBuilder<?> builder) {
  
	 Widget button = new ButtonWidget().setOnClick((clickData, widget) -> {
		 blocking=!blocking;
     })
         .setPlayClickSoundResource(
             () -> blocking ? SoundResource.GUI_BUTTON_UP.resourceLocation
                 : SoundResource.GUI_BUTTON_DOWN.resourceLocation)
         .setBackground(() -> {
             if (blocking) {
                 return new IDrawable[] { GT_UITextures.BUTTON_STANDARD_PRESSED,
                     GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_ON };
             } else {
                 return new IDrawable[] { GT_UITextures.BUTTON_STANDARD,
                     GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF };
             }
         })
         
         .addTooltip(StatCollector.translateToLocal("proghatches.ingredientdistr.blocking"))
         
         .setTooltipShowUpDelay(TOOLTIP_DELAY)
         .setPos(getVoidingModeButtonPos())
         .setSize(16, 16);
    
	 IntStream
		.range(0,
				Integer.valueOf(StatCollector.translateToLocal(
						"proghatches.ingredientdistr.blocking.desc")))
		.forEach(s -> button.addTooltip(LangManager.translateToLocal(
				"proghatches.ingredientdistr.blocking.desc." + s)));


	
	
    return (ButtonWidget) button;
}
public static boolean addHatch(IngredientDistributor thiz, IGregTechTileEntity aTileEntity,short s){
	if(thiz.hasHatchThisLayer){return false;}
	return thiz.addOutputHatchToMachineList(aTileEntity, s);
}
public static boolean addBus(IngredientDistributor thiz, IGregTechTileEntity aTileEntity,short s){
	if(thiz.hasBusThisLayer){return false;}
	return thiz.addOutputBusToMachineList(aTileEntity, s);
}

boolean clientport;
protected void drawTexts(DynamicPositionedColumn screenElements, SlotWidget inventorySlot) {
	
	super.drawTexts(screenElements, inventorySlot);

    screenElements.widget(
    		 TextWidget.dynamicString(()->
    		 lastfail==null?"":
    		lastfail.format()
    		 
    				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
            .setEnabled(widget -> {
                return (getBaseMetaTileEntity().isAllowedToWork())&&lastfail!=null;
            }));
    
    screenElements.widget(
      		 TextWidget.dynamicString(()->
      		 {		if(yield)return StatCollector.translateToLocal("proghatch.ingbuf.yield");
      			 if(count>0)return StatCollector.translateToLocal("proghatch.ingbuf.acquring");
      			if(lockRecipe)return StatCollector.translateToLocal("proghatch.ingbuf.locked");
      			 
      			return StatCollector.translateToLocal("proghatch.ingbuf.idle");
      		 }
      		 
      				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
              .setEnabled(widget -> clientport));
    
    
    
    screenElements.widget(
   		 TextWidget.dynamicString(()->
   		"lock:"+lockRecipe
   		 
   				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
           .setEnabled(widget -> clientport));
   
    screenElements.widget(
      		 TextWidget.dynamicString(()->
      		"count:"+count
      		 
      				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
              .setEnabled(widget -> clientport));
    
    screenElements.widget(
     		 TextWidget.dynamicString(()->
     		cd+"/"+cdmax
     		 
     				 ).setDefaultColor(COLOR_TEXT_WHITE.get())
             .setEnabled(widget -> clientport));
   
    
    
    
}
}
