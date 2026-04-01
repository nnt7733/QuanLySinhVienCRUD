package com.example.quanlythisinh.controller;

import com.example.quanlythisinh.model.dto.DangKyNguyenVongTableRow;
import com.example.quanlythisinh.model.dto.TruongDaiHocTableRow;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import com.example.quanlythisinh.service.DangKyNguyenVongService;
import com.example.quanlythisinh.service.LookupService;
import com.example.quanlythisinh.service.TruongDaiHocService;

import java.util.List;
import java.util.Map;

public class NguyenVongController {
    private final TruongDaiHocService truongDaiHocService;
    private final DangKyNguyenVongService dangKyNguyenVongService;
    private final LookupService lookupService;

    /** Ham khoi tao controller quan ly nguyen vong va truong DH. */
    public NguyenVongController(
            TruongDaiHocService truongDaiHocService,
            DangKyNguyenVongService dangKyNguyenVongService,
            LookupService lookupService
    ) {
        this.truongDaiHocService = truongDaiHocService;
        this.dangKyNguyenVongService = dangKyNguyenVongService;
        this.lookupService = lookupService;
    }

    /** Danh sách thí sinh cho combo / lọc (không để View gọi LookupService trực tiếp). */
    public List<ThiSinh> listAllThiSinhForLookup() {
        return lookupService.getAllThiSinh();
    }

    /** Danh sách trường cho combo editor mã trường / thêm NV. */
    public List<TruongDaiHoc> listAllTruongForLookup() {
        return lookupService.getAllTruongDaiHoc();
    }

    public List<TruongDaiHocTableRow> listTruongRows() {
        return truongDaiHocService.getAllRows();
    }

    public void addTruong(String maTruong, String tenTruong, String diaChi) {
        truongDaiHocService.add(maTruong, tenTruong, diaChi);
    }

    public void updateTruong(String maTruong, String tenTruong, String diaChi) {
        truongDaiHocService.update(maTruong, tenTruong, diaChi);
    }

    public void deleteTruong(String maTruong) {
        truongDaiHocService.delete(maTruong);
    }

    public List<DangKyNguyenVongTableRow> listNguyenVongByThiSinh(String soBaoDanh) {
        return dangKyNguyenVongService.getRowsBySoBaoDanh(soBaoDanh);
    }

    public void addNguyenVong(String soBaoDanh, String maTruong, int thuTuNguyenVong, String nganhDangKy) {
        dangKyNguyenVongService.add(soBaoDanh, maTruong, thuTuNguyenVong, nganhDangKy);
    }

    public void updateNguyenVong(Long id, String soBaoDanh, String maTruong, int thuTuNguyenVong, String nganhDangKy) {
        dangKyNguyenVongService.update(id, soBaoDanh, maTruong, thuTuNguyenVong, nganhDangKy);
    }

    /** Chỉ đổi thứ tự các nguyện vọng (map id NV → thứ tự mới 1..n). */
    public void reorderNguyenVongThuTu(String soBaoDanh, Map<Long, Integer> idToNewThuTu) {
        dangKyNguyenVongService.reorderThuTu(soBaoDanh, idToNewThuTu);
    }

    public void deleteNguyenVong(Long id) {
        dangKyNguyenVongService.delete(id);
    }

    public int suggestNextThuTu(String soBaoDanh) {
        return dangKyNguyenVongService.suggestNextThuTu(soBaoDanh);
    }
}
