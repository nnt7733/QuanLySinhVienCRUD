package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.TruongDaiHocDao;
import com.example.quanlythisinh.model.dto.TruongDaiHocTableRow;
import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import com.example.quanlythisinh.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;

public class TruongDaiHocService {

    /** Ham lay tat ca truong de hien thi bang. */
    public List<TruongDaiHocTableRow> getAllRows() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new TruongDaiHocDao(em).findAll().stream()
                    .map(t -> new TruongDaiHocTableRow(t.maTruong, t.tenTruong, t.diaChi))
                    .toList();
        } finally {
            em.close();
        }
    }

    /** Ham them truong dai hoc moi. */
    public void add(String maTruong, String tenTruong, String diaChi) {
        requireMaTen(maTruong, tenTruong);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TruongDaiHoc t = new TruongDaiHoc();
            t.maTruong = maTruong.trim();
            t.tenTruong = tenTruong.trim();
            t.diaChi = diaChi == null || diaChi.isBlank() ? null : diaChi.trim();
            new TruongDaiHocDao(em).persist(t);
            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw new IllegalStateException("Không thể thêm trường (mã trường có thể đã tồn tại).", ex);
        } finally {
            em.close();
        }
    }

    /** Ham cap nhat ten va dia chi; ma truong khong doi. */
    public void update(String maTruong, String tenTruong, String diaChi) {
        requireMaTen(maTruong, tenTruong);
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TruongDaiHocDao dao = new TruongDaiHocDao(em);
            TruongDaiHoc t = dao.findByMa(maTruong.trim());
            if (t == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy trường để cập nhật.");
            }
            t.tenTruong = tenTruong.trim();
            t.diaChi = diaChi == null || diaChi.isBlank() ? null : diaChi.trim();
            dao.merge(t);
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
            throw new IllegalStateException("Không thể cập nhật trường.", ex);
        } finally {
            em.close();
        }
    }

    /** Ham xoa truong neu khong con nguyen vong tham chieu. */
    public void delete(String maTruong) {
        if (maTruong == null || maTruong.isBlank()) {
            throw new IllegalStateException("Vui lòng chọn hoặc nhập mã trường.");
        }
        String ma = maTruong.trim();
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TruongDaiHocDao dao = new TruongDaiHocDao(em);
            TruongDaiHoc t = dao.findByMa(ma);
            if (t == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy trường để xóa.");
            }
            if (dao.countNguyenVongByMaTruong(ma) > 0) {
                tx.rollback();
                throw new IllegalStateException("Không thể xóa: vẫn còn nguyện vọng đăng ký cho trường này.");
            }
            dao.remove(t);
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

    private static void requireMaTen(String maTruong, String tenTruong) {
        if (maTruong == null || maTruong.isBlank()) {
            throw new IllegalStateException("Vui lòng nhập mã trường.");
        }
        if (tenTruong == null || tenTruong.isBlank()) {
            throw new IllegalStateException("Vui lòng nhập tên trường.");
        }
    }
}
