package reobf.proghatches.eucrafting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.parts.base.FCPart;
import com.google.common.collect.ImmutableList;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.TextWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.BaseTextFieldWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.TextFieldWidget;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Settings;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingProviderHelper;
import appeng.api.networking.events.MENetworkCraftingPatternChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.BusSupport;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.AEBasePart;
import appeng.parts.PartBasicState;
import appeng.parts.p2p.IPartGT5Power;
import appeng.util.item.AEItemStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.enums.GT_Values;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.tileentity.IEnergyConnected;
import gregtech.common.gui.modularui.widget.CoverCycleButtonWidget;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.eucrafting.IEUSource.ISource;
import reobf.proghatches.gt.metatileentity.ProgrammingCircuitProvider.CircuitProviderPatternDetial;
import reobf.proghatches.item.ItemProgrammingCircuit;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.util.ProghatchesUtil;

public class PartEUSource extends AEBasePart implements IGuiProvidingPart,
ICraftingProvider,IGridTickable,IInstantCompletable
,IPartGT5Power
, ISource{
	@Override
	public void markDirty() {
		this.getTile().markDirty();
		
	} 
	boolean onoff=true;
	long voltage;
	long amp;
	long consumed;
	long ampInjectedthisTick;
	boolean recycle=true;
	public PartEUSource(ItemStack is) {
		super(is);
	
	} @Override
    public final void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
    }
	  
	   
	   @Override
	    @SideOnly(Side.CLIENT)
	    public void renderInventory(final IPartRenderHelper rh, final RenderBlocks renderer) {
	        rh.setBounds(2, 2, 14, 14, 14, 16);

	        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
	        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

	        rh.setTexture(
	                sideTexture,
	                sideTexture,
	                backTexture,
	                FCPartsTexture.PartTerminalBroad.getIcon(),
	                sideTexture,
	                sideTexture);
	        rh.renderInventoryBox(renderer);

	       // rh.setInvColor(this.getColor().whiteVariant);
	       // rh.renderInventoryFace(this.getFrontBright(), ForgeDirection.SOUTH, renderer);

	        rh.setInvColor(this.getColor().mediumVariant);
	        rh.renderInventoryFace(this.getFrontBright(), ForgeDirection.SOUTH, renderer);

	       // rh.setInvColor(this.getColor().blackVariant);
	       // rh.renderInventoryFace(this.getFrontColored(), ForgeDirection.SOUTH, renderer);

	        rh.setBounds(4, 4, 13, 12, 12, 14);
	        rh.renderInventoryBox(renderer);
	    }
@Override
public boolean onPartActivate(EntityPlayer player, Vec3 pos) {

	//System.out.println(this.getTile().getWorldObj().isRemote);
	if(player.isSneaking())return false;
	TileEntity t=this.getTile();
	//System.out.println(getSide());
	EUUtil.open(player, player.getEntityWorld(),t.xCoord,t.yCoord,t.zCoord, getSide());
	return true;
}
	    @Override
	    @SideOnly(Side.CLIENT)
	    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh,
	            final RenderBlocks renderer) {
	        this.setRenderCache(rh.useSimplifiedRendering(x, y, z, this, this.getRenderCache()));

	        final IIcon sideTexture = CableBusTextures.PartMonitorSides.getIcon();
	        final IIcon backTexture = CableBusTextures.PartMonitorBack.getIcon();

	        rh.setTexture(
	                sideTexture,
	                sideTexture,
	                backTexture,
	                FCPartsTexture.PartTerminalBroad.getIcon(),
	                sideTexture,
	                sideTexture);

	        rh.setBounds(2, 2, 14, 14, 14, 16);
	        rh.renderBlock(x, y, z, renderer);

	        if (this.getLightLevel() > 0) {
	            final int l = 13;
	            Tessellator.instance.setBrightness(l << 20 | l << 4);
	        }

	        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = this
	                .getSpin();

	      //  Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
	      //  rh.renderFace(x, y, z, this.getFrontBright(), ForgeDirection.SOUTH, renderer);

	        Tessellator.instance.setColorOpaque_I(this.getColor().mediumVariant);
	        rh.renderFace(x, y, z, this.getFrontBright(), ForgeDirection.SOUTH, renderer);

	     //   Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
	     //  rh.renderFace(x, y, z, this.getFrontColored(), ForgeDirection.SOUTH, renderer);

	        renderer.uvRotateBottom = renderer.uvRotateEast = renderer.uvRotateNorth = renderer.uvRotateSouth = renderer.uvRotateTop = renderer.uvRotateWest = 0;

	        final IIcon sideStatusTexture = CableBusTextures.PartMonitorSidesStatus.getIcon();

	        rh.setTexture(
	                sideStatusTexture,
	                sideStatusTexture,
	                backTexture,
	                this.getItemStack().getIconIndex(),
	                sideStatusTexture,
	                sideStatusTexture);

	        rh.setBounds(4, 4, 13, 12, 12, 14);
	        rh.renderBlock(x, y, z, renderer);

	        final boolean hasChan =true;/* (this.getClientFlags() & (FCPart.POWERED_FLAG | FCPart.CHANNEL_FLAG))
	                == (FCPart.POWERED_FLAG | FCPart.CHANNEL_FLAG);*/
	        final boolean hasPower =true;// (this.getClientFlags() & FCPart.POWERED_FLAG) == FCPart.POWERED_FLAG;

	        if (hasChan) {
	            final int l = 14;
	            Tessellator.instance.setBrightness(l << 20 | l << 4);
	            Tessellator.instance.setColorOpaque_I(this.getColor().blackVariant);
	        } else if (hasPower) {
	            final int l = 9;
	            Tessellator.instance.setBrightness(l << 20 | l << 4);
	            Tessellator.instance.setColorOpaque_I(this.getColor().whiteVariant);
	        } else {
	            Tessellator.instance.setBrightness(0);
	            Tessellator.instance.setColorOpaque_I(0x000000);
	        }

	        final IIcon sideStatusLightTexture = CableBusTextures.PartMonitorSidesStatusLights.getIcon();

	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.EAST, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.WEST, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.UP, renderer);
	        rh.renderFace(x, y, z, sideStatusLightTexture, ForgeDirection.DOWN, renderer);
	    }

		private IIcon getFrontColored() {
			
			return a;
		}
		static IIcon a;
		private IIcon getFrontBright() {
			
			return a;
		}

		private int getSpin() {
			// TODO Auto-generated method stub
			return 0;
		}

		public static void registerIcons(IIconRegister _iconRegister) {
		
			a=_iconRegister.registerIcon("proghatches:eu_interface");
		}
		
	
		public ModularWindow createWindow(UIBuildContext buildContext) {
		TileEntity t=getTile();
			MyMod.LOG.debug("Activated:"+getSide()
			+","+t.xCoord
			+","+t.yCoord
			+","+t.zCoord
					);
		
		
		  ModularWindow.Builder builder = ModularWindow.builder(176,107);
          builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
         
          builder.widget(
                  ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                      .setGetter(() -> recycle ? 1 : 0)
                      .setSetter(s -> recycle = s == 1)
                      .setLength(2)
                      .setTextureGetter(i -> {
                          if (i == 1) return GT_UITextures.OVERLAY_BUTTON_CHECKMARK;
                          return GT_UITextures.OVERLAY_BUTTON_CROSS;
                      })

                      .addTooltip(0, LangManager.translateToLocal("proghatches.part.recycle.false"))
                      .addTooltip(1, LangManager.translateToLocal("proghatches.part.recycle.true"))
                      .setPos(8, 80)

              );
          builder.widget(
                  ((CycleButtonWidget) new CoverCycleButtonWidget().setSynced(false, true))
                      .setGetter(() -> onoff ? 1 : 0)
                      .setSetter(s -> {onoff = s == 1;postEvent();})
                      .setLength(2)
                      .setTextureGetter(i -> {
                          if (i == 1) return GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_ON;
                          return GT_UITextures.OVERLAY_BUTTON_POWER_SWITCH_OFF;
                      })

                      .addTooltip(0, LangManager.translateToLocal("proghatches.part.onoff.false"))
                      .addTooltip(1, LangManager.translateToLocal("proghatches.part.onoff.true"))
                      .setPos(8+16, 80)

              );
          builder.widget(
                  TextWidget.dynamicString(() -> consumed+"/"+amp+"A")
                      .setSynced(true)
                      .setPos(58,80)
                      .addTooltips(
                    		  
                    		 ImmutableList.of(
                    		  LangManager.translateToLocal("proghatches.part.hint.0"),
                    		  LangManager.translateToLocal("proghatches.part.hint.1")
                    		  )
                    		  
                    		  )
        		  )
          
          
          ;
        		  
        		  
        		  
        		
          BiFunction<Supplier<Long>,Consumer<Long>,Widget> gen=(a,b)->{

        	  TextFieldWidget  o=Optional.of(new TextFieldWidget())
                      .filter(s -> {
                          s.setText((a.get() + ""));
                          return true;
                      })
                      .get();
                      o.setValidator(val -> {
                          if (val == null) {
                              val = "";
                          }
                          return val;
                      })
                      .setSynced(true, true)
                      .setGetter(() -> { return a.get() + ""; })
                      .setSetter(s -> {
                          try {
                        	  b.accept( Math.max(Long.valueOf(s),0l));
                          } catch (Exception e) {
                        	  b.accept( 0l);
                          }
                          onChange();
                         o.notifyTooltipChange();
                      })
                      .setPattern(BaseTextFieldWidget.NATURAL_NUMS)
                      .setMaxLength(50)
                      .setScrollBar()
                      .setFocusOnGuiOpen(false)
                      .setTextColor(Color.WHITE.dark(1))

                      .setBackground(GT_UITextures.BACKGROUND_TEXT_FIELD.withOffset(-1, -1, 2, 2))
                      .setPos(16,16)
                      .setSize(16*8,16);
          
          return o;
          };
          builder.widget(gen.apply(()->this.voltage,a->{this.voltage=a;}).setPos(16,16).dynamicTooltip(()->{
         String a=GT_Values.VN[0],b;
         long v=8;
         for(int i=0;i<GT_Values.V.length-1;i++){
        	 
        	 if(voltage>GT_Values.V[i]){
        		 a=GT_Values.VN[i+1];v=GT_Values.V[i+1];
        	 }
         }
         b=Math.floor((100*(float)voltage/v))+"%";
         
         
         return  ImmutableList.of(
        StatCollector.translateToLocal("proghatches.part.tooltip.volt.0"),
          StatCollector.translateToLocalFormatted("proghatches.part.tooltip.volt.1",a,b)
          );}
        		  
        		  ));
          builder.widget(gen.apply(()->this.amp,a->this.amp=a).setPos(16,16+16+2).addTooltip(StatCollector.translateToLocal("proghatches.part.tooltip.amp")));
          builder.widget(new TextWidget("V:").setPos(8,16));
          builder.widget(new TextWidget("A:").setPos(8,16+16+2));
              //builder.bindPlayerInventory(buildContext.getPlayer());
         
          return builder.build();
			
		}
		
		
		@Override
		public long injectEnergyUnits( long aVoltage, long aAmperage) {
			//if(getVoltage()==0)return 0;
			if(consumed<=ampInjectedthisTick)return 0;
			try {
				long actual=Math.min(consumed-ampInjectedthisTick,aAmperage);
				
				 long consumed=((IEUSource) getProxy().getGrid().getCache(IEUSource.class))
				.inject(this,actual, aVoltage );
				 ampInjectedthisTick+=consumed;
				 return consumed;
			} catch (GridAccessException e) {
			
			
			}
			
			return 0;
		}
		@Override
		public boolean inputEnergy() {
			// TODO Auto-generated method stub
			return true;
		}
		@Override
		public boolean outputsEnergy() {
			// TODO Auto-generated method stub
			return false;
		};
		
		@Override
		public long getVoltage() {
			
			return voltage;
		}
		
		private void returnItems() {
			
			
			
			toReturn.removeIf(s -> {

				try {
					return Optional.ofNullable(

							this.getProxy().getStorage().getItemInventory().injectItems(AEItemStack.create(s)

									, Actionable.MODULATE, new MachineSource(this))

					).map(IAEItemStack::getStackSize).orElse(0L) == 0L

					;

				} catch (GridAccessException e) {
					return false;
				}
			});

		}
		@Override
		public void readFromNBT(NBTTagCompound data) {
			voltage=data.getLong("voltage");
			amp=data.getLong("amp");
			consumed=data.getLong("consumed");
			toReturn.clear();
	        int[] count = new int[1];
	        NBTTagCompound c;
	        while ((c = (NBTTagCompound) data.getTag("toReturn" + (count[0]++))) != null) {
	            toReturn.add(ItemStack.loadItemStackFromNBT(c));
	        }
			super.readFromNBT(data);
		}
		@Override
		public void writeToNBT(NBTTagCompound data) {
			data.setLong("voltage",voltage);
			data.setLong("amp",amp);
			data.setLong("consumed",consumed);
			  int[] count = new int[1];
		        toReturn.forEach(s -> data.setTag("toReturn" + (count[0]++), s.writeToNBT(new NBTTagCompound())));
		      
			super.writeToNBT(data);
		}
		
		public void onChange(){
			
			postEvent();
			
		}
		 private boolean postEvent() {
		        try {
		            this.getProxy()
		                .getGrid()
		                .postEvent(new MENetworkCraftingPatternChange(this, getGridNode(ForgeDirection.UNKNOWN)));
		            return true;
		        } catch (GridAccessException ignored) {
		            return false;
		        }
		    }
		

		    @Override
		    public TickingRequest getTickingRequest(final IGridNode node) {
		        return new TickingRequest(1,10, false, false);
		    }

		    

		    @Override
		    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
		       returnItems();
		    	if(recycle)
		       try {
					IAEItemStack ret = getProxy().getStorage().getItemInventory().extractItems(
							AEItemStack.create(buildToken(consumed>Integer.MAX_VALUE?Integer.MAX_VALUE:(int) consumed))
							
							, Actionable.MODULATE,new MachineSource(this));
					
					if(ret!=null){
						
						this.consumed-=ret.getStackSize();
						return TickRateModulation.FASTER;
					}
					
					
					
				} catch (GridAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		       
		       return TickRateModulation.SLOWER;
		    }

		ArrayList<ItemStack> toReturn=new ArrayList<>();
		@Override
		public boolean pushPattern(ICraftingPatternDetails patternDetails, InventoryCrafting table) {
			if(consumed>=amp){return false;}
			consumed++;
			
			returnItems();
		        toReturn.add(patternDetails.getOutput(
		                table,this.getTile().getWorldObj()
		               ));

		        return true;
		
		}
		@Override
		public boolean isBusy() {
			
			return (consumed>=amp);
		}
		
		public ItemStack buildToken(int size){
			return Optional.of(new ItemStack(MyMod.eu_token, size, 0)).map(s -> {
				s.stackTagCompound = new NBTTagCompound();
			
				s.stackTagCompound.setLong("voltage", voltage);
				
				
				return s;
			}).get();
			
		}
		@Override
		public void provideCrafting(ICraftingProviderHelper craftingTracker) {
			if(onoff==false)return;
			ItemStack is=buildToken(1);
			craftingTracker
             .addCraftingOption(this, new CircuitProviderPatternDetial(
            		 is
            		 ));

			
		}
		@Override
		public void complete() {
			returnItems();
			
		}
		@Override
		public void reset() {
			ampInjectedthisTick=0;
			
		}
		
		

}
