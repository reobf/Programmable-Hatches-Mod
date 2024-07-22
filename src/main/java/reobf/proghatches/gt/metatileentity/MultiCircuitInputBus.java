package reobf.proghatches.gt.metatileentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import gregtech.api.GregTech_API;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.GT_TooltipDataCache.TooltipData;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.util.IMultiCircuitSupport;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.registration.Registration;
import reobf.proghatches.util.ProghatchesUtil;

public class MultiCircuitInputBus extends GT_MetaTileEntity_Hatch_InputBus implements IMultiCircuitSupport{
@Override
	public ItemStackHandler getInventoryHandler() {
		// TODO Auto-generated method stub
		return super.getInventoryHandler();
	}
@Override
public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
	
	super.addUIWidgets(builder, buildContext);
	ProghatchesUtil.attachZeroSizedStackRemover(builder, buildContext);
	for(int i=1;i<4;i++)
		builder.widget(
	new SlotWidget(new BaseSlot(inventoryHandler, getCircuitSlot()+i) {

		public int getSlotStackLimit() {
			return 0;
		};

	}

	) {

		@Override
		public List<String> getExtraTooltip() {
			return Arrays
					.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1"));
		}
	}.disableShiftInsert().setHandlePhantomActionClient(true).setGTTooltip(() -> new TooltipData(
			Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
					LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")),
			Arrays.asList(LangManager.translateToLocal("programmable_hatches.gt.marking.slot.0"),
					LangManager.translateToLocal("programmable_hatches.gt.marking.slot.1")))).setPos(
							getCircuitSlotX()-1,getCircuitSlotY()-18 * i-1)
	
				);
}
	public MultiCircuitInputBus(int id, String name, String nameRegional, int tier, 
			String... optional) {

		super(id, name, nameRegional, tier, getSlots(tier) + 4, (optional.length > 0 ? optional
				: 
				reobf.proghatches.main.Config.get("MCIB", ImmutableMap.of(

						
						"slots", Math.min(16, (1 + tier) * (tier + 1))
						
				))

		)

		);
		
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));
	
	}
	  public MultiCircuitInputBus(String mName, byte mTier, String[] mDescriptionArray, ITexture[][][] mTextures) {
		super(mName,  mTier,getSlots(mTier) + 4,  mDescriptionArray,  mTextures);
	}
	@Override
	    public boolean isValidSlot(int aIndex) {
	        return aIndex < getCircuitSlot();
	    }
	  
	  
	  @Override
	    public boolean allowPullStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
	        ItemStack aStack) {
	        if (aIndex >= getCircuitSlot()) return false;
	        return side == getBaseMetaTileEntity().getFrontFacing();
	    }

	    @Override
	    public boolean allowPutStack(IGregTechTileEntity aBaseMetaTileEntity, int aIndex, ForgeDirection side,
	        ItemStack aStack) {
	        return side == getBaseMetaTileEntity().getFrontFacing() && aIndex < getCircuitSlot()
	            && (mRecipeMap == null || disableFilter || mRecipeMap.containsInput(aStack))
	            && (disableLimited || limitedAllowPutStack(aIndex, aStack));
	    }
	    int[] cSlotCache;
		@Override
		public int[] getCircuitSlots() {
			if(cSlotCache!=null)return cSlotCache;
			return cSlotCache=new int[]{
					getCircuitSlot(),
					getCircuitSlot()+1,
					getCircuitSlot()+2,
					getCircuitSlot()+3
			};
		}
		@Override
	    public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
	        return new MultiCircuitInputBus(mName, mTier, mDescriptionArray, mTextures);
	    }
@Override
	public void updateSlots() {
	for (int i = 0; i < mInventory.length - 4; i++)
        if (mInventory[i] != null && mInventory[i].stackSize <= 0) mInventory[i] = null;
    if (!disableSort) fillStacksIntoFirstSlots();
	}

protected void fillStacksIntoFirstSlots() {
    final int L = mInventory.length - 4;
    HashMap<GT_Utility.ItemId, Integer> slots = new HashMap<>(L);
    HashMap<GT_Utility.ItemId, ItemStack> stacks = new HashMap<>(L);
    List<GT_Utility.ItemId> order = new ArrayList<>(L);
    List<Integer> validSlots = new ArrayList<>(L);
    for (int i = 0; i < L; i++) {
        if (!isValidSlot(i)) continue;
        validSlots.add(i);
        ItemStack s = mInventory[i];
        if (s == null) continue;
        GT_Utility.ItemId sID = GT_Utility.ItemId.createNoCopy(s);
        slots.merge(sID, s.stackSize, Integer::sum);
        if (!stacks.containsKey(sID)) stacks.put(sID, s);
        order.add(sID);
        mInventory[i] = null;
    }
    int slotindex = 0;
    for (GT_Utility.ItemId sID : order) {
        int toSet = slots.get(sID);
        if (toSet == 0) continue;
        int slot = validSlots.get(slotindex);
        slotindex++;
        mInventory[slot] = stacks.get(sID)
            .copy();
        toSet = Math.min(toSet, mInventory[slot].getMaxStackSize());
        mInventory[slot].stackSize = toSet;
        slots.merge(sID, toSet, (a, b) -> a - b);
    }
}
}
