package dev.cylox.client.protocol;

/**
 * Extension point for a real packet translator.
 * Implementations must translate both directions; a catalogue entry alone is not enough.
 */
public interface ProtocolTranslator {
    int getClientProtocolId();
    int getServerProtocolId();
    String getName();

    /** Server-to-client packet translation into the 1.8.x client protocol. */
    byte[] translateClientbound(int packetId, byte[] payload) throws ProtocolTranslationException;

    /** Client-to-server packet translation into the target server protocol. */
    byte[] translateServerbound(int packetId, byte[] payload) throws ProtocolTranslationException;
}
