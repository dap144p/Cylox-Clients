package dev.cylox.client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProtocolCoreTest {
    @Test
    public void varIntRoundTripsBoundaryValues() throws Exception {
        int[] values = {0, 1, 127, 128, 255, 25565, Integer.MAX_VALUE, -1};
        for (int value : values) {
            assertEquals(value, VarIntCodec.decode(VarIntCodec.encode(value)));
        }
    }

    @Test(expected = IOException.class)
    public void varIntRejectsIncompleteValue() throws Exception {
        VarIntCodec.decode(new byte[] {(byte) 0x80});
    }

    @Test(expected = IOException.class)
    public void varIntRejectsMoreThanFiveBytes() throws Exception {
        VarIntCodec.decode(new byte[] {
            (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01
        });
    }

    @Test
    public void handshakeContainsFrameAndProtocolFields() throws Exception {
        byte[] packet = new ProtocolHandshake(47, "localhost", 25565, ProtocolState.LOGIN).encode();
        ByteArrayOutputStream expectedPrefix = new ByteArrayOutputStream();
        VarIntCodec.write(expectedPrefix, 0);
        VarIntCodec.write(expectedPrefix, 47);
        VarIntCodec.writeString(expectedPrefix, "localhost");
        expectedPrefix.write(0x63);
        expectedPrefix.write(0xDD);
        VarIntCodec.write(expectedPrefix, 2);
        byte[] body = expectedPrefix.toByteArray();
        assertEquals(body.length, VarIntCodec.decode(new byte[] {packet[0]}));
        assertEquals(body.length + 1, packet.length);
        for (int i = 0; i < body.length; i++) assertEquals(body[i], packet[i + 1]);
    }

    @Test
    public void protocolCatalogueRecognizesOneEightAndOneThirteen() {
        assertEquals(47, ProtocolVersion.V1_8_X.getProtocolId());
        assertEquals(393, ProtocolVersion.V1_13.getProtocolId());
        assertEquals(ProtocolVersion.V1_13, ProtocolVersion.fromProtocolId(393));
        assertNull(ProtocolVersion.fromProtocolId(99999));
    }

    @Test
    public void engineAllowsNativeOneEightButDoesNotFakeOneThirteenSupport() {
        ProtocolCompatibilityEngine engine =
            new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X);
        ProtocolConnectionPlan nativePlan = engine.planFor(ProtocolVersion.V1_8_X);
        assertEquals(ProtocolConnectionPlan.Mode.DIRECT, nativePlan.getMode());
        assertTrue(nativePlan.canConnectWithCurrentEngine());

        ProtocolConnectionPlan newerPlan = engine.planFor(ProtocolVersion.V1_13);
        assertEquals(ProtocolConnectionPlan.Mode.TRANSLATOR_MISSING, newerPlan.getMode());
        assertFalse(newerPlan.canConnectWithCurrentEngine());
    }

    @Test
    public void translatorRegistrationChangesTheCompatibilityPlan() {
        ProtocolCompatibilityEngine engine =
            new ProtocolCompatibilityEngine(ProtocolVersion.V1_8_X);
        engine.registerTranslator(new ProtocolTranslator() {
            public int getClientProtocolId() { return 47; }
            public int getServerProtocolId() { return 393; }
            public String getName() { return "Test 1.8-to-1.13 adapter"; }
            public byte[] translateClientbound(int packetId, byte[] payload) { return payload; }
            public byte[] translateServerbound(int packetId, byte[] payload) { return payload; }
        });
        ProtocolConnectionPlan plan = engine.planFor(ProtocolVersion.V1_13);
        assertEquals(ProtocolConnectionPlan.Mode.TRANSLATED, plan.getMode());
        assertTrue(plan.canConnectWithCurrentEngine());
    }
}
