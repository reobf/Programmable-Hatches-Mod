package reobf.proghatches.net;

import java.io.IOException;
import java.util.HashSet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.ae.TileMolecularAssemblerInterface;
import reobf.proghatches.block.ChunkTrackingGridCahce.ChunkInfo;
import reobf.proghatches.gt.metatileentity.VoidOutputBus;
import reobf.proghatches.gt.metatileentity.VoidOutputHatch;

public class MAFXMessage implements IMessage{
	public MAFXMessage(){}
	public MAFXMessage(TileEntity thiz,int f) {
this.x=thiz.xCoord;
this.y=thiz.yCoord;
this.z=thiz.zCoord;
id=f;

	}

	int x,y,z,id,amount;ItemStack is;
	
	public static class Handler implements IMessageHandler<MAFXMessage, MAFXMessage> {

		@Override
		public MAFXMessage onMessage(MAFXMessage message, MessageContext ctx) {
		try{
			
			TileEntity te=Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
			if(te instanceof TileMolecularAssemblerInterface){
				
				TileMolecularAssemblerInterface ma=(TileMolecularAssemblerInterface) te;
				if(message.id==0){ma.xfx=0.1f;}
				if(message.id==1){ma.yfx=0.1f;}
				if(message.id==2){ma.zfx=0.1f;}
				
			}
			
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
	
		 
		 
	}
	@Override
	public void toBytes(ByteBuf buf) {
	
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(id);
		
		
		
	}
}
