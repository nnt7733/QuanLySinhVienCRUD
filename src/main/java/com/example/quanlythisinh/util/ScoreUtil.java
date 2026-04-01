package com.example.quanlythisinh.util;

import com.example.quanlythisinh.model.entity.DiemThi;
import com.example.quanlythisinh.model.entity.ThiSinh;

public final class ScoreUtil {
    /** Ham tao private constructor de tranh khoi tao sai util class. */
    private ScoreUtil() {
    }

    /** Cả ba môn đều null = đã xóa điểm (chỉ giữ dòng đăng ký khối). */
    public static boolean isCleared(DiemThi d) {
        return d.diemMon1 == null && d.diemMon2 == null && d.diemMon3 == null;
    }

    /** Ham tinh tong diem 3 mon chua cong uu tien; ô trống coi là 0. */
    public static double baseScore(DiemThi d) {
        double m1 = d.diemMon1 != null ? d.diemMon1 : 0.0;
        double m2 = d.diemMon2 != null ? d.diemMon2 : 0.0;
        double m3 = d.diemMon3 != null ? d.diemMon3 : 0.0;
        return round2(m1 + m2 + m3);
    }

    /** Ham tinh diem uu tien theo khu vuc va doi tuong uu tien. */
    public static double priorityScore(ThiSinh ts, double tong3Mon) {
        double kv = switch (safe(ts.khuVuc).toUpperCase()) {
            case "KV1" -> 0.75;
            case "KV2" -> 0.5;
            case "KV2-NT" -> 0.25;
            default -> 0.0;
        };
        double ut = switch (ts.doiTuongUuTien) {
            case 1 -> 2.0;
            case 2 -> 1.0;
            default -> 0.0;
        };
        double coBan = kv + ut;
        if (coBan <= 0) {
            return 0.0;
        }
        if (tong3Mon < 22.5) {
            return round2(coBan);
        }
        double heSo = (30 - tong3Mon) / 7.5;
        if (heSo <= 0) {
            return 0.0;
        }
        return round2(coBan * Math.min(1, heSo));
    }

    /**
     * Tổng điểm 3 môn (không cộng ưu tiên). Trả {@code null} nếu thiếu ít nhất 1 môn.
     */
    public static Double tong3MonNullable(DiemThi d) {
        if (d.diemMon1 == null || d.diemMon2 == null || d.diemMon3 == null) {
            return null;
        }
        return baseScore(d);
    }

    /**
     * Điểm ưu tiên. Trả {@code null} nếu chưa đủ 3 môn để tính.
     */
    public static Double diemUuTienNullable(DiemThi d) {
        Double tong3 = tong3MonNullable(d);
        if (tong3 == null) {
            return null;
        }
        return priorityScore(d.thiSinh, tong3);
    }

    /**
     * Tổng điểm cuối (tổng 3 môn + ưu tiên). Trả {@code null} nếu thiếu ít nhất 1 môn.
     */
    public static Double finalScoreNullable(DiemThi d) {
        Double tong3 = tong3MonNullable(d);
        if (tong3 == null) {
            return null;
        }
        Double uTien = diemUuTienNullable(d);
        return round2(tong3 + (uTien != null ? uTien : 0.0));
    }

    /**
     * Dùng khi cần số thực (ví dụ thủ khoa chỉ xét bản ghi chưa xóa điểm).
     */
    public static double finalScore(DiemThi d) {
        Double v = finalScoreNullable(d);
        return v != null ? v : 0.0;
    }

    /** Ham bo sung gia tri mac dinh cho chuoi null. */
    public static String safe(String value) {
        return value == null ? "" : value;
    }

    /** Ham lam tron den 2 chu so thap phan. */
    public static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
