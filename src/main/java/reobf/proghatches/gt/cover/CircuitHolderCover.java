package reobf.proghatches.gt.cover;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.lang.LangManager;
import reobf.proghatches.util.ProghatchesUtil;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteArrayDataInput;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.shapes.Rectangle;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.math.Pos2d;
import com.gtnewhorizons.modularui.api.math.Size;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.ModularWindow.Builder;
import com.gtnewhorizons.modularui.api.widget.IDragAndDropHandler;
import com.gtnewhorizons.modularui.api.widget.ISyncedWidget;
import com.gtnewhorizons.modularui.api.widget.IWidgetParent;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.internal.network.NetworkUtils;
import com.gtnewhorizons.modularui.common.widget.ButtonWidget;
import com.gtnewhorizons.modularui.common.widget.ChangeableWidget;
import com.gtnewhorizons.modularui.common.widget.DrawableWidget;
import com.gtnewhorizons.modularui.common.widget.Scrollable;
import com.gtnewhorizons.modularui.common.widget.TextWidget;

import gregtech.api.gui.modularui.GT_CoverUIBuildContext;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.GT_CoverBehavior;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.ISerializableObject;

import gregtech.common.gui.modularui.widget.CoverDataControllerWidget;
import gregtech.common.gui.modularui.widget.CoverDataFollower_CycleButtonWidget;
import io.netty.buffer.ByteBuf;

public class CircuitHolderCover extends GT_CoverBehaviorBase<CircuitHolderCover.Data>{
	public CircuitHolderCover() {
		super(CircuitHolderCover.Data.class);
		
	}


	public static class Data implements ISerializableObject{
		NBTTagCompound tag;
		public Data(NBTTagCompound tag2) {
			tag=tag2;
		}

		@Override
		public ISerializableObject copy() {
			
			return new Data(tag);
		}

		@Override
		public NBTBase saveDataToNBT() {
			
			return tag.copy();
		}

		@Override
		public void writeToByteBuf(ByteBuf aBuf) {
			try {
				byte[] b = CompressedStreamTools.compress(tag);
				aBuf.writeInt(b.length);
				aBuf.writeBytes(b);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}

		@Override
		public void loadDataFromNBT(NBTBase aNBT) {
			tag=((NBTTagCompound) aNBT);
			
		}

		@Override
		public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
			 
			byte[] b =new byte[aBuf.readInt()];
			aBuf.readFully(b);
			NBTTagCompound xtag=new NBTTagCompound();
			try {
				xtag = CompressedStreamTools.func_152457_a(b, new NBTSizeTracker(Long.MAX_VALUE));
			} catch (IOException e) {
			e.printStackTrace();
			}
			
			return new Data(xtag);
		}}
	
	
   @Override
public boolean useModularUI() {
	// TODO Auto-generated method stub
	return true;
}
   @Override
	public ModularWindow createWindow(GT_CoverUIBuildContext buildContext) {
		
		return new CircuitHolderUIFactory(buildContext).createWindow();
	}


	private class CircuitHolderUIFactory extends UIFactory {

       @Override
    protected void addTitleToUI(Builder builder) {
    	   ItemStack coverItem = GT_Utility.intToStack(getUIBuildContext().getCoverID());
           if (coverItem != null) {
               builder.widget(
                   new ItemDrawable(coverItem).asWidget()
                       .setPos(5, 5)
                       .setSize(16, 16))
                   /*.widget(
                       new TextWidget(coverItem.getDisplayName()).setDefaultColor(COLOR_TITLE.get())
                           .setPos(25, 9))*/;
           }
    }
		@Override
		protected int getGUIWidth() {
		return 176/2;
		}
        public CircuitHolderUIFactory(GT_CoverUIBuildContext buildContext) {
            super(buildContext);
        
        }
        @Override
        protected void addUIWidgets(ModularWindow.Builder builder) {
      
    		builder.setPos((size, window) -> 
    		//window.
    		//Alignment.CenterLeft.getAlignedPos(size, new Size(getGUIWidth(), getGUIHeight()))
    		new Pos2d(
    				size.width/2-
    				window.getSize().width/2-getGUIWidth()-18,
    				size.height/2-window.getSize().height/2+getGUIHeight()/2
    				
    				
    				)
    		
    		
    				);
        	builder.setDraggable(true);
        		builder.widget(new DrawableWidget().setDrawable(new Rectangle().setColor(
        			Color.LIGHT_BLUE.bright(2)
        			)).setPos(3, 3+20).setSize(getGUIWidth()-6, getGUIHeight()-6-20)
        			
        			);
        		
        	builder.widget((chw=new ChangeableWidget(this::createSc){

				@Override
				public ItemStack[] getCircuits() {
					return ProghatchesUtil.deseri(getCoverData().tag,"circuit");
				}}).setPos(3, 3));
        	InventoryPlayer inv = getUIBuildContext().getPlayer().inventory;
        	ItemStack coveris= getUIBuildContext().getTile().getCoverItemAtSide(getUIBuildContext().getCoverSide());
        		
        	builder.widget( new ButtonWidget().setOnClick((clickData, widget) -> {
    			if(NetworkUtils.isClient()==false){
        		ItemStack[] is = ProghatchesUtil.deseri(getCoverData().tag,"circuit"); 
        		if(damageToLimit(coveris.getItemDamage())<=is.length){return;}
        		
        		if(inv.getItemStack()!=null){
    				//System.out.println(inv.getItemStack());
    				
    				ArrayList<ItemStack> ar=new ArrayList<>(is.length+1);
    				for(ItemStack iis:is){ar.add(iis);}
    				ar.add(inv.getItemStack());
    				 ItemStack[] x=ar.toArray(new ItemStack[is.length+1]);
    				 
    				 getCoverData().tag=ProghatchesUtil.ser(new NBTTagCompound() , x, "circuit");
    				 getUIBuildContext().getTile().markDirty();
    			}
    			
    			
    				chw.notifyChangeServer();
    			}
    			
    			
    			
    		}).setPlayClickSound(true).setBackground(GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_PLUS_LARGE)
    				.addTooltips(ImmutableList
    						.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.holder.max", "" +damageToLimit(coveris.getItemDamage()) )))
    				.setSize(16, 16).setPos(3+32+16,3));

        }
        ChangeableWidget chw;
        public Scrollable  createSc(ItemStack is[]){
        
        	Scrollable sc=new Scrollable();
        	
        	sc.setVerticalScroll();
        	
        	
        	//IDragAndDropHandler
        
        	/*sc.widget(
        	new DrawableWidget().setDrawable(new Rectangle().setColor(
        			Color.LIGHT_BLUE.bright(2)
        			)).setPos(0, 0).setSize(getGUIWidth()-6, getGUIHeight()-6-20));*/
        	int perrow=(getGUIWidth()-6)/16;
        	if(perrow<=0)perrow=1;
        	sc.setSize(getGUIWidth()-6, getGUIHeight()-6-20);
        	sc.setPos(0, 0+20);
        	for(int i=0;i<is.length;i++){final int fi =i;
        		ButtonWidget w=new ButtonWidget();w.setSize(16,16);
        		w.addTooltips(ImmutableList
						.of(LangManager.translateToLocalFormatted("programmable_hatches.gt.holder.apply")));
				w.setOnClick((a,b)->{
        			if(!NetworkUtils.isClient()){	
        			if(a.mouseButton==1){
        				ItemStack[] isx = ProghatchesUtil.deseri(getCoverData().tag,"circuit"); 
        				ArrayList<ItemStack> ar=new ArrayList<>(isx.length);
        				for(ItemStack iis:isx){ar.add(iis);}
        				 ar.remove(fi);
        				 ItemStack[] x=ar.toArray(new ItemStack[isx.length-1]);
        				  getCoverData().tag=ProghatchesUtil.ser(new NBTTagCompound() , x, "circuit");
        				 getUIBuildContext().getTile().markDirty();
        				 chw.notifyChangeServer();
        				 return;
        				
        			}
        			
        			//System.out.println(is[fi]);
        			
        			ICoverable te = getUIBuildContext().getTile();
        			if(te instanceof BaseMetaTileEntity ){
        				IMetaTileEntity mte = ((BaseMetaTileEntity)te).getMetaTileEntity();
        				if(mte instanceof IConfigurationCircuitSupport){
        					IConfigurationCircuitSupport ci=(IConfigurationCircuitSupport) mte;
        					mte.setInventorySlotContents(ci.getCircuitSlot(),is[fi]);
        				}
        			}
        			}
        		});
        		w.setBackground(GT_UITextures.BUTTON_STANDARD,new ItemDrawable().setItem(is[fi]));
        		w.setPos(16*(i%perrow), 16*(i/perrow));
        		sc.widget(w);
        		
        	}
        	
        	
        	
        	
        	return sc;
		}
        
    }
	

	
	
	public void writeTag(PacketBuffer b,NBTTagCompound t) {
	
		byte bb[];
		try {
			bb = CompressedStreamTools.compress(t);
			b.writeInt(bb.length);
			b.writeBytes(bb);
		} catch (IOException e) {
			e.printStackTrace();
			b.writeInt(0);
		}
	}
	public int damageToLimit(int itemDamage) {
		int offset=itemDamage-90;//90:see ItemDedicatedCover
		if(offset==0)return 9;
		if(offset==1)return 12;
		if(offset==2)return 15;
		if(offset==3)return 32;
		if(offset==4)return 64;
		return 1;
	}
	public NBTTagCompound readTag(PacketBuffer b) {
		byte bb[]=new byte[b.readInt()];
		b.readBytes(bb);
		try {
			NBTTagCompound tag = CompressedStreamTools.func_152457_a(bb, new NBTSizeTracker(Long.MAX_VALUE));
		return tag;
		} catch (IOException e) {
			e.printStackTrace();
			return new NBTTagCompound();
		}
		
	
	}
	

	public abstract class ChangeableWidget extends Widget implements ISyncedWidget, IWidgetParent {
		//sideonly.client
		NBTTagCompound tag;
		public NBTTagCompound getCircuitsTag() {
			
			if(NetworkUtils.isClient()){return tag;}
			NBTTagCompound t=new NBTTagCompound();
			ProghatchesUtil.ser(t, getCircuits(), "circuit");
			return t;
		}
		
		public abstract ItemStack[] getCircuits();
		
		private boolean needsUpdate;

		private final List<Widget> child = new ArrayList<>();

		@Nullable
		private Widget queuedChild = null;

		private final Function<NBTTagCompound, Widget> widgetSupplier;
		private boolean initialised = false;
		private boolean firstTick = true;

		/**
		 * Creates a widget which child can be changed dynamically. Call
		 * {@link #notifyChangeServer()} to notify the widget for a change.
		 *
		 * @param widgetSupplier
		 *            widget to supply. Can return null
		 */
		public ChangeableWidget(Function<ItemStack[], Scrollable> sp) {
			this.widgetSupplier = s->{
				
				ItemStack[] is = ProghatchesUtil.deseri(s,"circuit");
				return sp.apply(is);
				
			};
		}

		@Override
		protected @NotNull Size determineSize(int maxWidth, int maxHeight) {
			if (this.child.isEmpty()) {
				return Size.ZERO;
			}
			return this.child.get(0).getSize();
		}

		/**
		 * Notifies the widget that the child probably changed. Only executed on
		 * server and synced to client. This method is preferred!
		 */
		public void notifyChangeServer() {
			if (!isClient()) {
				notifyChange(true);
			}
		}

		/**
		 * Notifies the widget that the child probably changed. Only executed on
		 * client and NOT synced to server.
		 */
		public void notifyChangeClient() {
			notifyChangeNoSync();
		}

		/**
		 * Notifies the widget that the child probably changed. Can execute on
		 * both sides and NOT synced.
		 */
		public void notifyChangeNoSync() {
			notifyChange(false);
			initQueuedChild();
		}

		private void notifyChange(boolean sync) {
			if (this.widgetSupplier == null || !isInitialised()) {
				return;
			}
			NBTTagCompound tag = getCircuitsTag();
			if (sync && !isClient()) {
				syncToClient(0, s->writeTag(s,tag));
			}
			removeCurrentChild();
			this.queuedChild = this.widgetSupplier.apply(tag);
			this.initialised = false;
		}

		private void initQueuedChild() {
			if (this.queuedChild != null) {
				final Consumer<Widget> initChildrenWrapper = widget -> {
					if (widget instanceof IWidgetParent) {
						((IWidgetParent) widget).initChildren();
					}
				};
				initChildrenWrapper.accept(this.queuedChild);
				IWidgetParent.forEachByLayer(this.queuedChild, initChildrenWrapper);
				AtomicInteger syncId = new AtomicInteger(1);
				final Consumer<Widget> initSyncedWidgetWrapper = widget1 -> {
					if (widget1 instanceof ISyncedWidget) {
						getWindow().addDynamicSyncedWidget(syncId.getAndIncrement(), (ISyncedWidget) widget1, this);
					}
				};
				if (queuedChild instanceof IWidgetParent)
					initSyncedWidgetWrapper.accept(queuedChild);
				IWidgetParent.forEachByLayer(this.queuedChild, initSyncedWidgetWrapper);
				this.queuedChild.initialize(getWindow(), this, getLayer() + 1);
				this.child.add(this.queuedChild);
				this.initialised = true;
				this.queuedChild = null;
				this.firstTick = true;
			}
			checkNeedsRebuild();
		}

		public void removeCurrentChild() {
			if (!this.child.isEmpty()) {
				Widget widget = this.child.get(0);
				widget.setEnabled(false);
				if (widget instanceof IWidgetParent) {
					widget.onPause();
					widget.onDestroy();
				}
				IWidgetParent.forEachByLayer(widget, Widget::onPause);
				IWidgetParent.forEachByLayer(widget, Widget::onDestroy);
				this.child.clear();
			}
		}

		@Override
		public void detectAndSendChanges(boolean init) {
			if (init) {
				notifyChangeServer();
			}
			if (this.initialised && !this.child.isEmpty()) {
				final Consumer<Widget> detectAndSendChangesWrapper = widget -> {
					if (widget instanceof ISyncedWidget) {
						((ISyncedWidget) widget).detectAndSendChanges(firstTick);
					}
				};
				Widget widget = this.child.get(0);
				if (widget instanceof IWidgetParent)
					detectAndSendChangesWrapper.accept(widget);
				IWidgetParent.forEachByLayer(widget, detectAndSendChangesWrapper);
				firstTick = false;
			}
		}

		@Override
		public void readOnClient(int id, PacketBuffer packetBuffer) throws IOException {
			if (id == 0) {
				tag=readTag(packetBuffer);
				notifyChange(false);
				initQueuedChild();
				syncToServer(1, NetworkUtils.EMPTY_PACKET);
			}
		}

		@Override
		public void readOnServer(int id, PacketBuffer packetBuffer) throws IOException {
			if (id == 1) {
				initQueuedChild();
			}
		}

		@Override
		public void markForUpdate() {
			needsUpdate = true;
		}

		@Override
		public void unMarkForUpdate() {
			needsUpdate = false;
		}

		@Override
		public boolean isMarkedForUpdate() {
			return needsUpdate;
		}

		@Override
		public List<Widget> getChildren() {
			return this.child;
		}
	}


	@Override
	public Data createDataObject(int aLegacyData) {
		
		throw new UnsupportedOperationException();
	}
	@Override
	public Data createDataObject() {
		
		return new Data(new NBTTagCompound());
	}
	@Override
	public boolean allowsCopyPasteTool() {
		// TODO Auto-generated method stub
		return super.allowsCopyPasteTool();
	}
@Override
protected boolean onCoverRightClickImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
		ICoverable aTileEntity, EntityPlayer aPlayer, float aX, float aY, float aZ) {
	
	return super.onCoverRightClickImpl(side, aCoverID, aCoverVariable, aTileEntity, aPlayer, aX, aY, aZ);
}
@Override
protected boolean onCoverShiftRightClickImpl(ForgeDirection side, int aCoverID, Data aCoverVariable,
		ICoverable aTileEntity, EntityPlayer aPlayer) {
	// TODO Auto-generated method stub
	return super.onCoverShiftRightClickImpl(side, aCoverID, aCoverVariable, aTileEntity, aPlayer);
}
}