package com.example.quanlythisinh.controller;

import com.example.quanlythisinh.model.dto.DiemThiTableRow;
import com.example.quanlythisinh.model.entity.KhoiThi;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.service.DiemThiService;

import java.util.List;

public class DiemThiController {
    private final DiemThiService diemThiService;

    /** Ham khoi tao controller quan ly diem thi. */
    public DiemThiController(DiemThiService diemThiService) {
        this.diemThiService = diemThiService;
    }

    /** Ham lay danh sach diem thi hien thi bang. */
    public List<DiemThiTableRow> list() {
        return diemThiService.getAllRows();
    }

    /** Dòng thiếu điểm lên trước (giữ thứ tự trong nhóm). */
    public List<DiemThiTableRow> orderIncompleteScoresFirst(List<DiemThiTableRow> rows) {
        return diemThiService.orderIncompleteScoresFirst(rows);
    }

    /** Ham loc bang diem theo so bao danh (chuoi con). */
    public List<DiemThiTableRow> listBySoBaoDanhFilter(String keyword) {
        return diemThiService.getRowsBySoBaoDanhLike(keyword);
    }

    public List<ThiSinh> listAllThiSinh() {
        return diemThiService.findAllThiSinh();
    }

    public List<KhoiThi> listAllKhoi() {
        return diemThiService.findAllKhoi();
    }

    /** Tạo dòng điểm trống (NULL) nếu chưa có; trả về true nếu vừa tạo. */
    public boolean ensureDiemThiRow(String soBaoDanh, Long khoiThiId) {
        return diemThiService.ensureDiemThiRowExists(soBaoDanh, khoiThiId);
    }

    /** Ham cap nhat diem thi theo cap thi sinh-khoi, neu chua co thi tao moi. */
    public void upsert(String soBaoDanh, Long khoiThiId, double mon1, double mon2, double mon3) {
        diemThiService.upsertByThiSinhAndKhoi(soBaoDanh, khoiThiId, mon1, mon2, mon3);
    }

    /** Cập nhật điểm ba môn theo id dòng bảng (null = trống). */
    public void updateScoresById(Long id, Double mon1, Double mon2, Double mon3) {
        diemThiService.updateScoresById(id, mon1, mon2, mon3);
    }

    /** Xóa điểm: đặt ba cột điểm thành NULL, giữ bản ghi. */
    public void clearScoresById(Long diemThiId) {
        diemThiService.clearScoresById(diemThiId);
    }

    /** Ham tim 1 dong diem theo cap thi sinh-khoi de hien thi len form. */
    public DiemThiTableRow findRow(String soBaoDanh, Long khoiThiId) {
        return diemThiService.findRowByThiSinhAndKhoi(soBaoDanh, khoiThiId);
    }
}
