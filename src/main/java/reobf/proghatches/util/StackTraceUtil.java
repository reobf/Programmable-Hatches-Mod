package reobf.proghatches.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.function.IntFunction;

public class StackTraceUtil {

    public static String getCallerMethod(int index) {

        // if(un!=null)return un.apply(index);
        return getMethodFallback(index);
    }

    static Throwable t = new Throwable();

    public static String getMethodFallback(int index) {
        return t.fillInStackTrace()
            .getStackTrace()[index + 2].getClassName();
    }

    static IntFunction<String> un;// =scala.concurrent.util.Unsafe.instance;

    static {
        try {
            Class c = Class.forName("sun.reflect.Reflection");
            Method m = c.getDeclaredMethod("getCallerClass", int.class);

            MethodHandle mh = MethodHandles.lookup()
                .unreflect(m);

            un = i -> {
                try {
                    return ((Class) mh.invoke(i + 4)).getName();
                } catch (Throwable e) {
                    e.printStackTrace();
                    un = null;
                    return getMethodFallback(i);
                }
            };

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
