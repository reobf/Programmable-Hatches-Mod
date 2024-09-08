package reobf.proghatches.net;

import java.io.IOException;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import io.netty.buffer.ByteBuf;
import net.bdew.lib.network.NBTTagCompoundSerialize;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.gt.metatileentity.PatternDualInputHatchInventoryMappingSlave;

public class MasterSetMessage  implements IMessage {
	private int mx;
	private int my;
	private int mz;
	private boolean set;
	public MasterSetMessage(){}
	public MasterSetMessage(int x, int y, int z, PatternDualInputHatchInventoryMappingSlave te) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.mx=te.masterX;
		this.my=te.masterY;
		this.mz=te.masterZ;
		this.set=te.masterSet;
		
	}
	public static class Handler implements IMessageHandler<MasterSetMessage, MasterSetMessage> {

		@Override
		public MasterSetMessage onMessage(MasterSetMessage message, MessageContext ctx) {
			
			try{
				
				PatternDualInputHatchInventoryMappingSlave te=((PatternDualInputHatchInventoryMappingSlave)
			(
			(IGregTechTileEntity)
			Minecraft.getMinecraft().thePlayer.getEntityWorld()
			.getTileEntity(message.x, message.y, message.z)).getMetaTileEntity()
			);
			
			te.masterSet=message.set;
			te.masterX=message.mx;
			te.masterY=message.my;
			te.masterZ=message.mz;
			
			;}catch(Exception w){w.printStackTrace();}
			
			
			return null;
		}
}
int x,y,z;
	@Override
	public void fromBytes(ByteBuf buf) {
		x=buf.readInt();
		y=buf.readInt();
		z=buf.readInt();
		mx=buf.readInt();
		my=buf.readInt();
		mz=buf.readInt();
		set=buf.readBoolean();
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
	}}