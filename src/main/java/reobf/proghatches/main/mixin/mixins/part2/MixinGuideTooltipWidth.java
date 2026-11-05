package reobf.proghatches.main.mixin.mixins.part2;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.hfstudio.guidenh.guide.document.LytRect;

@Pseudo
@Mixin(targets = "com.hfstudio.guidenh.guide.internal.GuideScreen", remap = false)
public class MixinGuideTooltipWidth {

    @ModifyVariable(
        method = "drawHoveringTextAtAdjustedPosition(Ljava/util/List;IILnet/minecraft/client/gui/FontRenderer;Lcom/hfstudio/guidenh/guide/document/LytRect;)V",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0,
        require = 0)
    private List<String> ph$wrapLongTooltip(List<String> value, List<String> lines, int mouseX, int mouseY,
        FontRenderer font, LytRect bounds) {
        if (value == null || value.isEmpty() || font == null) return value;
        GuiScreen screen = (GuiScreen) (Object) this;
        int width = Math.min(screen.width, bounds.right()) - Math.max(0, bounds.x());
        int hardLimit = Math.max(1, width - 16);
        if (value.stream().noneMatch(line -> font.getStringWidth(line) > hardLimit)) return value;
        int wrapWidth = Math.min(hardLimit, Math.max(80, width / 2));
        List<String> wrapped = new ArrayList<>();
        for (String line : value) {
            if (font.getStringWidth(line) <= wrapWidth) wrapped.add(line);
            else wrapped.addAll(font.listFormattedStringToWidth(line, wrapWidth));
        }
        return wrapped;
    }
}
