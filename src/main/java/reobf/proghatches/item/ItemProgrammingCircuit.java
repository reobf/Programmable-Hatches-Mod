package reobf.proghatches.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.CommonProxy;
import reobf.proghatches.main.MyMod;

public class ItemProgrammingCircuit extends Item {

	public ItemProgrammingCircuit() {
		this.setCreativeTab(CommonProxy.tab);
	}

	@Override
	public boolean getHasSubtypes() {
		// TODO Auto-generated method stub
		return true;
	}

	public IIcon overlay;
	public IIcon def;

	@SideOnly(Side.CLIENT)
	public void registerIcons(net.minecraft.client.renderer.texture.IIconRegister register) {
		itemIcon = overlay = register.registerIcon("proghatches:overlay");
		def = register.registerIcon("proghatches:defaultcircuit");

	};

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_) {

		String n = getCircuit(p_77653_1_)
				.map(s -> LangManager.translateToLocalFormatted("item.prog_circuit.name.format", s.getDisplayName()))
				.orElse(null);

		;

		if (n != null)
			return n;
		// item.prog_circuit.name.format

		return super.getItemStackDisplayName(p_77653_1_);

	}

	@SideOnly(Side.CLIENT)
	@SuppressWarnings("unchecked")
	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
		int i = 0;
		while (true) {
			String k = "item.prog_circuit.name.tooltip";
			if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
				break;
			}
			String key = k + "." + i;
			String trans = LangManager.translateToLocal(key);

			p_77624_3_.add(trans);
			i++;

		}
		String sid=Optional.ofNullable(p_77624_1_.stackTagCompound).map(s->s.getCompoundTag("targetCircuit"))
		.map(s->s.getString("string_id")).orElse(null);
		
		getCircuit(p_77624_1_).filter(s -> s.getItem() != null).ifPresent(s -> {
			
			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			ArrayList<String> ls = new ArrayList<>();
			if(sid!=null&&!sid.isEmpty()){ls.add(sid+"@"+s.getItemDamage());}
			else {ls.add("§c"+StatCollector.translateToLocal("item.prog_circuit.legacy"));}
			ls.add(s.getDisplayName());
			s.getItem().addInformation(s, p_77624_2_, ls, p_77624_4_);

			int totallength = ls.stream().map(ss->{
				//System.out.println(fr.getStringWidth(ss));
				//System.out.println(ss);
			return fr.getStringWidth(ss);
			}
					).max(Integer::compare).orElse(0);
			// totallength/fr.getStringWidth("-")
			StringBuilder sb = new StringBuilder();
			do {
				sb.append("-");
			//System.out.println(totallength);
			//System.out.println(fr.getStringWidth(sb.toString()));
			//System.out.println((sb.toString()));
			} while (totallength > fr.getStringWidth(sb.toString()));

			ls.add(0, sb.toString());

			// .getStringWidth(p_78256_1_)
			p_77624_3_.addAll(ls);
		});

		Optional<ItemStack> op = getCircuit(p_77624_1_);
		if (op.isPresent()) {
			if (op.get().getItem() == MyMod.progcircuit) {

				p_77624_3_.add(LangManager.translateToLocal("item.prog_circuit.name.tooltip.warn"));
			}

		}

	}

	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack p_77636_1_) {
		return true;
	}
	
	
	public static ItemStack wrap(ItemStack is) {return wrap(is,1);}
	public static ItemStack wrap(ItemStack is,int i) {return wrap(is,i,false);}
	public static ItemStack wrap(ItemStack is,int i,boolean legacy) {

		ItemStack iss = new ItemStack(MyMod.progcircuit,i);
		if (is != null/* &&is.stackSize>0 */ && is.getItem() != null) {
			is = is.copy();
			is.stackSize = 1;// Math.max(1,is.stackSize);
			iss.stackTagCompound = new NBTTagCompound();
			NBTTagCompound tag = is.writeToNBT(new NBTTagCompound());
			
			if(!legacy)tag.setString("string_id", Item.itemRegistry.getNameForObject(is.getItem()));
			//System.out.println(tag);
			iss.stackTagCompound.setTag("targetCircuit",tag );
			
		}
		return iss;

	}
public static ItemStack parse(NBTTagCompound tag){
	
	String s=tag.getString("string_id");
	if(s.isEmpty()==false){
		//if string id is present, replace the number id
		tag.setInteger("id", Item.itemRegistry.getIDForObject(Item.itemRegistry.getObject(s)));
	}
	
     return ItemStack.loadItemStackFromNBT(tag);
	
	
	
}
	public static Optional<ItemStack> getCircuit(ItemStack is) {
		try {
			NBTTagCompound tg = Optional.ofNullable(is).map(ItemStack::getTagCompound)
					.map(tag -> tag.getCompoundTag("targetCircuit")).orElse(null);
			if (tg == null)
				return Optional.empty();

			return Optional.ofNullable(parse(tg));
		
		} catch (Exception e) {
			e.printStackTrace();
			// but how?
			return Optional.empty();
		}

	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		// TODO Auto-generated method stub
		return super.getUnlocalizedName(stack);
	}
}
