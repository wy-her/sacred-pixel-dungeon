/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebUrlCodec - URL-safe Base64 encoding/decoding
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import org.teavm.jso.JSBody;

/**
 * URL-safe Base64 encoding/decoding for URL fragment usage.
 *
 * Uses URL-safe alphabet:
 * - '+' → '-'
 * - '/' → '_'
 * - Padding '=' removed
 *
 * URL Format: https://sacredpixel.net/#CODE
 * - CODE is the compressed, base64-encoded game data
 * - User can share the URL directly; others paste it in browser to import
 *
 * This ensures the encoded string is safe for use in URL fragments
 * without percent-encoding.
 */
public class WebUrlCodec {

    private static final String BASE_URL = "https://sacredpixel.net/";
    private static final String DATA_PREFIX = "#";  // Simple hash fragment
    private static final String LEGACY_PREFIX = "#d=";  // Backward compatibility

    // Maximum URL length to prevent DoS attacks (60KB)
    private static final int MAX_URL_LENGTH = 60000;

    /**
     * Encodes byte array to URL-safe Base64 string.
     */
    @JSBody(params = {"data"}, script =
            "var binary = '';" +
            "var bytes = new Uint8Array(data);" +
            "for (var i = 0; i < bytes.byteLength; i++) {" +
            "    binary += String.fromCharCode(bytes[i]);" +
            "}" +
            "var base64 = btoa(binary);" +
            "return base64.replace(/\\+/g, '-').replace(/\\//g, '_').replace(/=+$/, '');")
    public static native String toUrlSafeBase64(byte[] data);

    /**
     * Decodes URL-safe Base64 string to byte array.
     */
    @JSBody(params = {"encoded"}, script =
            "var base64 = encoded.replace(/-/g, '+').replace(/_/g, '/');" +
            "while (base64.length % 4) { base64 += '='; }" +
            "var binary = atob(base64);" +
            "var bytes = new Uint8Array(binary.length);" +
            "for (var i = 0; i < binary.length; i++) {" +
            "    bytes[i] = binary.charCodeAt(i);" +
            "}" +
            "return bytes;")
    private static native byte[] fromUrlSafeBase64Native(String encoded);

    /**
     * Decodes URL-safe Base64 string to byte array with validation.
     */
    public static byte[] fromUrlSafeBase64(String encoded) {
        if (encoded == null || encoded.isEmpty()) {
            log("fromUrlSafeBase64: encoded is null or empty");
            return null;
        }

        log("fromUrlSafeBase64: input length=" + encoded.length() + ", first 30 chars: " + encoded.substring(0, Math.min(30, encoded.length())));

        try {
            byte[] result = fromUrlSafeBase64Native(encoded);
            if (result != null) {
                log("fromUrlSafeBase64: decoded " + result.length + " bytes");
            } else {
                log("fromUrlSafeBase64: native returned null");
            }
            return result;
        } catch (Exception e) {
            log("fromUrlSafeBase64: EXCEPTION - " + e.getMessage());
            return null;
        }
    }

    // Debug logging disabled for production
    @JSBody(params = {"msg"}, script = "")
    private static native void log(String msg);

    /**
     * Creates full share URL from compressed data.
     * @param compressedData Compressed byte array
     * @return Full URL like "https://sacredpixel.net/#CODE"
     */
    public static String createShareUrl(byte[] compressedData) {
        if (compressedData == null) {
            return null;
        }
        String encoded = toUrlSafeBase64(compressedData);
        return BASE_URL + DATA_PREFIX + encoded;
    }

    /**
     * Extracts encoded data from URL.
     * Supports multiple formats:
     * - https://sacredpixel.net/#CODE (new format)
     * - https://sacredpixel.net/#d=CODE (legacy format)
     * - https://sacredpixel.net/?d=CODE (query param format)
     * - Just the code itself
     *
     * @param url Full URL, fragment, query param, or just code
     * @return Encoded data string or null
     */
    public static String extractData(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        // Security: reject excessively long URLs to prevent DoS
        if (url.length() > MAX_URL_LENGTH) {
            log("extractData: URL too long (" + url.length() + " > " + MAX_URL_LENGTH + ")");
            return null;
        }

        // Check for query parameter format: ?d=CODE
        int queryIndex = url.indexOf("?d=");
        if (queryIndex >= 0) {
            String code = url.substring(queryIndex + 3);
            // Remove any trailing hash or other params
            int endIndex = code.indexOf('#');
            if (endIndex >= 0) code = code.substring(0, endIndex);
            endIndex = code.indexOf('&');
            if (endIndex >= 0) code = code.substring(0, endIndex);
            return code.isEmpty() ? null : code;
        }

        // Handle full URL - extract fragment
        int hashIndex = url.indexOf('#');
        String fragment = hashIndex >= 0 ? url.substring(hashIndex) : url;

        // Check for legacy prefix: #d=CODE
        if (fragment.startsWith(LEGACY_PREFIX)) {
            return fragment.substring(LEGACY_PREFIX.length());
        }

        // Check for new simple prefix: #CODE
        if (fragment.startsWith("#")) {
            String code = fragment.substring(1);
            // Validate it looks like base64 data (not empty, reasonable length)
            if (!code.isEmpty() && code.length() >= 10) {
                return code;
            }
        }

        // If no hash, might be just the code itself
        if (hashIndex < 0 && url.length() >= 10 && isValidBase64(url)) {
            return url;
        }

        return null;
    }

    /**
     * Checks if string looks like valid URL-safe base64.
     */
    private static boolean isValidBase64(String s) {
        if (s == null || s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                  (c >= '0' && c <= '9') || c == '-' || c == '_')) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets current page URL fragment.
     * Also checks sessionStorage for saved import hash (used when hash was removed for asset loading).
     */
    @JSBody(script =
            "var hash = window.location.hash || '';" +
            "if (!hash || hash.length <= 1) {" +
            "    var saved = sessionStorage.getItem('_spdImportHash');" +
            "    if (saved) {" +
            "        return saved;" +
            "    }" +
            "}" +
            "return hash;")
    public static native String getUrlFragment();

    /**
     * Sets current page URL fragment without navigation.
     */
    @JSBody(params = {"fragment"}, script =
            "if (history.replaceState) {" +
            "    history.replaceState(null, '', fragment || window.location.pathname);" +
            "}")
    public static native void setUrlFragment(String fragment);

    /**
     * Clears URL fragment after import.
     * Also clears sessionStorage saved hash.
     */
    public static void clearDataFragment() {
        setUrlFragment("");
        clearSessionHash();
    }

    @JSBody(script = "sessionStorage.removeItem('_spdImportHash');")
    private static native void clearSessionHash();

    /**
     * Reloads the page after import to refresh all game data.
     */
    @JSBody(script = "window.location.reload();")
    public static native void reloadPage();

    // ========== Seed URL Parameter Support ==========

    /**
     * Gets a URL query parameter value.
     * @param param Parameter name (e.g., "seed", "class", "challenges")
     * @return Parameter value or null if not found
     */
    @JSBody(params = {"param"}, script =
            "var urlParams = new URLSearchParams(window.location.search);" +
            "return urlParams.get(param);")
    public static native String getQueryParam(String param);

    /**
     * Checks if seed parameters are present in the URL.
     * @return true if ?seed= parameter exists
     */
    public static boolean hasSeedParams() {
        String seed = getQueryParam("seed");
        return seed != null && !seed.isEmpty();
    }

    /**
     * Gets the seed code from URL parameters.
     * @return Seed code (e.g., "ABC-DEF-GHI") or null
     */
    public static String getSeedParam() {
        return getQueryParam("seed");
    }

    /**
     * Gets the hero class from URL parameters.
     * @return Hero class name in lowercase (e.g., "warrior") or null
     */
    public static String getClassParam() {
        return getQueryParam("class");
    }

    /**
     * Gets the challenges from URL parameters.
     * @return Challenges value as integer, or 0 if not specified
     */
    public static int getChallengesParam() {
        String challenges = getQueryParam("challenges");
        if (challenges != null && !challenges.isEmpty()) {
            try {
                return Integer.parseInt(challenges);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Clears seed parameters from URL without reloading.
     */
    @JSBody(script =
            "if (history.replaceState) {" +
            "    var url = new URL(window.location.href);" +
            "    url.searchParams.delete('seed');" +
            "    url.searchParams.delete('class');" +
            "    url.searchParams.delete('challenges');" +
            "    history.replaceState(null, '', url.pathname + url.hash);" +
            "}")
    public static native void clearSeedParams();
}
