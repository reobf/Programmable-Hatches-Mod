package reobf.proghatches.gt.cover;

import net.glease.ggfab.mte.MTE_LinkedInputBus;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.GT_Utility;
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.gt.metatileentity.util.IProgrammingCoverBlacklisted;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.main.MyMod;

public class ProgrammingCover extends GT_CoverBehavior implements IProgrammer {

	@Override
	public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {

		return 1;
	}

	public void impl(ICoverable aTileEntity) {
	
		/*if ((((aTileEntity instanceof IMachineProgress)) && (!((IMachineProgress) aTileEntity).isAllowedToWork()))) {
			return;
		}*/
		TileEntity tile = (TileEntity) aTileEntity;

	/*	if (!(tile instanceof ISidedInventory)) {
			return;
		}
		if (!(tile instanceof IGregTechTileEntity)) {
			return;
		}*/
		IMetaTileEntity meta = ((IGregTechTileEntity) tile).getMetaTileEntity();
		if (meta instanceof IProgrammingCoverBlacklisted) {

			return;
		}

		

		if (!(meta instanceof IConfigurationCircuitSupport)) {
			return;
		}
		
		ArrayList<ItemStack> isa = new ArrayList<>();
		int[] slots = ( aTileEntity).getAccessibleSlotsFromSide(ForgeDirection.UNKNOWN.ordinal());
		for (int slot : slots) {
			ItemStack is = (aTileEntity).getStackInSlot(slot);
			if (is == null)
				continue;
			if (is.getItem() != MyMod.progcircuit)
				continue;

			
			if (((ISidedInventory) tile).decrStackSize(slot, 64).stackSize == 0) {
				continue;
			}
			isa.add(GT_Utility.copyAmount(0, ItemProgrammingCircuit.getCircuit(is).orElse(null)));
			
			
		}
		if(isa.isEmpty()==false){	
			if(meta instanceof IMultiCircuitSupport){
				int[] aslots=((IMultiCircuitSupport) meta).getCircuitSlots();
				for (int i = 0; i < aslots.length; i++) {
					if (i < isa.size()) {
						((IInventory) tile).setInventorySlotContents(
								((IMultiCircuitSupport) meta).getCircuitSlots()[i]
								,isa.get(i));
					} else {
						((IInventory) tile).setInventorySlotContents(
								((IMultiCircuitSupport) meta).getCircuitSlots()[i]
								,null);
					}
	
				}
	
				
				
				
			}else{
			
			
			((IInventory) tile).setInventorySlotContents(((IConfigurationCircuitSupport) meta).getCircuitSlot(),
					isa.get(0));
			
			}
		}
		
		

	}

	@Override
	public boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
		if (Optional.of(aTileEntity).filter(s -> s instanceof IGregTechTileEntity)
				.map(s -> ((IGregTechTileEntity) s).getMetaTileEntity())
				.filter(s -> s instanceof IProgrammingCoverBlacklisted).isPresent())
			return false;

	for(ForgeDirection d:	ForgeDirection.VALID_DIRECTIONS){
		GT_CoverBehaviorBase<?> beh = aTileEntity.getCoverBehaviorAtSideNew(d);
		if(beh!=null&&beh.getClass()==LinkedBusSlaveCover.class){return false;}
	}
		
		return super.isCoverPlaceable(side, aStack, aTileEntity);
	}

	@Override
	public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
			ICoverable aTileEntity, long aTimer) {
		impl(aTileEntity);
		if(aTileEntity instanceof IGregTechTileEntity){
			IMetaTileEntity x = ((IGregTechTileEntity) aTileEntity).getMetaTileEntity();
			if(x instanceof MTE_LinkedInputBus){
				markOrUpdate((MTE_LinkedInputBus) x);
			}
			
		}
		return aCoverVariable;
	}
	public static class Data{
		String str;
		ItemStack circuit;
		public Data(String s,ItemStack i){
			str=s;circuit=i;
			}
	}
    public static WeakHashMap<MTE_LinkedInputBus,Data> ggfabLinkedBus=new WeakHashMap<>();
    
    public static void markOrUpdate(
    		MTE_LinkedInputBus host
    		){
    	
    	Data bus = ggfabLinkedBus.get(host);
    if(bus==null)
    	ggfabLinkedBus.put(host, new Data(
    			ggfabGetRealChannel(host),
    		host.getStackInSlot(host.getCircuitSlot())
    					));
    else{
    	
    	bus.str=ggfabGetRealChannel(host);
    	if(!ItemStack.areItemStacksEqual(bus.circuit,host.getStackInSlot(host.getCircuitSlot())))
    	bus.circuit=host.getStackInSlot(host.getCircuitSlot());
    }
     }
    
    private static RuntimeException RESUABLE_EXCEPTION=new RuntimeException("",null,false,false){
    	private static final long serialVersionUID = 1L;};
    	
    public static ItemStack sync(MTE_LinkedInputBus host){
    	String chan=ggfabGetRealChannel(host);
    	Data data = ggfabLinkedBus.values().stream().filter(s->Objects.equals(s.str,chan)).findAny().orElse(null);
    	if(data==null)throw RESUABLE_EXCEPTION;
    	return data.circuit.copy();
    }


    private static String ggfabGetRealChannel(MTE_LinkedInputBus thiz) {
        if (thiz.getChannel() == null) return null;
        if (thiz.isPrivate()) return thiz.getBaseMetaTileEntity().getOwnerUuid() + thiz.getChannel();
        return new UUID(0, 0) + thiz.getChannel();
    }


}
