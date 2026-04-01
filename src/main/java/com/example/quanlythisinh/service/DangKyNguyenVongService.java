package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.DangKyNguyenVongDao;
import com.example.quanlythisinh.dao.ThiSinhDao;
import com.example.quanlythisinh.dao.TruongDaiHocDao;
import com.example.quanlythisinh.model.dto.DangKyNguyenVongTableRow;
import com.example.quanlythisinh.model.entity.DangKyNguyenVong;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import com.example.quanlythisinh.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DangKyNguyenVongService {

    /** Ham lay cac nguyen vong cua thi sinh de hien thi bang. */
    public List<DangKyNguyenVongTableRow> getRowsBySoBaoDanh(String soBaoDanh) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<DangKyNguyenVong> list = new DangKyNguyenVongDao(em).findByThiSinhSoBaoDanhOrderByThuTu(soBaoDanh);
            return list.stream().map(DangKyNguyenVongService::toRow).toList();
        } finally {
            em.close();
        }
    }

    /** Gợi ý thứ tự NV tiếp theo: max + 1, hoặc 1 nếu chưa có. */
    public int suggestNextThuTu(String soBaoDanh) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            Integer max = new DangKyNguyenVongDao(em).maxThuTuForThiSinh(soBaoDanh);
            return max == null ? 1 : max + 1;
        } finally {
            em.close();
        }
    }

    private static DangKyNguyenVongTableRow toRow(DangKyNguyenVong dk) {
        TruongDaiHoc tr = dk.truongDaiHoc;
        String ma = tr == null ? "" : tr.maTruong;
        String ten = tr == null ? "" : tr.tenTruong;
        ThiSinh ts = dk.thiSinh;
        String sbd = ts == null ? "" : ts.soBaoDanh;
        String hoTen = ts == null ? "" : ts.hoTen;
        return new DangKyNguyenVongTableRow(
                dk.id, sbd, hoTen, ma, ten, dk.thuTuNguyenVong,
                dk.nganhDangKy == null ? "" : dk.nganhDangKy);
    }

    /** Ham them nguyen vong cho thi sinh. */
    public void add(String soBaoDanh, String maTruong, int thuTuNguyenVong, String nganhDangKy) {
        validateThuTu(thuTuNguyenVong);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DangKyNguyenVongDao nvDao = new DangKyNguyenVongDao(em);
            ThiSinhDao tsDao = new ThiSinhDao(em);
            TruongDaiHocDao trDao = new TruongDaiHocDao(em);

            ThiSinh ts = tsDao.findBySoBaoDanh(soBaoDanh);
            if (ts == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy thí sinh.");
            }
            TruongDaiHoc tr = trDao.findByMa(maTruong.trim());
            if (tr == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy trường đại học.");
            }
            if (nvDao.findByThiSinhAndThuTu(soBaoDanh, thuTuNguyenVong) != null) {
                tx.rollback();
                throw new IllegalStateException("Thí sinh đã có nguyện vọng với thứ tự này.");
            }

            DangKyNguyenVong dk = new DangKyNguyenVong();
            dk.thiSinh = ts;
            dk.truongDaiHoc = tr;
            dk.thuTuNguyenVong = thuTuNguyenVong;
            dk.nganhDangKy = blankToNull(nganhDangKy);
            nvDao.persist(dk);
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
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    /**
     * Sắp lại chỉ thứ tự nguyện vọng cho thí sinh (một giao dịch).
     * {@code idToNewThuTu} phải gồm đủ mọi NV của thí sinh; giá trị là hoán vị của {@code 1..n}.
     */
    public void reorderThuTu(String soBaoDanh, Map<Long, Integer> idToNewThuTu) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DangKyNguyenVongDao nvDao = new DangKyNguyenVongDao(em);
            List<DangKyNguyenVong> all = nvDao.findByThiSinhSoBaoDanhOrderByThuTu(soBaoDanh);
            int n = all.size();
            if (n == 0) {
                tx.rollback();
                throw new IllegalStateException("Chưa có nguyện vọng để sửa thứ tự.");
            }
            if (idToNewThuTu.size() != n) {
                tx.rollback();
                throw new IllegalStateException("Danh sách thứ tự không khớp số nguyện vọng.");
            }
            for (DangKyNguyenVong dk : all) {
                if (!idToNewThuTu.containsKey(dk.id)) {
                    tx.rollback();
                    throw new IllegalStateException("Thiếu nguyện vọng trong bảng sắp xếp.");
                }
            }
            boolean[] used = new boolean[n + 1];
            for (int newT : idToNewThuTu.values()) {
                if (newT < 1 || newT > n) {
                    tx.rollback();
                    throw new IllegalStateException("Thứ tự phải từ 1 đến " + n + ".");
                }
                if (used[newT]) {
                    tx.rollback();
                    throw new IllegalStateException("Thứ tự nguyện vọng không được trùng nhau.");
                }
                used[newT] = true;
            }
            int maxT = all.stream().mapToInt(d -> d.thuTuNguyenVong).max().orElse(0);
            int slot = maxT + 1;
            for (DangKyNguyenVong dk : all) {
                dk.thuTuNguyenVong = slot++;
                nvDao.merge(dk);
            }
            em.flush();
            for (DangKyNguyenVong dk : all) {
                dk.thuTuNguyenVong = idToNewThuTu.get(dk.id);
                nvDao.merge(dk);
            }
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
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    /** Ham cap nhat nguyen vong (doi truong, thu tu, nganh). Neu trung thu tu voi ban ghi khac thi hoan doi thu tu trong cung transaction. */
    public void update(Long id, String soBaoDanh, String maTruong, int thuTuNguyenVong, String nganhDangKy) {
        validateThuTu(thuTuNguyenVong);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DangKyNguyenVongDao nvDao = new DangKyNguyenVongDao(em);
            TruongDaiHocDao trDao = new TruongDaiHocDao(em);

            DangKyNguyenVong dk = nvDao.findById(id);
            if (dk == null || !soBaoDanh.equals(dk.thiSinh.soBaoDanh)) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy nguyện vọng để cập nhật.");
            }
            TruongDaiHoc tr = trDao.findByMa(maTruong.trim());
            if (tr == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy trường đại học.");
            }

            int oldThu = dk.thuTuNguyenVong;
            DangKyNguyenVong other = nvDao.findByThiSinhAndThuTu(soBaoDanh, thuTuNguyenVong);
            if (other != null && !Objects.equals(other.id, id)) {
                Integer maxOpt = nvDao.maxThuTuForThiSinh(soBaoDanh);
                int maxT = maxOpt == null ? 0 : maxOpt;
                int tempT = maxT + 1;
                dk.thuTuNguyenVong = tempT;
                nvDao.merge(dk);
                em.flush();
                other.thuTuNguyenVong = oldThu;
                nvDao.merge(other);
                em.flush();
            }

            dk.truongDaiHoc = tr;
            dk.thuTuNguyenVong = thuTuNguyenVong;
            dk.nganhDangKy = blankToNull(nganhDangKy);
            nvDao.merge(dk);
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
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    /** Xóa theo khóa id (duy nhất toàn hệ thống); không so khớp chuỗi SBD để tránh lệch định dạng/khoảng trắng. */
    public void delete(Long id) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            DangKyNguyenVongDao nvDao = new DangKyNguyenVongDao(em);
            DangKyNguyenVong dk = nvDao.findById(id);
            if (dk == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy nguyện vọng để xóa.");
            }
            nvDao.remove(dk);
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
            throw new RuntimeException(ex);
        } finally {
            em.close();
        }
    }

    private static void validateThuTu(int thuTu) {
        if (thuTu <= 0) {
            throw new IllegalStateException("Thứ tự nguyện vọng phải là số nguyên dương.");
        }
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }
}
