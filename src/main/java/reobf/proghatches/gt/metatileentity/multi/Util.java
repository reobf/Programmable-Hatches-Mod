package reobf.proghatches.gt.metatileentity.multi;

import gregtech.api.util.HatchElementBuilder;

public class Util {

    public static <T> HatchElementBuilder<T> hint(HatchElementBuilder<T> r, int aHint) {
        return r.hint(aHint);
    }
}
