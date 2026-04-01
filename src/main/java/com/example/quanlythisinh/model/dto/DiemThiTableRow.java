package com.example.quanlythisinh.model.dto;

public record DiemThiTableRow(
        Long id,
        String soBaoDanh,
        String hoTen,
        String maKhoi,
        Double diemMon1,
        Double diemMon2,
        Double diemMon3,
        Double tong3Mon,
        Double diemUuTien,
        Double tongDiem
) {
}

