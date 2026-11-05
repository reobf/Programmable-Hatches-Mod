package reobf.proghatches.client.guide;

import java.io.IOException;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.network.PacketBuffer;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import io.netty.buffer.Unpooled;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.util.mui2.SyncHandlerAEFluid;
import reobf.proghatches.client.guide.Mui2GuiPreview.Click;

public class DualInputGuiAdapter implements Mui2PreviewAdapter {

    @Override
    public boolean accepts(Object mte) {
        return mte instanceof DualInputHatch;
    }

    @Override
    public void afterClick(Object model, Click click, PanelSyncManager sync) throws IOException {
        if (!click.target().equals("open_shared")) return;
        DualInputHatch hatch = (DualInputHatch) model;
        PanelSyncManager popup = sync.getModularSyncManager().getPanelSyncManager("shared_window");
        for (int i = 0; i < hatch.shared.fluidMEUpgrades; i++) {
            for (var panel : sync.getContainer().getScreen().getPanelManager().getOpenPanels()) {
                var mark = WidgetTree.findFirstWithNameNullable(panel, "shared_fluid_mark_" + i);
                if (mark instanceof FluidSlot slot)
                    ((FluidSlotSyncHandler) slot.getSyncHandler()).updateCacheFromSource(true);
            }
            var handler = (SyncHandlerAEFluid) popup.findSyncHandlerNullable("shared_fluid:" + i, 0);
            if (handler == null) continue;
            PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
            try {
                byte[] bytes = CompressedStreamTools.compress(hatch.guideFluidStock().getCompoundTag("i" + i));
                buffer.writeInt(bytes.length);
                buffer.writeBytes(bytes);
                handler.readOnClient(999, buffer);
            } finally {
                buffer.release();
            }
        }
    }

    @Override
    public String popupOpenedBy(String target) {
        if (target.equals("open_shared")) return "shared_panel";
        if (target.equals("open_patterns")) return "pattern_panel";
        return target.startsWith("open_buffer_") ? "buffer_panel_sync_" + target.substring("open_buffer_".length()) : null;
    }
}
