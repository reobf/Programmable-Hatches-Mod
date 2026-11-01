package reobf.proghatches.client.guide;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.item.ItemStack;

import com.hfstudio.guidenh.guide.compiler.PageCompiler;
import com.hfstudio.guidenh.guide.compiler.tags.BlockTagCompiler;
import com.hfstudio.guidenh.guide.document.block.LytBlockContainer;
import com.hfstudio.guidenh.guide.internal.item.GuideDisplayItemStacks;
import com.hfstudio.guidenh.libs.mdast.mdx.model.MdxJsxElementFields;
import com.hfstudio.guidenh.libs.mdast.model.MdAstParent;

public final class FlowchartCompiler extends BlockTagCompiler {

    @Override
    public Set<String> getTagNames() { return Set.of("Flowchart"); }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        try {
            var nodes = new LinkedHashMap<String, CompactFlowchart.Node>();
            var edges = new ArrayList<CompactFlowchart.Edge>();
            List<MdxJsxElementFields> children = new ArrayList<>();
            for (Object child : compiler.reparseBlockTagChildren(el)) collect(child, children);
            for (var child : children) {
                if ("FlowNode".equals(child.name())) {
                    String id = child.getAttributeString("id", "");
                    if (id.isEmpty() || nodes.containsKey(id)) throw new IllegalArgumentException("FlowNode needs a unique id");
                    String item = child.getAttributeString("item", "");
                    ItemStack stack = item.isEmpty() ? null : GuideDisplayItemStacks.resolveItemStack(item, "minecraft");
                    if (!item.isEmpty() && stack == null) throw new IllegalArgumentException("Unknown FlowNode item: " + item);
                    String shape = child.getAttributeString("shape", "circle");
                    if (!shape.equals("circle") && !shape.equals("rect")) throw new IllegalArgumentException("FlowNode shape: circle or rect");
                    nodes.put(id, new CompactFlowchart.Node(id, number(child, "x", 0), number(child, "y", 0),
                        number(child, "size", 72), shape.equals("circle"), color(child, "color", "#777f87"),
                        color(child, "textColor", "#ffffff"), child.getAttributeString("label", id), stack));
                } else if ("FlowEdge".equals(child.name())) {
                    edges.add(new CompactFlowchart.Edge(child.getAttributeString("from", ""), child.getAttributeString("to", ""),
                        child.getAttributeString("label", ""), color(child, "color", "#7f8994"),
                        Boolean.parseBoolean(child.getAttributeString("dashed", "false")),
                        number(child, "labelX", 0), number(child, "labelY", -6)));
                } else throw new IllegalArgumentException("Expected FlowNode or FlowEdge inside Flowchart");
            }
            if (nodes.isEmpty() || nodes.size() > 64 || edges.size() > 128)
                throw new IllegalArgumentException("Flowchart supports 1–64 nodes and up to 128 edges");
            for (var edge : edges) if (!nodes.containsKey(edge.from()) || !nodes.containsKey(edge.to()))
                throw new IllegalArgumentException("FlowEdge names an unknown node");
            int w = number(el, "width", 340), h = number(el, "height", 310);
            int cw = number(el, "canvasWidth", 340), ch = number(el, "canvasHeight", 300);
            if (w < 32 || h < 32 || cw < 32 || ch < 32 || Math.max(Math.max(w, h), Math.max(cw, ch)) > 4096)
                throw new IllegalArgumentException("Flowchart dimensions must be between 32 and 4096");
            for (var node : nodes.values()) if (node.size() < 16 || node.x() - node.size() / 2 < 0
                || node.y() - node.size() / 2 < 0 || node.x() + node.size() / 2 > cw
                || node.y() + node.size() / 2 > ch) throw new IllegalArgumentException("FlowNode outside canvas: " + node.id());
            parent.append(new CompactFlowchart(nodes, edges, w, h, cw, ch));
        } catch (IllegalArgumentException e) {
            parent.appendError(compiler, e.getMessage(), el);
        }
    }

    private static void collect(Object node, List<MdxJsxElementFields> children) {
        if (node instanceof MdxJsxElementFields el) children.add(el);
        else if (node instanceof MdAstParent<?> parent) for (Object child : parent.children()) collect(child, children);
    }

    private static int number(MdxJsxElementFields el, String key, int fallback) {
        return Integer.parseInt(el.getAttributeString(key, String.valueOf(fallback)));
    }

    private static int color(MdxJsxElementFields el, String key, String fallback) {
        String value = el.getAttributeString(key, fallback);
        if (!value.matches("#[0-9a-fA-F]{6}")) throw new IllegalArgumentException(key + " requires #RRGGBB");
        return 0xff000000 | Integer.parseInt(value.substring(1), 16);
    }
}
