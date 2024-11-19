package reobf.proghatches.ae;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import com.glodblock.github.common.item.ItemFluidPacket;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.fmp.LayerCraftingMachine.StateHolder;

public class TileMolecularAssemblerInterface extends TileEntity implements ICraftingMachine {

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
			ForgeDirection ejectionDirection) {
		
		TileEntity te = getTarget(ejectionDirection.getOpposite());
		if(te ==null)return false;  
		ArrayList<ItemStack> item = new ArrayList<>(table.getSizeInventory());
		ArrayList<FluidStack> fluid = new ArrayList<>(1);

		for (int i = 0; i < table.getSizeInventory(); i++) {

			ItemStack is = table.getStackInSlot(i);
			if (is == null) {
				continue;
			}

			if (is.getItem() instanceof ItemFluidPacket) {
				FluidStack fs = ItemFluidPacket.getFluidStack(is);
				if (fs == null) {
					continue;
				}
				fluid.add(fs);
			} else {

				item.add(is);
			}

		}
		
		if(itemCheck(te, item, false)>=0)return false;
		if(fluidCheck(te, fluid, false)>=0)return false;
		
		itemCheck(te, item,true);
		fluidCheck(te, fluid,true);
		
		
		return true;
	}

	@Override
	public boolean acceptsPlans() {
		/*ForgeDirection dir = StateHolder.state;
		TileEntity targ = getTarget();
		if(targ==null)return false;
		*/
		
		
		
		return true;
	}

	private TileEntity getTarget() {

		final TileEntity te = this.getWorldObj().getTileEntity(this.xCoord + this.getSide().offsetX,
				this.yCoord + this.getSide().offsetY, this.zCoord + this.getSide().offsetZ);

		return te;
	}
	private TileEntity getTarget(ForgeDirection d) {

		final TileEntity te = this.getWorldObj().getTileEntity(this.xCoord + d.offsetX,
				this.yCoord + d.offsetY, this.zCoord + d.offsetZ);

		return te;
	}
	interface ISideCheck {
		int[] getAccessibleSlotsFromSide(int p_94128_1_);

		boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_);

		public static ISideCheck ofInv(IInventory te) {

			if (te instanceof ISidedInventory) {
				ISidedInventory side = (ISidedInventory) te;
				return new ISideCheck() {

					@Override
					public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
						return side.getAccessibleSlotsFromSide(p_94128_1_);
					}

					@Override
					public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {

						return side.canInsertItem(p_102007_1_, p_102007_2_, p_102007_3_);
					}
				};
			}

			return new ISideCheck() {

				@Override
				public int[] getAccessibleSlotsFromSide(int p_94128_1_) {
					return IntStream.range(0, te.getSizeInventory()).toArray();
				}

				@Override
				public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_) {
					return true;
				}
			};
		}
	}

	//-1 pass 
	//>=0 first index in inputs that won't fit
	public int itemCheck( TileEntity t, ArrayList<ItemStack> item,boolean doInject) {
		if(t instanceof IInventory==false){return Integer.MAX_VALUE;}
		IInventory te=(IInventory) t;
		
		ForgeDirection dir=getSide();
		ISideCheck checker=ISideCheck.ofInv(te);
		int cnt=0;
		next:for(int i=0;i<item.size();i++){
			int[] slots=checker.getAccessibleSlotsFromSide(i);
			Arrays.sort(slots);
			while(true){
			
				if(checker.canInsertItem(slots[cnt], item.get(i), dir.ordinal())
						&&
						te.isItemValidForSlot(slots[cnt], item.get(i))
						&&
						te.getInventoryStackLimit()>=item.get(i).stackSize
						&&item.get(i).stackSize<=item.get(i).getMaxStackSize()
						){
					if(doInject){
						te.setInventorySlotContents(slots[cnt], item.get(i));
					}
					
					continue next;
			};
			cnt++;
			if(slots.length<=cnt)return i;
			}
			
			
			
		}
		
		
		
		
		
		return -1;
	}

	public int fluidCheck( TileEntity t, ArrayList<FluidStack> fluid,boolean doInject) {
		if(t instanceof IFluidHandler==false){return Integer.MAX_VALUE;}
		IFluidHandler f=(IFluidHandler) t;
		//SPECIAL CHECKS HERE
		if(fluid.size()>1){return 0;}
		for(int i=0;i<fluid.size();i++){
			if(f.fill(getSide(), fluid.get(i), false)==fluid.get(i).amount)return i;
			if(doInject)f.fill(getSide(), fluid.get(i), true);//TODO:check ret val?
			
		}
		
		
		return -1;
	}
	
	int mode =0;
	ForgeDirection side=ForgeDirection.DOWN;
	
	private ForgeDirection getSide() {

		
		return side;
	}

}
