/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * Sacred Pixel Dungeon
 * Copyright (C) 2026 AI SOFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Isolates java.util.zip references so they can be replaced via GWT super-source.
 */
public class GZIPCompat {

	private static final int GZIP_BUFFER = 1024 * 4; // 4 KB

	/**
	 * Tests whether the given header bytes indicate GZIP format.
	 */
	public static boolean isGZIPHeader(byte[] header) {
		return header[0] == (byte) 0x1f && header[1] == (byte) 0x8b;
	}

	/**
	 * Wraps an InputStream with GZIP decompression.
	 */
	public static InputStream wrapInputForGZIP(InputStream stream) throws IOException {
		return new GZIPInputStream(stream, GZIP_BUFFER);
	}

	/**
	 * Wraps an OutputStream with GZIP compression.
	 */
	public static OutputStream wrapOutputForGZIP(OutputStream stream) throws IOException {
		return new GZIPOutputStream(stream, GZIP_BUFFER);
	}
}
