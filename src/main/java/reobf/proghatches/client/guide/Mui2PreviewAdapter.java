package reobf.proghatches.client.guide;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import reobf.proghatches.client.guide.Mui2GuiPreview.Click;

/** The parts of a machine's GUI that need its server, supplied for the guide preview. */
public interface Mui2PreviewAdapter {

    List<Mui2PreviewAdapter> ADAPTERS = new CopyOnWriteArrayList<>();
    Mui2PreviewAdapter NONE = mte -> true;

    static Mui2PreviewAdapter of(Object mte) {
        for (Mui2PreviewAdapter adapter : ADAPTERS) if (adapter.accepts(mte)) return adapter;
        return NONE;
    }

    boolean accepts(Object mte);

    default void fillSyncedState(Object mte) {}

    default void initialize(Object model, PanelSyncManager sync) throws IOException {}

    default void afterClick(Object model, Click click, PanelSyncManager sync) throws IOException {}

    default String popupOpenedBy(String target) {
        return null;
    }

    default void pressOnServer(Object mte, Click click, EntityPlayer player) {}

    default ItemStack heldItem(Click click) {
        return null;
    }
}
