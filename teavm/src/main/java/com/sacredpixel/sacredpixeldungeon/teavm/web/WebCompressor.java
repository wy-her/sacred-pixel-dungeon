/*
 * Sacred Pixel Dungeon
 * Copyright (C) 2024 Sacred Pixel Team
 *
 * WebCompressor - DEFLATE compression using pako.js
 */
package com.sacredpixel.sacredpixeldungeon.teavm.web;

import org.teavm.jso.JSBody;

/**
 * DEFLATE compression wrapper using pako.js library.
 *
 * Uses raw DEFLATE (RFC 1951) without gzip/zlib headers for minimal size.
 * Compression level 6 provides good balance of speed and ratio.
 *
 * Required: pako.min.js must be loaded in HTML before this is used.
 */
public class WebCompressor {

    /**
     * Compresses data using DEFLATE algorithm.
     * @param data Raw byte array
     * @return Compressed byte array
     */
    @JSBody(params = {"data"}, script =
            "try {" +
            "    if (typeof pako === 'undefined') {" +
            "        console.error('pako.js not loaded');" +
            "        return null;" +
            "    }" +
            "    var input = new Uint8Array(data);" +
            "    var compressed = pako.deflateRaw(input, { level: 6 });" +
            "    return compressed;" +
            "} catch (e) {" +
            "    console.error('Compression error:', e);" +
            "    return null;" +
            "}")
    public static native byte[] deflate(byte[] data);

    // Maximum decompressed size to prevent zip bomb attacks (1MB)
    private static final int MAX_DECOMPRESSED_SIZE = 1 * 1024 * 1024;

    /**
     * Decompresses DEFLATE data.
     * @param compressed Compressed byte array
     * @return Decompressed byte array, or null if too large or error
     */
    @JSBody(params = {"compressed"}, script =
            "try {" +
            "    var MAX_SIZE = 1 * 1024 * 1024;" + // 1MB limit
            "    if (typeof pako === 'undefined') {" +
            "        console.error('[WebCompressor] pako.js not loaded');" +
            "        return null;" +
            "    }" +
            "    var input = new Uint8Array(compressed);" +
            "    var decompressed = pako.inflateRaw(input);" +
            "    if (decompressed.length > MAX_SIZE) {" +
            "        console.error('[WebCompressor] Decompressed data too large: ' + decompressed.length + ' bytes (max: ' + MAX_SIZE + ')');" +
            "        return null;" +
            "    }" +
            "    return decompressed;" +
            "} catch (e) {" +
            "    console.error('[WebCompressor] Decompression error:', e.message);" +
            "    return null;" +
            "}")
    public static native byte[] inflate(byte[] compressed);

    /**
     * Checks if pako.js is available.
     */
    @JSBody(script = "return typeof pako !== 'undefined';")
    public static native boolean isPakoAvailable();

    /**
     * Gets pako.js version string.
     */
    @JSBody(script =
            "if (typeof pako !== 'undefined' && pako.version) {" +
            "    return pako.version;" +
            "}" +
            "return 'unknown';")
    public static native String getPakoVersion();
}
