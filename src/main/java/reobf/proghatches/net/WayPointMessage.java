package reobf.proghatches.net;

import java.util.HashSet;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import journeymap.client.model.Waypoint;
import journeymap.client.waypoint.WaypointStore;
import net.minecraft.util.StatCollector;
import reobf.proghatches.block.ChunkTrackingGridCahce.ChunkInfo;

public class WayPointMessage implements IMessage{
	public WayPointMessage(){}
	public WayPointMessage(HashSet<ChunkInfo> improperlyUnloaded) {
	this.improperlyUnloaded=improperlyUnloaded;
	}
	
	HashSet<ChunkInfo> improperlyUnloaded;
	public static class Handler implements IMessageHandler<WayPointMessage, WayPointMessage> {

		@Override
		public WayPointMessage onMessage(WayPointMessage message, MessageContext ctx) {
		
			message.improperlyUnloaded.forEach(s->{
			journeymap.client.model.Waypoint deathpoint = journeymap.client.model.Waypoint
					.at((s.chunkx<<4)+8, 0, (s.chunky<<4)+8, Waypoint.Type.Normal,s.dim)
					;
			deathpoint.setName(
					StatCollector.translateToLocal("proghatch.waypoint.info"));
	        WaypointStore.instance().save(deathpoint);
	        
			
			}
			);
			
			
			return null;
		}
}
	@Override
	public void fromBytes(ByteBuf buf) {
		
		int size=buf.readInt();
		improperlyUnloaded=new HashSet<>();
		for(int i=0;i<size;i++){
			
			
			improperlyUnloaded.add(new ChunkInfo(buf.readByte(), buf.readByte(), buf.readByte()));
			
			
		}
		
		
		
	}
	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(improperlyUnloaded.size());
		
		improperlyUnloaded.forEach((s)->
		{
				buf.writeByte(s.chunkx);
				buf.writeByte(s.chunky);
				buf.writeByte(s.dim);
		}
				);	
		
		
	}
}
