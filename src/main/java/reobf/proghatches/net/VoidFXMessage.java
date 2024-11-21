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
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.block.ChunkTrackingGridCahce.ChunkInfo;
import reobf.proghatches.gt.metatileentity.VoidOutputBus;
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
	public VoidFXMessage(IGregTechTileEntity thiz, ItemStack aStack) {
		this.x=thiz.getXCoord();
		this.y=thiz.getYCoord();
		this.z=thiz.getZCoord();
		id=-1;is=aStack;
		//aStack.writeToNBT(new NBTTagCompound()).;
	}
	int x,y,z,id,amount;ItemStack is;
	
	public static class Handler implements IMessageHandler<VoidFXMessage, VoidFXMessage> {

		@Override
		public VoidFXMessage onMessage(VoidFXMessage message, MessageContext ctx) {
		
			try{
			
			if(message.id!=-1)
			try{
			((VoidOutputHatch)
			((IGregTechTileEntity)Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z)
			).getMetaTileEntity()).addVisual(
					new FluidStack(FluidRegistry.getFluid(message.id), message.amount)
					);
			
			;
			}catch(Exception e){e.printStackTrace();}
		else
			try{
				((VoidOutputBus)
				((IGregTechTileEntity)Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z)
				).getMetaTileEntity()).addVisual(
						message.is
						);
				
				;
				}catch(Exception e){e.printStackTrace();}
			
			
			
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
		
		 int len=buf.readInt();
		 byte[] b=new byte[len];
		 buf.readBytes(b);
		 if(id==-1)
		 try {is=ItemStack.loadItemStackFromNBT(
			CompressedStreamTools.func_152457_a(b, new NBTSizeTracker(Long.MAX_VALUE)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		 
		 
	}
	@Override
	public void toBytes(ByteBuf buf) {
	
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		buf.writeInt(id);
		buf.writeInt(amount);
		if(is!=null)
		try {
			byte[] b=CompressedStreamTools.compress(
					is.writeToNBT(new NBTTagCompound()));
			buf.writeInt(b.length);
			buf.writeBytes(b);
		} catch (Exception e) {
			buf.writeInt(0);
			e.printStackTrace();
		}else buf.writeInt(0);
		
		
	}
}
