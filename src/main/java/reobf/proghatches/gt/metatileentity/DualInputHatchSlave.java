package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_ME_CRAFTING_INPUT_SLAVE;
import static reobf.proghatches.main.Config.defaultObj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.extensions.ArrayExt;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import reobf.proghatches.main.registration.Registration;

public class DualInputHatchSlave<T extends MetaTileEntity & IDualInputHatch> extends GT_MetaTileEntity_Hatch_InputBus
    implements IDualInputHatch {

    private T master; // use getMaster() to access
    private int masterX, masterY, masterZ;
    private boolean masterSet = false; // indicate if values of masterX, masterY, masterZ are valid

    public DualInputHatchSlave(int aID, String aName, String aNameRegional) {
        super(
            aID,
            aName,
            aNameRegional,
            6,
            0,
            reobf.proghatches.main.Config.get("DHS", ImmutableMap.of())
            /*defaultObj(

                ArrayExt.of(
                    "Slave for Dual Input Hatch",
                    "Link with Crafting Input Buffer using Data Stick to share inventory",
                    "Left click on the Dual Input Hatch, then right click on this block to link them"
                    ,StatCollector.translateToLocal("programmable_hatches.addedby")
                ),
                ArrayExt.of("二合一输入仓的镜像端", "将所绑定的样板输入总成的内容物共享过来", "闪存左键点击二合一输入仓，然后右键点击输入镜像完成链接绑定",
                		StatCollector.translateToLocal("programmable_hatches.addedby")))
            */
            

        /*
         * new String[] { "Slave for (Buffered) Dual Input Hatch",
         * "Link with Crafting Input Buffer using Data Stick to share inventory",
         * "Left click on the Crafting Input Buffer, then right click on this block to link them"
         * , }
         */

        );
        Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
        disableSort = true;
    }

    public DualInputHatchSlave(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);
        disableSort = true;
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new DualInputHatchSlave<>(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return getTexturesInactive(aBaseTexture);
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_ME_CRAFTING_INPUT_SLAVE) };
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (aTimer % 100 == 0 && masterSet && getMaster() == null) {
            trySetMasterFromCoord(masterX, masterY, masterZ);
        }
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
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
        if (((IMetaTileEntity) master).getBaseMetaTileEntity() == null) { // master disappeared
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

    @Override
    public Iterator<? extends IDualInputInventory> inventories() {
        return getMaster() != null ? getMaster().inventories() : Collections.emptyIterator();
    }

    @Override
    public Optional<IDualInputInventory> getFirstNonEmptyInventory() {
        return getMaster() != null ? getMaster().getFirstNonEmptyInventory() : Optional.empty();
    }

    @Override
    public boolean supportsFluids() {
        return getMaster() != null && getMaster().supportsFluids();
    }

    @Override
    public boolean justUpdated() {
        return getMaster() != null && getMaster().justUpdated();
    }

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

        /*
         * if (tag.hasKey("masterName")) {
         * currenttip.add(EnumChatFormatting.GOLD + tag.getString("masterName") + EnumChatFormatting.RESET);
         * }
         */

        super.getWailaBody(itemStack, currenttip, accessor, config);
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
        /* if (getMaster() != null) tag.setString("masterName", getMaster().getnam); */

        super.getWailaNBTData(player, tile, tag, world, x, y, z);
    }
}
