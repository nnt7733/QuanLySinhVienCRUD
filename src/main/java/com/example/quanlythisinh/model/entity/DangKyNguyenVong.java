package com.example.quanlythisinh.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "DangKyNguyenVong",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_nv_thisinh_thutu",
                columnNames = {"thiSinh_soBaoDanh", "thuTuNguyenVong"}
        )
)
public class DangKyNguyenVong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "thiSinh_soBaoDanh", nullable = false, referencedColumnName = "soBaoDanh")
    public ThiSinh thiSinh;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "truongDaiHoc_maTruong", nullable = false, referencedColumnName = "maTruong")
    public TruongDaiHoc truongDaiHoc;

    @Column(nullable = false)
    public int thuTuNguyenVong;

    /** Can be NULL: major is optional. */
    @Column(length = 120, nullable = true)
    public String nganhDangKy;

    /** Empty constructor for JPA. */
    public DangKyNguyenVong() {
    }
}

