package reobf.proghatches.net;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatchInventoryMappingSlave;
import reobf.proghatches.main.MyMod;

public class TryOpenPatternCIRBMessage implements IMessage {

  

    public TryOpenPatternCIRBMessage() {}

    public TryOpenPatternCIRBMessage(int x, int y, int z, PatternDualInputHatchInventoryMappingSlave master) {
        this.x = x;
        this.y = y;
        this.z = z;
      this.has=master.getMaster()!=null;
      master.playerConfigClient=has;
      MyMod.LOG.info("Has CRIB master:"+has);
    }

    public static class Handler implements IMessageHandler<TryOpenPatternCIRBMessage, TryOpenPatternCIRBMessage> {

        @SuppressWarnings({ "unchecked", "rawtypes", "rawtypes", "rawtypes" })
		@Override
        public TryOpenPatternCIRBMessage onMessage(TryOpenPatternCIRBMessage message, MessageContext ctx) {

          

                PatternDualInputHatchInventoryMappingSlave te = ((PatternDualInputHatchInventoryMappingSlave) ((IGregTechTileEntity) 
                		ctx.getServerHandler().playerEntity.getEntityWorld()
                        .getTileEntity(message.x, message.y, message.z)).getMetaTileEntity());
              
                
            if(
                		(te.getMaster()==null)//has no master on server
                		&&
                		(message.has)//but has master on client??
			) {

				MyMod.LOG.error("Cannot open CRIB.");
				return null;

			}
                
            
          
        if(
            		(te.getMaster()!=null)//has master on server
            		&&
            		(!message.has)//has no master on client, this is normal just log it
		) {

			MyMod.LOG.error("Cannot read master on client, not showing master.");
			

		} 
        MyMod.LOG.info("Client player:"+ctx.getServerHandler().playerEntity.getDisplayName()+" has CRIB master:"+message.has);
               te.playerConfig.put(ctx.getServerHandler().playerEntity,message.has);
              if(te.getMaster()!=null)
               MyMod.net.sendTo(
                       new MasterSetMessage(
                          te.getBaseMetaTileEntity().getXCoord(),
                          te.getBaseMetaTileEntity().getYCoord(),
                          te.getBaseMetaTileEntity().getZCoord(),
                           te),
                       ctx.getServerHandler().playerEntity);
               
               
                te.openGui(ctx.getServerHandler().playerEntity);
               
            return null;//new PatternCRIBOpenMessage(message.x, message.y, message.z, message.has);
        }
    }

    int x, y, z;
    //not really 'hasMasterm', actually it means 'can read master on client'
    boolean has;
    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
     
        has = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);

        buf.writeBoolean(has);
    }
}
