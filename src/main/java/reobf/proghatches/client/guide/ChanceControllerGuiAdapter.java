package reobf.proghatches.client.guide;

import java.io.IOException;
import net.minecraft.network.PacketBuffer;
import com.cleanroommc.modularui.value.sync.IntSyncValue;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;
import io.netty.buffer.Unpooled;
import reobf.proghatches.ae.TileChanceController;
import reobf.proghatches.gt.metatileentity.chance.ChanceView;
import reobf.proghatches.gt.metatileentity.chance.ChanceViewNbt;
import reobf.proghatches.client.guide.Mui2GuiPreview.Click;

public final class ChanceControllerGuiAdapter implements Mui2PreviewAdapter {

    @Override
    public boolean accepts(Object model) {
        return model instanceof TileChanceController;
    }

    @Override
    public void initialize(Object model, PanelSyncManager sync) throws IOException {
        TileChanceController tile = (TileChanceController) model;
        ChanceView.Screen view = ChanceViewNbt.read(tile.guideSnapshot());
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        try {
            ChanceView.Screen.ADAPTER.serialize(buffer, view);
            sync.findSyncHandlerNullable("screen", 0).readOnClient(ValueSyncHandler.SYNC_VALUE, buffer);
        } finally {
            buffer.release();
        }
    }

    @Override
    public void afterClick(Object model, Click click, PanelSyncManager sync) {
        if (!click.target().equals("chance_apply")) return;
        int chosen = ((IntSyncValue) sync.findSyncHandlerNullable("chosen", 0)).getIntValue();
        ((TileChanceController) model).setMode(TileChanceController.RefillMode.values()[chosen]);
        ((IntSyncValue) sync.findSyncHandlerNullable("mode", 0)).setIntValue(chosen, false, false);
    }
}
