package reobf.proghatches.gametests;

import static appeng.util.item.AEItemStackType.ITEM_STACK_TYPE;
import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.lang.reflect.Method;
import java.util.ArrayList;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.container.ContainerNull;
import appeng.util.inv.MEInventoryCrafting;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import reobf.proghatches.ae.EmitterPattern;
import reobf.proghatches.ae.PatternCraftingJob;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.ae.TileRequestTunnel;
import reobf.proghatches.ae.part2.RequestTunnel;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.item.ItemFixer;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.registration.Registration;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"thaumicenergistics"})
public class LongPatternBoundaryTests {
    private static final long MAX = Integer.MAX_VALUE, HALF = 1_500_000_000L;
    private static final ForgeDirection SIDE = ForgeDirection.WEST;

    private static IAEItemStack seed(long n) { return AEItemStack.create(new ItemStack(Items.wheat_seeds)).setStackSize(n); }
    private static IAEFluidStack water(long n) { return AEFluidStack.create(new FluidStack(FluidRegistry.WATER, 1)).setStackSize(n); }
    private static MEInventoryCrafting table(IAEStack<?>... stacks) {
        var t = new MEInventoryCrafting(new ContainerNull(), stacks.length, 1);
        for (int i = 0; i < stacks.length; i++) t.setInventorySlotContents(i, stacks[i]);
        return t;
    }
    private static ICraftingPatternDetails pattern(GameTestHelper h, IAEStack<?>... inputs) {
        var raw = PalladiumLineTests.Line.pattern(inputs, new IAEStack<?>[] { AEItemStack.create(new ItemStack(Items.apple))}, false);
        return ((ICraftingPatternItem) raw.getItem()).getPatternForItem(raw, h.getWorld());
    }

    @GameTest(template = "craft", timeoutTicks = 100)
    public static void circuitRepairKeepsCount(GameTestHelper h) {
        for (long n : new long[] { MAX, MAX + 1, 2 * HALF}) {
            ItemStack legacy = ItemProgrammingCircuit.wrap(new ItemStack(Items.apple), 1, true);
            IAEItemStack circuit = AEItemStack.create(legacy).setStackSize(n);
            ItemStack raw = PalladiumLineTests.Line.pattern(new IAEStack<?>[] { circuit, seed(n)},
                new IAEStack<?>[] { seed(1)}, false);
            var fixed = ((ItemFixer) MyMod.fixer).fix(raw, () -> {});
            var details = ((ICraftingPatternItem) fixed.getItem()).getPatternForItem(fixed, h.getWorld());
            h.assertEquals(n, details.getAEInputs()[0].getStackSize(), "Migrated circuit quantity");
            h.assertEquals(n, details.getAEInputs()[1].getStackSize(), "Other ingredient quantity");
        }
        h.succeed();
    }

    @GameTest(template = "craft", timeoutTicks = 100)
    public static void tunnelInput(GameTestHelper h) {
        for (long n : new long[] { MAX, MAX + 1}) {
            TileRequestTunnel tile = new TileRequestTunnel();
            tile.setWorldObj(h.getWorld());
            RequestTunnel b = (RequestTunnel) LongInputBufferTests.field(tile, "internal");
            h.assertTrue(tile.pushPattern(pattern(h, seed(n), water(n)), table(seed(n), water(n)), SIDE), "Tunnel accepts " + n);
            checkPending(h, b, n);
        }
        h.succeed();
    }

    @GameTest(template = "craft", timeoutTicks = 100)
    public static void tunnelMergeAndReload(GameTestHelper h) throws Exception {
        var tile = new TileRequestTunnel();
        tile.setWorldObj(h.getWorld());
        var b = (RequestTunnel) LongInputBufferTests.field(tile, "internal");
        for (int i = 0; i < 2; i++) h.assertTrue(tile.pushPattern(pattern(h, seed(HALF), water(HALF)),
            table(seed(HALF), water(HALF)), SIDE), "1.5G push fits");
        checkPending(h, b, 2 * HALF);
        for (int i = 0; i < 2; i++) {
            b.injectCraftedItems(null, seed(HALF), Actionable.MODULATE);
            b.injectCraftedItems(null, water(HALF), Actionable.MODULATE);
        }
        for (String method : new String[]{"fillStacksIntoFirstSlots", "fillStacksIntoFirstSlotsF"}) {
            Method m = RequestTunnel.class.getDeclaredMethod(method, ArrayList.class);
            m.setAccessible(true);
            m.invoke(b, method.endsWith("F") ? b.cacheFR : b.cacheR);
        }
        var saved = new NBTTagCompound();
        b.writeToNBT_AENetworkX(saved);
        b.readFromNBT_AENetworkX(saved);
        h.assertEquals(2 * HALF, b.cacheR.stream().mapToLong(s -> s.stackSize).sum(), "Return items after reload");
        h.assertEquals(2 * HALF, b.cacheFR.stream().mapToLong(s -> s.amount).sum(), "Return fluid after reload");
        h.assertEquals((int) MAX, b.drain(SIDE, Integer.MAX_VALUE, false).amount, "Simulated drain");
        h.assertEquals((int) MAX, b.drain(SIDE, Integer.MAX_VALUE, true).amount, "First drain");
        h.assertEquals(2 * HALF - MAX, b.cacheFR.stream().mapToLong(s -> s.amount).sum(), "Undrained fluid");
        h.succeed();
    }

    private static void checkPending(GameTestHelper h, RequestTunnel b, long expected) {
        var tag = new NBTTagCompound();
        b.writeToNBT_AENetworkX(tag);
        b.readFromNBT_AENetworkX(tag);
        b.writeToNBT_AENetworkX(tag);
        NBTTagList list = tag.getTagList("pendingInputs", 10);
        long items = 0, fluids = 0;
        for (int i = 0; i < list.tagCount(); i++) {
            var stack = IAEStack.fromNBTGeneric(list.getCompoundTagAt(i));
            if (seed(1).equals(stack)) items += stack.getStackSize();
            if (water(1).equals(stack)) fluids += stack.getStackSize();
        }
        h.assertEquals(expected, items, "Pending seeds");
        h.assertEquals(expected, fluids, "Pending water");
    }

    private static final class Tank extends TileEntityFurnace implements IFluidHandler {
        final FluidTank tank = new FluidTank(Integer.MAX_VALUE);
        public int fill(ForgeDirection side, FluidStack s, boolean commit) {return tank.fill(s, commit);}
        public FluidStack drain(ForgeDirection side, int n, boolean commit) {return tank.drain(n, commit);}
        public FluidStack drain(ForgeDirection side, FluidStack s, boolean commit) {return tank.drain(s.amount, commit);}
        public boolean canFill(ForgeDirection side, Fluid f) {return true;}
        public boolean canDrain(ForgeDirection side, Fluid f) {return true;}
        public FluidTankInfo[] getTankInfo(ForgeDirection side) {return new FluidTankInfo[]{tank.getInfo()};}
    }

    @GameTest(template = "craft", timeoutTicks = 100)
    public static void maTankBoundary(GameTestHelper h) {
        var pos = h.absolute(at(3, 1, 2));
        var ma = new TileMolecularAssemblerInterface();
        ma.setWorldObj(h.getWorld());
        ma.xCoord = pos.x() - 1;
        ma.yCoord = pos.y();
        ma.zCoord = pos.z();
        var tank = new Tank();
        h.getWorld().setBlock(pos.x(), pos.y(), pos.z(), Blocks.furnace);
        h.getWorld().setTileEntity(pos.x(), pos.y(), pos.z(), tank);
        try {
            for (long n : new long[] { HALF, HALF, MAX, MAX + 1 }) {
                boolean accepted = ma.pushPattern(pattern(h, water(n)), table(water(n)), SIDE);
                h.assertEquals(n <= MAX, accepted, "Push " + n + " delivered " + tank.tank.getFluidAmount());
                h.assertEquals(n <= MAX ? n : 0L, (long) tank.tank.getFluidAmount(), "Tank contents");
                tank.tank.setFluid(null);
            }
        } finally {
            h.getWorld().setBlockToAir(pos.x(), pos.y(), pos.z());
        }
        h.succeed();
    }

    @GameTest(template = "sifter", timeoutTicks = 100)
    public static void hatchRejectsEssentiaAtomically(GameTestHelper h) {
        ChancePatternTests.buildSifter(h, true, Config.metaTileEntityOffset + Registration.ExPatternOffset);
        var hatch = (PatternDualInputHatch) h.gtnh().metaTileEntity(ChancePatternTests.HATCH);
        hatch.skipActiveCheck = true;
        var aspect = new AEEssentiaStack(Aspect.VOID, 8);
        var details = pattern(h, seed(1), aspect);
        h.assertFalse(hatch.pushPattern(details, table(seed(1), aspect)), "Mixed single push");
        h.assertEquals(0, hatch.pushPatternMulti(details, table(seed(1), aspect), 2)[0], "Mixed multi push");
        h.assertTrue(hatch.inputs().items[0] == null && hatch.inv0.stream().allMatch(b -> b.isEmpty()), "All buffers empty");
        h.succeed();
    }

    @GameTest(template = "craft", timeoutTicks = 240)
    public static void submitterCountsLargeIngredients(GameTestHelper h) {
        var grid = h.assertTileEntityPresent(TileChanceController.class, "chance_controller");
        var cell = AEApi.instance().definitions().items().cellSingularity().maybeStack(1).get();
        IMEInventoryHandler<IAEItemStack> inv = AEApi.instance().registries().cell().getHandler(cell)
            .getCellInventory(cell, null, ITEM_STACK_TYPE);
        for (int i = 0; i < 2; i++) {
            h.assertNull(inv.injectItems(seed(HALF), Actionable.MODULATE, new BaseActionSource()), "Cell accepts stock");
        }
        h.setSlot("drive", 0, cell);
        h.startSequence().thenWaitUntil("wait for stock", 120, () -> {
            try {
                var stored = grid.getProxy().getStorage().getItemInventory().getStorageList().findPrecise(seed(1));
                h.assertEquals(2 * HALF, stored == null ? 0L : stored.getStackSize(), "Stock");
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }).thenExecute("count and withdraw copies", () -> {
            try {
                var storage = grid.getProxy().getStorage();
                var source = new MachineSource(grid);
                for (long n : new long[] { MAX, MAX + 1, HALF }) {
                    var job = new PatternCraftingJob(pattern(h, seed(n)), storage);
                    h.assertEquals((int) (2 * HALF / n), job.canBeDone(grid.getProxy(), source), "Copies with ingredient " + n);
                }
                var cpu = new appeng.me.cluster.implementations.CraftingCPUCluster(
                    new appeng.api.util.WorldCoord(0, 0, 0), new appeng.api.util.WorldCoord(0, 0, 0));
                try {
                    new PatternCraftingJob(pattern(h, seed(MAX + 1)), storage).startCrafting(null, cpu, source);
                    h.assertEquals(MAX + 1, cpu.getStackAmount(seed(1), appeng.api.networking.crafting.CraftingItemList.STORAGE),
                        "The CPU receives the whole input");
                    h.assertEquals(2 * HALF - MAX - 1, storage.getItemInventory().getStorageList()
                        .findPrecise(seed(1)).getStackSize(), "Only the submitted amount leaves ME");
                } finally {
                    cpu.destroy();
                }
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        }).thenSucceed();
    }

    @GameTest(template = "craft", timeoutTicks = 100)
    public static void multiplierOutputView(GameTestHelper h) {
        for (long n : new long[] { MAX, MAX + 1, 2 * HALF}) {
            int m = n == MAX ? 1 : 2;
            var detail = new EmitterPattern(new ItemStack(MyMod.emitterpattern), seed(n / m));
            var scaled = new PatternDualInputHatch.DA(detail, m);
            h.assertEquals(n, scaled.getCondensedAEOutputs()[0].getStackSize(), "Exact AE output");
            h.assertEquals((int) Math.min(MAX, n), scaled.getOutput(null, h.getWorld()).stackSize, "Bounded item view for " + n);
        }
        h.succeed();
    }
}
