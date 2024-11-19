package reobf.proghatches.main.mixin.mixins.eucrafting;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Surrogate;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.glodblock.github.client.gui.GuiInterfaceWireless;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.util.ForgeDirection;
import reobf.proghatches.main.MyMod;
import reobf.proghatches.net.RenameMessage;

@Mixin(
		targets="com.glodblock.github.client.gui.GuiInterfaceWireless$InterfaceWirelessEntry"
		, remap = false
		)
public class MixinWirelessRename {
	
	
	public boolean isCover;
	@Shadow long id;	
	
	@Shadow int x;//, y, z, dim, side;
	@Shadow int y;
	@Shadow int z;
	@Shadow int dim;
	@Shadow int side;
	@Inject(cancellable=true,method = "mouseClicked", at = @At(value = "NEW",target="com/glodblock/github/network/CPacketRenamer"), require = 1)

	 public void mouseClicked(int mouseX, int mouseY, int btn,CallbackInfoReturnable<Boolean> c) {
		 if(isCover){
				
			 if(Minecraft.getMinecraft().thePlayer.getEntityWorld().provider.dimensionId==dim)
			 MyMod.net.sendToServer(
						new RenameMessage(x, y, z, ForgeDirection.getOrientation(side)));
			
				
				c.setReturnValue(true);
		 }
	 }
	@Surrogate
	@Inject(method = "<init>", at = @At(value = "RETURN"))
	public void  ctor(/*GuiInterfaceWireless parent,long idx, String name, int rows, int rowSize, boolean online,*/CallbackInfo x) {
		
		 if((id&Long.MIN_VALUE)!=0){
			isCover=true;
			//id=id&Long.MAX_VALUE;
		}
	
		
	}
	
}
