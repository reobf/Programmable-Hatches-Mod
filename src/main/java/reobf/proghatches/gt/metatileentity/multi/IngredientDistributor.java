package reobf.proghatches.gt.metatileentity.multi;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.metatileentity.BaseTileEntity.TOOLTIP_DELAY;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
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
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.DynamicPositionedColumn;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.block.BlockIOHub;
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
		
	@SuppressWarnings("unchecked")
	IStructureDefinition<IngredientDistributor> STRUCTURE_DEFINITION = StructureDefinition.<IngredientDistributor> builder()
		.addShape("first", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "m","f","c"}, { "m","f","c"}}  ))
		.addShape("second", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "~","c","c"}, { "m","c","c"} } ))	
		.addShape("third", StructureUtility.transpose (new String[][] { { "m","f","c"}, { "m","c","c"}, { "◎","c","c"}  }))	
		.addShape("piece", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { "h","h","h"}  }))	
		.addShape("piece_survival", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { "h","※","h"}  }))	
		
		.addShape("last", StructureUtility.transpose (new String[][] { { " "," "," "}, { " ","f"," "}, { " ","f"," "}  }))	
		
		.addElement('c', ofBlock(GregTech_API.sBlockCasings4, 12))
		.addElement('f', GT_StructureUtility.ofFrame(Materials.Terbium))
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
				.buildAndChain(GregTech_API.sBlockCasings4, 12))
		
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



@Override
protected void runMachine(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
super.runMachine(aBaseMetaTileEntity, aTick);
if(!mMachine)return;


if(ready>0&&aBaseMetaTileEntity.isAllowedToWork()){
	if(distribute()){
			ready--;
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
@SuppressWarnings("unchecked")
private boolean distribute() {
	//Optional<IRecipeProcessingAwareHatch> in = getInput();
	//in.ifPresent(s->s.startRecipeProcessing());
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


boolean blocking;
private boolean moveToOutpusME(IDualInputInventory opt) {
	ItemStack[] i = opt.getItemInputs();
	FluidStack[] f = opt.getFluidInputs();
	
	if(i.length>mOutputBusses.size())return false;
	if(f.length>mOutputHatches.size())return false;

	for(int index=0;index<mOutputBusses.size();++index){
		if(!(mOutputBusses.get(index) instanceof GT_MetaTileEntity_Hatch_OutputBus_ME)){return false;}
		if(!checkMEBus(((GT_MetaTileEntity_Hatch_OutputBus_ME)mOutputBusses.get(index)),
				index<i.length?i[index]:null
						)){return false;};
	}
	
	for(int index=0;index<mOutputHatches.size();++index){
		if(!(mOutputHatches.get(index) instanceof GT_MetaTileEntity_Hatch_Output_ME)){return false;}
		if(!checkMEHatch(((GT_MetaTileEntity_Hatch_Output_ME)mOutputHatches.get(index)),
				index<f.length?f[index]:null
						)){return false;};
	}
	
	
	for(int index=0;index<i.length;++index){
		i[index].stackSize=((GT_MetaTileEntity_Hatch_OutputBus_ME)mOutputBusses.get(index)).store(i[index]);
	}
	
	for(int index=0;index<f.length;++index){
		f[index].amount-=((GT_MetaTileEntity_Hatch_Output_ME)mOutputHatches.get(index)).tryFillAE(f[index]);
	}
	mInputBusses.forEach(s->s.updateSlots());
	mInputHatches.forEach(s->s.updateSlots());
	
	
	return true;
}

static Field f,f2;
@SuppressWarnings({ "unchecked", "unused" })
private boolean checkMEBus(GT_MetaTileEntity_Hatch_OutputBus_ME bus,ItemStack check){
	if(f==null)
	try {
		f=GT_MetaTileEntity_Hatch_OutputBus_ME.class.getDeclaredField("itemCache");
     f.setAccessible(true);
	} catch (Exception e) {
		e.printStackTrace();
	}
	try {
		IItemList<IAEItemStack> itemCache =(IItemList<IAEItemStack>) f.get(bus);
		Iterator<IAEItemStack> itr = itemCache.iterator();
		while(itr.hasNext()){
			if(itr.next().isSameType(check)==false){
				return false;
			}
		}
		itr = bus.getProxy().getStorage().getItemInventory().getStorageList().iterator();
		while(itr.hasNext()){
			if(itr.next().isSameType(check)==false){
				return false;
			}
		}
		if(check!=null){
		
		IAEItemStack notadded = bus.getProxy().getStorage().getItemInventory().injectItems(
				AEItemStack.create(check),
				Actionable.SIMULATE, 
				fakeSource
				);
		if(notadded!=null&&notadded.getStackSize()>0)return false;
		}
	} catch (Exception e) {
		e.printStackTrace();
		return false;
	}
	
	
	return true;
}
@SuppressWarnings({ "unchecked", "unused" })
private boolean checkMEHatch(GT_MetaTileEntity_Hatch_Output_ME bus,FluidStack check){
	if(f2==null)
	try {
		f2=GT_MetaTileEntity_Hatch_Output_ME.class.getDeclaredField("fluidCache");
     f2.setAccessible(true);
	} catch (Exception e) {
		e.printStackTrace();
	}
	try {
		IItemList<IAEFluidStack> itemCache =(IItemList<IAEFluidStack>) f2.get(bus);
		Iterator<IAEFluidStack> itr = itemCache.iterator();
		while(itr.hasNext()){
			if(!sameType(itr.next(),(check))){
				return false;
			}
		}
		itr = bus.getProxy().getStorage().getFluidInventory().getStorageList().iterator();
		while(itr.hasNext()){
			if(!sameType(itr.next(),(check))){
				return false;
			}
		}if(check!=null){
		IAEFluidStack notadded = bus.getProxy().getStorage().getFluidInventory().injectItems(
				AEFluidStack.create(check),
				Actionable.SIMULATE, 
				fakeSource
				);
		if(notadded!=null&&notadded.getStackSize()>0)return false;
		}
	} catch (Exception e) {
		e.printStackTrace();
		return false;
	}
	
	
	return true;
}
static BaseActionSource fakeSource=new  BaseActionSource();
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
	if(i.length>mOutputBusses.size())return false;
	if(f.length>mOutputHatches.size())return false;
	
	if(checkSpace){
	for(int index=0;index<i.length;++index){
		if(mOutputBusses.get(index) instanceof GT_MetaTileEntity_Hatch_OutputBus_ME){continue;}
		GT_MetaTileEntity_Hatch_OutputBus bus = mOutputBusses.get(index);
		int acc=0;
		for(int x=0;x<bus.getSizeInventory();x++){
			if(bus.isValidSlot(x))
			acc+=space(i[index],bus.getStackInSlot(x));
		}
		if(acc<i[index].stackSize){return false;}
		
	}
	for(int index=0;index<f.length;++index){
		if(mOutputHatches.get(index) instanceof GT_MetaTileEntity_Hatch_Output_ME){continue;}
		GT_MetaTileEntity_Hatch_Output hatch = mOutputHatches.get(index);
		int acc=0;
		acc+=space(f[index],hatch);
		if(acc<f[index].amount){return false;}
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
	super.loadNBTData(aNBT);
}
@Override
public void saveNBTData(NBTTagCompound aNBT) {
	aNBT.setInteger("ready", ready);
	aNBT.setBoolean("blocking", blocking);
	aNBT.setBoolean("emptyRun", emptyRun);
	aNBT.setBoolean("allMEHatch", allMEHatch);
	super.saveNBTData(aNBT);
}
@Override
public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	
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

}
