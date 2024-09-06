package reobf.proghatches.block;

import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;

public class TileReactorSyncer extends TileEntity{

	
	private boolean isDead;

	@Override
	public void validate() {
	super.validate();
	MyMod.callbacks.put(this,this::pretick);
	}
	
	
	public void pretick(){
		if((!isDead)&&(!isInvalid())){
			TileEntityNuclearReactorElectric reactor=findTarget();
			if(reactor!=null) tick=reactor.updateTicker%20;
		
			//int new_power=(tick>5&&tick<15)?15:0;
			int new_power=(tick!=0)?15:0;
			if(power!=new_power){
				
				//worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, blockType);
			
			worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType()
					, 0);
			//schedule it, update it in World#tick, just in case something magic happens...
			}
			
			power=new_power;
		}
	}
	
	
	public int power(){
	if(power<0){return 0;}
		return power;
	}
	public int tick;
	public int power=-1;//ic2 reactor reset its tick value everytime world loads, so it's no point saving the value
	public TileEntityNuclearReactorElectric findTarget(){
		
		ForgeDirection dir=ForgeDirection.values()[this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
		TileEntity te=this.worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
		
		if(te instanceof TileEntityNuclearReactorElectric)return (TileEntityNuclearReactorElectric) te;
		if(te instanceof TileEntityReactorChamberElectric){
			return ((TileEntityReactorChamberElectric) te).getReactor();}
		if(te instanceof TileEntityReactorAccessHatch){
			Object possible= ((TileEntityReactorAccessHatch) te).getReactor();
			return possible instanceof TileEntityNuclearReactorElectric?(TileEntityNuclearReactorElectric)possible:null;
		}
		return null;
	}
	
	@Override
    public void onChunkUnload() {
      
        super.onChunkUnload();
        isDead = true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        isDead = false;
    }
}
