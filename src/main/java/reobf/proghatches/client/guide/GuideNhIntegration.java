package reobf.proghatches.client.guide;

import com.hfstudio.guidenh.integration.api.GuideNhIntegrationRegistry;

import reobf.proghatches.gt.metatileentity.CyclicPatternGuiAdapter;

public final class GuideNhIntegration {

    private GuideNhIntegration() {}

    public static void register() {
        Mui2PreviewAdapter.ADAPTERS.add(new CyclicPatternGuiAdapter());
        Mui2PreviewAdapter.ADAPTERS.add(new DualInputGuiAdapter());
        Mui2PreviewAdapter.ADAPTERS.add(new ChanceControllerGuiAdapter());
        Mui2PreviewAdapter.ADAPTERS.add(new LargeProviderGuiAdapter());
        GuideNhIntegrationRegistry.global()
            .registerTagCompilerProvider(compilers -> compilers.add(new FlowchartCompiler()));
        GuideNhIntegrationRegistry.global()
            .registerSceneElementTagCompilerProvider(compilers -> compilers.add(new Mui2GuiCompiler()));
    }
}
