package reobf.proghatches.gt.metatileentity;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Predicate;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.SignedBytes;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import appeng.util.Platform;
import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchOutputBus;
import gregtech.api.util.GTUtility;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch.EntityDropParticleFX;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.VoidFXMessage;

public class VoidOutputBus extends MTEHatchOutputBus {

    public VoidOutputBus(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures) {
        super(mName, mTier, 0, mDescriptionArray, mTextures);

    }

    ItemStack[] filter = new ItemStack[16];
    @SideOnly(value = Side.CLIENT)
    LinkedList<ItemStack> toDisplay;// =new LinkedList<>();

    Predicate<ItemStack> filterPredicate = (s) -> false;

    public void rebuildFilter() {
        filterPredicate = (s) -> false;
        for (ItemStack is : filter) {
            if (is != null) {
                filterPredicate = filterPredicate.or(s -> { return Platform.isSameItemPrecise(is, s); });
            }

        }
    }

    ItemStackHandler handler = new ItemStackHandler(filter) {

        protected void onContentsChanged(int slot) {
            rebuildFilter();
        }

    };

    @Override
    public void addUIWidgets(Builder builder, UIBuildContext buildContext) {

        builder.widget(
            SlotGroup.ofItemHandler(handler, 4)
                .startFromSlot(0)
                .endAtSlot(15)
                .background(getGUITextureSet().getItemSlot())
                .slotCreator(i -> new BaseSlot(handler, i, true) {

                    @Override
                    public ItemStack getStack() {
                        return isEnabled() ? super.getStack() : null;
                    }

                    /*
                     * @Override
                     * public boolean isEnabled() {
                     * return mChannel != null;
                     * }
                     */
                })
                .build()
                .setPos(3, 3));
    }

    public VoidOutputBus(int aID, String aName, String aNameRegional, int tier) {
        super(aID, aName, aNameRegional, tier, reobf.proghatches.main.Config.get("VOB", ImmutableMap.of()), 0

        );

        Registration.items.add(new ItemStack(GregTechAPI.sBlockMachines, 1, aID));
    }

    @Override
    public VoidOutputBus newMetaEntity(IGregTechTileEntity aTileEntity) {

        return new VoidOutputBus(mName, mTier, mDescriptionArray, mTextures);
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
        super.loadNBTData(compound);
        NBTTagList nbttaglist = compound.getTagList("Items", 10);
        Arrays.fill(this.filter, null);

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.filter.length) {
                this.filter[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }

        NBTTagList nbttaglist0 = compound.getTagList("Fluids", 10);
        rebuildFilter();
        if (compound.hasKey("fx")) fx = compound.getBoolean("fx");
    }

    @Override
    public void saveNBTData(NBTTagCompound compound) {
        super.saveNBTData(compound);
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.filter.length; ++i) {
            if (this.filter[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                this.filter[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
        nbttaglist = new NBTTagList();

        compound.setBoolean("fx", fx);
    }

    public boolean dump(ItemStack aStack) {
        boolean b = filterPredicate.test(aStack);
        if (b && fx) MyMod.net.sendToAllAround(

            new VoidFXMessage(this.getBaseMetaTileEntity(), aStack),
            new TargetPoint(
                this.getBaseMetaTileEntity()
                    .getWorld().provider.dimensionId,
                this.getBaseMetaTileEntity()
                    .getXCoord(),
                this.getBaseMetaTileEntity()
                    .getYCoord(),
                this.getBaseMetaTileEntity()
                    .getZCoord(),
                64));

        return b;
    }

    @SideOnly(Side.CLIENT)
    long remainticks;
    @SideOnly(Side.CLIENT)
    LinkedList<ItemStack> types;

    @SideOnly(Side.CLIENT)
    public void addVisual(ItemStack f) {
        if (types == null) types = new LinkedList<>();
        remainticks = 40;// (f.amount);
        types.add(f);
        if (types.size() > 20) types.removeLast();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (types == null) types = new LinkedList<>();
        /*
         * MyMod.net.sendToDimension(new VoidFXMessage(
         * this.getBaseMetaTileEntity(), new FluidStack(FluidRegistry.LAVA, 1000)
         * ),this.getBaseMetaTileEntity().getWorld().provider.dimensionId);
         * try{
         * MyMod.net.sendToDimension(new VoidFXMessage(
         * this.getBaseMetaTileEntity(), new FluidStack(FluidRegistry.getFluid("liquid_drillingfluid"), 1000)
         * ),this.getBaseMetaTileEntity().getWorld().provider.dimensionId);
         * }catch(Exception e){}
         */
        if (remainticks > 0 && aBaseMetaTileEntity.getWorld().isRemote) {
            // System.out.println("ss");
            remainticks--;
            if (remainticks == 0) {
                types.clear();
                return;
            } ;
            /*
             * for(long k=1;k<Integer.MAX_VALUE;k=k*100)
             * if(remainticks>k*100){
             * remainticks-=k;
             * }
             */

            ItemStack f = types.get((int) (types.size() * Math.random()));

            ForgeDirection fc = aBaseMetaTileEntity.getFrontFacing();
            EntityDropParticleFX fx = new EntityDropParticleFX(
                Minecraft.getMinecraft().theWorld,

                aBaseMetaTileEntity.getXCoord() + 0.5D + (fc.offsetX) * 0.51f,
                aBaseMetaTileEntity.getYCoord() + 0.5D + (fc.offsetY) * 0.51f,
                aBaseMetaTileEntity.getZCoord() + 0.5D + (fc.offsetZ) * 0.51f,
                f);
            fx.motionX = (fc.offsetX) * 0.3 + (Math.random() - Math.random()) * 0.1;
            fx.motionY = (fc.offsetY) * 0.3 + (Math.random() - Math.random()) * 0.1;
            fx.motionZ = (fc.offsetZ) * 0.3 + (Math.random() - Math.random()) * 0.1;

            Minecraft.getMinecraft().effectRenderer.addEffect((EntityFX) fx);

        }

        super.onPreTick(aBaseMetaTileEntity, aTick);
    }

    @SideOnly(Side.CLIENT)
    public class EntityDropParticleFX extends EntityFX {

        private Render itemRenderer;

        {
            itemRenderer = new RenderItem() {

                @Override
                public byte getMiniBlockCount(ItemStack stack, byte original) {
                    return SignedBytes.saturatedCast(Math.min(stack.stackSize / 32, 15) + 1);
                }

                @Override
                public byte getMiniItemCount(ItemStack stack, byte original) {
                    return SignedBytes.saturatedCast(Math.min(stack.stackSize / 32, 7) + 1);
                }

                @Override
                public boolean shouldBob() {
                    return false;
                }

                @Override
                public boolean shouldSpreadItems() {
                    return false;
                }
            };
            itemRenderer.setRenderManager(RenderManager.instance);
        }
        /** the material type for dropped items/blocks */
        private Material materialType;
        /** The height of the current bob */
        private int bobTimer;
        // private static final String __OBFID = "CL_00000901";
        ItemStack is;

        public EntityDropParticleFX(World worldIn, double p_i1203_2_, double p_i1203_4_, double p_i1203_6_,
            ItemStack f) {
            super(worldIn, p_i1203_2_, p_i1203_4_, p_i1203_6_, 0.0D, 0.0D, 0.0D);
            this.motionX = this.motionY = this.motionZ = 0.0D;
            is = f;

            this.particleBlue = 0xFF;
            this.particleGreen = 0xFF;
            this.particleRed = 0xFF;

            setParticleIcon(
                f.getItem()
                    .getIcon(
                        f,
                        (int) (f.getItem()
                            .getRenderPasses(f.getItemDamage()) * Math.random()))

            );

            // this.setParticleTextureIndex(113);
            this.setSize(0.01F, 0.01F);
            this.particleGravity = 0.06F;
            // this.materialType = p_i1203_8_;
            this.bobTimer = 00;
            this.particleMaxAge = (int) (64.0D / (Math.random() * 0.8D + 0.2D));
            this.motionX = this.motionY = this.motionZ = 0.0D;
            particleMaxAge = 100;
        }

        @Override
        public int getFXLayer() {
            return 1;
        }

        public int getBrightnessForRender(float p_70070_1_) {
            return this.materialType == Material.water ? super.getBrightnessForRender(p_70070_1_) : 257;
        }

        /**
         * Gets how bright this entity is.
         */
        public float getBrightness(float p_70013_1_) {
            return this.materialType == Material.water ? super.getBrightness(p_70013_1_) : 1.0F;
        }

        /**
         * Called to update the entity's position/logic.
         */
        public void onUpdate() {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;

            /*
             * if (this.materialType == Material.water)
             * {
             * this.particleRed = 0.2F;
             * this.particleGreen = 0.3F;
             * this.particleBlue = 1.0F;
             * }
             * else
             * {
             * this.particleRed = 1.0F;
             * this.particleGreen = 16.0F / (float)(40 - this.bobTimer + 16);
             * this.particleBlue = 4.0F / (float)(40 - this.bobTimer + 8);
             * }
             */
            this.motionY -= (double) this.particleGravity;

            if (this.bobTimer-- > 0) {
                this.motionX *= 0.02D;
                this.motionY *= 0.02D;
                this.motionZ *= 0.02D;
                // this.setParticleTextureIndex(113);
                // this.setParticleTextureIndex(19 + this.rand.nextInt(4));
            } else {
                // this.setParticleTextureIndex(113);
                // this.setParticleTextureIndex(19 + this.rand.nextInt(4));
            }

            this.moveEntity(this.motionX, this.motionY, this.motionZ);
            this.motionX *= 0.9800000190734863D;
            this.motionY *= 0.9800000190734863D;
            this.motionZ *= 0.9800000190734863D;

            if (this.particleMaxAge-- <= 0) {
                this.setDead();
            }

            if (this.onGround) {
                this.c = 0;
                this.a = 90;
                this.a1 = 0;
                this.c1 = 0;
                this.b1 *= 0.8;
                // this.setDead();

                /*
                 * if (this.materialType == Material.water)
                 * {
                 * else
                 * {
                 * this.setParticleTextureIndex(114);
                 * }
                 */
                //
                // this.worldObj.spawnParticle("splash", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);

                this.motionX *= 0.699999988079071D;
                this.motionZ *= 0.699999988079071D;
            }

            Material material = this.worldObj
                .getBlock(
                    MathHelper.floor_double(this.posX),
                    MathHelper.floor_double(this.posY),
                    MathHelper.floor_double(this.posZ))
                .getMaterial();

            if (material.isLiquid() || material.isSolid()) {
                double d0 = (double) ((float) (MathHelper.floor_double(this.posY) + 1)
                    - BlockLiquid.getLiquidHeightPercent(
                        this.worldObj.getBlockMetadata(
                            MathHelper.floor_double(this.posX),
                            MathHelper.floor_double(this.posY),
                            MathHelper.floor_double(this.posZ))));

                if (this.posY < d0) {
                    this.setDead();
                }
            }
        }

        double a = Math.random() * 360;
        double b = Math.random() * 360;
        double c = Math.random() * 360;
        double a1 = Math.random() * 360;
        double b1 = Math.random() * 360;
        double c1 = Math.random() * 360;

        @Override
        public void renderParticle(Tessellator tessellator, float timeStep, float rotationX, float rotationXZ,
            float rotationZ, float rotationYZ, float rotationXY) {
            tessellator.draw();

            GL11.glPushMatrix();
            EntityClientPlayerMP p = Minecraft.getMinecraft().thePlayer;
            float f11 = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) timeStep);
            float f12 = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) timeStep);
            float f13 = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) timeStep);
            float f21 = (float) (p.prevPosX + (p.posX - p.prevPosX) * (double) timeStep);
            float f22 = (float) (p.prevPosY + (p.posY - p.prevPosY) * (double) timeStep);
            float f23 = (float) (p.prevPosZ + (p.posZ - p.prevPosZ) * (double) timeStep);
            GL11.glTranslated(f11 - f21, f12 - f22, f13 - f23

            );

            GL11.glRotated(a, 1, 0, 0);
            GL11.glRotated(b, 0, 1, 0);
            GL11.glRotated(c, 0, 0, 1);
            a += (a1 - b1) / 100;
            b += (b1 - c1) / 100;
            c += (c1 - a1) / 100;

            EntityItem customitem = new EntityItem(Minecraft.getMinecraft().theWorld);
            customitem.hoverStart = 0f;
            customitem.setEntityItemStack(is);
            itemRenderer.doRender(customitem, 0, 0, 0, 0, 0);

            GL11.glPopMatrix();

            tessellator.startDrawingQuads();
        }

    }

    boolean fx = true;

    @Override
    public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
        ItemStack aTool) {

        if (!getBaseMetaTileEntity().getCoverAtSide(side)
            .isGUIClickable()) return;
        fx = !fx;
        GTUtility.sendChatToPlayer(aPlayer, StatCollector.translateToLocal("proghatches.gt.void.fx." + fx));

    }

}
