package com.example.quanlythisinh.controller;

import com.example.quanlythisinh.model.dto.DiemThiTableRow;
import com.example.quanlythisinh.model.dto.NganhTheoTruongRow;
import com.example.quanlythisinh.model.dto.ThongKeKhoiRow;
import com.example.quanlythisinh.model.dto.ThuKhoaRow;
import com.example.quanlythisinh.model.dto.TruongDaiHocTableRow;
import com.example.quanlythisinh.service.BaoCaoService;

import java.util.List;

public class TimKiemThongKeController {
    private final BaoCaoService baoCaoService;

    /** Ham khoi tao controller bao cao thong ke (tim kiem thi sinh o man Quan ly thi sinh). */
    public TimKiemThongKeController(BaoCaoService baoCaoService) {
        this.baoCaoService = baoCaoService;
    }

    /** Ham lay danh sach thu khoa tung khoi. */
    public List<ThuKhoaRow> topByKhoi() {
        return baoCaoService.thuKhoaTheoKhoi();
    }

    /** Ham lay thong ke so luong theo khoi thi. */
    public List<ThongKeKhoiRow> statByKhoi() {
        return baoCaoService.thongKeTheoKhoi();
    }

    /** Sắp xếp theo tổng điểm; {@code maKhoiOrAll} là A/B/C hoặc ALL. */
    public List<DiemThiTableRow> sortByScore(String maKhoiOrAll) {
        return baoCaoService.sortByScore(maKhoiOrAll);
    }

    /** Sắp xếp theo tổng điểm; {@code ascending} true = thấp → cao, false = cao → thấp. */
    public List<DiemThiTableRow> sortByScore(String maKhoiOrAll, boolean ascending) {
        return baoCaoService.sortByScore(maKhoiOrAll, ascending);
    }

    /** Tong so ban ghi nguyen vong trong he thong. */
    public long statTongSoNguyenVong() {
        return baoCaoService.thongKeTongSoNguyenVong();
    }

    /** Chi tiết số thí sinh theo ngành trong một trường. */
    public List<NganhTheoTruongRow> statNganhTheoTruong(String maTruong) {
        return baoCaoService.thongKeNganhTheoTruong(maTruong);
    }

    public List<TruongDaiHocTableRow> listTruongForCombo() {
        return baoCaoService.listAllTruongRows();
    }
}
