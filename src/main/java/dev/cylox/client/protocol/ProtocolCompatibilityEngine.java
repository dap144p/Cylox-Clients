package dev.cylox.client.protocol;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cylox protocol compatibility registry.
 * The initial runtime speaks protocol 47 (Minecraft 1.8.x). Newer protocols are
 * not considered compatible until a complete bidirectional packet translator is registered.
 */
public final class ProtocolCompatibilityEngine {
    private final ProtocolVersion clientVersion;
    private final Map<Integer, ProtocolTranslator> translators =
        new HashMap<Integer, ProtocolTranslator>();

    public ProtocolCompatibilityEngine(ProtocolVersion clientVersion) {
        if (clientVersion == null) throw new IllegalArgumentException("Client version is required");
        this.clientVersion = clientVersion;
    }

    public ProtocolVersion getClientVersion() { return clientVersion; }

    public synchronized void registerTranslator(ProtocolTranslator translator) {
        if (translator == null) throw new IllegalArgumentException("Translator is required");
        if (translator.getClientProtocolId() != clientVersion.getProtocolId()) {
            throw new IllegalArgumentException("Translator client protocol does not match this client");
        }
        if (translator.getServerProtocolId() == clientVersion.getProtocolId()) {
            throw new IllegalArgumentException("A translator is not needed for an identical protocol");
        }
        if (translator.getName() == null || translator.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Translator name is required");
        }
        translators.put(translator.getServerProtocolId(), translator);
    }

    public synchronized ProtocolTranslator getTranslator(int serverProtocolId) {
        return translators.get(serverProtocolId);
    }

    public synchronized Map<Integer, ProtocolTranslator> getRegisteredTranslators() {
        return Collections.unmodifiableMap(new HashMap<Integer, ProtocolTranslator>(translators));
    }

    public synchronized ProtocolConnectionPlan planFor(ProtocolVersion serverVersion) {
        if (serverVersion == null) throw new IllegalArgumentException("Server version is required");
        if (serverVersion.getProtocolId() == clientVersion.getProtocolId()) {
            return new ProtocolConnectionPlan(clientVersion, serverVersion,
                ProtocolConnectionPlan.Mode.DIRECT, null,
                "Both sides use protocol " + clientVersion.getProtocolId() + "; direct 1.8.x networking is compatible.");
        }

        ProtocolTranslator translator = translators.get(serverVersion.getProtocolId());
        if (translator != null) {
            return new ProtocolConnectionPlan(clientVersion, serverVersion,
                ProtocolConnectionPlan.Mode.TRANSLATED, translator.getName(),
                "A bidirectional translator is registered: " + translator.getName() + ".");
        }

        return new ProtocolConnectionPlan(clientVersion, serverVersion,
            ProtocolConnectionPlan.Mode.TRANSLATOR_MISSING, null,
            "Server protocol " + serverVersion.getProtocolId() + " requires a complete packet translator. "
                + "This protocol is catalogued, but this build does not yet translate it.");
    }

    /** Creates a plan directly from the result of a Minecraft server status ping. */
    public synchronized ProtocolConnectionPlan planFor(ServerStatus status) {
        if (status == null) throw new IllegalArgumentException("Detected server status is required");
        ProtocolVersion serverVersion = status.getKnownProtocolVersion();
        if (serverVersion == null) {
            return ProtocolConnectionPlan.unknown(clientVersion, status.getProtocolId(), status.getVersionName());
        }
        return planFor(serverVersion);
    }
}
