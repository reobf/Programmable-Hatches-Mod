package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;

import com.google.common.collect.ImmutableMap;

import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidTank;

import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.extensions.ArrayExt;
import reobf.proghatches.main.Config;

public class SuperfluidHatch extends BufferedDualInputHatch {

    public SuperfluidHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures,
        boolean mMultiFluid, int bufferNum) {
        super(mName, mTier, 4 + 1, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

    }

    @Override
    public int fluidSlotsPerRow() {

        return 2;
    }

    @Override
    public int slotTierOverride(int mTier) {

        return 1;// 4 slots
    }

    public SuperfluidHatch(int id, String name, String nameRegional, int tier, boolean mMultiFluid, int bufferNum) {
        super(
            id,
            name,
            nameRegional,
            tier,
            4 + 1,
            mMultiFluid,
            bufferNum,
            reobf.proghatches.main.Config.get("SH", ImmutableMap.of())
          /*  (String[]) Config.defaultObj(

                ArrayExt.of(
                    "Dedicated to handle the siutation of many types of fluid input",
                    "Item/Fluid Input for Multiblocks",
                    "Contents are always separated with other bus/hatch",
                    "Programming Cover function integrated",
                    "Buffer: 1",
                    "For each buffer:",
                    "Capacity: 10,000,000L x24 types of fluid",
                    "16 Slots",
                    "Slot maximum stacksize:64",
                    StatCollector.translateToLocal("programmable_hatches.addedby")),
                ArrayExt.of(
                    "致力于解决超多种流体输入",
                    "多方块机器的物品/流体输入",
                    "总是与其它输入仓/输入总线隔离",
                    "自带编程覆盖板功能",
                    "缓冲数量: 1",
                    "缓冲容量: 10,000,000L x24种流体",
                    "16 格",
                    "每格堆叠限制:64"
                    ,StatCollector.translateToLocal("programmable_hatches.addedby")
                ))*/

        );

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new SuperfluidHatch(mName, mTier, mDescriptionArray, mTextures, mMultiFluid, bufferNum);

    }

    public int fluidLimit() {
        return 10_000_000;

    }

    public int itemLimit() {
        return 64;
    }

    public void initTierBasedField() {

        ArrayList<FluidTank> arr = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            arr.add(new ListeningFluidTank((int) (1000 * Math.pow(2, mTier)),this));

        }

        mStoredFluid = arr.toArray(new ListeningFluidTank[0]);

        return;
    }
}
