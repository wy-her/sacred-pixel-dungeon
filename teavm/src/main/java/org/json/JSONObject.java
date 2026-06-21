package org.json;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * GWT-compatible emulation of org.json.JSONObject.
 * Wraps a Map internally and provides the subset of org.json API used by Bundle.java.
 */
public class JSONObject {

    public static final Object NULL = new Object() {
        @Override
        public String toString() {
            return "null";
        }
        @Override
        public boolean equals(Object o) {
            return o == this || o == null;
        }
    };

    private final Map<String, Object> map;

    public JSONObject() {
        this.map = new LinkedHashMap<>();
    }

    /** Construct from a LibGDX JsonValue (object type) */
    JSONObject(JsonValue value) {
        this.map = new LinkedHashMap<>();
        if (value != null && value.type() == JsonValue.ValueType.object) {
            for (JsonValue child = value.child; child != null; child = child.next) {
                try {
                    map.put(child.name, convertChild(child));
                } catch (JSONException e) {
                    // skip malformed entries
                }
            }
        }
    }

    private static Object convertChild(JsonValue child) throws JSONException {
        switch (child.type()) {
            case object:
                return new JSONObject(child);
            case array:
                return new JSONArray(child);
            case stringValue:
                return child.asString();
            case doubleValue:
                return child.asDouble();
            case longValue:
                return child.asLong();
            case booleanValue:
                return child.asBoolean();
            case nullValue:
                return NULL;
            default:
                return child.asString();
        }
    }

    public JSONObject put(String key, Object value) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key");
        }
        if (value != null) {
            map.put(key, value);
        } else {
            map.remove(key);
        }
        return this;
    }

    public JSONObject put(String key, boolean value) throws JSONException {
        return put(key, (Object) Boolean.valueOf(value));
    }

    public JSONObject put(String key, int value) throws JSONException {
        return put(key, (Object) Integer.valueOf(value));
    }

    public JSONObject put(String key, long value) throws JSONException {
        return put(key, (Object) Long.valueOf(value));
    }

    public JSONObject put(String key, double value) throws JSONException {
        return put(key, (Object) Double.valueOf(value));
    }

    public Object get(String key) throws JSONException {
        Object value = map.get(key);
        if (value == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }
        return value;
    }

    public boolean isNull(String key) {
        Object value = map.get(key);
        return value == null || value == NULL;
    }

    public Object remove(String key) {
        return map.remove(key);
    }

    public Iterator<String> keys() {
        return map.keySet().iterator();
    }

    public boolean has(String key) {
        return map.containsKey(key);
    }

    public int length() {
        return map.size();
    }

    // opt methods (return default if missing or wrong type)

    public boolean optBoolean(String key) {
        return optBoolean(key, false);
    }

    public boolean optBoolean(String key, boolean defaultValue) {
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof String) {
            String s = (String) val;
            if ("true".equalsIgnoreCase(s)) return true;
            if ("false".equalsIgnoreCase(s)) return false;
        }
        return defaultValue;
    }

    public int optInt(String key) {
        return optInt(key, 0);
    }

    public int optInt(String key, int defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof String) {
            try { return Integer.parseInt((String) val); } catch (NumberFormatException e) { }
        }
        return defaultValue;
    }

    public long optLong(String key) {
        return optLong(key, 0L);
    }

    public long optLong(String key, long defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        if (val instanceof String) {
            try { return Long.parseLong((String) val); } catch (NumberFormatException e) { }
        }
        return defaultValue;
    }

    public double optDouble(String key, double defaultValue) {
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException e) { }
        }
        return defaultValue;
    }

    public String optString(String key) {
        return optString(key, "");
    }

    public String optString(String key, String defaultValue) {
        Object val = map.get(key);
        if (val == null || val == NULL) return defaultValue;
        return val.toString();
    }

    public JSONObject optJSONObject(String key) {
        Object val = map.get(key);
        if (val instanceof JSONObject) return (JSONObject) val;
        return null;
    }

    public JSONArray optJSONArray(String key) {
        Object val = map.get(key);
        if (val instanceof JSONArray) return (JSONArray) val;
        return null;
    }

    // get methods (throw if missing or wrong type)

    public String getString(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof String) return (String) val;
        return val.toString();
    }

    public int getInt(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONObject[" + key + "] is not an int.");
        }
    }

    public long getLong(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONObject[" + key + "] is not a long.");
        }
    }

    public double getDouble(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONObject[" + key + "] is not a double.");
        }
    }

    public boolean getBoolean(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if ("true".equalsIgnoreCase(val.toString())) return true;
        if ("false".equalsIgnoreCase(val.toString())) return false;
        throw new JSONException("JSONObject[" + key + "] is not a boolean.");
    }

    public JSONObject getJSONObject(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof JSONObject) return (JSONObject) val;
        throw new JSONException("JSONObject[" + key + "] is not a JSONObject.");
    }

    public JSONArray getJSONArray(String key) throws JSONException {
        Object val = get(key);
        if (val instanceof JSONArray) return (JSONArray) val;
        throw new JSONException("JSONObject[" + key + "] is not a JSONArray.");
    }

    /**
     * Serializes to JSON string using a single StringBuilder through the entire
     * recursive tree. Each toString() call creates a NEW local StringBuilder,
     * so this is re-entrant safe (no shared/static state).
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(256);
        appendTo(sb);
        return sb.toString();
    }

    void appendTo(StringBuilder sb) {
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('"');
            sb.append(escapeString(entry.getKey()));
            sb.append('"');
            sb.append(':');
            appendValue(entry.getValue(), sb);
        }
        sb.append('}');
    }

    static String valueToString(Object value) {
        StringBuilder sb = new StringBuilder(64);
        appendValue(value, sb);
        return sb.toString();
    }

    static void appendValue(Object value, StringBuilder sb) {
        if (value == null || value == NULL) {
            sb.append("null");
        } else if (value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Number) {
            sb.append(value.toString());
        } else if (value instanceof String) {
            sb.append('"');
            sb.append(escapeString((String) value));
            sb.append('"');
        } else if (value instanceof JSONObject) {
            ((JSONObject) value).appendTo(sb);
        } else if (value instanceof JSONArray) {
            ((JSONArray) value).appendTo(sb);
        } else {
            sb.append('"');
            sb.append(escapeString(value.toString()));
            sb.append('"');
        }
    }

    static String escapeString(String s) {
        // Fast-path: scan first, if no escaping needed return original string
        boolean needsEscape = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < ' ' || c == '"' || c == '\\') {
                needsEscape = true;
                break;
            }
        }
        if (!needsEscape) return s;

        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = Integer.toHexString(c);
                        sb.append("\\u");
                        for (int j = hex.length(); j < 4; j++) sb.append('0');
                        sb.append(hex);
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
