package com.watabou.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * TeaVM version: GZIP is not available in browsers.
 */
public class GZIPCompat {

    public static boolean isGZIPHeader(byte[] header) {
        return false;
    }

    public static InputStream wrapInputForGZIP(InputStream stream) throws IOException {
        throw new UnsupportedOperationException("GZIP not supported on HTML5");
    }

    public static OutputStream wrapOutputForGZIP(OutputStream stream) throws IOException {
        throw new UnsupportedOperationException("GZIP not supported on HTML5");
    }
}
