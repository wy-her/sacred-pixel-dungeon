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

/**
 * Compatibility utilities for methods not available in GWT.
 * On standard JVM, these delegate to native methods.
 * On GWT, a super-source version provides JavaScript-compatible implementations.
 */
public class Compat {

	/**
	 * Equivalent to cls.isInstance(obj).
	 */
	public static boolean isInstance(Class<?> cls, Object obj) {
		return cls.isInstance(obj);
	}

	/**
	 * Equivalent to cls.isAssignableFrom(other).
	 */
	public static boolean isAssignableFrom(Class<?> cls, Class<?> other) {
		return cls.isAssignableFrom(other);
	}

	/**
	 * Equivalent to array.clone() for float arrays.
	 */
	public static float[] cloneArray(float[] array) {
		return array.clone();
	}
}
