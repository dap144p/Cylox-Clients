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

        // Some Android launchers omit --version when translating legacy
        // minecraftArguments metadata. Vanilla 1.8.9 requires this option.
        if (!hasVersionArgument(safeArguments)) {
            String version = profile == null || profile.trim().isEmpty()
                    ? "Cylox-1.8.9"
                    : profile;
            safeArguments.add("--version");
            safeArguments.add(version);
            System.out.println("[Cylox Client] Added missing required --version argument: " + version);
        }

        launchArguments = safeArguments.toArray(new String[safeArguments.size()]);
    }

    private static boolean hasVersionArgument(List<String> arguments) {
        for (String argument : arguments) {
            if ("--version".equals(argument) || (argument != null && argument.startsWith("--version="))) {
                return true;
            }
        }
        return false;
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
