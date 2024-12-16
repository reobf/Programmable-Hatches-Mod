package reobf.proghatches.thaum;

import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.MTEHatchMaintenance;
import gregtech.api.metatileentity.implementations.MTEMultiBlockBase;
import gregtech.api.multitileentity.base.MultiTileEntity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import reobf.proghatches.main.mixin.mixins.MixinGolem;
import thaumcraft.common.config.Config;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumcraft.common.entities.golems.Marker;
import thaumcraft.common.lib.utils.BlockUtils;
import thaumcraft.common.lib.utils.Utils;

public class AIFix extends EntityAIBase{
	private EntityGolemBase theGolem;
	private World theWorld;
	private float distance;
	private FakePlayer player;
	private int delay;
	private int xx;
	private int yy;
	private int zz;
	private int count;
	boolean working;
	public AIFix(Object mixinGolem) {
		      this.theGolem = (EntityGolemBase) mixinGolem;
		      this.theWorld = theGolem.worldObj;
		      this.setMutexBits(3);
		      this.distance = (float)MathHelper.ceiling_float_int(this.theGolem.getRange() / 3.0F);
		      if(this.theWorld instanceof WorldServer) {
		         this.player = FakePlayerFactory.get((WorldServer)this.theWorld, new GameProfile((UUID)null, "FakeThaumcraftGolem"));
		      }

		   }
	@Override
	public boolean shouldExecute() {
		 if(working==false&&this.delay <= 0 && this.theGolem.ticksExisted % Config.golemDelay <= 0 && this.theGolem.getNavigator().noPath()) {
	      
	           
	            return findHatch();
	         
	      } else {
	         return false;
	      }
	}  
	public void startExecuting() {
	      this.count = 20*20;
	      this.theGolem.getNavigator().tryMoveToXYZ((double)this.xx + 0.5D, (double)this.yy + 0.5D, (double)this.zz + 0.5D, (double)this.theGolem.getAIMoveSpeed());
	   }
	 public boolean continueExecuting() {
	       boolean b=
	    		 /* this.theWorld.getBlock(this.xx, this.yy, this.zz) == this.block && 
	    		  this.theWorld.getBlockMetadata(this.xx, this.yy, this.zz) == this.blockMd && */
	    		  this.count-- > 0 && 
	    		  (this.delay > 0 ||needFix(hostx,hosty,hostz)
	    				  || !this.theGolem.getNavigator().noPath());
	    		  if(b==false)working=false;
	    		  
	    		  return b;
	   }
	 
	 int hostx,hosty,hostz;
	 
	 
	 private MTEHatchMaintenance getHatchOf(int hostx,int hosty,int hostz){
		 TileEntity te= theWorld.getTileEntity(hostx,hosty,hostz);
		 if(te instanceof BaseMetaTileEntity){
			 IMetaTileEntity  mte= ((BaseMetaTileEntity) te).getMetaTileEntity();
			 if(mte instanceof MTEMultiBlockBase){
				 MTEMultiBlockBase mb=(MTEMultiBlockBase) mte;
				Iterator<MTEHatchMaintenance> itr = mb.mMaintenanceHatches.iterator();
			if(itr.hasNext()){
				
				return itr.next();
			}
			 }
		 }
		 return null;
		 
	 }	 
	 private MTEHatchMaintenance getHatch(int xx,int yy,int zz){
		 TileEntity te= theWorld.getTileEntity(xx,yy,zz);
		 if(te instanceof BaseMetaTileEntity){
			 IMetaTileEntity  mte= ((BaseMetaTileEntity) te).getMetaTileEntity();
			 if(mte instanceof MTEHatchMaintenance){
				return (MTEHatchMaintenance) mte;
		 }}
		 return null;
		 
	 }	 
	 private boolean needFix(int hostx,int hosty,int hostz){
		 TileEntity te= theWorld.getTileEntity(hostx,hosty,hostz);
		 if(te instanceof BaseMetaTileEntity){
			 IMetaTileEntity  mte= ((BaseMetaTileEntity) te).getMetaTileEntity();
			 if(mte instanceof MTEMultiBlockBase){
				 MTEMultiBlockBase mb=(MTEMultiBlockBase) mte;
				 if
				 (
				 mb.mMachine&&
				 (!mb.mWrench ||
				 !mb.mScrewdriver ||
				 !mb.mSoftHammer ||
				 !mb.mHardHammer ||
				 !mb.mSolderingTool ||
				 !mb.mCrowbar)) {
					
					 return true;
				 }

			 }
		 }
		 return false;
	 }
	private boolean findHatch() {
	
		
	
	      for(Marker mark:theGolem.getMarkers()) {
	         
	    	  int x =mark.x;
	         int y = mark.y;
	         int z = mark.z;
	         if(
	        		 
	        		!theWorld.getChunkProvider().chunkExists(x>>4, z>>4) 
	        		 ){continue;}
	         
	         if(needFix(x,y,z)) {
	           MTEHatchMaintenance hatch = getHatchOf(x,y,z);
	        	 if(hatch==null){return false;}
	        	 IGregTechTileEntity bs = hatch.getBaseMetaTileEntity();
	        	 xx=bs.getXCoord();
	        	 yy=bs.getYCoord();
	        	 zz=bs.getZCoord();
	           hostx=x;
	          hosty=y;
	          hostz=z;
	         
	          working=true;
	          
	            return true;
	         }
	      }

	      return false;
	}
	public void updateTask() {
	      double dist = this.theGolem.getDistanceSq((double)this.xx + 0.5D, (double)this.yy + 0.5D, (double)this.zz + 0.5D);
	      this.theGolem.getLookHelper().setLookPosition((double)this.xx + 0.5D, (double)this.yy + 0.5D, (double)this.zz + 0.5D, 30.0F, 30.0F);
	      if(dist <= 4.0D) {
	    	  MTEHatchMaintenance hatch = getHatch(xx,yy,zz);
	    	  if(hatch==null){
	    		  System.out.println(xx+" "+yy+" "+zz);
	    		  working=false;return;
	    	  } 
	    	  if(working==true){
	    	  theGolem.startActionTimer();
	    	  hatch.getBaseMetaTileEntity().setActive(false);
	    	  hatch.mWrench =true;
	    	  hatch.mScrewdriver =true;
	    	  hatch.mSoftHammer =true;
	    	  hatch.mHardHammer =true;
	    	  hatch.mSolderingTool =true;
	    	  hatch.mCrowbar =true;
	    	  }
	    	  working=false;
	    	  
	    	  
	      }

	   }
}
