package reobf.proghatches.gt.metatileentity;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.GuiContainerWrapper;
import com.cleanroommc.modularui.screen.ModularContainer;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.GuiContext;
import com.cleanroommc.modularui.value.sync.ModularSyncManager;
import com.cleanroommc.modularui.value.sync.PanelSyncHandler;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widget.sizer.Area;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.PageButton;
import com.cleanroommc.modularui.widgets.PagedWidget;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.enums.ItemList;
import com.cleanroommc.modularui.drawable.GuiDraw;
import io.netty.buffer.Unpooled;
import reobf.proghatches.main.MyMod;

/** A complete, isolated MUI2 container with passive spectator hover and scripted actions. */
@SideOnly(Side.CLIENT)
public final class CyclicPatternGuiPreview implements AutoCloseable {

    public record Click(int at, String target, int x, int y, int z) {
        public boolean holdsDataStick() { return target.matches("cyclic_bind_[1-8]"); }
        public int leadTicks() { return holdsDataStick() ? 60 : 24; }
    }
    public record View(int x, int y, int width, int height) {}

    private final CyclicPatternDualInputHatch hatch;
    private final EntityOtherPlayerMP player;
    private final ModularSyncManager sync;
    private final ModularScreen screen;
    private final PanelSyncHandler popup;
    private final int width, height;
    private int applied;
    private long lastUpdate;
    private boolean closed;

    public CyclicPatternGuiPreview(World world, NBTTagCompound nbt, String page) throws IOException {
        BaseMetaTileEntity tile = new BaseMetaTileEntity();
        tile.setWorldObj(world);
        tile.readFromNBT((NBTTagCompound) nbt.copy());
        if (!(tile.getMetaTileEntity() instanceof CyclicPatternDualInputHatch cyclic))
            throw new IllegalArgumentException("MUI2Gui requires a cyclic pattern hatch");
        hatch = cyclic;
        refreshClientState();

        player = new EntityOtherPlayerMP(world,
            new GameProfile(UUID.fromString("ed6271ac-c706-46c0-970f-948464187a3d"), "GuidePreview"));
        PosGuiData data = new PosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord) {
            @Override
            public TileEntity getTileEntity() { return tile; }
        };
        sync = new ModularSyncManager(true);
        PanelSyncManager main = new PanelSyncManager(sync, true);
        UISettings settings = new UISettings();
        ModularPanel root = hatch.buildUI(data, main, settings).pos(6, 6);
        popup = (PanelSyncHandler) main.findPanelHandlerNullable("pattern_panel");
        if (popup == null) throw new IllegalStateException("Pattern popup handler missing");
        WidgetTree.collectSyncValues(main, root);
        ModularContainer container = new ModularContainer();
        container.construct(player, sync, settings, root.getName(), data);
        width = hatch.getGUIWidth() + 124;
        height = Math.max(hatch.getGUIHeight() + 12, 184);
        screen = new ModularScreen(MyMod.MODID, root).drawDarkBackground(false);
        screen.getContext().setSettings(settings);
        if (settings.getTheme() != null) screen.useTheme(settings.getTheme());
        // GuiContainer.initGui would replace the real player's open container.
        new GuiContainerWrapper(container, screen) {
            {
                mc = Minecraft.getMinecraft();
                fontRendererObj = mc.fontRenderer;
                width = CyclicPatternGuiPreview.this.width;
                height = CyclicPatternGuiPreview.this.height;
            }
        };
        screen.onResize(width, height);
        sync.onOpen();
        if (!page.equals("main")) {
            click(new Click(0, "open_patterns", 0, 0, 0));
            for (var child : patternPanel().getChildren())
                if (child instanceof PagedWidget<?> pages) pages.setPage(pageIndex(page));
        }
        screen.getContext().updateState(-10000, -10000, 0);
        screen.onUpdate();
        screen.onFrameUpdate();
    }

    public static int pageIndex(String page) {
        return switch (page) {
            case "main" -> -1;
            case "patterns" -> 0;
            case "lanes" -> 2;
            case "bind" -> 3;
            case "config" -> 4;
            default -> throw new IllegalArgumentException("MUI2Gui page: main, patterns, lanes, bind or config");
        };
    }

    public static void validateTarget(String target) {
        if (target.matches("cyclic_lane_([0-9]|[12][0-9]|3[0-5])") || target.matches("cyclic_bind_[1-8]")) return;
        switch (target) {
            case "open_patterns", "patterns_tab", "cyclic_lanes_tab", "cyclic_bind_tab", "cyclic_config_tab" -> {}
            default -> throw new IllegalArgumentException("Unknown MUI2Click target: " + target);
        }
    }

    private ModularPanel patternPanel() {
        return screen.getPanelManager().getOpenPanel("pattern_window");
    }

    private IWidget target(String name) {
        ModularPanel panel = name.equals("open_patterns") ? screen.getMainPanel() : patternPanel();
        return panel == null ? null : WidgetTree.findFirstWithNameNullable(panel, name);
    }

    private void click(Click click) throws IOException {
        String name = click.target();
        validateTarget(name);
        IWidget widget = target(name);
        if (widget instanceof ButtonWidget<?> button && name.equals("open_patterns")) {
            button.clickSound(() -> {});
            button.onMousePressed(0);
            if (patternPanel() == null) {
                PacketBuffer empty = new PacketBuffer(Unpooled.buffer());
                try {
                    // Complete the open locally; this container has no network session.
                    popup.readOnClient(PanelSyncHandler.SYNC_OPEN, empty);
                } finally {
                    empty.release();
                }
            }
        } else if (widget instanceof ButtonWidget<?> button) {
            button.clickSound(() -> {});
            button.onMousePressed(0);
            if (click.holdsDataStick()) {
                ItemStack stick = ItemList.Tool_DataStick.get(1);
                NBTTagCompound link = new NBTTagCompound();
                link.setString("type", "ProgHatchesDualInput");
                link.setInteger("x", click.x());
                link.setInteger("y", click.y());
                link.setInteger("z", click.z());
                link.setInteger("dim", player.worldObj.provider.dimensionId);
                stick.setTagCompound(link);
                player.inventory.setItemStack(stick);
                try {
                    String error = hatch.bindLane(Integer.parseInt(name.substring("cyclic_bind_".length())), player);
                    if (error != null) throw new IllegalStateException("Preview binding failed: " + error);
                } finally {
                    player.inventory.setItemStack(null);
                }
            } else if (name.startsWith("cyclic_lane_")) {
                hatch.stepPatternLane(Integer.parseInt(name.substring("cyclic_lane_".length())), 1);
            }
            refreshClientState();
        } else if (widget instanceof PageButton tab) {
            tab.getController().setPage(tab.getIndex());
        } else {
            throw new IllegalStateException("MUI2Click widget unavailable: " + name);
        }
        screen.onUpdate();
        screen.onFrameUpdate();
    }

    private void refreshClientState() {
        StringBuilder lanes = new StringBuilder();
        for (int lane : hatch.patternLane) lanes.append((char) ('0' + lane));
        hatch.cLanes = lanes.toString();
        for (int lane = 1; lane <= CyclicPatternDualInputHatch.LANES; lane++) {
            hatch.cLaneInfo[lane] = hatch.laneInfo(lane);
            hatch.cLaneIcon[lane] = hatch.laneIcon(lane);
        }
    }

    public void advance(int tick, List<Click> clicks) throws IOException {
        while (applied < clicks.size() && clicks.get(applied).at() <= tick) click(clicks.get(applied++));
    }

    public View view(boolean showMain) {
        if (showMain) return new View(0, 0, width, height);
        ModularPanel panel = patternPanel();
        if (panel == null) return new View(0, 0, 0, 0);
        Area a = panel.getArea();
        return new View(a.x, a.y - 6, a.w() + 38, a.h() + 12);
    }

    public void hover(int mouseX, int mouseY) {
        screen.getContext().updateState(mouseX, mouseY, 0);
        long now = Minecraft.getSystemTime() / 50;
        if (now != lastUpdate) {
            lastUpdate = now;
            screen.getContext().tick();
            screen.onUpdate();
        }
        screen.onFrameUpdate();
    }

    public void render(int tick, List<Click> clicks) {
        if (closed) return;
        screen.drawScreen();
        drawPointer(tick, clicks);
    }

    public void drawTooltip(GuiContext context) {
        IWidget widget = screen.getContext().getTopHovered();
        if (!(widget instanceof ITooltip<?> provider)) return;
        RichTooltip tooltip = provider.getTooltip();
        if (tooltip == null || !widget.isHoveringFor(tooltip.getShowUpTimer())) return;
        tooltip.pos(RichTooltip.Pos.NEXT_TO_MOUSE);
        tooltip.draw(context, widget instanceof ItemSlot slot ? slot.getSlot().getStack() : null);
    }

    private void drawPointer(int tick, List<Click> clicks) {
        Click focus = null;
        for (Click click : clicks) if (tick >= click.at() - click.leadTicks() && tick < click.at() + 16) {
            focus = click;
            break;
        }
        if (focus == null) return;
        IWidget widget = target(focus.target());
        if (widget == null) return;
        Area a = widget.getArea();
        float t = Math.max(0, Math.min(1, (tick - focus.at() + focus.leadTicks()) / (float) focus.leadTicks()));
        t = t * t * (3 - 2 * t);
        float px = a.x + a.w() / 2f + 26 * (1 - t), py = a.y + a.h() / 2f + 22 * (1 - t);
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2);
        GL11.glColor4f(1, .78f, .25f, .9f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glVertex2f(a.x - 1, a.y - 1); GL11.glVertex2f(a.x + a.w() + 1, a.y - 1);
        GL11.glVertex2f(a.x + a.w() + 1, a.y + a.h() + 1); GL11.glVertex2f(a.x - 1, a.y + a.h() + 1);
        GL11.glEnd();
        if (tick >= focus.at()) {
            float elapsed = (tick - focus.at()) / 16f, radius = 3 + elapsed * 12;
            GL11.glColor4f(1, .78f, .25f, 1 - elapsed);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i < 32; i++) {
                double angle = i * Math.PI / 16;
                GL11.glVertex2d(px + Math.cos(angle) * radius, py + Math.sin(angle) * radius);
            }
            GL11.glEnd();
        }
        GL11.glColor4f(0, 0, 0, 1);
        pointer(px, py, 1.25f);
        GL11.glColor4f(1, 1, 1, 1);
        pointer(px + 1, py + 1, .85f);
        if (focus.holdsDataStick()) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            GuiDraw.drawItem(ItemList.Tool_DataStick.get(1), (int) px + 5, (int) py + 6, 16, 16, 200);
        }
        GL11.glPopAttrib();
    }

    private static void pointer(float x, float y, float scale) {
        GL11.glBegin(GL11.GL_TRIANGLES);
        GL11.glVertex2f(x, y); GL11.glVertex2f(x + 3 * scale, y + 11 * scale);
        GL11.glVertex2f(x + 9 * scale, y + 7 * scale);
        GL11.glEnd();
    }

    @Override
    public void close() {
        if (closed) return;
        closed = true;
        screen.getPanelManager().closeAll();
        screen.getPanelManager().dispose();
        sync.onClose();
        sync.dispose();
    }
}
