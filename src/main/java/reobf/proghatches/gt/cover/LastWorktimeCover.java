package reobf.proghatches.gt.cover;



import java.util.ArrayList;

import com.gtnewhorizons.modularui.api.screen.ModularWindow;

import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.interfaces.tileentity.IMachineProgress;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.ISerializableObject;
import gregtech.common.gui.modularui.widget.CoverDataControllerWidget;
import gregtech.common.gui.modularui.widget.CoverDataFollower_CycleButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

public class LastWorktimeCover extends GT_CoverBehavior {

  
    public LastWorktimeCover() {
        this(null);
    }

    public LastWorktimeCover(ITexture coverTexture) {
        super(coverTexture);
    }

    @Override
    public boolean isRedstoneSensitive(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity,
        long aTimer) {
        return false;
    }

    @Override
    public int doCoverThings(ForgeDirection side, byte aInputRedstone, int aCoverID, int aCoverVariable,
        ICoverable aTileEntity, long aTimer) {
       
        if ((aTileEntity instanceof IMachineProgress)) {
        	
        boolean on=	(((IMachineProgress) aTileEntity).hasThingsToDo());
        	int bits=aCoverVariable&0b111;
            if(on){
        		aCoverVariable=bits|(101<<3);
        	}else{
        		isEnabled(0,aCoverVariable);
        		int val=aCoverVariable;//&(~0b111);
        		val=val>>3;
        		if(val>0){
        			if(isEnabled(0,aCoverVariable)){val--;}else{val=0;}
        		 aCoverVariable=bits|(val<<3);
        		}
        	
        	}
         
          }
      boolean on=(aCoverVariable>>3)>0;  
     // boolean settrue=false;
      
      if(on==false&&isEnabled(1,aCoverVariable)){
    	on=true;
       if ((aTileEntity instanceof IGregTechTileEntity)) {
        	IGregTechTileEntity mt=(IGregTechTileEntity) aTileEntity;
        	IMetaTileEntity meta = mt.getMetaTileEntity();
        	if(meta!=null&&meta instanceof GT_MetaTileEntity_MultiBlockBase){
        		GT_MetaTileEntity_MultiBlockBase multi=(GT_MetaTileEntity_MultiBlockBase) meta;
        		
        		if(multi.getStoredFluids().isEmpty()){
        		ArrayList<ItemStack> in = multi.getStoredInputs();
       			in.removeIf(s->s.stackSize<=0);
        		if(in.isEmpty()){
        			if(multi.mDualInputHatches.stream().map(s->s.getFirstNonEmptyInventory().orElse(null))
							.count()==0)
							on = false;
        			}
        		}
        		
        	}
        	
        	
        }
      }else{
    	//on=false;
    	  
      }
        
       aTileEntity.setOutputRedstoneSignal(side, (byte) (
    		   (on)
    		   ?15:0));
       
        return aCoverVariable;
    }
/*
    @Override
    public int onCoverScrewdriverclick(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity,
        EntityPlayer aPlayer, float aX, float aY, float aZ) {
        aCoverVariable = (aCoverVariable + (aPlayer.isSneaking() ? -1 : 1)) % 4;
        if (aCoverVariable < 0) {
            aCoverVariable = 3;
        }
        switch (aCoverVariable) {
            case 0 -> GT_Utility.sendChatToPlayer(aPlayer, GT_Utility.trans("018", "Normal"));
            // Progress scaled
            case 1 -> GT_Utility.sendChatToPlayer(aPlayer, GT_Utility.trans("019", "Inverted"));
            // ^ inverted
            case 2 -> GT_Utility.sendChatToPlayer(aPlayer, GT_Utility.trans("020", "Ready to work"));
            // Not Running
            case 3 -> GT_Utility.sendChatToPlayer(aPlayer, GT_Utility.trans("021", "Not ready to work"));
            // Running
        }
        return aCoverVariable;
    }*/

    @Override
    public boolean letsEnergyIn(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsEnergyOut(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsFluidIn(ForgeDirection side, int aCoverID, int aCoverVariable, Fluid aFluid,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsFluidOut(ForgeDirection side, int aCoverID, int aCoverVariable, Fluid aFluid,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsItemsIn(ForgeDirection side, int aCoverID, int aCoverVariable, int aSlot,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean letsItemsOut(ForgeDirection side, int aCoverID, int aCoverVariable, int aSlot,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public boolean manipulatesSidedRedstoneOutput(ForgeDirection side, int aCoverID, int aCoverVariable,
        ICoverable aTileEntity) {
        return true;
    }

    @Override
    public int getTickRate(ForgeDirection side, int aCoverID, int aCoverVariable, ICoverable aTileEntity) {
        return 1;
    }

    // GUI stuff

    @Override
    public boolean hasCoverGUI() {
        return true;
    }

    @Override
    public boolean useModularUI() {
        return true;
    }

    @Override
    public ModularWindow createWindow(GT_CoverUIBuildContext buildContext) {
        return new DoesWorkUIFactory(buildContext).createWindow();
    }

    private class DoesWorkUIFactory extends UIFactory {

        private static final int startX = 10;
        private static final int startY = 25;
        private static final int spaceX = 18;
        private static final int spaceY = 18;

        public DoesWorkUIFactory(GT_CoverUIBuildContext buildContext) {
            super(buildContext);
        }

        @Override
        protected void addUIWidgets(ModularWindow.Builder builder) {
            builder
                .widget(
                    new CoverDataControllerWidget.CoverDataIndexedControllerWidget_CycleButtons<>(
                        this::getCoverData,
                        this::setCoverData,
                        LastWorktimeCover.this,
                        (id, coverData) -> isEnabled(id, convert(coverData))?1:0,
                        (id, coverData) -> new ISerializableObject.LegacyCoverData(
                            getNewCoverVariable(id, convert(coverData))))
                                .addCycleButton(
                                    0,
                                   new  CoverDataFollower_CycleButtonWidget<>()
                                 
                                   ,
                                    widget -> widget.setLength(2)
                                    .addTooltip(0, StatCollector.translateToLocal("item.proghatch.cover.dedicated.3.tooltips.5sec.false"))
                                    .addTooltip(1, StatCollector.translateToLocal("item.proghatch.cover.dedicated.3.tooltips.5sec.true"))
                                    .setStaticTexture(GT_UITextures.OVERLAY_BUTTON_PROGRESS)
                                        .setPos(spaceX * 0, spaceY * 0)
                                        
                                		)
                                	.addCycleButton(
                                    1,
                                    new  CoverDataFollower_CycleButtonWidget<>(),
                                    widget -> widget.setLength(2)
                                    .addTooltip(0, StatCollector.translateToLocal("item.proghatch.cover.dedicated.3.tooltips.inputdetect.false"))
                                    .addTooltip(1, StatCollector.translateToLocal("item.proghatch.cover.dedicated.3.tooltips.inputdetect.true"))
                                    .setStaticTexture(GT_UITextures.OVERLAY_BUTTON_CHECKMARK)
                                        .setPos(spaceX * 1, spaceY * 0))
                               /* .addToggleButton(
                                    2,
                                    CoverDataFollower_ToggleButtonWidget.ofRedstone(),
                                    widget -> widget.setPos(spaceX * 0, spaceY * 1))*/
                                .setPos(startX, startY))
              /*  .widget(
                    TextWidget
                        .dynamicString(
                            () -> ((convert(getCoverData()) & 0x2) > 0) ? "5s":"0s")
                        .setSynced(false)
                        .setDefaultColor(COLOR_TEXT_GRAY.get())
                        .setPos(startX + spaceX * 3, 4 + startY + spaceY * 0))
                .widget(
                    TextWidget
                        .dynamicString(
                            () -> ((convert(getCoverData()) & 0x1) > 0) ? "empty":"")
                        .setSynced(false)
                        .setDefaultColor(COLOR_TEXT_GRAY.get())
                        .setPos(startX + spaceX * 3, 4 + startY + spaceY * 1))*/;
        }

		
        
    }private int getNewCoverVariable(int id, int data) {
			int mask=(1<<id);
			int stencil=~(1<<id);
			if((data&mask)>0){
				return data&stencil;
			}else{
				return data|mask;
			}
		}

		private boolean isEnabled(int id, int data) {
			
			return (data&(1<<id))>0;
		}
@Override
public boolean allowsTickRateAddition() {
	
	return false;
}
}
