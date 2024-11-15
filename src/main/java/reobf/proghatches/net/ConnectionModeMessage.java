package reobf.proghatches.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import crazypants.enderio.conduit.ConnectionMode;
import crazypants.enderio.conduit.IConduit;
import crazypants.enderio.conduit.IConduitBundle;
import crazypants.enderio.conduit.packet.ConTypeEnum;
import crazypants.enderio.conduit.packet.PacketConnectionMode;
import crazypants.enderio.conduit.redstone.IInsulatedRedstoneConduit;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eio.ICraftingMachineConduit;

public class ConnectionModeMessage  implements IMessage{
	int x,y,z;
 protected IConduit getTileCasted(MessageContext ctx) {
		        World world = ctx.getServerHandler().playerEntity.getEntityWorld();
		        if (world == null) {
		            return null;
		        }
		        TileEntity te = world.getTileEntity(x, y, z);
		        /*if (!(te instanceof IConduitBundle)) {
		            return null;
		        }*/
		        if (te==null||!te.getClass().getName().contains("crazypants.enderio.conduit.TileConduitBundle")) {
		            return null;
		        }
		        for(Method m:te.getClass().getMethods()){
		        	if(m.getName().equals("getConduit")){
		        		
		        		try {
							return (IConduit) m.invoke(te, ICraftingMachineConduit.class);
						} catch (Exception e) {
							e.printStackTrace();
						}break;
		        		
		        	}
		        	
		        }
		        
		        
		        
		        return  null;//bundle.getConduit(ICraftingMachineConduit.class);
		    }
	public static class Handler implements IMessageHandler<ConnectionModeMessage, ConnectionModeMessage> {
		
		 @Override
		    public ConnectionModeMessage onMessage(ConnectionModeMessage message, MessageContext ctx) {
		        //if (isInvalidPacketForGui(message, ctx)) return null;
		        IConduit conduit = message.getTileCasted(ctx);
		        if (conduit == null) {
		            return null;
		        }
		        if (conduit instanceof IInsulatedRedstoneConduit) {
		            ((IInsulatedRedstoneConduit) conduit).forceConnectionMode(message.dir, message.mode);
		        } else {
		            conduit.setConnectionMode(message.dir, message.mode);
		        }
		        ctx.getServerHandler().playerEntity.worldObj.markBlockForUpdate(message.x, message.y, message.z);
		        return null;
		    }
	
	
	}
	 private ForgeDirection dir;
	    private ConnectionMode mode;

	    public ConnectionModeMessage() {}

	    public ConnectionModeMessage(IConduit con, ForgeDirection dir) {
	      //  super(con.getBundle().getEntity(), ConTypeEnum.get(con));
	    	try{TileEntity te=(TileEntity)con.getClass().getMethod("getBundle").invoke(con);
	    	x=te.xCoord;
	    	y=te.yCoord;
	    	z=te.zCoord;
	        this.dir = dir;
	        mode = con.getConnectionMode(dir);}catch(Exception e){e.printStackTrace();}
	    }

	    @Override
	    public void toBytes(ByteBuf buf) {
	       
	        buf.writeShort(dir.ordinal());
	        buf.writeShort(mode.ordinal());
	        buf.writeInt(x);
	        buf.writeInt(y);
	        buf.writeInt(z);
	        
	    }

	    @Override
	    public void fromBytes(ByteBuf buf) {
	       
	        dir = ForgeDirection.values()[buf.readShort()];
	        mode = ConnectionMode.values()[buf.readShort()];
	        x=buf.readInt();
	        y=buf.readInt();
	        z=buf.readInt();
	        
	    }

	   
}
