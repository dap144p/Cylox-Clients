package dev.cylox.client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class ServerStatusClientTest {
    @Test
    public void parsesStatusAndMapsKnownProtocol() throws Exception {
        String json = "{\"version\":{\"name\":\"Paper 1.13.2\",\"protocol\":404},"
            + "\"description\":{\"text\":\"Test\",\"extra\":[{\"text\":\" server\"}]},"
            + "\"players\":{\"max\":20,\"online\":3}}";
        ServerStatus status = ServerStatus.parse(json);
        assertEquals("Paper 1.13.2", status.getVersionName());
        assertEquals(404, status.getProtocolId());
        assertEquals(ProtocolVersion.V1_13_2, status.getKnownProtocolVersion());
        assertEquals("Test server", status.getDescription());
        assertEquals(3, status.getOnlinePlayers());
        assertEquals(20, status.getMaxPlayers());
    }

    @Test
    public void unknownProtocolIsReportedWithoutGuessing() throws Exception {
        ServerStatus status = ServerStatus.parse(
            "{\"version\":{\"name\":\"Future Server\",\"protocol\":99999}}"
        );
        assertNull(status.getKnownProtocolVersion());
        assertEquals(99999, status.getProtocolId());
    }

    @Test
    public void queriesStatusUsingMinecraftHandshake() throws Exception {
        final ServerSocket server = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        final AtomicReference<Throwable> serverFailure = new AtomicReference<Throwable>();
        Thread mockServer = new Thread(new Runnable() {
            @Override
            public void run() {
                try (Socket socket = server.accept()) {
                    socket.setSoTimeout(3000);
                    InputStream input = socket.getInputStream();
                    OutputStream output = socket.getOutputStream();

                    byte[] handshake = readFrame(input);
                    ByteArrayInputStreamPacket parsedHandshake = new ByteArrayInputStreamPacket(handshake);
                    assertEquals(0, parsedHandshake.readVarInt());
                    assertEquals(47, parsedHandshake.readVarInt());
                    assertEquals("127.0.0.1", parsedHandshake.readString());
                    assertEquals(server.getLocalPort(), parsedHandshake.readUnsignedShort());
                    assertEquals(1, parsedHandshake.readVarInt());

                    byte[] request = readFrame(input);
                    assertEquals(1, request.length);
                    assertEquals(0, request[0] & 0xFF);

                    String json = "{\"version\":{\"name\":\"Test 1.13.2\",\"protocol\":404}}";
                    ByteArrayOutputStream packet = new ByteArrayOutputStream();
                    VarIntCodec.write(packet, 0);
                    VarIntCodec.writeString(packet, json);
                    byte[] packetBytes = packet.toByteArray();
                    ByteArrayOutputStream framed = new ByteArrayOutputStream();
                    VarIntCodec.write(framed, packetBytes.length);
                    framed.write(packetBytes);
                    output.write(framed.toByteArray());
                    output.flush();
                } catch (Throwable failure) {
                    serverFailure.set(failure);
                } finally {
                    try { server.close(); } catch (Exception ignored) { }
                }
            }
        }, "Cylox-Mock-Status-Server");
        mockServer.setDaemon(true);
        mockServer.start();

        ServerStatus result = new ServerStatusClient().query(
            "127.0.0.1", server.getLocalPort(), 3000
        );
        mockServer.join(3000);
        assertFalse("Mock server thread did not finish", mockServer.isAlive());
        if (serverFailure.get() != null) throw new AssertionError(serverFailure.get());
        assertEquals("Test 1.13.2", result.getVersionName());
        assertEquals(404, result.getProtocolId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsInvalidPort() throws Exception {
        new ServerStatusClient().query("localhost", 0, 1000);
    }

    private static byte[] readFrame(InputStream input) throws Exception {
        int length = readVarInt(input);
        byte[] result = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = input.read(result, offset, length - offset);
            if (count < 0) throw new EOFException();
            offset += count;
        }
        return result;
    }

    private static int readVarInt(InputStream input) throws Exception {
        int value = 0;
        for (int shift = 0; shift < 35; shift += 7) {
            int current = input.read();
            if (current < 0) throw new EOFException();
            value |= (current & 0x7F) << shift;
            if ((current & 0x80) == 0) return value;
        }
        throw new IllegalStateException("Invalid test VarInt");
    }

    private static final class ByteArrayInputStreamPacket {
        private final java.io.ByteArrayInputStream input;
        ByteArrayInputStreamPacket(byte[] bytes) { input = new java.io.ByteArrayInputStream(bytes); }
        int readVarInt() throws Exception { return ServerStatusClientTest.readVarInt(input); }
        int readUnsignedShort() { return (input.read() << 8) | input.read(); }
        String readString() throws Exception {
            int length = readVarInt();
            byte[] bytes = new byte[length];
            int offset = 0;
            while (offset < length) {
                int count = input.read(bytes, offset, length - offset);
                if (count < 0) throw new EOFException();
                offset += count;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
