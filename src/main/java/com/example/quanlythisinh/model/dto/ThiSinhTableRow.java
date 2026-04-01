package com.example.quanlythisinh.model.dto;

import java.time.LocalDate;

public record ThiSinhTableRow(
        String soBaoDanh,
        String hoTen,
        LocalDate ngaySinh,
        String danToc,
        String tonGiao,
        String gioiTinh,
        String noiSinh,
        String diaChi,
        String soCanCuoc,
        String soDienThoai,
        String email,
        String khuVuc,
        int doiTuongUuTien,
        String hoiDongThi
) {
}

