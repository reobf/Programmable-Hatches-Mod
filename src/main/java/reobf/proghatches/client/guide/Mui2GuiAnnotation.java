package reobf.proghatches.client.guide;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.hfstudio.guidenh.guide.document.LytRect;
import com.hfstudio.guidenh.guide.render.RenderContext;
import com.hfstudio.guidenh.guide.render.VanillaRenderContext;
import com.hfstudio.guidenh.guide.scene.CameraSettings;
import com.hfstudio.guidenh.guide.scene.LytGuidebookScene;
import com.hfstudio.guidenh.guide.scene.annotation.OverlayAnnotation;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import reobf.proghatches.client.guide.Mui2GuiPreview.Click;
import reobf.proghatches.client.guide.Mui2GuiPreview.View;
import reobf.proghatches.main.MyMod;

public final class Mui2GuiAnnotation extends OverlayAnnotation {

    private final LytGuidebookScene scene;
    private final int x, y, z, from, to;
    private final float scale;
    private final boolean showMain;
    private final net.minecraftforge.common.util.ForgeDirection partSide;
    private final List<Click> clicks;
    private final GuiContext tooltipContext = new GuiContext();
    private int lastTick = -1;
    private Mui2GuiPreview preview;
    private View view;
    private TileEntity boundTile;
    private NBTTagCompound boundState;
    private boolean failed;

    Mui2GuiAnnotation(LytGuidebookScene scene, int x, int y, int z, int from, int to, float scale, boolean showMain, List<Click> clicks, net.minecraftforge.common.util.ForgeDirection partSide) {
        this.scene = scene;
        this.partSide = partSide;
        this.x = x;
        this.y = y;
        this.z = z;
        this.from = from;
        this.to = to;
        this.scale = scale;
        this.showMain = showMain;
        this.clicks = clicks;
    }

    private boolean visible() {
        int tick = scene.getPonderCurrentTickForExport();
        return tick >= from && tick < to;
    }

    public boolean hasFailed() {
        return failed;
    }

    @Override
    public LytRect getBoundingRect(CameraSettings camera, LytRect viewport) {
        if (!visible() || preview == null || view == null || preview.bounds(showMain) == null)
            return new LytRect(viewport.x(), viewport.y(), 0, 0);
        float fit = Math.min(scale, Math.min((viewport.width() - 16f) / view.width(),
            (viewport.height() - 16f) / view.height()));
        int w = Math.max(1, Math.round(view.width() * fit));
        int h = Math.max(1, Math.round(view.height() * fit));
        return new LytRect(viewport.x() + viewport.width() - w - 8, viewport.y() + 8, w, h);
    }

    @Override
    public void render(CameraSettings camera, RenderContext context, LytRect viewport) {
        if (!visible()) {
            release();
            return;
        }
        if (failed) return;
        try {
            TileEntity tile = scene.getLevel().getTileEntity(x, y, z);
            if (!(tile instanceof IGregTechTileEntity) && !(tile instanceof IGuiHolder<?>)
                && !(partSide != net.minecraftforge.common.util.ForgeDirection.UNKNOWN && tile instanceof appeng.api.parts.IPartHost)) {
                release();
                return;
            }
            NBTTagCompound nbt = new NBTTagCompound();
            tile.writeToNBT(nbt);
            int tick = scene.getPonderCurrentTickForExport();
            if (preview == null || tile != boundTile || !nbt.equals(boundState) || tick < lastTick) {
                release();
                World world = scene.getLevel().getOrCreateFakeWorld();
                if (view == null) view = Mui2GuiPreview.measure(world, nbt, clicks, showMain, partSide);
                preview = new Mui2GuiPreview(world, nbt, partSide);
                boundTile = tile;
                boundState = nbt;
            }
            preview.advance(tick, clicks);
            lastTick = tick;
            LytRect rect = getBoundingRect(camera, viewport);
            if (rect.width() == 0) return;
            LytRect zero = context.toScreenRect(new LytRect(0, 0, 1, 1));
            float zoom = context instanceof VanillaRenderContext v ? v.getZoom() : 1;
            LytRect screenRect = rect;
            Minecraft mc = Minecraft.getMinecraft();
            ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
            int mx = Mouse.getX() * resolution.getScaledWidth() / mc.displayWidth;
            int my = resolution.getScaledHeight() - Mouse.getY() * resolution.getScaledHeight() / mc.displayHeight - 1;
            LytRect clip = context.currentScissor();
            if (clip == null) clip = screenRect;
            boolean hovered = mx >= screenRect.x() && mx < screenRect.right()
                && my >= screenRect.y() && my < screenRect.bottom()
                && mx >= clip.x() && mx < clip.right() && my >= clip.y() && my < clip.bottom();
            preview.hover(hovered ? view.x() + (mx - screenRect.x()) * view.width() / screenRect.width() : -10000,
                hovered ? view.y() + (my - screenRect.y()) * view.height() / screenRect.height() : -10000);
            drawPreview((rect.x() - zero.x()) / zoom, (rect.y() - zero.y()) / zoom,
                rect.width() / zoom, rect.height() / zoom, view, tick);
            if (hovered) drawTooltip(context, mx, my, resolution.getScaledWidth(), resolution.getScaledHeight());
        } catch (Exception e) {
            failed = true;
            release();
            MyMod.LOG.error("MUI2Gui preview failed at " + x + "," + y + "," + z, e);
        }
    }

    private void drawTooltip(RenderContext context, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        LytRect clip = new LytRect(0, 0, screenWidth, screenHeight);
        LytRect zero = context.toScreenRect(new LytRect(0, 0, 1, 1));
        float zoom = context instanceof VanillaRenderContext v ? v.getZoom() : 1;
        tooltipContext.updateScreenArea(Math.round(clip.width() / zoom), Math.round(clip.height() / zoom));
        tooltipContext.updateState(Math.round((mouseX - clip.x()) / zoom), Math.round((mouseY - clip.y()) / zoom), 0);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glPushMatrix();
        try {
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glTranslatef((clip.x() - zero.x()) / zoom, (clip.y() - zero.y()) / zoom, 0);
            preview.drawTooltip(tooltipContext);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private void drawPreview(float x, float y, float w, float h, View view, int tick) {
        int previousFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
        int previousMode = GL11.glGetInteger(GL11.GL_MATRIX_MODE);
        int modelDepth = GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH);
        int projectionDepth = GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH);
        Framebuffer buffer = null;
        boolean rendered = false;
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        try {
            int pw = view.width() * 3, ph = view.height() * 3;
            buffer = new Framebuffer(pw, ph, true);
            buffer.setFramebufferColor(0, 0, 0, 0);
            buffer.bindFramebuffer(true);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GL11.glClearColor(0, 0, 0, 0);
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(view.x(), view.x() + view.width(), view.y() + view.height(), view.y(), -1000, 1000);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();
            preview.render(tick, clicks);
            rendered = true;
        } finally {
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            while (GL11.glGetInteger(GL11.GL_MODELVIEW_STACK_DEPTH) > modelDepth) GL11.glPopMatrix();
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            while (GL11.glGetInteger(GL11.GL_PROJECTION_STACK_DEPTH) > projectionDepth) GL11.glPopMatrix();
            GL11.glMatrixMode(previousMode);
            if (!rendered && buffer != null) buffer.deleteFramebuffer();
            OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, previousFbo);
            GL11.glPopAttrib();
        }
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        try {
            if (buffer == null) return;
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glColor4f(1, 1, 1, 1);
            buffer.bindFramebufferTexture();
            GL11.glBegin(GL11.GL_QUADS);
            GL11.glTexCoord2f(0, 1); GL11.glVertex2f(x, y);
            GL11.glTexCoord2f(0, 0); GL11.glVertex2f(x, y + h);
            GL11.glTexCoord2f(1, 0); GL11.glVertex2f(x + w, y + h);
            GL11.glTexCoord2f(1, 1); GL11.glVertex2f(x + w, y);
            GL11.glEnd();
        } finally {
            if (buffer != null) buffer.deleteFramebuffer();
            OpenGlHelper.func_153171_g(OpenGlHelper.field_153198_e, previousFbo);
            GL11.glPopAttrib();
        }
    }

    private void release() {
        if (preview != null) preview.close();
        preview = null;
        boundTile = null;
        boundState = null;
        lastTick = -1;
    }
}
