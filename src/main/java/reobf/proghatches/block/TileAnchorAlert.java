package reobf.proghatches.block;

import java.util.EnumSet;
import java.util.UUID;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridNode;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class TileAnchorAlert extends TileEntity implements IGridProxyable{
	public static final int ALL = 0;
	public static final int DIM = 1;
	public static final int OWNER = 2;
	int mode;
	//2 inform owner only
	//1 inform all players in same dim
	//0 inform all online player
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
	
		return createProxy().getNode();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {
		
		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {
	
	}

	@Override
	public AENetworkProxy getProxy() {
		
		return createProxy();
	}
	AENetworkProxy proxy;
    protected AENetworkProxy createProxy() {
    	if(proxy!=null)return proxy;
    	
    	proxy=new AENetworkProxy(this, "proxy", new ItemStack(MyMod.alert), true);
    	
    	//proxy.setFlags(GridFlags.REQUIRE_CHANNEL);
    	proxy.setValidSides(EnumSet.range(ForgeDirection.DOWN, ForgeDirection.EAST));
        return proxy; }
	@Override
	public DimensionalCoord getLocation() {
	
		return new DimensionalCoord(this);
	}

	@Override
	public void gridChanged() {
		
		
	}
	UUID owner;
    
    
	public void mark(EntityPlayer placer) {
	createProxy().setOwner((EntityPlayer) placer);
	owner=placer.getUniqueID();
	}
	@Override
	public void readFromNBT(NBTTagCompound compound) {	
		mode=compound.getInteger("m");
		owner=ProghatchesUtil.deser(compound, "OWNER_UUID");
		if(owner.getLeastSignificantBits()==0&&owner.getMostSignificantBits()==0)owner=null;
		createProxy().readFromNBT(compound);
		super.readFromNBT(compound);
	}
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		compound.setInteger("m", mode);
		if(owner!=null)
		ProghatchesUtil.ser(compound, owner, "OWNER_UUID");
	    createProxy().writeToNBT(compound);
		super.writeToNBT(compound);
	}
@Override
public void updateEntity() {
	
	super.updateEntity();
	if(!getProxy().isReady())
	getProxy().onReady();
}
public void onChunkUnload() {
	
	this.getProxy().onChunkUnload();
}

public void invalidate() {
	
	this.getProxy().invalidate();
}

@Override
public void validate() {
	this.getProxy().validate();
}


}
