-- ============================================================
-- Bước 3: Nạp dữ liệu mẫu (sau 02_create_tables.sql)
-- ============================================================

USE quan_ly_thi_sinh;

INSERT INTO KhoiThi(maKhoi, tenKhoi, mon1, mon2, mon3) VALUES
('A', 'Khối A', 'Toán', 'Lý', 'Hóa'),
('B', 'Khối B', 'Toán', 'Hóa', 'Sinh'),
('C', 'Khối C', 'Văn', 'Sử', 'Địa');

INSERT INTO TruongDaiHoc(maTruong, tenTruong, diaChi) VALUES
('BKA', 'Đại học Bách Khoa Hà Nội', 'Hà Nội'),
('QSB', 'Đại học Khoa học Tự nhiên', 'TP Hồ Chí Minh'),
('KTH', 'Đại học Kinh tế Quốc dân', 'Hà Nội'),
('DNY', 'Đại học Đà Nẵng', 'Đà Nẵng'),
('CTU', 'Đại học Cần Thơ', 'Cần Thơ');

INSERT INTO ThiSinh(soBaoDanh, hoTen, ngaySinh, danToc, tonGiao, gioiTinh, noiSinh, diaChi, soCanCuoc, soDienThoai, email, khuVuc, doiTuongUuTien, hoiDongThi) VALUES
('000001', 'Nguyễn Văn An', '2007-03-12', 'Kinh', 'Không', 'Nam', 'Hà Nội', 'Ba Đình, Hà Nội', '001234567890', '0901000001', '000001@example.com', 'KV3', 0, 'Hà Nội'),
('000002', 'Trần Thị Bình', '2006-07-04', 'Kinh', 'Phật giáo', 'Nữ', 'Nghệ An', 'Vinh, Nghệ An', '001234567891', '0901000002', '000002@example.com', 'KV2', 1, 'Nghệ An'),
('000003', 'Lê Quang Huy', '2007-01-20', 'Kinh', 'Không', 'Nam', 'Đà Nẵng', 'Hải Châu, Đà Nẵng', '001234567892', '0901000003', '000003@example.com', 'KV2-NT', 2, 'Đà Nẵng'),
('000004', 'Phạm Gia Linh', '2006-11-15', 'Kinh', 'Công giáo', 'Nữ', 'Thanh Hóa', 'Thanh Hóa', '001234567893', '0901000004', '000004@example.com', 'KV1', 1, 'Thanh Hóa'),
('000005', 'Đỗ Minh Quân', '2007-09-02', 'Kinh', 'Không', 'Nam', 'Cần Thơ', 'Ninh Kiều, Cần Thơ', '001234567894', '0901000005', '000005@example.com', 'KV3', 0, 'Cần Thơ'),
('000006', 'Vũ Thị Mai', '2006-05-10', 'Kinh', 'Không', 'Nữ', 'Hải Phòng', 'Lê Chân, Hải Phòng', '001234567895', '0901000006', '000006@example.com', 'KV2', 0, 'Hải Phòng'),
('000007', 'Hoàng Đức Thắng', '2007-12-01', 'Kinh', 'Không', 'Nam', 'Hà Tĩnh', 'Hà Tĩnh', '001234567896', '0901000007', '000007@example.com', 'KV2-NT', 2, 'Hà Tĩnh'),
('000008', 'Bùi Khánh Chi', '2006-02-19', 'Kinh', 'Phật giáo', 'Nữ', 'An Giang', 'Long Xuyên, An Giang', '001234567897', '0901000008', '000008@example.com', 'KV1', 2, 'An Giang');

INSERT INTO DiemThi(thiSinh_soBaoDanh, khoiThi_id, diemMon1, diemMon2, diemMon3) VALUES
('000001', 1, 8.50, 8.00, 7.75),
('000002', 1, 7.80, 8.20, 8.40),
('000003', 2, 9.00, 8.60, 8.10),
('000004', 3, 8.70, 8.30, 8.90),
('000005', 2, 7.50, 7.70, 8.20),
('000006', 3, 8.10, 7.90, 8.00),
('000007', 1, 9.20, 9.00, 8.80),
('000008', 3, 9.00, 8.50, 8.60);

INSERT INTO DangKyNguyenVong(thiSinh_soBaoDanh, truongDaiHoc_maTruong, thuTuNguyenVong, nganhDangKy) VALUES
('000001', 'BKA', 1, 'Kỹ thuật phần mềm'),
('000001', 'KTH', 2, 'Kinh tế đầu tư'),
('000002', 'KTH', 1, 'Kế toán'),
('000002', 'BKA', 2, 'Công nghệ thông tin'),
('000003', 'QSB', 1, 'Khoa học máy tính'),
('000003', 'DNY', 2, 'Công nghệ sinh học'),
('000004', 'CTU', 1, 'Sư phạm Văn'),
('000004', 'KTH', 2, 'Luật kinh tế'),
('000005', 'DNY', 1, 'Công nghệ hóa học'),
('000006', 'QSB', 1, 'Văn học'),
('000007', 'BKA', 1, 'Kỹ thuật điều khiển'),
('000008', 'CTU', 1, 'Quản trị kinh doanh');
