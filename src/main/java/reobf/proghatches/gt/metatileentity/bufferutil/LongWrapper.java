package reobf.proghatches.gt.metatileentity.bufferutil;

public class LongWrapper {
public long val;
public LongWrapper(long v){
	val=v;
	
}
public int i(){
	
	return (int) Math.min(val, Integer.MAX_VALUE);
}
}
