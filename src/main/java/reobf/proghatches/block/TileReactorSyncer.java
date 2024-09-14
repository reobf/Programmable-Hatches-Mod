package reobf.proghatches.block;

import java.util.Arrays;
import java.util.function.Supplier;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.AdaptableUITexture;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.UITexture;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.FakeSyncWidget;
import com.gtnewhorizons.modularui.common.widget.FluidSlotWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;
import com.gtnewhorizons.modularui.common.widget.SlotWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.gui.modularui.GUITextureSet;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.block.reactor.tileentity.TileEntityReactorAccessHatch;
import ic2.core.block.reactor.tileentity.TileEntityReactorChamberElectric;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.block.TileIOHub.UIFactory;
import reobf.proghatches.gt.metatileentity.util.MappingItemHandler;
import reobf.proghatches.main.MyMod;

public class TileReactorSyncer extends TileEntity implements ITileWithModularUI{

	
	private boolean isDead;

	@Override
	public void validate() {
	super.validate();
	MyMod.callbacks.put(this,this::pretick);
	}
	
	
	public void pretick(){
		if((!isDead)&&(!isInvalid())){
			TileEntityNuclearReactorElectric reactor=findTarget();
			if(reactor!=null) 
				tick=reactor.updateTicker%20;else tick=-1;
			
			int new_power=tick!=-1?values[tick]:0;
			if(power!=new_power){
				
			
			worldObj.scheduleBlockUpdate(xCoord, yCoord, zCoord, getBlockType()
					, 0);
			//schedule it, update it in World#tick, just in case something magic happens...
			}
			
			power=new_power;
		}
	}
	
	
	public int power(){
	if(power<0){return 0;}
		return power;
	}
	public int tick;
	public int power=-1;//ic2 reactor reset its tick value everytime world loads, so it's no point saving the value
	public TileEntityNuclearReactorElectric findTarget(){
		
		ForgeDirection dir=ForgeDirection.values()[this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
		TileEntity te=this.worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
		
		if(te instanceof TileEntityNuclearReactorElectric)return (TileEntityNuclearReactorElectric) te;
		if(te instanceof TileEntityReactorChamberElectric){
			return ((TileEntityReactorChamberElectric) te).getReactor();}
		if(te instanceof TileEntityReactorAccessHatch){
			Object possible= ((TileEntityReactorAccessHatch) te).getReactor();
			return possible instanceof TileEntityNuclearReactorElectric?(TileEntityNuclearReactorElectric)possible:null;
		}
		return null;
	}
	
	@Override
    public void onChunkUnload() {
      
        super.onChunkUnload();
        isDead = true;
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        isDead = false;
    }


	@Override
	public ModularWindow createWindow(UIBuildContext buildContext) {
		return new UIFactory(buildContext).createWindow();
		
	}protected class UIFactory {

		private final UIBuildContext uiBuildContext;

		public UIFactory(UIBuildContext buildContext) {
			this.uiBuildContext = buildContext;
		}

		public ModularWindow createWindow() {
			ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
			builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
			// builder.setGuiTint(getUIBuildContext().getGuiColorization());
			if (doesBindPlayerInventory()) 
				builder.bindPlayerInventory(getUIBuildContext().getPlayer());
		
			addTitleToUI(builder);
			addUIWidgets(builder);
		
			return builder.build();
		}

		/**
		 * Override this to add widgets for your UI.
		 */

		// IItemHandlerModifiable fakeInv=new ItemHandlerModifiable();

		/**
		 * @param builderx
		 */
		/**
		 * @param builderx
		 */
		protected void addUIWidgets(ModularWindow.Builder builderx) {
			Scrollable builder=new Scrollable().setHorizontalScroll();
			for(int i=0;i<20;i++){
				
				 
				
				final int fi=i;
			builder.widget(new TextFieldWidget()
					.setGetterInt(()->values[fi])
					.setSetterInt(s->{values[fi]=
							Math.max(Math.min(s, 15),0);markDirty();
					}
					
							)
					
					.setSynced(true,true)
					.setPos(20*fi, 0)
					.setSize(20,20)
					.setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2)
					));
			
			builder.widget(new ButtonWidget()
					.setOnClick((a,b)->{values[fi]=15;markDirty();})
					.setBackground(GT_UITextures.BUTTON_STANDARD,GT_UITextures.OVERLAY_BUTTON_ARROW_GREEN_UP)
					.setPos(20*fi, 40)
					.setSize(20,20)
					);
			builder.widget(new ButtonWidget()
					.setOnClick((a,b)->{values[fi]=0;markDirty();})
					.setBackground(GT_UITextures.BUTTON_STANDARD,GT_UITextures.OVERLAY_BUTTON_ARROW_GREEN_DOWN)
					.setPos(20*fi, 60)
					.setSize(20,20)
					);
			builder.widget(new DrawableWidget().setDrawable(GT_UITextures.PICTURE_RADIATION_WARNING)
					.setPos(0, 20)
					.setSize(20,20)
					);
			
			builder.widget(new TextWidget(fi+"").setPos(20*fi+3, 60+20));
			
			
			
			
			}
			
			builder.setPos(3+20+20, 3)
			.setSize(getGUIWidth()-6-40,80+10);
			builderx
			.widget(builder);
			builderx.widget(TextWidget.localised("tile.reactor_syncer.info.rs").setPos(3, 3+10));
			builderx.widget(TextWidget.localised("tile.reactor_syncer.info.update").setPos(3, 3+10+20));
			
			builderx.widget(TextWidget.localised("tile.reactor_syncer.info.max").setPos(3, 3+10+40));
			builderx.widget(TextWidget.localised("tile.reactor_syncer.info.min").setPos(3, 3+10+60));
			
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
			return 107 + 18 * 3 + 18;
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
	int[] values=new int[20];
	
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		for(int i=0;i<20;i++){
			compound.setInteger("##"+i, values[i]);
		}
		super.writeToNBT(compound);
	}
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		for(int i=0;i<20;i++){
			values[i]=compound.getInteger("##"+i );
		}
		super.readFromNBT(compound);
	}
}
