package reobf.proghatches.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiData;
import com.cleanroommc.modularui.factory.ItemGuiFactory;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.item.patterngen.PatternGeneratorPanel;
import reobf.proghatches.item.patterngen.PatternGeneratorSettings;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

/**
 * Generates AE2 processing patterns from the recipe map of a GT machine item.
 * <p>
 * Blank patterns are not consumed. Circuits and NC items are encoded as PH virtual items so a
 * Programming Circuit Provider can supply them.
 */
public class ItemPatternGenerator extends Item implements IGuiHolder<GuiData> {

    public ItemPatternGenerator() {
        setMaxStackSize(1);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && player instanceof EntityPlayerMP) {
            ItemGuiFactory.INSTANCE.open((EntityPlayerMP) player);
        }
        return stack;
    }

    @Override
    public ModularPanel buildUI(GuiData data, PanelSyncManager sync, UISettings settings) {
        ItemStack tool = data.getMainHandItem();
        PatternGeneratorSettings config = PatternGeneratorSettings.load(tool);
        Runnable persist = () -> {
            ItemStack target = tool;
            if (data.getPlayer() instanceof EntityPlayerMP player) {
                ItemStack held = player.getHeldItem();
                if (held != null && held.getItem() == MyMod.pattern_generator) target = held;
                player.inventory.markDirty();
                if (player.inventoryContainer != null) player.inventoryContainer.detectAndSendChanges();
            }
            config.save(target);
        };
        return new PatternGeneratorPanel(data, sync, tool, config, persist).build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List lines, boolean advanced) {
        lines.add(LangManager.translateToLocal("item.pattern_generator.name.tooltip.0"));
        lines.add(LangManager.translateToLocal("item.pattern_generator.name.tooltip.1"));
        lines.add(LangManager.translateToLocal("item.pattern_generator.name.tooltip.2"));
        lines.add(LangManager.translateToLocal("item.pattern_generator.name.tooltip.3"));
    }
}
