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
 * Applies a one-time, graphics-only stress-test preset to vanilla options.txt.
 * Existing user settings are backed up before the selected graphics values are
 * changed. The in-game Video Settings screen reads the same file.
 */
public final class CyloxGraphicsPreset {
    private static final String[] PRESET = new String[] {
        "fancyGraphics:true",
        "renderDistance:16",
        "ao:2",
        "particles:0",
        "entityShadows:true",
        "renderClouds:true",
        "mipmapLevels:4",
        "maxFps:260",
        "enableVsync:false"
    };

    private CyloxGraphicsPreset() { }

    public static void applyBenchmarkPresetOnce(File gameDirectory) {
        File root = gameDirectory == null ? new File(".") : gameDirectory;
        File cyloxDirectory = new File(root, "cylox");
        File marker = new File(cyloxDirectory, "graphics-max-preset-1.4.5.applied");
        if (marker.isFile()) return;

        File optionsFile = new File(root, "options.txt");
        File backupFile = new File(cyloxDirectory, "options-before-graphics-preset.txt");
        File temporaryFile = new File(root, "options.txt.cylox-tmp");

        try {
            if (!cyloxDirectory.isDirectory() && !cyloxDirectory.mkdirs()
                && !cyloxDirectory.isDirectory()) {
                throw new IllegalStateException("Could not create " + cyloxDirectory);
            }

            if (optionsFile.isFile() && !backupFile.isFile()) {
                copyFile(optionsFile, backupFile);
            }

            Map<String, String> values = new LinkedHashMap<String, String>();
            for (String entry : PRESET) {
                int separator = entry.indexOf(':');
                values.put(entry.substring(0, separator), entry.substring(separator + 1));
            }

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

            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(temporaryFile), "UTF-8"))) {
                for (String line : updated) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            if (optionsFile.exists() && !optionsFile.delete()) {
                throw new IllegalStateException("Could not replace " + optionsFile);
            }
            if (!temporaryFile.renameTo(optionsFile)) {
                if (backupFile.isFile()) copyFile(backupFile, optionsFile);
                throw new IllegalStateException("Could not finalize graphics preset");
            }
            if (!marker.createNewFile() && !marker.isFile()) {
                throw new IllegalStateException("Could not create preset marker");
            }

            System.out.println("[Cylox] Applied one-time max-graphics benchmark preset."
                + (backupFile.isFile() ? " Original settings backed up to "
                + backupFile.getAbsolutePath() + "." : ""));
        } catch (Throwable error) {
            if (temporaryFile.exists()) temporaryFile.delete();
            System.err.println("[Cylox] Could not apply graphics benchmark preset: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
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
