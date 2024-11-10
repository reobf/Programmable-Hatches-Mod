package reobf.proghatches.eucrafting;

import static gregtech.api.enums.GT_Values.NW;

import java.util.ArrayList;
import java.util.EnumSet;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityProvider;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.api.util.IConfigManager;
import appeng.api.util.IConfigurableObject;
import appeng.api.util.IInterfaceViewable;
import appeng.helpers.DualityInterface;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.IPriorityHost;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.util.Platform;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.net.GT_Packet_SendCoverData;
import gregtech.api.util.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.AECover.Data;
import reobf.proghatches.main.FakeHost;
import reobf.proghatches.main.MyMod;

public class InterfaceData implements Data, IInterfaceHost, IGridTickable, IUpgradeableHost, ICustomNameObject,
		IConfigurableObject, IPriorityHost,
		IActualSideProvider
		{
	
	public IInterfaceHost getInterfaceOrNull(){return this;};
	public void setTag(NBTTagCompound tagCompound) {
	
		tag = tagCompound;
	}

	public NBTTagCompound getTag() {
		return tag;
	}

	NBTTagCompound tag;

	public InterfaceData() {
	}

	// hostName.contains("TileFluidInterface")->TRUE
	public static class FluidInterfaceData_TileFluidInterface extends InterfaceData {
		public FluidInterfaceData_TileFluidInterface() {
		}

		public boolean supportFluid() {
			return true;
		}
	}

	public boolean supportFluid() {
		return false;
	}

	AENetworkProxy gridProxy;
	ForgeDirection side = ForgeDirection.UNKNOWN;
	DimensionalCoord pos = new DimensionalCoord(0, 0, 0, -1000);

	public AENetworkProxy getGridProxy() {
		return gridProxy;
	}

	public void setGridProxy(AENetworkProxy gridProxy) {
		this.gridProxy = gridProxy;
	}

	public ForgeDirection getSide() {
		return side;
	}

	public void setSide(ForgeDirection side) {
		this.side = side;
	}

	public DimensionalCoord getPos() {
		return pos;
	}

	public void setPos(DimensionalCoord pos) {
		this.pos = pos;
	}

	public void onReady() {
		duality.initialize();
	};

	private TileEntity faketile = new TileEntity();
	private final DI duality = new DI(this.getProxy(), this);;


	public static class Disabled0 extends AppEngInternalAEInventory implements DisabledInventory {
		public Disabled0(IAEAppEngInventory te, int s) {
			super(te, s);
		}

		public void setInventorySlotContents(int slot, ItemStack newItemStack) {
		};

		public boolean isItemValidForSlot(int i, ItemStack itemstack) {
			return false;
		};
	}

	public static class Disabled1 extends AppEngInternalInventory implements DisabledInventory {
		public Disabled1(IAEAppEngInventory te, int s) {
			super(te, s);
		}

		public void setInventorySlotContents(int slot, ItemStack newItemStack) {
		};

		public boolean isItemValidForSlot(int i, ItemStack itemstack) {
			return false;
		};
	}

	public interface DisabledInventory {
	}

	public class DI extends DualityInterface implements IActualSideProvider {

		private final AppEngInternalAEInventory config = new Disabled0(this, NUMBER_OF_CONFIG_SLOTS);
		private final AppEngInternalInventory storage = new Disabled1(this, NUMBER_OF_CONFIG_SLOTS);

		public IInventory getStorage() {
			return config;
		};

		public AppEngInternalAEInventory getConfig() {
			return config;
		}

		@Override
		public IInventory getInventoryByName(String name) {
			if (name.equals("storage")) {
				return this.storage;
			}
			if (name.equals("config")) {
				return this.config;
			}
			return super.getInventoryByName(name);
		}

		public DI(AENetworkProxy networkProxy, IInterfaceHost ih) {
			super(networkProxy, ih);

		}

		public ForgeDirection getActualSide() {
			return side;
		}

		public IUpgradeableHost getHost() {
			return new FakeHost(getTileEntity(), InterfaceData.this);
		};

		public void markDirty() {
			/*
			 * if(pos.getWorld().getChunkProvider() .chunkExists(pos.x >> 4,
			 * pos.z >> 4))
			 * 
			 */
			super.markDirty();

		};

	};

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkChannelsChanged c) {
		this.duality.notifyNeighbors();
	}

	@Override
	public int getInstalledUpgrades(final Upgrades u) {
		return this.duality.getInstalledUpgrades(u);
	}

	@MENetworkEventSubscribe
	public void stateChange(final MENetworkPowerStatusChange c) {
		this.duality.notifyNeighbors();
	}

	@Override
	public void provideCrafting(ICraftingProviderHelper craftingTracker) {
		this.duality.provideCrafting(craftingTracker);

	}

	@Override
	public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
		// TODO Auto-generated method stub
		return this.duality.pushPattern(patternDetails, table);
	}

	@Override
	public boolean isBusy() {
		
		return this.duality.isBusy();
	}

	@Override
	public void gridChanged() {
		this.duality.gridChanged();
	}

	@Override
	public TileEntity getTile() {
		// TODO Auto-generated method stub
		return getTileEntity();
	}

	@Override
	public IConfigManager getConfigManager() {
		// TODO Auto-generated method stub
		return this.duality.getConfigManager();
	}

	@Override
	public IInventory getInventoryByName(String name) {
		// TODO Auto-generated method stub
		return this.duality.getInventoryByName(name);
	}

	@Override
	public ImmutableSet<ICraftingLink> getRequestedJobs() {
		// TODO Auto-generated method stub
		return this.duality.getRequestedJobs();
	}

	@Override
	public IAEItemStack injectCraftedItems(ICraftingLink link, IAEItemStack items, Actionable mode) {
		// TODO Auto-generated method stub
		return this.duality.injectCraftedItems(link, items, mode);
	}

	@Override
	public void jobStateChange(ICraftingLink link) {
		this.duality.jobStateChange(link);

	}

	@Override
	public IGridNode getActionableNode() {

		return this.getProxy().getNode();
	}

	@Override
	public IGridNode getGridNode(ForgeDirection dir) {

		return this.getProxy().getNode();
	}

	@Override
	public void securityBreak() {

	}

	@Override
	public DimensionalCoord getLocation() {

		return this.pos;
	}

	@Override
	public IInventory getPatterns() {

		return this.duality.getPatterns();
	}

	@Override
	public String getName() {

		return getCustomName();
	}

	@Override
	public boolean shouldDisplay() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public DualityInterface getInterfaceDuality() {
		// TODO Auto-generated method stub
		return this.duality;
	}

	@Override
	public EnumSet<ForgeDirection> getTargets() {
		// TODO Auto-generated method stub
		return EnumSet.of(ForgeDirection.UNKNOWN);
	}

	@Override
	public TileEntity getTileEntity() {

		if (faketile.getWorldObj() == null) {

			faketile.setWorldObj(pos.getWorld());
			faketile.xCoord = pos.x;
			faketile.yCoord = pos.y;
			faketile.zCoord = pos.z;
		}
		;

		return faketile;
	}

	@Override
	public void saveChanges() {
		duality.saveChanges();

	}

	@Override
	public NBTBase saveDataToNBT() {
		if(isWailaCall())return new NBTTagCompound();
		NBTBase t = Data.super.saveDataToNBT();
		((NBTTagCompound) t).setInteger("p", p);
		// ((NBTTagCompound) t).setString("name",name);
		duality.writeToNBT((NBTTagCompound) t);

		return t;
	}

	@Override
	public void loadDataFromNBT(NBTBase aNBT) {
		Data.super.loadDataFromNBT(aNBT);
		// System.out.println(pos.getWorld());
		p = ((NBTTagCompound) aNBT).getInteger("p");
		// name=((NBTTagCompound) aNBT).getString("name");
		faketile.xCoord = pos.x;
		faketile.yCoord = pos.y;
		faketile.zCoord = pos.z;
		faketile.setWorldObj(pos.getWorld());
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			duality.readFromNBT((NBTTagCompound) aNBT);
		}
		;

	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		
		return duality.getTickingRequest(node);
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
		if (first)
			return TickRateModulation.SAME;

		return duality.tickingRequest(node, TicksSinceLastCall);
	}
	
	
	
	
	
	

	public String getCustomName() {
		if (name != null&&name.length()>0){
			//System.out.println(name.length());
			
			return name;
			}
		return nameOverride();
	}
	private String nameOverride(){
		return supportFluid() ? "Dual ME Interface" : "ME Interface";
	}
	public boolean hasCustomName() {
		  return this.name != null && this.name.length() > 0;
	}

	private String name;

	public void setCustomName(String name) {
		this.name = name;
		if(name==null||name.isEmpty()){
			NBTTagCompound tg = this.getTag();
			if(tg!=null){
			tg.removeTag("display");
			this.setTag(tg);
			}
			return;
		}
		ItemStack is=new ItemStack(Items.apple);
		is.setTagCompound(this.getTag());
		is.setStackDisplayName(name);
		this.setTag(is.getTagCompound());
	}

	public int getPriority() {
		return p;
	};

	int p;

	public void setPriority(int newValue) {
		p = newValue;
	};

	@Override
	public void destroy() {
		final ArrayList<ItemStack> drops = new ArrayList<>();

		for (String s : new String[] { "patterns", "upgrades" }) {
			IInventory inv = duality.getInventoryByName(s);
			for (int l = 0; l < inv.getSizeInventory(); l++) {
				final ItemStack is = inv.getStackInSlot(l);
				inv.setInventorySlotContents(l, null);
				if (is != null) {
					drops.add(is);
				}
			}
		}

		Platform.spawnDrops(pos.getWorld(), pos.x, pos.y, pos.z, drops);

		Data.super.destroy();
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

	@Override
	public TileEntity fakeTile() {
		return faketile;
	}
@Override
public void addUIWidgets(Builder builder, GT_CoverUIBuildContext gt_CoverUIBuildContext) {
	if (hasAEGUI() && !gt_CoverUIBuildContext.getPlayer().getEntityWorld().isRemote) {
		gt_CoverUIBuildContext.getPlayer()
	.openGui(MyMod.instance, side.ordinal(), 	
			gt_CoverUIBuildContext.getPlayer().getEntityWorld(), this.getPos().x,
			 this.getPos().y, this.getPos().z);}
}


@Override
public ForgeDirection getActualSide() {

	return duality.getActualSide();
}



}
