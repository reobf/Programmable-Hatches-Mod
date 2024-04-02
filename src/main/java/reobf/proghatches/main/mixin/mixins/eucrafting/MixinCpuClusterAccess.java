package reobf.proghatches.main.mixin.mixins.eucrafting;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import appeng.me.cluster.implementations.CraftingCPUCluster;
import reobf.proghatches.eucrafting.TileCraftingMinimiumEUTile;
@Mixin(value = CraftingCPUCluster.class, remap = false, priority = 0)
public class MixinCpuClusterAccess {

	static{ TileCraftingMinimiumEUTile.Scope.setScope(MethodHandles.lookup());
	}
	
}
