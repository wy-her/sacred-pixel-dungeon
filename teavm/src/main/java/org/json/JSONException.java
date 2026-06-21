package org.json;

/**
 * GWT-compatible emulation of org.json.JSONException.
 */
public class JSONException extends Exception {

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }
}
