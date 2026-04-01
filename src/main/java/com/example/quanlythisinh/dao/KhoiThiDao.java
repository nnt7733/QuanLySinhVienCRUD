package com.example.quanlythisinh.dao;

import com.example.quanlythisinh.model.entity.KhoiThi;
import jakarta.persistence.EntityManager;

import java.util.List;

public class KhoiThiDao {
    private final EntityManager em;

    /** Ham khoi tao DAO voi EntityManager hien tai. */
    public KhoiThiDao(EntityManager em) {
        this.em = em;
    }

    /** Ham lay tat ca khoi thi de dua vao combobox. */
    public List<KhoiThi> findAll() {
        return em.createQuery("SELECT k FROM KhoiThi k ORDER BY k.maKhoi", KhoiThi.class).getResultList();
    }

    /** Ham tim khoi thi theo id. */
    public KhoiThi findById(Long id) {
        return em.find(KhoiThi.class, id);
    }

    /** Ham tim khoi thi theo ma khoi (A/B/C). */
    public KhoiThi findByMaKhoi(String maKhoi) {
        List<KhoiThi> list = em.createQuery("SELECT k FROM KhoiThi k WHERE k.maKhoi = :ma", KhoiThi.class)
                .setParameter("ma", maKhoi)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }
}
