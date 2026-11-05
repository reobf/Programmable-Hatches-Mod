package reobf.proghatches.client.guide;

import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;

public final class LargeProviderGuiAdapter implements Mui2PreviewAdapter {

    @Override
    public boolean accepts(Object model) {
        return model instanceof LargeProgrammingCircuitProvider;
    }

    @Override
    public void fillSyncedState(Object model) {
        LargeProgrammingCircuitProvider machine = (LargeProgrammingCircuitProvider) model;
        machine.clearHatches();
        machine.mMachine = machine.checkMachine(machine.getBaseMetaTileEntity(), machine.getStackInSlot(1));
        machine.mEUt = -(int) machine.getConsumption();
    }

    @Override
    public void initialize(Object model, PanelSyncManager sync) {
        if (sync.findSyncHandlerNullable("startupCheck", 0) instanceof IntSyncValue value)
            value.setIntValue(0, false, false);
    }

    @Override
    public String popupOpenedBy(String target) {
        return target.equals("lpcp_open_parallel") ? "lpcp_parallel" : null;
    }
}
