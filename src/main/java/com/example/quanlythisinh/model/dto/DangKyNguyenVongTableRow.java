package com.example.quanlythisinh.model.dto;

/** A row shown in Nguyen Vong table. */
public record DangKyNguyenVongTableRow(
        Long id,
        String soBaoDanh,
        String hoTen,
        String maTruong,
        String tenTruong,
        int thuTuNguyenVong,
        String nganhDangKy
) {
}

