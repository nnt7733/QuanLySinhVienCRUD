package com.example.quanlythisinh.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "KhoiThi")
public class KhoiThi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 10)
    public String maKhoi;

    @Column(nullable = false, length = 50)
    public String tenKhoi;

    @Column(nullable = false, length = 50)
    public String mon1;

    @Column(nullable = false, length = 50)
    public String mon2;

    @Column(nullable = false, length = 50)
    public String mon3;

    @OneToMany(mappedBy = "khoiThi")
    public List<DiemThi> diemThiList = new ArrayList<>();

    /** Empty constructor for JPA. */
    public KhoiThi() {
    }
}

