package reobf.proghatches.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.EUUtil;

//ModualrUI won't allow passing extra args, so store them in player tag.
public class OpenPartGuiMessage implements IMessage {
	public static class Handler implements IMessageHandler<OpenPartGuiMessage, OpenPartGuiMessage> {

		@Override
		public OpenPartGuiMessage onMessage(OpenPartGuiMessage message, MessageContext ctx) {
			EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
			NBTTagCompound tag = player.getEntityData();
			tag.setInteger(EUUtil.key, message.dir.ordinal());
			tag.setBoolean("extraarg", message.extraarg);

			return null;
		}
	}

	int x;
	int y;
	int z;
	ForgeDirection dir;
	boolean extraarg;

	public OpenPartGuiMessage() {
	}

	public OpenPartGuiMessage(int x, int y, int z, ForgeDirection dir) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.dir = dir;

	}

	public OpenPartGuiMessage mark(boolean extraarg) {
		this.extraarg = extraarg;
		return this;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		dir = ForgeDirection.getOrientation(buf.readInt());
		extraarg = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(dir.ordinal());
		buf.writeBoolean(extraarg);
	}

}
