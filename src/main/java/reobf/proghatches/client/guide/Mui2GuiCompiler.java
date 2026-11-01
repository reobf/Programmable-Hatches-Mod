package reobf.proghatches.client.guide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.document.LytErrorSink;
import com.hfstudio.guidenh.guide.scene.CameraSettings;
import com.hfstudio.guidenh.guide.scene.annotation.compiler.AnnotationTagCompiler;
import com.hfstudio.guidenh.guide.scene.element.SceneElementTagCompiler;
import com.hfstudio.guidenh.guide.scene.level.GuidebookLevel;
import com.hfstudio.guidenh.libs.mdast.mdx.model.MdxJsxElementFields;
import com.hfstudio.guidenh.libs.mdast.model.MdAstParent;

import reobf.proghatches.gt.metatileentity.CyclicPatternGuiPreview;
import reobf.proghatches.gt.metatileentity.CyclicPatternGuiPreview.Click;

public final class Mui2GuiCompiler implements SceneElementTagCompiler {

    @Override
    public Set<String> getTagNames() {
        return Set.of("MUI2Gui");
    }

    @Override
    public void compile(GuidebookLevel level, CameraSettings camera, PageCompiler compiler,
        LytErrorSink errors, MdxJsxElementFields el) {
        try {
            var scene = AnnotationTagCompiler.CURRENT_SCENE.get();
            if (scene == null) throw new IllegalArgumentException("MUI2Gui belongs inside GameScene");
            int x = integer(el, "x", "0"), y = integer(el, "y", "0"), z = integer(el, "z", "0");
            int from = integer(el, "from", "0"), to = integer(el, "to", "2147483647");
            String page = el.getAttributeString("page", "main");
            float scale = Float.parseFloat(el.getAttributeString("scale", "2"));
            String showMainText = el.getAttributeString("showMain", "true");
            if (!showMainText.equals("true") && !showMainText.equals("false"))
                throw new IllegalArgumentException("showMain must be true or false");
            boolean showMain = Boolean.parseBoolean(showMainText);
            CyclicPatternGuiPreview.pageIndex(page);
            if (from < 0 || to <= from || !Float.isFinite(scale) || scale <= 0 || scale > 8)
                throw new IllegalArgumentException("Require 0 <= from < to and 0 < scale <= 8");
            List<Click> clicks = new ArrayList<>();
            for (Object child : el.children()) collectClicks(child, clicks);
            if (clicks.size() > 32) throw new IllegalArgumentException("MUI2Gui supports up to 32 scripted clicks");
            int previous = from - 1;
            boolean popup = !page.equals("main");
            String activePage = page;
            for (Click click : clicks) {
                if (click.at() <= previous || click.at() < from || click.at() >= to)
                    throw new IllegalArgumentException("MUI2Click times must increase within [from, to)");
                CyclicPatternGuiPreview.validateTarget(click.target());
                if (click.target().equals("open_patterns")) popup = true;
                else if (!popup) throw new IllegalArgumentException("Open patterns before clicking a popup tab");
                if (click.target().equals("open_patterns") || click.target().equals("patterns_tab")) activePage = "patterns";
                else if (click.target().equals("cyclic_lanes_tab")) activePage = "lanes";
                else if (click.target().equals("cyclic_bind_tab")) activePage = "bind";
                else if (click.target().equals("cyclic_config_tab")) activePage = "config";
                else if (click.target().startsWith("cyclic_lane_") && !activePage.equals("lanes"))
                    throw new IllegalArgumentException("Lane clicks require the lanes page");
                else if (click.holdsDataStick() && !activePage.equals("bind"))
                    throw new IllegalArgumentException("Binding clicks require the binding page");
                previous = click.at();
            }
            scene.addAnnotation(new Mui2GuiAnnotation(scene, x, y, z, from, to, page, scale, showMain, List.copyOf(clicks)));
        } catch (IllegalArgumentException e) {
            errors.appendError(compiler, e.getMessage(), el);
        }
    }

    private static void collectClicks(Object node, List<Click> clicks) {
        if (node instanceof MdxJsxElementFields el) {
            if (!"MUI2Click".equals(el.name())) throw new IllegalArgumentException("Expected MUI2Click inside MUI2Gui");
            String target = el.getAttributeString("target", "");
            if (target.matches("cyclic_bind_[1-8]") && !(el.hasAttribute("x") && el.hasAttribute("y") && el.hasAttribute("z")))
                throw new IllegalArgumentException("Binding clicks require target x, y and z");
            clicks.add(new Click(integer(el, "at", "-1"), target,
                integer(el, "x", "0"), integer(el, "y", "0"), integer(el, "z", "0")));
        } else if (node instanceof MdAstParent<?> parent) {
            for (Object child : parent.children()) collectClicks(child, clicks);
        }
    }

    private static int integer(MdxJsxElementFields el, String key, String fallback) {
        return Integer.parseInt(el.getAttributeString(key, fallback));
    }
}
