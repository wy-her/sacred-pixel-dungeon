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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date/time compatibility layer.
 * Standard JVM version - delegates to java.util.Calendar etc.
 * GWT super-source version uses JavaScript Date.
 */
public class DateCompat {

	/** Get current year */
	public static int getYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	/** Get current month (0-indexed: 0=January) */
	public static int getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH);
	}

	/** Get current day of month (1-31) */
	public static int getDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}

	/** Get current hour of day (0-23) */
	public static int getHourOfDay() {
		return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
	}

	/** Get current day of year (1-366) */
	public static int getDayOfYear() {
		return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	}

	/** Get number of days in the current year */
	public static int getDaysInYear() {
		Calendar cal = Calendar.getInstance();
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/** Get day of year for a specific year */
	public static int getDaysInYear(int year) {
		Calendar cal = new GregorianCalendar(year, 0, 1);
		return cal.getActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/** Get current time in millis */
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/** Format millis as "yyyy-MM-dd" in UTC */
	public static String formatDateUTC(long millis) {
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt.format(new Date(millis));
	}

	/** Format millis as "HH:mm:ss" in UTC */
	public static String formatTimeUTC(long millis) {
		SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
		fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
		return fmt.format(new Date(millis));
	}

	/** Format a number with locale-aware formatting (e.g., thousands separator) */
	public static String formatNumber(long number, Locale locale) {
		return java.text.NumberFormat.getInstance(locale).format(number);
	}

	// Calendar month constants (matching java.util.Calendar)
	public static final int JANUARY = Calendar.JANUARY;
	public static final int FEBRUARY = Calendar.FEBRUARY;
	public static final int MARCH = Calendar.MARCH;
	public static final int APRIL = Calendar.APRIL;
	public static final int MAY = Calendar.MAY;
	public static final int JUNE = Calendar.JUNE;
	public static final int JULY = Calendar.JULY;
	public static final int AUGUST = Calendar.AUGUST;
	public static final int SEPTEMBER = Calendar.SEPTEMBER;
	public static final int OCTOBER = Calendar.OCTOBER;
	public static final int NOVEMBER = Calendar.NOVEMBER;
	public static final int DECEMBER = Calendar.DECEMBER;
}
