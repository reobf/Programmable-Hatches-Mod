package reobf.proghatches.gt.metatileentity;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.events.LocatableEventAnnounce;
import appeng.api.events.LocatableEventAnnounce.LocatableEvent;
import appeng.api.exceptions.ExistingConnectionException;
import appeng.api.exceptions.SecurityConnectionException;
import appeng.api.features.ILocatable;
import appeng.api.features.INetworkEncodable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.ICellProvider;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IInterfaceViewable;
import appeng.helpers.ICustomNameObject;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.networking.TileWireless;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import appeng.util.item.ItemList;
import cpw.mods.fml.client.event.ConfigChangedEvent.PostConfigChangedEvent;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.RecipeMapWorkable;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;
import gregtech.common.blocks.ItemMachines;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch.Inst;
import reobf.proghatches.gt.metatileentity.util.IMultiplePatternPushable;
import reobf.proghatches.gt.metatileentity.util.ISpecialOptimize;
import reobf.proghatches.item.ItemProgrammingCircuit;

public class RecipeFilterCRIB extends PatternDualInputHatch  {
    public class Inst2 extends RecipeFilterCRIB {

        public Inst2(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
            int bufferNum) {
            super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
        }
        public Inst2(String mName, byte mTier,int slots, String[] mDescriptionArray, ITexture[][][] mTextures, boolean mMultiFluid,
                int bufferNum) {
                super(mName, mTier,slots, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
            }
        @Override
        public boolean supportsFluids() {
            return RecipeFilterCRIB.this.supportsFluids();
        }
        }
	public RecipeFilterCRIB(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum,
			boolean sf, int page, String... optional) {
		super(id, name, nameRegional, tier, mMultiFluid, bufferNum, sf, page, reobf.proghatches.main.Config.get(
                "RPDIH" ,
                ImmutableMap.of(
                    "bufferNum",
                    bufferNum,
                    "fluidSlots",
                    page*16/* fluidSlots() */, /*
                                           * "cap", format.format((int)
                                           * (4000 * Math.pow(4, tier) /
                                           * (mMultiFluid ? 4 : 1))),
                                           */
                    "mMultiFluid",
                    mMultiFluid,
                    "slots",
                    page*16/*
                                                          * , "stacksize", (int) (64 *
                                                          * Math.pow(2, Math.max(tier - 3, 0)))
                                                          */))

        );

	}
	   public RecipeFilterCRIB(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
		        boolean mMultiFluid, int bufferNum) {
		        super(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

		    }
		    public RecipeFilterCRIB(String mName, byte mTier,int slots, String[] mDescriptionArray, ITexture[][][] mTextures,
		            boolean mMultiFluid, int bufferNum) {
		            super(mName, mTier,slots, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

		        }
		    
	
	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
	
		 return new Inst2(mName, mTier,16*page()+1, mDescriptionArray, mTextures, mMultiFluid, bufferNum);
	}
	
	@Override
	public boolean disablePatternSlots() {
	return true;
	}

	@Override
	public boolean shouldDisplay() {
		
		return false;
	}
	boolean acceptOredict;
	int eutFilter=Integer.MAX_VALUE;
	ItemStack filter;
	ItemStack filterCache;
	ItemList genPatterns=new ItemList();
	ICraftingPatternDetails[] genPatternsDetails=new ICraftingPatternDetails[]{};
	String modeHint;
	String modeHintLangKey;
	int recipeIndex;
	
	
@Override
public void saveNBTData(NBTTagCompound aNBT) {
	if(modeHint!=null)
	aNBT.setString("modeHint", modeHint);
	if(modeHintLangKey!=null)
	aNBT.setString("modeHintLangKey", modeHintLangKey);	
	if(filter!=null)
	aNBT.setTag("filter", filter.writeToNBT(new NBTTagCompound()));
	if(filterCache!=null)
	aNBT.setTag("filterCache", filterCache.writeToNBT(new NBTTagCompound()));	
	NBTTagList tagList = new NBTTagList();
    for (IAEItemStack stack : genPatterns) {
        NBTTagCompound itemTag = new NBTTagCompound();
        stack.writeToNBT(itemTag);
        tagList.appendTag(itemTag);
    }
    aNBT.setTag("genPatterns", tagList);
	
    aNBT.setLong("randomID",randomID);
	super.saveNBTData(aNBT);
}
@Override
public void loadNBTData(NBTTagCompound aNBT) {
	modeHint=aNBT.getString("modeHint");
	if(modeHint.isEmpty())modeHint=null;
	modeHintLangKey=aNBT.getString("modeHintLangKey");
	if(modeHintLangKey.isEmpty())modeHintLangKey=null;
	filter=ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("filter"));
	filterCache=ItemStack.loadItemStackFromNBT(aNBT.getCompoundTag("filterCache"));
	 NBTTagList tagList = aNBT.getTagList("genPatterns", 10);
	    int length = tagList.tagCount();
	     genPatterns .clear();//= new ItemStack[length];
	    for (int i = 0; i < length; i++) {
	    	genPatterns.add(AEItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(i)));
	   }
	for(IAEItemStack p:genPatterns){}
	genPatternsDetails=new ICraftingPatternDetails[genPatterns.size()];
	int i=0;
	for(IAEItemStack p:genPatterns){
			
		genPatternsDetails[i++]=((ICraftingPatternItem)p.getItem()).getPatternForItem(p.getItemStack(), this.getBaseMetaTileEntity().getWorld());
	}
	randomID=aNBT.getLong("randomID");
	super.loadNBTData(aNBT);
}

Deque<IAEItemStack> tonitify=new ArrayDeque<>();


private void postChangeInner(StorageChannel c,IAEItemStack is){
	if(this.getBaseMetaTileEntity().getWorld().isRemote==false)
	tonitify.push(is);
	/*
	 //cannot do it here...
	  try {
		fakeNode.getProxy()
		.getStorage()
		.postAlterationOfStoredItems(
		    StorageChannel.ITEMS,
		    ImmutableList.of(
		        is),
		    new MachineSource(RecipeFilterCRIB.this));
	} catch (GridAccessException e) {
	}*/
}
	public boolean updaterFilter(){
		
		
		
	
		for(IAEItemStack get:genPatterns){
			IAEItemStack cp = get.copy();
			cp.setStackSize(-cp.getStackSize());
			postChangeInner(StorageChannel.ITEMS,cp);
		}
		//if(ItemStack.areItemStacksEqual(filter, filterCache))return false;
		if(filter==null){return false;}
		List<RecipeMap<?>> get = getItemStackMachineRecipeMap(filter);
		if(get.size()==0){return false;}
		genPatterns.clear();
		genPatternsDetails=new ICraftingPatternDetails[]{};
		/*if(filter==null){
			filterCache=null;
			postMEPatternChange();
			return true;
		}*/
		filterCache=filter.copy();
		recipeIndex=recipeIndex%get.size();
		modeHint=null;
		if(get.size()>1){
			modeHint="Machine modes ("+(recipeIndex+1)+"/"+get.size()+"): "+"%s";
			modeHintLangKey=get.get(recipeIndex).unlocalizedName;
		}
		
		genPatterns=assemble(get.get(recipeIndex));
		refreshDetails();
		for(IAEItemStack getx:genPatterns){
			IAEItemStack cp = getx.copy();
			//cp.setStackSize(-cp.getStackSize());
			postChangeInner(StorageChannel.ITEMS,cp);
		}
		return true;
	}
	private void refreshDetails(){
		for(IAEItemStack p:genPatterns){}
		genPatternsDetails=new ICraftingPatternDetails[genPatterns.size()];
		int i=0;
		for(IAEItemStack p:genPatterns){
			
			genPatternsDetails[i++]=((ICraftingPatternItem)p.getItem()).getPatternForItem(p.getItemStack(), this.getBaseMetaTileEntity().getWorld());
				}
		 postMEPatternChange();
	}
	
	
	
	private static List<RecipeMap<?>> getItemStackMachineRecipeMap(ItemStack stack) {
        if (stack != null) {
            IMetaTileEntity metaTileEntity = ItemMachines.getMetaTileEntity(stack);
            if (metaTileEntity != null) {
                return getMetaTileEntityRecipeMap(metaTileEntity);
            }
        }
        return Collections.emptyList();
    }

    private static List<RecipeMap<?>> getMetaTileEntityRecipeMap(IMetaTileEntity metaTileEntity) {
        if (metaTileEntity instanceof RecipeMapWorkable ) {
            Collection<RecipeMap<?>> get = ((RecipeMapWorkable) metaTileEntity).getAvailableRecipeMaps();
            if(get instanceof List)return (List<RecipeMap<?>>) get;
            return new ArrayList<>(get);
          
            
        }
        return Collections.emptyList();
    }
    
    private static AEItemStack zeroToCircuit(AEItemStack in){
    	if(in.getStackSize()<=0)return AEItemStack.create(ItemProgrammingCircuit.wrap(in.getItemStack()));
    	
    	
    	return in;
    }
    static private AEItemStack[] E=new AEItemStack[0];
    private ItemList assemble(RecipeMap<?> map){
		ItemList all=new ItemList();
		if(Platform.isClient())return all;
    	for(GTRecipe xx:map.getAllRecipes()){
    		if(xx.mEUt>eutFilter){continue;}
		 ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);
         //FluidPatternDetails pattern = new FluidPatternDetails(patternStack);
         
        /* Stream<IAEItemStack> inputs = Stream.concat(
         Arrays.stream(xx.mInputs).filter(Objects::nonNull).map(AEItemStack::create).map(RecipeFilterCRIB::zeroToCircuit),
         Arrays.stream(xx.mFluidInputs).filter(Objects::nonNull).map(s->ItemFluidDrop.newAeStack(s)));*/
         List<IAEItemStack> inputsList = new ArrayList<>();
       
		for (ItemStack input : xx.mInputs) {
        	    if (input != null) {
        	        AEItemStack aeStack = AEItemStack.create(input);
        	        IAEItemStack processedStack = RecipeFilterCRIB.zeroToCircuit(aeStack);
        	        inputsList.add(processedStack);
        	    }
        	}
		for (FluidStack fluidInput : xx.mFluidInputs) {
        	    if (fluidInput != null) {
        	        IAEItemStack fluidStack = ItemFluidDrop.newAeStack(fluidInput);
        	        inputsList.add(fluidStack);
        	    }
        	}

         
         List<IAEItemStack> outputsList = new ArrayList<>();
         int index = 0;

     
         for (ItemStack output : xx.mOutputs) {
           
             boolean condition = xx.mChances == null || (index < xx.mChances.length && xx.mChances[index] >= 10000);
             if (condition && output != null) {
                 IAEItemStack aeStack = AEItemStack.create(output);
                 outputsList.add(aeStack);
             }
             index++;
         }

       
         for (FluidStack fluidOutput : xx.mFluidOutputs) {
             if (fluidOutput != null) {
                 IAEItemStack fluidStack = ItemFluidDrop.newAeStack(fluidOutput);
                 outputsList.add(fluidStack);
             }
         }
         
         
        
         if(inputsList.isEmpty())inputsList.add(AEItemStack.create(new ItemStack(Items.paper).setStackDisplayName("No inputs")));
         if(outputsList.isEmpty())outputsList.add(AEItemStack.create(new ItemStack(Items.paper).setStackDisplayName("No outputs")));
         
         
         
     
         NBTTagCompound tag = new NBTTagCompound();
         NBTTagList tag2;
         tag.setTag("Inputs", tag2=FluidPatternDetails.writeStackArray(inputsList.toArray(E)));
         tag.setTag("in", tag2.copy());
        
         tag.setTag("Outputs", tag2=FluidPatternDetails.writeStackArray(outputsList.toArray(E)));
         tag.setTag("out",tag2.copy());
         tag.setInteger("combine", 0);
         tag.setBoolean("beSubstitute",false);
        
         patternStack.setTagCompound(tag);
   
         
         
         all.add(AEItemStack.create(patternStack));
         
		}
    	return all;
    }
    
    @Override
    public void provideCrafting(ICraftingProviderHelper craftingTracker) {
        if (!isActive()) return;
        
    
        for(ICraftingPatternDetails d:genPatternsDetails){craftingTracker.addCraftingOption(this, d);}
    
    
    
    }
    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
    		ItemStack aTool) {
    	
    	recipeIndex++;
    	/*updaterFilter();
    	
    	
    	if(modeHint!=null){
    		aPlayer.addChatMessage(new ChatComponentText("Generated recipes: "+this.genPatternsDetails.length));
			aPlayer.addChatMessage(new ChatComponentTranslation(modeHint,new ChatComponentTranslation(modeHintLangKey)));
			//aPlayer.addChatMessage(new ChatComponentText("Use a screw driver to switch modes."));
		}else{
			aPlayer.addChatMessage(new ChatComponentText("No recipe."));
			
		}
    	*/
     

    	b:{
    		List<RecipeMap<?>> get = getItemStackMachineRecipeMap(filter);
    		if(stage>0){
    			aPlayer.addChatMessage(new ChatComponentText("Still generating!"));
    			break b;
    		}
    		
    		
    		recipeIndex=recipeIndex%get.size();
    		modeHint=null;
    		if(get.size()>1){
    			modeHint="Machine modes ("+(recipeIndex+1)+"/"+get.size()+"): "+"%s";
    			modeHintLangKey=get.get(recipeIndex).unlocalizedName;
    		}
    		if(getBaseMetaTileEntity().getWorld().isRemote==false)
    	regJob(get.get(recipeIndex));
    	return ;
    	}
    	
    	
    	
    	
    }

    public static long mean(long[] values, int currentTick, int recentCount) {
        long sum = 0L;
        int count = 0;
        
     
        for (int i = 0; i < recentCount; i++) {
       
            int index = (currentTick - i - 1) % values.length;
            if (index < 0) {
                index += values.length;
            }
            
           
            if (index >= 0 && index < values.length && values[index] > 0) {
                sum += values[index];
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    public static double getWorldTickTimeAll() {
    	double sum=0;
    	try{
    	for(int i:MinecraftServer.getServer().worldTickTimes.keySet())
    	sum+=mean(MinecraftServer.getServer().worldTickTimes.get(i),MinecraftServer.getServer(). getTickCounter()-1,10)
                * 1.0E-9D;}catch(Exception e){}
    	
    	return sum;
    	
    	
    }
    
    
    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer, ForgeDirection side,
    		float aX, float aY, float aZ) {
    	
    	if(aPlayer.getHeldItem()!=null){
    	final ItemStack term = aPlayer.getHeldItem().copy();
        INetworkEncodable networkEncodable = null;

        if (term.getItem() instanceof INetworkEncodable) {
            networkEncodable = (INetworkEncodable) term.getItem();
        }

        final IWirelessTermHandler wTermHandler = AEApi.instance().registries().wireless()
                .getWirelessTerminalHandler(term);
        if (wTermHandler != null) {
            networkEncodable = wTermHandler;
        }

        if (networkEncodable != null) {
            networkEncodable.setEncryptionKey(term, String.valueOf(randomID), "");
            aPlayer.getHeldItem().setTagCompound(term.getTagCompound());
        if(aPlayer.getEntityWorld().isRemote==false){
    		aPlayer.addChatMessage(new ChatComponentText("Linked."));
    		aPlayer.addChatMessage(new ChatComponentText("Click unwanted patterns to remove."));
    		}
        return true;
        }
    	}
       
    	
    	
    	
    	ItemStack old = filter;
    	filter=aPlayer.getHeldItem();
    	if(filter!=null)filter=filter.copy();
    	b:{
    		if(filter==null){break b;}
    		List<RecipeMap<?>> get = getItemStackMachineRecipeMap(filter);
    		if(get.size()==0){break b;}
    		if(stage>0){
    			aPlayer.addChatMessage(new ChatComponentText("Still generating!"));
    			break b;
    		}
    		
    		
    		recipeIndex=recipeIndex%get.size();
    		modeHint=null;
    		if(get.size()>1){
    			modeHint="Machine modes ("+(recipeIndex+1)+"/"+get.size()+"): "+"%s";
    			modeHintLangKey=get.get(recipeIndex).unlocalizedName;
    		}
    		if(getBaseMetaTileEntity().getWorld().isRemote==false)
    	regJob(get.get(recipeIndex));
    	return true;
    	}
    	filter=old;
    	/*
     	ItemStack old = filter;
    	filter=aPlayer.getHeldItem();
    	if(filter!=null)filter=filter.copy();
    	if(updaterFilter()){
    		if(aPlayer.getEntityWorld().isRemote==false){
    		aPlayer.addChatMessage(new ChatComponentText("Recipe updated."));
    		aPlayer.addChatMessage(new ChatComponentText("Generated recipes: "+this.genPatternsDetails.length));
    		if(modeHint!=null){
    			aPlayer.addChatMessage(new ChatComponentTranslation(modeHint,new ChatComponentTranslation(modeHintLangKey)));
    			aPlayer.addChatMessage(new ChatComponentText("Use a screw driver to switch modes."));
    		}
    		
    		
    		
    		}return true;
    	}
    	filter=old;
    	
    	*/
    	
    	
    	
    	return super.onRightclick(aBaseMetaTileEntity, aPlayer, side, aX, aY, aZ);
    }
    //0 idle
    //1 genitem
    //2 genpat
    //3 finish
    int stage=0; 
    int progress;
    Iterator<GTRecipe> todo;
    ArrayList<AEItemStack> pitem;
    ArrayList<ICraftingPatternDetails> pd;
    public void step(){
    	if(stage==0)return;
    	if(stage==3){
    		 EntityPlayer aPlayer = getBaseMetaTileEntity().getWorld().getClosestPlayer(
    				getBaseMetaTileEntity().getXCoord(), getBaseMetaTileEntity().getYCoord(), getBaseMetaTileEntity().getZCoord(), 100);
    		
    
    		
    		for(IAEItemStack get:genPatterns){
    			IAEItemStack cp = get.copy();
    			cp.setStackSize(-cp.getStackSize());
    			postChangeInner(StorageChannel.ITEMS,cp);
    		}
    		genPatterns=new ItemList();
    		for(AEItemStack item:pitem){genPatterns.add(item);};
    		genPatternsDetails= pd.toArray(new ICraftingPatternDetails[0]);;
    		
    		for(IAEItemStack getx:genPatterns){
    			IAEItemStack cp = getx.copy();
    			postChangeInner(StorageChannel.ITEMS,cp);
    		}
    		 postMEPatternChange();
    		 if(aPlayer!=null && aPlayer.getEntityWorld().isRemote==false){
         		aPlayer.addChatMessage(new ChatComponentText("Recipe updated."));
         		aPlayer.addChatMessage(new ChatComponentText("Generated recipes: "+this.genPatternsDetails.length));
         		if(modeHint!=null){
         			aPlayer.addChatMessage(new ChatComponentTranslation(modeHint,new ChatComponentTranslation(modeHintLangKey)));
         			aPlayer.addChatMessage(new ChatComponentText("Use a screw driver to switch modes."));
         	}
     		 }
         		
    		stage=0;
    		pitem=null;
    		pd=null;
    		
    	}
    	if(stage==2){
    		
    		int to=updatesPerTick();
    		while(to>0){
    			to--;
    			if(progress>=pitem.size()){
    				
    				stage=3;
    				return;
    			}
    			AEItemStack p = pitem.get(progress++);
    			pd.add(((ICraftingPatternItem)p.getItem()).getPatternForItem(p.getItemStack(), this.getBaseMetaTileEntity().getWorld()));
        	}
    		
    		/*for(IAEItemStack p:pitem){}
    		genPatternsDetails=new ICraftingPatternDetails[genPatterns.size()];
    		int i=0;
    		for(IAEItemStack p:genPatterns){
    			
    			genPatternsDetails[i++]=((ICraftingPatternItem)p.getItem()).getPatternForItem(p.getItemStack(), this.getBaseMetaTileEntity().getWorld());
    	}*/
    		
    		
    	}
    	if(stage==1){
    		int to=updatesPerTick();
    		while(to>0){
    			if(todo.hasNext()==false){stage=2;todo=null;return;}
    		GTRecipe xx = todo.next();
    		if(xx.mEUt>eutFilter){continue;}
    		to--;
    		
   		 ItemStack patternStack = new ItemStack(ItemAndBlockHolder.PATTERN);

            List<IAEItemStack> inputsList = new ArrayList<>();
          
   		for (ItemStack input : xx.mInputs) {
           	    if (input != null) {
           	        AEItemStack aeStack = AEItemStack.create(input);
           	        IAEItemStack processedStack = RecipeFilterCRIB.zeroToCircuit(aeStack);
           	        inputsList.add(processedStack);
           	    }
           	}
   		for (FluidStack fluidInput : xx.mFluidInputs) {
           	    if (fluidInput != null) {
           	        IAEItemStack fluidStack = ItemFluidDrop.newAeStack(fluidInput);
           	        inputsList.add(fluidStack);
           	    }
           	}

            
            List<IAEItemStack> outputsList = new ArrayList<>();
            int index = 0;

        
            for (ItemStack output : xx.mOutputs) {
              
                boolean condition = xx.mChances == null || (index < xx.mChances.length && xx.mChances[index] >= 10000);
                if (condition && output != null) {
                    IAEItemStack aeStack = AEItemStack.create(output);
                    outputsList.add(aeStack);
                }
                index++;
            }

          
            for (FluidStack fluidOutput : xx.mFluidOutputs) {
                if (fluidOutput != null) {
                    IAEItemStack fluidStack = ItemFluidDrop.newAeStack(fluidOutput);
                    outputsList.add(fluidStack);
                }
            }
            
            
           
            if(inputsList.isEmpty())inputsList.add(AEItemStack.create(new ItemStack(Items.paper).setStackDisplayName("No inputs")));
            if(outputsList.isEmpty())outputsList.add(AEItemStack.create(new ItemStack(Items.paper).setStackDisplayName("No outputs")));
            
            
            
        
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList tag2;
            tag.setTag("Inputs", tag2=FluidPatternDetails.writeStackArray(inputsList.toArray(E)));
            tag.setTag("in", tag2.copy());
           
            tag.setTag("Outputs", tag2=FluidPatternDetails.writeStackArray(outputsList.toArray(E)));
            tag.setTag("out",tag2.copy());
            tag.setInteger("combine", 0);
            tag.setBoolean("beSubstitute",false);
           
            patternStack.setTagCompound(tag);
      
            
            
            pitem.add(AEItemStack.create(patternStack));
    		
    	}
    	}
    	
    	
    	
    }
    private int updatesPerTick() {
    	int maxUpdates = 100;
    	   double get = getWorldTickTimeAll();
    	    if (get < 0.050) return  maxUpdates;  
    	    if (get < 0.100) return (int) (maxUpdates - (get - 0.050) / 0.050 * (maxUpdates - 10));
    	    return 10;
	}
	public void regJob(RecipeMap re){
    	progress=0;
    	stage=1;
    	todo=re.getAllRecipes().iterator();
    	pitem=new ArrayList<>();
    	pd=new ArrayList<>();
    }
    @Override
    public AENetworkProxy getProxy() {
    	
    	return super.getProxy();
    }
    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
    	if(aBaseMetaTileEntity.getWorld().isRemote==false){
    	/*System.out.println(fakeNode.xx.getGrid());
    	try {
			System.out.println(fakeNode.pro.getGrid());
		} catch (GridAccessException e) {
		e.printStackTrace();
		}*/
    		
    	}//System.out.println(getWorldTickTimeAll());
    	if(getBaseMetaTileEntity().getWorld().isRemote==false){
    		step();
    	}
    	while(tonitify.isEmpty()==false){
    		try {
    			fakeNode.getProxy()
    			.getStorage()
    			.postAlterationOfStoredItems(
    			    StorageChannel.ITEMS,
    			    ImmutableList.of(
    			    		tonitify.removeLast()),
    			    new MachineSource(RecipeFilterCRIB.this));
    		} catch (GridAccessException e) {
    		}
    	}
    	
    	 if (fakeNode.displayNeedsUpdate) {
    		 fakeNode.recalculateDisplay();
         }
    	super.onPostTick(aBaseMetaTileEntity, aTimer);
    }
    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
    	fakeNode.pro.onReady();
    	fakeNode.xx.setWorldObj(aBaseMetaTileEntity.getWorld());
    	fakeNode.xx.xCoord=aBaseMetaTileEntity.getXCoord();
    	fakeNode.xx.yCoord=aBaseMetaTileEntity.getYCoord();
    	fakeNode.xx.zCoord=aBaseMetaTileEntity.getZCoord();
    	fakeNode.xx.onReady();

    	try {
			new GridConnection(fakeNode.pro.getNode(), fakeNode.xx.getGridNode(ForgeDirection.UNKNOWN), ForgeDirection.UNKNOWN);
		} catch (Exception e) {
			//this will fail on client, but I just don't care
			//e.printStackTrace();
		}
    	
    	super.onFirstTick(aBaseMetaTileEntity);
    	if (Platform.isServer()) {
           
            MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(fakeNode, LocatableEvent.Register));
        }
    }

     @Override
     public void onRemoval() {
    	 fakeNode.pro.invalidate();
    	 fakeNode.xx.invalidate();
    	 super.onRemoval();
        MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(fakeNode, LocatableEvent.Unregister));
     
    }
     @Override
     public void onUnload() {
    	 fakeNode.pro.onChunkUnload();
    	 fakeNode.xx.onChunkUnload();
         super.onUnload();
         MinecraftForge.EVENT_BUS.post(new LocatableEventAnnounce(fakeNode, LocatableEvent.Unregister));
        
     }
     long randomID=new Random().nextLong()+System.currentTimeMillis();
    
     
     FakeNode fakeNode=new FakeNode();
     @SuppressWarnings("rawtypes")
	IMEInventoryHandler fakeInv=new IMEInventoryHandler(){

		@Override
		public IAEStack injectItems(IAEStack input, Actionable type, BaseActionSource src) {
			
			return input;//cannot inject
		}
		public  appeng.api.storage.data.IItemList getAvailableItems(appeng.api.storage.data.IItemList out) {
			for(IAEItemStack item:genPatterns){
				
				out.add(item);
			}
			return out;
		};    
		
	
		@Override
		public IAEStack extractItems(IAEStack request, Actionable mode, BaseActionSource src) {
			getBaseMetaTileEntity().markDirty();
			if(mode==Actionable.SIMULATE){
				return request;
			}
			IAEItemStack get = genPatterns.findPrecise((IAEItemStack) request);
			if(get!=null){
				// tell AE the pattern is dead
				IAEItemStack cp = get.copy();
				cp.setStackSize(-cp.getStackSize());
				postChangeInner(StorageChannel.ITEMS,cp);
			}
			
			if(get!=null)get.setStackSize(0);//poof！
			
			refreshDetails();
			 
			
			return null;// you get nothing!
		}

		@Override
		public StorageChannel getChannel() {
			
			return StorageChannel.ITEMS;
		}

		@Override
		public AccessRestriction getAccess() {
			
			return AccessRestriction.READ_WRITE;//write?
		}

		@Override
		public boolean isPrioritized(IAEStack input) {
			
			return false;
		}

		@Override
		public boolean canAccept(IAEStack input) {
		
			return false;//cannot inject!
		}

		@Override
		public int getPriority() {
		
			return 0;
		}

		@Override
		public int getSlot() {
			
			return 0;
		}

		@Override
		public boolean validForPass(int i) {
		
			return true;
		}};
     public class FakeNode implements ILocatable,IGridHost, IGridProxyable, IAEPowerStorage/*free power!*/,ICellContainer/*,IWirelessAccessPoint*/{
    	 @MENetworkEventSubscribe
    	    public void powerRender(final MENetworkPowerStatusChange c) {
    	        displayNeedsUpdate = true;
    	    }

    	    @MENetworkEventSubscribe
    	    public void channelRender(final MENetworkChannelsChanged c) {
    	        displayNeedsUpdate = true;
    	    }
    	    AENetworkProxy pro=new AENetworkProxy(this, "dontcare", new ItemStack(Items.apple), false);
       //{pro.setFlags(GridFlags.REQUIRE_CHANNEL);}
	    //a fake WAP, because wireless term only recognizes TileWireless!
        //IWirelessAccessPoint is just useless
       TileWireless xx=new TileWireless()/*{
    	   
    	   public DimensionalCoord getLocation() {
    		   
    		   return new DimensionalCoord((TileEntity) getBaseMetaTileEntity());
    	   };
    	   public double getRange() {return 256;};
       }*/;
	private boolean displayNeedsUpdate;
       
       
       
       @Override
		public long getLocatableSerial() {
			
			return randomID;
		}

		@Override
		public IGridNode getGridNode(ForgeDirection dir) {
			
			return pro.getNode();
		}

		@Override
		public AECableType getCableConnectionType(ForgeDirection dir) {
		
			return AECableType.NONE;
		}

		@Override
		public void securityBreak() {
		
			
		}

		@Override
		public AENetworkProxy getProxy() {
			
			return pro;
		}

		@Override
		public DimensionalCoord getLocation() {
			
			IGregTechTileEntity te = getBaseMetaTileEntity();
			return new DimensionalCoord( te.getWorld(),te.getXCoord(),te.getYCoord(),te.getZCoord());
		}

		@Override
		public void gridChanged() {
			
			
		}

		@Override
		public double extractAEPower(double amt, Actionable mode, PowerMultiplier usePowerMultiplier) {
			
			return amt;
		}

		@Override
		public double injectAEPower(double amt, Actionable mode) {
			
			return 0;
		}

		@Override
		public double getAEMaxPower() {
		
			return Integer.MAX_VALUE/2;
		}

		@Override
		public double getAECurrentPower() {
		
			return Integer.MAX_VALUE/2;
		}

		@Override
		public boolean isAEPublicPowerStorage() {
			
			return true;//false?
		}

		@Override
		public AccessRestriction getPowerFlow() {
		
			return AccessRestriction.READ_WRITE;
		}

		@Override
		public List<IMEInventoryHandler> getCellArray(StorageChannel channel) {
			
			if(channel==StorageChannel.FLUIDS)return ImmutableList.of();
			return ImmutableList.of(fakeInv);
		}

		@Override
		public int getPriority() {
			
			return 0;
		}

		@Override
		public IGridNode getActionableNode() {
		
			return getProxy().getNode();
		}

		@Override
		public void saveChanges(IMEInventory cellInventory) {
		
			
		}

		/*@Override
		public IGridNode getActionableNode() {
		
			return getProxy().getNode();
		}

		@Override
		public double getRange() {
		
			return 256;
		}

		@Override
		public boolean isActive() {
		
			return true;
		}

		@Override
		public IGrid getGrid() {
			
			try {
				return getProxy().getGrid();
			} catch (GridAccessException e) {
			
				
			}
			return null;
		}*/
		
		public void recalculateDisplay() {
	        this.displayNeedsUpdate = false;

	       
	            try {
	                this.getProxy().getGrid().postEvent(new MENetworkCellArrayUpdate());
	            } catch (final GridAccessException e) {
	                // :P
	            }
	        }

	       
	    
     };
	
	
	}