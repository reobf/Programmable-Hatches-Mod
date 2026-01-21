package reobf.proghatches.gt.metatileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatch;
import gregtech.api.render.TextureFactory;
import reobf.proghatches.main.registration.Registration;

public class CommunicationPortHatch extends MTEHatch {

    public CommunicationPortHatch(int aID, String aName, String aNameRegional, int aTier) {
        super(aID, aName, aNameRegional, aTier, 0, new String[0]);
        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
    }

    public CommunicationPortHatch(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
        super(aName, aTier, 0, aDescription, aTextures);

    }

    @Override
    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new CommunicationPortHatch(mName, mTier, mDescriptionArray, mTextures);
    }

    private static final IIconContainer textureFont = new Textures.BlockIcons.CustomIcon("icons/NeutronSensorFont");
    private static final IIconContainer textureFont_Glow = new Textures.BlockIcons.CustomIcon(
        "icons/NeutronSensorFont_GLOW");

    @Override
    public ITexture[] getTexturesActive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(textureFont), TextureFactory.builder()
            .addIcon(textureFont_Glow)
            .glow()
            .build() };
    }

    @Override
    public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
        return new ITexture[] { aBaseTexture, TextureFactory.of(textureFont) };
    }

    @Override
    public boolean isValidSlot(int aIndex) {
        return false;
    }

    @Override
    public boolean isFacingValid(ForgeDirection facing) {
        return true;
    }

    @Override
    public boolean allowGeneralRedstoneOutput() {
        return true;
    }

    @Override
    public boolean isAccessAllowed(EntityPlayer aPlayer) {
        return true;
    }

    public void setRS(boolean porton) {

        for (ForgeDirection s : ForgeDirection.values()) this.getBaseMetaTileEntity()
            .setInternalOutputRedstoneSignal(s, (byte) (porton ? 15 : 0));

        ((BaseMetaTileEntity) this.getBaseMetaTileEntity()).updateNeighbours(0xff, 0xff);// set all bits to 1 to update
                                                                                         // all 6 sides

    }

										    @Override
										    public void initDefaultModes(NBTTagCompound aNBT) {
										        for (ForgeDirection s : ForgeDirection.values()) this.getBaseMetaTileEntity()
            .setInternalOutputRedstoneSignal(s, (byte) 0);
    }

}
