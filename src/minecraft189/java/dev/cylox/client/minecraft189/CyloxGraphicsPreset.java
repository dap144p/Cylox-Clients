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
 * Safely removes the old one-time maximum-graphics benchmark overrides.
 * Only values that still match the old preset are restored from the saved
 * pre-benchmark options file. New installs and later user edits are untouched.
 */
public final class CyloxGraphicsPreset {
    private static final String LEGACY_PRESET_MARKER = "graphics-max-preset-1.4.5.applied";
    private static final String RESTORED_PRESET_MARKER = "graphics-max-preset-restored-1.5.5.applied";
    private static final String BACKUP_FILE = "options-before-graphics-preset.txt";
    private static final String[] LEGACY_PRESET = new String[] {
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

    public static void restoreBenchmarkPresetOnce(File gameDirectory) {
        File root = gameDirectory == null ? new File(".") : gameDirectory;
        File cyloxDirectory = new File(root, "cylox");
        File legacyMarker = new File(cyloxDirectory, LEGACY_PRESET_MARKER);
        File restoredMarker = new File(cyloxDirectory, RESTORED_PRESET_MARKER);
        if (!legacyMarker.isFile() || restoredMarker.isFile()) return;

        File optionsFile = new File(root, "options.txt");
        File backupFile = new File(cyloxDirectory, BACKUP_FILE);
        File temporaryFile = new File(root, "options.txt.cylox-restore-tmp");
        File currentBackupFile = new File(root, "options.txt.cylox-before-restore");

        if (!optionsFile.isFile()) {
            System.out.println("[Cylox] Skipped graphics preset migration because options.txt is missing.");
            return;
        }
        if (!backupFile.isFile()) {
            System.err.println("[Cylox] Skipped graphics preset migration because the original settings backup is missing; current settings were preserved.");
            return;
        }

        try {
            Map<String, String> originalValues = readValues(backupFile);
            Map<String, String> forcedValues = valuesFrom(LEGACY_PRESET);
            List<String> updated = new ArrayList<String>();
            Set<String> writtenKeys = new HashSet<String>();
            int restoredCount = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(optionsFile), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int separator = line.indexOf(':');
                    String key = separator < 0 ? "" : line.substring(0, separator);
                    String currentValue = separator < 0 ? "" : line.substring(separator + 1);
                    if (forcedValues.containsKey(key)
                        && forcedValues.get(key).equals(currentValue)) {
                        if (writtenKeys.add(key)) {
                            if (originalValues.containsKey(key)) {
                                updated.add(key + ":" + originalValues.get(key));
                            }
                            restoredCount++;
                        }
                    } else {
                        updated.add(line);
                    }
                }
            }

            if (restoredCount > 0) {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(new FileOutputStream(temporaryFile), "UTF-8"))) {
                    for (String line : updated) {
                        writer.write(line);
                        writer.newLine();
                    }
                }

                copyFile(optionsFile, currentBackupFile);
                if (!optionsFile.delete()) {
                    throw new IllegalStateException("Could not replace " + optionsFile);
                }
                if (!temporaryFile.renameTo(optionsFile)) {
                    if (currentBackupFile.isFile()) copyFile(currentBackupFile, optionsFile);
                    throw new IllegalStateException("Could not finalize restored graphics settings");
                }
            }

            if (!restoredMarker.createNewFile() && !restoredMarker.isFile()) {
                throw new IllegalStateException("Could not create graphics migration marker");
            }
            if (currentBackupFile.exists()) currentBackupFile.delete();
            System.out.println("[Cylox] Restored " + restoredCount
                + " unchanged benchmark graphics settings; later user edits were preserved.");
        } catch (Throwable error) {
            if (!optionsFile.exists() && currentBackupFile.isFile()) {
                try { copyFile(currentBackupFile, optionsFile); }
                catch (Throwable restoreError) {
                    System.err.println("[Cylox] Could not roll back options.txt: "
                        + restoreError.getClass().getSimpleName() + ": " + restoreError.getMessage());
                }
            }
            if (temporaryFile.exists()) temporaryFile.delete();
            System.err.println("[Cylox] Could not restore pre-benchmark graphics settings: "
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

    private static Map<String, String> readValues(File file) throws Exception {
        Map<String, String> values = new LinkedHashMap<String, String>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int separator = line.indexOf(':');
                if (separator > 0) values.put(line.substring(0, separator), line.substring(separator + 1));
            }
        }
        return values;
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
