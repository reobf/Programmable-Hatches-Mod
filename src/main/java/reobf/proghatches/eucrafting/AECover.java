package reobf.proghatches.eucrafting;

import static gregtech.api.enums.GT_Values.NW;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.UUID;

import com.glodblock.github.loader.ItemAndBlockHolder;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.ticking.ITickManager;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IReadOnlyCollection;
import appeng.helpers.IInterfaceHost;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UIInfos;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.net.GT_Packet_SendCoverData;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.ISerializableObject;
import gregtech.common.tileentities.machines.multi.GT_MetaTileEntity_AssemblyLine;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import reobf.proghatches.main.Config;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.StackTraceUtil;

public class AECover extends GT_CoverBehaviorBase<AECover.Data> {
	
	
	public static boolean mixinReady;//mixin will set this to true
	public static boolean getWailaBody;
	public static boolean getNBTData;
	public static interface IMemoryCardSensitive {
		//public boolean shiftClick(EntityPlayer entityPlayer);

		public default boolean memoryCard(EntityPlayer entityPlayer){return false;};
		
		
	}

	public AECover() {

		this(InterfaceData.class);

	}

	public AECover(Class<?> c) {
		super(Data.class);
		clazz = c;
	}

	Class<?> clazz;

	public static class DummyData implements Data {
		public IInterfaceHost getInterfaceOrNull(){return null;};
		public void setTag(NBTTagCompound tagCompound) {
		}

		public NBTTagCompound getTag() {
			return null;
		}
		@Override
		public boolean accept(ForgeDirection side, ICoverable aTileEntity, boolean onPlace) {
			return true;
		}
		@Override
		public ISerializableObject copy() {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public NBTBase saveDataToNBT() {
			// TODO Auto-generated method stub
			return new NBTTagCompound();
		}

		@Override
		public void writeToByteBuf(ByteBuf aBuf) {
			// TODO Auto-generated method stub

		}

		@Override
		public void loadDataFromNBT(NBTBase aNBT) {

		}

		@Override
		public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public AENetworkProxy getProxy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DimensionalCoord getLocation() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void gridChanged() {
			// TODO Auto-generated method stub

		}

		@Override
		public IGridNode getGridNode(ForgeDirection dir) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AECableType getCableConnectionType(ForgeDirection dir) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void securityBreak() {
			// TODO Auto-generated method stub

		}

		@Override
		public AENetworkProxy getGridProxy() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setGridProxy(AENetworkProxy gridProxy) {
			// TODO Auto-generated method stub

		}

		@Override
		public ForgeDirection getSide() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setSide(ForgeDirection side) {
			// TODO Auto-generated method stub

		}

		@Override
		public DimensionalCoord getPos() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setPos(DimensionalCoord pos) {
			// TODO Auto-generated method stub

		}

		@Override
		public Data newInst() {
			// TODO Auto-generated method stub
			return this;
		}

		@Override
		public boolean firstUpdate() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public TileEntity fakeTile() {
			// TODO Auto-generated method stub
			return null;
		}
	}
	static int h0="gregtech.crossmod.waila.GregtechWailaDataProvider".hashCode();
	public static interface Data extends ISerializableObject, IGridProxyable {
		
		
		default public boolean isWailaCall(){
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
					
					/*String s=t.fillInStackTrace().getStackTrace()[6].getMethodName();
					if (s.hashCode()==h0&&s.equals("getNBTData")) {
						return true;
					}
					 s=t.fillInStackTrace().getStackTrace()[5].getMethodName();
					if (s.hashCode()==h0&&s.equals("getNBTData")) {
						return true;
					}*/
			if(mixinReady){
				
				return getNBTData;
			}else{
				String s=StackTraceUtil.getCallerMethod(6);
				if (s.hashCode()==h0&&s.equals("gregtech.crossmod.waila.GregtechWailaDataProvider")) {
					return true;
				}
				 s=StackTraceUtil.getCallerMethod(5);
				if (s.hashCode()==h0&&s.equals("gregtech.crossmod.waila.GregtechWailaDataProvider")) {
					return true;
				}
			}
				
				
				
				
				
					
				
				}
			return false;
			}
		
		IInterfaceHost getInterfaceOrNull();
		default boolean hasModularGUI(){return false;}
		default String tagName(){
			NBTTagCompound tag = getTag();
			if(tag==null)return null;
			ItemStack is=new ItemStack(Items.apple);
			is.setTagCompound(tag);
			if(is.hasDisplayName())return is.getDisplayName();
			return null;
		}
		default String name(){return null;}
		default boolean dualityName(){return false;}
		// AENetworkProxy gridProxy;
		// ForgeDirection side=ForgeDirection.UNKNOWN;
		// DimensionalCoord pos=new DimensionalCoord(0, 0, 0, 0);
		default ItemStack getVisual() {
			return new ItemStack(ItemAndBlockHolder.FLUID_INTERFACE, 1);

		}

		default public boolean requireChannel() {
			return true;
		}

		default public AENetworkProxy getProxy() {

			if (getGridProxy() == null) {
				setGridProxy(new AENetworkProxy(this, "proxy", getVisual(), true));
				if (requireChannel())
					getGridProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
				getGridProxy().setValidSides(EnumSet.of(getSide()));

			}

			// gridProxy.setOwner();

			return this.getGridProxy();
		}

		public AENetworkProxy getGridProxy();

		public void setGridProxy(AENetworkProxy gridProxy);

		public ForgeDirection getSide();

		public void setSide(ForgeDirection side);

		public DimensionalCoord getPos();

		public void setPos(DimensionalCoord pos);

		@Override
		default public ISerializableObject copy() {

			Data o = newInst();
			o.setSide(getSide());
			o.setPos(getPos().copy());

			return o;
		}

		default public Data newInst() {

			try {
				return this.getClass().newInstance();
			} catch (Exception e) {

				e.printStackTrace();
			}
			return null;

		};

		@Override
		default public void writeToByteBuf(ByteBuf aBuf) {

			aBuf.writeInt(getSide().ordinal());
			DimensionalCoord loc = getLocation();
			aBuf.writeInt(loc.x);
			aBuf.writeInt(loc.y);
			aBuf.writeInt(loc.z);
			aBuf.writeInt(loc.getDimension());

		}

		public boolean firstUpdate();

		default World getW(int dim) {

			return getter.get(dim);
		}

		static Getter getter = new ClientGetter();

		public interface Getter {
			public default World get(int dim) {
				return DimensionManager.getWorld(dim);
			};
		}

		public class ClientGetter implements Getter {
			@SideOnly(Side.CLIENT)
			public World get(int dim) {
				if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
					return Getter.super.get(dim);
				}
				return Minecraft.getMinecraft().theWorld;
			};
		}

		@Override
		default public NBTBase saveDataToNBT() {
			NBTTagCompound tag = new NBTTagCompound();

			Optional.ofNullable(getTag()).ifPresent(s -> tag.setTag("itemtag", s));
			NBTTagCompound ae = new NBTTagCompound();
			getProxy().writeToNBT(ae);
			tag.setTag("ae", ae);
			getPos().writeToNBT(tag);

			tag.setInteger("side", getSide().ordinal());
			return tag;
		}

		@Override
		default public void loadDataFromNBT(NBTBase aNBT) {
			//StackTraceUtil.getCallerMethod(1);
			
			NBTTagCompound tag = (NBTTagCompound) aNBT;

			setTag(tag.getCompoundTag("itemtag"));

			setPos(DimensionalCoord.readFromNBT(tag));
			setPos(new DimensionalCoord(getW(getPos().getDimension()), getPos().x, getPos().y, getPos().z));
			setSide(ForgeDirection.getOrientation(tag.getInteger("side")));
			getProxy().readFromNBT(tag.getCompoundTag("ae"));
		}

		@Override
		default public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
			setSide(ForgeDirection.getOrientation(aBuf.readInt()));
			int x = aBuf.readInt();
			int y = aBuf.readInt();
			int z = aBuf.readInt();
			int dim = aBuf.readInt();
			this.setPos(new DimensionalCoord(getW(dim), x, y, z));
			if (fakeTile() != null) {
				fakeTile().xCoord = x;
				fakeTile().yCoord = y;
				fakeTile().zCoord = z;
				fakeTile().setWorldObj(getPos().getWorld());
			}
			return this;
		}

		public TileEntity fakeTile();

		@Override
		default public IGridNode getGridNode(ForgeDirection dir) {

			return getProxy().getNode();
		}

		@Override
		default public AECableType getCableConnectionType(ForgeDirection dir) {

			return AECableType.SMART;
		}

		@Override
		default public void securityBreak() {

		}

		@Override
		default public DimensionalCoord getLocation() {

			return getPos();
		}

		@Override
		default public void gridChanged() {

		}

		default public void destroy() {
			MyMod.LOG.info("Node destroy@" + getPos());

			try {if(this.getProxy().getNode()==null)return;
				IReadOnlyCollection<IGridConnection> col = this.getProxy().getNode().getConnections();
				Collection<IGridConnection> a = new ArrayList<>(col.size());// make
																			// a
																			// copy
																			// or
																			// get
																			// ConcurrentModException
				col.forEach(a::add);
				a.forEach(ax -> ax.destroy());

				this.getProxy().invalidate();

				this.getProxy().getGrid().getCache(ITickManager.class).removeNode(this.getProxy().getNode(), this);
			} catch (Exception e) {

				// e.printStackTrace();
			}
		}

		default boolean supportFluid() {
			return false;
		}
		static  DimensionalCoord unset_val =new DimensionalCoord(0,0,0,-1000);
		/**
		 * @param side
		 * @param aTileEntity
		 * @param onPlace
		 * @return
		 */
		@SuppressWarnings("unchecked")
		default public boolean accept(ForgeDirection side, ICoverable aTileEntity,boolean onPlace) {
			if(getPos().equals(unset_val)){
			//newly placed
			}else if(getPos().equals(new DimensionalCoord((TileEntity)aTileEntity))
					){
			//normal chunk save+load
			}else{
				
			//cover on machine item place, drop it 'cause its data is broken
			UUID own = ((IGregTechTileEntity) aTileEntity).getOwnerUuid();
					if(aTileEntity instanceof IGregTechTileEntity){
						MinecraftServer.getServer().getConfigurationManager().playerEntityList
						.stream().filter(s->
								((EntityPlayer)s).getUniqueID().equals(own)
								).findFirst()
						.ifPresent(s->{
							
							((EntityPlayer)s).addChatComponentMessage(new ChatComponentTranslation("programmable_hatches.cover.me.drop"));
						});
						;
						
						
					}
					
				
				aTileEntity.dropCover(side, side, true);
				return false;
				
			}
			setPos(new DimensionalCoord((TileEntity) aTileEntity));
			setSide(side);
			Optional.ofNullable(aTileEntity.getCoverItemAtSide(side)).filter(s -> s.hasDisplayName())
					.ifPresent(s -> setCustomName(s.getDisplayName()));
			;
			return true;
		}

		public default void setCustomName(String s) {
		}

		public default void onReady() {
		}

		public default boolean shiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable,
				ICoverable aTileEntity, EntityPlayer aPlayer) {
			return false;
		}

		public default boolean nonShiftClick(ForgeDirection side, int aCoverID, Data aCoverVariable,
				ICoverable aTileEntity, EntityPlayer aPlayer) {
			return false;
		}

		public default void update(ICoverable aTileEntity) {
		}

		public default void addUIWidgets(Builder builder, GT_CoverUIBuildContext gt_CoverUIBuildContext) {
		}

		default boolean hasAEGUI() {
			return true;
		}

		void setTag(NBTTagCompound tagCompound);

		NBTTagCompound getTag();
	}

	@Override
	public Data createDataObject(int aLegacyData) {

		throw new UnsupportedOperationException("no legacy");
	}

	@Override
	protected ItemStack getDisplayStackImpl(int aCoverID, Data aCoverVariable) {
		ItemStack is = super.getDisplayStackImpl(aCoverID, aCoverVariable);
		
		
		
		
		if(aCoverVariable.dualityName()){
			String s=aCoverVariable.name();
			if(s==null||s.equals("")){
				is.stackTagCompound=null;
			}else{
				is.setStackDisplayName(s);
			}
		
		}
		else{
			String s=aCoverVariable.tagName();
			if(s!=null&&!s.equals("")){
				is.setStackDisplayName(s);
			}
		}
		
		
		
		return is;
	}

	//private static Throwable t = new Throwable();

	@Override
	public Data createDataObject() {

		 if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
		
			 
			 if(mixinReady){
				 if(getWailaBody){	return new DummyData();}
			 }else{
			String s=StackTraceUtil.getCallerMethod(6);
			if(s.hashCode()==h0&&s.equals("gregtech.crossmod.waila.GregtechWailaDataProvider")){
			
			// do not actually load cover data on client side
			// or there'll be some performance issue
			// this happens when waila trying to get cover info
			return new DummyData();
			}
			
			

		}}
		;

		try {
			return (Data) clazz.newInstance();
		} catch (Exception e) {

			throw new RuntimeException(e);
		}
	}

	public void chunkUnload(Data t) {
		t.getProxy().onChunkUnload();

	}

	@Override
	public void placeCover(ForgeDirection side, ItemStack aCover, ICoverable aTileEntity) {
		super.placeCover(side, aCover, aTileEntity);

		Data data = ((Data) aTileEntity.getComplexCoverDataAtSide(side));
		data.setTag(aCover.getTagCompound());
		data.accept(side, aTileEntity,false);
		

	}
@Override
public void onPlayerAttach(EntityPlayer player, ItemStack aCover, ICoverable aTileEntity, ForgeDirection side) {
	
	Data data = (Data) aTileEntity.getComplexCoverDataAtSide(side);
	data.getProxy().setOwner(player);
}
	protected boolean onCoverRightClickImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity, EntityPlayer aPlayer, float aX, float aY, float aZ) {
		if (aCoverVariable.nonShiftClick(side, aCoverID, aCoverVariable, aTileEntity, aPlayer)) {
			return true;
		}
		openGUI( side,  aCoverID,  aCoverVariable,aTileEntity,  aPlayer);
		return false;
	};
@Override
protected Data onCoverScrewdriverClickImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
		ICoverable aTileEntity, EntityPlayer aPlayer, float aX, float aY, float aZ) {
	if (aCoverVariable.hasModularGUI())GT_UIInfos.openCoverUI(aTileEntity, aPlayer, side);
	return super.onCoverScrewdriverClickImpl(side, aCoverID, aCoverVariable, aTileEntity, aPlayer, aX, aY, aZ);
}
	
	private boolean openGUI(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity, EntityPlayer aPlayer) {
		
		
		/*
		if (aCoverVariable.hasModularGUI()&&Optional.ofNullable(aPlayer.getHeldItem()).map(ItemStack::getItem)
				.orElse(null)==MyMod.eu_tool
				) {
			GT_UIInfos.openCoverUI(aTileEntity, aPlayer, side);
			
			return true;
			
		}*/
		
		if (aCoverVariable.hasAEGUI() && !aPlayer.worldObj.isRemote) {
			aPlayer.openGui(MyMod.instance, side.ordinal(), aPlayer.getEntityWorld(), aTileEntity.getXCoord(),
						aTileEntity.getYCoord(), aTileEntity.getZCoord());
			return true;
			}	
		return false;
	}
  
	
	@Override
	protected boolean onCoverShiftRightClickImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity, EntityPlayer aPlayer) {
		if (aCoverVariable.shiftClick(side, aCoverID, aCoverVariable, aTileEntity, aPlayer)) {
			return true;
		}
		;

		openGUI( side,  aCoverID,  aCoverVariable,aTileEntity,  aPlayer);
			
		/*if (aCoverVariable.hasAEGUI() && !aPlayer.worldObj.isRemote) {
		aPlayer.openGui(MyMod.instance, side.ordinal(), aPlayer.getEntityWorld(), aTileEntity.getXCoord(),
					aTileEntity.getYCoord(), aTileEntity.getZCoord());
		}	*/	
		
		
		/*NW.sendPacketToAllPlayersInRange(aPlayer.getEntityWorld(),
					new GT_Packet_SendCoverData(side, aCoverID, aCoverVariable, aTileEntity), aTileEntity.getXCoord(),
					aTileEntity.getZCoord());
*/
		
		return true;
	}

	@Override
	public boolean allowsTickRateAddition() {

		return false;
	}

	@Override
	protected int getTickRateImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {

		return 1;
	}

	@Override
	protected boolean onCoverRemovalImpl(ForgeDirection side, int aCoverID, Data data, ICoverable aTileEntity,
			boolean aForced) {
		data.destroy();
		return true;
	}

	/*
	 * @Override protected void onBaseTEDestroyedImpl(ForgeDirection side, int
	 * aCoverID, Data data, ICoverable aTileEntity) {
	 * data.getProxy().getNode().getConnections().forEach(s->s.destroy());
	 * data.getProxy().invalidate(); try {
	 * data.getProxy().getGrid().getCache(ITickManager.class).removeNode(data.
	 * getProxy().getNode(), data); } catch (GridAccessException e) {
	 * 
	 * e.printStackTrace(); } }
	 */
	static Method m;
	
	static {
		try {
			m = GridNode.class.getDeclaredMethod("isValidDirection", ForgeDirection.class);
			m.setAccessible(true);
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

	static public boolean canConnect(Object obj, ForgeDirection dir) {

		if (obj instanceof GridNode) {
			GridNode node = (GridNode) obj;
			try {
				return (Boolean) m.invoke(node, dir);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		return false;
		// private boolean canConnect(final GridNode from, final ForgeDirection
		// dir) {
	}

	@Override
	protected Data doCoverThingsImpl(ForgeDirection side, byte aInputRedstone, int aCoverID, Data data,
			ICoverable aTileEntity, long aTimer) {
		if (data.firstUpdate())
			if(!data.accept(side, aTileEntity,false)){return data;};
		
		if (!data.getProxy().isReady()) {
			data.getProxy().onReady();
			data.onReady();
		}
		data.update(aTileEntity);
		//
		AENetworkProxy host=null;
		if (aTileEntity instanceof IGridProxyable &&( host=((IGridProxyable) aTileEntity).getProxy()) != null) {
			IGridNode thiz = data.getProxy().getNode();
			boolean found=false;
			try {
			Iterator<IGridConnection> it = thiz.getConnections().iterator();
			while (it.hasNext()) {
				IGridConnection item = it.next();
				if (item.a() == host.getNode() || item.b() == host.getNode()) {
					
						found = true;
					break;
				}
				;

			}
				if(!found){
					MyMod.LOG.info("Node internal connect@" + data.getPos());
					new GridConnection(thiz, host.getNode(), ForgeDirection.UNKNOWN);}
			} catch (FailedConnection e) {
				
				e.printStackTrace();
			}
		}
		//
		
		
		TileEntity te = aTileEntity.getTileEntityAtSide(side);
		lab: if (te != null && te instanceof IGridHost) {
			Iterator<IGridConnection> it = data.getProxy().getNode().getConnections().iterator();
			IGridConnection item = null;
			IGridNode thenode = ((IGridHost) te).getGridNode(side.getOpposite());
			if (thenode == null) {
				break lab;
			} // this is possible if there're only parts no cable
			boolean found = false;

			boolean thisSideValid = canConnect(thenode, side.getOpposite());

			while (it.hasNext()) {
				item = it.next();
				if (item.a() == thenode || item.b() == thenode) {
					if (thisSideValid == false) {
						item.destroy();
					} else
						found = true;
					break;
				}
				;

			}

			if (found == false && thisSideValid) {
				try {
					IGridNode thiz = data.getProxy().getNode();
					new GridConnection(thiz, thenode, side);

					MyMod.LOG.info("Node connect@" + data.getPos());
				} catch (FailedConnection e) {

					//System.out.println(item.a());
					//System.out.println(item.b());
					//System.out.println(thenode);
					e.printStackTrace();
				}

			}
			;

		}

		return super.doCoverThingsImpl(side, aInputRedstone, aCoverID, data, aTileEntity, aTimer);
	}

	@Override
	protected boolean letsEnergyInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsEnergyOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsFluidInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsFluidOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, Fluid aFluid,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsItemsInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsItemsOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable, int aSlot,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsRedstoneGoInImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	protected boolean letsRedstoneGoOutImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
			ICoverable aTileEntity) {
		return true;
	}

	@Override
	public boolean hasCoverGUI() {

		return true;
	}

	@Override
	public boolean useModularUI() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ModularWindow createWindow(GT_CoverUIBuildContext buildContext) {
		return new AECoverUIFactory(buildContext,
				((Data) buildContext.getTile().getComplexCoverDataAtSide(buildContext.getCoverSide()))).createWindow();
	}

	private class AECoverUIFactory extends UIFactory {

		public AECoverUIFactory(GT_CoverUIBuildContext buildContext, Data d) {
			super(buildContext);
			this.data = d;
		}

		Data data;

		@Override
		public ModularWindow createWindow() {
			// TODO Auto-generated method stub
			return super.createWindow();
		}

		@Override
		protected void addUIWidgets(Builder builder) {

			data.addUIWidgets(builder, getUIBuildContext());
		}
	}

	@Override
	public boolean allowsCopyPasteTool() {

		return false;// no!
	}

	@Override
	public boolean isCoverPlaceable(ForgeDirection side, ItemStack aStack, ICoverable aTileEntity) {
		if (!Config.MECover&&aTileEntity instanceof IGridProxyable && ((IGridProxyable) aTileEntity).getProxy() != null) {
			return false;
		} // cannot be placed on ME hatches
		
		
		return super.isCoverPlaceable(side, aStack, aTileEntity);
	}
	

}
