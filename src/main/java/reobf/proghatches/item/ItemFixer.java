package reobf.proghatches.item;

import org.spongepowered.libraries.com.google.common.base.Optional;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHost;
import appeng.core.localization.GuiText;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.PatternHelper;
import appeng.items.misc.ItemEncodedPattern;
import appeng.me.GridAccessException;
import appeng.me.helpers.IGridProxyable;
import appeng.util.Platform;
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
	
	if(host instanceof IInterfaceHost)
	fix((IInterfaceHost) host,()->cnt[0]++);
	});	
	
	System.out.println("Player tried to use fixer to fix patterns, if this info spams in log and server gets laggy, blame 'em.");
	System.out.println("player:"+p_77648_2_.getDisplayName()+",fix:"+cnt[0]+",dim:"+p_77648_2_.getEntityWorld().provider.dimensionId);
	p_77648_2_.addChatMessage(new ChatComponentTranslation("proghatch.itemfixer.info",cnt[0]));
	if(cnt[0]==0)p_77648_2_.addChatMessage(new ChatComponentTranslation("proghatch.itemfixer.warn"));

	}
	
	
	return true;
}

public ItemStack fixCircuit(ItemStack is,Runnable succ){
	if(is.getItem()!=MyMod.progcircuit)return null;
	is=is.copy();
	int ver=ItemProgrammingCircuit.isNew(is);
	if(ver==0||ver==1){
	if(	MixinCallback.fixCircuitTag(is.stackTagCompound)){
		succ.run();
	}
	}
	return is;
}
public ItemStack fix(ItemStack is,Runnable succ){
	//ICraftingPatternItem item=(ICraftingPatternItem) is.getItem();
	 final ItemStack unknownItem = new ItemStack(Blocks.fire);
      unknownItem.setStackDisplayName(GuiText.UnknownItem.getLocal());
      is=is.copy();
      //item.getPatternForItem(is, null);
	 final NBTTagCompound encodedValue = (NBTTagCompound) is.getTagCompound();
	 NBTTagList in = encodedValue.getTagList("in", 10);
	  for (int x = 0; x < in.tagCount(); x++) {
		  ItemStack gs = Platform.loadItemStackFromNBT(in.getCompoundTagAt(x));
		  if(gs==null)continue;
		  gs=fixCircuit(gs,succ);
		  if(gs==null)continue;
		  gs=gs.copy();
		  if(gs.stackSize<=0)gs.stackSize=1;
		  in.func_150304_a(x, Platform.writeItemStackToNBT(gs.copy(), new NBTTagCompound()).copy());
	  }
	 
	 
	 
	 
  
	return is.copy();
}
public void fix(IInterfaceHost iface,Runnable succ){
	iface.getTileEntity().markDirty();
	IInventory inv = iface.getPatterns();
	for(int i=0;i<inv.getSizeInventory();i++){
		ItemStack is = inv.getStackInSlot(i);
		if(is!=null&&is.getItem() instanceof ItemEncodedPattern){
			inv.setInventorySlotContents(i,fix(is.copy(),succ));
			
		}
	}
	
}


}
