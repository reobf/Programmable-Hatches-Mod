package reobf.proghatches.ae;

import java.util.List;

import appeng.crafting.v2.resolvers.CraftableItemResolver.RequestAndPerCraftAmount;

/**
 * Duck interface stamped onto AE2's CraftFromPatternTask by MixinCircuitHoistStrip, exposing the two
 * protected members {@link CircuitHoistTally} needs: the final craft count (read AFTER the tree has
 * settled, so every refund is already reflected) and the child list the hoisted request is attached to
 * so the crafting-tree GUI shows it under the final pattern.
 */
public interface ICircuitHoistTask {

    long proghatches$totalCraftsDone();

    List<RequestAndPerCraftAmount> proghatches$childRequests();
}
