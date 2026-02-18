package reobf.proghatches.gt.metatileentity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Unique;

import com.glodblock.github.common.item.FCBaseItemCell;
import com.glodblock.github.common.item.ItemFluidDrop;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.ICellContainer;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.cache.CraftingGridCache;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.me.storage.MEInventoryHandler;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEFluidStackType;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.IOutputBusTransaction;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.outputme.MTEHatchOutputME;

import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.gt.metatileentity.util.IStoageCellUpdate;
import reobf.proghatches.main.registration.Registration;
import tectech.util.TTUtility;

public class StorageOutputHatch extends MTEHatchOutputME
    {

    public StorageOutputHatch(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
        TTUtility.setTier(5, this);
    }

    @Override
    public String[] getDescription() {

        return mydesc;
    }

    String[] mydesc = reobf.proghatches.main.Config.get("SOH", ImmutableMap.of());

    public StorageOutputHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, aDescription, aTextures);

    }
    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

    	return   new StorageOutputHatch(mName, mTier, mDescriptionArray, mTextures);
    }

  
    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        NBTBase t = aNBT.getTag("cachedFluidsPH");
      
        if(t!=null) {
       	 aNBT.setTag("cachedFluids", t);
       }
      
        super.loadNBTData(aNBT);
    }

  
}
