package dev.cylox.launch;

import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Vanilla 1.8.9 entry point for the Cylox runtime.
 * It does not load Forge, FML, Fabric, or a third-party mod loader.
 */
public final class CyloxTweaker implements ITweaker {
    private String[] launchArguments = new String[0];

    @Override
    public void acceptOptions(List<String> arguments, File gameDirectory, File assetsDirectory, String profile) {
        List<String> safeArguments = arguments == null
                ? new ArrayList<String>()
                : new ArrayList<String>(arguments);
        launchArguments = safeArguments.toArray(new String[safeArguments.size()]);
    }

    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader) {
        classLoader.addClassLoaderExclusion("dev.cylox.");
        classLoader.registerTransformer(CyloxMainTransformer.class.getName());
    }

    @Override
    public String getLaunchTarget() {
        return "net.minecraft.client.main.Main";
    }

    @Override
    public String[] getLaunchArguments() {
        return launchArguments.clone();
    }
}
