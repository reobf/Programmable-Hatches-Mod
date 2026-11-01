package reobf.proghatches.client.guide;

import com.hfstudio.guidenh.integration.api.GuideNhIntegrationRegistry;

public final class GuideNhIntegration {

    private GuideNhIntegration() {}

    public static void register() {
        GuideNhIntegrationRegistry.global()
            .registerTagCompilerProvider(compilers -> compilers.add(new FlowchartCompiler()));
        GuideNhIntegrationRegistry.global()
            .registerSceneElementTagCompilerProvider(compilers -> compilers.add(new Mui2GuiCompiler()));
    }
}
