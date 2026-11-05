package dev.cylox.client.protocol;

import org.junit.Test;
import static org.junit.Assert.*;

public class CyloxProtocolTargetResolverTest {
    @Test
    public void autoModeSelectsKnownNewerProtocol() {
        assertEquals(393, CyloxProtocolTargetResolver.resolve(true, "Auto", 393));
        assertEquals(404, CyloxProtocolTargetResolver.resolve(true, "Auto", 404));
    }

    @Test
    public void unknownProtocolFallsBackToNative() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, "Auto", 9999));
    }

    @Test
    public void nativeModeDoesNotTranslate() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, "Native", 404));
        assertEquals(47, CyloxProtocolTargetResolver.resolve(false, "Auto", 404));
    }
}
