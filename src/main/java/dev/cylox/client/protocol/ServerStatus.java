package dev.cylox.client.protocol;

import java.util.List;
import java.util.Map;

/** Parsed result of the Minecraft server-list status query. */
public final class ServerStatus {
    private final String versionName;
    private final int protocolId;
    private final String description;
    private final int onlinePlayers;
    private final int maxPlayers;

    private ServerStatus(String versionName, int protocolId, String description,
                         int onlinePlayers, int maxPlayers) {
        this.versionName = versionName;
        this.protocolId = protocolId;
        this.description = description;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
    }

    public static ServerStatus parse(String json) throws ProtocolTranslationException {
        Object parsed = MiniJsonParser.parse(json);
        Map<?, ?> root = requireMap(parsed, "Status response must be a JSON object");
        Map<?, ?> version = requireMap(root.get("version"), "Status response has no version object");
        String name = requireString(version.get("name"), "Server version name is missing");
        int protocol = requireInt(version.get("protocol"), "Server protocol ID is missing");

        String description = flattenText(root.get("description"));
        int online = -1;
        int max = -1;
        Object playersValue = root.get("players");
        if (playersValue instanceof Map<?, ?>) {
            Map<?, ?> players = (Map<?, ?>) playersValue;
            online = optionalInt(players.get("online"), -1);
            max = optionalInt(players.get("max"), -1);
        }
        return new ServerStatus(name, protocol, description, online, max);
    }

    public String getVersionName() { return versionName; }
    public int getProtocolId() { return protocolId; }
    public String getDescription() { return description; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }

    /** Returns null when the server reports a protocol not yet in Cylox's catalogue. */
    public ProtocolVersion getKnownProtocolVersion() {
        return ProtocolVersion.fromProtocolId(protocolId);
    }

    private static Map<?, ?> requireMap(Object value, String message) throws ProtocolTranslationException {
        if (!(value instanceof Map<?, ?>)) throw new ProtocolTranslationException(message);
        return (Map<?, ?>) value;
    }

    private static String requireString(Object value, String message) throws ProtocolTranslationException {
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            throw new ProtocolTranslationException(message);
        }
        return (String) value;
    }

    private static int requireInt(Object value, String message) throws ProtocolTranslationException {
        if (!(value instanceof Number)) throw new ProtocolTranslationException(message);
        long result = ((Number) value).longValue();
        if (result < Integer.MIN_VALUE || result > Integer.MAX_VALUE) {
            throw new ProtocolTranslationException("Integer field is out of range");
        }
        return (int) result;
    }

    private static int optionalInt(Object value, int fallback) {
        if (!(value instanceof Number)) return fallback;
        long result = ((Number) value).longValue();
        return result < Integer.MIN_VALUE || result > Integer.MAX_VALUE ? fallback : (int) result;
    }

    private static String flattenText(Object value) {
        if (value instanceof String) return (String) value;
        if (!(value instanceof Map<?, ?>)) return "";
        Map<?, ?> object = (Map<?, ?>) value;
        StringBuilder result = new StringBuilder();
        Object text = object.get("text");
        if (text instanceof String) result.append((String) text);
        Object extra = object.get("extra");
        if (extra instanceof List<?>) {
            for (Object component : (List<?>) extra) result.append(flattenText(component));
        }
        return result.toString();
    }
}
