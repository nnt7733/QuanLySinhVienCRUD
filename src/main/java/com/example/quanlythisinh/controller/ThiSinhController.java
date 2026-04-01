package com.example.quanlythisinh.controller;

import com.example.quanlythisinh.model.dto.ThiSinhTableRow;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.service.ThiSinhService;

import java.util.List;

public class ThiSinhController {
    private final ThiSinhService thiSinhService;

    /** Ham khoi tao controller quan ly thi sinh. */
    public ThiSinhController(ThiSinhService thiSinhService) {
        this.thiSinhService = thiSinhService;
    }

    /** Ham lay danh sach thi sinh hien thi bang. */
    public List<ThiSinhTableRow> list() {
        return thiSinhService.getAllRows();
    }

    /** Tổng số thí sinh trong CSDL. */
    public long countAll() {
        return thiSinhService.countAll();
    }

    /** Ham loc bang: cac dieu kien khong rong ket hop AND (ho ten, SBD, noi sinh — LIKE). */
    public List<ThiSinhTableRow> searchCombined(String hoTen, String soBaoDanh, String noiSinh) {
        return thiSinhService.searchRowsCombined(hoTen, soBaoDanh, noiSinh);
    }

    /** Ham them thi sinh moi. */
    public ThiSinh add(ThiSinh thiSinh) {
        return thiSinhService.add(thiSinh);
    }

    /** Ham cap nhat thi sinh. */
    public void update(ThiSinh thiSinh) {
        thiSinhService.update(thiSinh);
    }

    /** Cập nhật thí sinh từ dòng bảng đầy đủ cột. */
    public void updateFromRow(ThiSinhTableRow row) {
        thiSinhService.updateFromRow(row);
    }

    /** Ham xoa thi sinh theo so bao danh. */
    public void delete(String soBaoDanh) {
        thiSinhService.delete(soBaoDanh);
    }

    /** Ham sinh so bao danh tiep theo (day so tang dan, 6 chu so). */
    public String generateNextSoBaoDanh() {
        return thiSinhService.generateNextSoBaoDanh();
    }
}
