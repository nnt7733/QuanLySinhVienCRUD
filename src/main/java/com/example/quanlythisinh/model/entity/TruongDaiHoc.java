package com.example.quanlythisinh.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TruongDaiHoc")
public class TruongDaiHoc {
    @Id
    @Column(nullable = false, length = 20)
    public String maTruong;

    @Column(nullable = false, length = 150)
    public String tenTruong;

    @Column(length = 255)
    public String diaChi;

    @OneToMany(mappedBy = "truongDaiHoc")
    public List<DangKyNguyenVong> dangKyNguyenVongList = new ArrayList<>();

    /** Empty constructor for JPA. */
    public TruongDaiHoc() {
    }
}

