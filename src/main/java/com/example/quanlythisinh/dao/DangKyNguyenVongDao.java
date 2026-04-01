package com.example.quanlythisinh.dao;

import com.example.quanlythisinh.model.entity.DangKyNguyenVong;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DangKyNguyenVongDao {
    private final EntityManager em;

    /** Ham khoi tao DAO voi EntityManager hien tai. */
    public DangKyNguyenVongDao(EntityManager em) {
        this.em = em;
    }

    /** Ham lay danh sach nguyen vong cua thi sinh theo thu tu NV (fetch quan he de dung sau khi dong EM). */
    public List<DangKyNguyenVong> findByThiSinhSoBaoDanhOrderByThuTu(String soBaoDanh) {
        return em.createQuery(
                        "SELECT dk FROM DangKyNguyenVong dk JOIN FETCH dk.thiSinh JOIN FETCH dk.truongDaiHoc "
                                + "WHERE dk.thiSinh.soBaoDanh = :sbd ORDER BY dk.thuTuNguyenVong",
                        DangKyNguyenVong.class)
                .setParameter("sbd", soBaoDanh)
                .getResultList();
    }

    /** Ham tim nguyen vong theo id. */
    public DangKyNguyenVong findById(Long id) {
        return em.find(DangKyNguyenVong.class, id);
    }

    /** Ham tim nguyen vong theo thi sinh va thu tu (neu co). */
    public DangKyNguyenVong findByThiSinhAndThuTu(String soBaoDanh, int thuTu) {
        List<DangKyNguyenVong> list = em.createQuery(
                        "SELECT dk FROM DangKyNguyenVong dk WHERE dk.thiSinh.soBaoDanh = :sbd AND dk.thuTuNguyenVong = :tt",
                        DangKyNguyenVong.class)
                .setParameter("sbd", soBaoDanh)
                .setParameter("tt", thuTu)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    /** Ham them nguyen vong. */
    public void persist(DangKyNguyenVong dk) {
        em.persist(dk);
    }

    /** Ham cap nhat nguyen vong. */
    public DangKyNguyenVong merge(DangKyNguyenVong dk) {
        return em.merge(dk);
    }

    /** Ham xoa nguyen vong. */
    public void remove(DangKyNguyenVong dk) {
        em.remove(em.contains(dk) ? dk : em.merge(dk));
    }

    /** Ham lay max thu tu NV cua thi sinh; khong co thi null. */
    public Integer maxThuTuForThiSinh(String soBaoDanh) {
        return em.createQuery(
                        "SELECT MAX(dk.thuTuNguyenVong) FROM DangKyNguyenVong dk WHERE dk.thiSinh.soBaoDanh = :sbd",
                        Integer.class)
                .setParameter("sbd", soBaoDanh)
                .getSingleResult();
    }

    /** Tong so ban ghi nguyen vong trong he thong. */
    public long countAll() {
        Long n = em.createQuery("SELECT COUNT(dk.id) FROM DangKyNguyenVong dk", Long.class).getSingleResult();
        return n == null ? 0L : n;
    }

    /** Ham thong ke so thi sinh distinct theo nganh dang ky trong mot truong. */
    public List<Object[]> countDistinctThiSinhByNganhForTruong(String maTruong) {
        return em.createQuery(
                        "SELECT dk.nganhDangKy, COUNT(DISTINCT dk.thiSinh.soBaoDanh) "
                                + "FROM DangKyNguyenVong dk WHERE dk.truongDaiHoc.maTruong = :ma "
                                + "GROUP BY dk.nganhDangKy ORDER BY COUNT(DISTINCT dk.thiSinh.soBaoDanh) DESC",
                        Object[].class)
                .setParameter("ma", maTruong)
                .getResultList();
    }
}
