package reobf.proghatches.gt.metatileentity;

import static gregtech.api.enums.Textures.BlockIcons.ITEM_IN_SIGN;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.google.common.collect.ImmutableMap;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_InputBus;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.recipe.check.CheckRecipeResult;
import gregtech.api.recipe.check.CheckRecipeResultRegistry;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Utility;
import gregtech.common.tileentities.machines.IRecipeProcessingAwareHatch;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.main.registration.Registration;

public class RemoteInputBus extends GT_MetaTileEntity_Hatch_InputBus implements IRecipeProcessingAwareHatch {

	static public ArrayList<String> blacklist = new ArrayList<>();
	static {

		blacklist.add(RemoteInputBus.class.getTypeName());
		blacklist.add("thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface");
		blacklist.add("remoteio.common.tile.TileRemoteInterface");
	}

	@Override
	public ITexture[] getTexturesActive(ITexture aBaseTexture) {
		return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
				? new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) }
				: new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) };
	}

	@Override
	public ITexture[] getTexturesInactive(ITexture aBaseTexture) {
		return GT_Mod.gregtechproxy.mRenderIndicatorsOnHatch
				? new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) }
				: new ITexture[] { aBaseTexture, TextureFactory.of(ITEM_IN_SIGN) };
	}

	public RemoteInputBus(String aName, int aTier, String[] aDescription, ITexture[][][] aTextures) {
		super(aName, aTier, aDescription, aTextures);

	}

	@Override
	public void onLeftclick(IGregTechTileEntity aBaseMetaTileEntity, EntityPlayer aPlayer) {
		if (aBaseMetaTileEntity.getWorld().isRemote)
			return;
		this.markDirty();
		if (aPlayer.isSneaking() && aPlayer.getHeldItem() == null) {
			linked = false;
			aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.detach"));

			return;
		}
		try {
			String s = aPlayer.getHeldItem().getTagCompound().getString("dataLines0");
			String[] splits = s.split("§b|§r");
			int x = Integer.valueOf(splits[1].replace(",", ""));
			int y = Integer.valueOf(splits[3].replace(",", ""));
			int z = Integer.valueOf(splits[5].replace(",", ""));
			int d = Integer.valueOf(splits[7].replace(",", ""));
			World w = this.getBaseMetaTileEntity().getWorld();
			if (d == w.provider.dimensionId) {

				this.x = x;
				this.y = y;
				this.z = z;

				if (checkBlackList()
				// blacklist.contains(this.getBaseMetaTileEntity().getWorld().getBlock(x,
				// y, z).getUnlocalizedName())
				) {

					aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.blacklisted"));
					this.linked = false;
					return;
				}
				aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.success"));
				this.linked = true;

				return;

			} else {
				this.linked = false;
				aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.dim"));
				aPlayer.addChatComponentMessage(null);
			}
			;

		} catch (Exception w) {
			this.linked = false;
			aPlayer.addChatMessage(new ChatComponentTranslation("programmable_hatches.remote.fail"));

		}
		super.onLeftclick(aBaseMetaTileEntity, aPlayer);
	}

	@SuppressWarnings("unused")
	private boolean checkBlackList() {
		World ww = this.getBaseMetaTileEntity().getWorld();
		return Optional.ofNullable(ww.getTileEntity(x, y, z)).map(TileEntity::getClass).map(Class::toString)
				.map(blacklist::contains).orElse(false) ||
		// Optional.ofNullable(ww.getBlock(x, y,
		// z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
				Optional.ofNullable(ww.getTileEntity(x, y, z)).filter(sp -> sp instanceof IGregTechTileEntity)
						.map(sp -> ((IGregTechTileEntity) sp).getMetaTileEntity().getClass().getTypeName())
						.map(blacklist::contains).orElse(false);

	}

	@SuppressWarnings("unused")
	private boolean checkBlackList(Optional<TileEntity> opt) {// World
																// ww=this.getBaseMetaTileEntity().getWorld();
		return opt.map(TileEntity::getClass).map(Class::toString).map(blacklist::contains).orElse(false) ||
		// Optional.ofNullable(ww.getBlock(x, y,
		// z)).map(Block::getClass).map(Class::toString).map(blacklist::contains).orElse(false)||
				opt.filter(sp -> sp instanceof IGregTechTileEntity)
						.map(sp -> ((IGregTechTileEntity) sp).getMetaTileEntity().getClass().getTypeName())
						.map(blacklist::contains).orElse(false);

	}

	public RemoteInputBus(int id, String name, String nameRegional, int tier) {
		super(id, name, nameRegional, 0, 0, reobf.proghatches.main.Config.get("RIB", ImmutableMap.of())
		/*
		 * defaultObj(
		 * 
		 * ArrayExt.of(
		 * "Item Input for Multiblocks, wirelessly linked to a container, just like RemoteIO interface."
		 * , "LMB click this block with a tricorder with target coord to link.",
		 * "Items not extractable by pipes will not be accessible, with an exception of virtual circuits."
		 * ,
		 * "Cannot work across dimension. Will not load target chunk. Will not work if target chunk is unloaded."
		 * ,LangManager.translateToLocal("programmable_hatches.addedby") ),
		 * ArrayExt.of( "像RemoteIO一样远程访问某个容器中的物品 作为多方块机器的输入",
		 * "三录仪记录目标坐标后,左键此方块设定坐标", "无法被管道抽出的物品(除了虚拟电路板,如果目标有)也不能被访问",
		 * "不能跨维度链接 不会触发目标区块加载,且目标区块未加载时不工作"
		 * ,LangManager.translateToLocal("programmable_hatches.addedby") ))
		 */);
		Registration.items.add(new ItemStack(GregTech_API.sBlockMachines, 1, id));

	}

	@Override
	public void addUIWidgets(Builder builder, UIBuildContext buildContext) {
		// super.addUIWidgets(builder, buildContext);
		builder.widget(TextWidget.dynamicString(() -> {

			if (!linked) {
				return LangManager.translateToLocal("programmable_hatches.remote.unlinked");
			}

			Optional<TileEntity> opt = getTile();
			if (this.getBaseMetaTileEntity().getWorld().getChunkProvider().chunkExists(x >> 4, z >> 4) == false)
				return LangManager.translateToLocal("programmable_hatches.remote.chunk");

			if (opt.isPresent() == false)
				return LangManager.translateToLocal("programmable_hatches.remote.nothing");
			if (opt.get() instanceof IInventory == false) {
				return LangManager.translateToLocal("programmable_hatches.remote.dummytarget");

			}

			return LangManager.translateToLocal("programmable_hatches.remote.ok");

		}

		).setSynced(true).setPos(5, 5));
		// buildContext.addCloseListener(() -> uiButtonCount = 0);
	}

	@Override
	public MetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {

		return new RemoteInputBus(mName, mTier, mDescriptionArray, mTextures);
	}

	@Override
	public boolean canExtractItem(int aIndex, ItemStack aStack, int ordinalSide) {

		return false;
	}

	@Override
	public boolean canInsertItem(int aIndex, ItemStack aStack, int ordinalSide) {

		return false;
	}

	public Optional<TileEntity> getTile() {
		if (this.getBaseMetaTileEntity().getWorld().getChunkProvider().chunkExists(x >> 4, z >> 4) == false) {
			return Optional.empty();
		}
		return Optional.ofNullable(this.getBaseMetaTileEntity().getWorld().getTileEntity(x, y, z));
	}

	public List<ItemStack> filterTakable(TileEntity e) {
		if (e == null || (e instanceof IInventory == false))
			return new ArrayList<ItemStack>();
		IInventory inv = (IInventory) e;

		ArrayList<ItemStack> arr = new ArrayList<ItemStack>();
		// boolean b=e instanceof ISidedInventory;

		int size = inv.getSizeInventory();
		if (e instanceof ISidedInventory) {

			ISidedInventory side = (ISidedInventory) e;
			HashSet<Integer> slots = new HashSet<Integer>();

			ForgeDirection dir = this.getBaseMetaTileEntity().getFrontFacing();
			// for(ForgeDirection dir:ForgeDirection.VALID_DIRECTIONS){
			for (int i : side.getAccessibleSlotsFromSide(dir.ordinal())) {
				if (side.canExtractItem(i, side.getStackInSlot(i), dir.ordinal()))
					slots.add(i);
			}
			;
			// }

			slots.stream().map(inv::getStackInSlot).forEach(arr::add);
			;
			for (int i = 0; i < size; i++) {
				if (slots.contains(i)) {
					ItemStack item = inv.getStackInSlot(i);
					arr.add(item);
				} else {
					arr.add(null);
				}

			}

		} else {

			for (int i = 0; i < size; i++) {
				ItemStack item = inv.getStackInSlot(i);
				arr.add(item);
			}

		}

		return arr;
	}

	int x, y, z;
	boolean linked;

	// boolean justQueried;
	@Override
	public void saveNBTData(NBTTagCompound aNBT) {

		super.saveNBTData(aNBT);

		aNBT.setIntArray("coord", new int[] { x, y, z });
		aNBT.setBoolean("linked", linked);
	}

	@Override
	public void loadNBTData(NBTTagCompound aNBT) {

		super.loadNBTData(aNBT);
		int[] i = aNBT.getIntArray("coord");
		x = i[0];
		y = i[1];
		z = i[2];

		linked = aNBT.getBoolean("linked");
	}

	@Override
	public boolean shouldDropItemAt(int index) {

		return false;
	}

	Throwable t = new Throwable();

	public boolean checkDepth() {
		t.fillInStackTrace();

		boolean b = t.getStackTrace().length > 80;
		if (b) {

			MyMod.LOG.fatal("Warning! Potential infinite recursion!");
			MyMod.LOG.fatal("To prevent stack overflow, the block will be removed.");
			MyMod.LOG.fatal(x + "," + y + "," + z + "@dim:" + getBaseMetaTileEntity().getWorld().provider.dimensionId);
			t.printStackTrace();

		}
		return b;
	}

	private int count;

	public boolean checkDepthLoose() {
		if (count++ < 40)
			return false;
		count = 0;
		return checkDepth();
	}

	@Override
	public int getSizeInventory() {
		if (!processingRecipe)
			return 1;
		// justQueried=true;
		Optional<TileEntity> opt = getTile();
		if (opt.isPresent() && checkBlackList(opt)) {
			this.linked = false;
		}
		if (!linked)
			return 1;
		if (checkDepthLoose()) {
			getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);

			return 0;
		}

		return opt.filter(s -> s instanceof IInventory).map(s -> ((IInventory) s).getSizeInventory()).orElse(0) + 2;

	}

	@Override
	public void setInventorySlotContents(int aIndex, ItemStack aStack) {
		markDirty();
		if (aIndex == 0) {
			mInventory[aIndex] = GT_Utility.copyAmount(0, aStack);
			return;
		}
		List<ItemStack> arr = getTile().map(this::filterTakable).orElseGet(ArrayList::new);

		if (aIndex >= 0 && aIndex - 1 < arr.size())
			arr.set(aIndex - 1, aStack);
	}

	@Override
	@Nullable
	public ItemStack getStackInSlot(int aIndex) {

		// justQueried=true;
		Optional<TileEntity> opt = getTile();
		if (opt.isPresent() && checkBlackList(opt)) {
			this.linked = false;
		}
		if (checkDepthLoose()) {
			getBaseMetaTileEntity().getWorld().setBlockToAir(this.x, this.y, this.z);
			return null;
		}

		int i = getCircuitSlot();
		if (i == aIndex)
			return mInventory[i];
		if (!processingRecipe)
			return null;
		if (!linked)
			return null;

		// Optional<TileEntity> opt = getTile();
		List<ItemStack> arr = opt.map(this::filterTakable).orElseGet(ArrayList::new);
		if (aIndex - 1 == arr.size()) {

			TileEntity gt = opt.orElse(null);
			if (gt != null && gt instanceof IGregTechTileEntity) {
				IMetaTileEntity meta = ((IGregTechTileEntity) gt).getMetaTileEntity();
				if (meta != null && (meta instanceof IConfigurationCircuitSupport)) {
					IConfigurationCircuitSupport c = (IConfigurationCircuitSupport) meta;
					return meta.getStackInSlot(c.getCircuitSlot());
				}
			}

			return null;
		}
		if (aIndex < 0 || aIndex - 1 >= arr.size()) {
			return null;
		}

		return arr.get(aIndex - 1);
		// }catch(Exception e){e.printStackTrace();return null;}

	}

	@Override
	public int getCircuitSlot() {

		return 0;
	}

	public void updateSlots() {
	}// no we don't

	public void removePhantom() {

		getTile().filter(s -> s instanceof IInventory).ifPresent(s -> {

			IInventory a = ((IInventory) s);
			int size = a.getSizeInventory();
			for (int i = 0; i < size; i++) {

				a.decrStackSize(i, 0);// remove 0-sized phantom item
			}

		});
	}

	@Override
	public void startRecipeProcessing() {
		processingRecipe = true;

	}

	protected boolean processingRecipe = false;

	@Override
	public CheckRecipeResult endRecipeProcessing(GT_MetaTileEntity_MultiBlockBase controller) {
		processingRecipe = false;
		removePhantom();
		return CheckRecipeResultRegistry.SUCCESSFUL;
	}

}
