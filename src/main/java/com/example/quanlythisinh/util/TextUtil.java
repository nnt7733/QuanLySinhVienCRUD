package com.example.quanlythisinh.util;

/**
 * Tiện ích chuỗi cho ô JTable ({@link #str}, {@link #cellD}) và trích message lỗi từ chuỗi exception.
 */
public final class TextUtil {
    private TextUtil() {
    }

    /** Giá trị ô → chuỗi trim; {@code null} → rỗng. */
    public static String str(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    /** Hiển thị ô Double trong JTable (null → rỗng). */
    public static Object cellD(Double v) {
        return v == null ? "" : v;
    }

    /** Lấy message lá (cause sâu nhất) để hiển thị ngắn. */
    public static String leafMessage(Throwable ex) {
        Throwable t = ex;
        String last = "";
        while (t != null) {
            String m = t.getMessage();
            if (m != null && !m.isBlank()) {
                last = m;
            }
            t = t.getCause();
        }
        return last;
    }
}
