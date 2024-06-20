package reobf.proghatches.main.mixin;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class LateMixinPlugin implements ILateMixinLoader{

	@Override
	public String getMixinConfig() {
		System.out.println("aaaaaaaaaaa");
		return "mixins.programmablehatches.late.json";
	}

	@Override
	public List<String> getMixins(Set<String> loadedMods) {
		System.out.println("aaaaaaaaaaa");
		return MixinPlugin.retLate;
	}

}
