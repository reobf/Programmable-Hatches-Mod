package reobf.proghatches.gt.metatileentity.util.mui2;

import java.io.IOException;
import java.util.function.Consumer;

import com.cleanroommc.modularui.api.IPacketWriter;
import com.cleanroommc.modularui.value.sync.SyncHandler;

import appeng.util.item.AEItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;


public abstract class SyncHandlerAEItem extends SyncHandler{

	public abstract  AEItemStack getOnServer();
	public  AEItemStack getOnClient() {
		return cache;
	}
	public  ItemStack getOnClientI() {
		if(cache==null)return null;
		return cache.getItemStack();
	}
	private int interval;
	 @Override
		public void detectAndSendChanges(boolean init) {
		
			 if(init||interval>20){
			 interval=0; }else{interval++;return;}
			 
		
		AEItemStack is=getOnServer();
		syncToClient(999, new IPacketWriter() {
			@Override
			public void write(PacketBuffer buffer) throws IOException {
				NBTTagCompound tag=new NBTTagCompound();
				if(is!=null)is.writeToNBT(tag);
				byte[] bla=CompressedStreamTools.compress(tag);
				buffer.writeInt(bla.length);
				buffer.writeBytes(bla);
			}	
			});
		}
		 AEItemStack cache; 
		@Override
		public void readOnClient(int id, PacketBuffer buf) throws IOException {
			if(id==999){
				byte[] bla=new byte[buf.readInt()];
				buf.readBytes(bla);
				NBTTagCompound tag = CompressedStreamTools.func_152457_a(bla, new NBTSizeTracker(Long.MAX_VALUE/2));
				AEItemStack is=(AEItemStack) AEItemStack.loadItemStackFromNBT(tag);
				cache=is;
				if(cb!=null)cb.accept(this);
			}
			
		}
		public SyncHandlerAEItem cb(Consumer<SyncHandlerAEItem> arg){;
		cb=arg;return this;}
		Consumer<SyncHandlerAEItem> cb;
		@Override
		public void readOnServer(int id, PacketBuffer buf) throws IOException {}
		

}
