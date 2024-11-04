package reobf.proghatches.main.mixin;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.networking.crafting.ICraftingMedium;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.MachineSource;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerNull;
import appeng.crafting.MECraftingInventory;
import appeng.me.cache.CraftingGridCache;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.item.AEItemStack;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import reobf.proghatches.eucrafting.IEUManager;
import reobf.proghatches.eucrafting.IEUManager.EUManager;
import reobf.proghatches.eucrafting.IEUManager.IDrain;
import reobf.proghatches.eucrafting.TileFluidInterface_EU.SISOPatternDetail;
import reobf.proghatches.gt.metatileentity.DualInputHatch;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class MixinCallback {

	public static boolean encodingSpecialBehaviour = true;
	public static Function<Object, Long> getter;
	public static BiConsumer<Object, Long> setter;
	//spotless:off
	static {
		try {
			Field n = Class.forName("appeng.me.cluster.implementations.CraftingCPUCluster$TaskProgress")
					.getDeclaredField("value");
			n.setAccessible(true);
			setter = (s, b) -> {
				try {n.set(s, b);} catch (Exception e) {e.printStackTrace();}
			};
			getter = s -> {
				try {return (Long) n.get(s);} catch (Exception e) {e.printStackTrace();}
				return null;
			};
		} catch (Exception e) {e.printStackTrace();}

	}
	//spotless:on

	public static void handleAddedToMachineList(IGregTechTileEntity aTileEntity, Object o) {
		GT_MetaTileEntity_MultiBlockBase thiz = (GT_MetaTileEntity_MultiBlockBase) o;
		try {
			if (aTileEntity == null)
				return;
			IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
			if (aMetaTileEntity != null && aMetaTileEntity instanceof DualInputHatch) {
				((DualInputHatch) aMetaTileEntity).setFilter(thiz.getRecipeMap());
			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}

	}
public static void aa(Object t){
	//System.out.println(t);
	
}

public static boolean fixCircuitTag(NBTTagCompound stackTagCompound){
	if(stackTagCompound!=null){
		try{
	
		boolean hasID=	
				stackTagCompound.getCompoundTag("targetCircuit").hasKey("id");
		boolean hasSTR=	
				stackTagCompound.getCompoundTag("targetCircuit").hasKey("string_id");	
	
		//V1->V2 
		//V2->V2
		//V3->V3
		try{
	if(hasID&&!hasSTR){	
		int id = stackTagCompound.getCompoundTag("targetCircuit").getInteger("id");
		stackTagCompound.getCompoundTag("targetCircuit").setString("string_id",
				Item.itemRegistry.getNameForObject(
				Item.itemRegistry.getObjectById(id)
				)
				);
		hasSTR=true;
	}		}catch(Exception e){}

	//V1->X 
	//V2->V3
	//V3->V3		
		try{
	if(hasSTR&&hasID){	
	stackTagCompound.getCompoundTag("targetCircuit").removeTag("id");	
	return true;
	}	}catch(Exception e){}
	
	
	
	
	//System.out.println(this);
		}catch(Exception e){}
	}
	return false;
}


private static AEItemStack type = AEItemStack.create(new ItemStack(MyMod.eu_token, 1, 1));
public static void cb(final IEnergyGrid eg, final CraftingGridCache cc, CallbackInfo RE
		,Map<IAEItemStack, Long> needed
		,Map<IAEItemStack, Long> storage
		,MECraftingInventory inventory,
		Map<ICraftingPatternDetails, Object> tasks,
		LinkedList<TileCraftingTile> tiles,
		 MachineSource machineSrc
		) {
	try{
	if (needed.isEmpty()) {
		storage.clear();
		return;
	}

	

	inventory.getItemList().findFuzzy(type, FuzzyMode.IGNORE_ALL).forEach(s -> {

		if (s.getItem() == MyMod.eu_token) {
			if (s.getItemDamage() == 1) {

				IAEItemStack u = s.copy().setStackSize(1);

				storage.merge(u, s.getStackSize(), Long::sum);
			}

		}

	});
	//System.out.println(storage);
	tasks.entrySet().forEach(s -> {
		// TODO remove

		if (s.getKey() instanceof SISOPatternDetail) {
			SISOPatternDetail d = (SISOPatternDetail) s.getKey();
			if (d.out.getItemDamage() == 1) {
				IAEItemStack key = d.o[0].copy().setStackSize(1);

				storage.merge(key, MixinCallback.getter.apply(s.getValue()), Long::sum);

			}

		}

	}); 
	//System.out.println(storage);
	// System.out.println(needed);

	needed.entrySet().forEach(s -> {

		long num = Optional.ofNullable(storage.get(s.getKey())).orElse(0l);
		long missing = s.getValue() - num;
		if (missing <= 0)
			return;
		// Object o=this;
		// CraftingCPUCluster thiz=(CraftingCPUCluster) o;
		// System.out.println(s.getValue()+" "+num);
		// System.out.println(missing);

		if (tiles.isEmpty()) {
			return;
		}
		a:try {
			EUManager man = tiles.get(0).getProxy().getGrid().getCache(IEUManager.class);
			Optional<IDrain> opt = man.cache.get(s.getKey().getTagCompound().getNBTTagCompoundCopy().getLong("voltage"))
			.stream().filter(sp->sp.getUUID().equals(ProghatchesUtil.deser(s.getKey().getTagCompound().getNBTTagCompoundCopy(), "EUFI")))
			.findFirst();
			if(!opt.isPresent())break a;
			if((!opt.get().allowOvercommit())&&opt.get().getAmp()>0){break a;}
			long get = man.request(s.getKey().getTagCompound().getNBTTagCompoundCopy().getLong("voltage"), missing);
			if(get>0){
			
				inventory.injectItems(s.getKey().copy().setStackSize(get), Actionable.MODULATE, machineSrc);
				opt
				.get().refund(-get);
				;
				MyMod.LOG.info("Auto Request:" + get + "*" + s.getKey().getTagCompound().getNBTTagCompoundCopy());
				MyMod.LOG.info(MinecraftServer.getServer().getTickCounter());
				//MyMod.LOG.info(MinecraftServer.getServer().getTickCounter());
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		/*	
			*/

	});

	storage.clear();
	needed.clear();
	}catch(Exception e){MyMod.LOG.error("caught error in mixin",e);}
}




}


