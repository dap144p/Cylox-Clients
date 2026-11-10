package dev.cylox.client.background;

import org.junit.Test;
import static org.junit.Assert.*;

public class BackgroundPolicyTest {
    @Test
    public void preservesRequestedForegroundFrameLimit() {
        assertEquals(120, BackgroundPolicy.frameLimit(true, 120));
        assertEquals(30, BackgroundPolicy.frameLimit(true, 30));
    }

    @Test
    public void keepsBackgroundMainLoopAtTickSafeTwentyHertz() {
        assertEquals(20, BackgroundPolicy.frameLimit(false, 120));
        assertEquals(20, BackgroundPolicy.frameLimit(false, 30));
    }

    @Test
    public void skipsRenderingWhenDisplayIsNotForeground() {
        assertTrue(BackgroundPolicy.shouldRender(true));
        assertFalse(BackgroundPolicy.shouldRender(false));
    }

    @Test
    public void leftClickToggleNeverOverridesAnOpenScreen() {
        assertTrue(BackgroundPolicy.forceLeftClick(false, true, true));
        assertFalse(BackgroundPolicy.forceLeftClick(false, false, true));
        assertFalse(BackgroundPolicy.forceLeftClick(false, true, false));
        assertTrue(BackgroundPolicy.forceLeftClick(true, false, false));
    }

    @Test
    public void leftClickToggleClearsOnlyTheVanillaRegainFocusDelay() {
        assertEquals(0, BackgroundPolicy.leftClickCounter(10000, true, true));
        assertEquals(10000, BackgroundPolicy.leftClickCounter(10000, false, true));
        assertEquals(10000, BackgroundPolicy.leftClickCounter(10000, true, false));
        assertEquals(5, BackgroundPolicy.leftClickCounter(5, true, false));
    }
}
