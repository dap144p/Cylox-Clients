package dev.cylox.client.protocol;

/** An honest compatibility decision made before opening a server connection. */
public final class ProtocolConnectionPlan {
    public enum Mode { DIRECT, TRANSLATED, TRANSLATOR_MISSING, UNKNOWN_PROTOCOL }

    private final ProtocolVersion clientVersion;
    private final ProtocolVersion serverVersion;
    private final int serverProtocolId;
    private final String serverVersionName;
    private final Mode mode;
    private final String translatorName;
    private final String explanation;

    ProtocolConnectionPlan(ProtocolVersion clientVersion, ProtocolVersion serverVersion,
                           Mode mode, String translatorName, String explanation) {
        this(clientVersion, serverVersion,
            serverVersion == null ? -1 : serverVersion.getProtocolId(),
            serverVersion == null ? "Unknown" : serverVersion.getDisplayName(),
            mode, translatorName, explanation);
    }

    private ProtocolConnectionPlan(ProtocolVersion clientVersion, ProtocolVersion serverVersion,
                                   int serverProtocolId, String serverVersionName, Mode mode,
                                   String translatorName, String explanation) {
        this.clientVersion = clientVersion;
        this.serverVersion = serverVersion;
        this.serverProtocolId = serverProtocolId;
        this.serverVersionName = serverVersionName;
        this.mode = mode;
        this.translatorName = translatorName;
        this.explanation = explanation;
    }

    static ProtocolConnectionPlan unknown(ProtocolVersion clientVersion, int protocolId, String versionName) {
        return new ProtocolConnectionPlan(clientVersion, null, protocolId, versionName,
            Mode.UNKNOWN_PROTOCOL, null,
            "The server advertises protocol " + protocolId + " (" + versionName
                + "), which is not in this build's protocol catalogue.");
    }

    public ProtocolVersion getClientVersion() { return clientVersion; }
    /** Null when the server advertises a protocol not yet in the catalogue. */
    public ProtocolVersion getServerVersion() { return serverVersion; }
    public int getServerProtocolId() { return serverProtocolId; }
    public String getServerVersionName() { return serverVersionName; }
    public Mode getMode() { return mode; }
    public String getTranslatorName() { return translatorName; }
    public String getExplanation() { return explanation; }

    public boolean canConnectWithCurrentEngine() {
        return mode == Mode.DIRECT || mode == Mode.TRANSLATED;
    }
}
