/*
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
 */

package com.watabou.utils;

import java.util.Locale;

/**
 * String formatting compatibility layer.
 * Standard JVM version delegates to String.format().
 * GWT super-source provides a simple implementation.
 */
public class StringCompat {

	public static String format(Locale locale, String format, Object... args) {
		return String.format(locale, format, args);
	}

	public static String format(String format, Object... args) {
		return String.format(format, args);
	}

	public static String decimalFormat(String pattern, double number, Locale locale) {
		java.text.DecimalFormat fmt = new java.text.DecimalFormat(pattern,
				java.text.DecimalFormatSymbols.getInstance(locale));
		return fmt.format(number);
	}
}
