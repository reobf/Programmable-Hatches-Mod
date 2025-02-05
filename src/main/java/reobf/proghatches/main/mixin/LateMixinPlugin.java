package reobf.proghatches.main.mixin;

import java.util.List;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

import cpw.mods.fml.common.Loader;
import reobf.proghatches.main.MyMod;

@LateMixin
public class LateMixinPlugin implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        System.out.println("aaaaaaaaaaa");
        return "mixins.programmablehatches.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        System.out.println("aaaaaaaaaaa");

        // if(mixinClassName.equals("reobf.proghatches.main.mixin.mixins.part2.MixinProcessLogicDoNotCache")){

        String str = (Loader.instance()
            .getIndexedModList()
            .get("galacticgreg")
            .getVersion());

        System.out.println(str);

        String v1 = str.split("\\.")[2];
        String v2 = str.split("\\.")[3];
        System.out.println(v1);
        System.out.println(v2);
        if (Integer.valueOf(v1) >= 51) {
            if (v2.contains("-")) {
                v2 = v2.substring(0, v2.lastIndexOf("-"));
            }
            if (Integer.valueOf(v2) >= 69) {
                MixinPlugin.retLate.add("part2.MixinProcessLogicDoNotCache");
                MyMod.newGTCache = true;
            }
        }

        // }
        return MixinPlugin.retLate;
    }

}
