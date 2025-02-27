package reobf.proghatches.ae;

import java.util.Collection;

import com.google.common.base.Objects;
import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.math.Alignment;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.CycleButtonWidget;
import com.gtnewhorizons.modularui.common.widget.textfield.NumericWidget;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.exceptions.FailedConnection;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.me.GridConnection;
import appeng.me.MachineSet;
import appeng.me.helpers.AENetworkProxy;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.networking.TileCableBus;
import appeng.util.item.AEItemStack;
import gregtech.api.gui.modularui.GTUITextures;
import gregtech.api.interfaces.IConfigurationCircuitSupport;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;

public class TileOrbSwitcher extends TileEntity implements IActionHost, IGridProxyable ,ITileWithModularUI{
	AENetworkProxy stocking = new AENetworkProxy(this, "stocking", new ItemStack(Items.apple), false);
	AENetworkProxy orb = new AENetworkProxy(this, "orb", new ItemStack(Items.apple), false);
	AENetworkProxy primary = new AENetworkProxy(this, "primary", new ItemStack(Items.apple), false);

	public TileOrbSwitcher() {
		primary.setFlags(GridFlags.CANNOT_CARRY);
		stocking.setFlags(GridFlags.CANNOT_CARRY);
		orb.setFlags(GridFlags.CANNOT_CARRY);
	} private boolean isDead;

@Override
public void validate() {
	if(this.worldObj.isRemote==false)
	  MyMod.callbacks.put(this, this::pretick);
	super.validate();
}
MachineSource source=new MachineSource(this);
ItemStack select=new ItemStack(Items.apple);


boolean nbt;
boolean meta;
boolean circuit=true;
boolean index;
private int amount=1;

public void pretick(){
	

		 if ((!isDead) && (!isInvalid())) {
		 if(this.worldObj.getTileEntity(xCoord, yCoord, zCoord)==this){
				
			 if (!init) {
					init = true;
					if(worldObj.isRemote==false){
					this.orb.onReady();
					this.primary.onReady();
					this.stocking.onReady();
			updateConnection();
					}
				}

			 
			 ForgeDirection dir=facing;//ForgeDirection.VALID_DIRECTIONS[ this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)];
			 select=null;
			TileEntity te = this.worldObj.getTileEntity(xCoord+dir.offsetX, yCoord+dir.offsetY, zCoord+dir.offsetZ);
			 
			if(circuit){
				if(te instanceof IGregTechTileEntity){
					IMetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
					 if(mte instanceof IConfigurationCircuitSupport){
						 
						 select=mte.getStackInSlot(((IConfigurationCircuitSupport) mte).getCircuitSlot());
						 if(select!=null){select=select.copy();
						 select.stackSize=amount;}
					 }
				 }
			}else{
				if(te instanceof IInventory){
					IInventory inv=(IInventory) te;
					for(int i=0;i<inv.getInventoryStackLimit();i++){
						if(inv.getStackInSlot(i)!=null&&inv.getStackInSlot(i).stackSize>0){
							select=inv.getStackInSlot(i).copy();
							select.stackSize=amount;
							
						}
						
					}
				}
				
				
			}	
			
			
				
			
			 try {
				
			 
				 IMEMonitor<IAEItemStack> stockinginv = stocking.getStorage().getItemInventory();
				 IMEMonitor<IAEItemStack> orbinv = orb.getStorage().getItemInventory();
				

				boolean foundTarget=false;
				 for(IAEItemStack i:orb.getStorage().getItemInventory()
				.getStorageList()){
					if(same(i,select)){foundTarget=true;
						if(select.stackSize<i.getStackSize()){
							IAEItemStack get = orbinv.extractItems(i.copy().setStackSize(-select.stackSize+i.getStackSize()), Actionable.MODULATE, source);
							if(get!=null)get=stockinginv.injectItems(get, Actionable.MODULATE, source);
							if(get!=null)orbinv.injectItems(get, Actionable.MODULATE, source);
						}else{
							IAEItemStack get = stockinginv.extractItems(i.copy().setStackSize(select.stackSize-i.getStackSize()), Actionable.MODULATE, source);
							if(get!=null)get=orbinv.injectItems(get, Actionable.MODULATE, source);
							if(get!=null)stockinginv.injectItems(get, Actionable.MODULATE, source);
						}
						
						
						
					}else{
						IAEItemStack get = orbinv.extractItems(i, Actionable.MODULATE, source);
						if(get!=null)get=stockinginv.injectItems(get, Actionable.MODULATE, source);
						if(get!=null)orbinv.injectItems(get, Actionable.MODULATE, source);
					}
					
					
				}
			   if(foundTarget==false&&select!=null){
				  AEItemStack sel = AEItemStack.create(select);
				   
				   IAEItemStack get = stockinginv.extractItems(sel.setStackSize(select.stackSize), Actionable.MODULATE, source);
					if(get==null){
						if(nbt==false||meta==false){
						//Collection<IAEItemStack> fuzz = stockinginv.getStorageList().findFuzzy(sel, FuzzyMode.IGNORE_ALL);
						
						for(IAEItemStack item:stockinginv.getStorageList()){
							if(same(item,select)){//find fuzzy might return some oredict items... check it!
								
								get = stockinginv.extractItems(item.copy().setStackSize(select.stackSize), Actionable.MODULATE, source);
								if(get!=null)break;
							}
							
						}
					}
					}
				   
				   
				   if(get!=null)get=orbinv.injectItems(get, Actionable.MODULATE, source);
				if(get!=null)stockinginv.injectItems(get, Actionable.MODULATE, source);
			
				   
			   }
			 
			 
			 
			 
			 
			 
			 } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			 
			 
			 
		 }
	}
	
}

public boolean same(IAEItemStack a, ItemStack b){
	
	
	if(a.getItem()!=b.getItem()){return false;}
	if(meta)if(a.getItemDamage()!=b.getItemDamage()){return false;}
	if(nbt)if(
			Objects.equal(
			a.getTagCompound(),b.getTagCompound())
			){return false;}
	return true;
}
	@Override
	public void writeToNBT(NBTTagCompound compound) {
		stocking.writeToNBT(compound);
		orb.writeToNBT(compound);
		primary.writeToNBT(compound);
		compound.setByte("dirorb", (byte) dirorb.ordinal());
		compound.setByte("dirstocking", (byte) dirstocking.ordinal());
		compound.setByte("facing", (byte) facing.ordinal());
		compound.setBoolean("nbt",nbt);
		compound.setBoolean("meta",meta);
		compound.setByte("amount", (byte) amount);
		
		super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		stocking.readFromNBT(compound);
		orb.readFromNBT(compound);
		primary.readFromNBT(compound);
		dirorb=ForgeDirection.VALID_DIRECTIONS[compound.getByte("dirorb")];
		dirstocking=ForgeDirection.VALID_DIRECTIONS[compound.getByte("dirstocking")];
		facing=ForgeDirection.VALID_DIRECTIONS[compound.getByte("facing")];
		nbt=compound.getBoolean("nbt");
		meta=compound.getBoolean("meta");
		amount=compound.getByte("amount");
		if(amount<0)amount=1;
		super.readFromNBT(compound);
	}

	public void updateConnection(){
		
		
		for (ForgeDirection forgeDirection : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tileEntity = worldObj.getTileEntity(
                    xCoord + forgeDirection.offsetX,
                    yCoord + forgeDirection.offsetY,
                    zCoord + forgeDirection.offsetZ);
            if (tileEntity != null && tileEntity instanceof IGridHost) {
                IGridNode gridNode = ((IGridHost) tileEntity).getGridNode(forgeDirection.getOpposite());
                if (gridNode != null){ 
                	for(IGridConnection side:gridNode.getConnections()){
                		IGridNode other = side.getOtherSide(gridNode);
                		if(other==orb.getNode()||other==stocking.getNode()){
                			side.destroy();break;
                		}
                	}
                	
                	
                	gridNode.updateState();}
            }
        }
		
		
		
		
	}
	public ForgeDirection facing=ForgeDirection.DOWN;
	ForgeDirection dirorb=ForgeDirection.DOWN;
	ForgeDirection dirstocking=ForgeDirection.UP;
	@Override
	public IGridNode getGridNode(ForgeDirection dir) {
		if (dir == dirstocking) {
			return stocking.getNode();
		}
		if (dir == dirorb) {
			return orb.getNode();
		}
		/*if (dir == ForgeDirection.UNKNOWN) {
			return primary.getNode();
		}*/
		return null;
	}

	public void mark(EntityPlayer player) {

		stocking.setOwner(player);
		orb.setOwner(player);
		primary.setOwner(player);
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir) {

		return AECableType.SMART;
	}

	@Override
	public void securityBreak() {

	}

	boolean init;
	

	@Override
	public void updateEntity() { isDead = false;
	
		super.updateEntity();
	}

	@Override
	public IGridNode getActionableNode() {

		return primary.getNode();
	}

	@Override
	public DimensionalCoord getLocation() {

		return new DimensionalCoord(this);
	}

	@Override
	public void gridChanged() {

	}
@Override
public void invalidate() {
	this.orb.invalidate();
	this.primary.invalidate();
	this.stocking.invalidate();
	super.invalidate();
}

@Override
public void onChunkUnload() {
	this.orb.onChunkUnload();
	this.primary.onChunkUnload();
	this.stocking.onChunkUnload();
	super.onChunkUnload(); isDead = true;
}
	@Override
	public AENetworkProxy getProxy() {

		return primary;
	}
	   
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	    {
		NBTTagCompound tag = pkt.func_148857_g();
		dirorb=ForgeDirection.VALID_DIRECTIONS[tag.getByte("dirorb")];
		dirstocking=ForgeDirection.VALID_DIRECTIONS[tag.getByte("dirstocking")];
		facing=ForgeDirection.VALID_DIRECTIONS[tag.getByte("facing")];
		worldObj.markBlockRangeForRenderUpdate(xCoord, yCoord, zCoord, xCoord, yCoord, zCoord);
	    }
	   
	   @Override
	public Packet getDescriptionPacket() {
	
		return new S35PacketUpdateTileEntity(
				 xCoord,
				 yCoord,
				 zCoord,
				this.worldObj.getBlockMetadata(xCoord, yCoord, zCoord)
				,new NBTTagCompound(){{
					this.setByte("dirorb", (byte)dirorb.ordinal());
					this.setByte("dirstocking", (byte)dirstocking.ordinal());
					this.setByte("facing", (byte)facing.ordinal());
				}}
				 );
	}
		 public  void sendPacket() {
			 
			 sendPacketToAllPlayers(getDescriptionPacket(),this.getWorldObj());
			 
		 }
	   
	   private static void sendPacketToAllPlayers(Packet packet, World world) {
	        for (Object player : world.playerEntities) {
	            if (player instanceof EntityPlayerMP) {
	                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(packet);
	            }
	        }
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
	@Override
	public ModularWindow createWindow(UIBuildContext buildContext) {
		 ModularWindow.Builder builder = ModularWindow.builder(getGUIWidth(), getGUIHeight());
         builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        
         if (doesBindPlayerInventory()) {
             builder.bindPlayerInventory(buildContext.getPlayer());
         }
         
         builder.widget(new NumericWidget().setSetter(val -> amount = (int) val).setGetter(() -> amount).setBounds(1, Integer.MAX_VALUE-1).setScrollValues(1, 4, 64).setTextAlignment(Alignment.Center).setTextColor(Color.WHITE.normal).setSize(70, 18).setPos(43, 3).setBackground(GTUITextures.BACKGROUND_TEXT_FIELD)
        		 .addTooltip(StatCollector.translateToLocal("proghatches.orb_switcher.amount"))
        		 );
         builder.widget(
                 new CycleButtonWidget().setToggle(() -> nbt, s -> nbt = s)
                     .setTextureGetter(s -> {
                         if (s == 0) return GTUITextures.OVERLAY_BUTTON_CROSS;

                         return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
                     })
                     .addTooltip(0, StatCollector.translateToLocal("proghatches.orb_switcher.nbt.mode.0"))
                     .addTooltip(1, StatCollector.translateToLocal("proghatches.orb_switcher.nbt.mode.1"))
                     .setBackground(() -> {
                         {
                             return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
                         }
                     })
                     .setSize(18, 18)
                     .setPos(3, 3));
         builder.widget(
                 new CycleButtonWidget().setToggle(() -> meta, s -> meta = s)
                     .setTextureGetter(s -> {
                         if (s == 0) return GTUITextures.OVERLAY_BUTTON_CROSS;

                         return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
                     })
                     .addTooltip(0, StatCollector.translateToLocal("proghatches.orb_switcher.meta.mode.0"))
                     .addTooltip(1, StatCollector.translateToLocal("proghatches.orb_switcher.meta.mode.1"))
                     .setBackground(() -> {
                         {
                             return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
                         }
                     })
                     .setSize(18, 18)
                     .setPos(3, 3+20));
        /* builder.widget(
                 new CycleButtonWidget().setToggle(() -> circuit, s -> circuit = s)
                     .setTextureGetter(s -> {
                         if (s == 0) return GTUITextures.OVERLAY_BUTTON_CROSS;

                         return GTUITextures.OVERLAY_BUTTON_CHECKMARK;
                     })
                     .addTooltip(0, StatCollector.translateToLocal("proghatches.orb_switcher.circuit.mode.0"))
                     .addTooltip(1, StatCollector.translateToLocal("proghatches.orb_switcher.circuit.mode.1"))
                     .setBackground(() -> {
                         {
                             return new IDrawable[] { GTUITextures.BUTTON_STANDARD, };
                         }
                     })
                     .setSize(18, 18)
                     .setPos(3, 3+20));*/
         
         
      return builder.build();
	}

}
