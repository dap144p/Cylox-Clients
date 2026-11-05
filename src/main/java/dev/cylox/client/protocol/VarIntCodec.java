package dev.cylox.client.protocol;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;

/** Minecraft protocol VarInt and UTF-8 string primitives. */
public final class VarIntCodec {
    private VarIntCodec() { }

    public static byte[] encode(int value) {
        ByteArrayOutputStream output = new ByteArrayOutputStream(5);
        write(output, value);
        return output.toByteArray();
    }

    public static void write(ByteArrayOutputStream output, int value) {
        if (output == null) throw new IllegalArgumentException("Output is required");
        do {
            int part = value & 0x7F;
            value >>>= 7;
            if (value != 0) part |= 0x80;
            output.write(part);
        } while (value != 0);
    }

    public static int decode(byte[] bytes) throws IOException {
        if (bytes == null) throw new IllegalArgumentException("Bytes are required");
        int result = 0;
        int shift = 0;
        for (byte current : bytes) {
            int value = current & 0xFF;
            result |= (value & 0x7F) << shift;
            if ((value & 0x80) == 0) return result;
            shift += 7;
            if (shift >= 35) throw new IOException("VarInt is longer than 5 bytes");
        }
        throw new EOFException("Incomplete VarInt");
    }

    public static void writeString(ByteArrayOutputStream output, String value) {
        if (output == null || value == null) {
            throw new IllegalArgumentException("Output and string are required");
        }
        byte[] encoded = value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        write(output, encoded.length);
        output.write(encoded, 0, encoded.length);
    }
}
