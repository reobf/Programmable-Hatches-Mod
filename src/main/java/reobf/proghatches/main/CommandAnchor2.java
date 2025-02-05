package reobf.proghatches.main;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.google.common.collect.ImmutableList;

import reobf.proghatches.block.ChunkTrackingGridCahce;

public class CommandAnchor2 extends CommandBase {

    @Override
    public String getCommandName() {

        return "proghatch";
    }

    @Override
    public List getCommandAliases() {

        return ImmutableList.of();
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {

        return "";
    }

    @Override
    public int getRequiredPermissionLevel() {

        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        try {
            if (args[0].equals("tracking")) {
                // MyMod.disable=!MyMod.disable;
                sender.addChatMessage(new ChatComponentText("tracking:" + !MyMod.disable));
                sender.addChatMessage(new ChatComponentText("max caches:" + MyMod.max));
                sender
                    .addChatMessage(new ChatComponentText("current caches:" + ChunkTrackingGridCahce.cacheinst.size()));
            }
        } catch (Exception e) {
            sender.addChatMessage(new ChatComponentText("/proghatch tracking"));

        }

    }

}
