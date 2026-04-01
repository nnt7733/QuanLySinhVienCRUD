package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.DangKyNguyenVongDao;
import com.example.quanlythisinh.dao.DiemThiDao;
import com.example.quanlythisinh.dao.TruongDaiHocDao;
import com.example.quanlythisinh.model.dto.DiemThiTableRow;
import com.example.quanlythisinh.model.dto.NganhTheoTruongRow;
import com.example.quanlythisinh.model.dto.TruongDaiHocTableRow;
import com.example.quanlythisinh.model.dto.ThongKeKhoiRow;
import com.example.quanlythisinh.model.dto.ThuKhoaRow;
import com.example.quanlythisinh.model.entity.DiemThi;
import com.example.quanlythisinh.config.JpaUtil;
import com.example.quanlythisinh.util.ScoreUtil;
import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaoCaoService {
    /** Ham thong ke so luong thi sinh theo khoi thi bang JPQL GROUP BY. */
    public List<ThongKeKhoiRow> thongKeTheoKhoi() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<Object[]> rows = em.createQuery(
                            "SELECT d.khoiThi.maKhoi, COUNT(DISTINCT d.thiSinh.soBaoDanh) " +
                                    "FROM DiemThi d GROUP BY d.khoiThi.maKhoi ORDER BY d.khoiThi.maKhoi",
                            Object[].class)
                    .getResultList();
            return rows.stream().map(r -> new ThongKeKhoiRow((String) r[0], (Long) r[1])).toList();
        } finally {
            em.close();
        }
    }

    /** Ham tim thu khoa tung khoi thi dua tren tong diem 3 mon. */
    public List<ThuKhoaRow> thuKhoaTheoKhoi() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<DiemThi> all = new DiemThiDao(em).findAll();
            Map<String, DiemThi> topByKhoi = all.stream()
                    // chỉ xét bản ghi đủ 3 môn
                    .filter(d -> !ScoreUtil.isCleared(d))
                    .filter(d -> d.diemMon1 != null && d.diemMon2 != null && d.diemMon3 != null)
                    .collect(Collectors.toMap(d -> d.khoiThi.maKhoi, d -> d,
                            (a, b) -> ScoreUtil.baseScore(a) >= ScoreUtil.baseScore(b) ? a : b));

            List<ThuKhoaRow> result = new ArrayList<>();
            for (Map.Entry<String, DiemThi> e : topByKhoi.entrySet()) {
                DiemThi d = e.getValue();
                result.add(new ThuKhoaRow(e.getKey(), d.thiSinh.soBaoDanh, d.thiSinh.hoTen, ScoreUtil.baseScore(d)));
            }
            result.sort(Comparator.comparing(ThuKhoaRow::maKhoi));
            return result;
        } finally {
            em.close();
        }
    }

    /**
     * Sắp xếp bản ghi điểm theo tổng điểm 3 môn (mặc định cao → thấp).
     * {@code maKhoiOrAll}: {@code "ALL"} hoặc null = mọi khối; {@code "A"}/{@code "B"}/{@code "C"} = chỉ khối đó.
     */
    public List<DiemThiTableRow> sortByScore(String maKhoiOrAll) {
        return sortByScore(maKhoiOrAll, false);
    }

    /**
     * Sắp xếp bản ghi điểm theo tổng điểm.
     *
     * @param ascending {@code false}: cao → thấp; {@code true}: thấp → cao (null tổng điểm luôn ở cuối).
     */
    public List<DiemThiTableRow> sortByScore(String maKhoiOrAll, boolean ascending) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<DiemThi> all = new DiemThiDao(em).findAll();
            if (maKhoiOrAll != null && !maKhoiOrAll.isBlank() && !"ALL".equalsIgnoreCase(maKhoiOrAll)) {
                String mk = maKhoiOrAll.trim().toUpperCase();
                all = all.stream()
                        .filter(d -> d.khoiThi != null && mk.equals(d.khoiThi.maKhoi))
                        .toList();
            }
            Comparator<Double> scoreCmp = ascending
                    ? Comparator.nullsLast(Comparator.naturalOrder())
                    : Comparator.nullsLast(Comparator.reverseOrder());
            return toRows(all).stream()
                    .sorted(Comparator.comparing(DiemThiTableRow::tong3Mon, scoreCmp))
                    .toList();
        } finally {
            em.close();
        }
    }

    /** Tong so ban ghi nguyen vong (toan he thong). */
    public long thongKeTongSoNguyenVong() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new DangKyNguyenVongDao(em).countAll();
        } finally {
            em.close();
        }
    }

    /** Danh sách trường cho combobox thống kê (không cache). */
    public List<TruongDaiHocTableRow> listAllTruongRows() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new TruongDaiHocDao(em).findAll().stream()
                    .map(t -> new TruongDaiHocTableRow(t.maTruong, t.tenTruong, t.diaChi))
                    .toList();
        } finally {
            em.close();
        }
    }

    /** Thống kê số thí sinh distinct theo ngành đăng ký trong một trường. */
    public List<NganhTheoTruongRow> thongKeNganhTheoTruong(String maTruong) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new DangKyNguyenVongDao(em).countDistinctThiSinhByNganhForTruong(maTruong).stream()
                    .map(r -> {
                        String raw = (String) r[0];
                        String label = (raw == null || raw.isBlank()) ? "(Chưa nhập ngành)" : raw.trim();
                        long n = r[1] instanceof Long l ? l : ((Number) r[1]).longValue();
                        return new NganhTheoTruongRow(label, n);
                    })
                    .toList();
        } finally {
            em.close();
        }
    }

    /** Ham chuyen danh sach entity DiemThi sang danh sach dong hien thi. */
    private List<DiemThiTableRow> toRows(List<DiemThi> diemThiList) {
        return diemThiList.stream()
                .map(d -> new DiemThiTableRow(
                        d.id, d.thiSinh.soBaoDanh, d.thiSinh.hoTen,
                        d.khoiThi.maKhoi,
                        d.diemMon1, d.diemMon2, d.diemMon3,
                        ScoreUtil.tong3MonNullable(d),
                        ScoreUtil.diemUuTienNullable(d),
                        ScoreUtil.finalScoreNullable(d)))
                .toList();
    }
}
