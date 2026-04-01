package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.DiemThiDao;
import com.example.quanlythisinh.dao.KhoiThiDao;
import com.example.quanlythisinh.dao.ThiSinhDao;
import com.example.quanlythisinh.model.dto.DiemThiTableRow;
import com.example.quanlythisinh.model.entity.DiemThi;
import com.example.quanlythisinh.model.entity.KhoiThi;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.config.JpaUtil;
import com.example.quanlythisinh.util.ScoreUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.ArrayList;
import java.util.List;

public class DiemThiService {
    private static void validateScoreRange(Double... scores) {
        for (Double s : scores) {
            if (s == null) {
                continue;
            }
            if (s < 0.0 || s > 10.0) {
                throw new IllegalArgumentException("Điểm phải nằm trong khoảng 0-10.");
            }
        }
    }

    private static void validateScoreRangePrimitive(double... scores) {
        for (double s : scores) {
            if (s < 0.0 || s > 10.0) {
                throw new IllegalArgumentException("Điểm phải nằm trong khoảng 0-10.");
            }
        }
    }

    /** Danh sách thí sinh (combobox thêm dòng điểm). */
    public List<ThiSinh> findAllThiSinh() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new ThiSinhDao(em).findAll();
        } finally {
            em.close();
        }
    }

    /** Danh sách khối thi (combobox thêm dòng điểm). */
    public List<KhoiThi> findAllKhoi() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new KhoiThiDao(em).findAll();
        } finally {
            em.close();
        }
    }

    /**
     * Tạo bản ghi {@link DiemThi} (ba điểm NULL) nếu chưa có cặp (thí sinh, khối).
     *
     * @return {@code true} nếu vừa tạo mới, {@code false} nếu đã tồn tại
     */
    public boolean ensureDiemThiRowExists(String soBaoDanh, Long khoiThiId) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            DiemThiDao dao = new DiemThiDao(em);
            if (dao.findByThiSinhAndKhoi(soBaoDanh, khoiThiId) != null) {
                return false;
            }
            tx.begin();
            ThiSinh ts = new ThiSinhDao(em).findBySoBaoDanh(soBaoDanh);
            KhoiThi kt = new KhoiThiDao(em).findById(khoiThiId);
            if (ts == null) {
                tx.rollback();
                throw new IllegalArgumentException("Không tìm thấy thí sinh.");
            }
            if (kt == null) {
                tx.rollback();
                throw new IllegalArgumentException("Không tìm thấy khối thi.");
            }
            DiemThi d = new DiemThi();
            d.thiSinh = ts;
            d.khoiThi = kt;
            d.diemMon1 = null;
            d.diemMon2 = null;
            d.diemMon3 = null;
            dao.persist(d);
            tx.commit();
            return true;
        } catch (IllegalArgumentException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /** Ham lay tat ca diem thi va map sang DTO de hien thi bang. */
    public List<DiemThiTableRow> getAllRows() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return mapRows(new DiemThiDao(em).findAll());
        } finally {
            em.close();
        }
    }

    /** Ham loc bang diem theo so bao danh (chuoi con, khong phan biet hoa thuong). */
    public List<DiemThiTableRow> getRowsBySoBaoDanhLike(String keyword) {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isBlank()) {
            return getAllRows();
        }
        EntityManager em = JpaUtil.createEntityManager();
        try {
            String pattern = "%" + kw.toLowerCase() + "%";
            return mapRows(new DiemThiDao(em).findByThiSinhSoBaoDanhLike(pattern));
        } finally {
            em.close();
        }
    }

    private List<DiemThiTableRow> mapRows(List<DiemThi> list) {
        return list.stream()
                .map(d -> new DiemThiTableRow(
                        d.id, d.thiSinh.soBaoDanh, d.thiSinh.hoTen,
                        d.khoiThi.maKhoi,
                        d.diemMon1, d.diemMon2, d.diemMon3,
                        ScoreUtil.tong3MonNullable(d),
                        ScoreUtil.diemUuTienNullable(d),
                        ScoreUtil.finalScoreNullable(d)))
                .toList();
    }

    /**
     * Thứ tự hiển thị: các dòng thiếu ít nhất một điểm môn lên trước; thứ tự tương đối trong từng nhóm giữ nguyên.
     */
    public List<DiemThiTableRow> orderIncompleteScoresFirst(List<DiemThiTableRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return rows == null ? List.of() : List.copyOf(rows);
        }
        List<DiemThiTableRow> incomplete = new ArrayList<>();
        List<DiemThiTableRow> complete = new ArrayList<>();
        for (DiemThiTableRow r : rows) {
            boolean isComplete = r.diemMon1() != null && r.diemMon2() != null && r.diemMon3() != null;
            if (isComplete) {
                complete.add(r);
            } else {
                incomplete.add(r);
            }
        }
        List<DiemThiTableRow> out = new ArrayList<>(incomplete.size() + complete.size());
        out.addAll(incomplete);
        out.addAll(complete);
        return out;
    }

    /** Ham cap nhat diem thi theo cap thi sinh-khoi; neu chua co thi tao moi. */
    public void upsertByThiSinhAndKhoi(String soBaoDanh, Long khoiThiId, double mon1, double mon2, double mon3) {
        validateScoreRangePrimitive(mon1, mon2, mon3);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DiemThiDao dao = new DiemThiDao(em);
            DiemThi d = dao.findByThiSinhAndKhoi(soBaoDanh, khoiThiId);
            if (d == null) {
                ThiSinh thiSinh = new ThiSinhDao(em).findBySoBaoDanh(soBaoDanh);
                KhoiThi khoiThi = new KhoiThiDao(em).findById(khoiThiId);
                d = new DiemThi();
                d.thiSinh = thiSinh;
                d.khoiThi = khoiThi;
                d.diemMon1 = mon1;
                d.diemMon2 = mon2;
                d.diemMon3 = mon3;
                dao.persist(d);
            } else {
                d.diemMon1 = mon1;
                d.diemMon2 = mon2;
                d.diemMon3 = mon3;
                dao.merge(d);
            }
            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /** Ham lay 1 ban ghi diem theo cap thi sinh-khoi de do len form. */
    public DiemThiTableRow findRowByThiSinhAndKhoi(String soBaoDanh, Long khoiThiId) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            DiemThi d = new DiemThiDao(em).findByThiSinhAndKhoi(soBaoDanh, khoiThiId);
            if (d == null) {
                return null;
            }
            return new DiemThiTableRow(
                    d.id, d.thiSinh.soBaoDanh, d.thiSinh.hoTen,
                    d.khoiThi.maKhoi,
                    d.diemMon1, d.diemMon2, d.diemMon3,
                    ScoreUtil.tong3MonNullable(d),
                    ScoreUtil.diemUuTienNullable(d),
                    ScoreUtil.finalScoreNullable(d));
        } finally {
            em.close();
        }
    }

    /** Cập nhật ba điểm theo id (có thể truyền null để làm trống từng môn). */
    public void updateScoresById(Long id, Double mon1, Double mon2, Double mon3) {
        validateScoreRange(mon1, mon2, mon3);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DiemThiDao dao = new DiemThiDao(em);
            DiemThi d = dao.findById(id);
            if (d == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy bản ghi điểm.");
            }
            d.diemMon1 = mon1;
            d.diemMon2 = mon2;
            d.diemMon3 = mon3;
            dao.merge(d);
            tx.commit();
        } catch (IllegalStateException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /** Xóa điểm = đặt cả ba môn thành NULL, không xóa dòng DiemThi. */
    public void clearScoresById(Long diemThiId) {
        updateScoresById(diemThiId, null, null, null);
    }
}
