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
 * Applies a one-time maximum-graphics preset as the rendering stress-test baseline.
 * Existing settings are backed up once; subsequent manual edits are never overwritten.
 * Also repairs only untouched values reverted by the 1.5.5 migration.
 */
public final class CyloxGraphicsPreset {
    private static final String PRESET_MARKER = "graphics-max-preset-1.4.5.applied";
    private static final String RESTORED_PRESET_MARKER = "graphics-max-preset-restored-1.5.5.applied";
    private static final String REAPPLIED_PRESET_MARKER = "graphics-max-preset-reapplied-1.5.6.applied";
    private static final String BACKUP_FILE = "options-before-graphics-preset.txt";
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
        File presetMarker = new File(cyloxDirectory, PRESET_MARKER);
        File restoredMarker = new File(cyloxDirectory, RESTORED_PRESET_MARKER);
        File reappliedMarker = new File(cyloxDirectory, REAPPLIED_PRESET_MARKER);

        if (presetMarker.isFile()) {
            // v1.5.5 restored some old settings. Reapply only keys still equal to
            // their pre-benchmark backup; values changed after that migration stay intact.
            if (restoredMarker.isFile() && !reappliedMarker.isFile()) {
                reapplyAfter155MigrationOnce(root, cyloxDirectory, reappliedMarker);
            }
            return;
        }

        applyFreshBenchmarkPresetOnce(root, cyloxDirectory, presetMarker);
    }

    private static void applyFreshBenchmarkPresetOnce(
            File root, File cyloxDirectory, File presetMarker) {
        File optionsFile = new File(root, "options.txt");
        File backupFile = new File(cyloxDirectory, BACKUP_FILE);
        File temporaryFile = new File(root, "options.txt.cylox-tmp");

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
                throw new IllegalStateException("Could not finalize graphics preset");
            }
            if (!presetMarker.createNewFile() && !presetMarker.isFile()) {
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

    private static void reapplyAfter155MigrationOnce(
            File root, File cyloxDirectory, File reappliedMarker) {
        File optionsFile = new File(root, "options.txt");
        File backupFile = new File(cyloxDirectory, BACKUP_FILE);
        File temporaryFile = new File(root, "options.txt.cylox-reapply-tmp");
        File currentBackupFile = new File(root, "options.txt.cylox-before-reapply");

        if (!optionsFile.isFile() || !backupFile.isFile()) {
            System.err.println("[Cylox] Skipped benchmark-preset repair because options.txt or its original backup is missing.");
            return;
        }

        try {
            Map<String, String> originalValues = readValues(backupFile);
            Map<String, String> presetValues = valuesFrom(PRESET);
            List<String> updated = new ArrayList<String>();
            Set<String> writtenKeys = new HashSet<String>();
            int reappliedCount = 0;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(optionsFile), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    int separator = line.indexOf(':');
                    String key = separator < 0 ? "" : line.substring(0, separator);
                    String currentValue = separator < 0 ? "" : line.substring(separator + 1);
                    if (presetValues.containsKey(key) && originalValues.containsKey(key)
                        && originalValues.get(key).equals(currentValue)) {
                        if (writtenKeys.add(key)) {
                            String presetValue = presetValues.get(key);
                            updated.add(key + ":" + presetValue);
                            if (!presetValue.equals(currentValue)) reappliedCount++;
                        }
                    } else {
                        updated.add(line);
                    }
                }
            }

            if (reappliedCount > 0) {
                writeLines(temporaryFile, updated);
                copyFile(optionsFile, currentBackupFile);
                if (!optionsFile.delete()) {
                    throw new IllegalStateException("Could not replace " + optionsFile);
                }
                if (!temporaryFile.renameTo(optionsFile)) {
                    if (currentBackupFile.isFile()) copyFile(currentBackupFile, optionsFile);
                    throw new IllegalStateException("Could not finalize benchmark-preset repair");
                }
            }

            if (!reappliedMarker.createNewFile() && !reappliedMarker.isFile()) {
                throw new IllegalStateException("Could not create benchmark-preset repair marker");
            }
            if (currentBackupFile.exists()) currentBackupFile.delete();
            System.out.println("[Cylox] Reapplied " + reappliedCount
                + " unchanged benchmark settings from the v1.5.5 migration; later edits were preserved.");
        } catch (Throwable error) {
            if (!optionsFile.exists() && currentBackupFile.isFile()) {
                try { copyFile(currentBackupFile, optionsFile); }
                catch (Throwable restoreError) {
                    System.err.println("[Cylox] Could not roll back options.txt: "
                        + restoreError.getClass().getSimpleName() + ": " + restoreError.getMessage());
                }
            }
            if (temporaryFile.exists()) temporaryFile.delete();
            System.err.println("[Cylox] Could not repair the v1.5.5 benchmark preset: "
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
