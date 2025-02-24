package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import org.joml.Math;

import com.google.common.collect.ImmutableMap;

import appeng.api.networking.crafting.ICraftingMedium;
import appeng.helpers.ICustomNameObject;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.IRecipeProcessingAwareDualHatch;
import reobf.proghatches.gt.metatileentity.util.polyfill.INeoDualInputInventory;
import reobf.proghatches.main.registration.Registration;

public class DualInputHatchSlaveHatch<T extends MetaTileEntity & IDualInputHatch & IMetaTileEntity>
    extends MTEHatchMultiInput implements IRecipeProcessingAwareHatch, IDataCopyablePlaceHolder {

    private T master; // use getMaster() to access
    private int masterX, masterY, masterZ;
    private boolean masterSet = false; // indicate if values of masterX,
    private boolean recipe;
    // masterY, masterZ are valid

    public DualInputHatchSlaveHatch(int aID, String aName, String aNameRegional) {
        super(aID, 1, aName, aNameRegional, 6

        );
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));

    }
    String[] desc;
@Override
public String[] getDescription() {
	
	if(desc==null){
		desc=reobf.proghatches.main.Config.get("DHSH", ImmutableMap.of());
		
	}
	
	return desc;
}
    public DualInputHatchSlaveHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, 1, 6, aDescription, aTextures);

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DualInputHatchSlaveHatch<>(mName, mTier, mDescriptionArray, mTextures);
    }

    /*
     * @Override
     * public ITexture[] getTexturesActive(ITexture aBaseTexture) {
     * return getTexturesInactive(aBaseTexture);
     * }
     * @Override
     * public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
     * return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_ME_CRAFTING_INPUT_SLAVE) };
     * }
     */

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
            trySetMasterFromCoord(masterX, masterY, masterZ);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);

        if (aNBT.hasKey("master")) {
            NBTTagCompound masterNBT = aNBT.getCompoundTag("master");
            masterX = masterNBT.getInteger("x");
            masterY = masterNBT.getInteger("y");
            masterZ = masterNBT.getInteger("z");
            masterSet = true;
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        if (masterSet) {
            NBTTagCompound masterNBT = new NBTTagCompound();
            masterNBT.setInteger("x", masterX);
            masterNBT.setInteger("y", masterY);
            masterNBT.setInteger("z", masterZ);
            aNBT.setTag("master", masterNBT);
        }
    }

    @Override
    public boolean isGivingInformation() {
        return true;
    }

    @Override
    public String[] getInfoData() {
        ArrayList<String> ret = new ArrayList<String>();
        if (getMaster() != null) {
            ret.add(
                "This bus is linked to the Crafting Input Buffer at " + masterX
                    + ", "
                    + masterY
                    + ", "
                    + masterZ
                    + ".");
            ret.addAll(Arrays.asList(getMaster().getInfoData()));
        } else ret.add("This bus is not linked to any Buffered Dual Inputhatch.");
        return ret.toArray(new String[0]);
    }

    public T getMaster() {
        if (master == null) return null;
        if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master
                                                                          // disappeared
            master = null;
        }
        return master;
    }

    @Override
    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    @Override
    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
        ItemStack aStack) {
        return false;
    }

    /*
     * @Override
     * public Iterator<? extends IDualInputInventory> inventories() {
     * return getMaster() != null ? getMaster().inventories() : Collections.emptyIterator();
     * }
     * @Override
     * public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
     * return getMaster() != null ? getMaster().getFirstNonEmptyInventory() : Optional.empty();
     * }
     */
    /*
     * @Override
     * public boolean supportsFluids() {
     * return getMaster() != null && getMaster().supportsFluids();
     * }
     * @Override
     * public boolean justUpdated() {
     * return getMaster() != null && getMaster().justUpdated();
     * }
     */
    @SuppressWarnings("unchecked")
    public IDualInputHatch trySetMasterFromCoord(int x, int y, int z) {
        TileEntity tileEntity = getBaseMetaTileEntity().getWorld()
            .getTileEntity(x, y, z);
        if (tileEntity == null) return null;
        if (!(tileEntity instanceof IGregTechTileEntity)) return null;
        IMetaTileEntity metaTileEntity = ((IGregTechTileEntity) tileEntity).getMetaTileEntity();
        if (!(metaTileEntity instanceof IDualInputHatch)) return null;

        if (!(metaTileEntity instanceof reobf.proghatches.gt.metatileentity.DualInputHatch)) return null;

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
        if (dataStick.hasTagCompound() && dataStick.stackTagCompound.getString("type")
            .equals("CraftingInputBuffer")) {
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.gt.slave.compat"));
            return false;
        }
        if (!dataStick.hasTagCompound() || !dataStick.stackTagCompound.getString("type")
            .equals("ProgHatchesDualInput")) {
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

    @Override
    public void getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
        IWailaConfigHandler config) {
        NBTTagCompound tag = accessor.getNBTData();
        currenttip.add((tag.getBoolean("linked") ? "Linked" : "Not linked"));

        if (tag.hasKey("masterX")) {
            currenttip.add(
                "Bound to " + tag
                    .getInteger("masterX") + ", " + tag.getInteger("masterY") + ", " + tag.getInteger("masterZ"));
        }

        if (tag.hasKey("masterName")) {
            currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName") + EnumChatFormatting.RESET);
        }

        super.getWailaBody(itemStack, currenttip, accessor, config);
    }

    public String getNameOf(T tg) {

        if (tg instanceof ICustomNameObject) {
            ICustomNameObject iv = (ICustomNameObject) tg;
            if (iv.hasCustomName()) return iv.getCustomName();

        }

        StringBuilder name = new StringBuilder();
        if (tg instanceof ICraftingMedium && ((ICraftingMedium) tg).getCrafterIcon() != null) {
            name.append(
                ((ICraftingMedium) tg).getCrafterIcon()
                    .getDisplayName());
        } else {
            name.append(tg.getLocalName());
        }

        return name.toString();
    }

    @Override
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
    }

    @Override
    public void startRecipeProcessing() {
        recipe = true;
        if (getMaster() != null) if (getMaster() instanceof IRecipeProcessingAwareDualHatch)
            ((IRecipeProcessingAwareDualHatch) getMaster()).startRecipeProcessing();

    }

    IDualInputInventory tmpinv;

    public IDualInputInventory getDual() {
        if (tmpinv != null) {
            return tmpinv;
        }
        if (getMaster() != null) {

            Optional<IDualInputInventory> opt = getMaster().getFirstNonEmptyInventory();
            if (opt.isPresent()) tmpinv = opt.get();
        }
        if (tmpinv == null) {
            tmpinv = new INeoDualInputInventory() {

                @Override
                public boolean isEmpty() {
                    // TODO Auto-generated method stub
                    return false;
                }

                @Override
                public ItemStack[] getItemInputs() {

                    return new ItemStack[0];
                }

                @Override
                public FluidStack[] getFluidInputs() {

                    return new FluidStack[0];
                }
            };

        }
        return tmpinv;
    }

    FluidStack[] tmpf;

    @Override
    public FluidStack[] getStoredFluid() {
        if (recipe) {
            if (tmpf == null) {
                tmpf = getDual().getFluidInputs();
            }
            return tmpf;

        }
        return new FluidStack[0];
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack aFluid, boolean doDrain) {
        if (recipe) {
            if (tmpf == null) {
                tmpf = getDual().getFluidInputs();
            }
            if (aFluid == null) return null;

            FluidStack ret = aFluid.copy();
            ret.amount = 0;
            int todo = aFluid.amount;
            for (FluidStack f : tmpf) {
                if (f != null && f.getFluid() == ret.getFluid()) {
                    int realtodo = Math.min(f.amount, todo);
                    f.amount -= realtodo;
                    ret.amount += realtodo;
                    todo -= realtodo;
                }
                if (todo == 0) {
                    break;
                }
            }
            return ret.amount == 0 ? null : ret;

        }
        return null;
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        recipe = false;
        tmpinv = null;
        tmpf = null;
        if (getMaster() != null) if (getMaster() instanceof IRecipeProcessingAwareDualHatch)
            return ((IRecipeProcessingAwareDualHatch) getMaster()).endRecipeProcessing(controller);
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public List<ItemStack> getItemsForHoloGlasses() {
        return getMaster() != null ? getMaster().getItemsForHoloGlasses() : null;
    }

    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
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
        if (nbt.hasKey("masterX")) masterX = nbt.getInteger("masterX");
        if (nbt.hasKey("masterY")) masterY = nbt.getInteger("masterY");
        if (nbt.hasKey("masterZ")) masterZ = nbt.getInteger("masterZ");
        if (nbt.hasKey("masterSet")) masterSet = nbt.getBoolean("masterSet");
        master = null;
        return true;
    }
}
