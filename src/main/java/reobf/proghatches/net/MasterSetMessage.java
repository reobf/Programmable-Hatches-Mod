package reobf.proghatches.net;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatchInventoryMappingSlave;

public class MasterSetMessage implements IMessage {

    private int mx;
    private int my;
    private int mz;
    private boolean set;

    public MasterSetMessage() {}

    public MasterSetMessage(int x, int y, int z, PatternDualInputHatchInventoryMappingSlave te) {
        this.x = x;
        this.y = y;
        this.z = z;
        if(te!=null){
        this.mx = te.masterX;
        this.my = te.masterY;
        this.mz = te.masterZ;
        this.set = te.masterSet;}else{
        	
        	my=Integer.MAX_VALUE;
        	
        	
        }

    }

    public static class Handler implements IMessageHandler<MasterSetMessage, MasterSetMessage> {

        @Override
        public MasterSetMessage onMessage(MasterSetMessage message, MessageContext ctx) {

            try {

                PatternDualInputHatchInventoryMappingSlave te = ((PatternDualInputHatchInventoryMappingSlave) ((IGregTechTileEntity) Minecraft
                    .getMinecraft().thePlayer.getEntityWorld()
                        .getTileEntity(message.x, message.y, message.z)).getMetaTileEntity());
                if( message.my!=Integer.MAX_VALUE){
                te.masterSet = message.set;
                te.masterX = message.mx;
                te.masterY = message.my;
                te.masterZ = message.mz;
                }
                
                te.shouldDisplayMaster= message.my!=Integer.MAX_VALUE;
                
                ;
            } catch (Exception w) {
                w.printStackTrace();
            }

            return null;
        }
    }

    int x, y, z;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        mx = buf.readInt();
        my = buf.readInt();
        mz = buf.readInt();
        set = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(mx);
        buf.writeInt(my);
        buf.writeInt(mz);
        buf.writeBoolean(set);
    }
}
