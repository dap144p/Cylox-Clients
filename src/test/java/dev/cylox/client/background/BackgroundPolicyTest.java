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
    public void skipsRenderingOnlyWhenNotForegroundUnlessExceptionIsEnabled() {
        assertTrue(BackgroundPolicy.shouldRender(true));
        assertFalse(BackgroundPolicy.shouldRender(false));
        assertTrue(BackgroundPolicy.shouldRender(false, true));
        assertFalse(BackgroundPolicy.shouldRender(false, false));
        assertTrue(BackgroundPolicy.shouldRender(true, false));
    }

    @Test
    public void capsBackgroundViewToSixBlocksOnlyWhenRenderExceptionIsEnabled() {
        assertEquals(96.0F, BackgroundPolicy.backgroundFarPlane(96.0F, true, true), 0.0001F);
        assertEquals(96.0F, BackgroundPolicy.backgroundFarPlane(96.0F, false, false), 0.0001F);
        assertEquals(6.0F, BackgroundPolicy.backgroundFarPlane(96.0F, false, true) * 1.41421356237F, 0.0001F);
        assertEquals(3.0F, BackgroundPolicy.backgroundFarPlane(3.0F, false, true), 0.0001F);
    }

    @Test
    public void pauseExceptionOnlyOverridesLostFocusWhileBackgrounded() {
        assertFalse(BackgroundPolicy.displayActiveForPause(false, false, false));
        assertTrue(BackgroundPolicy.displayActiveForPause(false, false, true));
        assertTrue(BackgroundPolicy.displayActiveForPause(true, false, false));
        assertFalse(BackgroundPolicy.displayActiveForPause(false, true, false));
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