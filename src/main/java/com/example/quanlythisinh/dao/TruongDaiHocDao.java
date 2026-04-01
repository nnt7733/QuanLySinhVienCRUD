package com.example.quanlythisinh.dao;

import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import jakarta.persistence.EntityManager;

import java.util.List;

public class TruongDaiHocDao {
    private final EntityManager em;

    /** Ham khoi tao DAO voi EntityManager hien tai. */
    public TruongDaiHocDao(EntityManager em) {
        this.em = em;
    }

    /** Ham lay tat ca truong dai hoc. */
    public List<TruongDaiHoc> findAll() {
        return em.createQuery("SELECT t FROM TruongDaiHoc t ORDER BY t.maTruong", TruongDaiHoc.class).getResultList();
    }

    /** Ham tim truong theo ma truong (PK). */
    public TruongDaiHoc findByMa(String maTruong) {
        return em.find(TruongDaiHoc.class, maTruong);
    }

    /** Ham them truong moi. */
    public void persist(TruongDaiHoc t) {
        em.persist(t);
    }

    /** Ham cap nhat truong. */
    public TruongDaiHoc merge(TruongDaiHoc t) {
        return em.merge(t);
    }

    /** Ham xoa truong (goi sau khi da kiem tra khong con nguyen vong). */
    public void remove(TruongDaiHoc t) {
        em.remove(em.contains(t) ? t : em.merge(t));
    }

    /** Ham dem so nguyen vong dang tham chieu toi truong nay. */
    public long countNguyenVongByMaTruong(String maTruong) {
        Long n = em.createQuery(
                        "SELECT COUNT(dk.id) FROM DangKyNguyenVong dk WHERE dk.truongDaiHoc.maTruong = :ma",
                        Long.class)
                .setParameter("ma", maTruong)
                .getSingleResult();
        return n == null ? 0L : n;
    }
}
