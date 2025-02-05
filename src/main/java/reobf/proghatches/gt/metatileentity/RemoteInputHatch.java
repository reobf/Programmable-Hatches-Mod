package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.FLUID_IN_SIGN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Interactable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.api.widget.Widget.ClickData;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SyncedWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import appeng.api.util.DimensionalCoord;
import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMultiInput;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GTUtility;
import gregtech.api.util.shutdown.ShutDownReasonRegistry;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.gt.metatileentity.util.IDataCopyablePlaceHolder;
import reobf.proghatches.gt.metatileentity.util.RecursiveLinkExcpetion;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class RemoteInputHatch extends MTEHatchMultiInput
    implements IRecipeProcessingAwareHatch, IDataCopyablePlaceHolder {

    static public ArrayList<String> blacklist = new ArrayList<>();
    static {

        blacklist.add(RemoteInputHatch.class.getTypeName());
        blacklist.add("thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface");
        blacklist.add("remoteio.common.tile.TileRemoteInterface");
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(FLUID_IN_SIGN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
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
            s = s.replaceAll("§b|§r|§m", "");
            s = s.replaceAll("(-){2,}", "");
            s = s.replace(" ", "");
            s = s.replace("X", "");
            s = s.replace("Y", "");
            s = s.replace("Z", "");
            s = s.replace("D", "");

            String[] splits = s.split(":");
            int x = Integer.valueOf(splits[1].replace(",", ""));
            int y = Integer.valueOf(splits[2].replace(",", ""));
            int z = Integer.valueOf(splits[3].replace(",", ""));
            int d = Integer.valueOf(splits[4].replace(",", ""));
            World w = this.getBaseMetaTileEntity()
                .getWorld();
            if (d == w.provider.dimensionId) {

                this.x = x;
                this.y = y;
                this.z = z;
                if (this.getBaseMetaTileEntity()
                    .getWorld()
                    .getChunkProvider()
                    .chunkExists(x >> 4, z >> 4) == false) {
                    aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.deferred"));
                    this.linked = true;
                    return;
                }
                if (checkBlackList()
                // blacklist.contains(this.getBaseMetaTileEntity().getWorld().getBlock(x,
                // y, z).getUnlocalizedName())
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
    private boolean checkBlackList(Optional<TileEntity> opt) {// World
                                                              // ww=this.getBaseMetaTileEntity().getWorld();
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
        super(id, 0, name, nameRegional, tier, reobf.proghatches.main.Config.get("RIH", ImmutableMap.of())

        );
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));

    }

    @SuppressWarnings("unchecked")
    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
        // super.addUIWidgets(builder, buildContext);

        // buildContext.addCloseListener(() -> uiButtonCount = 0);

        builder.widget(TextWidget.dynamicString(() -> {

            if (!linked) {
                return LangManager.translateToLocal("programmable_hatches.remote.unlinked");
            }

            Optional<TileEntity> opt = getTile();
            if (this.getBaseMetaTileEntity()
                .getWorld()
                .getChunkProvider()
                .chunkExists(x >> 4, z >> 4) == false)
                return LangManager.translateToLocal("programmable_hatches.remote.chunk");

            if (opt.isPresent() == false) return LangManager.translateToLocal("programmable_hatches.remote.nothing");
            else checkBlackList();
            if (opt.get() instanceof IFluidHandler == false) {
                return LangManager.translateToLocal("programmable_hatches.remote.dummytarget");

            }

            return LangManager.translateToLocal("programmable_hatches.remote.ok");

        }

        )
            .setSynced(true)
            .setPos(5, 5))

        ;
        List<FluidTank> is;

        builder.widget(
            SlotGroup.ofFluidTanks(
                (List) (is = Stream.generate(() -> new FluidTank(Integer.MAX_VALUE))
                    .limit(16)
                    .collect(Collectors.toList())),
                8)
                .widgetCreator((s, b) -> {
                    FluidSlotWidget sw = new FluidSlotWidget(b) {

                        @Override
                        public void buildTooltip(List<Text> tooltip) {
                            // super.buildTooltip(tooltip);
                            FluidStack fluid = getContent();
                            if (fluid != null) {
                                addFluidNameInfo(tooltip, fluid);
                                tooltip.add(Text.localised("modularui.fluid.phantom.amount", fluid.amount));
                                addAdditionalFluidInfo(tooltip, fluid);
                                if (!Interactable.hasShiftDown()) {
                                    tooltip.add(Text.EMPTY);
                                    tooltip.add(Text.localised("modularui.tooltip.shift"));
                                }
                            } else {
                                tooltip.add(
                                    Text.localised("modularui.fluid.empty")
                                        .format(EnumChatFormatting.WHITE));
                            }
                        }

                        @Override
                        protected void tryClickPhantom(ClickData clickData, ItemStack cursorStack) {}

                        @Override
                        protected void tryScrollPhantom(int direction) {}
                    };

                    return sw;
                })
                .phantom(true)
                .startFromSlot(0)
                .endAtSlot(16)
                .build()
                .setPos(3, 3 + 16));

        builder.widget(new SyncedWidget() {

            int count;

            @Override
            public void detectAndSendChanges(boolean init) {
                if (count-- <= 0) {
                    count = 100;
                } else return;

                Optional<TileEntity> opt = getTile();
                if (opt.isPresent()) {
                    List<FluidStack> list = opt.map(e -> {
                        try {
                            processingRecipe = true;
                            return filterTakable(e);
                        } finally {
                            processingRecipe = false;
                            tmp = null;
                        }

                    })
                        .get();
                    for (int i = 0; i < is.size(); i++) {
                        is.get(i)
                            .setFluid(list.size() > i ? list.get(i) : null);
                    }

                } else {
                    for (int i = 0; i < is.size(); i++) {
                        is.get(i)
                            .setFluid(null);

                    }

                }
            }

            public void readOnClient(int id, PacketBuffer buf) throws IOException {}

            public void readOnServer(int id, PacketBuffer buf) throws IOException {}
        });

        Widget w;
        builder.widget(
            w = new DrawableWidget().setDrawable(ModularUITextures.ICON_INFO)

                .setPos(3 + 18 * 8 + 1, 3 + 18 * 2 + 1)
                .setSize(16, 16)
        // .addTooltip("xxxxxxx")
        );

        IntStream
            .range(0, Integer.valueOf(StatCollector.translateToLocal("programmable_hatches.gt.remotehatch.tooltip")))
            .forEach(
                s -> w.addTooltip(LangManager.translateToLocal("programmable_hatches.gt.remotehatch.tooltip." + +s)));

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
        try {
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
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public FluidStack getFluid(int aSlot) {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return null;
             * }
             */

            return getTile().map(this::filterTakable)
                .map(s -> {
                    if (aSlot < 0 || aSlot >= s.size()) return null;
                    return s.get(aSlot);
                })
                .orElse(null);
        } catch (RecursiveLinkExcpetion e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public FluidStack getFillableStack() {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return null;
             * }
             */

            return getTile().map(this::filterTakable)
                .filter(s -> s.size() >= 1)
                .map(s -> s.get(0))
                .orElse(null);
        } catch (RecursiveLinkExcpetion e) {
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    public List<FluidStack> filterTakable(TileEntity e) {

        if (processingRecipe == false) return new ArrayList<FluidStack>();

        try {
            if (tmp != null) endRecipeProcessing(null);
            // this means this method is called twice during recipe check?
            // remove consumed fluid
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        processingRecipe = true;

        if (e == null || (e instanceof IFluidHandler == false)) return new ArrayList<FluidStack>();
        IFluidHandler inv = (IFluidHandler) e;

        ArrayList<FluidStack> arr = new ArrayList<FluidStack>();
        // boolean b=e instanceof IFluidHandler;

        if (e instanceof IFluidHandler) {

            IFluidHandler side = (IFluidHandler) e;
            HashMultiset<ShadowFluidStack> slots = HashMultiset.create();
            // for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
            FluidTankInfo[] info = side.getTankInfo(
                this.getBaseMetaTileEntity()
                    .getFrontFacing());
            if (info == null) return new ArrayList<>(0);
            for (FluidTankInfo i : info) {

                if (i.fluid != null && side.canDrain(
                    this.getBaseMetaTileEntity()
                        .getFrontFacing(),
                    i.fluid.getFluid()))

                    slots.add(new ShadowFluidStack(i.fluid));
            } ;
            // }
            ArrayList<ShadowFluidStack> arrm = new ArrayList<>(slots);
            tmp = arrm;
            return (List<FluidStack>) (Object) arrm;
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
        if (aNBT.hasKey("x") == false) return;
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
        if (2 > 1) return false;

        if (count++ < 40) return false;
        count = 0;
        return checkDepth();
    }

    private static HashSet<Object> record = new HashSet<>();

    public AutoCloseable mark() {
        if (!record.add(this)) {
            getBaseMetaTileEntity().getWorld()
                .setBlockToAir(this.x, this.y, this.z);
            throw new RecursiveLinkExcpetion();
        } ;

        return () -> { record.remove(this); };
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
    public FluidStack[] getStoredFluid() {
        if (blocked) {
            return new FluidStack[0];
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            /*
             * if (checkDepthLoose()) {
             * getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
             * return new FluidStack[0];
             * }
             */

            return getTile().map(this::filterTakable)
                .orElse(new ArrayList<>())
                .toArray(new FluidStack[0]);

        } catch (RecursiveLinkExcpetion e) {
            return new FluidStack[0];
        } catch (Exception e) {
            e.printStackTrace();
            return new FluidStack[0];
        }

    }

    protected boolean processingRecipe = false;
    static HashSet<DimensionalCoord> using = new HashSet<>();
    boolean blocked;

    @Override
    public void startRecipeProcessing() {

        processingRecipe = true;
        if (false == using.add(new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity()))) {
            blocked = true;

        } ;
    }

    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        if (!blocked) {
            using.remove(new DimensionalCoord((TileEntity) this.getBaseMetaTileEntity()));
        }
        processingRecipe = false;
        blocked = false;
        if (tmp == null) return CheckRecipeResultRegistry.SUCCESSFUL;
        TileEntity tile = getTile().orElse(null);
        if (tile != null) tile.markDirty();
        boolean missing = (tile == null);
        IFluidHandler fh = null;
        if (tile instanceof IFluidHandler) fh = (IFluidHandler) tile;
        else missing = true;
        AtomicBoolean fail = new AtomicBoolean(false);
        // even if missing, tmp might be consumed? just check it
        final boolean fmissing = missing;
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

        if (fail.get()) {
            controller.stopMachine(ShutDownReasonRegistry.CRITICAL_NONE);
            return CheckRecipeResultRegistry.CRASH;
        }

        tmp = null;
        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

    @Override
    public FluidStack getFluid() {
        FluidStack FS = getFillableStack();

        return FS;
    }

    @Override
    public FluidStack getDrainableStack() {

        return getFillableStack();
    }

    @Override
    public FluidStack drain(ForgeDirection side, FluidStack aFluid, boolean doDrain) {
        if (blocked) {
            return null;
        }
        try (AutoCloseable o = mark()) {
            Optional<TileEntity> opt = getTile();
            if (opt.isPresent() && checkBlackList(opt)) {
                this.linked = false;
            }
            List<FluidStack> all = filterTakable(getTile().orElse(null));
            // this is an ME input hatch. allowing draining via logistics would be very wrong (and against
            // canTankBeEmptied()) but we do need to support draining from controller, which uses the UNKNOWN direction.
            if (side != ForgeDirection.UNKNOWN) return null;
            // FluidStack stored = getMatchingFluidStack(aFluid);
            FluidStack stored = all.stream()
                .filter(s -> s.getFluid() == aFluid.getFluid())
                .findAny()
                .orElse(null);

            if (stored == null) return null;
            FluidStack drained = GTUtility.copyAmount(Math.min(stored.amount, aFluid.amount), stored);
            if (doDrain) {
                stored.amount -= drained.amount;
            }
            return drained;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * private FluidStack getMatchingFluidStack(FluidStack aFluid) {
     * if(tmp==null)return null;
     * return tmp.stream().filter(s->s.getFluid()==aFluid.getFluid())
     * .findAny().orElse(null);
     * }
     */
    @Override
    public NBTTagCompound getCopiedData(EntityPlayer player) {
        NBTTagCompound ret = new NBTTagCompound();
        writeType(ret, player);
        ret.setInteger("x", x);
        ret.setInteger("y", y);
        ret.setInteger("z", z);
        ret.setBoolean("linked", linked);
        return ret;
    }

    @Override
    public boolean pasteCopiedData(EntityPlayer player, NBTTagCompound nbt) {
        if (nbt == null || !getCopiedDataIdentifier(player).equals(nbt.getString("type"))) return false;
        if (nbt.hasKey("x")) x = nbt.getInteger("x");
        if (nbt.hasKey("y")) y = nbt.getInteger("y");
        if (nbt.hasKey("z")) z = nbt.getInteger("z");
        if (nbt.hasKey("linked")) linked = nbt.getBoolean("linked");
        return true;
    }
}
