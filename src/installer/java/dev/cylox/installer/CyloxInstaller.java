package dev.cylox.installer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class CyloxInstaller {
    private static final String PROFILE = "versions/Cylox-1.8.9/Cylox-1.8.9.json";
    private static final String CLIENT_PREFIX = "libraries/dev/cylox/client/";
    private static String version;
    private static String payloadName;

    private CyloxInstaller() { }

    public static void main(String[] args) {
        try {
            Manifest manifest = readManifest();
            Attributes attributes = manifest.getMainAttributes();
            version = requiredAttribute(attributes, "Cylox-Version");
            payloadName = requiredAttribute(attributes, "Cylox-Payload");
            final File target = chooseGameDirectory(args);
            if (target == null) return;
            int answer = JOptionPane.showConfirmDialog(null,
                "Install Cylox Client 1.8.9 v" + version + " into:\n" + target.getAbsolutePath()
                    + "\n\nOnly this version profile, its matching client JAR and license files will be installed.",
                "Cylox Installer " + version, JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) return;
            List<String> installed = install(target.toPath());
            JOptionPane.showMessageDialog(null,
                "Installation completed.\nVersion: Cylox-1.8.9\nBuild: " + version
                    + "\nFiles: " + installed.size()
                    + "\n\nRefresh the version list in MJ Launcher and select Cylox-1.8.9.",
                "Cylox Installer", JOptionPane.INFORMATION_MESSAGE);
        } catch (Throwable error) {
            String message = error.getMessage() == null ? error.toString() : error.getMessage();
            try {
                JOptionPane.showMessageDialog(null, "Installation failed:\n" + message,
                    "Cylox Installer", JOptionPane.ERROR_MESSAGE);
            } catch (Throwable ignored) {
                System.err.println("[Cylox Installer] " + message);
            }
        }
    }

    private static Manifest readManifest() throws Exception {
        InputStream input = CyloxInstaller.class.getResourceAsStream("/META-INF/MANIFEST.MF");
        if (input == null) throw new IllegalStateException("Installer manifest is missing.");
        try { return new Manifest(input); } finally { input.close(); }
    }

    private static String requiredAttribute(Attributes attributes, String name) {
        String value = attributes.getValue(name);
        if (value == null || value.trim().isEmpty()) throw new IllegalStateException("Missing installer attribute: " + name);
        return value.trim();
    }

    private static File chooseGameDirectory(String[] args) throws Exception {
        List<File> candidates = new ArrayList<File>();
        if (args != null && args.length > 0) candidates.add(new File(args[0]));
        addCandidate(candidates, System.getProperty("user.dir"));
        addCandidate(candidates, System.getProperty("user.home"));
        addCandidate(candidates, System.getenv("POJAV_GAME_DIR"));
        addCandidate(candidates, System.getenv("MINECRAFT_DIR"));
        for (File candidate : candidates) {
            File resolved = normalizeCandidate(candidate);
            if (isGameDirectory(resolved)) return resolved;
        }

        JFileChooser chooser = new JFileChooser(new File(System.getProperty("user.dir", "/storage/emulated/0")));
        chooser.setDialogTitle("Select MJ Launcher game directory (.minecraft)");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setApproveButtonText("Use this folder");
        int result = chooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) return null;
        File selected = chooser.getSelectedFile().getCanonicalFile();
        if (!selected.isDirectory()) throw new IllegalArgumentException("Selected path is not a directory.");
        return selected;
    }

    private static void addCandidate(List<File> candidates, String value) {
        if (value != null && !value.trim().isEmpty()) candidates.add(new File(value));
    }

    private static File normalizeCandidate(File candidate) throws Exception {
        File canonical = candidate.getCanonicalFile();
        if (canonical.getName().equals(".minecraft")) return canonical;
        File nested = new File(canonical, ".minecraft");
        if (nested.isDirectory()) return nested.getCanonicalFile();
        return canonical;
    }

    private static boolean isGameDirectory(File directory) {
        return directory != null && directory.isDirectory()
            && (new File(directory, "versions").isDirectory()
                || new File(directory, "libraries").isDirectory()
                || new File(directory, "assets").isDirectory());
    }

    private static List<String> install(Path gameDir) throws Exception {
        Path root = gameDir.toRealPath();
        List<String> installed = new ArrayList<String>();
        InputStream resource = CyloxInstaller.class.getResourceAsStream("/payload/" + payloadName);
        if (resource == null) throw new IllegalStateException("Embedded version-matched ZIP payload is missing.");
        ZipInputStream zip = new ZipInputStream(new BufferedInputStream(resource));
        try {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().replace('\\\\', '/');
                if (!isAllowedEntry(name)) continue;
                Path destination = root.resolve(name).normalize();
                if (!destination.startsWith(root)) throw new SecurityException("Unsafe package path: " + name);
                Path parent = destination.getParent();
                if (parent == null) throw new SecurityException("Invalid package path: " + name);
                Files.createDirectories(parent);
                Path temporary = Files.createTempFile(parent, ".cylox-", ".installing");
                try {
                    copy(zip, temporary);
                    if (Files.exists(destination) && sameFile(destination, temporary)) {
                        Files.deleteIfExists(temporary);
                        installed.add(name + " (unchanged)");
                        continue;
                    }
                    Path backup = null;
                    if (Files.exists(destination)) {
                        backup = root.resolve(".cylox-installer-backup").resolve(version).resolve(name).normalize();
                        if (!backup.startsWith(root)) throw new SecurityException("Unsafe backup path.");
                        Files.createDirectories(backup.getParent());
                        if (Files.exists(backup)) {
                            backup = backup.resolveSibling(backup.getFileName().toString() + "." + System.currentTimeMillis());
                        }
                        Files.move(destination, backup);
                    }
                    try {
                        Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (Exception moveFailure) {
                        if (backup != null && !Files.exists(destination) && Files.exists(backup)) {
                            Files.move(backup, destination, StandardCopyOption.REPLACE_EXISTING);
                        }
                        throw moveFailure;
                    }
                    installed.add(name);
                } finally {
                    Files.deleteIfExists(temporary);
                }
            }
        } finally {
            zip.close();
        }
        if (installed.isEmpty()) throw new IllegalStateException("The embedded ZIP did not contain any installable files.");
        return installed;
    }

    private static boolean isAllowedEntry(String name) {
        if (name.equals(PROFILE) || name.equals("licenses/LICENSE")
            || name.equals("licenses/THIRD_PARTY_NOTICES.md")
            || name.equals("licenses/CYLOX-INSTALL.txt")) return true;
        String expectedJar = CLIENT_PREFIX + version + "/Cylox-Client-1.8.9-" + version + ".jar";
        return name.equals(expectedJar);
    }

    private static void copy(InputStream input, Path output) throws Exception {
        FileOutputStream stream = new FileOutputStream(output.toFile());
        try {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) stream.write(buffer, 0, count);
            stream.getFD().sync();
        } finally {
            stream.close();
        }
    }

    private static boolean sameFile(Path first, Path second) throws Exception {
        if (Files.size(first) != Files.size(second)) return false;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] left = hash(first, digest);
        byte[] right = hash(second, MessageDigest.getInstance("SHA-256"));
        if (left.length != right.length) return false;
        for (int i = 0; i < left.length; i++) if (left[i] != right[i]) return false;
        return true;
    }

    private static byte[] hash(Path path, MessageDigest digest) throws Exception {
        InputStream input = new BufferedInputStream(new FileInputStream(path.toFile()));
        try {
            byte[] buffer = new byte[8192];
            int count;
            while ((count = input.read(buffer)) != -1) digest.update(buffer, 0, count);
            return digest.digest();
        } finally {
            input.close();
        }
    }
}
