package dev.cylox.client.minecraft189;

import dev.cylox.client.runtime.CyloxRuntime;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

/**
 * LaunchWrapper entry point for Cylox Client 1.8.9.
 * The launcher profile must use net.minecraft.launchwrapper.Launch as mainClass
 * and pass --tweakClass dev.cylox.client.minecraft189.CyloxTweaker.
 */
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
        classLoader.addTransformerExclusion("dev.cylox.client.");
        CyloxRuntime.initialize(gameDirectory);
        classLoader.registerTransformer("dev.cylox.client.minecraft189.CyloxClientTransformer");
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.toArray(new String[launchArguments.size()]);
    }
}
