package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.FluidPatternDetails;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.networking.crafting.CraftingItemList;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.storage.data.IAEStack;
import appeng.api.util.WorldCoord;
import appeng.me.cache.CraftingGridCache;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.inv.MEInventoryCrafting;
import appeng.util.item.AEItemStack;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.ae.cpu.IExternalManagerHolder;
import reobf.proghatches.ae.cpu.TileCPU;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import thaumcraft.api.aspects.Aspect;
import thaumicenergistics.common.storage.AEEssentiaStack;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"thaumicenergistics"})
public class CondenserEssentiaTests {

    private static final class CPU extends CraftingCPUCluster {
        CPU() {
            super(new WorldCoord(0, 0, 0), new WorldCoord(0, 0, 0));
        }

        void dispatch(IEnergyGrid energy, CraftingGridCache grid) {
            remainingOperations = 1;
            workableTasks.clear();
            workableTasks.putAll(tasks);
            executeCrafting(energy, grid);
        }

        @Override
        public void markDirty() {}
    }

    @GameTest(template = "craft", timeoutTicks = 200)
    public static void additionalPushesDeliverEssentia(GameTestHelper h) {
        run(h, 32, false);
    }

    @GameTest(template = "craft", timeoutTicks = 200)
    public static void shortEssentiaReturnsSeed(GameTestHelper h) {
        run(h, 1, false);
    }

    @GameTest(template = "craft", timeoutTicks = 200)
    public static void rejectedPushReturnsEssentiaAndSeed(GameTestHelper h) {
        run(h, 32, true);
    }

    private static void run(GameTestHelper h, int availableRounds, boolean rejectSecond) {
        TileCPU manager = (TileCPU) ChancePatternTests.place(h, at(4, 1, 2),
            Config.metaTileEntityOffset + Registration.CPU, ForgeDirection.WEST).getMetaTileEntity();
        TileChanceController power = h.assertTileEntityPresent(TileChanceController.class, "chance_controller");
        h.startSequence().thenWaitUntil("wait for power", 100, () -> h.assertTrue(power.getProxy().isActive(), "powered grid"))
            .thenExecute("dispatch a 32-copy processing order", () -> {
                CPU cpu = new CPU();
                try {
                    Field skips = TileCPU.class.getDeclaredField("accCacheCondenser");
                    skips.setAccessible(true);
                    skips.setLong(manager, 64);
                    ((IExternalManagerHolder) (Object) cpu).acceptIExternalManager(manager);
                    var seed = AEItemStack.create(new ItemStack(Items.wheat_seeds));
                    var aspect = new AEEssentiaStack(Aspect.VOID, 8);
                    var out = AEItemStack.create(new ItemStack(Items.ender_pearl));
                    FluidPatternDetails pattern = new FluidPatternDetails(new ItemStack(ItemAndBlockHolder.PATTERN));
                    pattern.setInputs(new IAEStack<?>[] {seed, aspect});
                    pattern.setOutputs(new IAEStack<?>[] {out});
                    CraftingGridCache grid = new CraftingGridCache(null);
                    List<Long> seeds = new ArrayList<>(), essentia = new ArrayList<>();
                    grid.addCraftingOption(new ICraftingMedium() {
                        public boolean isBusy() { return false; }
                        public boolean pushPattern(ICraftingPatternDetails details, InventoryCrafting table) {
                            MEInventoryCrafting inv = (MEInventoryCrafting) table;
                            seeds.add(amount(inv, seed));
                            essentia.add(amount(inv, aspect));
                            return !rejectSecond || seeds.size() == 1;
                        }
                    }, pattern);
                    cpu.addStorage(seed.copy().setStackSize(32));
                    cpu.addStorage(aspect.copy().setStackSize(8L * availableRounds));
                    cpu.addCrafting(pattern, 32);
                    cpu.dispatch(power.getProxy().getEnergy(), grid);
                    int accepted = rejectSecond ? 1 : availableRounds;
                    h.assertEquals(accepted + (rejectSecond ? 1 : 0), seeds.size(), "Push count; essentia per call: " + essentia);
                    for (int i = 0; i < seeds.size(); i++) {
                        h.assertEquals(1L, seeds.get(i).longValue(), "Seeds on push " + (i + 1));
                        h.assertEquals(8L, essentia.get(i).longValue(), "Essentia on push " + (i + 1) + "; all: " + essentia);
                    }
                    h.assertEquals(32L - accepted, cpu.getStackAmount(seed, CraftingItemList.STORAGE), "Remaining seeds");
                    h.assertEquals((availableRounds - accepted) * 8L, cpu.getStackAmount(aspect, CraftingItemList.STORAGE), "Remaining essentia");
                    h.assertEquals((long) accepted, cpu.getStackAmount(out, CraftingItemList.ACTIVE), "Expected outputs");
                    h.assertEquals(32L - accepted, cpu.getStackAmount(out, CraftingItemList.PENDING), "Remaining tasks");
                } catch (Exception e) {
                    throw new AssertionError(e);
                } finally {
                    cpu.destroy();
                }
            }).thenSucceed();
    }

    private static long amount(MEInventoryCrafting table, IAEStack<?> type) {
        long n = 0;
        for (int i = 0; i < table.getSizeInventory(); i++) {
            IAEStack<?> stack = table.getAEStackInSlot(i);
            if (type.equals(stack)) n += stack.getStackSize();
        }
        return n;
    }
}
