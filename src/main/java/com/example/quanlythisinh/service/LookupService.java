package com.example.quanlythisinh.service;

import com.example.quanlythisinh.dao.KhoiThiDao;
import com.example.quanlythisinh.dao.ThiSinhDao;
import com.example.quanlythisinh.dao.TruongDaiHocDao;
import com.example.quanlythisinh.model.entity.KhoiThi;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import com.example.quanlythisinh.config.JpaUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class LookupService {
    /** Ham lay danh sach thi sinh de do vao combobox. */
    public List<ThiSinh> getAllThiSinh() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new ThiSinhDao(em).findAll();
        } finally {
            em.close();
        }
    }

    /** Ham lay danh sach khoi thi de do vao combobox. */
    public List<KhoiThi> getAllKhoiThi() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new KhoiThiDao(em).findAll();
        } finally {
            em.close();
        }
    }

    /** Ham lay danh sach truong dai hoc de do vao combobox. */
    public List<TruongDaiHoc> getAllTruongDaiHoc() {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return new TruongDaiHocDao(em).findAll();
        } finally {
            em.close();
        }
    }
}
