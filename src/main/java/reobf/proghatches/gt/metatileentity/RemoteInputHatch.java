package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.FLUID_IN_SIGN;
import static reobf.proghatches.main.Config.defaultObj;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_MultiInput;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.extensions.ArrayExt;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class RemoteInputHatch extends GT_MetaTileEntity_Hatch_MultiInput implements IRecipeProcessingAwareHatch {

    static public ArrayList<String> blacklist = new ArrayList<>();
    static {

        blacklist.add(RemoteInputHatch.class.getTypeName());
        blacklist.add("thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface");
        blacklist.add("remoteio.common.tile.TileRemoteInterface");
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) };
    }

    public RemoteInputHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, 0, aTier, aDescription, aTextures);

    }

    @Override
    public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
        if (aBaseMetaTileEntity.getWorld().isRemote) return;
        this.markDirty();
        if (aPlayer.isSneaking() && aPlayer.getHeldItem() == null) {
            linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.detach"));

            return;
        }
        try {
            String s = aPlayer.getHeldItem()
                .getTagCompound()
                .getString("dataLines0");
            String[] splits = s.split("§b|§r");
            int x = Integer.valueOf(splits[1]);
            int y = Integer.valueOf(splits[3]);
            int z = Integer.valueOf(splits[5]);
            int d = Integer.valueOf(splits[7]);
            World w = this.getBaseMetaTileEntity()
                .getWorld();
            if (d == w.provider.dimensionId) {

                this.x = x;
                this.y = y;
                this.z = z;

                if (checkBlackList()
                // blacklist.contains(this.getBaseMetaTileEntity().getWorld().getBlock(x, y, z).getUnlocalizedName())
                ) {

                    aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.blacklisted"));
                    this.linked = false;
                    return;
                }
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.success"));
                this.linked = true;

                return;

            } else {
                this.linked = false;
                aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.dim"));
                aPlayer.addChatComponentMessage(null);
            } ;

        } catch (Exception w) {
            this.linked = false;
            aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.fail"));

        }
        super.onLeftclick(aBaseMetaTileEntity, aPlayer);
    }

    @SuppressWarnings("unused")
    private boolean checkBlackList() {
        World ww = this.getBaseMetaTileEntity()
            .getWorld();
        return Optional.ofNullable(ww.getTileEntity(x, y, z))
            .map(TileEntity::getClass)
            .map(Class::toString)
            .map(blacklist::contains)
            .orElse(false) ||
        // Optional.ofNullable(ww.getBlock(x, y,
        // z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
            Optional.ofNullable(ww.getTileEntity(x, y, z))
                .filter(sp -> sp instanceof IGregTechTileEntity)
                .map(
                    sp -> ((IGregTechTileEntity) sp).getMetaTileEntity()
                        .getClass()
                        .getTypeName())
                .map(blacklist::contains)
                .orElse(false);

    }

    @SuppressWarnings("unused")
    private boolean checkBlackList(Optional<TileEntity> opt) {// World ww=this.getBaseMetaTileEntity().getWorld();
        return opt.map(TileEntity::getClass)
            .map(Class::toString)
            .map(blacklist::contains)
            .orElse(false) ||
        // Optional.ofNullable(ww.getBlock(x, y,
        // z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
            opt.filter(sp -> sp instanceof IGregTechTileEntity)
                .map(
                    sp -> ((IGregTechTileEntity) sp).getMetaTileEntity()
                        .getClass()
                        .getTypeName())
                .map(blacklist::contains)
                .orElse(false);

    }

    public RemoteInputHatch(int id, String name, String nameRegional, int tier) {
        super(
            id,
            0,
            name,
            nameRegional,
            0,
            defaultObj(

                ArrayExt.of(
                    "Fluid Input for Multiblocks, wirelessly linked to a tank, just like RemoteIO interface.",
                    "LMB click this block with a tricorder with target coord to link.",
                    "Fluid not extractable by pipes will not be accessible.",
                    "Cannot work across dimension. Will not load target chunk. Will not work if target chunk is unloaded."
,StatCollector.translateToLocal("programmable_hatches.addedby")
                ),
                ArrayExt.of(
                    "像RemoteIO一样远程访问某个容器中的流体 作为多方块机器的输入",
                    "三录仪记录目标坐标后,左键此方块设定坐标",
                    "无法被管道抽出的流体也不能被访问",
                    "不能跨维度链接 不会触发目标区块加载,且目标区块未加载时不工作"
,StatCollector.translateToLocal("programmable_hatches.addedby")
                )));
        Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));

    }

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        // super.addUIWidgets(builder, buildContext);

        // buildContext.addCloseListener(() -> uiButtonCount = 0);

        builder.widget(TextWidget.dynamicString(() -> {

            if (!linked) {
                return StatCollector.translateToLocal("programmable_hatches.remote.unlinked");
            }

            Optional<TileEntity> opt = getTile();
            if (this.getBaseMetaTileEntity()
                .getWorld()
                .getChunkProvider()
                .chunkExists(x >> 4, z >> 4) == false)
                return StatCollector.translateToLocal("programmable_hatches.remote.chunk");

            if (opt.isPresent() == false) return StatCollector.translateToLocal("programmable_hatches.remote.nothing");
            if (opt.get() instanceof IFluidHandler == false) {
                return StatCollector.translateToLocal("programmable_hatches.remote.dummytarget");

            }

            return StatCollector.translateToLocal("programmable_hatches.remote.ok");

        }

        )
            .setSynced(true)
            .setPos(5, 5))

        ;

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new RemoteInputHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    @Override
    public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

        return false;
    }

    public Optional<TileEntity> getTile() {
        if (this.getBaseMetaTileEntity()
            .getWorld()
            .getChunkProvider()
            .chunkExists(x >> 4, z >> 4) == false) {
            return Optional.empty();
        }
        return Optional.ofNullable(
            this.getBaseMetaTileEntity()
                .getWorld()
                .getTileEntity(x, y, z));
    }

    public List<? extends FluidStack> filterTakable(TileEntity e) {

        if (processingRecipe == false) return new ArrayList<FluidStack>();
        if (e == null || (e instanceof IFluidHandler == false)) return new ArrayList<FluidStack>();
        IFluidHandler inv = (IFluidHandler) e;

        ArrayList<FluidStack> arr = new ArrayList<FluidStack>();
        // boolean b=e instanceof IFluidHandler;

        if (e instanceof IFluidHandler) {

            IFluidHandler side = (IFluidHandler) e;
            HashSet<ShadowFluidStack> slots = new HashSet<ShadowFluidStack>();
            // for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            for (FluidTankInfo i : side.getTankInfo(
                this.getBaseMetaTileEntity()
                    .getFrontFacing())) {

                if (i.fluid != null && side.canDrain(
                    this.getBaseMetaTileEntity()
                        .getFrontFacing(),
                    i.fluid.getFluid()))

                    slots.add(new ShadowFluidStack(i.fluid));
            } ;
            // }
            ArrayList<ShadowFluidStack> arrm = new ArrayList<>(slots);
            tmp = arrm;
            return arrm;
        }

        return new ArrayList<>();
    }

    private volatile ArrayList<ShadowFluidStack> tmp = null;

    public class ShadowFluidStack extends FluidStack {

        FluidStack original;

        public ShadowFluidStack(FluidStack stack) {
            super(stack.copy()/* might be a clone itself */, stack.amount);
            original = stack.copy();
        }
    }

    int x, y, z;
    boolean linked;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        super.saveNBTData(aNBT);

        aNBT.setIntArray("coord", new int[] { x, y, z });
        aNBT.setBoolean("linked", linked);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {

        super.loadNBTData(aNBT);
        int[] i = aNBT.getIntArray("coord");
        x = i[0];
        y = i[1];
        z = i[2];

        linked = aNBT.getBoolean("linked");
    }

    @Override
    public boolean shouldDropItemAt(int index) {

        return false;
    }

    @Override
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {}// no we don't

    Throwable t = new Throwable();

    public int getCapacityPerTank(int aTier, int aSlot) {
        return 0;
    }

    public boolean checkDepth() {
        t.fillInStackTrace();

        boolean b = t.getStackTrace().length > 80;
        if (b) {

            MyMod.LOG.fatal("Warning! Potential infinite recursion!");
            MyMod.LOG.fatal("To prevent stack overflow, the block will be removed.");
            MyMod.LOG.fatal(x + "," + y + "," + z + "@dim:" + getBaseMetaTileEntity().getWorld().provider.dimensionId);
            t.printStackTrace();

        }
        return b;
    }

    private int count;

    public boolean checkDepthLoose() {
        if (count++ < 40) return false;
        count = 0;
        return checkDepth();
    }

    @Override
    public int getSizeInventory() {

        return 0;

    }

    @Override
    public void setInventorySlotContents(int aIndex, ItemStack aStack) {

    }

    @Override
    @Nullable
    public ItemStack getStackInSlot(int aIndex) {
        return null;

    }

    public void updateSlots() {}// no we don't

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        /*
         * x=aBaseMetaTileEntity.getXCoord();
         * y=aBaseMetaTileEntity.getYCoord()+1;
         * z=aBaseMetaTileEntity.getZCoord();
         * linked=true;
         */

        // if(justQueried){justQueried=false;removePhantom();}

        super.onPostTick(aBaseMetaTileEntity, aTimer);
    }

    @Override
    public FluidStack[] getStoredFluid() {

        Optional<TileEntity> opt = getTile();
        if (opt.isPresent() && checkBlackList(opt)) {
            this.linked = false;
        }
        if (checkDepthLoose()) {
            getBaseMetaTileEntity().getWorld()
                .setBlockToAir(this.x, this.y, this.z);
            return new FluidStack[0];
        }

        return getTile().map(this::filterTakable)
            .orElse(new ArrayList<>())
            .toArray(new FluidStack[0]);

    }

    protected boolean processingRecipe = false;

    @Override
    public void startRecipeProcessing() {

        processingRecipe = true;
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
        processingRecipe = false;
        if (tmp == null) return CheckRecipeResultRegistry.SUCCESSFUL;
        TileEntity tile = getTile().orElse(null);
        boolean missing = (tile == null);
        IFluidHandler fh = null;
        if (tile instanceof IFluidHandler) fh = (IFluidHandler) tile;
        else missing = true;
        AtomicBoolean fail = new AtomicBoolean(false);
         // even if missing, tmp might be consumed? just check it
        final boolean  fmissing = missing;
        final IFluidHandler ffh = fh;

        tmp.forEach(s -> {
            int consume = -s.amount + s.original.amount;
            Fluid fluid = s.original.getFluid();
            if (consume > 0 && fmissing) {
                fail.set(true);
                return;
            }
            if (consume == 0) return;
            if (consume != ffh.drain(
                this.getBaseMetaTileEntity()
                    .getFrontFacing(),
                new FluidStack(fluid, consume),
                true).amount) {
                fail.set(true);
                // return CheckRecipeResultRegistry.CRASH;
            }

        });

        if (fail.get()) return CheckRecipeResultRegistry.CRASH;

        tmp = null;
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

}
