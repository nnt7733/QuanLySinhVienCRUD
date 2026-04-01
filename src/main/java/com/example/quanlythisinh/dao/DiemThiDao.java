package com.example.quanlythisinh.dao;

import com.example.quanlythisinh.model.entity.DiemThi;
import jakarta.persistence.EntityManager;

import java.util.List;

public class DiemThiDao {
    private final EntityManager em;

    /** Ham khoi tao DAO voi EntityManager hien tai. */
    public DiemThiDao(EntityManager em) {
        this.em = em;
    }

    /** Ham lay toan bo diem thi va join nguoc ve thi sinh/khoi de hien thi. */
    public List<DiemThi> findAll() {
        return em.createQuery("SELECT d FROM DiemThi d JOIN FETCH d.thiSinh JOIN FETCH d.khoiThi ORDER BY d.id", DiemThi.class)
                .getResultList();
    }

    /** Ham loc diem thi theo so bao danh thi sinh (LIKE, khong phan biet hoa thuong). */
    public List<DiemThi> findByThiSinhSoBaoDanhLike(String keywordLowerWithPercents) {
        return em.createQuery(
                        "SELECT d FROM DiemThi d JOIN FETCH d.thiSinh JOIN FETCH d.khoiThi "
                                + "WHERE LOWER(d.thiSinh.soBaoDanh) LIKE :kw ORDER BY d.thiSinh.soBaoDanh, d.id",
                        DiemThi.class)
                .setParameter("kw", keywordLowerWithPercents)
                .getResultList();
    }

    /** Ham tim diem thi theo id. */
    public DiemThi findById(Long id) {
        return em.find(DiemThi.class, id);
    }

    /** Ham tim diem thi theo cap thi sinh va khoi thi. */
    public DiemThi findByThiSinhAndKhoi(String soBaoDanh, Long khoiThiId) {
        List<DiemThi> list = em.createQuery(
                        "SELECT d FROM DiemThi d JOIN FETCH d.thiSinh JOIN FETCH d.khoiThi " +
                                "WHERE d.thiSinh.soBaoDanh = :soBaoDanh AND d.khoiThi.id = :khoiThiId",
                        DiemThi.class)
                .setParameter("soBaoDanh", soBaoDanh)
                .setParameter("khoiThiId", khoiThiId)
                .setMaxResults(1)
                .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    /** Ham luu diem thi moi. */
    public void persist(DiemThi diemThi) {
        em.persist(diemThi);
    }

    /** Ham cap nhat diem thi da ton tai. */
    public DiemThi merge(DiemThi diemThi) {
        return em.merge(diemThi);
    }

    /** Ham xoa diem thi. */
    public void remove(DiemThi diemThi) {
        em.remove(diemThi);
    }
}
