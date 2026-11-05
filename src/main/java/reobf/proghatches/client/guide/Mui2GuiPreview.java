package reobf.proghatches.client.guide;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL20;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.widget.ITooltip;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.drawable.GuiDraw;
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
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.mojang.authlib.GameProfile;

import gregtech.api.metatileentity.BaseMetaTileEntity;
import io.netty.buffer.Unpooled;

/** A machine's real MUI2 GUI, built from its NBT and driven by scripted clicks, with no server behind it. */
public final class Mui2GuiPreview implements AutoCloseable {

    public record Click(int at, String target, int x, int y, int z, boolean located, String value) {
        public Click(int at, String target, int x, int y, int z, boolean located) {
            this(at, target, x, y, z, located, null);
        }
    }

    public record View(int x, int y, int width, int height) {

        View union(View other) {
            if (other == null) return this;
            int x0 = Math.min(x, other.x), y0 = Math.min(y, other.y);
            int x1 = Math.max(x + width, other.x + other.width), y1 = Math.max(y + height, other.y + other.height);
            return new View(x0, y0, x1 - x0, y1 - y0);
        }

        View grow(int by) {
            return new View(x - by, y - by, width + 2 * by, height + 2 * by);
        }
    }

    private static final int SCREEN = 1024, MARGIN = 6;

    private final Object mte;
    private final Mui2PreviewAdapter adapter;
    private final EntityOtherPlayerMP player;
    private final ModularSyncManager sync;
    private final PanelSyncManager syncManager;
    private final ModularScreen screen;
    private int applied;
    private long lastUpdate;
    private boolean closed;

    public Mui2GuiPreview(World world, NBTTagCompound nbt) throws IOException {
        this(world, nbt, net.minecraftforge.common.util.ForgeDirection.UNKNOWN);
    }

    @SuppressWarnings("unchecked")
    public Mui2GuiPreview(World world, NBTTagCompound nbt, net.minecraftforge.common.util.ForgeDirection partSide) throws IOException {
        TileEntity tile;
        if (nbt.hasKey("mID")) {
            tile = new BaseMetaTileEntity();
            tile.setWorldObj(world);
            tile.readFromNBT((NBTTagCompound) nbt.copy());
        } else {
            tile = TileEntity.createAndLoadEntity((NBTTagCompound) nbt.copy());
            if (tile == null) throw new IllegalArgumentException("Unknown preview tile entity");
            tile.setWorldObj(world);
        }
        if (partSide != net.minecraftforge.common.util.ForgeDirection.UNKNOWN) {
            if (!(tile instanceof appeng.api.parts.IPartHost host))
                throw new IllegalArgumentException("MUI2Gui part requires a cable host");
            mte = host.getPart(partSide);
        } else mte = tile instanceof BaseMetaTileEntity gt ? gt.getMetaTileEntity() : tile;
        if (!(mte instanceof IGuiHolder<?>)) throw new IllegalArgumentException("MUI2Gui requires a MUI2 GUI holder");
        IGuiHolder<PosGuiData> holder = (IGuiHolder<PosGuiData>) mte;
        adapter = Mui2PreviewAdapter.of(mte);
        adapter.fillSyncedState(mte);

        player = new EntityOtherPlayerMP(world,
            new GameProfile(UUID.fromString("ed6271ac-c706-46c0-970f-948464187a3d"), "GuidePreview"));
        PosGuiData data = partSide == net.minecraftforge.common.util.ForgeDirection.UNKNOWN
            ? new PosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord) {
                @Override
                public TileEntity getTileEntity() { return tile; }
            } : new com.cleanroommc.modularui.factory.SidedPosGuiData(player, tile.xCoord, tile.yCoord, tile.zCoord, partSide) {
                @Override
                public TileEntity getTileEntity() { return tile; }
            };
        sync = new ModularSyncManager(true);
        syncManager = new PanelSyncManager(sync, true);
        UISettings settings = new UISettings();
        ModularPanel root = holder.buildUI(data, syncManager, settings);
        WidgetTree.collectSyncValues(syncManager, root);
        adapter.initialize(mte, syncManager);
        ModularContainer container = new ModularContainer();
        container.construct(player, sync, settings, root.getName(), data);
        screen = holder.createScreen(data, root).drawDarkBackground(false);
        screen.getContext().setSettings(settings);
        if (settings.getTheme() != null) screen.useTheme(settings.getTheme());
        // GuiContainer.initGui would replace the real player's open container.
        new GuiContainerWrapper(container, screen) {
            {
                mc = Minecraft.getMinecraft();
                fontRendererObj = mc.fontRenderer;
                width = SCREEN;
                height = SCREEN;
            }
        };
        screen.onResize(SCREEN, SCREEN);
        sync.onOpen();
        initializeFluidSlots(root);
        screen.getContext().updateState(-10000, -10000, 0);
        screen.onUpdate();
        screen.onFrameUpdate();
    }

    public static View measure(World world, NBTTagCompound nbt, List<Click> clicks, boolean showMain)
        throws IOException {
        return measure(world, nbt, clicks, showMain, net.minecraftforge.common.util.ForgeDirection.UNKNOWN);
    }

    public static View measure(World world, NBTTagCompound nbt, List<Click> clicks, boolean showMain,
        net.minecraftforge.common.util.ForgeDirection partSide) throws IOException {
        try (Mui2GuiPreview probe = new Mui2GuiPreview(world, nbt, partSide)) {
            View view = probe.bounds(showMain);
            for (Click click : clicks) {
                probe.click(click);
                View now = probe.bounds(showMain);
                view = view == null ? now : view.union(now);
            }
            return view == null ? null : view.grow(MARGIN);
        }
    }

    public View bounds(boolean showMain) {
        View view = null;
        for (ModularPanel panel : screen.getPanelManager().getOpenPanels()) {
            if (!showMain && panel == screen.getMainPanel()) continue;
            view = union(view, panel.getArea());
            for (IWidget child : panel.getChildren()) if (child.isEnabled()) view = union(view, child.getArea());
        }
        return view;
    }

    private static View union(View view, Area area) {
        if (area.w() <= 0 || area.h() <= 0) return view;
        View next = new View(area.x, area.y, area.w(), area.h());
        return view == null ? next : view.union(next);
    }

    public void advance(int tick, List<Click> clicks) throws IOException {
        while (applied < clicks.size() && clicks.get(applied).at() <= tick) click(clicks.get(applied++));
    }

    private void click(Click click) throws IOException {
        String name = click.target();
        IWidget widget = target(name);
        if (widget == null) throw new IllegalStateException("MUI2Click widget unavailable: " + name);
        if (!shown(widget)) throw new IllegalStateException("MUI2Click widget is hidden: " + name);
        if (click.value() != null) {
            if (!(widget instanceof com.cleanroommc.modularui.widgets.textfield.TextFieldWidget field))
                throw new IllegalStateException("MUI2Click value requires a text field: " + name);
            field.setText(click.value());
            field.onRemoveFocus(screen.getContext());
        } else if (widget instanceof PageButton tab) {
            tab.getController().setPage(tab.getIndex());
        } else if (widget instanceof com.cleanroommc.modularui.widgets.CycleButtonWidget cycle) {
            cycle.onMousePressed(0);
        } else if (widget instanceof ButtonWidget<?> button) {
            button.clickSound(() -> {});
            button.onMousePressed(0);
            String popup = adapter.popupOpenedBy(name);
            if (popup != null) openPopup(popup);
        } else {
            throw new IllegalStateException("MUI2Click widget is not a button: " + name);
        }
        adapter.pressOnServer(mte, click, player);
        adapter.fillSyncedState(mte);
        adapter.afterClick(mte, click, syncManager);
        screen.onUpdate();
        screen.onFrameUpdate();
    }

    private void openPopup(String name) throws IOException {
        if (!(syncManager.findPanelHandlerNullable(name) instanceof PanelSyncHandler handler))
            throw new IllegalStateException("Popup handler missing: " + name);
        if (handler.isPanelOpen()) return;
        var previousPanels = new java.util.HashSet<>(screen.getPanelManager().getOpenPanels());
        PacketBuffer empty = new PacketBuffer(Unpooled.buffer());
        try {
            // Complete the open locally; this container has no network session.
            handler.readOnClient(PanelSyncHandler.SYNC_OPEN, empty);
            for (ModularPanel panel : screen.getPanelManager().getOpenPanels())
                if (!previousPanels.contains(panel)) initializeFluidSlots(panel);
        } finally {
            empty.release();
        }
    }

    private static void initializeFluidSlots(IWidget widget) {
        if (widget instanceof com.cleanroommc.modularui.widgets.slot.FluidSlot slot
            && slot.getSyncHandler() instanceof com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler handler)
            handler.updateCacheFromSource(true);
        for (IWidget child : widget.getChildren()) initializeFluidSlots(child);
    }

    private IWidget target(String name) {
        List<ModularPanel> panels = screen.getPanelManager().getOpenPanels();
        for (int i = panels.size() - 1; i >= 0; i--) {
            IWidget widget = WidgetTree.findFirstWithNameNullable(panels.get(i), name);
            if (widget != null) return widget;
        }
        return null;
    }

    private static boolean shown(IWidget widget) {
        for (IWidget w = widget; w != null; w = w instanceof ModularPanel ? null : w.getParent())
            if (!w.isEnabled()) return false;
        return true;
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
        GL20.glBlendEquationSeparate(GL14.GL_FUNC_ADD, GL14.GL_MAX);
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

    private int lead(Click click) {
        return adapter.heldItem(click) != null ? 60 : 24;
    }

    private void drawPointer(int tick, List<Click> clicks) {
        Click focus = null;
        for (Click click : clicks) if (tick >= click.at() - lead(click) && tick < click.at() + 16) {
            focus = click;
            break;
        }
        if (focus == null) return;
        IWidget widget = target(focus.target());
        if (widget == null) return;
        Area a = widget.getArea();
        int lead = lead(focus);
        float t = Math.max(0, Math.min(1, (tick - focus.at() + lead) / (float) lead));
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
        ItemStack held = adapter.heldItem(focus);
        if (held != null) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glColor4f(1, 1, 1, 1);
            GuiDraw.drawItem(held, (int) px + 5, (int) py + 6, 16, 16, 200);
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
