package dev.cylox.client.protocol;

/** Resolves the server protocol selected for the optional 1.8.9 client-side bridge. */
public final class CyloxProtocolTargetResolver {
    private CyloxProtocolTargetResolver() { }

    /**
     * Auto mode accepts only protocol IDs explicitly catalogued by this Cylox build.
     * Unknown/newer protocols safely fall back to the native 1.8.x connection.
     */
    public static int resolve(boolean bridgeEnabled, String mode, int detectedProtocolId) {
        if (!bridgeEnabled || mode == null || "Native".equalsIgnoreCase(mode)) {
            return ProtocolVersion.V1_8_X.getProtocolId();
        }
        if (!"Auto".equalsIgnoreCase(mode)) {
            return ProtocolVersion.V1_8_X.getProtocolId();
        }
        ProtocolVersion detected = ProtocolVersion.fromProtocolId(detectedProtocolId);
        return detected == null
            ? ProtocolVersion.V1_8_X.getProtocolId()
            : detected.getProtocolId();
    }
}
