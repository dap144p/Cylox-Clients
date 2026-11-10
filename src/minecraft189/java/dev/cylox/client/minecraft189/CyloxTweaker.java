package dev.cylox.client.minecraft189;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

/** Minimal LaunchWrapper entry point for the vanilla Minecraft 1.8.9 profile. */
public final class CyloxTweaker implements ITweaker {
    private List<String> launchArguments = Collections.emptyList();
    private File gameDirectory;

    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile) {
        launchArguments = args == null
            ? Collections.<String>emptyList()
            : new ArrayList<String>(args);
        gameDirectory = gameDir;
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("dev.cylox.client.");
        // Cylox is parent-loaded; force the Netty API and NIO channel classes to
        // resolve through the same loader or Channel.isInstance(candidate) is false.
        classLoader.addClassLoaderExclusion("io.netty.");
        classLoader.addTransformerExclusion("dev.cylox.client.");
        System.out.println("[Cylox] Netty package parent delegation enabled: io.netty.");
        // Apply the one-time stress baseline or migrate only untouched defaults before Minecraft reads options.txt.
        CyloxGraphicsPreset.applyBenchmarkPresetOnce(gameDirectory);
        // Register before ViaLoadingBase initializes protocols so the bundled
        // ViaRewind classes pass through Cylox's transformer on first load.
        classLoader.registerTransformer("dev.cylox.client.minecraft189.CyloxClientTransformer");
        CyloxProtocolBridge.initialize(gameDirectory);
        System.out.println("[Cylox] Background client 1.8.9 initialized.");
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        List<String> arguments = new ArrayList<String>(launchArguments);
        if (!hasOption(arguments, "--version")) {
            arguments.add("--version");
            arguments.add("Cylox-1.8.9");
            System.out.println("[Cylox] Added missing required Minecraft argument: --version Cylox-1.8.9");
        }
        int versionIndex = arguments.indexOf("--version");
        if (versionIndex >= 0 && versionIndex + 1 < arguments.size()) {
            System.out.println("[Cylox] Minecraft version argument: " + arguments.get(versionIndex + 1));
        } else {
            throw new IllegalStateException("[Cylox] Failed to supply required --version argument");
        }
        return arguments.toArray(new String[arguments.size()]);
    }

    private static boolean hasOption(List<String> arguments, String option) {
        for (String argument : arguments) {
            if (option.equals(argument)) return true;
        }
        return false;
    }
}
