package reobf.proghatches.eucrafting;

import java.util.Optional;

import javax.annotation.Nullable;

import com.gtnewhorizons.modularui.ModularUI;
import com.gtnewhorizons.modularui.api.screen.IContainerCreator;
import com.gtnewhorizons.modularui.api.screen.IGuiCreator;
import com.gtnewhorizons.modularui.api.screen.IItemWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.InternalUIMapper;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.tile.AEBaseTile;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.IEUSource.EUSource;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.net.OpenPartGuiMessage;

public class EUUtil {
	public static void register(){
		AEApi.instance().registries().gridCache().registerGridCache(IEUSource.class, EUSource.class);
		
		
		
	}
public static boolean  check(ISegmentedInventory inv){
	
	IInventory sub = inv.getInventoryByName("upgrades");
	
	for(int i=0;i<sub.getSizeInventory();i++){
		if(
		Optional.ofNullable(sub.getStackInSlot(i))
			.map(ItemStack::getItem)
			.filter(s->s==MyMod.euupgrade)
			.isPresent()
		){
			return true;
		}

		
		
	}
	
	
	return false;
}

@Nullable
public static IGuiProvidingPart select(EntityPlayer player ,int x, int y, int z){
	
	NBTTagCompound tag = player.getEntityData();
	if(tag.hasKey(key)==false)return null;
	int i=tag.getInteger(key);
		//tag.removeTag(key);
	TileEntity tt= player.getEntityWorld().getTileEntity(x, y, z);
	if(tt instanceof IPartHost){
	IPartHost ae=(IPartHost) tt;

	return (IGuiProvidingPart) Optional.ofNullable(ae.getPart(ForgeDirection.getOrientation(i)))
	.filter(s->s instanceof IGuiProvidingPart)
	.get();
	
	
	}
	return null;
}public static final String key="proghatches.last.part.direction";

public static void open(EntityPlayer player, World world, int x, int y, int z,ForgeDirection dir) {
	 if (NetworkUtils.isClient()==false||player instanceof FakePlayer) {return;}
	NBTTagCompound tag = player.getEntityData();
	tag.setInteger(key,dir.ordinal());
	
	
	
	MyMod.net.sendToServer(new OpenPartGuiMessage(x,y,z,dir));
}

public static final UIInfo<?, ?> PART_MODULAR_UI   = UIBuilder.of().container((player, world, x, y, z) -> {
  return Optional.ofNullable(select(player,  x, y, z)).map(part->{

UIBuildContext buildContext = new UIBuildContext(player);
ModularWindow window = part.createWindow(buildContext);
return new ModularUIContainer(
                new ModularUIContext(buildContext, () -> {
                	player.inventoryContainer.detectAndSendChanges();
                	part.markDirty();
                }),
                window,
                null);
		
		
		
  }).orElse(null)
		;
  
}).gui((player, world, x, y, z) -> {
	  return Optional.ofNullable(select(player,  x, y, z)).map(part->{

		  UIBuildContext buildContext = new UIBuildContext(player);
		  ModularWindow window = part.createWindow(buildContext);
		  return new ModularGui(new ModularUIContainer(
		                  new ModularUIContext(buildContext, () -> player.inventoryContainer.detectAndSendChanges()),
		                  window,
		                  null));
		  		
		  		
		  		
		    }).orElse(null)
		  		;
}).build();
}
