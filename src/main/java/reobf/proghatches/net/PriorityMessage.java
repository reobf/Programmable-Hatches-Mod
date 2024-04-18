package reobf.proghatches.net;

import static gregtech.api.enums.GT_Values.NW;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.net.GT_Packet_SendCoverData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;

public class PriorityMessage implements IMessage{
	public static class Handler implements IMessageHandler<PriorityMessage, PriorityMessage>{

		@Override
		public PriorityMessage onMessage(PriorityMessage m, MessageContext ctx) {
			ctx.getServerHandler().playerEntity.openGui(MyMod.instance,
					m.dir.ordinal()|0B1000, 
					ctx.getServerHandler().playerEntity.worldObj,
					m.x,m.y,m.z);
			return null;
		}}
	public PriorityMessage(){}
	public PriorityMessage(int x,int y,int z,ForgeDirection dir){
		this.x=x;
		this.y=y;
		this.z=z;
		this.dir=dir;	
		
		
	}
	@Override
	public void fromBytes(ByteBuf buf) {
		x=buf.readInt();
		y=buf.readInt();
		z=buf.readInt();
		dir=ForgeDirection.getOrientation(buf.readInt());
		
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(dir.ordinal());
		
	}
	int x;int y;int z;ForgeDirection dir;


}
