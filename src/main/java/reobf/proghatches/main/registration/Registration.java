package reobf.proghatches.main.registration;

import static gregtech.api.enums.Textures.BlockIcons.MACHINE_CASINGS;
import static reobf.proghatches.main.Config.*;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.render.TextureFactory;
import reobf.proghatches.gt.cover.ProgrammingCover;
import reobf.proghatches.gt.cover.SmartArmCover;
import reobf.proghatches.gt.cover.WirelessControlCover;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatchSlave;
import reobf.proghatches.gt.metatileentity.FilterOutputBus;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.RemoteInputBus;
import reobf.proghatches.gt.metatileentity.RemoteInputHatch;
import reobf.proghatches.gt.metatileentity.SuperfluidHatch;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;

public class Registration implements Runnable {

    public static ArrayList<ItemStack> items = new ArrayList<ItemStack>();
    public final static int DualInputHatchOffset = 0;// -15
    public final static int QuadDualInputHatchOffset = 16;// -31
    public final static int BufferedQuadDualInputHatchOffset = 100;
    public final static int BufferedDualInputHatchOffset = 32;// -47
    public final static int CircuitProviderOffset = 48;
    public final static int SlaveOffset = 49;
    public final static int RemoteInputBusOffset = 50;
    public final static int BufferedQuadDualInputHatchMKIIOffset = 51;// -66
    public final static int RemoteInputHatchOffset = 67;
    public final static int SuperFluidHatch = 68;
    public final static int PatternOffset = 69;
    public final static int TenaciousOffset = 70;
    public final static int FilterOffset = 74;

    @Override
    public void run() {

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + DualInputHatchOffset + i,
                "hatch.input.dual.tier." + i,
                defaultName(
                    String.format("Programmable Dual Input Hatch (%s)", GT_Values.VN[i]),
                    String.format("编程二合一输入仓 (%s)", GT_Values.VN[i])),
                i,
                false);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + QuadDualInputHatchOffset + i,
                "hatch.input.dual.quad.tier." + i,
                defaultName(
                    String.format("Programmable Multifluid Dual Input Hatch (%s)", GT_Values.VN[i]),
                    String.format("编程多流体二合一输入仓 (%s)", GT_Values.VN[i])),
                i,
                true);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedDualInputHatchOffset + i,
                "hatch.input.buffered.dual.tier." + i,
                defaultName(
                    String.format("Programmable Buffered Dual Input Hatch (%s)", GT_Values.VN[i]),
                    String.format("编程缓冲二合一输入仓 (%s)", GT_Values.VN[i])),
                i,
                false,
                1);

        }
        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchOffset + i,
                "hatch.input.buffered.dual.quad.tier." + i,
                defaultName(
                    String.format("Programmable Buffered Multifluid Dual Input Hatch (%s)", GT_Values.VN[i]),
                    String.format("编程缓冲多流体二合一输入仓 (%s)", GT_Values.VN[i])),
                i,
                true,
                1);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchMKIIOffset + i,
                "hatch.input.buffered.dual.quad.tier.mkii." + i,
                defaultName(
                    String.format("Programmable Advanced Buffered Multifluid Dual Input Hatch (%s)", GT_Values.VN[i]),
                    String.format("进阶编程缓冲多流体二合一输入仓 (%s)", GT_Values.VN[i])),
                i,
                true,
                6);

        }

        new ProgrammingCircuitProvider(
            Config.metaTileEntityOffset + CircuitProviderOffset,
            "circuitprovider",
            defaultName(String.format("Programming Circuit Provider"), String.format("编程器电路提供器")),
            0,
            1);
        GregTech_API.registerCover(
            new ItemStack(MyMod.cover, 1, 0),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            new ProgrammingCover());
        GregTech_API.registerCover(
            new ItemStack(MyMod.cover, 1, 1),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            new WirelessControlCover());

        for (int i = 0; i < 15; i++) {
            ;
            GregTech_API.registerCover(
                new ItemStack(MyMod.smartarm, 1, i),
                TextureFactory
                    .of(MACHINE_CASINGS[i][0], TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_ARM)),
                new SmartArmCover(i));
        }

        new DualInputHatchSlave<>(
            Config.metaTileEntityOffset + SlaveOffset,
            "hatch.dualinput.slave",
            defaultName(

                String.format("Dual Input Slave"),
                String.format("二合一输入镜像")));

        /*
         * boolean compat;
         * try {
         * Class.forName("gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch");
         * compat=true;} catch (ClassNotFoundException e) {
         * compat=false;
         * }
         */

        {
            new RemoteInputBus(
                Config.metaTileEntityOffset + RemoteInputBusOffset,
                "hatch.input.item.remote",
                defaultName(

                    String.format("Remote Input Bus"),
                    String.format("远程输入总线")),
                1);
            new RemoteInputHatch(
                Config.metaTileEntityOffset + RemoteInputHatchOffset,
                "hatch.input.fluid.remote",
                defaultName(

                    String.format("Remote Input Hatch"),
                    String.format("远程输入仓")),
                1);
        }
        int tier = 8;
        new SuperfluidHatch(
            Config.metaTileEntityOffset + SuperFluidHatch,
            "hatch.input.buffered.superfluid",
            defaultName(String.format("Superfluid Dual Input Hatch"), String.format("超级流体二合一输入仓")),
            8,
            true,
            1) {

        };
        new PatternDualInputHatch(
            Config.metaTileEntityOffset + PatternOffset,
            "hatch.input.buffered.me",
            defaultName("Programmable Crafting Input Buffer", "编程样板输入总成")

            ,
            10,
            true,
            6,
            true);

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + TenaciousOffset + i,
            "hatch.output.tenacious." + i,
            defaultName(
                String.format("Tenacious Ouput Bus (%s)", GT_Values.VN[i]),
                String.format("吝物输出总线 (%s)", GT_Values.VN[i]))

            ,
            i,
            true);

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + FilterOffset + i,
            "hatch.output.filter." + i,
            defaultName(
                String.format("Filter Ouput Bus (%s)", GT_Values.VN[i]),
                String.format("过滤输出总线 (%s)", GT_Values.VN[i]))

            ,
            i,
            false);

    }

}
