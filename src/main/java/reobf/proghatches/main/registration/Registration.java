package reobf.proghatches.main.registration;

import static gregtech.api.enums.Textures.BlockIcons.MACHINE_CASINGS;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import com.glodblock.github.loader.ItemAndBlockHolder;

import appeng.api.AEApi;
import gregtech.api.covers.CoverPlacer;
import gregtech.api.covers.CoverRegistry;
import gregtech.api.enums.Dyes;
import gregtech.api.enums.GTValues;
import gregtech.api.interfaces.ITexture;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.LightingHelper;
import gregtech.common.covers.Cover;
import gregtech.common.render.GTCopiedBlockTextureRender;
import reobf.proghatches.eucrafting.AECover;
import reobf.proghatches.eucrafting.BridgingData;
import reobf.proghatches.eucrafting.InterfaceData;
import reobf.proghatches.eucrafting.InterfaceP2PData;
import reobf.proghatches.eucrafting.InterfaceP2PEUData;
import reobf.proghatches.eucrafting.InterfaceP2PNoFluidData;
import reobf.proghatches.gt.cover.CircuitHolderCover;
import reobf.proghatches.gt.cover.LevelControlCover;
import reobf.proghatches.gt.cover.LinkedBusSlaveCover;
import reobf.proghatches.gt.cover.ProgrammingCover;
import reobf.proghatches.gt.cover.SmartArmCover;
import reobf.proghatches.gt.cover.WirelessControlCover;
import reobf.proghatches.gt.metatileentity.AutoDataOrbHatch;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.gt.metatileentity.CommunicationPortHatch;
import reobf.proghatches.gt.metatileentity.DataHatchME;
import reobf.proghatches.gt.metatileentity.DualInputHachOC;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.DualInputHatchInventoryMappingSlave;
import reobf.proghatches.gt.metatileentity.DualInputHatchSlave;
import reobf.proghatches.gt.metatileentity.DualInputHatchSlaveBus;
import reobf.proghatches.gt.metatileentity.DualInputHatchSlaveHatch;
import reobf.proghatches.gt.metatileentity.FilterOutputBus;
import reobf.proghatches.gt.metatileentity.IngredientBuffer;
import reobf.proghatches.gt.metatileentity.MultiCircuitInputBus;
import reobf.proghatches.gt.metatileentity.NBTHatchMECatalyst;
import reobf.proghatches.gt.metatileentity.NBTHatchMEFocus;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatchInventoryMappingSlave;
import reobf.proghatches.gt.metatileentity.PhantomInputBus;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProviderPrefabricated;
import reobf.proghatches.gt.metatileentity.ProviderChainer;
import reobf.proghatches.gt.metatileentity.RemoteInputBus;
import reobf.proghatches.gt.metatileentity.RemoteInputHatch;
import reobf.proghatches.gt.metatileentity.StockingDualInputHatchME;
import reobf.proghatches.gt.metatileentity.StorageOutputBus;
import reobf.proghatches.gt.metatileentity.StorageOutputHatch;
import reobf.proghatches.gt.metatileentity.SuperChestME;
import reobf.proghatches.gt.metatileentity.SuperTankME;
import reobf.proghatches.gt.metatileentity.SuperfluidHatch;
import reobf.proghatches.gt.metatileentity.VoidOutputBus;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch;
import reobf.proghatches.gt.metatileentity.WaterProvider;
import reobf.proghatches.gt.metatileentity.meinput.DecoyInputBusME;
import reobf.proghatches.gt.metatileentity.meinput.DecoyInputHatchME;
import reobf.proghatches.gt.metatileentity.meinput.PriorityFilterInputBusME;
import reobf.proghatches.gt.metatileentity.meinput.PriorityFilterInputHatchME;
import reobf.proghatches.gt.metatileentity.meinput.RestrictedInputBusME;
import reobf.proghatches.gt.metatileentity.meinput.RestrictedInputHatchME;
import reobf.proghatches.gt.metatileentity.multi.IngredientDistributor;
import reobf.proghatches.gt.metatileentity.multi.LargeProgrammingCircuitProvider;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;

// spotless:off
public class Registration implements Runnable {

    public static ArrayList<ItemStack> items = new ArrayList<ItemStack>();
    public static ArrayList<ItemStack> items_eucrafting = new ArrayList<ItemStack>();
    public final static int DualInputHatchOffset = 0;// -15
    public final static int QuadDualInputHatchOffset = 16;// -31

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
    public final static int IngBufferOffset = 79;// -80
    public final static int WaterProviderOffset = 81;
    public final static int MEFocusOffset = 82;
    public final static int DataOrbOffset = 83;
    public final static int MECatalystOffset = 84;
    public final static int StockingDualInputOffset = 85;// -86
    public final static int PhantomInputBusOffset = 87;
    // public static final int MBProxyOffset = 88;
    // public static final int PatternHousingOffset = 89;

    public final static int BufferedQuadDualInputHatchOffset = 100;// -115
    public final static int LargeProviderOffset = 116;
    public final static int ChainerOffset = 117;
    public final static int CircuitProviderOffsetT0 = 118;
    public final static int MEChest = 119;// -129
    public final static int PatternOffsetBus = 130;
    public final static int METank = 131;// -141
    public static final int IngredientDistributorOffset = 142;
    public static final int PrefabOffset = 143;//
    public static final int MultiCircuitBusOffset = 150;// -153
    public static final int MultiCircuitDualInputHatchOffset = 154;// -157
    public static final int MultiCircuitQuadDualInputHatchOffset = 158;// -161
    public static final int DataHatchMEOffset = 162;
    public static final int RestrictedHatchME = 163;
    public static final int RestrictedBusME = 164;
    public final static int DualInputHatchOCOffset = 165;
    public final static int MappingSlaveOffset = 166;
    public final static int PatternMappingSlaveOffset = 167;
    public static final int SlaveBusOffset = 168;
    public static final int SlaveHatchOffset = 169;
    public static final int CommunicationPortHatchOffset = 170;
    public static final int DecoyBusME = 171;
    public static final int DecoyHatchME = 172;
    public static final int PFilterBusME = 173;
    public static final int PFilterHatchME = 174;
    public static final int VHatch = 175;
    public static final int VBus = 176;
    public static final int StorageOutputBus = 177;
    public static final int StorageOutputHatch = 178;

    @SuppressWarnings("deprecation")
    @Override
    public void run() {

        for (int i = 0; i < GTValues.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + DualInputHatchOffset + i,
                "hatch.input.dual.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.dual.tier.name"), GTValues.VN[i]),
                i,
                false);

        }

        for (int i = 0; i < GTValues.VN.length - 1; i++) {

            new DualInputHatch(
                Config.metaTileEntityOffset + QuadDualInputHatchOffset + i,
                "hatch.input.dual.quad.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.dual.quad.tier.name"), GTValues.VN[i]),
                i,
                true);

        }

        for (int i = 0; i < GTValues.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedDualInputHatchOffset + i,
                "hatch.input.buffered.dual.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.buffered.dual.tier.name"), GTValues.VN[i]),
                i,
                false,
                1);

        }
        for (int i = 0; i < GTValues.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchOffset + i,
                "hatch.input.buffered.dual.quad.tier." + i,
                String.format(LangManager.translateToLocal("hatch.input.buffered.dual.quad.tier.name"), GTValues.VN[i]),

                i,
                true,
                1);

        }

        for (int i = 0; i < GTValues.VN.length - 1; i++) {

            new BufferedDualInputHatch(
                Config.metaTileEntityOffset + BufferedQuadDualInputHatchMKIIOffset + i,
                "hatch.input.buffered.dual.quad.tier.mkii." + i,
                String.format(
                    LangManager.translateToLocal("hatch.input.buffered.dual.quad.tier.mkii.name"),
                    GTValues.VN[i]),
                i,
                true,
                6 + Math.max(i - 4, 0) * 2);

        }
        new ProgrammingCircuitProvider(
            Config.metaTileEntityOffset + CircuitProviderOffsetT0,
            "circuitprovider.t0",
            LangManager.translateToLocal("circuitprovider.name.tech0"),
            3,
            1,
            0);

        new ProgrammingCircuitProvider(
            Config.metaTileEntityOffset + CircuitProviderOffset,
            "circuitprovider",
            LangManager.translateToLocal("circuitprovider.name"),
            5,
            4,
            1);

        registerCover(
            new ItemStack(MyMod.cover, 1, 100),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            (s, tex) -> new LevelControlCover(s, tex),
            null

        );
        registerCover(
            new ItemStack(MyMod.cover, 1, 0),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            (s, tex) -> new ProgrammingCover(s, tex),
            ProgrammingCover.placer());
        registerCover(
            new ItemStack(MyMod.cover, 1, 1),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            (s, tex) -> new WirelessControlCover(s, tex),
            null);
        registerCover(
            new ItemStack(MyMod.cover, 1, 4),
            TextureFactory.of(
                MACHINE_CASINGS[1][0],
                TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
            (s, tex) -> new LinkedBusSlaveCover(s, tex),
            LinkedBusSlaveCover.placer());
        // WIP
        /*
         * registerCover(
         * new ItemStack(MyMod.cover, 1, 15),
         * TextureFactory.of(
         * MACHINE_CASINGS[1][0],
         * TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
         * new RecipeOutputAwarenessCover());
         */
        /*
         * registerCover(
         * new ItemStack(MyMod.cover, 1, 2),
         * TextureFactory.of(
         * MACHINE_CASINGS[1][0],
         * TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
         * new RecipeCheckResultCover());
         */
        /*
         * registerCover(
         * new ItemStack(MyMod.cover, 1, 3),
         * TextureFactory.of(
         * MACHINE_CASINGS[1][0],
         * TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
         * s->new LastWorktimeCover());
         */
        ITexture arm = TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_ARM);
        ITexture scre = TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW);
        for (int i = 0; i < 15; i++) {
            int ii = i;
            registerCover(
                new ItemStack(MyMod.smartarm, 1, i),
                TextureFactory.of(MACHINE_CASINGS[i][0], arm),
                (s, tex) -> new SmartArmCover(ii, s, arm),
                null);
        }

        registerCover(
            new ItemStack(MyMod.cover, 1, 32),
            TextureFactory.of(
                AEApi.instance()
                    .blocks().blockInterface.block()),

            (s, tex) -> new AECover(s, InterfaceData.class, tex),
            AECover.placer());

        registerCover(
            new ItemStack(MyMod.cover, 1, 33),
            TextureFactory.of(ItemAndBlockHolder.INTERFACE),

            (s, tex) -> new AECover(s, InterfaceData.FluidInterfaceData_TileFluidInterface.class, tex),
            AECover.placer());
        registerCover(
            new ItemStack(MyMod.cover, 1, 34),
            TextureFactory.of(
                AEApi.instance()
                    .blocks().blockInterface.block()),

            (s, tex) -> new AECover(s, InterfaceP2PNoFluidData.class, tex),
            AECover.placer());
        registerCover(
            new ItemStack(MyMod.cover, 1, 35),
            TextureFactory.of(ItemAndBlockHolder.INTERFACE),

            (s, tex) -> new AECover(s, InterfaceP2PData.class, tex),
            AECover.placer());
        registerCover(
            new ItemStack(MyMod.cover, 1, 36),
            new DeferredGetterTexture(() -> MyMod.block_euinterface, ForgeDirection.UP, 0, Dyes._NULL.mRGBa, false),

            (s, tex) -> new AECover(s, InterfaceP2PEUData.class, tex),
            AECover.placer());
        registerCover(
            new ItemStack(MyMod.cover, 1, 37),

            TextureFactory.of(MyMod.iohub, 0X7F)

            ,

            (s, tex) -> new AECover(s, BridgingData.class, tex),
            null);

        for (int i = 0; i <= 4; i++) {
            registerCover(
                new ItemStack(MyMod.cover, 1, 90 + i),
                TextureFactory.of(
                    MACHINE_CASINGS[1][0],
                    TextureFactory.of(gregtech.api.enums.Textures.BlockIcons.OVERLAY_SCREEN_GLOW)),
                (s, tex) -> new CircuitHolderCover(s, scre),
                null);
        } ;

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
                6);
            new RemoteInputHatch(
                Config.metaTileEntityOffset + RemoteInputHatchOffset,
                "hatch.input.fluid.remote",
                LangManager.translateToLocal("hatch.input.fluid.remote.name"),
                6);
        }
        // int tier = 8;
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
            24,
            true);
        new PatternDualInputHatch(
            Config.metaTileEntityOffset + PatternOffsetBus,
            "hatch.input.buffered.me.itemonly",
            LangManager.translateToLocal("hatch.input.buffered.me.itemonly.name"),
            10,
            true,
            24,
            false) {

            public boolean supportsFluids() {
                super.supportsFluids();
                return false;
            }
        };

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + TenaciousOffset + i,
            "hatch.output.tenacious." + i,
            LangManager.translateToLocalFormatted("hatch.output.tenacious.name", GTValues.VN[i]),
            i,
            true);

        for (int i = 0; i < 4; i++) new FilterOutputBus(
            Config.metaTileEntityOffset + FilterOffset + i,
            "hatch.output.filter." + i,
            LangManager.translateToLocalFormatted("hatch.output.filter.name", GTValues.VN[i]),
            i,
            false);

        /*
         * new RecipeCheckResultDetector(
         * Config.metaTileEntityOffset + RecipeCheckResultDetectorOffset,
         * "recipe_check_result_detector",
         * LangManager.translateToLocal("recipe_check_result_detector.name"),
         * 0);
         */
        for (int[] i : IngredientBuffer.tiers) new IngredientBuffer(
            Config.metaTileEntityOffset + IngBufferOffset + i[0],
            "buffer.ingredientbuffer." + i[0],
            LangManager.translateToLocal("buffer.ingredientbuffer.name." + i[0]),
            i[1],
            new String[] {});

        /*
         * new MultiblockProxy(
         * Config.metaTileEntityOffset + MBProxyOffset,
         * "multimachine.mbproxy",
         * LangManager.translateToLocalFormatted("multimachine.mbproxy.name"));
         */
        new LargeProgrammingCircuitProvider(
            Config.metaTileEntityOffset + LargeProviderOffset,
            "multimachine.largeprogrammingcircuit",
            LangManager.translateToLocalFormatted("multimachine.largeprogrammingcircuitprovider.name"));
        new ProviderChainer(
            Config.metaTileEntityOffset + ChainerOffset,
            "providerchainer",
            LangManager.translateToLocal("providerchainer.name"),
            4,
            0);
        for (int i = 1; i <= 10; i++) new SuperChestME(
            Config.metaTileEntityOffset + MEChest + i - 1,
            "mesuperchest." + i,
            SuperChestME.name(i),
            i,
            1 + 16);
        for (int i = 1; i <= 10; i++) new SuperTankME(
            Config.metaTileEntityOffset + METank + i - 1,
            "mesupertank." + i,
            SuperTankME.name(i),
            i,
            1 + 16);
        new IngredientDistributor(
            Config.metaTileEntityOffset + IngredientDistributorOffset,
            "multimachine.ingredientdistributor",
            LangManager.translateToLocalFormatted("multimachine.ingredientdistributor.name"));

        for (int i = 0; i <= 5; i++) new ProgrammingCircuitProviderPrefabricated(
            Config.metaTileEntityOffset + PrefabOffset + i,
            "circuitprovider.prefab." + i,
            LangManager.translateToLocal("circuitprovider.prefab." + i + ".name"),
            4,
            0,
            i);

        for (int i = 0; i < 4; i++) new MultiCircuitInputBus(
            Config.metaTileEntityOffset + MultiCircuitBusOffset + i,
            "bus.input.multicircuit." + i,
            LangManager.translateToLocalFormatted("bus.input.multicircuit.name", GTValues.VN[i]),
            i + 4);

        new DataHatchME(
            Config.metaTileEntityOffset + DataHatchMEOffset,
            "data.input.me",
            LangManager.translateToLocalFormatted("data.input.me.name")

        );

        new RestrictedInputHatchME(
            Config.metaTileEntityOffset + RestrictedHatchME,
            false,
            "fluid.input.me.restriced",
            LangManager.translateToLocalFormatted("fluid.input.me.restriced.name")

        );
        new RestrictedInputBusME(
            Config.metaTileEntityOffset + RestrictedBusME,
            false,
            "fluid.input.bus.me.restriced",
            LangManager.translateToLocalFormatted("fluid.input.bus.me.restriced.name")

        );
        new DualInputHachOC(
            Config.metaTileEntityOffset + DualInputHatchOCOffset,
            "hatch.input.dual.oc",
            LangManager.translateToLocalFormatted("hatch.input.dual.oc"),
            10,
            true

        );
        new DualInputHatchInventoryMappingSlave<>(
            Config.metaTileEntityOffset + MappingSlaveOffset,
            "hatch.dualinput.slave.inv.mapping",
            LangManager.translateToLocal("hatch.dualinput.slave.inv.mapping.name"),
            6);

        new PatternDualInputHatchInventoryMappingSlave<>(
            Config.metaTileEntityOffset + PatternMappingSlaveOffset,
            "hatch.dualinput.slave.inv.mapping.me",
            LangManager.translateToLocal("hatch.dualinput.slave.inv.mapping.me.name"),
            6);

        new DualInputHatchSlaveBus<>(
            Config.metaTileEntityOffset + SlaveBusOffset,
            "hatch.dualinput.slave.item",
            LangManager.translateToLocal("hatch.dualinput.slave.item.name"));
        new DualInputHatchSlaveHatch<>(
            Config.metaTileEntityOffset + SlaveHatchOffset,
            "hatch.dualinput.slave.fluid",
            LangManager.translateToLocal("hatch.dualinput.slave.fluid.name"));

        new CommunicationPortHatch(
            Config.metaTileEntityOffset + CommunicationPortHatchOffset,
            "hatch.commport",
            LangManager.translateToLocalFormatted("hatch.commport.name"),
            4

        );
        new DecoyInputBusME(
            Config.metaTileEntityOffset + DecoyBusME,
            "fluid.input.bus.me.decoy",
            LangManager.translateToLocalFormatted("fluid.input.bus.me.decoy.name")

        );
        new DecoyInputHatchME(
            Config.metaTileEntityOffset + DecoyHatchME,
            "fluid.input.me.decoy",
            LangManager.translateToLocalFormatted("fluid.input.me.decoy.name")

        );
        new PriorityFilterInputBusME(
            Config.metaTileEntityOffset + PFilterBusME,
            "fluid.input.bus.me.pfilter",
            LangManager.translateToLocalFormatted("fluid.input.bus.me.pfilter.name")

        );
        new PriorityFilterInputHatchME(
            Config.metaTileEntityOffset + PFilterHatchME,
            "fluid.input.me.pfilter",
            LangManager.translateToLocalFormatted("fluid.input.me.pfilter.name")

        );

        new VoidOutputHatch(
            Config.metaTileEntityOffset + VHatch,
            "ph.hatch.void",
            LangManager.translateToLocalFormatted("ph.hatch.void.name"),
            5

        );
        new VoidOutputBus(
            Config.metaTileEntityOffset + VBus,
            "ph.bus.void",
            LangManager.translateToLocalFormatted("ph.bus.void.name"),
            5);

        new StorageOutputBus(
            Config.metaTileEntityOffset + StorageOutputBus,

            "output.bus.storage",
            LangManager.translateToLocalFormatted("output.bus.storage.name")

        );

        new StorageOutputHatch(
            Config.metaTileEntityOffset + StorageOutputHatch,

            "output.hacth.storage",
            LangManager.translateToLocalFormatted("output.hacth.storage.name")

        );

        new WaterProvider(
            Config.metaTileEntityOffset + WaterProviderOffset,

            "provider.water",
            LangManager.translateToLocalFormatted("provider.water.name"),
            6

        );
        new NBTHatchMECatalyst(
            Config.metaTileEntityOffset + MECatalystOffset,
            "input.catalyst.me",
            LangManager.translateToLocal("input.catalyst.me.name"));
        new NBTHatchMEFocus(
            Config.metaTileEntityOffset + MEFocusOffset,
            "input.focus.me",
            LangManager.translateToLocal("input.focus.me.name"));
        new AutoDataOrbHatch(
            Config.metaTileEntityOffset + DataOrbOffset,
            "input.dataorb",
            LangManager.translateToLocal("input.dataorb.name"),
            10);
        new StockingDualInputHatchME(
            Config.metaTileEntityOffset + StockingDualInputOffset,
            "hatch.dualinput.stocking.me",
            LangManager.translateToLocal("hatch.dualinput.stocking.me.name"),
            8,
            false);
        new StockingDualInputHatchME(
            Config.metaTileEntityOffset + StockingDualInputOffset + 1,
            "hatch.dualinput.stocking.autopull.me",
            LangManager.translateToLocal("hatch.dualinput.stocking.autopull.me.name"),
            10,
            true);
        new PhantomInputBus(
            Config.metaTileEntityOffset + PhantomInputBusOffset,
            "phantom.bus",
            LangManager.translateToLocal("phantom.bus.name"),
            10);
        /*
         * for (int i = 0; i < 4; i++) {
         * new DualInputHatch(
         * Config.metaTileEntityOffset + MultiCircuitDualInputHatchOffset + i,
         * "hatch.input.multicircuit.dual.tier." + (i+4),
         * String.format(LangManager.translateToLocal("hatch.input.multicircuit.dual.tier.name"), GTValues.VN[i+4]),
         * i+4,
         * false,true);
         * }
         * for (int i = 0; i < 4; i++) {
         * new DualInputHatch(
         * Config.metaTileEntityOffset + MultiCircuitQuadDualInputHatchOffset + i,
         * "hatch.input.multicircuit.dual.quad.tier." + (i+4),
         * String.format(LangManager.translateToLocal("hatch.input.multicircuit.dual.quad.tier.name"),
         * GTValues.VN[i+4]),
         * i,
         * true,true);
         * }
         */
        /*
         * new PatternHousing(
         * Config.metaTileEntityOffset + PatternHousingOffset,
         * "hatch.patternhousing",
         * LangManager.translateToLocalFormatted("hatch.patternhousing.name"), 0);
         */
    }

    private Method registerCover;

    private void registerCover(ItemStack itemStack, ITexture of,
        BiFunction<gregtech.api.covers.CoverContext, ITexture, Cover> cv,

        CoverPlacer x) {

        if (x == null) CoverRegistry.registerCover(itemStack, of, s -> { return cv.apply(s, of); });
        if (x != null) CoverRegistry.registerCover(itemStack, of, s -> { return cv.apply(s, of); }, x);

    }

    public class DeferredGetterTexture extends GTCopiedBlockTextureRender {

        private IIcon getIcon(int ordinalSide) {

            return getBlock().getIcon(ordinalSide, 0);
        }

        @Override
        public void renderXPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            final IIcon aIcon = getIcon(ForgeDirection.EAST.ordinal());
            aRenderer.field_152631_f = true;
            startDrawingQuads(aRenderer, 1.0f, 0.0f, 0.0f);
            new LightingHelper(aRenderer).setupLightingXPos(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.EAST, 0xffffff);
            aRenderer.renderFaceXPos(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
            aRenderer.field_152631_f = false;
        }

        @Override
        public void renderXNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            startDrawingQuads(aRenderer, -1.0f, 0.0f, 0.0f);
            final IIcon aIcon = getIcon(ForgeDirection.WEST.ordinal());
            new LightingHelper(aRenderer).setupLightingXNeg(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.WEST, 0xffffff);
            aRenderer.renderFaceXNeg(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
        }

        @Override
        public void renderYPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            startDrawingQuads(aRenderer, 0.0f, 1.0f, 0.0f);
            final IIcon aIcon = getIcon(ForgeDirection.UP.ordinal());
            new LightingHelper(aRenderer).setupLightingYPos(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.UP, 0xffffff);
            aRenderer.renderFaceYPos(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
        }

        @Override
        public void renderYNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            startDrawingQuads(aRenderer, 0.0f, -1.0f, 0.0f);
            final IIcon aIcon = getIcon(ForgeDirection.DOWN.ordinal());
            new LightingHelper(aRenderer).setupLightingYNeg(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.DOWN, 0xffffff);
            aRenderer.renderFaceYNeg(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
        }

        @Override
        public void renderZPos(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            startDrawingQuads(aRenderer, 0.0f, 0.0f, 1.0f);
            final IIcon aIcon = getIcon(ForgeDirection.SOUTH.ordinal());
            new LightingHelper(aRenderer).setupLightingZPos(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.SOUTH, 0xffffff);
            aRenderer.renderFaceZPos(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
        }

        @Override
        public void renderZNeg(RenderBlocks aRenderer, Block aBlock, int aX, int aY, int aZ) {
            startDrawingQuads(aRenderer, 0.0f, 0.0f, -1.0f);
            final IIcon aIcon = getIcon(ForgeDirection.NORTH.ordinal());
            aRenderer.field_152631_f = true;
            new LightingHelper(aRenderer).setupLightingZNeg(aBlock, aX, aY, aZ)
                .setupColor(ForgeDirection.NORTH, 0xffffff);
            aRenderer.renderFaceZNeg(aBlock, aX, aY, aZ, aIcon);
            draw(aRenderer);
            aRenderer.field_152631_f = false;
        }

        protected DeferredGetterTexture(Supplier<Block> aBlock, ForgeDirection up, int aMeta, short[] aRGBa,
            boolean allowAlpha) {
            super(null, up.ordinal(), aMeta, aRGBa, allowAlpha);
            block = aBlock;
        }

        Supplier<Block> block;

        @Override
        public boolean isValidTexture() {
            return true;
        }

        @Override
        public Block getBlock() {
            return block.get();
        }

    }
}
