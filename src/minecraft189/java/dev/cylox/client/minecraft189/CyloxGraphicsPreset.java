package dev.cylox.client.minecraft189;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies a one-time low-load graphics preset for mobile play.
 * Existing settings are backed up once; subsequent manual edits are never overwritten.
 */
public final class CyloxGraphicsPreset {
    private static final String PRESET_MARKER = "graphics-lightweight-preset-1.5.13.applied";
    private static final String BACKUP_FILE = "options-before-lightweight-preset-1.5.13.txt";
    private static final String[] PRESET = new String[] {
        "renderDistance:2",
        "fancyGraphics:false",
        "ao:0",
        "particles:2",
        "entityShadows:false",
        "renderClouds:false",
        "mipmapLevels:0",
        "maxFps:60",
        "enableVsync:false"
    };

    private CyloxGraphicsPreset() { }

    public static void applyLightweightPresetOnce(File gameDirectory) {
        File root = gameDirectory == null ? new File(".") : gameDirectory;
        File cyloxDirectory = new File(root, "cylox");
        File presetMarker = new File(cyloxDirectory, PRESET_MARKER);

        if (presetMarker.isFile()) return;
        applyFreshLightweightPresetOnce(root, cyloxDirectory, presetMarker);
    }

    private static void applyFreshLightweightPresetOnce(
            File root, File cyloxDirectory, File presetMarker) {
        File optionsFile = new File(root, "options.txt");
        File backupFile = new File(cyloxDirectory, BACKUP_FILE);
        File temporaryFile = new File(root, "options.txt.cylox-light-tmp");

        try {
            if (!cyloxDirectory.isDirectory() && !cyloxDirectory.mkdirs()
                && !cyloxDirectory.isDirectory()) {
                throw new IllegalStateException("Could not create " + cyloxDirectory);
            }

            if (optionsFile.isFile() && !backupFile.isFile()) {
                copyFile(optionsFile, backupFile);
            }

            Map<String, String> values = valuesFrom(PRESET);
            List<String> updated = new ArrayList<String>();
            Set<String> writtenKeys = new HashSet<String>();
            if (optionsFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(optionsFile), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        int separator = line.indexOf(':');
                        String key = separator < 0 ? "" : line.substring(0, separator);
                        if (values.containsKey(key)) {
                            if (writtenKeys.add(key)) updated.add(key + ":" + values.get(key));
                        } else {
                            updated.add(line);
                        }
                    }
                }
            }

            for (Map.Entry<String, String> entry : values.entrySet()) {
                if (writtenKeys.add(entry.getKey())) {
                    updated.add(entry.getKey() + ":" + entry.getValue());
                }
            }

            writeLines(temporaryFile, updated);
            if (optionsFile.exists() && !optionsFile.delete()) {
                throw new IllegalStateException("Could not replace " + optionsFile);
            }
            if (!temporaryFile.renameTo(optionsFile)) {
                if (backupFile.isFile()) copyFile(backupFile, optionsFile);
                throw new IllegalStateException("Could not finalize lightweight graphics preset");
            }
            if (!presetMarker.createNewFile() && !presetMarker.isFile()) {
                throw new IllegalStateException("Could not create preset marker");
            }

            System.out.println("[Cylox] Applied one-time lightweight graphics preset."
                + (backupFile.isFile() ? " Previous settings backed up to "
                + backupFile.getAbsolutePath() + "." : ""));
        } catch (Throwable error) {
            if (temporaryFile.exists()) temporaryFile.delete();
            System.err.println("[Cylox] Could not apply lightweight graphics preset: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    private static Map<String, String> valuesFrom(String[] entries) {
        Map<String, String> values = new LinkedHashMap<String, String>();
        for (String entry : entries) {
            int separator = entry.indexOf(':');
            if (separator > 0) {
                values.put(entry.substring(0, separator), entry.substring(separator + 1));
            }
        }
        return values;
    }

    private static void writeLines(File file, List<String> lines) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    private static void copyFile(File source, File destination) throws Exception {
        try (InputStream input = new FileInputStream(source);
             FileOutputStream output = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
            output.flush();
        }
    }
}
