package com.example.quanlythisinh.dao;

import com.example.quanlythisinh.model.entity.ThiSinh;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class ThiSinhDao {
    private final EntityManager em;

    /** Ham khoi tao DAO voi EntityManager hien tai. */
    public ThiSinhDao(EntityManager em) {
        this.em = em;
    }

    /** Ham lay tat ca thi sinh de hien thi len bang. */
    public List<ThiSinh> findAll() {
        return em.createQuery("SELECT t FROM ThiSinh t ORDER BY t.soBaoDanh", ThiSinh.class).getResultList();
    }

    /** Đếm tổng số bản ghi thí sinh trong CSDL. */
    public long countAll() {
        return em.createQuery("SELECT COUNT(t) FROM ThiSinh t", Long.class).getSingleResult();
    }

    /** Ham tim thi sinh theo so bao danh phuc vu tim kiem nhanh. */
    public ThiSinh findBySoBaoDanh(String soBaoDanh) {
        return em.find(ThiSinh.class, soBaoDanh);
    }

    /**
     * Loc thi sinh: moi tham so khong rong thi them dieu kien AND (LIKE, khong phan biet hoa thuong).
     * Can it nhat mot dieu kien; goi tu service sau khi da kiem tra.
     */
    public List<ThiSinh> findByCombinedLike(String hoTenKw, String soBaoDanhKw, String noiSinhKw) {
        StringBuilder jpql = new StringBuilder("SELECT t FROM ThiSinh t WHERE 1=1");
        if (hoTenKw != null && !hoTenKw.isBlank()) {
            jpql.append(" AND LOWER(t.hoTen) LIKE :hoTen");
        }
        if (soBaoDanhKw != null && !soBaoDanhKw.isBlank()) {
            jpql.append(" AND LOWER(t.soBaoDanh) LIKE :sbd");
        }
        if (noiSinhKw != null && !noiSinhKw.isBlank()) {
            jpql.append(" AND LOWER(COALESCE(t.noiSinh, '')) LIKE :noi");
        }
        jpql.append(" ORDER BY t.soBaoDanh");
        TypedQuery<ThiSinh> q = em.createQuery(jpql.toString(), ThiSinh.class);
        if (hoTenKw != null && !hoTenKw.isBlank()) {
            q.setParameter("hoTen", "%" + hoTenKw.trim().toLowerCase() + "%");
        }
        if (soBaoDanhKw != null && !soBaoDanhKw.isBlank()) {
            q.setParameter("sbd", "%" + soBaoDanhKw.trim().toLowerCase() + "%");
        }
        if (noiSinhKw != null && !noiSinhKw.isBlank()) {
            q.setParameter("noi", "%" + noiSinhKw.trim().toLowerCase() + "%");
        }
        return q.getResultList();
    }

    /** Ham lay toan bo so bao danh phuc vu sinh SBD tu dong. */
    public List<String> findAllSoBaoDanh() {
        return em.createQuery("SELECT t.soBaoDanh FROM ThiSinh t", String.class).getResultList();
    }

    /** Ham luu moi thi sinh vao CSDL. */
    public void persist(ThiSinh thiSinh) {
        em.persist(thiSinh);
    }

    /** Ham cap nhat du lieu thi sinh da ton tai. */
    public ThiSinh merge(ThiSinh thiSinh) {
        return em.merge(thiSinh);
    }

    /** Ham xoa thi sinh khoi CSDL. */
    public void remove(ThiSinh thiSinh) {
        em.remove(thiSinh);
    }
}
