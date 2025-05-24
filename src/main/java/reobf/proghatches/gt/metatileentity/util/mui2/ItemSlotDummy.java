package reobf.proghatches.gt.metatileentity.util.mui2;

import com.cleanroommc.modularui.ModularUI;
import com.cleanroommc.modularui.api.ITheme;
import com.cleanroommc.modularui.api.widget.IVanillaSlot;
import com.cleanroommc.modularui.api.widget.Interactable;
import com.cleanroommc.modularui.drawable.GuiDraw;
import com.cleanroommc.modularui.integration.nei.NEIIngredientProvider;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiContainerAccessor;
import com.cleanroommc.modularui.mixins.early.minecraft.GuiScreenAccessor;
import com.cleanroommc.modularui.screen.ClientScreenHandler;
import com.cleanroommc.modularui.screen.NEAAnimationHandler;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.theme.WidgetSlotTheme;
import com.cleanroommc.modularui.theme.WidgetTheme;
import com.cleanroommc.modularui.utils.GlStateManager;
import com.cleanroommc.modularui.utils.Platform;
import com.cleanroommc.modularui.utils.item.IItemHandlerModifiable;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.SyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;
import com.cleanroommc.modularui.widgets.slot.PhantomItemSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSlotDummy extends ItemSlot  {
@Override
protected void setSyncHandler(@Nullable SyncHandler syncHandler) {
	
	super.setSyncHandler(new ItemSlotSH(((ItemSlotSH)syncHandler).getSlot()){@Override
	public void detectAndSendChanges(boolean init) {
		
		
	}});
}
  
}
