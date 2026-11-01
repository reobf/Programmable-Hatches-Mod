package reobf.proghatches.gametests;

import static appeng.util.item.AEFluidStackType.FLUID_STACK_TYPE;
import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;
import static reobf.proghatches.gametests.ChancePatternTests.place;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.gtnewhorizon.structurelib.alignment.constructable.IConstructable;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import com.gtnewhorizons.horizonqa.api.gt.Multiblock;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartHost;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.AEColor;
import appeng.me.GridAccessException;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEFluidStack;
import bartworks.system.material.WerkstoffLoader;
import gregtech.api.GregTechAPI;
import gregtech.api.enums.ItemList;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import gregtech.api.util.GTUtility;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch.DualInvBuffer;
import reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import reobf.proghatches.gt.metatileentity.chance.ChanceData;
import reobf.proghatches.gt.metatileentity.chance.ChanceMark;
import reobf.proghatches.gt.metatileentity.cycle.CycleAnalysis;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.CustomItemList;

/**
 * Palladium enriched ammonia enters from upstream platinum processing. Metallic powder circulates through
 * precipitation and ammonia recycling, with zero allowed surplus. The sifter's chance pattern promises fifteen
 * powder per sixteen salt; nine go to precipitation and six return to enriched ammonia.
 */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class PalladiumLineTests {

    static final TestPos REACTOR = at(2, 1, 6);
    static final TestPos CYCLIC = at(1, 1, 7);
    static final TestPos CIRCUITS = at(0, 2, 1);
    private static final TestPos REACTOR_OUTPUT = at(1, 1, 6);
    private static final TestPos REACTOR_FLUID_OUTPUT = at(1, 1, 8);
    private static final TestPos REACTOR_ENERGY = at(3, 1, 6);
    private static final TestPos REACTOR_MAINTENANCE = at(2, 0, 6);
    private static final TestPos COIL = at(2, 0, 7);
    private static final TestPos[] PLAIN_CASINGS = { at(2, 2, 7), at(3, 1, 7), at(2, 1, 8) };

    private static final int ROUNDS = 5;
    private static final long POWDER_SEEDS = 60, POWDER_SPARE = 32, SALT_SEEDS = 64, SALT_SPARE = 128,
        ENRICHED_SEEDS = 24_000, ENRICHED_SPARE = 30_000, AMMONIA = 60_000;
    private static final BaseActionSource SOURCE = new BaseActionSource();

    // Each round consumes 3000 L upstream enriched ammonia and 6000 L ammonia for two reprecipitated palladium.
    @GameTest(template = "palladium", timeoutTicks = 8000)
    public static void palladiumCycleFillsAnOrder(GameTestHelper helper) {
        Line line = new Line(helper);
        AtomicReference<ICraftingLink> link = new AtomicReference<>();

        helper.startSequence()
            .thenWaitUntil("wait for the ME network and the CPU", 120, line::assertReady)
            .thenExecute("install the cycle: the reactor patterns on the hatch, the sifter pattern on a lane", line::installCycle)
            .thenWaitUntil(
                "wait for the balancing to be offered",
                600,
                () -> helper
                    .assertTrue(line.offered(line.raw), "The cycle should offer reprecipitated palladium" + line.why()))
            .thenExecute("pull the seeds", () -> {
                line.assertCircuitInputs();
                helper.assertEquals(0L, line.stored(Line.circuit(1).getItemStack()), "Circuits should not be pre-stocked");
                helper.assertEquals(0L, line.stored(Line.circuit(9).getItemStack()), "Circuits should not be pre-stocked");
                line.syncSeeds();
                helper.assertEquals(POWDER_SEEDS, line.seed(line.powder, "stock"), "The powder seeds" + line.why());
                helper.assertEquals(SALT_SEEDS, line.seed(line.salt, "stock"), "The salt seeds" + line.why());
                helper.assertEquals(
                    ENRICHED_SEEDS,
                    line.seed(line.enriched(1), "stock"),
                    "The enriched ammonia seeds" + line.why());
            })
            .thenExecute("order ten reprecipitated palladium", () -> link.set(line.submit(line.raw, 2L * ROUNDS)))
            .thenWaitUntil("wait for the job to finish", 7000, () -> {
                helper.assertFalse(link.get().isCanceled(), "The crafting job should run" + line.why());
                helper.assertFalse(line.cpuBusy(), "The crafting job should finish" + line.why());
                helper.assertEquals(2L * ROUNDS, line.stored(line.raw), "The order should be in storage" + line.why());
                helper.assertEquals(
                    POWDER_SPARE,
                    line.stored(line.powder),
                    "Metallic powder should remain an internal intermediate" + line.why());
                helper.assertEquals(
                    AMMONIA - 6000L * ROUNDS,
                    line.storedFluid(line.ammonia(1)),
                    "Every round should take 6000 L of ammonia" + line.why());
                helper.assertEquals(
                    ENRICHED_SPARE - 3000L * ROUNDS,
                    line.storedFluid(line.enriched(1)),
                    "Every round should take 3000 L of upstream enriched ammonia" + line.why());
                helper.assertEquals(POWDER_SEEDS, line.seed(line.powder, "stock"), "The powder seeds should be back" + line.why());
                helper.assertEquals(SALT_SEEDS, line.seed(line.salt, "stock"), "The salt seeds should be back" + line.why());
                helper.assertEquals(
                    ENRICHED_SEEDS,
                    line.seed(line.enriched(1), "stock"),
                    "The enriched ammonia seeds should be back" + line.why());
                helper.assertEquals(
                    0L,
                    line.seed(line.powder, "owed") + line.seed(line.salt, "owed") + line.seed(line.enriched(1), "owed"),
                    "No seed should be owed" + line.why());
                helper.assertEquals(0L, line.stored(Line.circuit(1).getItemStack()), "Every supplied circuit 1 should be used");
                helper.assertEquals(0L, line.stored(Line.circuit(9).getItemStack()), "Every supplied circuit 9 should be used");
                helper.assertTrue(ItemStack.areItemStacksEqual(line.provider.getStackInSlot(0), GTUtility.getIntegratedCircuit(1)),
                    "The provider's circuit 1 template should remain");
                helper.assertTrue(ItemStack.areItemStacksEqual(line.provider.getStackInSlot(1), GTUtility.getIntegratedCircuit(9)),
                    "The provider's circuit 9 template should remain");
                line.assertSettled();
            })
            .thenSucceed();
    }

    /**
     * Builds the Large Chemical Reactor with the given input hatch, an ME output bus and hatch, debug power and
     * creative maintenance. The auto-build puts a coil on every side centre; one stays, the rest become casings or
     * hatches.
     */
    static void buildReactor(GameTestHelper helper, int hatchId) {
        IGregTechTileEntity controller = place(
            helper,
            REACTOR,
            ItemList.Machine_Multi_LargeChemicalReactor,
            ForgeDirection.NORTH);
        ((IConstructable) controller.getMetaTileEntity())
            .construct(ItemList.Machine_Multi_LargeChemicalReactor.get(1), false);
        setBlock(helper, COIL, GregTechAPI.sBlockCasings5, 0);
        for (TestPos p : PLAIN_CASINGS) setBlock(helper, p, GregTechAPI.sBlockCasings8, 0);
        place(helper, CYCLIC, hatchId, ForgeDirection.WEST);
        place(helper, REACTOR_OUTPUT, ItemList.Hatch_Output_Bus_ME, ForgeDirection.WEST);
        place(helper, REACTOR_FLUID_OUTPUT, ItemList.Hatch_Output_ME, ForgeDirection.WEST);
        place(helper, REACTOR_ENERGY, ItemList.DebugEnergyHatch, ForgeDirection.EAST);
        place(helper, REACTOR_MAINTENANCE, CustomItemList.hatch_CreativeMaintenance, ForgeDirection.DOWN);
        MTEHatchEnergyDebug energy = (MTEHatchEnergyDebug) helper.gtnh()
            .gtTile(REACTOR_ENERGY)
            .getMetaTileEntity();
        energy.setVoltageTier(6);
        energy.setAmperage(2);
        energy.setEUVar(Long.MAX_VALUE);
        Multiblock reactor = helper.gtnh()
            .multiblock(REACTOR);
        helper.assertTrue(reactor.forceStructureCheck(), "The Large Chemical Reactor should form");
        buildNetworkLink(helper);
    }

    private static void buildNetworkLink(GameTestHelper helper) {
        Block cableBus = AEApi.instance().definitions().blocks().multiPart().maybeBlock().get();
        ItemStack cable = AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1);
        for (int z = 4; z < 10; z++) {
            TestPos pos = helper.absolute(at(0, 1, z));
            helper.getWorld().setBlock(pos.x(), pos.y(), pos.z(), cableBus, 0, 3);
            IPartHost host = helper.assertInstanceOf(
                IPartHost.class,
                helper.getWorld().getTileEntity(pos.x(), pos.y(), pos.z()),
                "The network link should host an ME cable");
            helper.assertEquals(
                ForgeDirection.UNKNOWN,
                host.addPart(cable.copy(), ForgeDirection.UNKNOWN, null),
                "The Fluix ME glass cable should be placed");
        }
    }

    private static void setBlock(GameTestHelper helper, TestPos pos, Block block, int meta) {
        TestPos abs = helper.absolute(pos);
        helper.getWorld()
            .setBlock(abs.x(), abs.y(), abs.z(), block, meta, 3);
    }

    /** The two machines, their hatches, the network's stock and a trace of every change. */
    static final class Line {

        final GameTestHelper helper;
        final ItemStack powder, salt, raw;
        final PatternDualInputHatch laneHatch;
        final CyclicPatternDualInputHatch cyclic;
        final ProgrammingCircuitProvider provider;
        final List<String> trace = new ArrayList<>();

        Line(GameTestHelper helper) {
            this(helper, true);
        }

        Line(GameTestHelper helper, boolean withProvider) {
            this.helper = helper;
            powder = WerkstoffLoader.PDMetallicPowder.get(OrePrefixes.dust);
            salt = WerkstoffLoader.PDSalt.get(OrePrefixes.dust);
            raw = WerkstoffLoader.PDRawPowder.get(OrePrefixes.dust);
            ChancePatternTests.buildSifter(helper, true, false);
            laneHatch = helper.assertInstanceOf(
                PatternDualInputHatch.class,
                helper.gtnh()
                    .metaTileEntity(ChancePatternTests.HATCH),
                "The sifter's hatch should be placed");
            buildReactor();
            cyclic = helper.assertInstanceOf(
                CyclicPatternDualInputHatch.class,
                helper.gtnh()
                    .metaTileEntity(CYCLIC),
                "The cyclic hatch should be placed");
            if (withProvider) {
                provider = helper.assertInstanceOf(ProgrammingCircuitProvider.class,
                    place(helper, CIRCUITS, Config.metaTileEntityOffset + Registration.CircuitProviderOffset, ForgeDirection.DOWN)
                        .getMetaTileEntity(), "The circuit provider should be placed");
                provider.setInventorySlotContents(0, GTUtility.getIntegratedCircuit(1));
                provider.setInventorySlotContents(1, GTUtility.getIntegratedCircuit(9));
            } else provider = null;
            helper.setSlot("drive", 0, itemCell());
            helper.setSlot("drive", 1, fluidCell(ammonia((int) AMMONIA)));
            helper.setSlot("drive", 2, fluidCell(enriched((int) (ENRICHED_SEEDS + ENRICHED_SPARE))));
            startTrace();
        }

        FluidStack ammonia(int mb) {
            return Materials.Ammonia.getGas(mb);
        }

        FluidStack enriched(int mb) {
            return WerkstoffLoader.PDAmmonia.getFluidOrGas(mb);
        }

        void buildReactor() {
            PalladiumLineTests
                .buildReactor(helper, Config.metaTileEntityOffset + Registration.CyclicPatternOffset);
        }

        @SuppressWarnings("unchecked")
        ItemStack itemCell() {
            ItemStack cell = AEApi.instance()
                .definitions()
                .items()
                .cell64k()
                .maybeStack(1)
                .get();
            IMEInventoryHandler<IAEItemStack> inv = AEApi.instance()
                .registries()
                .cell()
                .getHandler(cell)
                .getCellInventory(cell, null, ITEM_STACK_TYPE);
            helper.assertNull(
                inv.injectItems(ae(powder, POWDER_SEEDS + POWDER_SPARE), Actionable.MODULATE, SOURCE),
                "The cell should take the powder");
            helper.assertNull(
                inv.injectItems(ae(salt, SALT_SEEDS + SALT_SPARE), Actionable.MODULATE, SOURCE),
                "The cell should take the salt");
            return cell;
        }

        @SuppressWarnings("unchecked")
        ItemStack fluidCell(FluidStack content) {
            ItemStack cell = new ItemStack(ItemAndBlockHolder.CELL64K);
            IMEInventoryHandler<IAEFluidStack> inv = AEApi.instance()
                .registries()
                .cell()
                .getHandler(cell)
                .getCellInventory(cell, null, FLUID_STACK_TYPE);
            helper.assertNull(
                inv.injectItems(AEFluidStack.create(content), Actionable.MODULATE, SOURCE),
                "The fluid cell should take " + content.getLocalizedName());
            return cell;
        }

        void installCycle() {
            setting(ae(salt, 1), false, false);
            setting(AEFluidStack.create(enriched(1)), false, true);
            setting(ae(powder, 1), false, false, 0);
            ItemStack enrichPattern = pattern(
                new IAEStack<?>[] { circuit(1), ae(powder, 1), AEFluidStack.create(ammonia(1000)) },
                new IAEStack<?>[] { AEFluidStack.create(enriched(1000)) },
                false);
            ItemStack reactorPattern = pattern(
                new IAEStack<?>[] { circuit(9), ae(powder, 9), AEFluidStack.create(enriched(9000)) },
                new IAEStack<?>[] { ae(salt, 16), ae(raw, 2) },
                false);
            ItemStack sifterPattern = pattern(
                new IAEStack<?>[] { ae(salt, 16) },
                new IAEStack<?>[] { ae(powder, 15) },
                true);
            cyclic.getPatterns()
                .setInventorySlotContents(0, reactorPattern);
            cyclic.getPatterns()
                .setInventorySlotContents(4, sifterPattern);
            cyclic.getPatterns()
                .setInventorySlotContents(8, enrichPattern);
            TestPos lane = helper.absolute(ChancePatternTests.HATCH), me = helper.absolute(CYCLIC);
            bindLane(1, 4, lane.x() - me.x(), lane.y() - me.y(), lane.z() - me.z());
        }

        /** A programming circuit carrying the given integrated circuit, unwrapped by the hatch for its own buffer. */
        static IAEItemStack circuit(int n) {
            return ae(ItemProgrammingCircuit.wrap(GTUtility.getIntegratedCircuit(n)), 1);
        }

        /** A fluid-capable pattern, written the way the cyclic hatch writes its balancings. */
        static ItemStack pattern(IAEStack<?>[] in, IAEStack<?>[] out, boolean chance) {
            ItemStack pattern = new ItemStack(ItemAndBlockHolder.PATTERN);
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagList ins = FluidPatternDetails.writeStackArray(in), outs = FluidPatternDetails.writeStackArray(out);
            tag.setTag("Inputs", ins);
            tag.setTag("in", ins.copy());
            tag.setTag("Outputs", outs);
            tag.setTag("out", outs.copy());
            tag.setInteger("combine", 0);
            tag.setBoolean("beSubstitute", false);
            if (chance) tag.setBoolean(ChanceMark.KEY, true);
            pattern.setTagCompound(tag);
            return pattern;
        }

        /** The cyclic hatch's setting of a stack in its first group: whether it is a product and whether an input. */
        void setting(IAEStack<?> stack, boolean output, boolean input) {
            setting(stack, output, input, 5);
        }

        @SuppressWarnings("unchecked")
        void setting(IAEStack<?> stack, boolean output, boolean input, int tolerance) {
            try {
                Field settings = CyclicPatternDualInputHatch.class.getDeclaredField("settings");
                settings.setAccessible(true);
                Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
                Method of = key.getDeclaredMethod("of", IAEStack.class);
                of.setAccessible(true);
                Class<?> setting = Class
                    .forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$CycSetting");
                Constructor<?> make = setting.getDeclaredConstructor();
                make.setAccessible(true);
                Object s = make.newInstance();
                Field out = setting.getDeclaredField("output");
                out.setAccessible(true);
                out.setBoolean(s, output);
                Field in = setting.getDeclaredField("input");
                in.setAccessible(true);
                in.setBoolean(s, input);
                Field tol = setting.getDeclaredField("tol");
                tol.setAccessible(true);
                tol.setInt(s, tolerance);
                ((Map<Object, Object>) ((List<?>) settings.get(cyclic)).get(0)).put(of.invoke(null, stack), s);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's settings should be reachable", e);
            }
        }

        /** Points a lane of the cyclic hatch at the sifter's hatch and puts the given pattern slot on it. */
        void bindLane(int lane, int slot, int dx, int dy, int dz) {
            try {
                Field offsets = CyclicPatternDualInputHatch.class.getDeclaredField("laneOffset");
                offsets.setAccessible(true);
                ((int[][]) offsets.get(cyclic))[lane] = new int[] { dx, dy, dz };
                Method set = CyclicPatternDualInputHatch.class.getDeclaredMethod("setPatternLane", int.class, int.class);
                set.setAccessible(true);
                set.invoke(cyclic, slot, lane);
                Field dirty = CyclicPatternDualInputHatch.class.getDeclaredField("analysisDirty");
                dirty.setAccessible(true);
                dirty.setBoolean(cyclic, true);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's lanes should be reachable", e);
            }
        }

        void syncSeeds() {
            try {
                Method m = CyclicPatternDualInputHatch.class.getDeclaredMethod("syncSeeds");
                m.setAccessible(true);
                m.invoke(cyclic);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's seeds should be reachable", e);
            }
        }

        long seed(ItemStack item, String field) {
            return seed(ae(item, 1), field);
        }

        long seed(FluidStack fluid, String field) {
            return seed(AEFluidStack.create(fluid), field);
        }

        /** A seed's stock or owed count in the cyclic hatch; -1 while the stack is no seed. */
        long seed(IAEStack<?> stack, String field) {
            try {
                Field seeds = CyclicPatternDualInputHatch.class.getDeclaredField("seeds");
                seeds.setAccessible(true);
                Class<?> key = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey");
                Method of = key.getDeclaredMethod("of", IAEStack.class);
                of.setAccessible(true);
                Object st = ((Map<?, ?>) seeds.get(cyclic)).get(of.invoke(null, stack));
                if (st == null) return -1;
                Field f = st.getClass()
                    .getDeclaredField(field);
                f.setAccessible(true);
                return f.getLong(st);
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's seeds should be reachable", e);
            }
        }

        TileChanceController chanceController() {
            return helper.assertTileEntityPresent(TileChanceController.class, "chance_controller");
        }

        IStorageGrid storage() {
            try {
                return chanceController().getProxy()
                    .getStorage();
            } catch (GridAccessException e) {
                throw new AssertionError("The network storage should be reachable", e);
            }
        }

        ICraftingGrid crafting() {
            try {
                return chanceController().getProxy()
                    .getCrafting();
            } catch (GridAccessException e) {
                throw new AssertionError("The crafting grid should be reachable", e);
            }
        }

        ICraftingPatternDetails publishedCycle() {
            return crafting().getCraftingFor(ae(raw, 1), null, -1, helper.getWorld()).iterator().next();
        }

        void assertCircuitInputs() {
            long one = 0, nine = 0;
            for (IAEStack<?> input : publishedCycle().getCondensedAEInputs()) {
                if (input.equals(circuit(1))) one += input.getStackSize();
                if (input.equals(circuit(9))) nine += input.getStackSize();
            }
            helper.assertEquals(6L, one, "The published cycle should require six programming circuits 1");
            helper.assertEquals(1L, nine, "The published cycle should require one programming circuit 9");
        }

        void assertReady() {
            if (provider != null) helper.assertTrue(provider.getProxy().isActive(), "The circuit provider should be on the network");
            IGridNode node = helper.assertInstanceOf(IGridHost.class, helper.assertTileEntityPresent("controller"))
                .getGridNode(ForgeDirection.UNKNOWN);
            helper.assertTrue(node != null && node.isActive(), "The ME controller should be active");
            helper.assertTrue(
                chanceController().getProxy()
                    .isActive(),
                "The chance controller should be on the network");
            helper.assertTrue(
                laneHatch.getProxy()
                    .isActive(),
                "The sifter's hatch should be on the network");
            helper.assertTrue(
                cyclic.getProxy()
                    .isActive(),
                "The cyclic hatch should be on the network");
            helper.assertTrue(
                helper.assertTileEntityPresent(TileCraftingTile.class, "cpu_storage")
                    .isFormed(),
                "The crafting CPU should form");
        }

        long stored(ItemStack stack) {
            IAEItemStack s = storage().getItemInventory()
                .extractItems(ae(stack, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
            return s == null ? 0 : s.getStackSize();
        }

        long storedFluid(FluidStack fluid) {
            IAEFluidStack req = AEFluidStack.create(fluid);
            req.setStackSize(Long.MAX_VALUE);
            IAEFluidStack s = storage().getFluidInventory()
                .extractItems(req, Actionable.SIMULATE, SOURCE);
            return s == null ? 0 : s.getStackSize();
        }

        boolean offered(ItemStack stack) {
            return !crafting().getCraftingFor(ae(stack, 1), null, -1, helper.getWorld())
                .isEmpty();
        }

        boolean cpuBusy() {
            for (ICraftingCPU cpu : crafting().getCpus()) if (cpu.isBusy()) return true;
            return false;
        }

        ICraftingLink submit(ItemStack stack, long amount) {
            TileChanceController c = chanceController();
            Future<ICraftingJob> future = null;
            try {
                future = crafting().beginCraftingJob(
                    c.getWorldObj(),
                    c.getProxy()
                        .getGrid(),
                    SOURCE,
                    ae(stack, amount),
                    null);
                ICraftingJob job = future.get(5000, TimeUnit.MILLISECONDS);
                helper.assertFalse(job.isSimulation(), "The crafting job should be executable" + why());
                ICraftingLink link = crafting().submitJob(job, null, null, false, SOURCE);
                helper.assertNotNull(link, "The crafting job should start on the CPU" + why());
                return link;
            } catch (InterruptedException e) {
                Thread.currentThread()
                    .interrupt();
                throw new AssertionError("The crafting calculation was interrupted", e);
            } catch (Exception e) {
                if (future != null) future.cancel(true);
                throw new AssertionError("The crafting job should calculate and start" + why(), e);
            }
        }

        void assertSettled() {
            for (DualInvBuffer b : laneHatch.inv0) {
                ChanceData c = b.chance;
                helper.assertFalse(
                    c != null && (c.owed() || c.pending),
                    "A sifter buffer still owes outputs or waits for a refill" + why());
            }
            helper.assertFalse(cyclic.isBusy(), "The cyclic hatch should have handed out every dose" + why());
        }

        private void startTrace() {
            String[] last = { "" };
            int[] tick = { 0 };
            helper.onEachTick("trace the palladium line", () -> {
                tick[0]++;
                StringBuilder sb = new StringBuilder();
                sb.append(" powder=")
                    .append(stored(powder))
                    .append(" salt=")
                    .append(stored(salt))
                    .append(" raw=")
                    .append(stored(raw))
                    .append(" ammonia=")
                    .append(storedFluid(ammonia(1)))
                    .append(" enriched=")
                    .append(storedFluid(enriched(1)))
                    .append(" seeds=")
                    .append(seed(powder, "stock"))
                    .append('/')
                    .append(seed(powder, "owed"))
                    .append(',')
                    .append(seed(salt, "stock"))
                    .append('/')
                    .append(seed(salt, "owed"))
                    .append(',')
                    .append(seed(enriched(1), "stock"))
                    .append('/')
                    .append(seed(enriched(1), "owed"))
                    .append(cyclic.isBusy() ? " dosing" : "")
                    .append(cpuBusy() ? " cpu-busy" : "");
                for (int i = 0; i < laneHatch.inv0.size(); i++) {
                    DualInvBuffer b = laneHatch.inv0.get(i);
                    List<IAEStack<?>> contents = b.chanceContents();
                    if (contents.isEmpty() && b.chance == null) continue;
                    sb.append(" b")
                        .append(i)
                        .append(contents);
                    if (b.chance != null) sb.append(" owe=")
                        .append(Arrays.toString(b.chance.debt))
                        .append(b.chance.pending ? " pending" : "");
                }
                String now = sb.toString();
                if (!now.equals(last[0])) {
                    trace.add("t" + tick[0] + now);
                    last[0] = now;
                }
            });
        }

        /** The cyclic hatch's analysis of its first group: balancings, warnings and what it publishes. */
        String analysisState() {
            try {
                Field analysis = CyclicPatternDualInputHatch.class.getDeclaredField("analysis");
                analysis.setAccessible(true);
                Field hatchWarnings = CyclicPatternDualInputHatch.class.getDeclaredField("hatchWarnings");
                hatchWarnings.setAccessible(true);
                Field published = CyclicPatternDualInputHatch.class.getDeclaredField("published");
                published.setAccessible(true);
                Method analysing = CyclicPatternDualInputHatch.class.getDeclaredMethod("analysing");
                analysing.setAccessible(true);
                CycleAnalysis<?> a = (CycleAnalysis<?>) ((Object[]) analysis.get(cyclic))[0];
                StringBuilder sb = new StringBuilder("analysing=").append(analysing.invoke(cyclic))
                    .append(" published=")
                    .append(((Object[]) published.get(cyclic)).length)
                    .append(" hatchWarnings=")
                    .append(((List<?>) hatchWarnings.get(cyclic)).get(0));
                if (a != null) sb.append(" cyclic=")
                    .append(a.getCyclic())
                    .append(" products=")
                    .append(a.getProducts())
                    .append(" balancings=")
                    .append(a.getBalancings())
                    .append(" warnings=")
                    .append(a.getWarnings())
                    .append(" unsolved=")
                    .append(a.getUnsolved());
                String s = sb.toString();
                if (a != null) {
                    Method name = Class.forName("reobf.proghatches.gt.metatileentity.CyclicPatternDualInputHatch$SKey")
                        .getDeclaredMethod("name");
                    name.setAccessible(true);
                    for (Object k : a.getItems()) s = s.replace(k.toString(), String.valueOf(name.invoke(k)));
                }
                return s;
            } catch (ReflectiveOperationException e) {
                throw new AssertionError("The cyclic hatch's analysis should be reachable", e);
            }
        }

        String why() {
            int from = Math.max(0, trace.size() - 80);
            return "\n" + analysisState() + (from > 0 ? "\n(" + from + " earlier lines)" : "") + "\n"
                + String.join("\n", trace.subList(from, trace.size()));
        }
    }
}
