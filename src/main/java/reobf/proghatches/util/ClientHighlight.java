package reobf.proghatches.util;

/**
 * CLIENT-CLASSLOAD-ONLY holder for the AE block-highlight action. All references to client-only
 * classes (Minecraft, and transitively WorldClient / EntityClientPlayerMP through its fields) live
 * in THIS class, which is only ever loaded when a GUI button callback actually invokes it on the
 * client. Putting this code directly inside a MetaTileEntity class made the dedicated server crash
 * at REGISTRATION time: passing Minecraft.theWorld (declared type WorldClient) as a World argument
 * forces the bytecode verifier to load WorldClient during class verification of the MTE — long
 * before any side check could run.
 */
public final class ClientHighlight {

    private ClientHighlight() {}

    /** Highlights the position with AE2's pulsing box + chat message and closes the current screen. */
    public static void highlightAndClose(int x, int y, int z) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        appeng.client.render.highlighter.BlockPosHighlighter.highlightBlocks(
            mc.thePlayer,
            java.util.Collections.singletonList(new appeng.api.util.DimensionalCoord(mc.theWorld, x, y, z)),
            appeng.core.localization.PlayerMessages.MachineHighlighted.getUnlocalized(),
            appeng.core.localization.PlayerMessages.MachineInOtherDim.getUnlocalized());
        mc.thePlayer.closeScreen();
    }
}
