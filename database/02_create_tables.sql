USE quan_ly_thi_sinh;

DROP TABLE IF EXISTS DangKyNguyenVong;
DROP TABLE IF EXISTS DiemThi;
DROP TABLE IF EXISTS TruongDaiHoc;
DROP TABLE IF EXISTS KhoiThi;
DROP TABLE IF EXISTS ThiSinh;

CREATE TABLE ThiSinh (
    soBaoDanh VARCHAR(20) PRIMARY KEY,
    hoTen VARCHAR(120) NOT NULL,
    ngaySinh DATE NOT NULL,
    danToc VARCHAR(50),
    tonGiao VARCHAR(50),
    gioiTinh VARCHAR(10),
    noiSinh VARCHAR(120),
    diaChi VARCHAR(255),
    soCanCuoc VARCHAR(20) UNIQUE,
    soDienThoai VARCHAR(20),
    email VARCHAR(100),
    khuVuc VARCHAR(20),
    doiTuongUuTien INT DEFAULT 0,
    hoiDongThi VARCHAR(50)
);

CREATE TABLE KhoiThi (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    maKhoi VARCHAR(10) NOT NULL UNIQUE,
    tenKhoi VARCHAR(50) NOT NULL,
    mon1 VARCHAR(50) NOT NULL,
    mon2 VARCHAR(50) NOT NULL,
    mon3 VARCHAR(50) NOT NULL
);

CREATE TABLE DiemThi (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thiSinh_soBaoDanh VARCHAR(20) NOT NULL,
    khoiThi_id BIGINT NOT NULL,
    diemMon1 DECIMAL(4,2) NULL,
    diemMon2 DECIMAL(4,2) NULL,
    diemMon3 DECIMAL(4,2) NULL,
    CONSTRAINT fk_diem_thisinh FOREIGN KEY (thiSinh_soBaoDanh) REFERENCES ThiSinh(soBaoDanh),
    CONSTRAINT fk_diem_khoithi FOREIGN KEY (khoiThi_id) REFERENCES KhoiThi(id),
    CONSTRAINT uk_diem_thisinh_khoithi UNIQUE (thiSinh_soBaoDanh, khoiThi_id)
);

CREATE TABLE TruongDaiHoc (
    maTruong VARCHAR(20) PRIMARY KEY,
    tenTruong VARCHAR(150) NOT NULL,
    diaChi VARCHAR(255)
);

CREATE TABLE DangKyNguyenVong (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    thiSinh_soBaoDanh VARCHAR(20) NOT NULL,
    truongDaiHoc_maTruong VARCHAR(20) NOT NULL,
    thuTuNguyenVong INT NOT NULL,
    nganhDangKy VARCHAR(120),
    CONSTRAINT fk_nv_thisinh FOREIGN KEY (thiSinh_soBaoDanh) REFERENCES ThiSinh(soBaoDanh),
    CONSTRAINT fk_nv_truong FOREIGN KEY (truongDaiHoc_maTruong) REFERENCES TruongDaiHoc(maTruong),
    CONSTRAINT uk_nv_thisinh_thutu UNIQUE (thiSinh_soBaoDanh, thuTuNguyenVong)
);
