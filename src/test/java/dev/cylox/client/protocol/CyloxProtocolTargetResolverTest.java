package dev.cylox.client.protocol;

import org.junit.Test;
import static org.junit.Assert.*;

public class CyloxProtocolTargetResolverTest {
    @Test
    public void manualSelectionIsUsedExactly() {
        assertEquals(107, CyloxProtocolTargetResolver.resolve(true, 107));
        assertEquals(404, CyloxProtocolTargetResolver.resolve(true, 404));
        assertEquals(763, CyloxProtocolTargetResolver.resolve(true, 763));
    }

    @Test
    public void disabledBridgeUsesNativeProtocol() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(false, 404));
    }

    @Test
    public void invalidProtocolUsesNativeProtocol() {
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, -1));
        assertEquals(47, CyloxProtocolTargetResolver.resolve(true, 0));
    }
}
