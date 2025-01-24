package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_PIPE_IN;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.helpers.ICustomNameObject;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import gregtech.api.render.TextureFactory;
import gregtech.common.tileentities.machines.IDualInputHatch;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import reobf.proghatches.block.BlockIOHub;
import reobf.proghatches.gt.metatileentity.DualInputHatch.Net;
import reobf.proghatches.gt.metatileentity.util.ICraftingV2;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.ISkipStackSizeCheck;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class DualInputHatchInventoryMappingSlave<T extends MetaTileEntity & IDualInputHatch&IMetaTileEntity> 
extends MTETieredMachineBlock implements ISkipStackSizeCheck,IDataCopyablePlaceHolder,ICraftingV2{
	private T master; // use getMaster() to access
	private int masterX, masterY, masterZ;
	private boolean masterSet = false; // indicate if values of masterX,
										// masterY, masterZ are valid
	public DualInputHatchInventoryMappingSlave(int aID, String aName, String aNameRegional, int aTier
			 ) {
		super(aID, aName, aNameRegional, aTier, 0, 
				
				
				
				Config.get("DIHIMS", ImmutableMap.of()));
		Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
	}
	public DualInputHatchInventoryMappingSlave(String aName, int aTier, int aInvSlotCount, String[] aDescription,
			ITexture[][][] aTextures) {
		super(aName, aTier, aInvSlotCount, aDescription, aTextures);
	
	}

	@Override
	public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
	
		return new DualInputHatchInventoryMappingSlave<>(mName, mTier, 0, mDescriptionArray, mTextures);
	}
	@Override
	public void saveNBTData(NBTTagCompound aNBT) {
		aNBT.setBoolean("allowAllSides",allowAllSides);
		if (masterSet) {
			NBTTagCompound masterNBT = new NBTTagCompound();
			masterNBT.setInteger("x", masterX);
			masterNBT.setInteger("y", masterY);
			masterNBT.setInteger("z", masterZ);
			aNBT.setTag("master", masterNBT);
		}
		
	}
	@Override
	public void loadNBTData(NBTTagCompound aNBT) {
		if(aNBT.hasKey("x")==false)return;
		
		allowAllSides=aNBT.getBoolean("allowAllSides");
		if (aNBT.hasKey("master")) {
			NBTTagCompound masterNBT = aNBT.getCompoundTag("master");
			masterX = masterNBT.getInteger("x");
			masterY = masterNBT.getInteger("y");
			masterZ = masterNBT.getInteger("z");
			masterSet = true;
		}
	}
	@Override
	public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		if(!passSideCheck(side)){return false;}
		T master;
		if((master=getMaster())!=null){
			return master.allowPullStack(aBaseMetaTileEntity, aIndex, getMasterFront(), aStack);
		};
		return false;
	}
	@Override
	public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
			ItemStack aStack) {
		if(!passSideCheck(side)){return false;}
		T master;
		if((master=getMaster())!=null){
			return master.allowPutStack(aBaseMetaTileEntity, aIndex, getMasterFront(), aStack);
		};
		return false;
	}
	@Override
	public boolean canDrain(ForgeDirection side, Fluid aFluid) {
		T master;
		if((master=getMaster())!=null){
			return master.canDrain(getMasterFront(), aFluid);
		};
		return false;
	}
	@Override
	public boolean canFill(ForgeDirection side, Fluid aFluid) {
		if(!passSideCheck(side)){return false;}
		T master;
		if((master=getMaster())!=null){
			return master.canFill(getMasterFront(), aFluid);
		};
		return false;
	}
	@Override
	public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {
		if(!passSideCheck(ordinalSide)){return false;}
		T master;
		if((master=getMaster())!=null){
			return master.canExtractItem(aIndex, aStack, getMasterFront().ordinal());
		};
		return false;
	}
	@Override
	public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {
		if(!passSideCheck(ordinalSide)){return false;}
		if((master=getMaster())!=null){
			return master.canInsertItem(aIndex, aStack,  getMasterFront().ordinal());
		};
		return false;
	}
	@Override
	public ItemStackHandler getInventoryHandler() {
		if((master=getMaster())!=null){
			return master.getInventoryHandler();
		};
		return super.getInventoryHandler();
	}
	
	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if((master=getMaster())!=null){
			return master.fill(resource, doFill);
		};
		return 0;
	}
	@Override
	public int fill(ForgeDirection side, FluidStack aFluid, boolean doFill) {
		if(!passSideCheck(side)){return 0;}
		if((master=getMaster())!=null){
			return master.fill( getMasterFront(), aFluid, doFill);
		};
		return 0;
	}
	@Override
	public FluidStack drain(ForgeDirection side, FluidStack aFluid, boolean doDrain) {
		if(!passSideCheck(side)){return null;}
		if((master=getMaster())!=null){
			return master.drain( getMasterFront(), aFluid, doDrain);
		};
		return null;
	}
	@Override
	public FluidStack drain(ForgeDirection side, int maxDrain, boolean doDrain) {
		if((master=getMaster())!=null){
			return master.drain( getMasterFront(), maxDrain, doDrain);
		};
		return null;
	}
	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if((master=getMaster())!=null){
			return master.drain(maxDrain, doDrain);
		};
		return null;
	}
	
	@Override
	public ItemStack decrStackSize(int aIndex, int aAmount) {
		if((master=getMaster())!=null){
			return master.decrStackSize(aIndex, aAmount);
		};
		return null;
	}
	@Override
	public boolean isItemValidForSlot(int aIndex, ItemStack aStack) {
		if((master=getMaster())!=null){
			return master.isItemValidForSlot(aIndex, aStack);
		};
		return false;
	}
	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		if((master=getMaster())!=null){
			master.setInventorySlotContents(aIndex, aStack);
		};
		
	}
	@Override
	public ItemStack getStackInSlot(int aIndex) {
		if((master=getMaster())!=null){
			return master.getStackInSlot(aIndex);
		};
		return null;
	}
	@Override
	public int getInventoryStackLimit() {
		if((master=getMaster())!=null){
			return master.getInventoryStackLimit();
		};
		return 0;
	}
	@Override
	public int getSizeInventory() {
		if((master=getMaster())!=null){
			return master.getSizeInventory();
		};
		return 0;
	}
	@Override
	public FluidStack getFluid() {
		if((master=getMaster())!=null){
			return master.getFluid();
		};
		return null;
	}
	@Override
	public int getFluidAmount() {
		if((master=getMaster())!=null){
			return master.getFluidAmount();
		};
		return 0;
	}
	
	
	@Override
	public boolean shouldDropItemAt(int index) {
		return false;
	}
	
	@Override
	public boolean isAccessAllowed(EntityPlayer aPlayer) {
		
		return true;
	}
	@Override
	public ITexture[] getTexture(IGregTechTileEntity baseMetaTileEntity, ForgeDirection side, ForgeDirection facing,
			int colorIndex, boolean active, boolean redstoneLevel) {
	
	 
	            if (side != facing) 
	             
	                 return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1] };
	                else return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[mTier][colorIndex + 1], TextureFactory.of(MyMod.iohub, BlockIOHub.magicNO_inv_me_slave), TextureFactory.of(OVERLAY_PIPE_IN) };
	           
	}
	@Override
	public ITexture[][][] getTextureSet(ITexture[] aTextures) {
		
		  return new ITexture[0][0][0];
	}
	@Override
	public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
		super.onPostTick(aBaseMetaTileEntity, aTimer);
		if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
			trySetMasterFromCoord(masterX, masterY, masterZ);
		}
	}
	@SuppressWarnings("unchecked")
	public IDualInputHatch trySetMasterFromCoord(int x, int y, int z) {
		TileEntity tileEntity = getBaseMetaTileEntity().getWorld().getTileEntity(x, y, z);
		if (tileEntity == null)
			return null;
		if (!(tileEntity instanceof IGregTechTileEntity))
			return null;
		IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
		if (!(metaTileEntity instanceof IDualInputHatch))
			return null;

		if (!(metaTileEntity instanceof reobf.proghatches.gt.metatileentity.DualInputHatch))
			return null;

		masterX = x;
		masterY = y;
		masterZ = z;
		masterSet = true;
		master = (T) metaTileEntity;
		return master;
	}

	private boolean tryLinkDataStick(EntityPlayer aPlayer) {
		ItemStack dataStick = aPlayer.inventory.getCurrentItem();

		if (!ItemList.Tool_DataStick.isStackEqual(dataStick, false, true)) {
			return false;
		}
		if (dataStick.hasTagCompound()&&dataStick.stackTagCompound.getString("type")
	            .equals("CraftingInputBuffer")) {
			aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.slave.compat"));
	        return false;
	     }
		if (!dataStick.hasTagCompound()
				|| !dataStick.stackTagCompound.getString("type").equals("ProgHatchesDualInput")) {
			return false;
		}

		NBTTagCompound nbt = dataStick.stackTagCompound;
		int x = nbt.getInteger("x");
		int y = nbt.getInteger("y");
		int z = nbt.getInteger("z");
		if (trySetMasterFromCoord(x, y, z) != null) {
			aPlayer.addChatMessage(new ChatComponentText("Link successful"));
			return true;
		}
		aPlayer.addChatMessage(new ChatComponentText("Link failed"));
		return true;
	}

	@Override
	public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (!(aPlayer instanceof EntityPlayerMP)) {
			return false;
		}
		if (tryLinkDataStick(aPlayer)) {
			return true;
		}
		IDualInputHatch master = getMaster();
		if (master != null) {
			return ((MetaTileEntity) master).onRightclick(((IMetaTileEntity) master).getBaseMetaTileEntity(), aPlayer);
		}
		return false;
	}
	public T getMaster() {
		if (master == null)
			return null;
		if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master
																			// disappeared
			master = null;
		}
		return master;
	}
	
	public ForgeDirection getMasterFront() {
		if (master == null)
			return this.getBaseMetaTileEntity().getFrontFacing();//throw new RuntimeExcpetion()?
		//do not check master, becasue it's always called after getMaster()
		return master.getBaseMetaTileEntity().getFrontFacing();
	}@Override
	public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		NBTTagCompound tag = accessor.getNBTData();
		currenttip.add((tag.getBoolean("linked") ? "Linked" : "Not linked"));

		if (tag.hasKey("masterX")) {
			currenttip.add("Bound to " + tag.getInteger("masterX") + ", " + tag.getInteger("masterY") + ", "
					+ tag.getInteger("masterZ"));
		}

		
		  if (tag.hasKey("masterName")) {
		  currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName")
		  + EnumChatFormatting.RESET); }
		 

		super.getWailaBody(itemStack, currenttip, accessor, config);
	}@Override
	public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
			int z) {

		tag.setBoolean("linked", getMaster() != null);
		if (masterSet) {
			tag.setInteger("masterX", masterX);
			tag.setInteger("masterY", masterY);
			tag.setInteger("masterZ", masterZ);
		} 
		if (getMaster() != null) tag.setString("masterName", getNameOf(getMaster()));
		/*
		 * if (getMaster() != null) tag.setString("masterName",
		 * getMaster().getnam);
		 */

		super.getWailaNBTData(player, tile, tag, world, x, y, z);
	}  public String getNameOf(T tg) {
    	
    	if(tg instanceof ICustomNameObject){
    		ICustomNameObject iv=(ICustomNameObject) tg;
    		if(iv.hasCustomName())
    		return iv.getCustomName();
    		
    	}
    	
        StringBuilder name = new StringBuilder();
        if (tg instanceof ICraftingMedium &&((ICraftingMedium)tg).getCrafterIcon() != null) {
            name.append(((ICraftingMedium)tg).getCrafterIcon().getDisplayName());
        } else {
            name.append(tg.getLocalName());
        }

      
        return name.toString();
    }
	@Override
	public boolean isFacingValid(ForgeDirection facing) {
	
		return true;
	}
	boolean allowAllSides;
	public boolean passSideCheck(ForgeDirection dir){
		if(allowAllSides)return true;
		return this.getBaseMetaTileEntity().getFrontFacing()==dir;
	}
	public boolean passSideCheck(int dir){
		return passSideCheck(ForgeDirection.values()[dir]);
	}
	  @Override
	    public boolean onWireCutterRightClick(ForgeDirection side, ForgeDirection wrenchingSide, EntityPlayer aPlayer,
	        float aX, float aY, float aZ) {
		  allowAllSides = !allowAllSides;
	        
	        aPlayer.addChatComponentMessage(
	            new ChatComponentTranslation("GT5U.hatch.additionalConnection." + allowAllSides));
	        return true;
	    }
	  @Override
	public FluidTankInfo[] getTankInfo(ForgeDirection side) {
		  if(!passSideCheck(side)){return null;}
		  if((master=getMaster())!=null){
				return master.getTankInfo(side);
			};
		return super.getTankInfo(side);
	}
	  @Override
	  public NBTTagCompound getCopiedData(EntityPlayer player) {
	   NBTTagCompound ret=new NBTTagCompound();
	   writeType(ret, player);
	   ret.setInteger("masterX", masterX);
	   ret.setInteger("masterY", masterY);
	   ret.setInteger("masterZ", masterZ);
	   ret.setBoolean("masterSet", masterSet);
	  return ret;
	  }
	  @Override
	  public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) { 
	  	if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
	  	if(nbt.hasKey("masterX"))masterX=nbt.getInteger("masterX");
	  	if(nbt.hasKey("masterY")) masterY=nbt.getInteger("masterY");
	  	if(nbt.hasKey("masterZ"))masterZ=nbt.getInteger("masterZ");
	  	if(nbt.hasKey("masterSet")) masterSet=nbt.getBoolean("masterSet");
	  	master=null;
	  return true;
	  }
	
	@Override
	public boolean pushPatternCM(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
	
		if((master=getMaster())!=null){
			if(master instanceof ICraftingV2)
			return ((ICraftingV2)master).pushPatternCM(patternDetails, table, getMasterFront());
		};
		return false;
	}
	@Override
	public boolean acceptsPlansCM() {
		if((master=getMaster())!=null){
			if(master instanceof ICraftingV2)
			return ((ICraftingV2)master).acceptsPlansCM();
		};
		return false;
	}
	@Override
	public boolean enableCM() {
		if((master=getMaster())!=null){
			if(master instanceof ICraftingV2)
			return ((ICraftingV2)master).enableCM();
		};
		return false;
	}
}
