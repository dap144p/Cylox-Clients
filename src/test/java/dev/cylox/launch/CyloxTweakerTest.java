package dev.cylox.launch;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public final class CyloxTweakerTest {
    @Test
    public void addsRequiredVersionWhenAndroidLauncherOmitsIt() {
        CyloxTweaker tweaker = new CyloxTweaker();
        tweaker.acceptOptions(Collections.<String>emptyList(), new File("."), new File("assets"), "Cylox-1.8.9");
        assertArrayEquals(new String[] {"--version", "Cylox-1.8.9"}, tweaker.getLaunchArguments());
    }

    @Test
    public void preservesExistingVersionArgument() {
        CyloxTweaker tweaker = new CyloxTweaker();
        tweaker.acceptOptions(Arrays.asList("--username", "Player", "--version", "1.8.9"),
                new File("."), new File("assets"), "Cylox-1.8.9");
        assertArrayEquals(new String[] {"--username", "Player", "--version", "1.8.9"},
                tweaker.getLaunchArguments());
    }

    @Test
    public void recognizesEqualsVersionSyntax() {
        CyloxTweaker tweaker = new CyloxTweaker();
        tweaker.acceptOptions(Collections.singletonList("--version=1.8.9"),
                new File("."), new File("assets"), "Cylox-1.8.9");
        assertTrue(Arrays.asList(tweaker.getLaunchArguments()).contains("--version=1.8.9"));
    }
}
