package reobf.proghatches.eucrafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import gregtech.api.enums.GT_Values;
import reobf.proghatches.main.MyMod;

public interface IEUManager extends IGridCache{
	public long inject(ISource s,long amp,long v);
	public void voltageChanged(IDrain gridNode);
	public interface ISource{
		public long getVoltage();
		public void reset();
		/*public long request(long amp);
		public void refund(long amp);
		public long providing();*/
		
	}
	public void refund(UUID id,long amount);
	public interface IDrain{
		public default boolean isP2POut(){return false;}
		public long getVoltage();
		
		public default long inject(long a,long v){
			accept(a);
			return doInject(a,v);
		};
		
		public UUID getUUID();
		public void refund(long amp);
		
		public long doInject(long a, long v);
		public long expectedAmp();
		public void accept(long a);
		public void reset();
		
		
	}
	
	public static class EUManager implements IEUManager{
		 public IGrid myGrid;
		 public Multimap<Long,IDrain> cache=HashMultimap.create();
		 public ArrayList<ISource> cache2=new ArrayList<>();
		public EUManager(final IGrid g) {
		        this.myGrid = g;
		      
		    }

		
		public long inject(ISource s,long amp,long v){
			if(amp==0){return 0;}
			//long v=s.getVoltage(); //use actual voltage
			long[] a=new long[]{amp};
			cache.get(s.getVoltage()).stream()
			.filter(sx->!sx.isP2POut())
			.map(d->{
				long val=Math.min(a[0], d.expectedAmp());
				return (a[0]-=d.inject( val,v));
			})
			.filter(l->l==0)//short-cut the first iteration that makes a[0]==0
			.findFirst()
			.orElse(0l);
			return amp-a[0];
		}
		
		@Override
		public void onUpdateTick() {
			
			cache.values().forEach(IDrain::reset);
			cache2.forEach(ISource::reset);
			
			
		}

		@Override
		public void removeNode(IGridNode gridNode, IGridHost machine) {
		
			IGridHost mach = gridNode.getMachine();
			if(mach instanceof IDrain){
				cache.values().removeIf(s->s==mach);
			}else
			if(mach instanceof ISource){
				cache2.remove((ISource) mach);
			}
		}

		@Override
		public void addNode(IGridNode gridNode, IGridHost machine) {
			IGridHost mach = gridNode.getMachine();
			if(mach instanceof IDrain){
				cache.put(((IDrain)mach).getVoltage(), (IDrain) mach);
			}else
			if(mach instanceof ISource){
				cache2.add((ISource) mach);
			}
			
			//ISource is not cached
		}
public void voltageChanged(IDrain gridNode){
	cache.entries().removeIf(s->s.getValue()==gridNode);
	cache.put(gridNode.getVoltage(),  gridNode);
}


		@Override
		public void onSplit(IGridStorage destinationStorage) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onJoin(IGridStorage sourceStorage) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void populateGridStorage(IGridStorage destinationStorage) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void refund(UUID id, long amount) {
		if(id.getLeastSignificantBits()==0&&0==id.getMostSignificantBits()){
			return;
		}
	
	     for(IDrain	s:cache.values()){
			
			if(s.getUUID().equals(id)){
				
				s.refund(amount);
				return;
			}
			
		};
		
		
		
			
		}
}
	
	
	
	
}
