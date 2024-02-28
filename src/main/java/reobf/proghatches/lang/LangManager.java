package reobf.proghatches.lang;

import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.SidedProxy;
import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import reobf.proghatches.main.CommonProxy;
import reobf.proghatches.main.Config;


public abstract class LangManager {
	public static  String translateToLocal(String p_74838_0_) {
		return proxy.translateToLocal(p_74838_0_);
	}
	  public static String translateToLocalFormatted(String p_74837_0_, Object ... p_74837_1_) {
		return proxy.translateToLocalFormatted(p_74837_0_, p_74837_1_);
	}
	  public static boolean canTranslate(String p_94522_0_) {
		return proxy.canTranslate(p_94522_0_);
	}
	public abstract static class Translation {
	  public abstract  String translateToLocal(String p_74838_0_);
	  public abstract String translateToLocalFormatted(String p_74837_0_, Object ... p_74837_1_);
	  public abstract boolean canTranslate(String p_94522_0_);
	}
	    @SidedProxy(
	    		clientSide = "reobf.proghatches.lang.LangManager$ClientTranslation", 
	    		serverSide = "reobf.proghatches.lang.LangManager$ServerTranslation")
	    public static Translation proxy;
	    
	    public static class  ClientTranslation extends Translation{
		    	@Override
				public String translateToLocal(String p_74838_0_) {
					return StatCollector.translateToLocal(p_74838_0_);
				}
	
				@Override
				public String translateToLocalFormatted(String p_74837_0_, Object... p_74837_1_) {
					return StatCollector.translateToLocalFormatted(p_74837_0_, p_74837_1_);
				}
	
				@Override
				public boolean canTranslate(String p_94522_0_) {
					return StatCollector.canTranslate(p_94522_0_);
				}
			}
	    
	    
	    public static class  ServerTranslation extends Translation{
	    	HashMap<String,String> translationMap=new HashMap<>();
	    	boolean init;
	    	public void init(){ 
	    		if(!init){init =true;}else return;
	    		translationMap.putAll(
	    		StringTranslate.parseLangFile(
	    		Config.class.getResourceAsStream(
	    			  "/assets/proghatches/lang/"+
	    					  Config.lang+".lang"
	    					  )));
	    		
	    		
	    		
	    		
	    	}
	    	
			@Override
			public String translateToLocal(String a) {
				init();
				return translationMap.getOrDefault(a, StatCollector.translateToLocal(a));
			}

			@Override
			public String translateToLocalFormatted(String a, Object... b) {
				init();try{
				return String.format(
						translationMap.getOrDefault(a, StatCollector.translateToLocal(a))
						 ,b)
						;
			}catch(java.util.IllegalFormatException  e){
				return a;
			}
			}

			@Override
			public boolean canTranslate(String a) {init();
				return translationMap.containsKey(a)||StatCollector.canTranslate(a);
			}}
	    
	    
}
