package reobf.proghatches.oc;

import static reobf.proghatches.oc.WirelessPeripheralManager.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.DoubleUnaryOperator;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.SoundResource;
import gregtech.api.util.GT_Utility;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.integration.util.Wrench;
import li.cil.oc.server.machine.Machine;
import li.cil.oc.util.BlockPosition;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.oc.ItemWirelessPeripheralCard.Env;
import scala.Some;

public class TileWirelessPeripheralStation extends TileEntity implements li.cil.oc.api.network.Environment {
	public TileWirelessPeripheralStation() {
	}

	static public IWailaDataProvider provider = new IWailaDataProvider() {
		//spotless:off
		@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
	        IWailaConfigHandler config) {
		currenttip.add(LangManager.translateToLocalFormatted("tile.proghatches.oc.peripheral_station.waila.channel", accessor.getNBTData().getString("UUID")));
		currenttip.add(LangManager.translateToLocalFormatted("tile.proghatches.oc.peripheral_station.waila.connection",
				LangManager.translateToLocal("tile.proghatches.oc.peripheral_station.waila.connection."+
				(accessor.getNBTData().getBoolean("connection")?"true":accessor.getNBTData().getBoolean("inrange")?
						accessor.getNBTData().getBoolean("oneComputer")?"false":"more_than_one":"out_of_range"))
				
				
				));
		return currenttip;
		
	} //spotless:on

		@Override
		public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x,
				int y, int z) {
			tag.setString("UUID", ((TileWirelessPeripheralStation) world.getTileEntity(x, y, z)).thisUUID.toString());
			tag.setBoolean("inrange", ((TileWirelessPeripheralStation) world.getTileEntity(x, y, z)).inrange);
			tag.setBoolean("oneComputer", ((TileWirelessPeripheralStation) world.getTileEntity(x, y, z)).oneComputer);

			Optional.ofNullable(WirelessPeripheralManager.cards
					.get(((TileWirelessPeripheralStation) world.getTileEntity(x, y, z)).thisUUID)).ifPresent(s -> {
						Optional.ofNullable(WirelessPeripheralManager.stations
								.get(((TileWirelessPeripheralStation) world.getTileEntity(x, y, z)).thisUUID))
								.ifPresent(w -> {

									tag.setBoolean("connection", s.network() == w.network());

								});
						;

					});

			;

			return tag;
		};

		@Override
		public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {

			return null;
		}

		@Override
		public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {

			return currenttip;
		}

		@Override
		public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
				IWailaConfigHandler config) {

			return currenttip;
		}
	};

	public static class ItemBlock extends net.minecraft.item.ItemBlock {
		@SideOnly(Side.CLIENT)
		@Override
		public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
			int i = 0;
			while (true) {
				String k = "tile.proghatches.peripheral_station.tooltip";
				if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
					break;
				}
				String key = k + "." + i;
				String trans = LangManager.translateToLocal(key);

				p_77624_3_.add(trans);
				i++;
			}

			super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
		}

		public ItemBlock(net.minecraft.block.Block p_i45328_1_) {
			super(p_i45328_1_);

		}

	}

	public static class Block extends net.minecraft.block.BlockContainer {
		@Override
		@SideOnly(value = Side.CLIENT)
		public void registerBlockIcons(IIconRegister reg) {
			super.registerBlockIcons(reg);
			a = reg.registerIcon("proghatches:pstation");
			b = reg.registerIcon("proghatches:pstation_0");
			c = reg.registerIcon("proghatches:pstation_1");
		}

		IIcon a, b, c;

		@Override
		@SideOnly(value = Side.CLIENT)
		public IIcon getIcon(int side, int meta) {
			if (side <= 1)
				return a;// top bottom
			if (meta == 0)
				return b;
			else
				return c;
		}

		@Override
		public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
				float subY, float subZ) {
			TileWirelessPeripheralStation tile = (TileWirelessPeripheralStation) worldIn.getTileEntity(x, y, z);
			if (Wrench.holdsApplicableWrench(player, new BlockPosition(x, y, z, Some.apply(worldIn)))) {
				if (!worldIn.isRemote && tile.oneComputer == false) {
					GT_Utility.sendSoundToPlayers(worldIn, SoundResource.RANDOM_ANVIL_BREAK, 1.0F, -1.0F, x, y, z);
					Wrench.wrenchUsed(player, new BlockPosition(x, y, z, Some.apply(worldIn)));
					tile.oneComputer = true;
				}
				return true;
			}
			ItemStack is;
			/*
			 * if (worldIn.isRemote) return false;
			 */
			if ((is = player.getHeldItem()) != null && is.getItem() instanceof ItemWirelessPeripheralCard) {

				if (is.getTagCompound() == null)
					is.setTagCompound(new NBTTagCompound());
				GT_Utility.doSoundAtClient(SoundResource.IC2_TOOLS_OD_SCANNER, 1, 1.0F, x + 0.5, y + 0.5, z + 0.5);
				
				is.getTagCompound().setString("remoteUUID", tile.thisUUID.toString());

			}

			return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
		}

		public Block() {
			super(Material.rock);
			setHardness(1);
			setHarvestLevel("pickaxe", 1);
			setBlockName("proghatch.peripheral_station");
		}

		@Override
		public TileEntity createNewTileEntity(World worldIn, int meta) {

			return new TileWirelessPeripheralStation();
		}

	}

	private Node node = li.cil.oc.api.Network.newNode(this, Visibility.Network).withConnector().create();

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound t = new NBTTagCompound();
		Optional.ofNullable(node()).ifPresent(s -> s.save(t));
		nbt.setTag("node", t);
		nbt.setString("UUID", thisUUID.toString());
		nbt.setBoolean("inrange", inrange);
		nbt.setBoolean("oneComputer", oneComputer);
		super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		Optional.ofNullable(nbt.getTag("node")).ifPresent(s -> {
			if (node() != null)
				node().load((NBTTagCompound) s);
		});
		thisUUID = UUID.fromString(nbt.getString("UUID"));
		inrange = nbt.getBoolean("inrange");
		oneComputer = nbt.getBoolean("oneComputer");
		super.readFromNBT(nbt);
	}

	@Override
	public Node node() {
		return node;
	}

	// TODO:use oc UUID?
	UUID thisUUID = UUID.randomUUID();

	@Override
	public void onConnect(Node node) {
		// TODO:check if node belongs to card
		WirelessPeripheralManager.add(stations, thisUUID, this.node);
	}

	@Override
	public void onDisconnect(Node node) {
		// card will call onDisconnect to cut the connection
		// do nothing here
	}

	@Override
	public void onMessage(Message message) {

	}

	@Override
	public void invalidate() {
		if (node != null) {
			WirelessPeripheralManager.remove(stations, thisUUID);
		}
	}

	@Override
	public void validate() {
		if (node != null) {
			WirelessPeripheralManager.add(stations, thisUUID, node);
		}
	}

	@Override
	public void onChunkUnload() {
		if (node != null) {
			WirelessPeripheralManager.remove(stations, thisUUID);
		}
	}

	private boolean init;
	boolean inrange = true;
	boolean oneComputer = true;
	int range = 16;
	private static Class<?> computer;
	static {
		try {
			computer = Class.forName("li.cil.oc.common.tileentity.traits.Computer");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void updateEntity() {
		this.markDirty();
		if (!init) {
			init = true;
			li.cil.oc.api.Network.joinOrCreateNetwork(this);
			if (node != null) {

				WirelessPeripheralManager.add(stations, thisUUID, node);

			}
		}

		if (this.worldObj.isRemote) {
			thisUUID = new UUID(0, 0);// set to 0 on client to prevent Waila
										// from showing random UUID

		} else {

			if (oneComputer ^ this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord) == 0)
				this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord,
						1 - this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord), 2);

			if (!oneComputer) {
				return;
			}
			if (node == null)
				return;

			int[] counter = new int[1];
			node.network().nodes().forEach(s -> {
				if (s.host() instanceof li.cil.oc.server.machine.Machine) {
					li.cil.oc.server.machine.Machine mch = (Machine) s.host();
					// if(mch.host() instanceof Computer)//won't compile...
					// projectred interfaces missing

					if (computer.isInstance(mch.host())) // workaround

						counter[0]++;
				}
			});
			oneComputer = (counter[0] <= 1);

			Node remoteID = WirelessPeripheralManager.cards.get(thisUUID);
			if (remoteID == null) {
				inrange = true;
			}
			if (remoteID != null) {
				ItemWirelessPeripheralCard.Env me = (Env) remoteID.host();
				DoubleUnaryOperator op = s -> s * s;
				double x = me.host.xPosition(), y = me.host.yPosition(), z = me.host.zPosition();
				double x1 = this.xCoord + 0.5, y1 = this.yCoord + 0.5, z1 = this.zCoord + 0.5;
				double epsilon = 0.01;
				inrange = range * range + epsilon >= op.applyAsDouble(x - x1) + op.applyAsDouble(y - y1)
						+ op.applyAsDouble(z - z1) && me.host.world() == this.worldObj;
				boolean sameNet = node.network() == remoteID.network();

				if ((inrange && oneComputer) && !sameNet) {
					this.node.connect(remoteID);
				}
				if (!(inrange && oneComputer) && sameNet) {
					this.node.disconnect(remoteID);
				}

			}

		}

	}

}
