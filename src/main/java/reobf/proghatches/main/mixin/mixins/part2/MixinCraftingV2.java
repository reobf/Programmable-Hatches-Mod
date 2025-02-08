package reobf.proghatches.main.mixin.mixins.part2;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import appeng.api.implementations.tiles.ICraftingMachine;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CommonMetaTileEntity;
import reobf.proghatches.gt.metatileentity.util.ICraftingV2;

@Mixin(value = BaseMetaTileEntity.class, remap = false)
public abstract class MixinCraftingV2 /*extends CommonMetaTileEntity */implements ICraftingMachine {
	
	
	private IMetaTileEntity getMetaTileEntity0(){
		
		BaseMetaTileEntity x=(BaseMetaTileEntity)(Object)this;
		return x.getMetaTileEntity();
	};
    @Override
    public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table,
        ForgeDirection ejectionDirection) {
        if (notV2) return false;
        IMetaTileEntity mte;
        if ((mte = getMetaTileEntity0()) instanceof ICraftingV2) {
            if (((ICraftingV2) mte).enableCM() == false) {
                return false;
            }
            return ((ICraftingV2) mte).pushPatternCM(patternDetails, table, ejectionDirection);
        } /*
           * else{
           * if(mte!=null){
           * }
           * }
           */

        return false;
    }

    @Unique
    boolean notV2;

    @Override
    public boolean acceptsPlans() {
        if (notV2) return false;
        IMetaTileEntity mte;
        if ((mte = getMetaTileEntity0()) instanceof ICraftingV2) {
            if (((ICraftingV2) mte).enableCM() == false) {
                return false;
            }
            return ((ICraftingV2) mte).acceptsPlansCM();
        } else {
            if (mte != null) {
                notV2 = true;
            }
        }

        return false;
    }

}
