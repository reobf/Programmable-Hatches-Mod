package reobf.proghatches.gt.metatileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.ItemList;
import reobf.proghatches.client.guide.Mui2GuiPreview.Click;
import reobf.proghatches.client.guide.DualInputGuiAdapter;

@SideOnly(Side.CLIENT)
public final class CyclicPatternGuiAdapter extends DualInputGuiAdapter {

    private static final String LANE = "cyclic_lane_", BIND = "cyclic_bind_";

    @Override
    public boolean accepts(Object mte) {
        return mte instanceof CyclicPatternDualInputHatch;
    }

    @Override
    public void fillSyncedState(Object mte) {
        CyclicPatternDualInputHatch hatch = (CyclicPatternDualInputHatch) mte;
        StringBuilder lanes = new StringBuilder();
        for (int lane : hatch.patternLane) lanes.append((char) ('0' + lane));
        hatch.cLanes = lanes.toString();
        for (int lane = 1; lane <= CyclicPatternDualInputHatch.LANES; lane++) {
            hatch.cLaneInfo[lane] = hatch.laneInfo(lane);
            hatch.cLaneIcon[lane] = hatch.laneIcon(lane);
        }
    }

    @Override
    public ItemStack heldItem(Click click) {
        return click.target().startsWith(BIND) ? ItemList.Tool_DataStick.get(1) : null;
    }

    @Override
    public void pressOnServer(Object mte, Click click, EntityPlayer player) {
        CyclicPatternDualInputHatch hatch = (CyclicPatternDualInputHatch) mte;
        String name = click.target();
        if (name.startsWith(LANE)) hatch.stepPatternLane(Integer.parseInt(name.substring(LANE.length())), 1);
        if (!name.startsWith(BIND)) return;
        if (!click.located()) throw new IllegalArgumentException("Binding clicks require target x, y and z");
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
            String error = hatch.bindLane(Integer.parseInt(name.substring(BIND.length())), player);
            if (error != null) throw new IllegalStateException("Preview binding failed: " + error);
        } finally {
            player.inventory.setItemStack(null);
        }
    }
}
