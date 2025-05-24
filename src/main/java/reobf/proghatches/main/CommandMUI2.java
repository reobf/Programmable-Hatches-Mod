package reobf.proghatches.main;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import com.google.common.collect.ImmutableList;

import gregtech.api.modularui2.GTGuis;
import reobf.proghatches.main.MyMod.Prop;

public class CommandMUI2 extends CommandBase {

    @Override
    public String getCommandName() {

        return "mui2toggle";
    }

    @Override
    public List getCommandAliases() {

        return ImmutableList.of("mui2");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {

        return 2;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
      
    	GTGuis.GLOBAL_SWITCH_MUI2=!GTGuis.GLOBAL_SWITCH_MUI2;
    	
    	sender.addChatMessage(new ChatComponentText(""+GTGuis.GLOBAL_SWITCH_MUI2));

    }

}
