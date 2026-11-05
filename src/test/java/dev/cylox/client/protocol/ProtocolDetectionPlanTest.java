package dev.cylox.client.protocol;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProtocolDetectionPlanTest {
    @Test
    public void compatibilityPlanConsumesDetectedServerVersion() throws Exception {
        ServerStatus detected = ServerStatus.parse(
            "{\"version\":{\"name\":\"Paper 1.13.2\",\"protocol\":404}}"
        );
        ProtocolConnectionPlan plan =
            new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X).planFor(detected);

        assertEquals(ProtocolConnectionPlan.Mode.TRANSLATOR_MISSING, plan.getMode());
        assertEquals(404, plan.getServerProtocolId());
        assertEquals(ProtocolVersion.V1_13_2, plan.getServerVersion());
        assertFalse(plan.canConnectWithCurrentEngine());
    }

    @Test
    public void compatibilityPlanKeepsUnknownProtocolExplicit() throws Exception {
        ServerStatus detected = ServerStatus.parse(
            "{\"version\":{\"name\":\"Experimental Server\",\"protocol\":50000}}"
        );
        ProtocolConnectionPlan plan =
            new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X).planFor(detected);

        assertEquals(ProtocolConnectionPlan.Mode.UNKNOWN_PROTOCOL, plan.getMode());
        assertEquals(50000, plan.getServerProtocolId());
        assertEquals("Experimental Server", plan.getServerVersionName());
        assertNull(plan.getServerVersion());
        assertFalse(plan.canConnectWithCurrentEngine());
    }
}
