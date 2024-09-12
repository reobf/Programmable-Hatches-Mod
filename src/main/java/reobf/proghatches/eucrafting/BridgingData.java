package reobf.proghatches.eucrafting;

import java.util.ArrayList;
import java.util.Optional;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.api.util.WorldCoord;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridConnection;
import appeng.me.helpers.AENetworkProxy;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.util.ISerializableObject;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.main.MyMod;

public class BridgingData implements Data {
	public IInterfaceHost getInterfaceOrNull(){return null;};
	public interface TriConsumer<a, b, c> {
		public void accept(a aa, b bb, c cc);

	}

	public void setTag(NBTTagCompound tagCompound) {
		tag = tagCompound;
	}

	public NBTTagCompound getTag() {
		return tag;
	}

	NBTTagCompound tag;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	DimensionalCoord pos = new DimensionalCoord(0, 0, 0, -1000);
	AENetworkProxy gridProxy;

	@Override
	public AENetworkProxy getGridProxy() {

		return gridProxy;
	}

	@Override
	public void setGridProxy(AENetworkProxy gridProxy) {
		this.gridProxy = gridProxy;

	}

	@Override
	public ForgeDirection getSide() {

		return side;
	}

	@Override
	public void setSide(ForgeDirection side) {
		this.side = side;
	}

	@Override
	public DimensionalCoord getPos() {

		return pos;
	}

	@Override
	public void setPos(DimensionalCoord pos) {
		this.pos = pos;

	}

	@Override
	public void update(ICoverable aTileEntity) {

		// TileEntity te=aTileEntity.getTileEntityAtSide(side);
		/*
		 * for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
		 * if(side==this.side)continue; Iterator<IGridConnection> it = this
		 * .getProxy() .getNode() .getConnections().iterator(); IGridConnection
		 * item=null; IGridNode thenode=null; ISerializableObject obj =
		 * aTileEntity.getComplexCoverDataAtSide(side); if(obj instanceof Data)
		 * thenode =((Data)obj).getGridNode(side); if(thenode==null){continue;}
		 * boolean found=false;
		 * 
		 * 
		 * //boolean
		 * thisSideValid=AECover.canConnect(thenode,side.getOpposite());
		 * 
		 * 
		 * 
		 * while(it.hasNext()){ item=it.next();
		 * if(item.a()==thenode||item.b()==thenode)
		 * 
		 * { found=true; break; } };
		 * 
		 * 
		 * 
		 * if(found==false){ try {IGridNode thiz = this.getProxy().getNode();
		 * new GridConnection(thiz, thenode, ForgeDirection.UNKNOWN);
		 * 
		 * MyMod.LOG.info("Bridging Node connect@"+this.getPos()+" "
		 * +this.side+"->"+side); } catch (FailedConnection e) {
		 * System.out.println(this.getProxy().getNode());
		 * System.out.println(thenode); e.printStackTrace(); }
		 * 
		 * }; }
		 */

		IGridNode thiz = this.getProxy().getNode();
		IReadOnlyCollection<IGridConnection> it = this.getProxy().getNode().getConnections();
		ArrayList<IGridNode> a = new ArrayList<>(it.size());
		it.forEach(s -> {
			if (s.a() == thiz)
				a.add(s.b());
			else
				a.add(s.a());
		});

		TriConsumer<IGridNode, String, ForgeDirection> tryConnect = (thenode, info, dir) -> {
			if (!a.contains(thenode)) {
				try {
					new GridConnection(thiz, thenode, ForgeDirection.UNKNOWN);

					MyMod.LOG.info(info + " Bridging Node connect@" + this.getPos() + " " + this.side + "->" + dir);
				} catch (FailedConnection e) {
					System.out.println(this.getProxy().getNode());
					System.out.println(thenode);
					e.printStackTrace();
				}

			}
		};

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (side == this.side)
				continue;
			ISerializableObject obj = aTileEntity.getComplexCoverDataAtSide(side);
			IGridNode thenode = null;
			if (obj instanceof Data)
				thenode = ((Data) obj).getGridNode(side);
			if (thenode == null) {
				continue;
			}

			tryConnect.accept(thenode, "Internal", side);
		}

		WorldCoord npos = this.getPos().copy().add(side, 1);
		Optional.ofNullable(this.getPos().getWorld().getTileEntity(npos.x, npos.y, npos.z))
				.map(s -> s instanceof ICoverable ? (ICoverable) s : null)
				.map(s -> s.getComplexCoverDataAtSide(side.getOpposite())).map(s -> s instanceof Data ? (Data) s : null)
				.map(s -> s.getProxy().getNode()).ifPresent(s -> tryConnect.accept(s, "External", side));

		/*
		 * for(ForgeDirection side:ForgeDirection.VALID_DIRECTIONS){
		 * if(side==this.side)continue;
		 * 
		 * }
		 */

		Data.super.update(aTileEntity);
	}

	

	@Override
	public boolean firstUpdate() {
		if (first) {
			first = false;
			return true;
		}
		return false;
	}

	boolean first = true;

	public boolean hasAEGUI() {
		return false;
	};

	@Override
	public TileEntity fakeTile() {

		return null;
	}

	public boolean requireChannel() {
		return false;
	}
}
