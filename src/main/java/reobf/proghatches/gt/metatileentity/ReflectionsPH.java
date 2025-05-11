package reobf.proghatches.gt.metatileentity;

import java.lang.reflect.Field;

import gregtech.api.metatileentity.implementations.MTEHatch;

public class ReflectionsPH {

    static Field f0;

    static {

        try {
            f0 = MTEHatch.class.getDeclaredField("texturePage");
            f0.setAccessible(true);
        } catch (Exception e) {
            throw new AssertionError();
        }

    }

    static public int getTexturePage(Object o) {

        try {
            return f0.getInt(o);
        } catch (Exception e) {

        }
        return 0;

    }
}
