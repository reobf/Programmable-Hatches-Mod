package reobf.proghatches.net;

import java.io.IOException;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import net.bdew.lib.network.NBTTagCompoundSerialize;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.DualInputHatch;

public class UpgradesMessage  implements IMessage {
	public UpgradesMessage(){}
	public UpgradesMessage(int x, int y, int z, DualInputHatch te) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.tag = te.shared.ser();

	}
	public static class Handler implements IMessageHandler<UpgradesMessage, UpgradesMessage> {

		@Override
		public UpgradesMessage onMessage(UpgradesMessage message, MessageContext ctx) {
			
			try{((DualInputHatch)
			(
			(IGregTechTileEntity)
			Minecraft.getMinecraft().thePlayer.getEntityWorld()
			.getTileEntity(message.x, message.y, message.z)).getMetaTileEntity()
			).shared.deser(message.tag);
			
			;}catch(Exception w){w.printStackTrace();}
			
			
			return null;
		}
}
int x,y,z;public NBTTagCompound tag;
	@Override
	public void fromBytes(ByteBuf buf) {
		x=buf.readInt();
		y=buf.readInt();
		z=buf.readInt();
		
		try {
			byte[] b=new byte[buf.readInt()];buf.readBytes(b);
			tag=CompressedStreamTools.func_152457_a(b,new NBTSizeTracker(Integer.MAX_VALUE));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		try {byte[] b = CompressedStreamTools.compress(tag);
		buf.writeInt(b.length);
			buf.writeBytes(b);
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}}