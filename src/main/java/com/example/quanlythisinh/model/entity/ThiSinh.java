package com.example.quanlythisinh.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ThiSinh")
public class ThiSinh {
    @Id
    public String soBaoDanh;

    @Column(nullable = false, length = 120)
    public String hoTen;

    @Column(nullable = false)
    public LocalDate ngaySinh;

    @Column(length = 50)
    public String danToc;

    @Column(length = 50)
    public String tonGiao;

    @Column(length = 10)
    public String gioiTinh;

    @Column(length = 120)
    public String noiSinh;

    @Column(length = 255)
    public String diaChi;

    @Column(length = 20, unique = true)
    public String soCanCuoc;

    @Column(length = 20)
    public String soDienThoai;

    @Column(length = 100)
    public String email;

    @Column(length = 20)
    public String khuVuc;

    @Column
    public int doiTuongUuTien;

    @Column(length = 50)
    public String hoiDongThi;

    /**
     * Khi xóa thí sinh trong ứng dụng, dùng {@link com.example.quanlythisinh.service.ThiSinhService#delete(String)}
     * (bulk JPQL + remove). Cascade/orphan ở đây hỗ trợ thao tác persistence trong session khác, không thay
     * cho luồng xóa qua service.
     */
    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<DiemThi> diemThiList = new ArrayList<>();

    /** @see #diemThiList */
    @OneToMany(mappedBy = "thiSinh", cascade = CascadeType.ALL, orphanRemoval = true)
    public List<DangKyNguyenVong> dangKyNguyenVongList = new ArrayList<>();

    /** Empty constructor for JPA. */
    public ThiSinh() {
    }
}

