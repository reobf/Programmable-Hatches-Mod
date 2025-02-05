package reobf.proghatches.net;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.main.MyMod;

public class RenameMessage implements IMessage {

    public static class Handler implements IMessageHandler<RenameMessage, RenameMessage> {

        @Override
        public RenameMessage onMessage(RenameMessage m, MessageContext ctx) {
            ctx.getServerHandler().playerEntity.openGui(
                MyMod.instance,
                m.dir.ordinal() | 0B10000,
                ctx.getServerHandler().playerEntity.worldObj,
                m.x,
                m.y,
                m.z);
            return null;
        }
    }

    public RenameMessage() {}

    public RenameMessage(int x, int y, int z, ForgeDirection dir) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;

    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dir = ForgeDirection.getOrientation(buf.readInt());

    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(dir.ordinal());

    }

    int x;
    int y;
    int z;
    ForgeDirection dir;

}
