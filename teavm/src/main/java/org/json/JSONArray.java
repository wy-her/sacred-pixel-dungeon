package org.json;

import com.badlogic.gdx.utils.JsonValue;

import java.util.ArrayList;
import java.util.List;

/**
 * GWT-compatible emulation of org.json.JSONArray.
 * Uses an ArrayList internally and provides the subset of org.json API used by Bundle.java.
 */
public class JSONArray {

    private final List<Object> list;

    public JSONArray() {
        this.list = new ArrayList<>();
    }

    /** Construct from a LibGDX JsonValue (array type) */
    JSONArray(JsonValue value) {
        this.list = new ArrayList<>();
        if (value != null && value.type() == JsonValue.ValueType.array) {
            for (JsonValue child = value.child; child != null; child = child.next) {
                try {
                    list.add(convertChild(child));
                } catch (JSONException e) {
                    list.add(null);
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
                return JSONObject.NULL;
            default:
                return child.asString();
        }
    }

    public int length() {
        return list.size();
    }

    public Object get(int index) throws JSONException {
        if (index < 0 || index >= list.size()) {
            throw new JSONException("JSONArray[" + index + "] not found.");
        }
        return list.get(index);
    }

    // put with index (used by Bundle for array building)
    public JSONArray put(int index, Object value) throws JSONException {
        // Expand list if needed
        while (list.size() <= index) {
            list.add(null);
        }
        list.set(index, value);
        return this;
    }

    public JSONArray put(int index, boolean value) throws JSONException {
        return put(index, (Object) Boolean.valueOf(value));
    }

    public JSONArray put(int index, int value) throws JSONException {
        return put(index, (Object) Integer.valueOf(value));
    }

    public JSONArray put(int index, long value) throws JSONException {
        return put(index, (Object) Long.valueOf(value));
    }

    public JSONArray put(int index, double value) throws JSONException {
        return put(index, (Object) Double.valueOf(value));
    }

    // put without index (append to end, used by Bundle for collection serialization)
    public JSONArray put(Object value) {
        list.add(value);
        return this;
    }

    // get methods with type coercion

    public int getInt(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONArray[" + index + "] is not an int.");
        }
    }

    public long getLong(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONArray[" + index + "] is not a long.");
        }
    }

    public double getDouble(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (NumberFormatException e) {
            throw new JSONException("JSONArray[" + index + "] is not a double.");
        }
    }

    public double optDouble(int index, double defaultValue) {
        if (index < 0 || index >= list.size()) return defaultValue;
        Object val = list.get(index);
        if (val instanceof Number) return ((Number) val).doubleValue();
        if (val instanceof String) {
            try { return Double.parseDouble((String) val); } catch (NumberFormatException e) { }
        }
        return defaultValue;
    }

    public boolean getBoolean(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof Boolean) return (Boolean) val;
        if ("true".equalsIgnoreCase(val.toString())) return true;
        if ("false".equalsIgnoreCase(val.toString())) return false;
        throw new JSONException("JSONArray[" + index + "] is not a boolean.");
    }

    public String getString(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof String) return (String) val;
        return val.toString();
    }

    public JSONObject getJSONObject(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof JSONObject) return (JSONObject) val;
        throw new JSONException("JSONArray[" + index + "] is not a JSONObject.");
    }

    public JSONArray getJSONArray(int index) throws JSONException {
        Object val = get(index);
        if (val instanceof JSONArray) return (JSONArray) val;
        throw new JSONException("JSONArray[" + index + "] is not a JSONArray.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        appendTo(sb);
        return sb.toString();
    }

    void appendTo(StringBuilder sb) {
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            JSONObject.appendValue(list.get(i), sb);
        }
        sb.append(']');
    }
}
