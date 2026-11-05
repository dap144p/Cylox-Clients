package dev.cylox.client.protocol;

public final class ProtocolTranslationException extends Exception {
    public ProtocolTranslationException(String message) { super(message); }
    public ProtocolTranslationException(String message, Throwable cause) { super(message, cause); }
}
