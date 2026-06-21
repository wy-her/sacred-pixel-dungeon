package com.watabou.utils;

import java.util.Locale;

/**
 * TeaVM version of StringCompat.
 * Implements a simple format() since String.format() may not be fully available in TeaVM.
 */
public class StringCompat {

    public static String format(Locale locale, String format, Object... args) {
        return simpleFormat(format, args);
    }

    public static String format(String format, Object... args) {
        return simpleFormat(format, args);
    }

    private static String simpleFormat(String format, Object[] args) {
        if (args == null || args.length == 0) {
            return format;
        }

        StringBuilder sb = new StringBuilder();
        int argIndex = 0;
        int i = 0;

        while (i < format.length()) {
            char c = format.charAt(i);

            if (c == '%' && i + 1 < format.length()) {
                i++;
                char next = format.charAt(i);

                if (next == '%') {
                    sb.append('%');
                    i++;
                    continue;
                }

                boolean plusSign = false;
                boolean zeroPad = false;
                int width = 0;
                int precision = -1;
                int positionalIndex = -1;

                int savedI = i;
                int posNum = 0;
                boolean hasPositional = false;
                while (savedI < format.length() && format.charAt(savedI) >= '0' && format.charAt(savedI) <= '9') {
                    posNum = posNum * 10 + (format.charAt(savedI) - '0');
                    savedI++;
                }
                if (savedI < format.length() && format.charAt(savedI) == '$' && posNum > 0) {
                    positionalIndex = posNum - 1;
                    hasPositional = true;
                    i = savedI + 1;
                }

                boolean useThousandsSeparator = false;
                while (i < format.length()) {
                    char fc = format.charAt(i);
                    if (fc == '+') {
                        plusSign = true;
                        i++;
                    } else if (fc == '0') {
                        zeroPad = true;
                        i++;
                    } else if (fc == ',') {
                        useThousandsSeparator = true;
                        i++;
                    } else if (fc == '-' || fc == ' ') {
                        i++;
                    } else {
                        break;
                    }
                }

                while (i < format.length() && format.charAt(i) >= '0' && format.charAt(i) <= '9') {
                    width = width * 10 + (format.charAt(i) - '0');
                    i++;
                }

                if (i < format.length() && format.charAt(i) == '.') {
                    i++;
                    precision = 0;
                    while (i < format.length() && format.charAt(i) >= '0' && format.charAt(i) <= '9') {
                        precision = precision * 10 + (format.charAt(i) - '0');
                        i++;
                    }
                }

                if (i < format.length()) {
                    char type = format.charAt(i);
                    i++;

                    int useIndex;
                    if (hasPositional) {
                        useIndex = positionalIndex;
                    } else {
                        useIndex = argIndex++;
                    }

                    if (useIndex >= 0 && useIndex < args.length) {
                        Object arg = args[useIndex];
                        String formatted;

                        switch (type) {
                            case 's':
                                formatted = String.valueOf(arg);
                                break;
                            case 'd':
                                long lval = toLong(arg);
                                formatted = Long.toString(lval);
                                if (useThousandsSeparator) {
                                    formatted = addThousandsSeparator(formatted);
                                }
                                if (plusSign && lval > 0) formatted = "+" + formatted;
                                if (zeroPad && width > 0) {
                                    boolean neg = formatted.startsWith("-");
                                    String digits = neg ? formatted.substring(1) : formatted;
                                    while (digits.length() < width - (neg || plusSign ? 1 : 0)) {
                                        digits = "0" + digits;
                                    }
                                    formatted = (neg ? "-" : (plusSign ? "+" : "")) + digits;
                                }
                                break;
                            case 'f':
                                double dval = toDouble(arg);
                                if (precision < 0) precision = 6;
                                formatted = formatDouble(dval, precision);
                                if (plusSign && dval > 0) formatted = "+" + formatted;
                                break;
                            case 'x':
                                formatted = Long.toHexString(toLong(arg));
                                break;
                            case 'X':
                                formatted = Long.toHexString(toLong(arg)).toUpperCase();
                                break;
                            case 'b':
                                formatted = String.valueOf(arg).toLowerCase();
                                break;
                            case 'c':
                                if (arg instanceof Character) {
                                    formatted = String.valueOf((char) arg);
                                } else {
                                    formatted = String.valueOf((char) ((Number) arg).intValue());
                                }
                                break;
                            default:
                                formatted = String.valueOf(arg);
                                break;
                        }

                        while (formatted.length() < width) {
                            if (zeroPad) {
                                formatted = "0" + formatted;
                            } else {
                                formatted = " " + formatted;
                            }
                        }

                        sb.append(formatted);
                    } else {
                        sb.append('%');
                        sb.append(type);
                    }
                }
            } else {
                sb.append(c);
                i++;
            }
        }

        return sb.toString();
    }

    private static String addThousandsSeparator(String numStr) {
        boolean negative = numStr.startsWith("-");
        String digits = negative ? numStr.substring(1) : numStr;
        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(',');
            }
            sb.append(digits.charAt(i));
        }
        return negative ? "-" + sb.toString() : sb.toString();
    }

    private static long toLong(Object obj) {
        if (obj instanceof Number) return ((Number) obj).longValue();
        return 0;
    }

    private static double toDouble(Object obj) {
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return 0;
    }

    public static String decimalFormat(String pattern, double number, Locale locale) {
        int decimalPlaces = 0;
        int dotIdx = pattern.indexOf('.');
        if (dotIdx >= 0) {
            for (int j = dotIdx + 1; j < pattern.length(); j++) {
                if (pattern.charAt(j) == '#' || pattern.charAt(j) == '0') {
                    decimalPlaces++;
                }
            }
        }
        return formatDouble(number, decimalPlaces);
    }

    private static String formatDouble(double val, int precision) {
        if (precision == 0) {
            return Long.toString(Math.round(val));
        }

        boolean negative = val < 0;
        if (negative) val = -val;

        long factor = 1;
        for (int i = 0; i < precision; i++) factor *= 10;

        long rounded = Math.round(val * factor);
        long intPart = rounded / factor;
        long fracPart = rounded % factor;

        String fracStr = Long.toString(fracPart);
        while (fracStr.length() < precision) {
            fracStr = "0" + fracStr;
        }

        return (negative ? "-" : "") + intPart + "." + fracStr;
    }
}
