package reobf.proghatches.main.asm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.gtnewhorizon.gtnhmixins.IEarlyMixinLoader;

import cpw.mods.fml.relauncher.FMLCorePlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.Name("PHCoreMod")
public class FMLPlugin implements IEarlyMixinLoader, IFMLLoadingPlugin{
@Override
public String[] getASMTransformerClass() {

	return new String[]{EUInterfaceTransformer.class.getName()};
}

@Override
public String getModContainerClass() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String getSetupClass() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public void injectData(Map<String, Object> data) {
	// TODO Auto-generated method stub
	
}

@Override
public String getAccessTransformerClass() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public String getMixinConfig() {
	// TODO Auto-generated method stub
	return null;
}

@Override
public List<String> getMixins(Set<String> loadedCoreMods) {

	return new ArrayList<>();
}
}
