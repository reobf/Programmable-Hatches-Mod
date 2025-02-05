package reobf.proghatches.eucrafting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.collect.ImmutableList;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnelStatic;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTETieredMachineBlock;
import reobf.proghatches.main.Config;
import tectech.mechanics.pipe.IConnectsToEnergyTunnel;
import tectech.thing.metaTileEntity.pipe.MTEPipeEnergy;

public class PartLazerP2P<S extends MetaTileEntity & IConnectsToEnergyTunnel, D extends MetaTileEntity & IConnectsToEnergyTunnel>

    extends PartP2PTunnelStatic<PartLazerP2P> implements ILazer, IGridTickable {

    public PartLazerP2P(ItemStack is) {
        super(is);

    }

    @Override
    public boolean canConnect(ForgeDirection side) {

        return true;
    }

    @Override
    public byte getColorization() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public byte setColorization(byte aColor) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public ILazer getLazerP2PIn(ForgeDirection dir) {

        return this;
    }

    @Override
    public ForgeDirection getLazerDir() {

        return side;
    }
    /*
     * @Override
     * public List<ILazer> getLazerP2POuts() {
     * if(this.output)return ImmutableList.of();
     * List<ILazer> all=new ArrayList<>();
     * try {
     * this.getOutputs().forEach(all::add);
     * } catch (GridAccessException e) {
     * }
     * return all;
     * }
     */

    public static class RestrictedTarget<D extends MetaTileEntity & IConnectsToEnergyTunnel> {

        Deque<Consumer<Long>> callback = new ArrayDeque<>();

        public RestrictedTarget<D> mark(Consumer<Long> cb) {
            callback.addLast(cb);
            return this;
        }

        D target;
        long limit;// -1 means not restricted

        public RestrictedTarget(D target, long limit) {
            this.target = target;
            this.limit = limit;
        }

        public RestrictedTarget(D target) {
            this.target = target;
            this.limit = -1;
        }

    }

    public List<RestrictedTarget<D>> collectAllEndpoints() {
        if (this.output) return ImmutableList.of();
        ArrayList<RestrictedTarget<D>> all = new ArrayList<>();
        try {
            getOutputs().forEach(s -> {

                Object o = s.getForward();
                if (o instanceof ILazer) {
                    all.addAll((Collection<? extends RestrictedTarget<D>>) ((ILazer) o).collectAllEndpoints());
                } else if (isDist(o)) {
                    all.add(new RestrictedTarget((D) o));
                }

            });
        } catch (GridAccessException e) {}

        return all;
    }

    /**
     * make it works both for tectech&bartworks
     */
    public static boolean isSourece(Object o) {
        if (o instanceof IConnectsToEnergyTunnel) {
            if (o instanceof MetaTileEntity) {

                return ((MetaTileEntity) o).maxEUOutput() > 1;
            }

        }
        return false;
    }

    /**
     * make it works both for tectech&bartworks
     */
    static public boolean isDist(Object o) {
        if (o instanceof IConnectsToEnergyTunnel) {
            if (o instanceof MetaTileEntity) {
                return ((MetaTileEntity) o).maxEUInput() > 1;
            }

        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private S getBackward() {

        {
            TileEntity thiz = this.getHost()
                .getTile();
            ForgeDirection face = this.getLazerDir();

            final ForgeDirection opposite = face;
            for (short dist = 1; dist < 1000; dist++) {

                TileEntity rawtile = thiz.getWorldObj()
                    .getTileEntity(
                        thiz.xCoord + dist * opposite.offsetX,
                        thiz.yCoord + dist * opposite.offsetY,
                        thiz.zCoord + dist * opposite.offsetZ);
                if (rawtile == null) return null;
                IGregTechTileEntity tGTTileEntity = rawtile instanceof IGregTechTileEntity
                    ? (IGregTechTileEntity) rawtile
                    : null;
                if (tGTTileEntity != null) {
                    IMetaTileEntity aMetaTileEntity = tGTTileEntity.getMetaTileEntity();
                    if (aMetaTileEntity != null) {
                        if (isSourece(aMetaTileEntity)
                        // && ((IConnectsToEnergyTunnel)tGTTileEntity).canConnect(opposite.getOpposite()))
                        ) {

                            return (S) (aMetaTileEntity);
                        } else if (aMetaTileEntity instanceof MTEPipeEnergy) {
                            if (((MTEPipeEnergy) aMetaTileEntity).connectionCount < 2) {
                                return null;
                            } else {
                                ((MTEPipeEnergy) aMetaTileEntity).markUsed();
                            }
                            continue;
                        }
                    }
                } else {

                    /*
                     * if(rawtile instanceof ILazer){
                     * return ((ILazer) rawtile).getLazerP2PIn(opposite);
                     * }
                     */

                }

            }
        }
        return null;

    }

    public Object getForward() {

        {
            TileEntity thiz = this.getHost()
                .getTile();
            ForgeDirection face = this.getLazerDir();

            final ForgeDirection opposite = face;
            for (short dist = 1; dist < 1000; dist++) {

                TileEntity rawtile = thiz.getWorldObj()
                    .getTileEntity(
                        thiz.xCoord + dist * opposite.offsetX,
                        thiz.yCoord + dist * opposite.offsetY,
                        thiz.zCoord + dist * opposite.offsetZ);
                if (rawtile == null) return null;
                IGregTechTileEntity tGTTileEntity = rawtile instanceof IGregTechTileEntity
                    ? (IGregTechTileEntity) rawtile
                    : null;
                if (tGTTileEntity != null) {
                    IMetaTileEntity aMetaTileEntity = tGTTileEntity.getMetaTileEntity();
                    if (aMetaTileEntity != null) {
                        if (isDist(aMetaTileEntity)
                        // && opposite.getOpposite() == tGTTileEntity.getFrontFacing()
                        ) {
                            return (aMetaTileEntity);
                        } else if (aMetaTileEntity instanceof MTEPipeEnergy) {
                            if (((MTEPipeEnergy) aMetaTileEntity).connectionCount < 2) {
                                return null;
                            } else {
                                ((MTEPipeEnergy) aMetaTileEntity).markUsed();
                            }
                            continue;
                        }
                    }
                } else {
                    if (Config.recursiveLazer) if (rawtile instanceof ILazer) {

                        return ((ILazer) rawtile).getLazerP2PIn(opposite);

                    }

                }

            }
        }
        return null;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {

        return new TickingRequest(20, 20, false, false);// update every 20 ticks
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (this.output) {
            return TickRateModulation.SAME;
        }
        S source = getBackward();
        List<RestrictedTarget<D>> dist = collectAllEndpoints();
        if (source != null) moveForward(source, dist);

        return TickRateModulation.SAME;
    }

    public static <S extends MetaTileEntity & IConnectsToEnergyTunnel, D extends MetaTileEntity & IConnectsToEnergyTunnel> void moveForward(
        Object ss, List<RestrictedTarget<D>> dist) {
        S s = (S) ss;
        long record[] = new long[] { s.maxAmperesOut() * 20L * s.maxEUOutput() };

        for (RestrictedTarget<D> dd : dist) {
            moveForward(s, dd, record);
            if (record[0] <= 0) {
                break;
            }
        }

    }

    public static <S extends MetaTileEntity & IConnectsToEnergyTunnel, D extends MetaTileEntity & IConnectsToEnergyTunnel> void moveForward(
        S s, RestrictedTarget<D> dd0, long[] max) {
        D dd = (D) dd0.target;
        dd0.callback.forEach(st -> {

            if (s instanceof MTETieredMachineBlock) st.accept(GTValues.V[((MTETieredMachineBlock) s).mTier]);

        });
        if (s.maxEUOutput() > (dd).maxEUInput()) {
            dd.doExplosion(s.maxEUOutput());
            s.setEUVar(
                s.getBaseMetaTileEntity()
                    .getStoredEU() - s.maxEUOutput());
            return;
        } else if (s.maxEUOutput() == (dd).maxEUInput()) {

            D aMetaTileEntity = dd;
            IGregTechTileEntity aBaseMetaTileEntity = s.getBaseMetaTileEntity();
            long diff = Math.min(
                s.maxAmperesOut() * 20L * s.maxEUOutput(),
                Math.min(
                    (aMetaTileEntity).maxEUStore() - aMetaTileEntity.getBaseMetaTileEntity()
                        .getStoredEU(),
                    aBaseMetaTileEntity.getStoredEU()));
            diff = Math.min(max[0], diff);
            max[0] -= diff;
            s.setEUVar(aBaseMetaTileEntity.getStoredEU() - diff);

            dd.setEUVar(
                dd.getBaseMetaTileEntity()
                    .getStoredEU() + diff);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(Blocks.stone.getIcon(0, 0));

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        rh.setTexture(
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.BlockP2PTunnel2.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);
    }

    /**
     * @return If enabled it returns the icon of an AE quartz block, else vanilla quartz block icon
     */
    public IIcon getTypeTexture() {

        return Blocks.gold_block.getIcon(0, 0);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
        AEColor paint = this.getColor();

        if (paint == AEColor.Transparent) rh.setTexture(Blocks.stone.getIcon(0, 0));
        else rh.setTexture(Blocks.wool.getIcon(0, paint.ordinal())
        // this.getTypeTexture()
        );

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.BlockP2PTunnel2.getIcon(),
            this.getItemStack()
                .getIconIndex(),
            CableBusTextures.PartTunnelSides.getIcon(),
            CableBusTextures.PartTunnelSides.getIcon());

        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(3, 3, 13, 13, 13, 14);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(CableBusTextures.BlockP2PTunnel3.getIcon());

        rh.setBounds(6, 5, 12, 10, 11, 13);
        rh.renderBlock(x, y, z, renderer);

        rh.setBounds(5, 6, 12, 11, 10, 13);
        rh.renderBlock(x, y, z, renderer);

        this.renderLights(x, y, z, rh, renderer);
    }

    @Override
    public ItemStack getItemStack(final PartItemStack type) {

        return super.getItemStack(type);
    }
}
