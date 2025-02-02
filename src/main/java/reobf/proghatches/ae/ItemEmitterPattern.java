package reobf.proghatches.ae;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.forge.ItemStackHandler;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.IItemWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.wrapper.BaseSlot;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.items.misc.ItemEncodedPattern;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.modularui.GTUIInfos;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.util.GTUtil;
import gregtech.api.util.GTUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.IItemRenderer.ItemRendererHelper;
import net.minecraftforge.fluids.FluidStack;
import reobf.proghatches.item.DummySuper2;
import reobf.proghatches.item.ItemProgrammingCircuit;

import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class ItemEmitterPattern extends DummySuper2 implements ICraftingPatternItem,IItemWithModularUI{

	public ItemEmitterPattern() {
		super();
		 if (Platform.isClient()) {
	            MinecraftForgeClient.registerItemRenderer(this, new ItemEmitterPatternRenderer());
	        }
	}
	

public static class ItemEmitterPatternRenderer implements IItemRenderer {

    private final RenderItem ri = new RenderItem();
    private boolean recursive = false;

    @Override
    public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
        final boolean isShiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);

        if (!this.recursive && type == IItemRenderer.ItemRenderType.INVENTORY && isShiftHeld) {
            final ItemEmitterPattern iep = (ItemEmitterPattern) item.getItem();

            if (iep.getOutput(item) != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item,
            final ItemRendererHelper helper) {
        return false;
    }

    @Override
    public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
        this.recursive = true;

        final ItemEmitterPattern iep = (ItemEmitterPattern) item.getItem();
        final ItemStack is = iep.getOutput(item);
        final Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_LIGHTING_BIT);
        RenderHelper.enableGUIStandardItemLighting();
        this.ri.renderItemAndEffectIntoGUI(mc.fontRenderer, mc.getTextureManager(), is, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GL11.glPopAttrib();

        this.recursive = false;
    }
}

@Override
public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer player) {
	if(itemStackIn.stackSize>1){return itemStackIn;}
	
	GTUIInfos.openPlayerHeldItemUI(player);
	return itemStackIn;
}
	public ItemStack getOutput(ItemStack item) {
	
	ICraftingPatternDetails k = getPatternForItem(item, null);
	
	if(k!=null)return k.getOutput(null, null);
	return null;
}
	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
		NBTTagCompound tag = is.getTagCompound();
		if(tag!=null){
			
			ItemStack isx=ItemStack.loadItemStackFromNBT(tag.getCompoundTag("out"));
			if(isx!=null)return new EmitterPattern(is, AEItemStack.create(isx).setStackSize(tag.getLong("num")));
		}
		
		return null;
	}

	@Override
	public ModularWindow createWindow(UIBuildContext buildContext, ItemStack heldStack) {
	
		return new UIFactory(buildContext).createWindow();
	}
	protected class UIFactory {

		private final UIBuildContext uiBuildContext;
		private ItemStack getCurrentItem() {
            return uiBuildContext.getPlayer().inventory.getCurrentItem();
        }
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
			 * CoverBehaviorBase<?> behavior = coverInfo.getCoverBehavior();
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
			
			builder.widget(new TextFieldWidget().setGetter(() -> {
				if(getCurrentItem().getTagCompound()!=null){
					return getCurrentItem().getTagCompound().getLong("num")+"";
				}
				
				return "1";
			})
                    .setSetter(val -> {
                    	if(getCurrentItem().getTagCompound()==null){getCurrentItem().setTagCompound(new NBTTagCompound());};
        				
                    	 getCurrentItem().getTagCompound().setLong("num",Long.valueOf( val));
                    	
                    })
                    .setNumbersLong(s->{if(s<0)return 1l;return s;})
                    //.setBounds(1, 3)
                    .setTextColor(Color.WHITE.normal)
                    .setTextAlignment(Alignment.Center)
                    .setBackground(GTUITextures.BACKGROUND_TEXT_FIELD)
                    .setSize(18*4, 18)
                    .setPos(50, 3));
			builder.widget(SlotWidget.phantom(new IItemHandlerModifiable() {
				
				@Override
				public ItemStack insertItem(int var1, ItemStack var2, boolean var3) {
					if(var2!=null&&var2.getItem() instanceof ItemFluidPacket){
						FluidStack x = ItemFluidPacket.getFluidStack(var2);
						if(x!=null)
							var2=ItemFluidDrop.newStack(x);
					}
					if(GTUtility.getFluidFromDisplayStack(var2) != null){
						FluidStack x = GTUtility.getFluidFromDisplayStack(var2);
						x.amount=1;
						var2=ItemFluidDrop.newStack(x);
					}
					
					if(getCurrentItem().getTagCompound()==null){getCurrentItem().setTagCompound(new NBTTagCompound());};
				getCurrentItem().getTagCompound().setTag("out", one(var2.copy()).writeToNBT(new NBTTagCompound()));
					
					return null;
				}
				
				@Override
				public ItemStack getStackInSlot(int var1) {
					if(getCurrentItem().getTagCompound()!=null){
						
						
						return ItemStack.loadItemStackFromNBT(getCurrentItem().getTagCompound().getCompoundTag("out"));
					}
					return null;
				}
				
				@Override
				public int getSlots() {
					
					return 1;
				}
				
				@Override
				public int getSlotLimit(int var1) {
					
					return Integer.MAX_VALUE;
				}
				
				@Override
				public ItemStack extractItem(int var1, int var2, boolean var3) {
					if(getCurrentItem().getTagCompound()==null){
					getCurrentItem().setTagCompound(new NBTTagCompound());
						
					}
					return null;
				}
				
				@Override
				public void setStackInSlot(int var1, ItemStack var2) {
					if(var2!=null&&var2.getItem() instanceof ItemFluidPacket){
						FluidStack x = ItemFluidPacket.getFluidStack(var2);
						if(x!=null)
						var2=ItemFluidDrop.newStack(x);
					}
					if(GTUtility.getFluidFromDisplayStack(var2) != null){
						FluidStack x = GTUtility.getFluidFromDisplayStack(var2);
						x.amount=1;
						var2=ItemFluidDrop.newStack(x);
					}
					
					
					
					
					if(getCurrentItem().getTagCompound()==null){getCurrentItem().setTagCompound(new NBTTagCompound());};
					
					if(var2==null)
						getCurrentItem().getTagCompound().removeTag("out");
						else
					getCurrentItem().getTagCompound().setTag("out", one(var2.copy()).writeToNBT(new NBTTagCompound()));
						
					
				}
			}, 0).setPos(3, 3));
		}
public ItemStack one(ItemStack is){is.stackSize=1;return is;}
		public UIBuildContext getUIBuildContext() {
			return uiBuildContext;
		}

		/*
		 * public boolean isCoverValid() { return !getUIBuildContext().getTile()
		 * .isDead() && getUIBuildContext().getTile()
		 * .getCoverBehaviorAtSideNew(getUIBuildContext().getCoverSide()) !=
		 * GregTechAPI.sNoBehavior; }
		 */

		protected void addTitleToUI(ModularWindow.Builder builder) {
			/*
			 * ItemStack coverItem =
			 * GTUtility.intToStack(getUIBuildContext().getCoverID()); if
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
	@Override
	public void addCheckedInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
		p_77624_3_.add(StatCollector.translateToLocal("item.emitterpattern.name.tooltip.0"));
		p_77624_3_.add(StatCollector.translateToLocal("item.emitterpattern.name.tooltip.1"));
		p_77624_3_.add(StatCollector.translateToLocal("item.emitterpattern.name.tooltip.2"));
		p_77624_3_.add(StatCollector.translateToLocal("item.emitterpattern.name.tooltip.3"));
	}
}
