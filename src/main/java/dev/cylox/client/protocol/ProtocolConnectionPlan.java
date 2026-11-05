package dev.cylox.client.protocol;

/** An honest compatibility decision made before opening a server connection. */
public final class ProtocolConnectionPlan {
    public enum Mode { DIRECT, TRANSLATED, TRANSLATOR_MISSING }

    private final ProtocolVersion clientVersion;
    private final ProtocolVersion serverVersion;
    private final Mode mode;
    private final String translatorName;
    private final String explanation;

    ProtocolConnectionPlan(ProtocolVersion clientVersion, ProtocolVersion serverVersion,
                           Mode mode, String translatorName, String explanation) {
        this.clientVersion = clientVersion;
        this.serverVersion = serverVersion;
        this.mode = mode;
        this.translatorName = translatorName;
        this.explanation = explanation;
    }

    public ProtocolVersion getClientVersion() { return clientVersion; }
    public ProtocolVersion getServerVersion() { return serverVersion; }
    public Mode getMode() { return mode; }
    public String getTranslatorName() { return translatorName; }
    public String getExplanation() { return explanation; }

    public boolean canConnectWithCurrentEngine() {
        return mode == Mode.DIRECT || mode == Mode.TRANSLATED;
    }
}
