package reobf.proghatches.item.patterngen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import cpw.mods.fml.common.Loader;

/**
 * Named pattern-generator presets stored as one compressed NBT file per preset.
 * <p>
 * Files live under {@code config/proghatches/pattern_generator_presets} so they can be copied
 * between worlds and between players. Only plain settings are stored; live GT recipes, recipe
 * maps and other object references never enter a preset.
 */
public final class PatternGeneratorPresets {

    private static final String DIRECTORY = "pattern_generator_presets";
    private static final String EXTENSION = ".dat";
    private static final int MAX_NAME = 48;
    private static final long MAX_FILE_BYTES = 1L << 20;

    private PatternGeneratorPresets() {}

    public static final class Preset {

        public final String name;
        public final PatternGeneratorSettings settings;
        public final int machineMode;
        public final long updatedAt;

        private Preset(String name, PatternGeneratorSettings settings, int machineMode, long updatedAt) {
            this.name = name;
            this.settings = settings;
            this.machineMode = machineMode;
            this.updatedAt = updatedAt;
        }

        public static Preset fromSettings(String name, PatternGeneratorSettings settings, int machineMode) {
            PatternGeneratorSettings copy = new PatternGeneratorSettings();
            copy.copyConfigFrom(settings);
            return new Preset(safeName(name), copy, machineMode, System.currentTimeMillis());
        }

        public void applyTo(PatternGeneratorSettings target) {
            if (target == null) return;
            target.copyConfigFrom(this.settings);
        }

        NBTTagCompound toTag() {
            NBTTagCompound tag = this.settings.toTag();
            // A preset is configuration, not a paused fill queue.
            tag.removeTag("fillArmed");
            tag.removeTag("pendingRecipes");
            tag.removeTag("pendingTotal");
            tag.removeTag("pendingDim");
            tag.removeTag("pendingX");
            tag.removeTag("pendingY");
            tag.removeTag("pendingZ");
            tag.setString("name", this.name);
            tag.setInteger("machineMode", this.machineMode);
            tag.setLong("updatedAt", this.updatedAt);
            return tag;
        }

        static Preset fromTag(NBTTagCompound tag) {
            if (tag == null) return null;
            if (tag.getInteger("schema") != PatternGeneratorSettings.CURRENT_SCHEMA) return null;
            String rawName = tag.getString("name");
            if (rawName == null || rawName.trim()
                .isEmpty()) return null;
            String name = safeName(rawName);
            PatternGeneratorSettings settings = PatternGeneratorSettings.fromTag(tag);
            int machineMode = tag.getInteger("machineMode");
            long updatedAt = tag.getLong("updatedAt");
            return new Preset(name, settings, machineMode, updatedAt);
        }
    }

    public static File directory() {
        File config;
        try {
            config = Loader.instance()
                .getConfigDir();
        } catch (Throwable ignored) {
            config = new File("config");
        }
        return new File(new File(config, "proghatches"), DIRECTORY);
    }

    public static List<Preset> list() {
        return list(directory());
    }

    static List<Preset> list(File dir) {
        Map<String, Preset> byName = new LinkedHashMap<>();
        File[] files = dir == null ? null : dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file == null || !file.isFile() || !file.getName()
                    .endsWith(EXTENSION)) continue;
                Preset preset = read(file);
                if (preset == null) continue;
                Preset previous = byName.get(preset.name);
                if (previous == null || preset.updatedAt >= previous.updatedAt) byName.put(preset.name, preset);
            }
        }
        List<Preset> result = new ArrayList<>(byName.values());
        result.sort(
            Comparator.comparing((Preset preset) -> preset.name.toLowerCase())
                .thenComparing(preset -> preset.name));
        return result;
    }

    public static Preset load(String name) {
        return load(directory(), name);
    }

    static Preset load(File dir, String name) {
        if (dir == null || name == null || name.trim()
            .isEmpty()) return null;
        return read(fileFor(dir, name));
    }

    public static boolean save(Preset preset) {
        return save(directory(), preset);
    }

    static boolean save(File dir, Preset preset) {
        if (dir == null || preset == null) return false;
        File target = fileFor(dir, preset.name);
        File temporary = new File(dir, target.getName() + ".tmp");
        try {
            if (!dir.isDirectory() && !dir.mkdirs()) return false;
            NBTTagCompound tag = preset.toTag();
            tag.setString("name", safeName(preset.name));
            try (FileOutputStream out = new FileOutputStream(temporary)) {
                CompressedStreamTools.writeCompressed(tag, out);
            }
            try {
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (Throwable ignored) {
                Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Throwable ignored) {
            if (temporary.isFile()) temporary.delete();
            return false;
        }
    }

    public static boolean rename(String oldName, String newName) {
        return rename(directory(), oldName, newName);
    }

    static boolean rename(File dir, String oldName, String newName) {
        if (dir == null) return false;
        String safeOld = safeName(oldName);
        String safeNew = safeName(newName);
        if (safeOld.isEmpty() || safeNew.isEmpty()) return false;
        if (safeOld.equals(safeNew)) return true;
        if (fileFor(dir, safeNew).exists()) return false;
        Preset old = load(dir, safeOld);
        if (old == null) return false;
        Preset renamed = new Preset(
            safeNew,
            old.settings,
            old.machineMode,
            System.currentTimeMillis());
        if (!save(dir, renamed)) return false;
        return fileFor(dir, safeOld).delete();
    }

    public static boolean delete(String name) {
        return delete(directory(), name);
    }

    static boolean delete(File dir, String name) {
        if (dir == null || name == null) return false;
        File file = fileFor(dir, name);
        return file.isFile() && file.delete();
    }

    static String safeName(String name) {
        if (name == null) return "";
        String trimmed = name.trim();
        StringBuilder builder = new StringBuilder(Math.min(trimmed.length(), MAX_NAME));
        for (int i = 0; i < trimmed.length() && builder.length() < MAX_NAME; i++) {
            char c = trimmed.charAt(i);
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-' || c == '.') {
                builder.append(c);
            } else {
                builder.append('_');
            }
        }
        String safe = builder.toString()
            .trim();
        while (safe.endsWith(".")) safe = safe.substring(0, safe.length() - 1);
        if (safe.startsWith(".")) safe = "_" + safe;
        return safe.isEmpty() ? "preset" : safe;
    }

    private static File fileFor(File dir, String name) {
        return new File(dir, safeName(name) + EXTENSION);
    }

    private static Preset read(File file) {
        if (file == null || !file.isFile() || file.length() <= 0 || file.length() > MAX_FILE_BYTES) return null;
        try (FileInputStream in = new FileInputStream(file)) {
            return Preset.fromTag(CompressedStreamTools.readCompressed(in));
        } catch (Throwable ignored) {
            return null;
        }
    }
}
