package com.watabou.utils;

import java.util.Date;
import java.util.Locale;

import org.teavm.jso.JSBody;

/**
 * TeaVM version - uses JavaScript Date via @JSBody.
 */
public class DateCompat {

    public static int getYear() {
        return new Date().getYear() + 1900;
    }

    public static int getMonth() {
        return new Date().getMonth(); // 0-indexed like Calendar
    }

    public static int getDayOfMonth() {
        return new Date().getDate();
    }

    public static int getHourOfDay() {
        return new Date().getHours();
    }

    public static int getDayOfYear() {
        Date now = new Date();
        Date yearStart = new Date(now.getYear(), 0, 1);
        long diff = now.getTime() - yearStart.getTime();
        return (int)(diff / (24 * 60 * 60 * 1000)) + 1;
    }

    public static int getDaysInYear() {
        return getDaysInYear(getYear());
    }

    public static int getDaysInYear(int year) {
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            return 366;
        }
        return 365;
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static String formatDateUTC(long millis) {
        return jsFormatDateUTC(millis);
    }

    @JSBody(params = {"millis"}, script =
        "var d = new Date(millis);" +
        "var year = d.getUTCFullYear();" +
        "var month = d.getUTCMonth() + 1;" +
        "var day = d.getUTCDate();" +
        "function pad2(n) { return n < 10 ? '0' + n : '' + n; }" +
        "function pad4(n) {" +
        "  if (n < 10) return '000' + n;" +
        "  if (n < 100) return '00' + n;" +
        "  if (n < 1000) return '0' + n;" +
        "  return '' + n;" +
        "}" +
        "return pad4(year) + '-' + pad2(month) + '-' + pad2(day);")
    private static native String jsFormatDateUTC(double millis);

    public static String formatTimeUTC(long millis) {
        long totalSecs = millis / 1000;
        int hours = (int)((totalSecs / 3600) % 24);
        int mins = (int)((totalSecs / 60) % 60);
        int secs = (int)(totalSecs % 60);
        return pad2(hours) + ":" + pad2(mins) + ":" + pad2(secs);
    }

    public static String formatNumber(long number, Locale locale) {
        // Simple thousand-separator implementation for TeaVM
        // Uses comma as default, period for German/etc locales
        boolean negative = number < 0;
        if (negative) number = -number;

        String numStr = String.valueOf(number);
        if (numStr.length() <= 3) {
            return negative ? "-" + numStr : numStr;
        }

        String sep = ",";
        if (locale != null) {
            String lang = locale.getLanguage();
            // German, French, Spanish, Portuguese, Italian, etc. use period or space
            if ("de".equals(lang) || "fr".equals(lang) || "it".equals(lang) || 
                "pt".equals(lang) || "es".equals(lang) || "nl".equals(lang) ||
                "pl".equals(lang) || "cs".equals(lang) || "hu".equals(lang) ||
                "sv".equals(lang) || "ru".equals(lang) || "uk".equals(lang) ||
                "be".equals(lang) || "el".equals(lang) || "tr".equals(lang) ||
                "vi".equals(lang) || "in".equals(lang) || "eo".equals(lang)) {
                sep = ".";
            }
        }

        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = numStr.length() - 1; i >= 0; i--) {
            if (count > 0 && count % 3 == 0) {
                sb.insert(0, sep);
            }
            sb.insert(0, numStr.charAt(i));
            count++;
        }

        return negative ? "-" + sb.toString() : sb.toString();
    }

    private static String pad2(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }

    private static String pad4(int n) {
        if (n < 10) return "000" + n;
        if (n < 100) return "00" + n;
        if (n < 1000) return "0" + n;
        return String.valueOf(n);
    }

    public static final int JANUARY = 0;
    public static final int FEBRUARY = 1;
    public static final int MARCH = 2;
    public static final int APRIL = 3;
    public static final int MAY = 4;
    public static final int JUNE = 5;
    public static final int JULY = 6;
    public static final int AUGUST = 7;
    public static final int SEPTEMBER = 8;
    public static final int OCTOBER = 9;
    public static final int NOVEMBER = 10;
    public static final int DECEMBER = 11;
}
