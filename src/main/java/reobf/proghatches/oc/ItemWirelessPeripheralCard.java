package reobf.proghatches.oc;
import static reobf.proghatches.oc.WirelessPeripheralManager.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import gregtech.api.enums.SoundResource;
import gregtech.api.util.GT_Utility;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemWirelessPeripheralCard extends Item implements li.cil.oc.api.driver.item.HostAware {
	


@Override
public boolean worksWith(ItemStack stack) {
	
	return stack.getItem()==this;
}

@Override
public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
	
	return new Env(stack, host);
}
	public class Env implements ManagedEnvironment {
		private ItemStack stack;
		public Env(ItemStack stack, EnvironmentHost host) {
		this.host=host;
		this.stack=stack;
		}
		public EnvironmentHost host;
		private Node node=li.cil.oc.api.Network.newNode(this, Visibility.Network).
				  withConnector().
					create();
		 @Override
	        public void load(NBTTagCompound nbt) {
	        	Optional.ofNullable(nbt.getTag("node")).ifPresent(s->{if(node()!=null)node().load((NBTTagCompound) s);});

	        }
	        @Override
	        public void save(NBTTagCompound nbt) {
	        	NBTTagCompound t=new NBTTagCompound();
	        	Optional.ofNullable(node()).ifPresent(s->s.save(t));
	        	nbt.setTag("node", t);
	        }
		
		@Override
		public void onMessage(Message message) {
		
			
		}
		public void createTagIfAbsent(){
			if(stack.getTagCompound()==null)stack.setTagCompound(new NBTTagCompound());
		}
		public UUID getUUID(){
			createTagIfAbsent();
			return UUID.fromString(stack.getTagCompound().getString("remoteUUID"));
		}
		public void markBad(){
			createTagIfAbsent();
			stack.getTagCompound().setBoolean("isBad", true);
		}
		public boolean isBad(){
			createTagIfAbsent();
			return stack.getTagCompound().getBoolean("isBad");
		}
		public boolean isValid(){
		
			return (!isBad())&&Optional.ofNullable(stack.getTagCompound()).map(s->!s.getString("remoteUUID").isEmpty()).orElse(false);
		}
		
		@Override
		public void onDisconnect(Node node) {
			if(!isValid())return;
			if(node==this.node){//this means the card is removed
			WirelessPeripheralManager.remove(cards,getUUID());
			}else{//otherwise means another component is removed, just ignore
			}
			
		}
		
		@Override
		public void onConnect(Node node) {
			if(!isValid())return;
			
		if(WirelessPeripheralManager.cards.containsKey(getUUID())
		&&WirelessPeripheralManager.cards.get(getUUID())!=this.node){
				//channel in use, disable it
			markBad();
			return;
		}
		if(this.node!=null){
			//TODO: check if node is a Computer Case?
			WirelessPeripheralManager.add(cards,getUUID(), this.node);
		}
		}
		
		@Override
		public Node node() {
			
			return node;
		}
		boolean init;
		@Override
		public void update() {
		
		if(!init){init=true;
			if(isValid()&&node!=null){
				WirelessPeripheralManager.cards.put(this.getUUID(), node);
			}
			}
		}
		
		@Override
		public boolean canUpdate() {
		
			return true;
		}
	};


@Override
public String slot(ItemStack stack) {

	   return li.cil.oc.common.Slot.Card();
}

@Override
public int tier(ItemStack stack) {

	return 1;
}

@Override
public NBTTagCompound dataTag(ItemStack stack) {
	
 return null;
}
@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
	
	if(player.isSneaking()){
		GT_Utility.doSoundAtClient(SoundResource.IC2_TOOLS_OD_SCANNER, 1, 1.0F,player.posX,player.posY,player.posZ);
		
		itemStackIn.setTagCompound(null);
	}	
		return super.onItemRightClick(itemStackIn, worldIn, player);
	}
@SuppressWarnings("unchecked")
@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
	
	int i = 0;
    while (true) {
        String k = "item.proghatches.oc.peripheral_card.tooltip";
        if (StatCollector.translateToLocal(k)
            .equals(
                Integer.valueOf(i)
                    .toString())) {
            break;
        }
        String key = k + "." + i;
        String trans = StatCollector.translateToLocal(key);

        p_77624_3_.add(trans);
        i++;
    }
	
	
	NBTTagCompound tag = p_77624_1_.getTagCompound();
	if(tag==null){
		p_77624_3_.add(StatCollector.translateToLocal("item.proghatches.oc.peripheral_card.tooltip.unbound"));
	}else{
		if(tag.getBoolean("isBad")){
			p_77624_3_.add(StatCollector.translateToLocal("item.proghatches.oc.peripheral_card.tooltip.bad"));	
		}else
		p_77624_3_.add(StatCollector.translateToLocalFormatted("item.proghatches.oc.peripheral_card.tooltip.valid",tag.getString("remoteUUID")));
	}
	
	
	
		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}
@Override
public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {

	return stack.getItem()==this;
}
}
