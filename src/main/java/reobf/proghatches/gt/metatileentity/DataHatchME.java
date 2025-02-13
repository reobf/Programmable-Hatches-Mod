package reobf.proghatches.gt.metatileentity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableMap;

import appeng.api.config.FuzzyMode;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.util.item.AEItemStack;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Textures;
import gregtech.api.enums.Textures.BlockIcons;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchDataAccess;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.AssemblyLineUtils;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GTRecipe.RecipeAssemblyLine;
import reobf.proghatches.gt.metatileentity.util.IMEStorageChangeAwareness;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.casing.BlockGTCasingsTT;

public class DataHatchME extends MTEHatchDataAccess
    implements IPowerChannelState, IGridProxyable, IMEStorageChangeAwareness {

    public DataHatchME(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional, 8);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
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
    String[] descCache;
    @Override
    public String[] getDescription() {

        return descCache==null?(descCache=reobf.proghatches.main.Config.get("DHME", ImmutableMap.of())):descCache;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DataHatchME(mName, mDescriptionArray, mTextures);
    }

    ItemStack[] inv = new ItemStack[0];
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
    public void onFacingChange() {
        updateValidGridProxySides();
    }

    @Override
    public AENetworkProxy getProxy() {
        if (gridProxy == null) {
            if (getBaseMetaTileEntity() instanceof IGridProxyable) {
                gridProxy = new AENetworkProxy(
                    (IGridProxyable) getBaseMetaTileEntity(),
                    "proxy",
                    new ItemStack(GregTechAPI.sBlockMachines, 1, getBaseMetaTileEntity().getMetaTileID()),
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

    }

    private boolean prevOk;

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if ((!aBaseMetaTileEntity.getWorld().isRemote) && aTick % 40 == 20) {
            boolean ok = (getProxy().isPowered()) && (getProxy().isActive());
            // System.out.println("power check");
            if (aTick % 400 == 20 || ok != prevOk) {
                // System.out.println("force update");
                updateCache();
                updateValidGridProxySides();
            }
            prevOk = ok;
        }

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    private void updateCache() {
    		cachedRecipes=null;
        if ((!getProxy().isPowered()) || (!getProxy().isActive())) {
            inv = new ItemStack[] {};
            return;
        }
        try {
            ArrayList<ItemStack> list = new ArrayList<>(
                getProxy().getStorage()
                    .getItemInventory()
                    .getStorageList()
                    .size());
            getProxy().getStorage()
                .getItemInventory()
                .getStorageList()
                .findFuzzy(AEItemStack.create(ItemList.Tool_DataStick.get(1)), FuzzyMode.IGNORE_ALL)

                /*
                 * ;
                 * getProxy().getStorage().getItemInventory().getStorageList()
                 */
                .forEach(s -> {
                    ItemStack is = ItemList.Tool_DataStick.get(1);
                    if (/* is.getItem()==s.getItem()&& */is.getItemDamage() == s.getItemDamage()) {
                        ItemStack cp = s.getItemStack();
                        cp.stackSize = 1;
                        list.add(cp);

                    }

                });
            inv = list.toArray(new ItemStack[0]);

        } catch (Exception w) {
            inv = new ItemStack[] {};
        }

    }

    // @Override
    public boolean useModularUI() {

        return false;
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0; i < inv.length; i++) {
            if (inv[i] == null) continue;
            NBTTagCompound fluidTag = inv[i].writeToNBT(new NBTTagCompound());
            nbtTagList.appendTag(fluidTag);
        }

        aNBT.setTag("dataStickCache", nbtTagList);
        aNBT.setInteger("dataStickCacheLen", inv.length);
        getProxy().writeToNBT(aNBT);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        if (aNBT.hasKey("dataStickCache")) {
            NBTTagList nbtTagList = aNBT.getTagList("dataStickCache", 10);
            int c = Math.min(nbtTagList.tagCount(), aNBT.getInteger("dataStickCacheLen"));
            inv = new ItemStack[aNBT.getInteger("dataStickCacheLen")];
            for (int i = 0; i < c; i++) {
                NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
                ItemStack fluidStack = GTUtility.loadItem(nbtTagCompound);
                inv[i] = fluidStack;
            }
        }

        getProxy().readFromNBT(aNBT);

    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {

        return this.getProxy()
            .getNode();
    }

    @Override
    public void securityBreak() {

    }

    @SuppressWarnings("unchecked")
    public List<ItemStack> getItemsForHoloGlasses() {

        return Arrays.asList(inv);

    }

    @Override
    public DimensionalCoord getLocation() {

        return new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity());
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
        int mTexturePage = getTexturePage(); // set clientside
        int textureIndex = texturePointer | (mTexturePage << 7); // Shift seven since one page is 128 textures!
        try {
            if (side != aFacing) {
                if (textureIndex > 0)
                    return new ITexture[] { Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer] };
                else return new ITexture[] { Textures.BlockIcons.casingTexturePages[BlockGTCasingsTT.texturePage][1] };
            } else {
                if (textureIndex > 0) {
                    if (aActive)
                        return getTexturesActive(Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer]);
                    else return getTexturesInactive(
                        Textures.BlockIcons.casingTexturePages[mTexturePage][texturePointer]);
                } else {
                    if (aActive) return getTexturesActive(
                        Textures.BlockIcons.casingTexturePages[BlockGTCasingsTT.texturePage][1]);
                    else return getTexturesInactive(
                        Textures.BlockIcons.casingTexturePages[BlockGTCasingsTT.texturePage][1]);
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
        return new ITexture[] { aBaseTexture, TextureFactory.of(BlockIcons.OVERLAY_ME_INPUT_HATCH) };
    }

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

        return false;// no gui
    }

    private long lastupdate = -99999;

    //@Override
    public List<ItemStack> getInventoryItems(Predicate<ItemStack> filter) {
        long thistick = ((BaseMetaTileEntity) this.getBaseMetaTileEntity()).mTickTimer;
        if (Math.abs(lastupdate - thistick) > 100) {
            updateCache();
            lastupdate = thistick;
        }
        return super_getInventoryItems(filter);
    }
    public List super_getInventoryItems(Predicate filter) {
        ArrayList items = new ArrayList();
        IGregTechTileEntity te = this.getBaseMetaTileEntity();

        for(int i = 0; i < te.getSizeInventory(); ++i) {
           ItemStack slot = te.getStackInSlot(i);
           if (slot != null && filter != null && filter.test(slot)) {
              items.add(slot);
           }
        }

        return items;
     }
    
    
    List<RecipeAssemblyLine> cachedRecipes;
    public List<RecipeAssemblyLine> getAssemblyLineRecipes() {
    	 long thistick = ((BaseMetaTileEntity) this.getBaseMetaTileEntity()).mTickTimer;
    	 if (Math.abs(lastupdate - thistick) > 100) {
             updateCache();
             lastupdate = thistick;
         }
    	 if (cachedRecipes == null) {
             cachedRecipes = new ArrayList<>();
             Method f=null;
            try {
			f=AssemblyLineUtils.class.getDeclaredMethod("findALRecipeFromDataStick", ItemStack.class);
			} catch (Exception e) {}
            try {
    			f=AssemblyLineUtils.class.getDeclaredMethod("findAssemblyLineRecipeFromDataStick", ItemStack.class);
    			} catch (Exception e) {}
			
             if(f==null)throw new AssertionError();
             for (int i = 0; i < getSizeInventory(); i++) {
                 try {
					cachedRecipes.addAll(
							 
							 (Collection<? extends RecipeAssemblyLine>) f.invoke(null, getStackInSlot(i))
						
							 
							 
							 );
				} catch (Exception e) {
					e.printStackTrace();
				}
             }
         }

        
		return cachedRecipes;

 
    }
}
