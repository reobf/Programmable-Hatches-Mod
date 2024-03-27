package reobf.proghatches.net;

import appeng.client.gui.widgets.GuiProgressBar.Direction;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.EUUtil;

public class OpenPartGuiMessage implements IMessage{
	public static class Handler implements IMessageHandler<OpenPartGuiMessage, OpenPartGuiMessage>{

		@Override
		public OpenPartGuiMessage onMessage(OpenPartGuiMessage message, MessageContext ctx) {
			EntityPlayerMP player = ctx.getServerHandler().playerEntity;
			NBTTagCompound tag = player.getEntityData();
			tag.setInteger(EUUtil.key,message.dir.ordinal());
			EUUtil.PART_MODULAR_UI
			.open(player, player.worldObj, message.x, message.y, message.z);
			return null;
		}}
	
	int x;int y;int z;ForgeDirection dir;
	public OpenPartGuiMessage(){}
	public OpenPartGuiMessage(int x,int y,int z,ForgeDirection dir){
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

}
