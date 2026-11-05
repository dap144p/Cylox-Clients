package dev.cylox.client.protocol;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Small bounded-purpose JSON parser used for Minecraft status responses. */
final class MiniJsonParser {
    private final String input;
    private int index;

    private MiniJsonParser(String input) {
        this.input = input;
    }

    static Object parse(String input) throws ProtocolTranslationException {
        if (input == null) throw new ProtocolTranslationException("JSON input is null");
        MiniJsonParser parser = new MiniJsonParser(input);
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.index != input.length()) {
            throw parser.error("Unexpected trailing data");
        }
        return value;
    }

    private Object readValue() throws ProtocolTranslationException {
        skipWhitespace();
        if (index >= input.length()) throw error("Unexpected end of JSON");
        char current = input.charAt(index);
        if (current == '{') return readObject();
        if (current == '[') return readArray();
        if (current == '"') return readString();
        if (current == 't') return readLiteral("true", Boolean.TRUE);
        if (current == 'f') return readLiteral("false", Boolean.FALSE);
        if (current == 'n') return readLiteral("null", null);
        if (current == '-' || (current >= '0' && current <= '9')) return readNumber();
        throw error("Unexpected JSON character");
    }

    private Map<String, Object> readObject() throws ProtocolTranslationException {
        Map<String, Object> object = new LinkedHashMap<String, Object>();
        index++;
        skipWhitespace();
        if (consume('}')) return object;
        while (true) {
            skipWhitespace();
            if (index >= input.length() || input.charAt(index) != '"') {
                throw error("Expected object key");
            }
            String key = readString();
            skipWhitespace();
            require(':');
            Object value = readValue();
            object.put(key, value);
            skipWhitespace();
            if (consume('}')) return object;
            require(',');
        }
    }

    private List<Object> readArray() throws ProtocolTranslationException {
        List<Object> array = new ArrayList<Object>();
        index++;
        skipWhitespace();
        if (consume(']')) return array;
        while (true) {
            array.add(readValue());
            skipWhitespace();
            if (consume(']')) return array;
            require(',');
        }
    }

    private String readString() throws ProtocolTranslationException {
        require('"');
        StringBuilder result = new StringBuilder();
        while (index < input.length()) {
            char current = input.charAt(index++);
            if (current == '"') return result.toString();
            if (current < 0x20) throw error("Unescaped control character");
            if (current != '\\') {
                result.append(current);
                continue;
            }
            if (index >= input.length()) throw error("Incomplete escape sequence");
            char escaped = input.charAt(index++);
            switch (escaped) {
                case '"': result.append('"'); break;
                case '\\': result.append('\\'); break;
                case '/': result.append('/'); break;
                case 'b': result.append('\b'); break;
                case 'f': result.append('\f'); break;
                case 'n': result.append('\n'); break;
                case 'r': result.append('\r'); break;
                case 't': result.append('\t'); break;
                case 'u':
                    if (index + 4 > input.length()) throw error("Incomplete unicode escape");
                    try {
                        result.append((char) Integer.parseInt(input.substring(index, index + 4), 16));
                    } catch (NumberFormatException invalidHex) {
                        throw error("Invalid unicode escape");
                    }
                    index += 4;
                    break;
                default: throw error("Invalid escape sequence");
            }
        }
        throw error("Unterminated string");
    }

    private Object readNumber() throws ProtocolTranslationException {
        int start = index;
        if (consume('-') && index >= input.length()) throw error("Incomplete number");
        if (consume('0')) {
            if (index < input.length() && Character.isDigit(input.charAt(index))) {
                throw error("Leading zero in number");
            }
        } else {
            readDigits();
        }
        boolean decimal = false;
        if (consume('.')) {
            decimal = true;
            readDigits();
        }
        if (consume('e') || consume('E')) {
            decimal = true;
            if (!consume('+')) consume('-');
            readDigits();
        }
        String token = input.substring(start, index);
        try {
            return decimal ? Double.valueOf(token) : Long.valueOf(token);
        } catch (NumberFormatException invalidNumber) {
            throw error("Invalid number");
        }
    }

    private void readDigits() throws ProtocolTranslationException {
        int start = index;
        while (index < input.length() && Character.isDigit(input.charAt(index))) index++;
        if (start == index) throw error("Expected digit");
    }

    private Object readLiteral(String token, Object value) throws ProtocolTranslationException {
        if (!input.regionMatches(index, token, 0, token.length())) {
            throw error("Invalid literal");
        }
        index += token.length();
        return value;
    }

    private void skipWhitespace() {
        while (index < input.length()) {
            char value = input.charAt(index);
            if (value == ' ' || value == '\n' || value == '\r' || value == '\t') index++;
            else break;
        }
    }

    private boolean consume(char expected) {
        if (index < input.length() && input.charAt(index) == expected) {
            index++;
            return true;
        }
        return false;
    }

    private void require(char expected) throws ProtocolTranslationException {
        if (!consume(expected)) throw error("Expected '" + expected + "'");
    }

    private ProtocolTranslationException error(String message) {
        return new ProtocolTranslationException(message + " at JSON offset " + index);
    }
}
