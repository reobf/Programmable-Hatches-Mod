package reobf.proghatches.oc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import com.gtnewhorizon.structurelib.util.XSTR;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.common.covers.redstone.GT_Cover_AdvancedRedstoneReceiverBase.GateMode;
import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.util.ProghatchesUtil;

public class ItemGTRedstoneCard extends Item implements li.cil.oc.api.driver.item.HostAware {

	public ItemGTRedstoneCard() {

	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)

	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

		p_77624_3_.add("UUID:" + (getUUID(p_77624_1_) != null ? getUUID(p_77624_1_).toString() : "Not bound"));

		int i = 0;
		while (true) {
			String k = "item.proghatches.oc.redstone.tooltip";
			if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
				break;
			}
			String key = k + "." + i;
			String trans = LangManager.translateToLocal(key);

			p_77624_3_.add(trans);
			i++;
		}

		// p_77624_3_.add(LangManager.translateToLocal("item.proghatches.oc.redstone.tooltip"));

		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}

	@Override
	public boolean worksWith(ItemStack stack) {

		return stack.getItem() instanceof ItemGTRedstoneCard;
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {

		return new RedstoneEnv(stack);
	}

	public class RedstoneEnv implements ManagedEnvironment {

		// public RedstoneEnv(EnvironmentHost
		// env){this.env=env;};EnvironmentHost env;
		private Node _node = Network.newNode(this, Visibility.Network).withComponent("gtredstone")

				.create();

		public RedstoneEnv(ItemStack stack) {
			this.stack = stack;
		}

		ItemStack stack;

		@Override
		public Node node() {
			return _node;

		}

		@Callback(doc = "getWireless(frequency:number[,isPublic:boolean]) --get GT wireless redstone, 'isPublic' defaults to true", direct = false)
		public Object[] getWireless(final Context context, final Arguments args) {
			boolean ispublic = args.isBoolean(1) ? args.checkBoolean(1) : true;
			UUID uid = getUUID(stack);
			if (uid == null && ispublic == false)
				throw new IllegalStateException("Using private freq without bound UUID.");

			return new Object[] {
					ProghatchesUtil.getSignalAt(ispublic ? null : uid, args.checkInteger(0), getMode(stack)) };
		}

		@Callback(doc = "setWireless(frequency:number,signal:number[,isPublic:boolean]) --set GT wireless redstone, 'isPublic' defaults to true", direct = false)
		public Object[] setWireless(final Context context, final Arguments args) {

			boolean ispublic = args.isBoolean(2) ? args.checkBoolean(2) : true;
			UUID uid = getUUID(stack);
			if (uid == null && ispublic == false)
				throw new IllegalStateException("Using private freq without bound UUID.");
			int b = args.checkInteger(1);
			if (b < 0 || b >= 16) {
				throw new IllegalArgumentException("Redstone signal out of range [0,15].");
			}
			ProghatchesUtil.setSignalAt(ispublic ? null : uid, args.checkInteger(0), signalSource(stack), (byte) b);
			return null;
		}

		@Callback(doc = "setGateMode(mode:string) --set GateMode, available modes: AND,NAND,OR,NOR,SINGLE_SOURCE", direct = false)
		public Object[] setGateMode(final Context context, final Arguments args) {

			// try{
			setMode(stack, GateMode.valueOf(args.checkString(0)));
			// }catch(Exception e){throw new RuntimeException("Failed, check
			// your args.");}
			return null;
		}

		@Callback(doc = "getGateMode() --get GateMode", direct = false)
		public Object[] getGateMode(final Context context, final Arguments args) {

			// try{

			// }catch(Exception e){throw new RuntimeException("Failed, check
			// your args.");}
			return new Object[] { getMode(stack).toString() };
		}

		@Callback(doc = "clearWireless(isPublic:boolean[,frequency:number]) --clear GT wireless redstone, will clear all signals emitted by this card if 'frequency' is absent.", direct = false)
		public Object[] clearWireless(final Context context, final Arguments args) {

			boolean ispublic = args.checkBoolean(0);
			UUID uid = getUUID(stack);
			if (uid == null && ispublic == false)
				throw new IllegalStateException("Using private freq without bound UUID.");

			if (args.isInteger(1)) {
				ProghatchesUtil.removeSignalAt(ispublic ? null : uid, args.checkInteger(1), signalSource(stack));
			} else {

				ProghatchesUtil.removeAllSignalAt(ispublic ? null : uid, signalSource(stack));
			}

			return null;
		}

		@Callback(doc = "getUUID() --get bound owner's UUID, or nil if absent", direct = false)
		public Object[] getOwnerUUID(final Context context, final Arguments args) {

			return new Object[] {
					Optional.ofNullable(ItemGTRedstoneCard.this.getUUID(stack)).map(Object::toString).orElse(null) };
		}

		@Override
		public void onConnect(Node node) {

		}

		@Override
		public void onDisconnect(Node node) {
			ProghatchesUtil.removeAllSignalAt(getUUID(stack), signalSource(stack));

		}

		@Override
		public void onMessage(Message message) {

		}

		@Override
		public void load(NBTTagCompound nbt) {
			Optional.ofNullable(nbt.getTag("node")).ifPresent(s -> {
				if (node() != null)
					node().load((NBTTagCompound) s);
			});

		}

		@Override
		public void save(NBTTagCompound nbt) {
			NBTTagCompound t = new NBTTagCompound();
			Optional.ofNullable(node()).ifPresent(s -> s.save(t));
			nbt.setTag("node", t);
		}

		@Override
		public boolean canUpdate() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void update() {

		}
	}

	@Override
	public String slot(ItemStack stack) {

		return li.cil.oc.common.Slot.Card();
	}

	@Override
	public int tier(ItemStack stack) {

		return 0;
	}

	@Override
	public NBTTagCompound dataTag(ItemStack stack) {

		return null;
	}

	public NBTTagCompound getOrCreateTag(ItemStack stack) {
		NBTTagCompound tag = stack.getTagCompound();
		if (tag == null) {
			stack.setTagCompound(new NBTTagCompound());
			return (getOrCreateTag(stack));
		}
		return tag;

	}

	public long signalSource(ItemStack stack) {

		Optional.of(getOrCreateTag(stack)).filter(s -> s.hasKey("signalSource") == false)
				.ifPresent(s -> s.setLong("signalSource", XSTR.XSTR_INSTANCE.nextLong()));

		return getOrCreateTag(stack).getLong("signalSource");

	}

	@Nullable
	public UUID getUUID(ItemStack stack) {

		long l = getOrCreateTag(stack).getLong("uuid_l");
		long m = getOrCreateTag(stack).getLong("uuid_m");
		if (l == m && m == 0)
			return null;
		return new UUID(m, l);

	}

	@Nullable
	public GateMode getMode(ItemStack stack) {

		int l = getOrCreateTag(stack).getInteger("mode");

		return GateMode.values()[l];

	}

	public void setMode(ItemStack stack, GateMode m) {

		getOrCreateTag(stack).setInteger("mode", m.ordinal());

	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World worldIn, EntityPlayer player) {
		if (worldIn.isRemote == false) {
			player.addChatMessage(new ChatComponentTranslation("item.proghatch.oc.redstone.bind"));

			UUID uid = player.getUniqueID();
			getOrCreateTag(stack).setLong("uuid_l", uid.getLeastSignificantBits());
			getOrCreateTag(stack).setLong("uuid_m", uid.getMostSignificantBits());
		}
		return super.onItemRightClick(stack, worldIn, player);
	}

	@Override
	public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {

		return stack.getItem() instanceof ItemGTRedstoneCard;
	}

}
