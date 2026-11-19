package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;

import java.util.concurrent.Future;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEFluidStack;
import reobf.proghatches.gt.metatileentity.WaterProvider;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

/** The water provider answers a water order with fluid water, a bucket per craft. */
@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance")
public class WaterProviderTests {

    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "craft", timeoutTicks = 1200)
    public static void waterOrderEndsAsFluid(GameTestHelper h) {
        WaterProvider provider = (WaterProvider) ChancePatternTests
            .place(h, at(1, 1, 2), Config.metaTileEntityOffset + Registration.WaterProviderOffset, ForgeDirection.WEST)
            .getMetaTileEntity();
        h.setSlot(
            "drive",
            0,
            AEApi.instance()
                .definitions()
                .items()
                .cell64k()
                .maybeStack(1)
                .get());
        h.setSlot("drive", 1, new ItemStack(ItemAndBlockHolder.CELL64K));
        IAEFluidStack order = AEFluidStack.create(new FluidStack(FluidRegistry.WATER, 2500));
        Object[] job = new Object[1];
        ICraftingCPU[] cpu = new ICraftingCPU[1];

        h.startSequence()
            .thenWaitUntil("wait for the provider to offer water", 300, () -> {
                try {
                    h.assertTrue(
                        provider.getProxy()
                            .isActive(),
                        "The provider should be online");
                    h.assertTrue(provider.getProxy().getCrafting().getCraftingMultiPatterns().containsKey(order),
                        "Water should be craftable");
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            })
            .thenExecute("plan the order", () -> {
                try {
                    job[0] = provider.getProxy()
                        .getCrafting()
                        .beginCraftingJob(h.getWorld(), provider.getProxy().getGrid(), SOURCE, order, null);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            })
            .thenWaitUntil("wait for the plan", 200, () -> h.assertTrue(((Future<?>) job[0]).isDone(), "The plan should be ready"))
            .thenExecute("submit the order", () -> {
                try {
                    @SuppressWarnings("unchecked")
                    ICraftingJob plan = ((Future<ICraftingJob>) job[0]).get();
                    h.assertFalse(plan.isSimulation(), "The order should be executable");
                    cpu[0] = provider.getProxy().getCrafting().getCpus().iterator().next();
                    var link = provider.getProxy()
                        .getCrafting()
                        .submitJob(plan, null, cpu[0], false, SOURCE);
                    h.assertNotNull(link, "The CPU should accept the order");
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            })
            .thenWaitUntil("wait for the order", 600, () -> h.assertFalse(cpu[0].isBusy(), "The crafting CPU should finish the order"))
            .thenWaitUntil("wait for the water to be stored", 100, () -> h.assertEquals(3000L, water(provider), "Three buckets arrive"))
            .thenExecute("check the item storage", () -> {
                try {
                    for (IAEItemStack s : provider.getProxy()
                        .getStorage()
                        .getItemInventory()
                        .getStorageList())
                        h.assertFalse(s.getItem() instanceof ItemFluidDrop, "No fluid drop reaches the item storage");
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            })
            .thenSucceed();
    }

    private static long water(WaterProvider provider) {
        try {
            IAEFluidStack stored = provider.getProxy()
                .getStorage()
                .getFluidInventory()
                .getStorageList()
                .findPrecise(AEFluidStack.create(new FluidStack(FluidRegistry.WATER, 1)));
            return stored == null ? 0 : stored.getStackSize();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }
}
