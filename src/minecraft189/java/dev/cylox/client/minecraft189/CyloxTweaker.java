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
        classLoader.addTransformerExclusion("dev.cylox.client.");
        CyloxProtocolBridge.initialize(gameDirectory);
        classLoader.registerTransformer("dev.cylox.client.minecraft189.CyloxClientTransformer");
        System.out.println("[Cylox] Background client 1.8.9 initialized.");
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
