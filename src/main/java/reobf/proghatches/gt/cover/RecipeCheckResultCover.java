package reobf.proghatches.gt.cover;

import java.lang.reflect.Field;
import java.util.Arrays;

import com.google.common.io.ByteArrayDataInput;

import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_BasicMachine;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import reobf.proghatches.gt.cover.WirelessControlCover.Data;

public class RecipeCheckResultCover extends GT_CoverBehaviorBase<RecipeCheckResultCover.Data> {

	public RecipeCheckResultCover() {
		super(Data.class);

	}

	public static class Data implements ISerializableObject {
		private int pulses;
		private int lastSuccess;

		public void loadDataFromNBT(NBTBase aNBTt) {

			NBTTagCompound aNBT = (NBTTagCompound) aNBTt;
			lastSuccess = aNBT.getInteger("lastSuccess");
			pulses = aNBT.getInteger("pulses");

		}

		public NBTBase saveDataToNBT() {
			NBTTagCompound aNBT = new NBTTagCompound();

			aNBT.setInteger("lastSuccess", lastSuccess);
			aNBT.setInteger("pulses", pulses);
			return aNBT;
		}

		@Override
		public ISerializableObject copy() {
			Data d = new Data();
			d.loadDataFromNBT(this.saveDataToNBT());
			;
			return d;
		}

		@Override
		public void writeToByteBuf(ByteBuf aBuf) {
			aBuf.writeInt(pulses);
			aBuf.writeInt(lastSuccess);
		}

		@Override
		public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
			Data d = new Data();
			d.pulses = aBuf.readInt();
			d.lastSuccess = aBuf.readInt();

			return d;
		}

	}

	@Override
	protected int getTickRateImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {

		return 1;
	}

	@Override
	public boolean allowsTickRateAddition() {

		return false;
	}

	@Override
	protected boolean manipulatesSidedRedstoneOutputImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity) {

		return true;
	}

	private static int check(CheckRecipeResult crr) {
		if (crr.wasSuccessful())
			return 1;
		if (crr == CheckRecipeResultRegistry.NO_RECIPE)
			return 0;
		return 2;
	}

	public static void start(int lastSuccess, int newSuccess, Data d) {

		if (lastSuccess == 1 && newSuccess == 0) {
			d.pulses++;
		}
	}

	@Override
	protected Data doCoverThingsImpl(ForgeDirection side, byte aInputRedstone, int aCoverID, Data aCoverVariable,
			ICoverable aBaseMetaTileEntity, long aTimer) {
	
		if(
		aBaseMetaTileEntity instanceof BaseMetaTileEntity){
			IMetaTileEntity mte = ((BaseMetaTileEntity) aBaseMetaTileEntity).getMetaTileEntity() ;
			if(mte!=null&&mte instanceof GT_MetaTileEntity_BasicMachine){
				//when placed on single-block machine
				GT_MetaTileEntity_BasicMachine mach=(GT_MetaTileEntity_BasicMachine) mte;
			boolean working=((BaseMetaTileEntity) aBaseMetaTileEntity).getMaxProgress()>0;
				start(aCoverVariable.lastSuccess, working?1:0 , aCoverVariable);
				aCoverVariable.lastSuccess = working?1:0;
			
			}
			
		}
		
		
		if(aCoverVariable.pulses>0){
			aCoverVariable.pulses--;
			
			
			aBaseMetaTileEntity.setOutputRedstoneSignal(side, (byte) 15);
			
		}else{
			
			aBaseMetaTileEntity.setOutputRedstoneSignal(side, (byte) 0);
			
			
			
		}
		return aCoverVariable;
	}

	public static void update(CheckRecipeResult controller, Data d) {
		try {
			CheckRecipeResult res = controller;
			int newSuccess = check(res);
			start(d.lastSuccess, newSuccess, d);
			d.lastSuccess = newSuccess;
		} catch (Exception e) {

			e.printStackTrace();

		}

		/// return CheckRecipeResultRegistry.SUCCESSFUL;
	}

	@Override
	public Data createDataObject(int aLegacyData) {

		throw new UnsupportedOperationException("no legacy");
	}

	@Override
	public Data createDataObject() {

		return new Data();
	}

	@Override
	protected boolean letsItemsInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
			ICoverable aTileEntity) {

		return true;
	}

	@Override
	protected boolean letsItemsOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsEnergyInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsEnergyOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsFluidInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsFluidOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
			ICoverable aTileEntity) {
		return true;
	}
}
