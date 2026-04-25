package com.example.shop.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtils {

    private static final Locale VI_LOCALE = Locale.forLanguageTag("vi-VN");

    public static String formatMoneyVnd(BigDecimal money) {
        if (money == null) money = BigDecimal.ZERO;
        NumberFormat nf = NumberFormat.getInstance(VI_LOCALE);
        nf.setMaximumFractionDigits(0);
        return nf.format(money) + " ₫";
    }

    public static String formatCompactVnd(BigDecimal vnd) {
        if (vnd == null) vnd = BigDecimal.ZERO;
        BigDecimal v = vnd.abs();
        if (v.compareTo(BigDecimal.valueOf(1_000_000_000L)) >= 0) {
            return v.divide(BigDecimal.valueOf(1_000_000_000L), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "B";
        }
        if (v.compareTo(BigDecimal.valueOf(1_000_000L)) >= 0) {
            return v.divide(BigDecimal.valueOf(1_000_000L), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "M";
        }
        if (v.compareTo(BigDecimal.valueOf(1_000L)) >= 0) {
            return v.divide(BigDecimal.valueOf(1_000L), 0, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "K";
        }
        return formatMoneyVnd(vnd);
    }

    public static String formatPercent(BigDecimal value, int scale) {
        if (value == null) value = BigDecimal.ZERO;
        return value.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    public static String formatCount(long value) {
        return NumberFormat.getInstance(VI_LOCALE).format(value);
    }

    public static String formatCompactCount(long value) {
        if (value >= 1_000_000) {
            return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000_000), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "M";
        }
        if (value >= 1_000) {
            return BigDecimal.valueOf(value).divide(BigDecimal.valueOf(1_000), 1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString() + "K";
        }
        return String.valueOf(value);
    }
}
