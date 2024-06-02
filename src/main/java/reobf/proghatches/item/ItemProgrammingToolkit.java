package reobf.proghatches.item;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.screen.IItemWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.modularui.GT_UIInfos;

public class ItemProgrammingToolkit extends Item implements IItemWithModularUI {

	/*
	 * @Override public ModularWindow createWindow(UIBuildContext buildContext,
	 * ItemStack heldStack) { // TODO Auto-generated method stub return null; }
	 */
	IIcon[] icons = new IIcon[16];

	@Override
	public boolean getHasSubtypes() {
		// TODO Auto-generated method stub
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register) {
		icons[0] = register.registerIcon("proghatches:toolkit0");
		icons[1] = register.registerIcon("proghatches:toolkit1");
		icons[2] = register.registerIcon("proghatches:toolkit2");
		// super.registerIcons(register);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int p_77617_1_) {
		return this.icons[p_77617_1_];
	}

	{
		this.maxStackSize = 1;
	}

	@SideOnly(Side.CLIENT)
	public static boolean holding() {
		return lastholdingtick == Minecraft.getMinecraft().thePlayer.ticksExisted;
	}

	@SideOnly(Side.CLIENT)
	public static boolean addEmptyProgCiruit() {
		return mode == 2;
	}

	public static long lastholdingtick;
	public static int mode;

	@SideOnly(Side.CLIENT)
	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int p_77663_4_, boolean p_77663_5_) {
		if (stack.getItemDamage() > 0)
			if (entityIn == Minecraft.getMinecraft().thePlayer)
				lastholdingtick = Minecraft.getMinecraft().thePlayer.ticksExisted;
		mode = stack.getItemDamage();

		super.onUpdate(stack, worldIn, entityIn, p_77663_4_, p_77663_5_);
	}

	@SuppressWarnings("unchecked")
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {

		super.addInformation(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);

		switch (p_77624_1_.getItemDamage()) {

		case 0: {
			p_77624_3_.add(LangManager.translateToLocal("item.prog_toolkit.name.tooltip.mode.0"));
		}
			break;
		case 1: {
			p_77624_3_.add(LangManager.translateToLocal("item.prog_toolkit.name.tooltip.mode.1"));
		}
			break;
		case 2: {
			p_77624_3_.add(LangManager.translateToLocal("item.prog_toolkit.name.tooltip.mode.2"));
		}

		}

		int i = 0;
		while (true) {
			String k = "item.prog_toolkit.name.tooltip";
			if (LangManager.translateToLocal(k).equals(Integer.valueOf(i).toString())) {
				break;
			}
			String key = k + "." + i;
			String trans = LangManager.translateToLocal(key);

			p_77624_3_.add(trans);
			i++;

		}

		;

	}

	int maxModes = 3;

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
		if (player.isSneaking()) {

			itemStackIn.setItemDamage((itemStackIn.getItemDamage() + 1) % maxModes);

		} else {

			GT_UIInfos.openPlayerHeldItemUI(player);
		}

		return super.onItemRightClick(itemStackIn, worldIn, player);
	}

	@Override
	public ModularWindow createWindow(UIBuildContext buildContext, ItemStack heldStack) {

		return new UIFactory(buildContext).createWindow();
	}

	protected class UIFactory {

		private final UIBuildContext uiBuildContext;

		public UIFactory(UIBuildContext buildContext) {
			this.uiBuildContext = buildContext;
		}

		public ModularWindow createWindow() {
			ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
			builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
			// builder.setGuiTint(getUIBuildContext().getGuiColorization());
			if (doesBindPlayerInventory()) {
				builder.bindPlayerInventory(getUIBuildContext().getPlayer());
			}
			// builder.bindPlayerInventory(builder.getPlayer(), 7,
			// getGUITextureSet().getItemSlot());

			addTitleToUI(builder);
			addUIWidgets(builder);
			/*
			 * if (getUIBuildContext().isAnotherWindow()) { builder.widget(
			 * ButtonWidget.closeWindowButton(true) .setPos(getGUIWidth() - 15,
			 * 3)); }
			 */

			/*
			 * final CoverInfo coverInfo = uiBuildContext.getTile()
			 * .getCoverInfoAtSide(uiBuildContext.getCoverSide()); final
			 * GT_CoverBehaviorBase<?> behavior = coverInfo.getCoverBehavior();
			 * if (coverInfo.getMinimumTickRate() > 0 &&
			 * behavior.allowsTickRateAddition()) { builder.widget( new
			 * GT_CoverTickRateButton(coverInfo, builder).setPos(getGUIWidth() -
			 * 24, getGUIHeight() - 24)); }
			 */
			return builder.build();
		}

		/**
		 * Override this to add widgets for your UI.
		 */

		// IItemHandlerModifiable fakeInv=new ItemHandlerModifiable();
class TakeOnlyItemStackHandler extends ItemStackHandler{
	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		
		return false;//not allowed to put in
	}
}
		protected void addUIWidgets(ModularWindow.Builder builder) {
			ItemStackHandler is0 = new ItemStackHandler();
			builder.widget(new SlotWidget(new BaseSlot(is0, 0)) {

				@Override
				public void onDestroy() {
					// System.out.println(this.getMcSlot().getStack());
					EntityPlayer p = getContext().getPlayer();
					if (p.getEntityWorld().isRemote == false)
						if (this.getMcSlot().getStack() != null)
							p.getEntityWorld().spawnEntityInWorld(

									new EntityItem(p.getEntityWorld(), p.posX, p.posY, p.posZ,
											this.getMcSlot().getStack().copy()));
				}
			}.addTooltip(LangManager.translateToLocal("item.prog_toolkit.name.tooltip.emptyinput")).setPos(3, 3));
			ItemStackHandler is = new TakeOnlyItemStackHandler();
			SlotWidget sw = new SlotWidget(new BaseSlot(is, 0)) {

				@Override
				public void detectAndSendChanges(boolean init) {
					
					
					this.getMcSlot().putStack(ItemProgrammingCircuit
							.wrap(Optional.ofNullable(is0.getStackInSlot(0))
									.map(s->{if(s.getItem()==MyMod.progcircuit){return ItemProgrammingCircuit.getCircuit(s).orElse(null);}return s;})
									.map(ItemStack::copy).orElse(null),64));
					
					
					super.detectAndSendChanges(init);
				}

			};
			// sw.setTicker(s->{});
			builder.widget(sw.setPos(3 + 18, 3));

			
			is = new TakeOnlyItemStackHandler();
			Widget sw2 = new SlotWidget(new BaseSlot(is, 0)) {

				@Override
				public void detectAndSendChanges(boolean init) {
					
					
					this.getMcSlot().putStack(ItemProgrammingCircuit
							.wrap(Optional.ofNullable(is0.getStackInSlot(0))
									.map(s->{if(s.getItem()==MyMod.progcircuit){return ItemProgrammingCircuit.getCircuit(s).orElse(null);}return s;})
									.map(ItemStack::copy).orElse(null),64,true));
					if(is0.getStackInSlot(0)==null){
						this.getMcSlot().putStack(null);
						
					}
					
					super.detectAndSendChanges(init);
				}

			}.addTooltips(ImmutableList.of(
					StatCollector.translateToLocal("item.prog_toolkit.legacywarning.0"),
					StatCollector.translateToLocal("item.prog_toolkit.legacywarning.1"),
					StatCollector.translateToLocal("item.prog_toolkit.legacywarning.2"),
					StatCollector.translateToLocal("item.prog_toolkit.legacywarning.3")
					));;
			
			
			// sw.setTicker(s->{});
			builder.widget(sw2.setPos(getGUIWidth()- 18-3, 3));
		}

		public UIBuildContext getUIBuildContext() {
			return uiBuildContext;
		}

		/*
		 * public boolean isCoverValid() { return !getUIBuildContext().getTile()
		 * .isDead() && getUIBuildContext().getTile()
		 * .getCoverBehaviorAtSideNew(getUIBuildContext().getCoverSide()) !=
		 * GregTech_API.sNoBehavior; }
		 */

		protected void addTitleToUI(ModularWindow.Builder builder) {
			/*
			 * ItemStack coverItem =
			 * GT_Utility.intToStack(getUIBuildContext().getCoverID()); if
			 * (coverItem != null) { builder.widget( new
			 * ItemDrawable(coverItem).asWidget() .setPos(5, 5) .setSize(16,
			 * 16)) .widget( new
			 * TextWidget(coverItem.getDisplayName()).setDefaultColor(
			 * COLOR_TITLE.get()) .setPos(25, 9)); }
			 */
		}

		protected int getGUIWidth() {
			return 176;
		}

		protected int getGUIHeight() {
			return 107;
		}

		protected boolean doesBindPlayerInventory() {
			return true;
		}

		protected int getTextColorOrDefault(String textType, int defaultColor) {
			return defaultColor;
		}

		protected final Supplier<Integer> COLOR_TITLE = () -> getTextColorOrDefault("title", 0x222222);
		protected final Supplier<Integer> COLOR_TEXT_GRAY = () -> getTextColorOrDefault("text_gray", 0x555555);
		protected final Supplier<Integer> COLOR_TEXT_WARN = () -> getTextColorOrDefault("text_warn", 0xff0000);
	}

}
