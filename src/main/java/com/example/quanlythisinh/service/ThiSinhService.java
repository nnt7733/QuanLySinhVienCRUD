package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.ThiSinhDao;
import com.example.quanlythisinh.model.dto.ThiSinhTableRow;
import com.example.quanlythisinh.model.entity.DangKyNguyenVong;
import com.example.quanlythisinh.model.entity.DiemThi;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.config.JpaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Objects;

public class ThiSinhService {
    /** Ham lay tat ca thi sinh va map sang DTO de hien thi bang. */
    public List<ThiSinhTableRow> getAllRows() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new ThiSinhDao(em).findAll().stream().map(ThiSinhService::toRow).toList();
        } finally {
            em.close();
        }
    }

    /** Tổng số thí sinh trong CSDL (dùng hiển thị thống kê). */
    public long countAll() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new ThiSinhDao(em).countAll();
        } finally {
            em.close();
        }
    }

    /**
     * Tim kiem ket hop: cac truong khong rong duoc noi bang AND (dong thoi thoa).
     */
    public List<ThiSinhTableRow> searchRowsCombined(String hoTen, String soBaoDanh, String noiSinh) {
        String h = hoTen == null ? "" : hoTen.trim();
        String s = soBaoDanh == null ? "" : soBaoDanh.trim();
        String n = noiSinh == null ? "" : noiSinh.trim();
        if (h.isBlank() && s.isBlank() && n.isBlank()) {
            return List.of();
        }
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new ThiSinhDao(em).findByCombinedLike(h, s, n).stream().map(ThiSinhService::toRow).toList();
        } finally {
            em.close();
        }
    }

    private static ThiSinhTableRow toRow(ThiSinh t) {
        return new ThiSinhTableRow(
                t.soBaoDanh,
                t.hoTen,
                t.ngaySinh,
                nz(t.danToc),
                nz(t.tonGiao),
                nz(t.gioiTinh),
                nz(t.noiSinh),
                nz(t.diaChi),
                nz(t.soCanCuoc),
                nz(t.soDienThoai),
                nz(t.email),
                nz(t.khuVuc),
                t.doiTuongUuTien,
                nz(t.hoiDongThi)
        );
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    /**
     * Gộp dữ liệu từ một dòng chỉnh sửa vào entity đã tồn tại (giữ khóa SBD).
     */
    public static void applyRowOntoEntity(ThiSinhTableRow row, ThiSinh dest) {
        Objects.requireNonNull(dest, "dest");
        dest.soBaoDanh = row.soBaoDanh();
        dest.hoTen = row.hoTen();
        dest.ngaySinh = row.ngaySinh();
        dest.danToc = blankToNull(row.danToc());
        dest.tonGiao = blankToNull(row.tonGiao());
        dest.gioiTinh = blankToNull(row.gioiTinh());
        dest.noiSinh = blankToNull(row.noiSinh());
        dest.diaChi = blankToNull(row.diaChi());
        dest.soCanCuoc = blankToNull(row.soCanCuoc());
        dest.soDienThoai = blankToNull(row.soDienThoai());
        dest.email = blankToNull(row.email());
        dest.khuVuc = blankToNull(row.khuVuc());
        dest.doiTuongUuTien = row.doiTuongUuTien();
        dest.hoiDongThi = blankToNull(row.hoiDongThi());
    }

    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Ham them thi sinh moi vao he thong va tra ve ban ghi vua tao. */
    public ThiSinh add(ThiSinh thiSinh) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            new ThiSinhDao(em).persist(thiSinh);
            tx.commit();
            return thiSinh;
        } catch (Exception ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    /** Ham cap nhat thi sinh theo so bao danh. */
    public void update(ThiSinh thiSinh) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            new ThiSinhDao(em).merge(thiSinh);
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

    /** Cập nhật thí sinh từ một dòng bảng (khóa SBD phải khớp bản ghi đã có). */
    public void updateFromRow(ThiSinhTableRow row) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            ThiSinhDao dao = new ThiSinhDao(em);
            ThiSinh existing = dao.findBySoBaoDanh(row.soBaoDanh());
            if (existing == null) {
                tx.rollback();
                throw new IllegalStateException("Không tìm thấy thí sinh.");
            }
            applyRowOntoEntity(row, existing);
            dao.merge(existing);
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

    /**
     * Xóa thí sinh theo SBD. Luôn dùng luồng này: xóa hàng loạt {@code DiemThi} và {@code DangKyNguyenVong}
     * bằng JPQL trước, rồi {@code remove(ThiSinh)} — đảm bảo xóa con kể cả khi quan hệ LAZY chưa nạp.
     * Cascade trên entity {@link ThiSinh} phục vụ thao tác đồ thị trong session khác (nếu có), không thay
     * cho bước bulk delete ở đây.
     */
    public void delete(String soBaoDanh) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Bulk delete to guarantee removal in DB even if collections are LAZY.
            em.createQuery("DELETE FROM DiemThi d WHERE d.thiSinh.soBaoDanh = :sbd")
                    .setParameter("sbd", soBaoDanh)
                    .executeUpdate();
            em.createQuery("DELETE FROM DangKyNguyenVong dk WHERE dk.thiSinh.soBaoDanh = :sbd")
                    .setParameter("sbd", soBaoDanh)
                    .executeUpdate();

            ThiSinhDao dao = new ThiSinhDao(em);
            ThiSinh t = dao.findBySoBaoDanh(soBaoDanh);
            if (t != null) {
                dao.remove(t);
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

    /**
     * Ham sinh so bao danh tiep theo: mot day so tang dan (6 chu so, vd 000001),
     * khong tien to TS, khong hau to khoi. Bo qua SBD cu khong parse duoc so.
     */
    public String generateNextSoBaoDanh() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<String> all = new ThiSinhDao(em).findAllSoBaoDanh();
            long max = 0;
            for (String sbd : all) {
                if (sbd == null) {
                    continue;
                }
                String t = sbd.trim();
                if (t.isEmpty()) {
                    continue;
                }
                try {
                    long v = Long.parseLong(t);
                    if (v > max) {
                        max = v;
                    }
                } catch (NumberFormatException ignored) {
                    // dinh dang legacy
                }
            }
            long next = max + 1;
            if (next <= 999_999L) {
                return String.format("%06d", next);
            }
            return String.valueOf(next);
        } finally {
            em.close();
        }
    }
}
