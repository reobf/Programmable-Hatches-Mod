package reobf.proghatches.net;

import java.util.HashSet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.Minecraft;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.block.ChunkTrackingGridCahce.ChunkInfo;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch;

public class VoidFXMessage implements IMessage{
	public VoidFXMessage(){}
	public VoidFXMessage(IGregTechTileEntity thiz,FluidStack f) {
this.x=thiz.getXCoord();
this.y=thiz.getYCoord();
this.z=thiz.getZCoord();
id=f.getFluidID();
amount=f.amount;
	}
	int x,y,z,id,amount;
	
	public static class Handler implements IMessageHandler<VoidFXMessage, VoidFXMessage> {

		@Override
		public VoidFXMessage onMessage(VoidFXMessage message, MessageContext ctx) {
		
			try{
			((VoidOutputHatch)
			((IGregTechTileEntity)Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z)
			).getMetaTileEntity()).addVisual(
					
					
					new FluidStack(FluidRegistry.getFluid(message.id), message.amount)
					
					
					
					);
			
			;
			}catch(Exception e){e.printStackTrace();}
			return null;
		}
}
	@Override
	public void fromBytes(ByteBuf buf) {
		
	x=buf.readInt();
	y=buf.readInt();
	z=buf.readInt();
		 id=buf.readInt();
		 amount=buf.readInt();
		
	}
	@Override
	public void toBytes(ByteBuf buf) {
	
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(id);
		buf.writeInt(amount);
	}
}
