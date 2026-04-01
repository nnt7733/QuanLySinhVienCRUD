package com.example.quanlythisinh.model.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "DiemThi",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_diem_thisinh_khoithi",
                columnNames = {"thiSinh_soBaoDanh", "khoiThi_id"}
        )
)
public class DiemThi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "thiSinh_soBaoDanh", nullable = false, referencedColumnName = "soBaoDanh")
    public ThiSinh thiSinh;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "khoiThi_id", nullable = false)
    public KhoiThi khoiThi;

    /** Can be NULL when scores were cleared (keep row for (thi sinh, khoi)). */
    @Column(nullable = true)
    public Double diemMon1;

    @Column(nullable = true)
    public Double diemMon2;

    @Column(nullable = true)
    public Double diemMon3;

    /** Empty constructor for JPA. */
    public DiemThi() {
    }
}

