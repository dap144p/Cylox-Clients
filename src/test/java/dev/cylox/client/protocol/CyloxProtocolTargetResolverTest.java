package dev.cylox.client.protocol;

import org.junit.Test;
import static org.junit.Assert.*;

public class CyloxProtocolTargetResolverTest {
    @Test
    public void autoModeSelectsCataloguedNewerProtocols() {
        assertEquals(107, CyloxProtocolTargetResolver.resolve(true, "Auto", 107));
        assertEquals(393, CyloxProtocolTargetResolver.resolve(true, "Auto", 393));
        assertEquals(404, CyloxProtocolTargetResolver.resolve(true, "Auto", 404));
    }

    @Test
    public void nativeAndDisabledModesKeepProtocol47() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, "Native", 404));
        assertEquals(47, CyloxProtocolTargetResolver.resolve(false, "Auto", 404));
    }

    @Test
    public void unknownProtocolsSafelyFallBackToNative() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, "Auto", 9999));
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, "Unknown", 404));
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, null, 404));
    }
}
