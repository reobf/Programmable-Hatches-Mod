package reobf.proghatches.item;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.libraries.com.google.common.base.Optional;

import com.github.technus.tectech.mechanics.pipe.IConnectsToEnergyTunnel;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.IInterfaceViewable;
import appeng.core.localization.GuiText;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.PatternHelper;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.mixin.MixinCallback;

public class ItemFixer extends Item{
	@Override
		public boolean hasEffect(ItemStack par1ItemStack, int pass) {
			
			return true;
		}
	
@Override
public boolean onItemUse(ItemStack p_77648_1_, EntityPlayer p_77648_2_, World w, int x,
		int y, int z, int p_77648_7_, float p_77648_8_, float p_77648_9_, float p_77648_10_) {
	
	p_77648_2_.swingItem();
	if(w.isRemote)return true;
	
	TileEntity te = w.getTileEntity(x, y, z);
	/*System.out.println(te instanceof IConnectsToEnergyTunnel);
	if(te instanceof IConnectsToEnergyTunnel)
	System.out.println(((IConnectsToEnergyTunnel) te).canConnect(ForgeDirection.UP));
	
	
	if(te!=null){
		Class<?> c = te.getClass();
	
		while(c!=null){
			System.out.println(c);
		System.out.println(Arrays.toString(
				
		c.getInterfaces())
		
				);;c=(Class<? >) c.getSuperclass();}
	}
	
	
	
	*/
	
	
	
IGrid g = null;
	if(te instanceof IGridProxyable){
		
	try {
		g=(((IGridProxyable) te).getProxy().getGrid());
	} catch (Exception e) {
	
	}
	}
	if(te instanceof IGridHost){
		
		if(g==null){
			
			IGridNode n=((IGridHost) te).getGridNode(ForgeDirection.UP);
			if(n!=null)
			g=n.getGrid();
			
		
		}
		
	}
	if(g!=null){
	int cnt[]=new int[]{0};
		
	g.getNodes().forEach(s->{
	
	IGridHost host = s.getMachine();
		
	//System.out.println(host);
	//System.out.println(host instanceof IInterfaceHost);
	
	if(host instanceof IInterfaceViewable){
		//System.out.println("fix:"+host);
		
	fix((IInterfaceViewable) host,()->cnt[0]++);}
	});	
	
	System.out.println("Player tried to use fixer to fix patterns, if this info spams in log and server gets laggy, blame 'em.");
	System.out.println("player:"+p_77648_2_.getDisplayName()+",fix:"+cnt[0]+",dim:"+p_77648_2_.getEntityWorld().provider.dimensionId);
	p_77648_2_.addChatMessage(new ChatComponentTranslation("proghatch.itemfixer.info",cnt[0]));
	if(cnt[0]==0)p_77648_2_.addChatMessage(new ChatComponentTranslation("proghatch.itemfixer.warn"));

	}
	
	
	return true;
}

public IAEItemStack fixCircuit(IAEItemStack gs,Runnable succ){
	if(gs.getItem()!=MyMod.progcircuit)return null;
	gs=gs.copy();
	int ver=ItemProgrammingCircuit.isNew(gs.getItemStack());
	if(ver==0||ver==1){
	NBTTagCompound tag = gs.getTagCompound().getNBTTagCompoundCopy();
	if(	MixinCallback.fixCircuitTag(tag)){
		ItemStack is = gs.getItemStack();
		is.stackTagCompound=(NBTTagCompound) tag.copy();
		gs=AEItemStack.create(is);
		succ.run();
	}
	}
	return gs;
}
public ItemStack fix(ItemStack is,Runnable succ){
	//ICraftingPatternItem item=(ICraftingPatternItem) is.getItem();
	 final ItemStack unknownItem = new ItemStack(Blocks.fire);
      unknownItem.setStackDisplayName(GuiText.UnknownItem.getLocal());
      is=is.copy();
      //item.getPatternForItem(is, null);
	
     boolean fluid= is.getItem() instanceof ItemFluidEncodedPattern;
      final NBTTagCompound encodedValue = (NBTTagCompound) is.getTagCompound();
	 NBTTagList in = encodedValue.getTagList("in", 10);
	  for (int x = 0; x < in.tagCount(); x++) {
		  IAEItemStack gs = AEItemStack.loadItemStackFromNBT(in.getCompoundTagAt(x));
		  if(gs==null)continue;
		  gs=fixCircuit(gs,succ);
		  if(gs==null)continue;
		  gs=gs.copy();
		  if(gs.getStackSize()<=0)gs.setStackSize(1);
		  
		 
		  if(fluid){
		 NBTTagCompound t=new NBTTagCompound();
		  gs.writeToNBT(t);
		  in.func_150304_a(x, t);
		  }
		  else{
			  in.func_150304_a(x, gs.getItemStack().writeToNBT(new NBTTagCompound()));
			  
		  }
	  }
	
	 if(fluid){
		 //your shits, my pain
		 is.getTagCompound().setTag("Inputs",
		encodedValue.getTagList("in", 10).copy());
	 }
	 
	// System.out.println(is.getTagCompound());
  
	return is.copy();
}
public void fix(IInterfaceViewable iface,Runnable succ){
	iface.getTileEntity().markDirty();
	IInventory inv = iface.getPatterns();
	for(int i=0;i<inv.getSizeInventory();i++){
		ItemStack is = inv.getStackInSlot(i);
		if(is!=null&&is.getItem() instanceof ItemEncodedPattern){
			inv.setInventorySlotContents(i,fix(is.copy(),succ));
			
		}
	}
	
}
@Override
public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	p_77624_3_.add(StatCollector.translateToLocal("item.proghatch_circuit_fixer.name.tooltip"));
	super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	//throw new RuntimeException();
}

}
