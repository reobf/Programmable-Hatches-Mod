package reobf.proghatches.main;

import java.io.File;
import java.util.Locale;

import reobf.proghatches.Tags;

public class JarNameCheck {

    public static void check(File source) {
        if (source == null || !source.isFile()) return;
        String name = source.getName();
        if (name.toLowerCase(Locale.ROOT)
            .startsWith("p")) return;
        String expected = "programmablehatches-" + Tags.VERSION + ".jar";
        String message = String.join(
            System.lineSeparator(),
            "The Programmable Hatches jar has been renamed to \"" + name + "\".",
            "Its file name must start with \"p\", e.g. " + expected + ".",
            "Forge loads mods in file name order. A renamed jar can make GregTech load before Et Futurum Requiem,",
            "which removes recipes such as the Small Coal Boiler and the Bricked Blast Furnace.",
            "Rename the file back and restart the game.");
        MyMod.LOG.fatal(message);
        throw new IllegalStateException(message);
    }
}
