package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_DATA_ACCESS;
import static net.minecraft.util.StatCollector.translateToLocal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.github.technus.tectech.thing.casing.GT_Block_CasingsTT;
import com.github.technus.tectech.util.CommonValues;
import com.google.common.collect.ImmutableMap;

import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_DataAccess;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Utility;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.gt.metatileentity.util.IMEStorageChangeAwareness;
import reobf.proghatches.main.registration.Registration;


public class DataHatchME extends GT_MetaTileEntity_Hatch_DataAccess implements IPowerChannelState,IGridProxyable,IMEStorageChangeAwareness{
	public DataHatchME(int aID, String aName, String aNameRegional) {
		super(aID, aName, aNameRegional, 8);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}
	public DataHatchME(String aName, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, 8, aDescription, aTextures);
		
	} 
	@Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
        getProxy().onReady();
        updateCache();
    }
	  @Override
	    public String[] getDescription() {
		
	        return reobf.proghatches.main.Config
					.get("DHME", ImmutableMap.of());
	    }
	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
		return new DataHatchME(mName, mDescriptionArray, mTextures);
	}
	ItemStack[] inv=new ItemStack[0];
	private AENetworkProxy gridProxy;
@Override
public int getSizeInventory() {
	
	return inv.length;
}
@Override
public ItemStack getStackInSlot(int aIndex) {
	
	return inv[aIndex];
}   
private void updateValidGridProxySides() {
  
        getProxy().setValidSides(EnumSet.of(getBaseMetaTileEntity().getFrontFacing()));
    
}

@Override
public AENetworkProxy getProxy() {
    if (gridProxy == null) {
        if (getBaseMetaTileEntity() instanceof IGridProxyable) {
            gridProxy = new AENetworkProxy(
                (IGridProxyable) getBaseMetaTileEntity(),
                "proxy",
                new ItemStack(Items.apple),
                true);
            gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
            updateValidGridProxySides();
            if (getBaseMetaTileEntity().getWorld() != null) gridProxy.setOwner(
                getBaseMetaTileEntity().getWorld()
                    .getPlayerEntityByName(getBaseMetaTileEntity().getOwnerName()));
        }
    }
    return this.gridProxy;
}

@Override
public boolean isPowered() {
    return getProxy() != null && getProxy().isPowered();
}

@Override
public boolean isActive() {
    return getProxy() != null && getProxy().isActive();

}/*
@MENetworkEventSubscribe
public void storageChange(MENetworkStorageEvent w){
	updateCache();
	
} */
@Override
public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	if(aTick%200==123)updateCache();

	super.onPostTick(aBaseMetaTileEntity, aTick);
}

private void updateCache() {
	try{
		ArrayList<ItemStack> list=new ArrayList<>(getProxy().getStorage().getItemInventory().getStorageList().size());
		getProxy().getStorage().getItemInventory().getStorageList().forEach(s->{
			ItemStack is = ItemList.Tool_DataStick.get(1);
			if(is.getItem()==s.getItem()&&is.getItemDamage()==s.getItemDamage()){
			//do not call getItemDamage#getItemStack(), because it will create new NBT instance which is useless...
				ItemStack cp = s.getItemStack();
				cp.stackSize=1;
				list.add(cp);
				
			}
			
		});
		inv=list.toArray(new ItemStack[0]);
		
		
		
		
	}catch(Exception w){
		inv=new ItemStack[]{};
	}
	
}
@Override
public boolean useModularUI() {
	
	return false;
}


@Override
public void saveNBTData(NBTTagCompound aNBT) {
    super.saveNBTData(aNBT);
    NBTTagList nbtTagList = new NBTTagList();
    for (int i = 0; i < inv.length; i++) {
        if(inv[i]==null)continue;
        NBTTagCompound fluidTag = inv[i].writeToNBT(new NBTTagCompound());
        nbtTagList.appendTag(fluidTag);
    }

    aNBT.setTag("dataStickCache", nbtTagList);
    aNBT.setInteger("dataStickCacheLen", inv.length);
    getProxy().writeToNBT(aNBT);
}

@Override
public void loadNBTData(NBTTagCompound aNBT) {
    super.loadNBTData(aNBT);
    if (aNBT.hasKey("dataStickCache")) {
        NBTTagList nbtTagList = aNBT.getTagList("dataStickCache", 10);
        int c = Math.min(nbtTagList.tagCount(), aNBT.getInteger("dataStickCacheLen"));
        inv=new ItemStack[aNBT.getInteger("dataStickCacheLen")];
        for (int i = 0; i < c; i++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            ItemStack fluidStack = GT_Utility.loadItem(nbtTagCompound);
            inv[i] = fluidStack;
        }
    }

    getProxy().readFromNBT(aNBT);
}
@Override
public IGridNode getGridNode(ForgeDirection dir) {
	
	return this.getProxy().getNode();
}
@Override
public void securityBreak() {
	
	
} 
@SuppressWarnings("unchecked")
public List<ItemStack> getItemsForHoloGlasses(){
	
return Arrays.asList(inv);
	
	
}
@Override
public DimensionalCoord getLocation() {

	return new DimensionalCoord((TileEntity)this.getBaseMetaTileEntity());
}
@Override
public void storageChange(MENetworkStorageEvent w) {
	updateCache();
	
}
@Override
public boolean isValidSlot(int aIndex) {
	
	return false;
}
@Override
public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, ForgeDirection side, ForgeDirection aFacing,
    int colorIndex, boolean aActive, boolean redstoneLevel) {
   
	
	
	int texturePointer = getUpdateData(); // just to be sure, from my testing the 8th bit cannot be
	int  mTexturePage= getTexturePage();                                  // set clientside
    int textureIndex = texturePointer | (mTexturePage << 7); // Shift seven since one page is 128 textures!
    try {
        if (side != aFacing) {
            if (textureIndex > 0)
                return new ITexture[] { Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer] };
            else return new ITexture[] {Textures.BlockIcons.casingTexturePages[GT_Block_CasingsTT.texturePage][1] };
        } else {
            if (textureIndex > 0) {
                if (aActive)
                    return getTexturesActive(Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer]);
                else return getTexturesInactive(
                    Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer]);
            } else {
                if (aActive) return getTexturesActive(Textures.BlockIcons.casingTexturePages[GT_Block_CasingsTT.texturePage][1]);
                else return getTexturesInactive(Textures.BlockIcons.casingTexturePages[GT_Block_CasingsTT.texturePage][1]);
            }
        }
    } catch (NullPointerException npe) {
        return new ITexture[] { Textures.BlockIcons.MACHINE_CASINGS[0][0] };
    }
}
@Override
public ITexture[] getTexturesActive(ITexture aBaseTexture) {
    return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.OVERLAY_ME_INPUT_HATCH_ACTIVE) };
}

@Override
public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
    return new ITexture[] { aBaseTexture,  TextureFactory.of(BlockIcons.OVERLAY_ME_INPUT_HATCH) };
}
}