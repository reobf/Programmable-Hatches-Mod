package reobf.proghatches.ae;

import java.lang.ref.WeakReference;
import java.util.HashSet;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.parts.IPartRenderHelper;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnelStatic;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.fmp.ICraftingMachinePart;
import reobf.proghatches.fmp.LayerCraftingMachine.StateHolder;

public class PartMAP2P extends PartP2PTunnelStatic<PartMAP2P> implements ICraftingMachinePart {

    public PartMAP2P(ItemStack is) {
        super(is);

    }

    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {
        if (isOutput()) {
            return false;
        }
        if (getFrequency() == 0) {
            return false;
        }

        if (tick == MinecraftServer.getServer()
            .getTickCounter()) {
            // same tick... try the one found in acceptsPlans!
            ICraftingMachine val;
            if ((val = candidate.get()) != null && val.pushPattern(patternDetails, table, candidateDir)) {
                return true;
            }
        }
        // direct call to pushPattern? or acceptsPlans but push failed?
        // just iterate again to find another valid!
        try {

            for (PartMAP2P out : getOutputs()) {

                TileEntity te = out.getTarget();
                if (te == null) {
                    continue;
                }
                if (te instanceof ICraftingMachine) {

                    ICraftingMachine ep = (ICraftingMachine) te;
                    ForgeDirection old = StateHolder.state;
                    StateHolder.state = out.getSide()
                        .getOpposite();
                    if (ep.acceptsPlans()) {
                        if (ep.pushPattern(
                            patternDetails,
                            table,
                            out.getSide()
                                .getOpposite())) {
                            return true;
                        }
                        StateHolder.state = old;
                        continue;
                    }
                    StateHolder.state = old;
                }

            } ;
        } catch (GridAccessException e) {

        }

        return false;
    }

    ForgeDirection candidateDir;
    WeakReference<ICraftingMachine> candidate = new WeakReference<>(null);
    int tick;

    @Override
    public boolean acceptsPlans(ForgeDirection ejectionDirection) {
        try {

            if (chain.contains(this)) {

                return false;
            }
            chain.add(this);

            if (isOutput()) {
                return false;
            }
            if (getFrequency() == 0) {
                return false;
            }

            try {
                for (PartMAP2P out : getOutputs()) {

                    TileEntity te = out.getTarget();
                    if (te == null) {
                        continue;
                    }
                    /*
                     * if(te instanceof IPartHost){
                     * //shortcut fot recursive p2p
                     * IPart p=((IPartHost) te).getPart(side.getOpposite());
                     * if( p instanceof PartMAP2P){
                     * ((PartMAP2P)p).getOuputEndpoints(fret);
                     * return;
                     * }
                     * }
                     */
                    if (te instanceof ICraftingMachine) {

                        ICraftingMachine ep = (ICraftingMachine) te;
                        ForgeDirection old = StateHolder.state;
                        StateHolder.state = out.getSide()
                            .getOpposite();
                        if (ep.acceptsPlans()) {
                            tick = MinecraftServer.getServer()
                                .getTickCounter();
                            candidate = new WeakReference<ICraftingMachine>(ep);
                            candidateDir = out.getSide()
                                .getOpposite();
                            StateHolder.state = old;
                            return true;
                        }
                        StateHolder.state = old;
                    }

                } ;
            } catch (GridAccessException e) {

            }

            return false;
        } finally {
            chain.remove(this);
        }
    }

    private TileEntity getTarget() {
        if (!this.getProxy()
            .isActive()) {
            return null;
        }

        final TileEntity te = this.getTile()
            .getWorldObj()
            .getTileEntity(
                this.getTile().xCoord + this.getSide().offsetX,
                this.getTile().yCoord + this.getSide().offsetY,
                this.getTile().zCoord + this.getSide().offsetZ);

        return te;
    }

    public static HashSet<Object> chain = new HashSet();
    /*
     * public Multimap<ICraftingMachine,ForgeDirection> getOuputEndpoints(Multimap<ICraftingMachine,ForgeDirection> ret)
     * {
     * try{
     * if(ret==null)
     * ret=HashMultimap.create();
     * if(chain.contains(this)){return ret;}
     * chain.add(this);
     * final Multimap<ICraftingMachine,ForgeDirection> fret=ret;
     * try {
     * getOutputs().forEach((out)->{
     * TileEntity te = out.getTarget();
     * if(te==null){return;}
     * if(te instanceof IPartHost){
     * //shortcut fot recursive p2p
     * IPart p=((IPartHost) te).getPart(side.getOpposite());
     * if( p instanceof PartMAP2P){
     * ((PartMAP2P)p).getOuputEndpoints(fret);
     * return;
     * }
     * }
     * if(te instanceof ICraftingMachine){
     * //ForgeDirection old=StateHolder.state;
     * //StateHolder.state=out.getSide().getOpposite();
     * fret.put((ICraftingMachine) te,out.getSide().getOpposite());
     * //StateHolder.state=old;
     * }
     * });
     * } catch (GridAccessException e) {
     * }
     * return ret;}finally{
     * chain.remove(this);
     * }
     * }
     */

    @Override
    @SideOnly(Side.CLIENT)
    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
        rh.setTexture(Blocks.crafting_table.getIcon(1, 0));

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

    @Override
    @SideOnly(Side.CLIENT)
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
        final RenderBlocks renderer) {
        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));

        rh.setTexture(Blocks.crafting_table.getIcon(1, 0));

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
}
