package reobf.proghatches.main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.util.ByteArrayBuffer;

import net.minecraft.util.StatCollector;
import net.minecraft.util.StringTranslate;
import net.minecraftforge.common.config.Configuration;
import reobf.proghatches.lang.LangManager;
import scala.actors.threadpool.Arrays;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

public class Config {
public static boolean appendAddedBy=true;
    public static String greeting = "Hello World";
    public static int metaTileEntityOffset = 22000;
    public static boolean skipRecipeAdding;
   
    
    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

       // greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        metaTileEntityOffset= configuration.getInt("MetaTEOffset", "ID", metaTileEntityOffset, 14301, 29999, "The GT MetaTE ID used by this mod, will use range:[offset,offset+200], make sure it's in [14301,14999] or [17000,29999]");
        configuration.addCustomCategoryComment("ID", "Configurable ID settings, DO NOT change it until necessary.");
        skipRecipeAdding= configuration.getBoolean("skipRecipeAddition",  Configuration.CATEGORY_GENERAL, skipRecipeAdding, "If true, this mod will not add any recipe.");
        appendAddedBy= configuration.getBoolean("appendAddedBy",  Configuration.CATEGORY_GENERAL, appendAddedBy, "Append 'Added by ProgrammableHatches' at the end of machine desc.");
       // lang=configuration.getString("language",  Configuration.CATEGORY_GENERAL, lang, "Language for dedicated server, no effect on client side. If you change it, you should delete GregTech.lang to re-generate.");
        
        /*if(System.getProperty("proghatches.language")!=null){
        lang=System.getProperty("proghatches.language");}*/
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
/*
    static public boolean isCN;
    static {

        // Locale locale = Locale.getDefault();// net.minecraft.client.resources.Locale
        // String language = locale.getLanguage();
        isCN = System.getProperty("user.language")
            .equalsIgnoreCase("zh");
    }*/
    public static String lang =  System.getProperty("user.language")
            .equalsIgnoreCase("zh")?"zh_CN":"en_US";
   /* public static String defaultName(String en, String cn) {
        return isCN ? cn : en;
    }

    public static <T> T defaultObj(T en, T cn) {
        return isCN ? cn : en;
    }

    public static <T> T defaultObj(Supplier<T> en, Supplier<T> cn) {
        return isCN ? cn.get() : en.get();
    }*/
    public static String[] get(String key,Map<String,Object> fmtter){
    	return  get(key,fmtter,false);
    }
    public static String[] get(String key,Map<String,Object> fmtter,boolean defaulted){
    	try(InputStream in=(defaulted? getInputEN:getInput).apply(key)){
    		if(in==null){
    			if(defaulted)
    			return new String[]{
    					"!!! FATAL !!!",
    					"en_US language file missing:"+key+".lang",
    					"do not remove en_US folder!",
    				
    			};
    			
    			return  get(key,fmtter,true);
    		}
    		byte[] b=new byte[in.available()];
    		int off=0;
    		int tmp;
    		do{
    		tmp=in.read(b, off, b.length - off);
    		off+=tmp;
    		}while(in.available()>0);
    		String[] arr=new String(b,"UTF-8").split("\r?\n");
    		for(int i=0;i<arr.length;i++){final int ii=i;
    			String[] arrf = arr;
    			fmtter.forEach((k,v)->{
    				arrf[ii]=arrf[ii].replace(String.format("{%s}",k),v.toString());
    				//arr[ii].inde
    			
    			Pattern p=Pattern.compile("\\{%s\\?\\}.*?\\{%s:\\}.*?\\{%s!\\}".replace("%s",k));
    			while(true){
    			Matcher m=p.matcher(arrf[ii]);
    			if(m.find()){
    				String torep=arrf[ii].substring(m.start(),m.end());
    				String repby;
    				int ia=torep.indexOf("{"+k+"?}");
    				int ib=torep.indexOf("{"+k+":}");
    				int ic=torep.indexOf("{"+k+"!}");
    				if(Boolean.valueOf(v.toString())){
    					repby=torep.substring(ia+("{"+k+"?}").length(),ib);
    				}else{
    					repby=torep.substring(ib+("{"+k+":}").length(),ic);
    				}
    				
    				
    				arrf[ii]=arrf[ii].replace(torep, repby);
    			}else{break;}
    			}
    			
    			
    			
    			
    			
    			});
    			
    			
    			
    		}
    		//System.out.println(Arrays.asList(arr));
    		
    		if(appendAddedBy){
    			String[] t=arr;
    			t=new String[arr.length+1];
    			System.arraycopy(arr, 0, t, 0, arr.length);
    			t[t.length-1]=LangManager.translateToLocal("programmable_hatches.addedby");
    			arr=t;
    		}
    		if(defaulted){
    			
    			
    			
    			
    			MyMod.LOG.fatal("Your current translation key:'programmable_hatches.gt.lang.dir' maps to:" + LangManager.translateToLocal("programmable_hatches.gt.lang.dir"));
    			MyMod.LOG.fatal("That means yous should put translated "+key+".lang in "+
    					 "/assets/proghatches/lang/"+
		  				  LangManager.translateToLocal("programmable_hatches.gt.lang.dir")+
		  				  "/"+
		  				  key+
		  				  ".lang"
    					);
    			MyMod.LOG.fatal("... then delete GregTech.lang to regenerate that file."
   					);
    			
    			
    			
    			
    		}
    		
    		
    		return arr;
    		
    	} catch (IOException e) {
	    	MyMod.LOG.fatal("failed to get GT description:"+key);
    		e.printStackTrace();
		}
    	
    	
    	return null;
    }
    
    
    
    
    static Function<String,InputStream> getInputEN= 
  		  s->
  		  Config.class.getResourceAsStream(
  		  		  "/assets/proghatches/lang/en_US/"+
  		  				  s+
  		  				  ".lang"
  		  );
  static Function<String,InputStream> getInput= 
		  s->
		  Config.class.getResourceAsStream(
		  		  "/assets/proghatches/lang/"+
		  				  LangManager.translateToLocal("programmable_hatches.gt.lang.dir")+
		  				  "/"+
		  				  s+
		  				  ".lang"
		  );



}
