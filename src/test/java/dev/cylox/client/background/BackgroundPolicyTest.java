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
    public void skipsRenderingOnlyWhenNotForeground() {
        assertTrue(BackgroundPolicy.shouldRender(true));
        assertFalse(BackgroundPolicy.shouldRender(false));
    }
}
