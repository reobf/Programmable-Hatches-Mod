package reobf.proghatches.ae;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;

public class NodeAdditionCallback implements INodeAdditionCallback {

    public NodeAdditionCallback(final IGrid g) {
        grid = g;
    }

    IGrid grid;

    @Override
    public void onUpdateTick() {

    }

    @Override
    public void removeNode(IGridNode gridNode, IGridHost machine) {
        grid.getMachines(PartSubnetExciter.class)
            .forEach(s -> ((PartSubnetExciter) s.getMachine()).check(gridNode));

    }

    @Override
    public void onSplit(IGridStorage destinationStorage) {

    }

    @Override
    public void onJoin(IGridStorage sourceStorage) {

    }

    @Override
    public void populateGridStorage(IGridStorage destinationStorage) {

    }

    @Override
    public void addNode(IGridNode gridNode, IGridHost machine) {
        grid.getMachines(PartSubnetExciter.class)
            .forEach(s -> ((PartSubnetExciter) s.getMachine()).gridChanged());

    }

}
