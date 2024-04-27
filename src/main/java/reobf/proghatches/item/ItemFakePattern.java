package reobf.proghatches.item;

import java.util.List;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import reobf.proghatches.eucrafting.TileFluidInterface_EU;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;

/**
 * for ae2 crafting visualizer
 */
public  class ItemFakePattern extends Item implements ICraftingPatternItem {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override 
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        p_77624_3_.add("Technical item, not for use.");
        super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
    }

    @Override
    public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
        try {
           
        	if(is.getTagCompound().getByte("type")==1){
        		ItemStack i = ItemStack.loadItemStackFromNBT((NBTTagCompound) is.getTagCompound().getTag("i"));
        		ItemStack o = ItemStack.loadItemStackFromNBT((NBTTagCompound) is.getTagCompound().getTag("o"));
                return new TileFluidInterface_EU.SISOPatternDetail(i,o);
        	}
        	
        	if(is.getTagCompound().getByte("type")==2){
        		ItemStack i = ItemStack.loadItemStackFromNBT((NBTTagCompound) is.getTagCompound().getTag("i"));
        		ItemStack o = ItemStack.loadItemStackFromNBT((NBTTagCompound) is.getTagCompound().getTag("o"));
        		ItemStack p = ItemStack.loadItemStackFromNBT((NBTTagCompound) is.getTagCompound().getTag("p"));
                
        		return new TileFluidInterface_EU.WrappedPatternDetail(
        				((ItemEncodedPattern)p.getItem()).getPatternForItem(p, w)
        				,i,o,is.getTagCompound().getInteger("pr"));
        	}
        	
        	
        	
        	
        	
        	ItemStack iss = ItemStack.loadItemStackFromNBT(is.getTagCompound());
            return new CircuitProviderPatternDetial(iss);
            
            
            
        } catch (Exception ew) {
            ew.printStackTrace();
            return new CircuitProviderPatternDetial(new ItemStack(Items.baked_potato).setStackDisplayName("ERROR"));
        }
    }

}