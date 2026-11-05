package dev.cylox.client.protocol;

/** The two legal next-state values used by a Minecraft handshake packet. */
public enum ProtocolState {
    STATUS(1), LOGIN(2);

    private final int handshakeValue;

    ProtocolState(int handshakeValue) { this.handshakeValue = handshakeValue; }
    public int getHandshakeValue() { return handshakeValue; }
}
