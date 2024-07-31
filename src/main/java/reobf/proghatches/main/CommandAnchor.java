package reobf.proghatches.main;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import reobf.proghatches.main.MyMod.Prop;

public class CommandAnchor extends CommandBase{

	@Override
	public String getCommandName() {
	
		return "anchoralert";
	}
	
	@Override
	public List getCommandAliases() {
	
		return ImmutableList.of("aa");
	}
	@Override
	public String getCommandUsage(ICommandSender sender) {
		
		return "anchoralert.usage";
	}
@Override
public int getRequiredPermissionLevel() {

	return 0;
}
	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(sender instanceof EntityPlayer==false){return;}
		EntityPlayer p=(EntityPlayer) sender;	
		MyMod.Prop pr=(Prop) p.getExtendedProperties(MyMod.GET_PROGHATCHBOOK);
		if(args.length<2){
			p.addChatMessage(new ChatComponentTranslation("anchoralert.usage"));
			p.addChatMessage(new ChatComponentTranslation("anchoralert.private."+((pr.alert_mask&1)!=0)));
			p.addChatMessage(new ChatComponentTranslation("anchoralert.dim."+((pr.alert_mask&2)!=0)));
			p.addChatMessage(new ChatComponentTranslation("anchoralert.global."+((pr.alert_mask&4)!=0)));
			
			return;}
		
	
		if(pr==null)return;
		if(!args[0].equals("toggle")){return;}
		switch(args[1]){
		case "private":{
			pr.alert_mask=pr.alert_mask^1;
			p.addChatMessage(new ChatComponentTranslation("anchoralert.private."+((pr.alert_mask&1)!=0)));
			return;}
		case "dim":{
			pr.alert_mask=pr.alert_mask^2;
			p.addChatMessage(new ChatComponentTranslation("anchoralert.dim."+((pr.alert_mask&2)!=0)));
			return;}
		case "global":{
			pr.alert_mask=pr.alert_mask^4;
			p.addChatMessage(new ChatComponentTranslation("anchoralert.global."+((pr.alert_mask&4)!=0)));
			return;}
		case "all":{
			if((pr.alert_mask&7)!=0){
				pr.alert_mask=7;
			}
			pr.alert_mask=pr.alert_mask^7;
			
			p.addChatMessage(new ChatComponentTranslation("anchoralert.private."+((pr.alert_mask&1)!=0)));
			p.addChatMessage(new ChatComponentTranslation("anchoralert.dim."+((pr.alert_mask&2)!=0)));
			p.addChatMessage(new ChatComponentTranslation("anchoralert.global."+((pr.alert_mask&4)!=0)));
			return;}	
		
		
		
		}
		
		
	}

}
