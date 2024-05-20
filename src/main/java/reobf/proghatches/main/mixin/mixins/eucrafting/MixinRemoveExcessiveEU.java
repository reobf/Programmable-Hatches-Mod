package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.networking.crafting.ICraftingCPU;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.MECraftingInventory;
import appeng.crafting.v2.CraftingJobV2;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.util.item.AEItemStack;
import li.cil.oc.api.network.SimpleComponent.SkipInjection;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.SISOPatternDetail;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.WrappedPatternDetail;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.mixin.MixinCallback;
import reobf.proghatches.util.ProghatchesUtil;

@Mixin(CraftingJobV2.class)
public class MixinRemoveExcessiveEU {

	private static Field f;
	static {

		try {
			f = CraftingCPUCluster.class.getDeclaredField("tasks");
			f.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	private boolean removeAll = true;

	@Inject(method = "startCrafting", at = { @At("RETURN") }, remap = false)
	public void startCrafting(MECraftingInventory storage, ICraftingCPU rawCluster, BaseActionSource src,
			CallbackInfo c) {
		CraftingCPUCluster cluster = (CraftingCPUCluster) rawCluster;

		if (removeAll) {
			try {
				@SuppressWarnings("unchecked")
				Map<ICraftingPatternDetails, Object> tasks = (Map<ICraftingPatternDetails, Object>) f.get(cluster);

				tasks.forEach((a, b) -> {

					if (a instanceof CircuitProviderPatternDetial) {
						CircuitProviderPatternDetial w = (CircuitProviderPatternDetial) a;
						MyMod.LOG.info("Removing EU Source Pattern Request: " + w.out.getTagCompound());
						if (w.out.getItem() == MyMod.eu_token) {
							MixinCallback.setter.accept(b, 0l);
						}
					}
					if (a instanceof SISOPatternDetail) {
						SISOPatternDetail w = (SISOPatternDetail) a;
						MyMod.LOG.info("Removing EU Interface Binding Pattern Request: " + w.out.getTagCompound());
						if (w.in.getItem() == MyMod.eu_token && w.out.getItem() == MyMod.eu_token) {
							MixinCallback.setter.accept(b, 0l);
						}
					}

				});

				return;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		try {
			@SuppressWarnings("unchecked")
			Map<ICraftingPatternDetails, Object> tasks = (Map<ICraftingPatternDetails, Object>) f.get(cluster);
			Map<UUID, Long> num = new HashMap<>();

			tasks.forEach((a, b) -> {
				if (a instanceof WrappedPatternDetail) {
					WrappedPatternDetail pattern = (WrappedPatternDetail) a;
					UUID id = ProghatchesUtil.deser(pattern.extraIn0.getTagCompound(), "EUFI");
					num.put(id, pattern.extraIn.getStackSize());
				}
			});

			Map<SISOPatternDetail, Long> tokill = new HashMap<>();
			tasks.entrySet().forEach((d) -> {
				if (d.getKey() instanceof SISOPatternDetail == false)
					return;
				UUID id = ProghatchesUtil.deser(((SISOPatternDetail) d.getKey()).out.getTagCompound(), "EUFI");
				long need = num.getOrDefault(id, 0l);
				long actual = MixinCallback.getter.apply(d.getValue());
				long excessive = actual - need;
				if (excessive <= 0)
					return;
				tokill.merge((SISOPatternDetail) d.getKey(), excessive, Long::sum);
				return;
			});

			// System.out.println(tokill);
			HashMap<IAEItemStack, Long> killnum = new HashMap();
			tokill.forEach((a, b) -> {
				tasks.computeIfPresent(a,

						(x, y) -> {
							MyMod.LOG.info("Removing Excessive EU Request:" + b);
							// MixinCallback.setter.accept(y,MixinCallback.getter.apply(y)-b);
							MixinCallback.setter.accept(y, 0l);
							// killnum[0]+=b;

							IAEItemStack is = a.i[0].copy().setStackSize(1);

							killnum.merge(is, a.i[0].getStackSize() * b, Long::sum);

							return y;
						});

			});
			// System.out.println(killnum);
			// HashSet<> =
			// tokill.keySet().stream().map(S->S.i[0]).collect(Collectors.toCollection(HashSet::new));
			tasks.forEach((a, b) -> {

				if (a instanceof CircuitProviderPatternDetial) {
					CircuitProviderPatternDetial w = (CircuitProviderPatternDetial) a;

					if (killnum.containsKey(AEItemStack.create(w.out))) {

						// MixinCallback.setter.accept(b,MixinCallback.getter.apply(b)-killnum.get(AEItemStack.create(w.out)));
						MixinCallback.setter.accept(b, 0l);

					}

				}

			});

			/*
			 * tasks.forEach((s,b)->{
			 * 
			 * System.out.println(s+" "+MixinCallback.getter.apply(b));
			 * 
			 * 
			 * });
			 */

			//
			// System.out.println(killnum.get(AEItemStack.create(w.out)));

		} catch (Exception e) {
			e.printStackTrace();
		}

		// MixinCallback.getter.apply(cluster);

	}

}
