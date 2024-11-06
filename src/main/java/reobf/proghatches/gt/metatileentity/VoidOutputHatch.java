package reobf.proghatches.gt.metatileentity;

import static gregtech.api.util.GT_Utility.moveMultipleItemStacks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.common.GT_Client;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.MappingFluidTank;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

public class VoidOutputHatch  extends GT_MetaTileEntity_Hatch_Output {

	public VoidOutputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures
			) {
		super(mName, mTier,0, mDescriptionArray, mTextures);
		
	}
    FluidStack[] filter=new FluidStack[16];
   
    
    
	Predicate<FluidStack> filterPredicate= (s)->false;
    
    
    public void rebuildFilter(){
    filterPredicate= (s)->false;
    	for( FluidStack is: filter){
    		if(is!=null){
    			filterPredicate=filterPredicate.or(
    					s->{
    						return s!=null&&is.getFluid().equals(s.getFluid());
    					}
    					);
    		}
    		
    	}
    }
    
    
  
    
	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		
		for( int ii=0;ii<16;ii++){
			final int i=ii;
		 builder.widget(FluidSlotWidget.phantom(new MappingFluidTank(s->{filter[i]=s==null?null:s.copy();rebuildFilter();}, ()->filter[i]), 
				 false
				 ).setPos(3+(i%9)*4, 3+(i/9)*4));
				
				 ;}
		
	/*	
		builder .widget(
                 SlotGroup.ofItemHandler(handler, 4).startFromSlot(0).endAtSlot(15)
                         .background(getGUITextureSet().getItemSlot())
                         .slotCreator(i -> new BaseSlot(handler, i, true) {

                             @Override
                             public ItemStack getStack() {
                                 return isEnabled() ? super.getStack() : null;
                             }

                     
                         }).build().setPos(7, 24));*/
	}

	public VoidOutputHatch(int aID, String aName, String aNameRegional, int tier) {
		super(aID, aName, aNameRegional, tier,
				
				new String[0]
				/*reobf.proghatches.main.Config.get("FOB",
				ImmutableMap.of())
				*/
				
				,0
		

		);
		
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}



	@Override
	public VoidOutputHatch newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new VoidOutputHatch(mName, mTier, mDescriptionArray, mTextures );
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		super.loadNBTData(compound);
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
	        Arrays.fill(this.filter,null);

	       

	        for (int i = 0; i < nbttaglist.tagCount(); ++i)
	        {
	            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
	            int j = nbttagcompound1.getByte("Slot") & 255;

	            if (j >= 0 && j < this.filter.length)
	            {
	                this.filter[j] = FluidStack.loadFluidStackFromNBT(nbttagcompound1);
	            }
	        }
	        
	        NBTTagList nbttaglist0 = compound.getTagList("Fluids", 10);
	       
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		super.saveNBTData(compound);
		NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.filter.length; ++i)
        {
            if (this.filter[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.filter[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
         nbttaglist = new NBTTagList();

   
	}

	public boolean dump(FluidStack aStack) {
		return filterPredicate.test(aStack);
		
	}
	  public boolean outputsSteam() {
	        return false;
	    }

	    public boolean outputsLiquids() {
	    	 return false;
	    }

	    public boolean outputsItems() {
	    	 return false;
	    }

@Override
public boolean doesFillContainers() {
    return false;
}

@Override
public boolean doesEmptyContainers() {
    return false;
}
}

