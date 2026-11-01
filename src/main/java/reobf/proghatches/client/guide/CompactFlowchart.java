package reobf.proghatches.client.guide;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.opengl.GL11;

import com.hfstudio.guidenh.guide.color.ConstantColor;
import com.hfstudio.guidenh.guide.document.LytRect;
import com.hfstudio.guidenh.guide.document.block.LytBlock;
import com.hfstudio.guidenh.guide.document.block.LytItemImage;
import com.hfstudio.guidenh.guide.document.block.LytMermaidCanvas;
import com.hfstudio.guidenh.guide.document.block.shapes.FlowchartShapes;
import com.hfstudio.guidenh.guide.internal.mermaid.MermaidNodeShape;
import com.hfstudio.guidenh.guide.layout.LayoutContext;
import com.hfstudio.guidenh.guide.render.RenderContext;
import com.hfstudio.guidenh.guide.style.ResolvedTextStyle;
import com.hfstudio.guidenh.guide.style.TextAlignment;
import com.hfstudio.guidenh.guide.style.WhiteSpaceMode;

/** A positioned diagram with native item rendering, tooltips and fit-to-panel zoom. */
public final class CompactFlowchart extends LytMermaidCanvas<CompactFlowchart> {

    public record Node(String id, int x, int y, int size, boolean circle, int color, int textColor,
        String label, ItemStack item) {
        LytRect rect() { return new LytRect(x - size / 2, y - size / 2, size, size); }
        MermaidNodeShape shape() { return circle ? MermaidNodeShape.CIRCLE : MermaidNodeShape.SQUARE; }
    }
    public record Edge(String from, String to, String label, int color, boolean dashed, int labelX, int labelY) {}

    private static final int BACKGROUND = 0xffedf0f4;
    private final Map<String, Node> nodes;
    private final List<Edge> edges;
    private final int canvasWidth, canvasHeight;
    private boolean laidOut;

    CompactFlowchart(Map<String, Node> nodes, List<Edge> edges, int width, int height, int canvasWidth, int canvasHeight) {
        this.nodes = new LinkedHashMap<>(nodes);
        this.edges = List.copyOf(edges);
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        setPreferredSize(width, height);
        Map<String, LytBlock> icons = new LinkedHashMap<>();
        for (Node node : nodes.values()) if (node.item() != null) icons.put(node.id(), new LytItemImage(node.item()));
        initNodeContentBlocks(icons);
    }

    @Override protected void onLayoutMoved(int dx, int dy) {}
    @Override protected int canvasPadding() { return 6; }
    @Override protected int contentWidth() { return canvasWidth; }
    @Override protected int contentHeight() { return canvasHeight; }
    @Override protected int contentOriginX() { return 0; }
    @Override protected int contentOriginY() { return 0; }
    @Override protected boolean diagramReady() { return laidOut; }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        int w = Math.max(1, Math.min(availableWidth, Math.round(preferredWidth * context.getVisualScale())));
        int h = Math.max(24, Math.round(preferredHeight * w / (float) preferredWidth));
        Minecraft mc = Minecraft.getMinecraft();
        int maxHeight = Math.max(80, Math.round(new ScaledResolution(mc, mc.displayWidth, mc.displayHeight).getScaledHeight() * .6f));
        if (h > maxHeight) {
            w = Math.max(1, Math.round(w * maxHeight / (float) h));
            h = maxHeight;
        }
        for (var icon : nodeContentBlocks.values()) icon.layout(context, 0, 0, 16);
        float fit = Math.min(Math.max(1, w - 12) / (float) canvasWidth, Math.max(1, h - 12) / (float) canvasHeight);
        if (!laidOut || bounds.width() != w || bounds.height() != h) {
            snapTo(Math.round((w - 12 - canvasWidth * fit) / 2), Math.round((h - 12 - canvasHeight * fit) / 2), fit);
        }
        laidOut = true;
        return new LytRect(x, y, w, h);
    }

    @Override
    public void resetView() {
        LytRect vp = getInnerViewport();
        float fit = Math.min(vp.width() / (float) canvasWidth, vp.height() / (float) canvasHeight);
        snapTo(Math.round((vp.width() - canvasWidth * fit) / 2), Math.round((vp.height() - canvasHeight * fit) / 2), fit);
    }

    @Override
    public boolean scroll(int x, int y, int wheel) {
        LytRect vp = getInnerViewport();
        if (wheel == 0 || !vp.contains(x, y)) return false;
        float old = getRawZoom();
        float fit = Math.min(vp.width() / (float) canvasWidth, vp.height() / (float) canvasHeight);
        float next = Math.max(Math.min(.1f, fit / 4), Math.min(3f, old * (wheel > 0 ? 1.1f : 1 / 1.1f)));
        float anchorX = (x - vp.x() - getRawOffsetX()) / old;
        float anchorY = (y - vp.y() - getRawOffsetY()) / old;
        snapTo(Math.round(x - vp.x() - anchorX * next), Math.round(y - vp.y() - anchorY * next), next);
        clampOffsets();
        return true;
    }

    @Override
    public boolean mouseClicked(com.hfstudio.guidenh.guide.ui.GuideUiHost host, int x, int y, int button, boolean twice) {
        if (button == 0 && twice && bounds.contains(x, y)) {
            resetView();
            return true;
        }
        return super.mouseClicked(host, x, y, button, twice);
    }

    @Override
    protected void renderPanel(RenderContext context) {
        context.fillRect(bounds, BACKGROUND);
        context.drawBorder(bounds, 0xffb0b6bd, 1);
    }

    @Override
    protected void renderDiagram(RenderContext context, int baseX, int baseY, float zoom) {
        for (Edge edge : edges) drawEdge(context, baseX, baseY, zoom, edge);
        for (Node node : nodes.values()) {
            LytRect r = node.rect();
            LytRect scaled = new LytRect(scaled(baseX, r.x(), zoom), scaled(baseY, r.y(), zoom),
                Math.round(r.width() * zoom), Math.round(r.height() * zoom));
            FlowchartShapes.render(context, scaled, node.shape(), node.color(), 0xff30363b);
            LytRect inset = new LytRect(scaled.x() + 2, scaled.y() + 2, Math.max(1, scaled.width() - 4),
                Math.max(1, scaled.height() - 4));
            FlowchartShapes.render(context, inset, node.shape(), node.color(), 0xffc4cbd0);
            LytBlock icon = nodeContentBlocks.get(node.id());
            String[] lines = node.label().split("\\|");
            float textScale = Math.min(1, Math.max(.65f, .9f * zoom));
            for (String line : lines) {
                int width = context.getStringWidth(line, style(1, node.textColor()));
                if (width > 0) textScale = Math.min(textScale, (scaled.width() - 10f * zoom) / width);
            }
            int lineHeight = Math.max(1, Math.round(11 * textScale));
            int contentHeight = lines.length * lineHeight + (icon == null ? 0 : Math.round(20 * zoom));
            int top = scaled.y() + (scaled.height() - contentHeight) / 2;
            if (icon != null) {
                int iw = Math.max(1, Math.round(16 * zoom));
                GL11.glPushMatrix();
                try {
                    GL11.glTranslatef(scaled.x() + (scaled.width() - iw) / 2f, top, 0);
                    GL11.glScalef(zoom, zoom, 1);
                    context.renderItem(node.item(), 0, 0);
                } finally {
                    GL11.glPopMatrix();
                }
                top += Math.round(20 * zoom);
            }
            ResolvedTextStyle textStyle = style(textScale, node.textColor());
            for (String line : lines) {
                int tw = context.getStringWidth(line, textStyle);
                context.drawText(line, scaled.x() + (scaled.width() - tw) / 2, top, textStyle);
                top += lineHeight;
            }
        }
    }

    private void drawEdge(RenderContext ctx, int bx, int by, float zoom, Edge edge) {
        Node from = nodes.get(edge.from()), to = nodes.get(edge.to());
        var start = FlowchartShapes.edgeIntersect(from.rect(), from.shape(), to.x(), to.y());
        var end = FlowchartShapes.edgeIntersect(to.rect(), to.shape(), from.x(), from.y());
        float sx = bx + start.getX() * zoom, sy = by + start.getY() * zoom;
        float ex = bx + end.getX() * zoom, ey = by + end.getY() * zoom;
        double distance = Math.hypot(ex - sx, ey - sy);
        if (distance < 1) return;
        float ux = (float) ((ex - sx) / distance), uy = (float) ((ey - sy) / distance);
        float arrow = Math.max(2, 5 * zoom), thickness = Math.max(1, 1.5f * zoom);
        float stop = Math.max(0, (float) distance - arrow);
        if (edge.dashed()) {
            float dash = Math.max(2, 4 * zoom);
            for (float d = 0; d < stop; d += dash * 1.8f) {
                float e = Math.min(stop, d + dash);
                ctx.drawLine(sx + ux * d, sy + uy * d, sx + ux * e, sy + uy * e, thickness, edge.color());
            }
        } else ctx.drawLine(sx, sy, ex - ux * arrow, ey - uy * arrow, thickness, edge.color());
        ctx.fillTriangle(ex, ey, ex - ux * arrow - uy * arrow / 2, ey - uy * arrow + ux * arrow / 2,
            ex - ux * arrow + uy * arrow / 2, ey - uy * arrow - ux * arrow / 2, edge.color());
        if (!edge.label().isEmpty()) {
            ResolvedTextStyle text = style(.8f * (zoom < 1 ? (float) Math.sqrt(zoom) : zoom), 0xff343a40);
            int w = ctx.getStringWidth(edge.label(), text), h = ctx.getLineHeight(text);
            int x = Math.round((sx + ex) / 2 + edge.labelX() * zoom - w / 2f);
            int y = Math.round((sy + ey) / 2 + edge.labelY() * zoom - h / 2f);
            ctx.fillRect(x - 1, y - 1, w + 2, h + 2, BACKGROUND);
            ctx.drawText(edge.label(), x, y, text);
        }
    }

    private static ResolvedTextStyle style(float scale, int color) {
        return new ResolvedTextStyle(scale, false, false, false, false, false, false, false, null,
            new ConstantColor(color), WhiteSpaceMode.NORMAL, TextAlignment.LEFT, false, null, false);
    }

    @Override
    protected NodeHit pickNodeHit(int x, int y) {
        LytRect vp = getInnerViewport();
        float zoom = getActiveZoom();
        if (zoom <= 0 || !vp.contains(x, y)) return null;
        float localX = (x - vp.x() - getVisualOffsetX()) / zoom;
        float localY = (y - vp.y() - getVisualOffsetY()) / zoom;
        for (Node node : nodes.values()) {
            LytBlock icon = nodeContentBlocks.get(node.id());
            if (icon == null || !node.rect().contains((int) localX, (int) localY)) continue;
            if (node.circle() && Math.hypot(localX - node.x(), localY - node.y()) > node.size() / 2f) continue;
            return new NodeHit(icon, null, 8, 8);
        }
        return null;
    }
}
