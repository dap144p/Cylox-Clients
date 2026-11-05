package dev.cylox.client.protocol;

public enum ProtocolVersion {
    V1_8_X("1.8.x (1.8–1.8.9)", 47),
    V1_9("1.9", 107),
    V1_9_1("1.9.1", 108),
    V1_9_2("1.9.2", 109),
    V1_9_3_1_9_4("1.9.3–1.9.4", 110),
    V1_10_X("1.10.x", 210),
    V1_11("1.11", 315),
    V1_11_1_1_11_2("1.11.1–1.11.2", 316),
    V1_12("1.12", 335),
    V1_12_1("1.12.1", 338),
    V1_12_2("1.12.2", 340),
    V1_13("1.13", 393),
    V1_13_1("1.13.1", 401),
    V1_13_2("1.13.2", 404);

    private final String displayName;
    private final int protocolId;

    ProtocolVersion(String displayName, int protocolId) {
        this.displayName = displayName;
        this.protocolId = protocolId;
    }

    public String getDisplayName() { return displayName; }
    public int getProtocolId() { return protocolId; }

    public static ProtocolVersion fromProtocolId(int protocolId) {
        for (ProtocolVersion version : values()) {
            if (version.protocolId == protocolId) return version;
        }
        return null;
    }
}
