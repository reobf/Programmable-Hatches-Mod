package reobf.proghatches.gt.metatileentity;

import static gregtech.api.util.GT_Utility.moveMultipleItemStacks;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Predicate;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import appeng.util.Platform;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTech_API;
import gregtech.api.enums.ParticleFX;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Output;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_OutputBus;
import gregtech.api.util.GT_Utility;
import gregtech.common.GT_Client;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityBreakingFX;
import net.minecraft.client.particle.EntityDropParticleFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.block.ChunkTrackingGridCahce;
import reobf.proghatches.block.IChunkTrackingGridCahce;
import reobf.proghatches.gt.metatileentity.util.MappingFluidTank;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.net.VoidFXMessage;
import reobf.proghatches.net.WayPointMessage;
import reobf.proghatches.util.ProghatchesUtil;

public class VoidOutputHatch  extends GT_MetaTileEntity_Hatch_Output {

	public VoidOutputHatch(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures
			) {
		super(mName, mTier,0, mDescriptionArray, mTextures);
		
	}
    FluidStack[] filter=new FluidStack[16];
   
    
    
	Predicate<FluidStack> filterPredicate= (s)->false;
    
    
    public void rebuildFilter(){
    filterPredicate= (s)->false;
    	for( FluidStack is: filter){
    		if(is!=null){
    			filterPredicate=filterPredicate.or(
    					s->{
    						return s!=null&&is.getFluid().equals(s.getFluid());
    					}
    					);
    		}
    		
    	}
    }
    
    
  
    
	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		
		for( int ii=0;ii<16;ii++){
			final int i=ii;
		 builder.widget(FluidSlotWidget.phantom(new MappingFluidTank(s->{filter[i]=s==null?null:s.copy();rebuildFilter();}, ()->filter[i]), 
				 false
				 ).setPos(3+(i%4)*16, 3+(i/4)*16));
				
				 ;}
		
	/*	
		builder .widget(
                 SlotGroup.ofItemHandler(handler, 4).startFromSlot(0).endAtSlot(15)
                         .background(getGUITextureSet().getItemSlot())
                         .slotCreator(i -> new BaseSlot(handler, i, true) {

                             @Override
                             public ItemStack getStack() {
                                 return isEnabled() ? super.getStack() : null;
                             }

                     
                         }).build().setPos(7, 24));*/
	}

	public VoidOutputHatch(int aID, String aName, String aNameRegional, int tier) {
		super(aID, aName, aNameRegional, tier,
				
				
				reobf.proghatches.main.Config.get("VOH",
				ImmutableMap.of())
				
				
				,0
		

		);
		
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, aID));
	}



	@Override
	public VoidOutputHatch newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new VoidOutputHatch(mName, mTier, mDescriptionArray, mTextures );
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		super.loadNBTData(compound);
		NBTTagList nbttaglist = compound.getTagList("Items", 10);
	        Arrays.fill(this.filter,null);

	       

	        for (int i = 0; i < nbttaglist.tagCount(); ++i)
	        {
	            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
	            int j = nbttagcompound1.getByte("Slot") & 255;

	            if (j >= 0 && j < this.filter.length)
	            {
	                this.filter[j] = FluidStack.loadFluidStackFromNBT(nbttagcompound1);
	            }
	        }
	        
	        NBTTagList nbttaglist0 = compound.getTagList("Fluids", 10);
	        rebuildFilter();
	        if(compound.hasKey("fx"))
	 	       fx=compound.getBoolean("fx");
	}
	
	@Override
	public void saveNBTData(NBTTagCompound compound) {
		super.saveNBTData(compound);
		NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.filter.length; ++i)
        {
            if (this.filter[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.filter[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        compound.setTag("Items", nbttaglist);
         nbttaglist = new NBTTagList();
         compound.setBoolean("fx", fx);
   
	}

	public boolean dump(FluidStack aStack) {
		boolean b= filterPredicate.test(aStack);
		
		
		if(b&&fx)MyMod.net.sendToDimension(new VoidFXMessage(
			this.getBaseMetaTileEntity(), aStack
				),this.getBaseMetaTileEntity().getWorld().provider.dimensionId);
		
		return b;
	}
	  public boolean outputsSteam() {
	        return false;
	    }

	    public boolean outputsLiquids() {
	    	 return false;
	    }

	    public boolean outputsItems() {
	    	 return false;
	    }

@Override
public boolean doesFillContainers() {
    return false;
}

@Override
public boolean doesEmptyContainers() {
    return false;
}



@SideOnly(Side.CLIENT)
long remainticks;
@SideOnly(Side.CLIENT)
LinkedList<FluidStack> types;
@SideOnly(Side.CLIENT)
public void addVisual(FluidStack f){
	if(types==null)types=new LinkedList<>();
	remainticks=40;//(f.amount);
	types.add(f);
	if(types.size()>20)types.removeLast();
}
@Override@SideOnly(Side.CLIENT)
public void onPreTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
	if(types==null)types=new LinkedList<>();
	/*MyMod.net.sendToDimension(new VoidFXMessage(
			this.getBaseMetaTileEntity(), new FluidStack(FluidRegistry.LAVA, 1000)
				),this.getBaseMetaTileEntity().getWorld().provider.dimensionId);
	
	try{
	MyMod.net.sendToDimension(new VoidFXMessage(
			this.getBaseMetaTileEntity(), new FluidStack(FluidRegistry.getFluid("liquid_drillingfluid"), 1000)
				),this.getBaseMetaTileEntity().getWorld().provider.dimensionId);
	}catch(Exception e){}
	*/
	if(remainticks>0&&aBaseMetaTileEntity.getWorld().isRemote){
		//System.out.println("ss");
		remainticks--;
		if(remainticks==0){types.clear();return;}
		/*for(long k=1;k<Integer.MAX_VALUE;k=k*100)
		if(remainticks>k*100){
			remainticks-=k;
		}*/
		
		FluidStack f=types.get((int) (types.size()*Math.random()));
		int col=f.getFluid().getColor();
		ForgeDirection fc = aBaseMetaTileEntity.getFrontFacing();
		EntityDropParticleFX fx=new EntityDropParticleFX(Minecraft.getMinecraft().theWorld,
				
				aBaseMetaTileEntity.getXCoord()+0.5D+(fc.offsetX)*0.51f,
				aBaseMetaTileEntity.getYCoord()+0.5D+(fc.offsetY)*0.51f,
				aBaseMetaTileEntity.getZCoord()+0.5D+(fc.offsetZ)*0.51f,
				f.getFluid());
		fx.motionX=(fc.offsetX)*0.3+(Math.random()-Math.random())*0.1;
		fx.motionY=(fc.offsetY)*0.3+(Math.random()-Math.random())*0.1;
		fx.motionZ=(fc.offsetZ)*0.3+(Math.random()-Math.random())*0.1;
		
		
		
		Minecraft.getMinecraft().effectRenderer.addEffect((EntityFX)fx);
		
		
		
	}
	
	
	
	
	super.onPreTick(aBaseMetaTileEntity, aTick);
}


@SideOnly(Side.CLIENT)
public class EntityDropParticleFX extends EntityFX
{
    /** the material type for dropped items/blocks */
    private Material materialType;
    /** The height of the current bob */
    private int bobTimer;
    //private static final String __OBFID = "CL_00000901";

    public EntityDropParticleFX(World worldIn, double p_i1203_2_, double p_i1203_4_, double p_i1203_6_, Fluid f)
    {
        super(worldIn, p_i1203_2_, p_i1203_4_, p_i1203_6_, 0.0D, 0.0D, 0.0D);
        this.motionX = this.motionY = this.motionZ = 0.0D;

        int col=f.
        		getColor();
    	this.particleBlue=col&0xFF;
		this.particleGreen=(col&0xFF00)>>8;
		this.particleRed=(col&0xFF0000)>>16;
       
		this.particleBlue=0xFF;
		this.particleGreen=0xFF;
		this.particleRed=0xFF;
		
		setParticleIcon(f.getIcon());

       // this.setParticleTextureIndex(113);
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        //this.materialType = p_i1203_8_;
        this.bobTimer = 00;
        this.particleMaxAge = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
        this.motionX = this.motionY = this.motionZ = 0.0D;
        particleMaxAge=100;
    }
	@Override
	public int getFXLayer() {
	return 1;
	}
    public int getBrightnessForRender(float p_70070_1_)
    {
        return this.materialType == Material.water ? super.getBrightnessForRender(p_70070_1_) : 257;
    }

    /**
     * Gets how bright this entity is.
     */
    public float getBrightness(float p_70013_1_)
    {
        return this.materialType == Material.water ? super.getBrightness(p_70013_1_) : 1.0F;
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

       /* if (this.materialType == Material.water)
        {
            this.particleRed = 0.2F;
            this.particleGreen = 0.3F;
            this.particleBlue = 1.0F;
        }
        else
        {
            this.particleRed = 1.0F;
            this.particleGreen = 16.0F / (float)(40 - this.bobTimer + 16);
            this.particleBlue = 4.0F / (float)(40 - this.bobTimer + 8);
        }
*/
        this.motionY -= (double)this.particleGravity;

        if (this.bobTimer-- > 0)
        {
            this.motionX *= 0.02D;
            this.motionY *= 0.02D;
            this.motionZ *= 0.02D;
            //this.setParticleTextureIndex(113); 
           // this.setParticleTextureIndex(19 + this.rand.nextInt(4));
        }
        else
        {
           // this.setParticleTextureIndex(113);
        	//this.setParticleTextureIndex(19 + this.rand.nextInt(4));
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.particleMaxAge-- <= 0)
        {
            this.setDead();
        }

        if (this.onGround)
        {
            /*if (this.materialType == Material.water)
            {
              
            else
            {
                this.setParticleTextureIndex(114);
            }
*/ 
        //	this.setDead();
//this.worldObj.spawnParticle("splash", this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);

            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }

        Material material = this.worldObj.getBlock(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ)).getMaterial();

        if (material.isLiquid() || material.isSolid())
        {
            double d0 = (double)((float)(MathHelper.floor_double(this.posY) + 1) - BlockLiquid.getLiquidHeightPercent(this.worldObj.getBlockMetadata(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posY), MathHelper.floor_double(this.posZ))));

            if (this.posY < d0)
            {
                this.setDead();
            }
        }
    }
    
    @Override
    public void renderParticle(Tessellator tess, float timeStep, float rotationX, float rotationXZ, float rotationZ,
            float rotationYZ, float rotationXY) {
        double x = (this.prevPosX + (this.posX - this.prevPosX) * timeStep - interpPosX);
        double y = (this.prevPosY + (this.posY - this.prevPosY) * timeStep - interpPosY);
        double z = (this.prevPosZ + (this.posZ - this.prevPosZ) * timeStep - interpPosZ);

        float minU = this.particleTextureIndexX / 16.0F;
        float maxU = minU + 0.0624375F;
        float minV = this.particleTextureIndexY / 16.0F;
        float maxV = minV + 0.0624375F;
        float scale = 0.1F * this.particleScale;

        if (this.particleIcon != null) {
            minU = this.particleIcon.getMinU();
            maxU = this.particleIcon.getMaxU();
            minV = this.particleIcon.getMinV();
            maxV = this.particleIcon.getMaxV();
        }

        tess.setColorRGBA_F(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha);

        for (int i = 0; i < 5; i++) {
            renderParticle(
                    tess,
                    x,
                    y,
                    z,
                    rotationX,
                    rotationXZ,
                    rotationZ,
                    rotationYZ,
                    rotationXY,
                    minU,
                    maxU,
                    minV,
                    maxV,
                    scale);
        }
    }

    private void renderParticle(Tessellator tess, double x, double y, double z, float rotationX,
            float rotationXZ, float rotationZ, float rotationYZ, float rotationXY, float minU, float maxU, float minV,
            float maxV, float scale) {
        tess.addVertexWithUV(
                (x - rotationX * scale - rotationYZ * scale),
                (y - rotationXZ * scale),
                (z - rotationZ * scale - rotationXY * scale),
                maxU,
                maxV);
        tess.addVertexWithUV(
                (x - rotationX * scale + rotationYZ * scale),
                (y + rotationXZ * scale),
                (z - rotationZ * scale + rotationXY * scale),
                maxU,
                minV);
        tess.addVertexWithUV(
                (x + rotationX * scale + rotationYZ * scale),
                (y + rotationXZ * scale),
                (z + rotationZ * scale + rotationXY * scale),
                minU,
                minV);
        tess.addVertexWithUV(
                (x + rotationX * scale - rotationYZ * scale),
                (y - rotationXZ * scale),
                (z + rotationZ * scale - rotationXY * scale),
                minU,
                maxV);
    }

}


boolean fx=true;
@Override
public void onScrewdriverRightClick(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ,
		ItemStack aTool) {
	
	if (!getBaseMetaTileEntity().getCoverInfoAtSide(side)
            .isGUIClickable()) return;
	fx=!fx;
	GT_Utility.sendChatToPlayer(
               aPlayer,
               StatCollector.translateToLocal("proghatches.gt.void.fx."+fx)
			   );
	
}



}

