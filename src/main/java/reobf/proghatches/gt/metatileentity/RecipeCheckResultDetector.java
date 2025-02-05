package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.ITEM_IN_SIGN;
import static gregtech.api.enums.Textures.BlockIcons.OVERLAY_PIPE_IN;

import java.lang.reflect.Field;
import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.google.common.collect.ImmutableMap;

import gregtech.GTMod;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchInputBus;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.main.registration.Registration;

// spotless:off
@Deprecated
public class RecipeCheckResultDetector extends MTEHatchInputBus implements IRecipeProcessingAwareHatch {

    @Override
    public boolean onRightclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {

        return false;// no gui!
    }

    public RecipeCheckResultDetector(int id, String name, String nameRegional, int tier) {
        super(
            id,
            name,
            nameRegional,
            tier,
            1,

            reobf.proghatches.main.Config.get("RCRD", ImmutableMap.of()));
        Registration.items_eucrafting.add(new ItemStack(GregTechAPI.sBlockMachines, 1, id));

    }

    public RecipeCheckResultDetector(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 1, aDescription, aTextures);
    }

    @Override
    public void updateSlots() {

        // no-op
    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new RecipeCheckResultDetector(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public void startRecipeProcessing() {}

    /**
     * 0 no_recipe
     * 1 running fine
     * 2 other fail
     */
    private int lastSuccess;

    private int check(CheckRecipeResult crr) {
        if (crr.wasSuccessful()) return 1;
        if (crr == CheckRecipeResultRegistry.NO_RECIPE) return 0;
        return 2;
    }

    private static Field f;
    private int pulses;

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        if (aNBT.hasKey("x") == false) return;
        super.loadNBTData(aNBT);
        lastSuccess = aNBT.getInteger("lastSuccess");
        pulses = aNBT.getInteger("pulses");

    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {

        super.saveNBTData(aNBT);
        aNBT.setInteger("lastSuccess", lastSuccess);
        aNBT.setInteger("pulses", pulses);
    }

    static {
        f = Arrays.stream(MTEMultiBlockBase.class.getDeclaredFields())
            .filter(s -> s.getType() == CheckRecipeResult.class)
            .findAny()
            .get();
        f.setAccessible(true);

    }

    public void start(int lastSuccess, int newSuccess) {

        if (lastSuccess == 1 && newSuccess == 0) {
            pulses++;
        }
    }

    @Override
    public boolean allowGeneralRedstoneOutput() {

        return true;
    }

    @Override
    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTimer) {
        super.onPostTick(aBaseMetaTileEntity, aTimer);
        if (pulses > 0) {
            pulses--;
            BaseMetaTileEntity j;

            aBaseMetaTileEntity.setInternalOutputRedstoneSignal(aBaseMetaTileEntity.getFrontFacing(), (byte) 15);

        } else {

            aBaseMetaTileEntity.setInternalOutputRedstoneSignal(aBaseMetaTileEntity.getFrontFacing(), (byte) 0);

        }
    }

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN), TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return GTMod.gregtechproxy.mRenderIndicatorsOnHatch
            ? new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN), TextureFactory.of(ITEM_IN_SIGN) }
            : new ITexture[] { aBaseTexture, TextureFactory.of(OVERLAY_PIPE_IN) };
    }

    // TODO: call this via mixin @Return, because other ProcessingAwareHatch might fail the recipecheck
    @Override
    public CheckRecipeResult endRecipeProcessing(MTEMultiBlockBase controller) {
        try {
            CheckRecipeResult res = (CheckRecipeResult) f.get(controller);
            int newSuccess = check(res);
            start(lastSuccess, newSuccess);
            lastSuccess = newSuccess;
        } catch (Exception e) {

            e.printStackTrace();

        }

        return CheckRecipeResultRegistry.SUCCESSFUL;
    }

}
