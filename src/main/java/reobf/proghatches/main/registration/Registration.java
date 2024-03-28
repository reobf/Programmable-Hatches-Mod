package reobf.proghatches.main.registration;

import static gregtech.api.enums.Textures.BlockIcons.MACHINE_CASINGS;
import static reobf.proghatches.main.Config.*;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.render.TextureFactory;
import reobf.proghatches.gt.cover.ProgrammingCover;
import reobf.proghatches.gt.cover.RecipeCheckResultCover;
import reobf.proghatches.gt.cover.RecipeOutputAwarenessCover;
import reobf.proghatches.gt.cover.SmartArmCover;
import reobf.proghatches.gt.cover.WirelessControlCover;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatchSlave;
import reobf.proghatches.gt.metatileentity.FilterOutputBus;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.RecipeCheckResultDetector;
import reobf.proghatches.gt.metatileentity.RemoteInputBus;
import reobf.proghatches.gt.metatileentity.RemoteInputHatch;
import reobf.proghatches.gt.metatileentity.SuperfluidHatch;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;

public class Registration implements Runnable {

    public static ArrayList<ItemStack> items = new ArrayList<ItemStack>();
    public static ArrayList<ItemStack> items_eucrafting = new ArrayList<ItemStack>();
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
    public final static int TenaciousOffset = 70;// -73
    public final static int FilterOffset = 74;// -77
	private static final int RecipeCheckResultDetectorOffset = 78;

    @Override
    public void run() {

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + DualInputHatchOffset + i,
                "hatch.input.dual.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.dual.tier.name"), GT_Values.VN[i]),
                i,
                false);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + QuadDualInputHatchOffset + i,
                "hatch.input.dual.quad.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.dual.quad.tier.name"), GT_Values.VN[i]),
                i,
                true);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedDualInputHatchOffset + i,
                "hatch.input.buffered.dual.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.buffered.dual.tier.name"), GT_Values.VN[i]),
                i,
                false,
                1);

        }
        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchOffset + i,
                "hatch.input.buffered.dual.quad.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.buffered.dual.quad.tier.name"), GT_Values.VN[i]),
                
                i,
                true,
                1);

        }

        for (int i = 0; i < GT_Values.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchMKIIOffset + i,
                "hatch.input.buffered.dual.quad.tier.mkii." + i,
                String.format(LangManager.translateToLocal("hatch.input.buffered.dual.quad.tier.mkii.name"), GT_Values.VN[i]),
                i,
                true,
                6);

        }

        new ProgrammingCircuitProvider(
            Config.metaTileEntityOffset + CircuitProviderOffset,
            "circuitprovider",
            LangManager.translateToLocal("circuitprovider.name"),
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
        //WIP
        GregTech_API.registerCover(
                new ItemStack(MyMod.cover, 1, 15),
                TextureFactory.of(
                    MACHINE_CASINGS[1][0],
                    TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
                new RecipeOutputAwarenessCover());
        GregTech_API.registerCover(
        		new ItemStack(MyMod.cover, 1, 2),
        TextureFactory.of(
            MACHINE_CASINGS[1][0],
            TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
        
        new RecipeCheckResultCover());
        
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
            LangManager.translateToLocal("hatch.dualinput.slave.name"));

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
                LangManager.translateToLocal("hatch.input.item.remote.name"),
                1);
            new RemoteInputHatch(
                Config.metaTileEntityOffset + RemoteInputHatchOffset,
                "hatch.input.fluid.remote",
                LangManager.translateToLocal("hatch.input.fluid.remote.name"),
                1);
        }
        int tier = 8;
        new SuperfluidHatch(
            Config.metaTileEntityOffset + SuperFluidHatch,
            "hatch.input.buffered.superfluid",
            LangManager.translateToLocal("hatch.input.buffered.superfluid.name"),
            8,
            true,
            1) {

        };
        new PatternDualInputHatch(
            Config.metaTileEntityOffset + PatternOffset,
            "hatch.input.buffered.me",
            LangManager.translateToLocal("hatch.input.buffered.me.name"),
            10,
            true,
            6,
            true);

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + TenaciousOffset + i,
            "hatch.output.tenacious." + i,
            LangManager.translateToLocalFormatted("hatch.output.tenacious.name", GT_Values.VN[i]),
            i,
            true);

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + FilterOffset + i,
            "hatch.output.filter." + i,
            LangManager.translateToLocalFormatted("hatch.output.filter.name", GT_Values.VN[i]),
            i,
            false);
        
        new RecipeCheckResultDetector(
                Config.metaTileEntityOffset + RecipeCheckResultDetectorOffset,
                "recipe_check_result_detector",
                LangManager.translateToLocal("recipe_check_result_detector.name"),
                 0);

    }

}
