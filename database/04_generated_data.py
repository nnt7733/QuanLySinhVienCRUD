import argparse
import random
from datetime import date, timedelta
from pathlib import Path

_SCRIPT_DIR = Path(__file__).resolve().parent
_DEFAULT_SQL_OUT = _SCRIPT_DIR / "generated_exam_data.sql"

KHOI_ORDER = ["A", "B", "C"]

TEN_DEM = [
    "Văn", "Thị", "Ngọc", "Quốc", "Đức", "Gia", "Minh", "Bảo", "Thanh", "Hoàng",
    "Thu", "Hữu", "Đình", "Xuân", "Hồng", "Kim", "Nhật", "Tuấn", "Phương", "Diệu",
]

TEN = [
    "An", "Bình", "Chi", "Dũng", "Giang", "Hạnh", "Khánh", "Linh", "Minh", "Nam",
    "Oanh", "Phúc", "Quân", "Trang", "Tuấn", "Vy", "Yến", "Huy", "Nhi", "Thảo",
    "Mai", "Lan", "Hùng", "Kiên", "Phương", "Hà", "Đức", "Trung", "Sơn", "Vân",
]

HO = [
    "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Phan", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ",
    "Huỳnh", "Dương", "Lý", "Tôn", "Đinh", "Mai", "Cao", "Đoàn",
]

NOI_SINH = [
    "Hà Nội", "TP. Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng", "Nghệ An",
    "Thanh Hóa", "An Giang", "Huế", "Lâm Đồng", "Khánh Hòa", "Bình Dương",
    "Đồng Nai", "Quảng Nam", "Hà Tĩnh", "Nam Định",
]

DIA_CHI = [
    "Quận 1, TP. Hồ Chí Minh", "Ba Đình, Hà Nội", "Hải Châu, Đà Nẵng",
    "Ninh Kiều, Cần Thơ", "Lê Chân, Hải Phòng", "Đống Đa, Hà Nội",
    "Tân Bình, TP. Hồ Chí Minh", "Gò Vấp, TP. Hồ Chí Minh", "Vinh, Nghệ An",
    "Thành phố Thanh Hóa", "Long Xuyên, An Giang", "Nha Trang, Khánh Hòa",
]

KHU_VUC = ["KV1", "KV2", "KV2-NT", "KV3"]
TRUONG_MA_CODES = ["BKA", "QSB", "KTH", "DNY", "CTU"]
KHOI_TO_ID = {"A": 1, "B": 2, "C": 3}

NGANH_DANG_KY = [
    "Công nghệ thông tin",
    "Kỹ thuật phần mềm",
    "Kinh tế",
    "Kế toán",
    "Sư phạm Toán",
    "Sư phạm Văn",
    "Công nghệ sinh học",
    "Quản trị kinh doanh",
    "Luật kinh tế",
    "Tài chính – Ngân hàng",
    "Marketing",
    "Y đa khoa",
    "Dược học",
    "Kiến trúc",
    "Kỹ thuật xây dựng",
]

# DDL + INSERT khối / trường (CREATE DATABASE + USE ghi trong generate_full_install).
SQL_SCHEMA_TEMPLATE = """
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO KhoiThi(maKhoi, tenKhoi, mon1, mon2, mon3) VALUES
('A', 'Khối A', 'Toán', 'Lý', 'Hóa'),
('B', 'Khối B', 'Toán', 'Hóa', 'Sinh'),
('C', 'Khối C', 'Văn', 'Sử', 'Địa');

INSERT INTO TruongDaiHoc(maTruong, tenTruong, diaChi) VALUES
('BKA', 'Đại học Bách khoa Hà Nội', 'Hà Nội'),
('QSB', 'Đại học Khoa học Tự nhiên', 'TP. Hồ Chí Minh'),
('KTH', 'Đại học Kinh tế Quốc dân', 'Hà Nội'),
('DNY', 'Đại học Đà Nẵng', 'Đà Nẵng'),
('CTU', 'Đại học Cần Thơ', 'Cần Thơ');
""".strip()


def random_name() -> str:
    return f"{random.choice(HO)} {random.choice(TEN_DEM)} {random.choice(TEN)}"


def random_birth() -> date:
    start = date(2006, 1, 1)
    end = date(2007, 12, 31)
    delta = (end - start).days
    return start + timedelta(days=random.randint(0, delta))


def build_sbd(seq: int) -> str:
    return f"{seq:06d}"


def split_students_by_khoi(total: int, n_khoi: int) -> list[int]:
    base, rem = divmod(total, n_khoi)
    return [base + (1 if i < rem else 0) for i in range(n_khoi)]


def sql_quote(value: str) -> str:
    return "'" + value.replace("'", "''") + "'"


def escape_db_name(name: str) -> str:
    return name.strip().replace("`", "``")


def sql_use_database(database: str) -> str:
    safe = escape_db_name(database)
    if not safe:
        raise ValueError("database rỗng")
    return f"USE `{safe}`;"


def sql_clear_transactional_tables() -> list[str]:
    return [
        "-- Chỉ làm mới thí sinh / điểm / NV (bảng đã tồn tại)",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "TRUNCATE TABLE DangKyNguyenVong;",
        "TRUNCATE TABLE DiemThi;",
        "TRUNCATE TABLE ThiSinh;",
        "SET FOREIGN_KEY_CHECKS = 1;",
        "",
    ]


def unique_cccd_for(global_seq: int) -> str:
    return f"1{global_seq:011d}"


def unique_sdt_for(global_index: int) -> str:
    return f"09{(global_index - 1) % 100_000_000:08d}"


def build_data_inserts(students: int) -> list[str]:
    """Sinh các dòng INSERT ThiSinh, DiemThi, DangKyNguyenVong (đã có random.seed ở ngoài)."""
    thi_sinh_rows = []
    diem_rows = []
    nguyen_vong_rows = []

    counts = split_students_by_khoi(students, len(KHOI_ORDER))
    global_idx = 0

    for khoi, n in zip(KHOI_ORDER, counts):
        khoi_id = KHOI_TO_ID[khoi]
        for _ in range(n):
            global_idx += 1
            sbd = build_sbd(global_idx)

            ho_ten = random_name()
            ngay_sinh = random_birth().isoformat()
            noi_sinh = random.choice(NOI_SINH)
            dia_chi = random.choice(DIA_CHI)
            cccd = unique_cccd_for(global_idx)
            sdt = unique_sdt_for(global_idx)
            email = f"{sbd}@vidu.vn"
            khu_vuc = random.choice(KHU_VUC)
            doi_tuong = random.choice([0, 0, 0, 1, 2])
            gioi_tinh = random.choice(["Nam", "Nữ"])
            ton_giao = random.choice(["Không", "Phật giáo", "Công giáo", "Tin lành"])
            # Hội đồng thi: lưu tên tỉnh/thành (không dùng mã HD01, Hội đồng 01, ...)
            hoi_dong = random.choice(NOI_SINH)

            thi_sinh_rows.append(
                "("
                f"{sql_quote(sbd)}, {sql_quote(ho_ten)}, {sql_quote(ngay_sinh)}, "
                f"{sql_quote('Kinh')}, {sql_quote(ton_giao)}, {sql_quote(gioi_tinh)}, "
                f"{sql_quote(noi_sinh)}, {sql_quote(dia_chi)}, {sql_quote(cccd)}, "
                f"{sql_quote(sdt)}, {sql_quote(email)}, {sql_quote(khu_vuc)}, {doi_tuong}, {sql_quote(hoi_dong)}"
                ")"
            )

            if random.random() >= 0.2:
                m1 = round(random.uniform(4.0, 9.8), 2)
                m2 = round(random.uniform(4.0, 9.8), 2)
                m3 = round(random.uniform(4.0, 9.8), 2)
                diem_rows.append(f"({sql_quote(sbd)}, {khoi_id}, {m1:.2f}, {m2:.2f}, {m3:.2f})")

            # Một số thí sinh không đăng ký NV nào; phần còn lại 1–2 NV, ngành có thể NULL
            if random.random() >= 0.14:
                so_nv = 1 if random.random() < 0.65 else 2
                chosen = random.sample(TRUONG_MA_CODES, so_nv)
                for idx, ma_truong in enumerate(chosen, start=1):
                    if random.random() < 0.22:
                        nguyen_vong_rows.append(
                            f"({sql_quote(sbd)}, {sql_quote(ma_truong)}, {idx}, NULL)"
                        )
                    else:
                        nganh = random.choice(NGANH_DANG_KY)
                        nguyen_vong_rows.append(
                            f"({sql_quote(sbd)}, {sql_quote(ma_truong)}, {idx}, {sql_quote(nganh)})"
                        )

    lines: list[str] = []
    lines.append("-- ========== Dữ liệu thí sinh (sinh ngẫu nhiên) ==========")
    lines.append(
        "INSERT INTO ThiSinh(soBaoDanh, hoTen, ngaySinh, danToc, tonGiao, gioiTinh, noiSinh, diaChi, soCanCuoc, soDienThoai, email, khuVuc, doiTuongUuTien, hoiDongThi) VALUES"
    )
    lines.append(",\n".join(thi_sinh_rows) + ";")
    lines.append("")

    if diem_rows:
        lines.append(
            "INSERT INTO DiemThi(thiSinh_soBaoDanh, khoiThi_id, diemMon1, diemMon2, diemMon3) VALUES"
        )
        lines.append(",\n".join(diem_rows) + ";")
        lines.append("")

    if nguyen_vong_rows:
        lines.append(
            "INSERT INTO DangKyNguyenVong(thiSinh_soBaoDanh, truongDaiHoc_maTruong, thuTuNguyenVong, nganhDangKy) VALUES"
        )
        lines.append(",\n".join(nguyen_vong_rows) + ";")
        lines.append("")

    return lines


def generate_full_install(students: int, seed: int, database: str) -> str:
    """Một tệp chạy xong: tạo CSDL, bảng, khối/trường, thí sinh + điểm + NV."""
    random.seed(seed)
    db = escape_db_name(database)
    if not db:
        raise ValueError("Tên database không hợp lệ")

    lines: list[str] = []
    lines.append("-- ==========================================================")
    lines.append("-- Cài đặt trọn gói: CREATE DATABASE + bảng + dữ liệu")
    lines.append("-- Sinh bởi database/04_generated_data.py (UTF-8, tiếng Việt có dấu)")
    lines.append("-- Chạy cả tệp trong MySQL Workbench / mysql CLI")
    lines.append("-- ==========================================================")
    lines.append("")
    lines.append(f"CREATE DATABASE IF NOT EXISTS `{db}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;")
    lines.append(f"USE `{db}`;")
    lines.append("")
    lines.append(SQL_SCHEMA_TEMPLATE)
    lines.append("")
    lines.extend(build_data_inserts(students))
    return "\n".join(lines)


def generate_data_only(
    students: int, seed: int, clear_old: bool, database: str | None
) -> str:
    """Chỉ TRUNCATE + INSERT (CSDL và bảng đã có)."""
    random.seed(seed)
    lines: list[str] = []
    lines.append("-- Chỉ chèn lại thí sinh / điểm / NV (bảng đã tồn tại)")
    lines.append("-- Sinh bởi database/04_generated_data.py --data-only")
    lines.append("")

    if database is not None:
        lines.append(sql_use_database(database))
        lines.append("")

    if clear_old:
        lines.extend(sql_clear_transactional_tables())

    lines.extend(build_data_inserts(students))
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(
        description=(
            "Sinh tệp SQL: mặc định = tạo database + bảng + toàn bộ dữ liệu; "
            "hoặc --data-only khi đã có schema."
        )
    )
    parser.add_argument("--students", type=int, default=500, help="Số thí sinh sinh ra")
    parser.add_argument(
        "--output",
        type=str,
        default=str(_DEFAULT_SQL_OUT),
        help=f"Tệp SQL đầu ra (mặc định: {_DEFAULT_SQL_OUT.name} cùng thư mục script)",
    )
    parser.add_argument("--seed", type=int, default=42, help="Seed ngẫu nhiên")
    parser.add_argument(
        "--data-only",
        action="store_true",
        help="Không tạo DB/bảng; chỉ USE (tuỳ chọn) + TRUNCATE + INSERT thí sinh/điểm/NV",
    )
    parser.add_argument(
        "--no-clear",
        action="store_true",
        help="Với --data-only: không TRUNCATE (nguy hiểm nếu trùng PK)",
    )
    parser.add_argument(
        "--database",
        type=str,
        default="quan_ly_thi_sinh",
        help="Tên database (CREATE + USE khi cài full; hoặc USE khi --data-only)",
    )
    parser.add_argument(
        "--no-use",
        action="store_true",
        help="Với --data-only: không ghi lệnh USE (chọn schema tay trong Workbench)",
    )
    args = parser.parse_args()

    if args.students <= 0:
        raise ValueError("--students phải > 0")

    if args.data_only:
        db_for_use = None if args.no_use else args.database.strip() or None
        sql = generate_data_only(args.students, args.seed, not args.no_clear, db_for_use)
    else:
        sql = generate_full_install(args.students, args.seed, args.database)

    with open(args.output, "w", encoding="utf-8") as f:
        f.write(sql)

    print(f"Sinh ra {args.students} thi sinh.")


if __name__ == "__main__":
    main()
