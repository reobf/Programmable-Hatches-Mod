package reobf.proghatches.item;

import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import com.google.common.base.Optional;
import com.gtnewhorizons.modularui.api.widget.Interactable;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.features.INetworkEncodable;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.tiles.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.WorldCoord;
import appeng.items.tools.powered.ToolWirelessTerminal;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.tile.misc.TileSecurity;
import appeng.tile.networking.TileWireless;
import appeng.util.item.AEFluidStack;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.SoundResource;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.metatileentity.IMetaTileEntityItemPipe;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;

import gregtech.api.metatileentity.implementations.MTEBasicTank;
import gregtech.api.util.GTUtility;
import gregtech.common.tileentities.machines.IDualInputHatch;
import gregtech.common.tileentities.machines.IDualInputInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import reobf.proghatches.gt.metatileentity.BufferedDualInputHatch;
import reobf.proghatches.lang.LangManager;

public class ItemMEPlunger extends DummySuper implements INetworkEncodable {

	public IAEItemPowerStorage asPowerStorage() {
		return (IAEItemPowerStorage) this;
	}

	public ItemMEPlunger(double powerCapacity) {
		super(powerCapacity, Optional.absent());
	}

	public static IGridNode getWirelessGrid(ItemStack is) {
		if (is.getItem() instanceof ItemMEPlunger) {
			String key = ((ItemMEPlunger) is.getItem()).getEncryptionKey(is);
			IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable()
					.getLocatableBy(Long.parseLong(key));
			if (securityTerminal == null)
				return null;
			return securityTerminal.getGridNode(ForgeDirection.UNKNOWN);
		}
		return null;
	}

	public static IGridHost getWirelessGridHost(ItemStack is) {
		if (is.getItem() instanceof ItemMEPlunger) {
			String key = ((ItemMEPlunger) is.getItem()).getEncryptionKey(is);
			IGridHost securityTerminal = (IGridHost) AEApi.instance().registries().locatable()
					.getLocatableBy(Long.parseLong(key));
			if (securityTerminal == null)
				return null;
			return securityTerminal;
		}
		return null;
	}

	private static final String LINK_KEY_STRING = "key";

	@Override
	public String getEncryptionKey(final ItemStack wirelessTerminal) {
		if (wirelessTerminal == null) {
			return null;
		}
		// Ensure the terminal has a tag
		if (wirelessTerminal.hasTagCompound()) {
			// Get the security terminal source key
			String sourceKey = wirelessTerminal.getTagCompound().getString(LINK_KEY_STRING);

			// Ensure the source is not empty nor null
			if ((sourceKey != null) && (!sourceKey.isEmpty())) {
				// The terminal is linked.
				return sourceKey;
			}
		}

		// Terminal is unlinked.
		return "";
	}

	private NBTTagCompound ensureTagCompound(ItemStack is) {
		if (!is.hasTagCompound()) {
			is.setTagCompound(new NBTTagCompound());
		}
		return is.getTagCompound();
	}

	@Override
	public void setEncryptionKey(final ItemStack wirelessTerminal, final String sourceKey, final String name) {
		final NBTTagCompound tag = ensureTagCompound(wirelessTerminal);
		tag.setString(LINK_KEY_STRING, sourceKey);
	}

	public boolean damage(ItemStack aStack) {
		if(aStack.getItemDamage()==1){return true;}
		return asPowerStorage().extractAEPower(aStack, 1000)>999;
	
	}
public boolean check(ItemStack stack, Entity player,IGrid grid){
	if(grid==null)return false;
	for (IGridNode node : grid.getMachines(TileWireless.class)) {
        IWirelessAccessPoint accessPoint = (IWirelessAccessPoint) node.getMachine();
        
        
        
        if(stack.getItemDamage()==0)
	        if (accessPoint.isActive() && accessPoint.getLocation().getDimension() == player.dimension) {
	            WorldCoord distance = accessPoint.getLocation()
	                    .subtract((int) player.posX, (int) player.posY, (int) player.posZ);
	            int squaredDistance = distance.x * distance.x + distance.y * distance.y + distance.z * distance.z;
	            if (squaredDistance <= accessPoint.getRange() * accessPoint.getRange()) {
	               return true;
	               
	            }
	        }
	        else{
	        	 if (accessPoint.isActive()){return true;}
	        	
	        	
	        }
        
        
    }return false;
}
@Override
public int getMaxDamage(ItemStack stack) {
	return 1;
}

protected void getCheckedSubItems(final Item p_150895_1_, final CreativeTabs p_150895_2_,
        final List<ItemStack> p_150895_3_) {

	  p_150895_3_.add(new ItemStack(p_150895_1_, 1, 0));
	  p_150895_3_.add(new ItemStack(p_150895_1_, 1, 1));
}

@Override
public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
		float hitX, float hitY, float hitZ) {

		
		
		if(getEncryptionKey(stack).equals("")){
			if(!world.isRemote)
				player.addChatComponentMessage(new ChatComponentTranslation("item.proghatch_me_plunger.unbound"));
			return true;}
		if( getWirelessGrid(stack)==null){
			if(!world.isRemote)
				player.addChatComponentMessage(new ChatComponentTranslation("item.proghatch_me_plunger.unbound"));
			return true;}
		if(!check(stack,player, getWirelessGrid(stack).getGrid())){
			if(!world.isRemote)
				player.addChatComponentMessage(new ChatComponentTranslation("item.proghatch_me_plunger.range"));
			return true;}
		
		
		if (clearDual(stack, player, world, x, y, z, ForgeDirection.getOrientation(side))) {
			return true;
		}
		;
		if (clearItem(stack, player, world, x, y, z, ForgeDirection.getOrientation(side))) {
			return true;
		}
		;
		if (clearFluid(stack, player, world, x, y, z, ForgeDirection.getOrientation(side))) {
			return true;
		}
		;
		return super.onItemUseFirst(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean clearDual(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ,
			ForgeDirection side) {
		
		if (aWorld.isRemote) {
			return false;
		}
		TileEntity aTileEntity = aWorld.getTileEntity(aX, aY, aZ);
		if (aTileEntity instanceof IGregTechTileEntity) {
			IGregTechTileEntity tTileEntity = (IGregTechTileEntity) aTileEntity;
			IMetaTileEntity mTileEntity = tTileEntity.getMetaTileEntity();
			if (mTileEntity instanceof IDualInputHatch/*BufferedDualInputHatch*/) {
				IStorageGrid storage = getWirelessGrid(aStack).getGrid().getCache(IStorageGrid.class);
				IDualInputHatch dual=(IDualInputHatch) mTileEntity;
				for(IDualInputInventory inv:(Iterable<IDualInputInventory>)(Iterable)(()->dual.inventories())){
					if((aPlayer.capabilities.isCreativeMode)||damage(aStack)){}else{continue;}
				
					for(ItemStack is:inv.getItemInputs()){
						
						IAEItemStack left = storage.getItemInventory()
						.injectItems(
							AEItemStack.create(is)
								,
								Actionable.MODULATE,
								new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));
						is.stackSize=Optional.fromNullable(left).transform(s->s.getStackSize()).or(0l).intValue();
					}
					for(FluidStack is:inv.getFluidInputs()){
					
						IAEFluidStack left = storage.getFluidInventory()
						.injectItems(
							AEFluidStack.create(is)
								,
								Actionable.MODULATE,
								new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));
						is.amount=Optional.fromNullable(left).transform(s->s.getStackSize()).or(0l).intValue();
					}
					
					GTUtility.sendSoundToPlayers(aWorld, SoundResource.IC2_TOOLS_RUBBER_TRAMPOLINE, 1.0F, -1.0F, aX, aY,
							aZ);
				}
				
			return true;	
			}
		
		
		
		}
		
		return false;
	}
	public boolean clearFluid(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ,
			ForgeDirection side) {
		if (aWorld.isRemote) {
			return false;
		}

		IStorageGrid fluid = getWirelessGrid(aStack).getGrid().getCache(IStorageGrid.class);
		/*
		 * IAEItemStack notadd =
		 * fluid.getItemInventory().injectItems(null,Actionable.SIMULATE , new
		 * PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)) );
		 */

		TileEntity aTileEntity = aWorld.getTileEntity(aX, aY, aZ);
		if ((aTileEntity instanceof IFluidHandler)) {
			for (ForgeDirection tDirection : ForgeDirection.VALID_DIRECTIONS) {
				if (((IFluidHandler) aTileEntity).drain(tDirection, 1000, false) != null) {
					if ((aPlayer.capabilities.isCreativeMode) || damage(aStack)) {

						IAEFluidStack notadd = fluid.getFluidInventory()
								.injectItems(
										AEFluidStack.create(((IFluidHandler) aTileEntity).drain(tDirection,
												Integer.MAX_VALUE, false)),
										Actionable.SIMULATE,
										new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));

						if (notadd != null && notadd.getStackSize() > 0) {continue;}

							fluid.getFluidInventory()
									.injectItems(
											AEFluidStack.create(((IFluidHandler) aTileEntity).drain(tDirection,
													Integer.MAX_VALUE, true)),
											Actionable.MODULATE,
											new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));
						

						GTUtility.sendSoundToPlayers(aWorld, SoundResource.IC2_TOOLS_RUBBER_TRAMPOLINE, 1.0F, -1.0F,
								aX, aY, aZ);
						return true;
					}
				}
			}
		}
		if (aTileEntity instanceof IGregTechTileEntity) {
			IGregTechTileEntity tTileEntity = (IGregTechTileEntity) aTileEntity;
			IMetaTileEntity mTileEntity = tTileEntity.getMetaTileEntity();
			if (mTileEntity instanceof MTEBasicTank) {
				MTEBasicTank machine = (MTEBasicTank) mTileEntity;
				if (machine.mFluid != null && machine.mFluid.amount > 0){}else return false;
					// machine.mFluid.amount = machine.mFluid.amount -
					// Math.min(machine.mFluid.amount, 1000);
					
				IAEFluidStack notadd = fluid.getFluidInventory().injectItems(
							AEFluidStack.create(machine.mFluid.copy()), Actionable.SIMULATE,
							new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));

					if (notadd != null && notadd.getStackSize() > 0) {

						fluid.getFluidInventory().injectItems(AEFluidStack.create(machine.mFluid.copy()),
								Actionable.MODULATE,
								new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack))

						);
						machine.mFluid.amount =0;
					}
				
				GTUtility.sendSoundToPlayers(aWorld, SoundResource.IC2_TOOLS_RUBBER_TRAMPOLINE, 1.0F, -1.0F, aX, aY,
						aZ);
				return true;
			}
		}
		return false;
	}

	public boolean clearItem(ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX, int aY, int aZ,
			ForgeDirection side) {
		if (aWorld.isRemote) {
			return false;
		}
		TileEntity aTileEntity = aWorld.getTileEntity(aX, aY, aZ);
		if (aTileEntity instanceof IGregTechTileEntity) {
			IGregTechTileEntity gtTE = (IGregTechTileEntity) aTileEntity;
			IMetaTileEntity tMetaTileEntity = gtTE.getMetaTileEntity();
			if ((tMetaTileEntity instanceof IMetaTileEntityItemPipe)) {
				for (IMetaTileEntityItemPipe tTileEntity : GTUtility
						.sortMapByValuesAcending(IMetaTileEntityItemPipe.Util
								.scanPipes((IMetaTileEntityItemPipe) tMetaTileEntity, new HashMap<>(), 0L, false, true))
						.keySet()) {
					int i = 0;
					for (int j = tTileEntity.getSizeInventory(); i < j; i++) {
						if (tTileEntity.isValidSlot(i)) {
							if ((tTileEntity.getStackInSlot(i) != null)
									&& ((aPlayer.capabilities.isCreativeMode) || damage(aStack))) {

								IStorageGrid item = getWirelessGrid(aStack).getGrid().getCache(IStorageGrid.class);
								IAEItemStack notadd = item.getItemInventory().injectItems(
										AEItemStack.create(tTileEntity.getStackInSlot(i)), Actionable.SIMULATE,
										new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));
								if (notadd != null && notadd.getStackSize() > 0) {
									continue;
								}

								final ItemStack tStack = tTileEntity.decrStackSize(i, Integer.MAX_VALUE / 2);
								if (tStack != null) {
									item.getItemInventory().injectItems(AEItemStack.create(tStack), Actionable.MODULATE,
											new PlayerSource(aPlayer, (IActionHost) getWirelessGridHost(aStack)));
									// discard ret value

									GTUtility.sendSoundToPlayers(aWorld, SoundResource.IC2_TOOLS_RUBBER_TRAMPOLINE,
											1.0F, -1.0F, aX, aY, aZ);
								}

							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
		if(stack.getItemDamage()==1)asPowerStorage().injectAEPower(stack, 10000);
		super.onUpdate(stack, worldIn, entityIn, p_77663_4_, p_77663_5_);
	}
	 @SideOnly(Side.CLIENT)
	    public boolean hasEffect(ItemStack p_77636_1_)
	    {
	        return p_77636_1_.getItemDamage() > 0;
	    }
	 
	 @Override
	public EnumRarity getRarity(ItemStack p_77613_1_) {
	
		 return p_77613_1_.getItemDamage() == 0 ? super.getRarity(p_77613_1_) : EnumRarity.epic;
	}
	@SideOnly(Side.CLIENT)
    public void addCheckedInformation(final ItemStack p_77624_1_, final EntityPlayer p_77624_2_, final List<String> p_77624_3_,
            final boolean p_77624_4_) {
      /*  super.addInformation(stack, player, lines, displayMoreInfo);
    }
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
*/
		//if(p_77624_1_.getItemDamage()==1)p_77624_3_.add("");
		IntStream
				.range(0,
						Integer.valueOf(StatCollector.translateToLocal(
								"item.proghatch_me_plunger.tooltips")))
				.forEach(s -> p_77624_3_.add(LangManager.translateToLocal(
						"item.proghatch_me_plunger.tooltips" + "." + s)));

		super.addCheckedInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
	}
}
