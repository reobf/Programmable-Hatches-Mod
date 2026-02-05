package reobf.proghatches.gt.metatileentity.multi;

import java.lang.reflect.InvocationTargetException;

import gregtech.api.interfaces.IHatchElement;
import gregtech.api.util.HatchElementBuilder;

public class Util {
	
    public static <T>HatchElementBuilder<T> hint(HatchElementBuilder<T> r,int aHint) {
		
    	
      	try {
			r.getClass().getMethod("hint", int.class).invoke(r, aHint);
		} catch (Exception e) {
		}
      	try {
			r.getClass().getMethod("dot", int.class).invoke(r, aHint);
		} catch (Exception e) {
		}      
		
		return r;
    }


    
    
    
}
