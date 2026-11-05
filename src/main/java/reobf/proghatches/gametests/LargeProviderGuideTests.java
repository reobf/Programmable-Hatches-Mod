package reobf.proghatches.gametests;

import static com.gtnewhorizons.horizonqa.api.TestPos.at;
import static reobf.proghatches.gametests.ChancePatternTests.ae;
import static reobf.proghatches.gametests.ChancePatternTests.place;

import java.util.HashSet;
import java.util.concurrent.Future;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.gson.JsonObject;
import com.gtnewhorizons.horizonqa.api.GameTestHelper;
import com.gtnewhorizons.horizonqa.api.TestPos;
import com.gtnewhorizons.horizonqa.api.annotation.GameTest;
import com.gtnewhorizons.horizonqa.api.annotation.GameTestHolder;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEColor;
import gregtech.api.enums.ItemList;
import gregtech.api.metatileentity.implementations.MTEHatchEnergyDebug;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import tectech.thing.CustomItemList;

@GameTestHolder(value = MyMod.MODID, templatePrefix = "chance", requiredMods = {"guidenh"})
public class LargeProviderGuideTests {

    private static final TestPos CONTROL = at(3, 1, 2), ORDINARY = at(2, 1, 3), PREFAB = at(4, 1, 3),
        ACCESS = at(2, 1, 4), REMOTE = at(0, 1, 5);
    private static final BaseActionSource SOURCE = new BaseActionSource();

    @GameTest(template = "large_provider", timeoutTicks = 900)
    public static void combinesLocalPrefabAndNetworkProviders(GameTestHelper helper) {
        Run r = new Run(helper);
        helper.startSequence()
            .thenWaitUntil("wait for the large provider and both networks", 300, r::ready)
            .thenExecute("record the structure and provider configuration", r::record)
            .thenExecute("select four circuits per operation", r::multiply)
            .thenWaitUntil("publish the multiplied pattern", 100, r::multiplied)
            .thenExecute("order eight programming circuits", r::request)
            .thenWaitUntil("submit the circuit crafting job", 100, r::submit)
            .thenWaitUntil("collect all eight circuits", 150, r::finished)
            .thenExecute("install the upgrade chip", r::installChip)
            .thenWaitUntil("verify the installed chip", 40, () -> helper.assertTrue(r.large.instant(), "The chip should enable instant completion"))
            .thenExecute("enable unused-circuit cleanup", r::cleanup)
            .thenWaitUntil("observe cleanup", 100, () -> helper.assertEquals(0L, r.stored(), "Idle stored programming circuits should be removed"))
            .thenExecute("save the tutorial recording", r::save)
            .thenSucceed();
    }

    private static final class Run {
        final GameTestHelper h;
        final LargeProgrammingCircuitProvider large;
        final ProgrammingCircuitProvider ordinary, remote;
        final GuideSceneRecorder scene;
        final ItemStack mold = ItemList.Shape_Mold_Ingot.get(1), remoteMold = ItemList.Shape_Mold_Plate.get(1);
        final ItemStack output = ItemProgrammingCircuit.wrap(mold);
        Future<ICraftingJob> job;
        boolean submitted;

        Run(GameTestHelper h) {
            this.h = h;
            large = (LargeProgrammingCircuitProvider) place(h, CONTROL,
                Config.metaTileEntityOffset + Registration.LargeProviderOffset, ForgeDirection.NORTH).getMetaTileEntity();
            large.construct(new ItemStack(net.minecraft.init.Items.stick, 2), false);
            place(h, at(2, 1, 2), CustomItemList.hatch_CreativeMaintenance, ForgeDirection.NORTH);
            var energy = (MTEHatchEnergyDebug) place(h, at(4, 1, 2), ItemList.DebugEnergyHatch,
                ForgeDirection.NORTH).getMetaTileEntity();
            energy.setVoltageTier(4); energy.setAmperage(2); energy.setEUVar(Long.MAX_VALUE);
            ordinary = (ProgrammingCircuitProvider) place(h, ORDINARY, Config.metaTileEntityOffset + Registration.CircuitProviderOffset,
                ForgeDirection.WEST).getMetaTileEntity();
            ordinary.setInventorySlotContents(0, mold.copy());
            place(h, PREFAB, Config.metaTileEntityOffset + Registration.PrefabOffset + 3, ForgeDirection.EAST);
            place(h, ACCESS, Config.metaTileEntityOffset + Registration.ChainerOffset, ForgeDirection.WEST);
            remote = (ProgrammingCircuitProvider) place(h, REMOTE, Config.metaTileEntityOffset + Registration.CircuitProviderOffset,
                ForgeDirection.NORTH).getMetaTileEntity();
            remote.setInventorySlotContents(0, remoteMold.copy());
            h.setBlock(3, 1, 3, AEApi.instance().definitions().blocks().craftingAccelerator().maybeBlock().get(), 2);
            for (TestPos p : new TestPos[] {at(1, 1, 2), at(1, 1, 1), at(2, 1, 1), at(3, 1, 1), at(1, 1, 4)}) cable(p);
            h.setSlot("drive", 0, AEApi.instance().definitions().items().cell64k().maybeStack(1).get());
            h.assertTrue(h.gtnh().multiblock(CONTROL).forceStructureCheck(), "The large provider should form");
            scene = new GuideSceneRecorder(h, "large_provider", "proghatches.guide.large", 6, 3, 7);
        }

        void cable(TestPos p) {
            TestPos a = h.absolute(p);
            h.getWorld().setBlock(a.x(), a.y(), a.z(), AEApi.instance().definitions().blocks().multiPart().maybeBlock().get(), 0, 3);
            ((IPartHost) h.getWorld().getTileEntity(a.x(), a.y(), a.z())).addPart(
                AEApi.instance().definitions().parts().cableGlass().stack(AEColor.Transparent, 1), ForgeDirection.UNKNOWN, null);
        }

        void ready() {
            h.assertTrue(large.getProxy().isActive() && large.getBaseMetaTileEntity().isActive(), "The large provider needs power and a channel");
            h.assertTrue(remote.getProxy().isActive(), "The remote provider should be connected to its own network");
            h.assertEquals(3, large.providers.size(), "Three provider components should be counted");
            h.assertEquals(768L, large.getConsumption(), "Three providers should consume 768 EU/t");
            h.assertTrue(large.checkLoop(new HashSet<>()), "The two networks should form an acyclic provider graph");
            offered(output, 1);
            offered(ItemProgrammingCircuit.wrap(remoteMold), 1);
            offered(ItemProgrammingCircuit.wrap(ItemList.Shape_Extruder_Rod.get(1)), 1);
            h.assertFalse(ordinary.getProxy().isActive(), "The embedded ordinary provider should use the multiblock connection");
        }

        void offered(ItemStack target, int count) {
            try {
                var recipes = large.getProxy().getCrafting().getCraftingFor(ae(target, 1), null, -1, h.getWorld());
                h.assertTrue(recipes.stream().anyMatch(p -> p.getOutputs()[0].getStackSize() == count),
                    "The provider should offer " + target.getDisplayName() + " x" + count);
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void record() {
            scene.capture();
            scene.frame(0, "overview", true);
            scene.buildStages(60, 65, p -> {
                if (p.x() < 2 || p.z() < 2) return 4;
                if (p.z() == 2) return 0;
                if (p.equals(ORDINARY) || p.equals(PREFAB) || p.equals(ACCESS)) return 3;
                return p.z() == 5 ? 2 : 1;
            }, "front", "body", "back", "providers", "networks");
            scene.frame(420, "template", true);
            scene.frame(500, "prefab", true);
            scene.frame(580, "access", true);
            scene.frame(660, "main", false);
            scene.frame(720, "parallel", false);
        }

        void multiply() {
            NBTTagCompound settings = large.getCopiedData(null);
            settings.setInteger("multiply", 4);
            h.assertTrue(large.pasteCopiedData(null, settings), "The multiplier setting should apply");
        }

        void multiplied() { offered(output, 4); }

        void request() {
            try {
                job = large.getProxy().getCrafting().beginCraftingJob(h.getWorld(), large.getProxy().getGrid(), SOURCE, ae(output, 8), null);
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void submit() {
            h.assertTrue(job.isDone(), "Waiting for AE crafting calculation");
            if (submitted) return;
            try {
                var plan = job.get();
                h.assertFalse(plan.isSimulation(), "Programming circuits should be craftable");
                h.assertNotNull(large.getProxy().getCrafting().submitJob(plan, null, null, false, SOURCE), "The circuit order should start");
                submitted = true;
            } catch (Exception e) { throw new AssertionError(e); }
        }

        long stored() {
            try {
                var stack = large.getProxy().getStorage().getItemInventory().extractItems(ae(output, Long.MAX_VALUE), Actionable.SIMULATE, SOURCE);
                return stack == null ? 0 : stack.getStackSize();
            } catch (Exception e) { throw new AssertionError(e); }
        }

        void finished() {
            h.assertEquals(8L, stored(), "The order should return eight programming circuits");
            try {
                for (ICraftingCPU cpu : large.getProxy().getCrafting().getCpus()) h.assertFalse(cpu.isBusy(), "The CPU should finish");
            } catch (Exception e) { throw new AssertionError(e); }
            h.assertTrue(ItemStack.areItemStacksEqual(mold, ordinary.getStackInSlot(0)), "The ordinary template should remain");
            h.assertTrue(ItemStack.areItemStacksEqual(remoteMold, remote.getStackInSlot(0)), "The remote template should remain");
        }

        void installChip() {
            large.setInventorySlotContents(1, new ItemStack(MyMod.chip));
        }

        void cleanup() {
            JsonObject frame = scene.frame(820, "chip", true);
            scene.patch(frame, CONTROL);
            NBTTagCompound settings = large.getCopiedData(null);
            settings.setBoolean("removeStorageCircuit", true);
            large.pasteCopiedData(null, settings);
            scene.frame(920, "cleanup", false);
        }

        void save() {
            scene.patch(scene.frame(1000, "done", true), CONTROL);
            scene.save(1100);
        }
    }
}
