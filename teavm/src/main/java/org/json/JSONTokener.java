package org.json;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * GWT-compatible emulation of org.json.JSONTokener.
 * Uses LibGDX's JsonReader internally for parsing.
 */
public class JSONTokener {

    private final String source;

    public JSONTokener(String source) {
        this.source = source;
    }

    public Object nextValue() throws JSONException {
        try {
            JsonReader reader = new JsonReader();
            JsonValue value = reader.parse(source);
            return convertJsonValue(value);
        } catch (Exception e) {
            throw new JSONException("Failed to parse JSON: " + e.getMessage());
        }
    }

    static Object convertJsonValue(JsonValue value) throws JSONException {
        if (value == null) {
            return null;
        }

        switch (value.type()) {
            case object:
                return new JSONObject(value);
            case array:
                return new JSONArray(value);
            case stringValue:
                return value.asString();
            case doubleValue:
                return value.asDouble();
            case longValue:
                return value.asLong();
            case booleanValue:
                return value.asBoolean();
            case nullValue:
                return JSONObject.NULL;
            default:
                throw new JSONException("Unknown JsonValue type: " + value.type());
        }
    }
}
