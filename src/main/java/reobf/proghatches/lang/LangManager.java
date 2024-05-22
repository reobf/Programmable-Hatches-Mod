package reobf.proghatches.lang;

import net.minecraft.util.StatCollector;

public abstract class LangManager {
	public static String translateToLocal(String p_74838_0_) {
		return StatCollector.translateToLocal(p_74838_0_);
	}

	public static String translateToLocalFormatted(String p_74837_0_, Object... p_74837_1_) {
		return StatCollector.translateToLocalFormatted(p_74837_0_, p_74837_1_);
	}

	public static boolean canTranslate(String p_94522_0_) {
		return StatCollector.canTranslate(p_94522_0_);
	}

}
