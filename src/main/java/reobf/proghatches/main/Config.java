package reobf.proghatches.main;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

import com.google.common.base.Supplier;

public class Config {

    public static String greeting = "Hello World";
    public static int metaTileEntityOffset = 22000;
    public static boolean skipRecipeAdding;
    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

       // greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");
        metaTileEntityOffset= configuration.getInt("MetaTEOffset", "ID", metaTileEntityOffset, 14301, 29999, "The GT MetaTE ID used by this mod, will use range:[offset,offset+200], make sure it's in [14301,14999] or [17000,29999]");
        configuration.addCustomCategoryComment("ID", "Configurable ID settings, DO NOT change it until necessary.");
        skipRecipeAdding= configuration.getBoolean("skipRecipeAddition",  Configuration.CATEGORY_GENERAL, skipRecipeAdding, "If true, this mod will not add any recipe.");
        
        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    static public boolean isCN;
    static {

        // Locale locale = Locale.getDefault();// net.minecraft.client.resources.Locale
        // String language = locale.getLanguage();
        isCN = System.getProperty("user.language")
            .equalsIgnoreCase("zh");
    }

    public static String defaultName(String en, String cn) {
        return isCN ? cn : en;
    }

    public static <T> T defaultObj(T en, T cn) {
        return isCN ? cn : en;
    }

    public static <T> T defaultObj(Supplier<T> en, Supplier<T> cn) {
        return isCN ? cn.get() : en.get();
    }

}
