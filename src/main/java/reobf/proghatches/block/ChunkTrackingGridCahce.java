package reobf.proghatches.block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.util.DimensionalCoord;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.MyMod.Prop;

public class ChunkTrackingGridCahce implements IChunkTrackingGridCahce{
	public ChunkTrackingGridCahce(final IGrid g) {
	        this.myGrid = g;
	        callbacks.put(this, null);
	    }  
	private final IGrid myGrid;
	
	public static WeakHashMap<ChunkTrackingGridCahce,Object> callbacks=new WeakHashMap<>();
	
	
	
	 static class ChunkInfo implements Cloneable{
		 
		 public String toString() {
			 
			 return chunkx+" "+ chunky+" "+dim;
			 
		 };
		 ChunkInfo(int worldx,int worldy,World w){
			 this.chunkx=worldx>>4;
			 this.chunky=worldy>>4;
			 this.dim=w.provider.dimensionId;
		 }
		 final int chunkx,chunky;
		 final int dim;
		 @Override
		public boolean equals(Object obj) {
			if(obj==null)return false;
			if(!(obj instanceof ChunkInfo))return false;
			ChunkInfo other=(ChunkInfo)obj;
			return chunkx==other.chunkx&&
					chunky==other.chunky&&
					dim==other.dim;
		}			
		@Override
			public int hashCode() {
					return chunkx^chunky^dim;
			}	
		
		 
	 }
	 
	
	 public HashMap<ChunkInfo,Integer> track=new HashMap<>();
	 
	 
	 @Override
	public void onUpdateTick() {
		// System.out.println(this.hashCode()+" "+track);
		;
	}

	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine) {
		if(machine instanceof TileAnchorAlert){
			terminals.remove((TileAnchorAlert) machine);
		}
IGridBlock gb = gridNode.getGridBlock();
		
		if(gb.isWorldAccessible()){
			DimensionalCoord loc = gb.getLocation();
			World w= loc.getWorld();
			if(w==null){
				return;//?
			}
			
		Integer ret=track.merge(new ChunkInfo(loc.x, loc.z,w), -1, (a,b)->{
			int retu=a+b;
			return retu==0?null:retu;
			});
	
		
		}
		
	}
   LinkedList<TileAnchorAlert> terminals=new LinkedList<>();
	@Override
	public void addNode(IGridNode gridNode, IGridHost machine) {
		if(machine instanceof TileAnchorAlert){
			terminals.add((TileAnchorAlert) machine);
		}
		IGridBlock gb = gridNode.getGridBlock();
		
		if(gb.isWorldAccessible()){
			DimensionalCoord loc = gb.getLocation();
			World w= loc.getWorld();
			if(w==null){
				return;//?
			}
			
		track.merge(new ChunkInfo(loc.x, loc.z,w), 1, (a,b)->a+b);
		}
	}

	@Override
	public void onSplit(IGridStorage destinationStorage) {
	
		
	}

	@Override
	public void onJoin(IGridStorage sourceStorage) {
		
		
	}

	@Override
	public void populateGridStorage(IGridStorage destinationStorage) {
	
		
	}
	
	private int getMask(EntityPlayer p){
		MyMod.Prop pr=(Prop) p.getExtendedProperties(MyMod.GET_PROGHATCHBOOK);
		if(pr==null)return 0;//huh?
		return pr.alert_mask;
	}
	
	public boolean warnPrivate(EntityPlayer p,TileAnchorAlert source, ChunkInfo info,int i){
		if((getMask(p)&1)!=0)return false;
		
		
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert.private"
				,	"X:"+source.xCoord+",Y:"+source.yCoord+",Z:"+source.zCoord+",dim:"+source.getWorldObj().provider.dimensionId
				
				));
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert",
			
				"X:"+info.chunkx+",Z:"+info.chunky+",dim:"+info.dim+" "+
				"Center:"+((info.chunkx<<4)+8)+","+((info.chunky<<4)+8)
				
				
				,i
				
				));
		
		return true;
		
	}
	public boolean warnDimension(EntityPlayer p,TileAnchorAlert source, ChunkInfo info,int i){
		if((getMask(p)&2)!=0)return false;
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert.dim"
				,	"X:"+source.xCoord+",Y:"+source.yCoord+",Z:"+source.zCoord+",dim:"+source.getWorldObj().provider.dimensionId
				
				));
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert",
			
				"X:"+info.chunkx+",Z:"+info.chunky+",dim:"+info.dim+" "+
				"Center:"+((info.chunkx<<4)+8)+","+((info.chunky<<4)+8)
				
				
				,i
				
				));
		
		return true;
		
	}
	public boolean warnGlobal(EntityPlayer p,TileAnchorAlert source, ChunkInfo info,int i){
		if((getMask(p)&4)!=0)return false;
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert.global"
				,	"X:"+source.xCoord+",Y:"+source.yCoord+",Z:"+source.zCoord+",dim:"+source.getWorldObj().provider.dimensionId
				
				));
		((EntityPlayer)p).addChatMessage(new ChatComponentTranslation(
				"proghatch.chunk_loading_alert.alert",
			
				"X:"+info.chunkx+",Z:"+info.chunky+",dim:"+info.dim+" "+
				"Center:"+((info.chunkx<<4)+8)+","+((info.chunky<<4)+8)
				
				
				,i
				
				));
		
		return true;
		
	}
	@SuppressWarnings("rawtypes")
	public void warnAlt(ChunkInfo info,int i){
		HashMap<UUID,TileAnchorAlert> playersUUIDToInform=new HashMap<>();
		HashMap<World,TileAnchorAlert> dimensionsToInform=new HashMap<>();
		HashSet<TileAnchorAlert>  informAll=new HashSet();
		
		terminals.forEach(s->{
			if(s.mode==s.ALL){informAll.add(s);}
			if(s.mode==s.DIM){dimensionsToInform.put(s.getWorldObj(), s);}
			if(s.mode==s.OWNER){playersUUIDToInform.put(s.owner, s);}
		});
		
		HashSet<EntityPlayer> warned=new HashSet<>();
		
		List l=MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		List<EntityPlayer> player=l;
		for(EntityPlayer p:player){
			if(playersUUIDToInform.containsKey(p.getUniqueID())){
				warned.add(p);
				if(!warnPrivate(p, playersUUIDToInform.get(p.getUniqueID()),info,i)){
					warned.remove(p);
				};
				
			}
		}
		for(EntityPlayer p:player){
			if(dimensionsToInform.containsKey(p.getEntityWorld())){
				boolean already = warned.add(p);
				if(already){//true means not warned
					if(!warnDimension(p, dimensionsToInform.get(p.getEntityWorld()),info,i)){
						warned.remove(p);
					};
					
				}
				
			}
			
		}
		if(informAll.isEmpty()==false)
		for(EntityPlayer p:player){
			if(warned.contains(p)==false){
				
				if(!warnGlobal(p, informAll.iterator().next(),info,i)){
					//warned.remove(p);
					//do not care since there’re no fallbacks then...
				};
			}
			
		}
		
		
		
		
	}
	
	@SuppressWarnings("rawtypes")
	public void warn(ChunkInfo info,int i){
		
		
		HashSet<UUID> playersUUIDToInform=new HashSet<>();
		HashSet<World> dimensionsToInform=new HashSet<>();
		boolean[] informAll=new boolean[1];
		
		terminals.forEach(s->{
			if(s.mode==s.ALL){informAll[0]=true;}
			if(s.mode==s.DIM){dimensionsToInform.add(s.getWorldObj());}
			if(s.mode==s.OWNER){playersUUIDToInform.add(s.owner);}
		});
		HashSet<EntityPlayer> playersToInform=new HashSet<>();
		if(informAll[0]==false)
		if(playersUUIDToInform.isEmpty()==false){
			List l=MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			List<EntityPlayer> player=l;
			for(EntityPlayer p:player){
				if(playersUUIDToInform.contains(p.getUniqueID())){
					playersToInform.add(p);
				}
			}
		}
		if(informAll[0]==false)
		dimensionsToInform.forEach(s->{
			playersToInform.addAll(s.playerEntities);
		});
		
		if(informAll[0]){
			playersToInform.addAll(MinecraftServer.getServer().getConfigurationManager().playerEntityList);
		}
		
		
		
		
		
		for(Object o:playersToInform){
			if(((EntityPlayer)o).getEntityWorld().provider.dimensionId==info.dim)
			((EntityPlayer)o).addChatMessage(new ChatComponentTranslation(
					"proghatch.chunk_loading_alert.alert",
					
					"X:"+info.chunkx+",Z:"+info.chunky+" "+
					"Center:"+((info.chunkx<<4)+8)+","+((info.chunky<<4)+8)
					
					
					,i
					
					));;
		}
		
		
		System.out.println("Unload chunk:"+info+" with "+i+" Node(s) inside.");
		
	}
	
	
   public void unload(Chunk  o) {ChunkInfo info;
		Integer tck = track.get(info=new ChunkInfo(o.xPosition<<4, o.zPosition<<4, o.worldObj));
		if(tck!=null){
			warnAlt(info,tck);
		}
	}
	public void unload(World  o) {
		track.forEach((a,b)->{
			if(a.dim==o.provider.dimensionId){
				if(b!=null)
				warnAlt(a,b);
			}
		});
	}
	
	
	
	
}
