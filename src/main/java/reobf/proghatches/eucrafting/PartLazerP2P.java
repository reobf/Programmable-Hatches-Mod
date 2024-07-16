package reobf.proghatches.eucrafting;

import java.util.ArrayList;
import java.util.List;

import com.github.technus.tectech.mechanics.pipe.IConnectsToEnergyTunnel;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_DynamoTunnel;
import com.github.technus.tectech.thing.metaTileEntity.hatch.GT_MetaTileEntity_Hatch_EnergyTunnel;
import com.github.technus.tectech.thing.metaTileEntity.pipe.GT_MetaTileEntity_Pipe_Energy;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import appeng.api.AEApi;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AEColor;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.p2p.PartP2PTunnelStatic;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class PartLazerP2P
<S extends MetaTileEntity&IConnectsToEnergyTunnel,
D extends MetaTileEntity&IConnectsToEnergyTunnel
>

extends PartP2PTunnelStatic<PartLazerP2P> implements ILazer,IGridTickable{

	public PartLazerP2P(ItemStack is) {
		super(is);
	
	}

	@Override
	public boolean canConnect(ForgeDirection side) {
	
		return true;
	}

	@Override
	public byte getColorization() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public byte setColorization(byte aColor) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isHost() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ILazer getLazerP2PIn(ForgeDirection dir) {
	
		return this;
	}

	@Override
	public ForgeDirection getLazerDir() {
		
		return side;
	}

	@Override
	public List<ILazer> getLazerP2POuts() {
		if(this.output)return ImmutableList.of();
		List<ILazer> all=new ArrayList<>();
	    try {
			this.getOutputs().forEach(all::add);
		} catch (GridAccessException e) {
		}
		return all;
	}

	@Override
	public TileEntity findConnected() {
		// TODO Auto-generated method stub
		return ILazer.super.findConnected();
	}
	
	private List<D> collectAllEndpoints(){
		if(this.output)return ImmutableList.of();
		ArrayList<D> all=new ArrayList<>();
		try {
			getOutputs().forEach(s->{
				
				
			Object o=	s.getForward();
			if(o instanceof PartLazerP2P){
				all.addAll(((PartLazerP2P) o).collectAllEndpoints());
			}else if(isDist(o )){
				all.add( (D) o);
			}	
			
			
			});
		} catch (GridAccessException e) {}
		
		
		
		
		return all;
	}
	/**
	 * make it works both for tectech&bartworks
	 * */
	public boolean isSourece(Object o){
		if(o instanceof IConnectsToEnergyTunnel){
			if(o instanceof MetaTileEntity){
				
				
			
				return ((MetaTileEntity) o).maxEUOutput()>1;
			}
			
		}
		return false;
	}/**
	 * make it works both for tectech&bartworks
	 * */
	public boolean isDist(Object o){
		if(o instanceof IConnectsToEnergyTunnel){
			if(o instanceof MetaTileEntity){
				return ((MetaTileEntity) o).maxEUInput()>1;
			}
			
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private S getBackward() {
		
		 {TileEntity thiz = this.getHost().getTile();
     	ForgeDirection face=this.getLazerDir();
     	
      
         final ForgeDirection opposite = face;
         for (short dist = 1; dist < 1000; dist++) {
            
         	TileEntity rawtile = thiz
                     .getWorldObj().getTileEntity(
                     		thiz.xCoord+dist*opposite.offsetX,
                     		thiz.yCoord+dist*opposite.offsetY,
                     		thiz.zCoord+dist*opposite.offsetZ
                     		);
         	if(rawtile==null)return null;
         	IGregTechTileEntity tGTTileEntity  =rawtile instanceof IGregTechTileEntity?(IGregTechTileEntity)rawtile:null;
             if (tGTTileEntity != null) {
                 IMetaTileEntity aMetaTileEntity = tGTTileEntity.getMetaTileEntity();
                 if (aMetaTileEntity != null) {
                     if (isSourece(aMetaTileEntity )
                            // && ((IConnectsToEnergyTunnel)tGTTileEntity).canConnect(opposite.getOpposite()))
                    		 ){
                    	 
                         return (S) (aMetaTileEntity);
                     } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Pipe_Energy) {
                         if (((GT_MetaTileEntity_Pipe_Energy) aMetaTileEntity).connectionCount < 2) {
                         	   return null;
                         } else {
                             ((GT_MetaTileEntity_Pipe_Energy) aMetaTileEntity).markUsed();
                         }
                         continue;
                     } 
                 }
             }else{
             	
             	/*if(rawtile instanceof ILazer){
             		
             		return ((ILazer) rawtile).getLazerP2PIn(opposite);
             		
             		
             	}*/
             	
             }
             
             
             
             
         }
     }
		return null;
		
	}
	private Object getForward() {
	      
	        {TileEntity thiz = this.getHost().getTile();
	        	ForgeDirection face=this.getLazerDir();
	        	
	         
	            final ForgeDirection opposite = face;
	            for (short dist = 1; dist < 1000; dist++) {
	               
	            	TileEntity rawtile = thiz
	                        .getWorldObj().getTileEntity(
	                        		thiz.xCoord+dist*opposite.offsetX,
	                        		thiz.yCoord+dist*opposite.offsetY,
	                        		thiz.zCoord+dist*opposite.offsetZ
	                        		);
	            	if(rawtile==null)return null;
	            	IGregTechTileEntity tGTTileEntity  =rawtile instanceof IGregTechTileEntity?(IGregTechTileEntity)rawtile:null;
	                if (tGTTileEntity != null) {
	                    IMetaTileEntity aMetaTileEntity = tGTTileEntity.getMetaTileEntity();
	                    if (aMetaTileEntity != null) {
	                        if (isDist(aMetaTileEntity) 
	                              //  && opposite.getOpposite() == tGTTileEntity.getFrontFacing()
	                                ) {
	                            return ( aMetaTileEntity);
	                        } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Pipe_Energy) {
	                            if (((GT_MetaTileEntity_Pipe_Energy) aMetaTileEntity).connectionCount < 2) {
	                            	   return null;
	                            } else {
	                                ((GT_MetaTileEntity_Pipe_Energy) aMetaTileEntity).markUsed();
	                            }
	                            continue;
	                        } 
	                    }
	                }else{
	                	
	                	if(rawtile instanceof ILazer){
	                		
	                		return ((ILazer) rawtile).getLazerP2PIn(opposite);
	                		
	                		
	                	}
	                	
	                }
	                
	                
	                
	                
	            }
	        }
			return null;
	    }

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		
		return new TickingRequest(20, 20, false, false);//update every 20 ticks
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
	if(this.output){	return TickRateModulation.SAME;}
	S source = getBackward();
	List<D> dist = collectAllEndpoints();
	if(source!=null)
	moveForward(source,dist);
		
		
	return TickRateModulation.SAME;
	}
	public void moveForward(S s,List<D> d){
		long record[]=new long[]{s.maxAmperesOut() * 20L * s.maxEUOutput()};
		
		for(D dd:d){
			moveForward(s,dd, record);
			if(record[0]<=0){break;}
		}
	
	}
	public void moveForward(S s,D d
			,long[] max
			){
		
		if (s.maxEUOutput() > ( d).maxEUInput()) {
            d.doExplosion(s.maxEUOutput());
            s.setEUVar(s.getBaseMetaTileEntity().getStoredEU() - s.maxEUOutput());
            return;
        } else if (s.maxEUOutput()
                == ( d).maxEUInput()) {
        	
        	D aMetaTileEntity = d;
        	IGregTechTileEntity aBaseMetaTileEntity = s.getBaseMetaTileEntity();
                    long diff = Math.min(
                            s.maxAmperesOut() * 20L * s.maxEUOutput(),
                            Math.min(
                                    ( aMetaTileEntity)
                                            .maxEUStore()
                                            - aMetaTileEntity.getBaseMetaTileEntity().getStoredEU(),
                                    aBaseMetaTileEntity.getStoredEU()));
                    diff=Math.min(max[0], diff);
                    max[0]-=diff;
                    s.setEUVar(aBaseMetaTileEntity.getStoredEU() - diff);

                    d.setEUVar(d.getBaseMetaTileEntity().getStoredEU() + diff);
                }
	}
	
	 @Override
	    @SideOnly(Side.CLIENT)
	    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
		  rh.setTexture(Blocks.stone.getIcon(0,0));

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderInventoryBox(renderer);

	        rh.setTexture(
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.BlockP2PTunnel2.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.PartTunnelSides.getIcon());

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderInventoryBox(renderer);
	    }

	    /**
	     * @return If enabled it returns the icon of an AE quartz block, else vanilla quartz block icon
	     */
	    public IIcon getTypeTexture() {
	    
	     return Blocks
	    		 .gold_block.getIcon(0, 0);
	        
	    }

	    @Override
	    @SideOnly(Side.CLIENT)
	    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
	            final RenderBlocks renderer) {
	        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));
	        AEColor paint=this.getColor();
	    
	        
	        if(paint==AEColor.Transparent)
	        rh.setTexture(Blocks.stone.getIcon(0,0));
	        	else
	        rh.setTexture(
	        		Blocks.wool.getIcon(0,paint.ordinal())
	        		//this.getTypeTexture()
	        		);
	        
	        
	        

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setTexture(
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.BlockP2PTunnel2.getIcon(),
	                this.getItemStack().getIconIndex(),
	                CableBusTextures.PartTunnelSides.getIcon(),
	                CableBusTextures.PartTunnelSides.getIcon());

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setBounds(3, 3, 13, 13, 13, 14);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setTexture(CableBusTextures.BlockP2PTunnel3.getIcon());

	        rh.setBounds(6, 5, 12, 10, 11, 13);
	        rh.renderBlock(x, y, z, renderer);

	        rh.setBounds(5, 6, 12, 11, 10, 13);
	        rh.renderBlock(x, y, z, renderer);

	        this.renderLights(x, y, z, rh, renderer);
	    }
	    @Override
	    public ItemStack getItemStack(final PartItemStack type) {
	        

	        return super.getItemStack(type);
	    }
}
