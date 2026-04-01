package com.example.quanlythisinh.view;

import com.example.quanlythisinh.util.TextUtil;

import java.time.format.DateTimeParseException;

/**
 * Chuẩn hóa thông báo lỗi ngắn cho JOptionPane (NV, thí sinh, điểm); tránh lặp logic ở từng frame.
 */
public final class ViewMessages {
    private ViewMessages() {
    }

    /** Lỗi chung (NV/trường): NFE → “Số không hợp lệ”; không thì message lá hoặc “Lỗi.” */
    public static String err(Exception ex) {
        if (ex instanceof NumberFormatException) {
            return "Số không hợp lệ.";
        }
        String m = TextUtil.leafMessage(ex);
        return m.isBlank() ? "Lỗi." : m;
    }

    /** Lỗi khi lưu/thêm/xóa thí sinh: duplicate SBD/CCCD, ngày sai định dạng, đối tượng không phải số, v.v. */
    public static String thiSinhErr(Exception ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            if (t instanceof NumberFormatException) {
                return "Đối tượng ưu tiên: số nguyên.";
            }
            if (t instanceof DateTimeParseException) {
                return "Ngày sinh: yyyy-MM-dd.";
            }
        }
        String msg = TextUtil.leafMessage(ex);
        if (msg.contains("Duplicate entry") && msg.contains("soBaoDanh")) {
            return "Số BD trùng.";
        }
        if (msg.contains("Duplicate entry") && msg.contains("soCanCuoc")) {
            return "CCCD trùng.";
        }
        return msg.isBlank() ? "Lỗi." : msg;
    }

    /** Lỗi khi lưu điểm từ bảng (parse số hoặc lỗi DB). */
    public static String diemSaveErr(Exception ex) {
        if (ex instanceof NumberFormatException) {
            return "Điểm không hợp lệ.";
        }
        String m = TextUtil.leafMessage(ex);
        return m.isBlank() ? "Lỗi lưu." : m;
    }

    /** Xóa điểm (ghi NULL) — DB còn NOT NULL hoặc lỗi khác. */
    public static String clearScoreErr(Throwable ex) {
        String root = TextUtil.leafMessage(ex);
        String low = root.toLowerCase();
        boolean notNull =
                low.contains("cannot be null")
                        || low.contains("doesn't have a default value")
                        || root.contains("1048")
                        || (low.contains("column") && low.contains("null"));
        if (notNull) {
            return "Điểm chưa cho NULL — chạy docs/migrate_optional_nulls.sql\n" + root;
        }
        return root.isBlank() ? "Không xóa được điểm." : root;
    }
}
