package reobf.proghatches.net;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import baubles.api.BaublesApi;
import baubles.common.network.PacketHandler;
import baubles.common.network.PacketSyncBauble;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.main.MyMod;

public class ModeSwitchedMessage implements IMessage {

    public ModeSwitchedMessage() {}

    public ModeSwitchedMessage(int i, int j) {

        index = i;
        mode = j;
    }

    int index, mode;

    public interface I {

        public default ModeSwitchedMessage onMessage(ModeSwitchedMessage message, MessageContext ctx) {
            return null;
        }
    }

    public static class Handler implements I, IMessageHandler<ModeSwitchedMessage, ModeSwitchedMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public ModeSwitchedMessage onMessage(ModeSwitchedMessage message, MessageContext ctx) {

            IInventory inv = BaublesApi.getBaubles(Minecraft.getMinecraft().thePlayer);

            ItemStack is = inv.getStackInSlot(message.index);
            if (is != null && is.getItem() instanceof ItemProgrammingToolkit) {
             	Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new
                		ChatComponentText("[Client]"));
            	        	Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new
        			ChatComponentTranslation("proghatch.keybinding.kit.switch.mode." + (is.getItemDamage())));
      
            }
            
        	

        	 
        	 /* 	 ((NetHandlerPlayServer) ctx.netHandler).playerEntity.addChatMessage(new

                     ChatComponentTranslation("proghatch.keybinding.kit.switch.mode." + (is.getItemDamage())));
           */
        	 
        	 
        	 return null;

        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        index = buf.readInt();
        mode = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(index);
        buf.writeInt(mode);
    }

}
