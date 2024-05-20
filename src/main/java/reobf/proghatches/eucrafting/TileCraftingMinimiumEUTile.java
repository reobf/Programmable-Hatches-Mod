package reobf.proghatches.eucrafting;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import static reobf.proghatches.eucrafting.TileCraftingMinimiumEUTile.Scope.*;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.tile.TileEvent;
import appeng.tile.crafting.TileCraftingTile;
import appeng.tile.events.TileEventType;

public class TileCraftingMinimiumEUTile extends TileCraftingTile {
	public static class Scope {
		public static void setScope(Lookup lup) {
			scope = lup;
			try {
				isComplete = scope.findGetter(CraftingCPUCluster.class, "isComplete", boolean.class);
			} catch (Exception e) {

				throw new AssertionError(e);
			}
		};

		public static Lookup scope;
		public static MethodHandle isComplete;
	}

	public CraftingCPUCluster getCraftingCluster() {
		return (CraftingCPUCluster) this.getCluster();
	}

	@TileEvent(TileEventType.TICK)
	public void tick() {
		try {
			CraftingCPUCluster cluster = getCraftingCluster();
			if (cluster == null)
				return;
			if (cluster.getRemainingOperations() == 0)
				return;

			isComplete.invoke(cluster);

		} catch (Throwable e) {
			e.printStackTrace();
		}

	}

}
