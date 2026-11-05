package dev.cylox.client.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Performs a Minecraft server-list status ping to discover the server's protocol.
 * This is detection only; it does not alter the client handshake or bypass server policy.
 */
public final class ServerStatusClient {
    private static final int MAX_FRAME_SIZE = 1024 * 1024;
    private static final int STATUS_PACKET_ID = 0;

    public ServerStatus query(String host, int port, int timeoutMillis) throws IOException {
        validate(host, port, timeoutMillis);
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            socket.setSoTimeout(timeoutMillis);
            OutputStream output = socket.getOutputStream();
            InputStream input = socket.getInputStream();

            byte[] handshake = new ProtocolHandshake(
                ProtocolVersion.V1_8_X.getProtocolId(), host, port, ProtocolState.STATUS
            ).encode();
            output.write(handshake);

            // Status Request: packet ID 0, with a one-byte frame body.
            output.write(1);
            output.write(STATUS_PACKET_ID);
            output.flush();

            int frameLength = readVarInt(input);
            if (frameLength <= 0 || frameLength > MAX_FRAME_SIZE) {
                throw new IOException("Invalid status response frame length: " + frameLength);
            }
            byte[] frame = readFully(input, frameLength);
            ByteArrayInputStream packet = new ByteArrayInputStream(frame);
            int packetId = readVarInt(packet);
            if (packetId != STATUS_PACKET_ID) {
                throw new IOException("Unexpected status response packet ID: " + packetId);
            }

            int jsonLength = readVarInt(packet);
            if (jsonLength < 0 || jsonLength > MAX_FRAME_SIZE || jsonLength > packet.available()) {
                throw new IOException("Invalid status JSON length: " + jsonLength);
            }
            byte[] jsonBytes = readFully(packet, jsonLength);
            if (packet.available() != 0) {
                throw new IOException("Unexpected trailing bytes in status response");
            }
            try {
                String json = new String(jsonBytes, StandardCharsets.UTF_8);
                return ServerStatus.parse(json);
            } catch (ProtocolTranslationException malformedStatus) {
                throw new IOException("Could not parse Minecraft server status", malformedStatus);
            }
        }
    }

    public ServerStatus query(String host, int port) throws IOException {
        return query(host, port, 3000);
    }

    private static void validate(String host, int port, int timeoutMillis) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("Server host is required");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Server port must be between 1 and 65535");
        }
        if (timeoutMillis < 1 || timeoutMillis > 60000) {
            throw new IllegalArgumentException("Timeout must be between 1 and 60000 milliseconds");
        }
    }

    private static int readVarInt(InputStream input) throws IOException {
        int result = 0;
        for (int index = 0; index < 5; index++) {
            int current = input.read();
            if (current < 0) throw new EOFException("Unexpected end of stream while reading VarInt");
            result |= (current & 0x7F) << (index * 7);
            if ((current & 0x80) == 0) return result;
        }
        throw new IOException("VarInt is longer than 5 bytes");
    }

    private static byte[] readFully(InputStream input, int length) throws IOException {
        byte[] result = new byte[length];
        int offset = 0;
        while (offset < length) {
            int count = input.read(result, offset, length - offset);
            if (count < 0) throw new EOFException("Unexpected end of Minecraft status packet");
            offset += count;
        }
        return result;
    }

    private static byte[] frame(byte[] packetBody) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(packetBody.length + 5);
        VarIntCodec.write(output, packetBody.length);
        output.write(packetBody, 0, packetBody.length);
        return output.toByteArray();
    }
}
