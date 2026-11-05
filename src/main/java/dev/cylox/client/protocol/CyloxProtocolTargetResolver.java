package dev.cylox.client.protocol;

/** Ensures manual target selection is used instead of inferring a server's advertised version. */
public final class CyloxProtocolTargetResolver {
    private CyloxProtocolTargetResolver() { }

    public static int resolve(boolean bridgeEnabled, int selectedProtocolId) {
        if (!bridgeEnabled || selectedProtocolId < 47) return 47;
        return selectedProtocolId;
    }
}
