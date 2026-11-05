package dev.cylox.client.protocol;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/** Builds a framed Minecraft handshake packet (packet id 0x00). */
public final class ProtocolHandshake {
    private final int protocolId;
    private final String serverAddress;
    private final int serverPort;
    private final ProtocolState nextState;

    public ProtocolHandshake(int protocolId, String serverAddress, int serverPort, ProtocolState nextState) {
        if (protocolId < 0) throw new IllegalArgumentException("Protocol id cannot be negative");
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Server address is required");
        }
        if (serverAddress.getBytes(StandardCharsets.UTF_8).length > 255) {
            throw new IllegalArgumentException("Server address is longer than 255 UTF-8 bytes");
        }
        if (serverPort < 0 || serverPort > 65535) {
            throw new IllegalArgumentException("Server port must be between 0 and 65535");
        }
        if (nextState == null) throw new IllegalArgumentException("Next protocol state is required");
        this.protocolId = protocolId;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.nextState = nextState;
    }

    public int getProtocolId() { return protocolId; }
    public String getServerAddress() { return serverAddress; }
    public int getServerPort() { return serverPort; }
    public ProtocolState getNextState() { return nextState; }

    /** Returns the packet including its VarInt frame length prefix. */
    public byte[] encode() {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        VarIntCodec.write(body, 0);
        VarIntCodec.write(body, protocolId);
        VarIntCodec.writeString(body, serverAddress);
        body.write((serverPort >>> 8) & 0xFF);
        body.write(serverPort & 0xFF);
        VarIntCodec.write(body, nextState.getHandshakeValue());

        ByteArrayOutputStream framed = new ByteArrayOutputStream(body.size() + 5);
        VarIntCodec.write(framed, body.size());
        byte[] packetBody = body.toByteArray();
        framed.write(packetBody, 0, packetBody.length);
        return framed.toByteArray();
    }
}
