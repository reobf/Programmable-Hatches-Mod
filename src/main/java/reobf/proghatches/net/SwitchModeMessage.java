package reobf.proghatches.net;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.util.ChatComponentTranslation;

import baubles.api.BaublesApi;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import reobf.proghatches.item.ItemProgrammingToolkit;
import reobf.proghatches.main.MyMod;

public class SwitchModeMessage implements IMessage {

    public SwitchModeMessage() {}

    public static class Handler implements IMessageHandler<SwitchModeMessage, SwitchModeMessage> {

        @Override
        public SwitchModeMessage onMessage(SwitchModeMessage message, MessageContext ctx) {

            IInventory inv = BaublesApi.getBaubles(((NetHandlerPlayServer) ctx.netHandler).playerEntity);
            for (int i = 0; i < inv.getSizeInventory(); i++) {
                ItemStack is = inv.getStackInSlot(i);
                if (is != null && is.getItem() instanceof ItemProgrammingToolkit) {

                    is.setItemDamage((is.getItemDamage() + 1) % ItemProgrammingToolkit.maxModes);
                    ((NetHandlerPlayServer) ctx.netHandler).playerEntity.addChatMessage(new

                    ChatComponentTranslation("proghatch.keybinding.kit.switch.mode." + (is.getItemDamage())));
                    MyMod.net.sendTo(
                        new ModeSwitchedMessage(i, is.getItemDamage()),
                        ((NetHandlerPlayServer) ctx.netHandler).playerEntity);

                    break;
                    // ((ItemProgrammingToolkit)is.getItem()).onItemRightClick(is, null, null);
                }

            }
            return null;

        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {

    }

    @Override
    public void toBytes(ByteBuf buf) {

    }

}
