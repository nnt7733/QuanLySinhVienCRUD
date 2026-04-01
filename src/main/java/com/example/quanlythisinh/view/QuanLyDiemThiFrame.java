package com.example.quanlythisinh.view;

import com.example.quanlythisinh.controller.DiemThiController;
import com.example.quanlythisinh.controller.TimKiemThongKeController;
import com.example.quanlythisinh.model.dto.DiemThiTableRow;
import com.example.quanlythisinh.model.dto.ThongKeKhoiRow;
import com.example.quanlythisinh.model.dto.ThuKhoaRow;
import com.example.quanlythisinh.model.entity.KhoiThi;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.util.TextUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Màn hình CRUD điểm thi theo khối: lọc SBD, sửa ô điểm, lưu; “Xóa điểm” ghi NULL giữ dòng.
 */
public class QuanLyDiemThiFrame extends JFrame {
    private static final String[] COLS = {
            "ID", "Số BD", "Họ tên", "Khối",
            "Môn 1", "Môn 2", "Môn 3",
            "Tổng điểm 3 môn",
            "Điểm ưu tiên",
            "Tổng điểm"
    };

    private final DiemThiController controller;
    private final TimKiemThongKeController baoCaoController;
    private final Runnable onBack;
    private final String preselectedSoBaoDanh;
    private final boolean autoBackAfterSave;
    private JTable table;
    private JTextField txtLocSbd;
    private JComboBox<String> cbLocKhoi;

    /**
     * @param baoCaoController    báo cáo sắp xếp điểm / thủ khoa / số lượng theo khối
     * @param onBack              khi Quay lại (hoặc sau lưu nếu {@code autoBackAfterSave})
     * @param preselectedSoBaoDanh nếu khác null: sau load chọn dòng có SBD này (vd. từ màn thí sinh)
     * @param autoBackAfterSave   true: sau Lưu thành công gọi {@code onBack} (luồng từ thí sinh)
     */
    public QuanLyDiemThiFrame(
            DiemThiController controller,
            TimKiemThongKeController baoCaoController,
            Runnable onBack,
            String preselectedSoBaoDanh,
            boolean autoBackAfterSave
    ) {
        this.controller = controller;
        this.baoCaoController = baoCaoController;
        this.onBack = onBack;
        this.preselectedSoBaoDanh = preselectedSoBaoDanh;
        this.autoBackAfterSave = autoBackAfterSave;
        initUi();
        loadData();
    }

    /** Bố cục bảng điểm, thanh lọc SBD, nút Lưu / Xóa điểm / Quay lại. */
    private void initUi() {
        setTitle("Quản lý Điểm thi");
        setSize(1000, 520);
        setLocationRelativeTo(null);
        UiStyles.styleChildFrame(this, onBack);

        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UiStyles.styleTable(table);
        UiStyles.enableZebraRows(table);

        EditableGridSupport.installEnterCommits(table, this::luuDongDangChon);

        txtLocSbd = new JTextField(14);
        UiStyles.styleTextField(txtLocSbd);

        // Filter theo khối để người dùng chỉ xem đúng dữ liệu của khối đó.
        List<KhoiThi> khois = controller.listAllKhoi();
        Map<String, String> maKhoiToTen = khois.stream()
                .filter(k -> k.maKhoi != null)
                .collect(java.util.stream.Collectors.toMap(
                        k -> k.maKhoi.trim().toUpperCase(),
                        k -> k.tenKhoi,
                        (a, b) -> a
                ));
        Vector<String> khoiItems = new Vector<>();
        khoiItems.add("ALL");
        for (KhoiThi k : khois) {
            if (k.maKhoi == null) {
                continue;
            }
            String mk = k.maKhoi.trim().toUpperCase();
            if (!khoiItems.contains(mk)) {
                khoiItems.add(mk);
            }
        }
        cbLocKhoi = new JComboBox<>(khoiItems);
        UiStyles.styleCombo(cbLocKhoi);
        cbLocKhoi.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                String mk = value == null ? "ALL" : value.toString();
                if ("ALL".equalsIgnoreCase(mk)) {
                    setText("Tất cả khối");
                } else {
                    setText(mk + (maKhoiToTen.get(mk) != null ? " — " + maKhoiToTen.get(mk) : ""));
                }
                return this;
            }
        });

        JButton btnLocSbd = new JButton("Lọc theo SBD");
        JButton btnHienAllDiem = new JButton("Hiện tất cả điểm");
        UiStyles.styleButton(btnLocSbd, true);
        UiStyles.styleButton(btnHienAllDiem, false);
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        filterBar.setOpaque(false);
        filterBar.add(UiStyles.toolbarLabel("Số BD:"));
        filterBar.add(txtLocSbd);
        filterBar.add(UiStyles.toolbarLabel("Khối:"));
        filterBar.add(cbLocKhoi);
        filterBar.add(btnLocSbd);
        filterBar.add(btnHienAllDiem);
        btnLocSbd.addActionListener(e -> applyFilters());
        cbLocKhoi.addActionListener(e -> applyFilters());
        btnHienAllDiem.addActionListener(e -> {
            txtLocSbd.setText("");
            if (cbLocKhoi != null) {
                cbLocKhoi.setSelectedItem("ALL");
            }
            loadData();
        });

        JButton btnLuu = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa điểm");
        JButton btnThuKhoa = new JButton("Thủ khoa từng khối");
        JButton btnSoLuongTheoKhoi = new JButton("Số lượng thí sinh theo khối");
        JButton btnBack = new JButton("Quay lại");
        UiStyles.styleButton(btnLuu, true);
        UiStyles.styleButton(btnDelete, false);
        UiStyles.styleButton(btnThuKhoa, false);
        UiStyles.styleButton(btnSoLuongTheoKhoi, false);
        UiStyles.styleButton(btnBack, false);

        btnLuu.addActionListener(e -> luuTatCaDong());
        btnDelete.addActionListener(e -> deleteDiemThi());
        btnThuKhoa.addActionListener(e -> apDungThuKhoaLenBangChinh());
        btnSoLuongTheoKhoi.addActionListener(e -> hienSoLuongThiSinhTheoKhoi());
        btnBack.addActionListener(e -> goBack());

        JPanel south = new JPanel(new BorderLayout(UiStyles.GAP_PANEL, 0));
        south.setOpaque(false);
        JPanel southLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        southLeft.setOpaque(false);
        southLeft.add(btnLuu);
        southLeft.add(btnDelete);
        southLeft.add(btnThuKhoa);
        southLeft.add(btnSoLuongTheoKhoi);
        south.add(southLeft, BorderLayout.WEST);
        JPanel southRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyles.GAP_H, UiStyles.GAP_V));
        southRight.setOpaque(false);
        southRight.add(btnBack);
        south.add(southRight, BorderLayout.EAST);

        JPanel root = UiStyles.createRootPanel(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        JPanel northCard = UiStyles.createCardPanel(new BorderLayout());
        northCard.add(filterBar, BorderLayout.CENTER);
        root.add(northCard, BorderLayout.NORTH);
        root.add(UiStyles.wrapScrollPane(table), BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        add(root, BorderLayout.CENTER);
    }

    /** Chọn và cuộn tới dòng có SBD trùng {@code preselectedSoBaoDanh} (sau khi nạp bảng). */
    private void preselectRowBySbd() {
        if (preselectedSoBaoDanh == null || preselectedSoBaoDanh.isBlank()) {
            return;
        }
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        String want = preselectedSoBaoDanh.trim();
        for (int mr = 0; mr < m.getRowCount(); mr++) {
            if (want.equals(TextUtil.str(m.getValueAt(mr, 1)))) {
                for (int vr = 0; vr < table.getRowCount(); vr++) {
                    if (table.convertRowIndexToModel(vr) == mr) {
                        table.setRowSelectionInterval(vr, vr);
                        table.scrollRectToVisible(table.getCellRect(vr, 0, true));
                        return;
                    }
                }
                return;
            }
        }
    }

    /** Nạp toàn bộ điểm từ server, rồi {@link #preselectRowBySbd()}. */
    private void loadData() {
        applyFilters();
    }

    /** Tải lại bảng theo cả ô lọc SBD và khối hiện tại (sau Lưu / Xóa điểm). */
    private void refreshScoreTable() {
        applyFilters();
    }

    /** Áp dụng bộ lọc (SBD + Khối), sau đó nạp vào bảng. */
    private void applyFilters() {
        String kw = txtLocSbd == null ? "" : txtLocSbd.getText().trim();
        String mk = cbLocKhoi == null || cbLocKhoi.getSelectedItem() == null
                ? "ALL"
                : cbLocKhoi.getSelectedItem().toString();
        boolean hasKw = !kw.isEmpty();
        boolean hasKhoi = mk != null && !"ALL".equalsIgnoreCase(mk.trim());

        List<DiemThiTableRow> rows = controller.listBySoBaoDanhFilter(kw);
        if (hasKhoi) {
            String mkNorm = mk.trim().toUpperCase();
            rows = rows.stream()
                    .filter(r -> r.maKhoi() != null && mkNorm.equalsIgnoreCase(r.maKhoi()))
                    .toList();
        }

        if ((hasKw || hasKhoi) && rows.isEmpty()) {
            if (hasKw && hasKhoi) {
                JOptionPane.showMessageDialog(this, "Không có kết quả theo SBD và khối.");
            } else if (hasKw) {
                JOptionPane.showMessageDialog(this, "Không có kết quả theo SBD.");
            } else {
                JOptionPane.showMessageDialog(this, "Không có kết quả theo khối.");
            }
        }

        fillScoreTable(rows);
        preselectRowBySbd();
    }

    /** Đổ {@code rows} vào model, bật sort, set độ rộng cột. */
    private void fillScoreTable(List<DiemThiTableRow> rows) {
        DefaultTableModel model = EditableGridSupport.createModel(COLS, 0, 1, 2, 3, 7, 8, 9);

        // Rows missing at least one subject should appear on top for easier data entry.
        // Important: do not reorder complete rows here so sorting from backend remains consistent.
        List<DiemThiTableRow> incomplete = new ArrayList<>();
        List<DiemThiTableRow> complete = new ArrayList<>();
        for (DiemThiTableRow r : rows) {
            boolean isComplete = r.diemMon1() != null && r.diemMon2() != null && r.diemMon3() != null;
            if (isComplete) {
                complete.add(r);
            } else {
                incomplete.add(r);
            }
        }

        for (DiemThiTableRow r : incomplete) {
            model.addRow(new Object[]{
                    r.id(),
                    r.soBaoDanh(),
                    r.hoTen(),
                    r.maKhoi(),
                    TextUtil.cellD(r.diemMon1()),
                    TextUtil.cellD(r.diemMon2()),
                    TextUtil.cellD(r.diemMon3()),
                    TextUtil.cellD(r.tong3Mon()),
                    TextUtil.cellD(r.diemUuTien()),
                    TextUtil.cellD(r.tongDiem())
            });
        }

        for (DiemThiTableRow r : complete) {
            model.addRow(new Object[]{
                    r.id(),
                    r.soBaoDanh(),
                    r.hoTen(),
                    r.maKhoi(),
                    TextUtil.cellD(r.diemMon1()),
                    TextUtil.cellD(r.diemMon2()),
                    TextUtil.cellD(r.diemMon3()),
                    TextUtil.cellD(r.tong3Mon()),
                    TextUtil.cellD(r.diemUuTien()),
                    TextUtil.cellD(r.tongDiem())
            });
        }
        table.setModel(model);
        UiStyles.enableSorting(table);
        UiStyles.applyColumnWidths(table, 56, 100, 180, 72, 72, 72, 72, 120, 120, 90);
    }

    /** Lưu điểm ba môn của dòng đang chọn; ô trống → NULL trong DB. */
    private void luuDongDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn dòng.");
            return;
        }
        int mr = table.convertRowIndexToModel(row);
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        try {
            long id = parseLongCell(m.getValueAt(mr, 0));
            Double m1 = parseNullableDouble(m.getValueAt(mr, 4));
            Double m2 = parseNullableDouble(m.getValueAt(mr, 5));
            Double m3 = parseNullableDouble(m.getValueAt(mr, 6));
            controller.updateScoresById(id, m1, m2, m3);
            refreshScoreTable();
            JOptionPane.showMessageDialog(this, "Đã lưu.");
            if (autoBackAfterSave) {
                goBack();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.diemSaveErr(ex));
        }
    }

    /**
     * Lưu điểm cho tất cả dòng hiện đang hiển thị trên bảng.
     * Mục tiêu: nếu người dùng nhập nhiều ô rồi ấn "Lưu" thì phải persist toàn bộ.
     */
    private void luuTatCaDong() {
        // Ensure the current cell editor writes its value back to table model.
        if (table.isEditing() && table.getCellEditor() != null) {
            try {
                table.getCellEditor().stopCellEditing();
            } catch (Exception ignored) {
                // If stopCellEditing fails, we still try to save what is currently in the model.
            }
        }

        DefaultTableModel m = (DefaultTableModel) table.getModel();
        int rowCount = m.getRowCount();
        if (rowCount <= 0) {
            JOptionPane.showMessageDialog(this, "Không có dòng nào để lưu.");
            return;
        }

        try {
            for (int mr = 0; mr < rowCount; mr++) {
                long id = parseLongCell(m.getValueAt(mr, 0));
                Double m1 = parseNullableDouble(m.getValueAt(mr, 4));
                Double m2 = parseNullableDouble(m.getValueAt(mr, 5));
                Double m3 = parseNullableDouble(m.getValueAt(mr, 6));
                controller.updateScoresById(id, m1, m2, m3);
            }
            refreshScoreTable();
            JOptionPane.showMessageDialog(this, "Đã lưu.");
            if (autoBackAfterSave) {
                goBack();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.diemSaveErr(ex));
        }
    }

    /** Xóa điểm: gọi controller ghi NULL ba môn (giữ bản ghi thí sinh + khối). */
    private void deleteDiemThi() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chưa chọn dòng để xóa.");
            return;
        }
        int mr = table.convertRowIndexToModel(row);
        try {
            long id = parseLongCell(((DefaultTableModel) table.getModel()).getValueAt(mr, 0));
            controller.clearScoresById(id);
            refreshScoreTable();
            JOptionPane.showMessageDialog(this, "Đã xóa điểm.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.clearScoreErr(ex));
        }
    }

    /** Parse ID cột từ bảng (Number hoặc chuỗi). */
    private static long parseLongCell(Object raw) {
        if (raw instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(TextUtil.str(raw));
    }

    /** Ô rỗng → {@code null} để lưu NULL trong DB. */
    private static Double parseNullableDouble(Object raw) {
        String s = TextUtil.str(raw);
        if (s.isEmpty()) {
            return null;
        }
        return Double.parseDouble(s);
    }

    /** Đóng frame và gọi callback quay về màn trước (menu / thí sinh). */
    private void goBack() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }

    /** Chỉ hiển thị trên bảng chính các dòng điểm tương ứng thủ khoa từng khối (đủ cột để sửa / lưu). */
    private void apDungThuKhoaLenBangChinh() {
        List<ThuKhoaRow> tops = baoCaoController.topByKhoi();
        List<DiemThiTableRow> all = controller.list();
        List<DiemThiTableRow> out = new ArrayList<>();
        for (ThuKhoaRow tk : tops) {
            for (DiemThiTableRow r : all) {
                if (tk.soBaoDanh().equals(r.soBaoDanh()) && tk.maKhoi().equals(r.maKhoi())) {
                    out.add(r);
                    break;
                }
            }
        }
        // Only show complete rows (all 3 môn present).
        out = out.stream()
                .filter(r -> r.diemMon1() != null && r.diemMon2() != null && r.diemMon3() != null)
                .toList();

        // Respect current khối filter (if any).
        if (cbLocKhoi != null && cbLocKhoi.getSelectedItem() != null) {
            String mk = cbLocKhoi.getSelectedItem().toString();
            if (!"ALL".equalsIgnoreCase(mk.trim())) {
                String mkNorm = mk.trim().toUpperCase();
                out = out.stream()
                        .filter(r -> r.maKhoi() != null && mkNorm.equalsIgnoreCase(r.maKhoi()))
                        .toList();
            }
        }

        fillScoreTable(out);
        preselectRowBySbd();
    }

    private void hienSoLuongThiSinhTheoKhoi() {
        List<ThongKeKhoiRow> rows = baoCaoController.statByKhoi();
        DefaultTableModel model = readOnlyModel(new Object[]{"Khối", "Số lượng"}, 0);
        for (ThongKeKhoiRow r : rows) {
            model.addRow(new Object[]{r.maKhoi(), r.soLuong()});
        }
        JTable t = new JTable(model);
        UiStyles.enableSorting(t);
        UiStyles.applyColumnWidths(t, 120, 140);
        showReadOnlyTableDialog("Số lượng thí sinh theo khối", t);
    }

    private static DefaultTableModel readOnlyModel(Object[] columnNames, int rowCount) {
        return new DefaultTableModel(columnNames, rowCount) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void showReadOnlyTableDialog(String title, JTable t) {
        UiStyles.styleTable(t);
        UiStyles.enableZebraRows(t);
        JDialog d = new JDialog(this, title, true);
        d.setLayout(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        d.add(UiStyles.wrapScrollPane(t), BorderLayout.CENTER);
        JButton btnClose = new JButton("Đóng");
        UiStyles.styleButton(btnClose, true);
        btnClose.addActionListener(e -> d.dispose());
        d.add(UiStyles.createActionBar(btnClose), BorderLayout.SOUTH);
        d.pack();
        d.setSize(Math.min(980, Math.max(640, d.getWidth())), Math.min(520, Math.max(320, d.getHeight())));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }
}
