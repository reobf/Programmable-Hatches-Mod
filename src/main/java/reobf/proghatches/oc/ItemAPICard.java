package reobf.proghatches.oc;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import reobf.proghatches.lang.LangManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.oc.api.Network;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;

public class ItemAPICard extends Item implements li.cil.oc.api.driver.item.HostAware {

	public ItemAPICard() {

	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)

	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

		int i = 0;
		while (true) {
			String k = "item.proghatches.oc.api.tooltip";
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

	@Override
	public boolean worksWith(ItemStack stack) {

		return stack.getItem() instanceof ItemAPICard;
	}

	@Override
	public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {

		return new APIEnv(stack);
	}

	public class APIEnv implements ManagedEnvironment {

		// public RedstoneEnv(EnvironmentHost
		// env){this.env=env;};EnvironmentHost env;
		private Node _node = Network.newNode(this, Visibility.Network).withComponent("itemapi")

				.create();

		public APIEnv(ItemStack stack) {
			this.stack = stack;
		}

		ItemStack stack;

		@Override
		public Node node() {
			return _node;

		}

		@Callback(doc = "getID(tag:string):table --parse binary deflated NBT tag as readable table", direct = false)
		public Object[] getTag(final Context context, final Arguments args) {

			try {

				return new Object[] { CompressedStreamTools.readCompressed(new ByteArrayInputStream(
						// args.checkString(0)).getBytes() will truncate byte to
						// 0~127
						// have no idea why that happens
						(byte[]) args.checkAny(0))) };
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("format error");
			}

			// return new Object[] { };

		}

		@Callback(doc = "getID(database:address,index:number):string --get item ID in number", direct = false)
		public Object[] getID(final Context context, final Arguments args) {
			Node n = node().network().node(args.checkString(0));
			if (n == null)
				throw new IllegalArgumentException("no such component");
			if (!(n instanceof li.cil.oc.server.network.Component))
				throw new IllegalArgumentException("no such component");
			li.cil.oc.api.network.Environment env = n.host();
			if (!(env instanceof Database))
				throw new IllegalArgumentException("not a database");
			Database database = (Database) env;
			ItemStack s = database.getStackInSlot(args.checkInteger(1));
			if (s == null)
				throw new IllegalArgumentException("no item in such index");

			return new Object[] { Item.getIdFromItem(s.getItem()) };

			// database.GET
		}

		@Callback(doc = "getOreDict(database:address,index:number):string --get OreDicts", direct = false)
		public Object[] getOreDict(final Context context, final Arguments args) {
			Node n = node().network().node(args.checkString(0));
			if (n == null)
				throw new IllegalArgumentException("no such component");
			if (!(n instanceof li.cil.oc.server.network.Component))
				throw new IllegalArgumentException("no such component");
			li.cil.oc.api.network.Environment env = n.host();
			if (!(env instanceof Database))
				throw new IllegalArgumentException("not a database");
			Database database = (Database) env;
			ItemStack s = database.getStackInSlot(args.checkInteger(1));
			if (s == null)
				throw new IllegalArgumentException("no item in such index");

			int[] ids = OreDictionary.getOreIDs(s);
			boolean found = false;

			return Arrays.stream(ids).mapToObj(OreDictionary::getOreName).filter(ss -> !ss.equals("Unknown")).toArray()

			;

			// return new Object[]{ OreDictionary.getOreID(s)};

			// database.GET
		}

		@Callback(doc = "match(database:address,index:number,regex:string[,mode:string]):boolean --check with regex to see if target has OreDict, 'mode' can be 'find'(default) or 'match'", direct = false)
		public Object[] match(final Context context, final Arguments args) {
			Node n = node().network().node(args.checkString(0));
			if (n == null)
				throw new IllegalArgumentException("no such component");
			if (!(n instanceof li.cil.oc.server.network.Component))
				throw new IllegalArgumentException("no such component");
			li.cil.oc.api.network.Environment env = n.host();
			if (!(env instanceof Database))
				throw new IllegalArgumentException("not a database");
			Database database = (Database) env;
			ItemStack s = database.getStackInSlot(args.checkInteger(1));
			if (s == null)
				throw new IllegalArgumentException("no item in such index");

			int[] ids = OreDictionary.getOreIDs(s);
			boolean found = false;
			Pattern p0 = java.util.regex.Pattern.compile(args.checkString(2));
			Predicate<? super String> predicate = null;
			String mode = args.isString(3) ? args.checkString(3) : "find";
			if (mode.equals("find")) {
				predicate = pp -> p0.matcher(pp).find();

			} else if (mode.equals("match")) {
				predicate = pp -> p0.matcher(pp).matches();
			}
			if (predicate == null)
				throw new IllegalArgumentException("mode is not one of \"find\" and \"match\" ");

			return new Object[] { Arrays.stream(ids).mapToObj(OreDictionary::getOreName)
					.filter(ss -> !ss.equals("Unknown")).filter(predicate).findAny().isPresent() };

			// return new Object[]{ OreDictionary.getOreID(s)};

			// database.GET
		}

		@Callback(doc = "nameToID(name:string):int --string ID -> number ID", direct = false)
		public Object[] nameToID(final Context context, final Arguments args) {

			return new Object[] { Item.getIdFromItem((Item) itemRegistry.getObject(args.checkString(0))) };
		}

		@Callback(doc = "idToName(id:number):string --number ID -> string ID", direct = false)
		public Object[] idToName(final Context context, final Arguments args) {

			return new Object[] { itemRegistry.getNameForObject(Item.getItemById(args.checkInteger(0))) };
		}

		@Override
		public void onConnect(Node node) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onDisconnect(Node node) {

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

			return false;
		}

		@Override
		public void update() {
			// TODO Auto-generated method stub

		}
	}

	@Override
	public String slot(ItemStack stack) {
		// TODO Auto-generated method stub
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

	@Override
	public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
		// TODO Auto-generated method stub
		return stack.getItem() instanceof ItemAPICard;
	}

}
