package reobf.proghatches.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.ae.cpu.TileCPU;

public class JoinMessage  implements IMessage {
	 public static class Handler implements IMessageHandler<JoinMessage, JoinMessage> {

		@Override
		public JoinMessage onMessage(JoinMessage message, MessageContext ctx) {
		//TileCPU.init();
			return null;
		}}
	@Override
	public void fromBytes(ByteBuf buf) {
	}

	@Override
	public void toBytes(ByteBuf buf) {
	}

}
