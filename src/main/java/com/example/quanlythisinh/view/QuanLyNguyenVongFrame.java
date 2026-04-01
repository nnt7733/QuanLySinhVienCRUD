package com.example.quanlythisinh.view;

import com.example.quanlythisinh.controller.NguyenVongController;
import com.example.quanlythisinh.controller.TimKiemThongKeController;
import com.example.quanlythisinh.model.dto.DangKyNguyenVongTableRow;
import com.example.quanlythisinh.model.dto.NganhTheoTruongRow;
import com.example.quanlythisinh.model.dto.TruongDaiHocTableRow;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.model.entity.TruongDaiHoc;
import com.example.quanlythisinh.service.LookupService;
import com.example.quanlythisinh.util.TextUtil;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Hai tab: trường ĐH (CRUD) và nguyện vọng theo thí sinh (sửa NV, kéo thả sắp thứ tự).
 */
public class QuanLyNguyenVongFrame extends JFrame {
    private static final String[] COLS_TRUONG = {"Mã trường", "Tên trường", "Địa chỉ"};
    private static final String[] COLS_NV = {"ID", "Số BD", "Họ tên", "Mã trường", "Tên trường", "Thứ tự NV", "Ngành ĐK"};

    /** Tiêu đề cửa sổ và nhãn nút menu (rút gọn so với mô tả đầy đủ). */
    public static final String FRAME_TITLE = "Quản lý nguyện vọng và trường đại học";

    private final NguyenVongController controller;
    private final LookupService lookupService;
    private final TimKiemThongKeController baoCaoController;
    private final Runnable onBack;

    private JTable tableTruong;
    private JTable tableNv;
    private JComboBox<ThiSinh> cbThiSinh;
    /** Lọc combo thí sinh theo số báo danh (chuỗi con, không phân biệt hoa thường). */
    private JTextField txtLocSbd;
    private List<ThiSinh> cachedAllThiSinh = new ArrayList<>();
    private boolean suppressThiSinhLoad;

    /** Hàng nút theo tab Trường / NV — hiển thị cùng dòng với Quay lại. */
    private JPanel actionBarTruong;
    private JPanel actionBarNv;

    /**
     * @param lookupService      danh sách thí sinh / trường cho combo và editor
     * @param baoCaoController   tổng NV, chi tiết ngành theo trường
     * @param onBack             Quay lại → hiện menu
     */
    public QuanLyNguyenVongFrame(
            NguyenVongController controller,
            LookupService lookupService,
            TimKiemThongKeController baoCaoController,
            Runnable onBack
    ) {
        this.controller = controller;
        this.lookupService = lookupService;
        this.baoCaoController = baoCaoController;
        this.onBack = onBack;
        initUi();
        loadThiSinhCombo();
        loadTruongTable();
    }

    /** Tab Trường + tab NV; hàng dưới: chức năng (trái) + Quay lại (phải). */
    private void initUi() {
        setTitle(FRAME_TITLE);
        setSize(1020, 580);
        setLocationRelativeTo(null);
        UiStyles.styleChildFrame(this, onBack);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Trường đại học", buildTabTruong());
        tabs.addTab("Nguyện vọng", buildTabNguyenVong());

        JButton btnBack = new JButton("Quay lại");
        UiStyles.styleButton(btnBack, true);
        btnBack.addActionListener(e -> goBack());

        JPanel actionCards = new JPanel(new CardLayout());
        actionCards.setOpaque(false);
        actionCards.add(actionBarTruong, "0");
        actionCards.add(actionBarNv, "1");
        tabs.addChangeListener(
                e -> ((CardLayout) actionCards.getLayout()).show(actionCards, String.valueOf(tabs.getSelectedIndex())));

        JPanel south = new JPanel(new BorderLayout(UiStyles.GAP_PANEL, 0));
        south.setOpaque(false);
        south.add(actionCards, BorderLayout.WEST);
        JPanel southRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyles.GAP_H, UiStyles.GAP_V));
        southRight.setOpaque(false);
        southRight.add(btnBack);
        south.add(southRight, BorderLayout.EAST);

        JPanel root = UiStyles.createRootPanel(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        root.add(tabs, BorderLayout.CENTER);
        root.add(south, BorderLayout.SOUTH);
        add(root, BorderLayout.CENTER);
    }

    /** Tab bảng trường ĐH; nút thao tác nằm hàng dưới cùng cửa sổ (cùng Quay lại). */
    private JPanel buildTabTruong() {
        tableTruong = new JTable();
        tableTruong.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableTruong.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UiStyles.styleTable(tableTruong);
        UiStyles.enableZebraRows(tableTruong);
        EditableGridSupport.installEnterCommits(tableTruong, this::luuTruongDangChon);

        JButton btnAdd = new JButton("Thêm");
        JButton btnLuu = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa");
        UiStyles.styleButton(btnAdd, true);
        UiStyles.styleButton(btnLuu, false);
        UiStyles.styleButton(btnDelete, false);
        btnAdd.addActionListener(e -> moDialogThemTruong());
        btnLuu.addActionListener(e -> luuTruongDangChon());
        btnDelete.addActionListener(e -> xoaTruong());

        JButton btnNganhTheoTruong = new JButton("Chi tiết ngành theo trường");
        UiStyles.styleButton(btnNganhTheoTruong, false);
        btnNganhTheoTruong.addActionListener(e -> moChiTietNganhTheoTruong());

        actionBarTruong = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        actionBarTruong.setOpaque(false);
        actionBarTruong.add(btnAdd);
        actionBarTruong.add(btnLuu);
        actionBarTruong.add(btnDelete);
        actionBarTruong.add(btnNganhTheoTruong);

        JPanel p = new JPanel(new BorderLayout(0, UiStyles.GAP_PANEL));
        p.setOpaque(false);
        p.add(UiStyles.wrapScrollPane(tableTruong), BorderLayout.CENTER);
        return p;
    }

    /** Tab NV: lọc thí sinh trên đầu; nút thao tác hàng dưới cùng cửa sổ (cùng Quay lại). */
    private JPanel buildTabNguyenVong() {
        tableNv = new JTable();
        tableNv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableNv.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UiStyles.styleTable(tableNv);
        UiStyles.enableZebraRows(tableNv);
        EditableGridSupport.installEnterCommits(tableNv, this::luuNvDangChon);

        cbThiSinh = new JComboBox<>();
        UiStyles.styleCombo(cbThiSinh);
        cbThiSinh.setRenderer(new ThiSinhCellRenderer());
        cbThiSinh.addActionListener(e -> {
            if (!suppressThiSinhLoad) {
                loadNvTable();
            }
        });

        txtLocSbd = new JTextField(12);
        UiStyles.styleTextField(txtLocSbd);
        txtLocSbd.setToolTipText("Nhập số báo danh để thu hẹp danh sách thí sinh (để trống = tất cả).");
        txtLocSbd.getDocument().addDocumentListener(new DocumentListener() {
            private void onChange() {
                SwingUtilities.invokeLater(QuanLyNguyenVongFrame.this::refillThiSinhCombo);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                onChange();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                onChange();
            }
        });

        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        filterBar.setOpaque(false);
        filterBar.add(UiStyles.toolbarLabel("Số BD:"));
        filterBar.add(txtLocSbd);
        filterBar.add(UiStyles.toolbarLabel("Thí sinh:"));
        filterBar.add(cbThiSinh);

        JButton btnAdd = new JButton("Thêm NV");
        JButton btnLuu = new JButton("Lưu");
        JButton btnReorder = new JButton("Sửa thứ tự nguyện vọng");
        JButton btnDelete = new JButton("Xóa NV");
        UiStyles.styleButton(btnAdd, true);
        UiStyles.styleButton(btnLuu, false);
        UiStyles.styleButton(btnReorder, false);
        UiStyles.styleButton(btnDelete, false);
        btnAdd.addActionListener(e -> moDialogThemNv());
        btnLuu.addActionListener(e -> luuNvDangChon());
        btnReorder.addActionListener(e -> moDialogSuaThuTuNguyenVong());
        btnDelete.addActionListener(e -> xoaNv());

        JButton btnTongNv = new JButton("Tổng số nguyện vọng");
        UiStyles.styleButton(btnTongNv, false);
        btnTongNv.addActionListener(e -> hienTongSoNguyenVong());

        actionBarNv = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        actionBarNv.setOpaque(false);
        actionBarNv.add(btnAdd);
        actionBarNv.add(btnLuu);
        actionBarNv.add(btnReorder);
        actionBarNv.add(btnDelete);
        actionBarNv.add(btnTongNv);

        JPanel northCard = UiStyles.createCardPanel(new BorderLayout());
        northCard.add(filterBar, BorderLayout.CENTER);

        JPanel p = new JPanel(new BorderLayout(0, UiStyles.GAP_PANEL));
        p.setOpaque(false);
        p.add(northCard, BorderLayout.NORTH);
        p.add(UiStyles.wrapScrollPane(tableNv), BorderLayout.CENTER);
        return p;
    }

    /** Nạp danh sách thí sinh từ DB rồi áp dụng lọc SBD hiện tại. */
    private void loadThiSinhCombo() {
        cachedAllThiSinh = new ArrayList<>(lookupService.getAllThiSinh());
        refillThiSinhCombo();
    }

    /**
     * Đổ lại combo chỉ với thí sinh khớp ô Số BD (chuỗi con; để trống = toàn bộ {@link #cachedAllThiSinh}).
     */
    private void refillThiSinhCombo() {
        if (cbThiSinh == null || txtLocSbd == null) {
            return;
        }
        String kw = TextUtil.str(txtLocSbd.getText()).trim().toLowerCase();
        ThiSinh prev = (ThiSinh) cbThiSinh.getSelectedItem();
        String prevSbd = prev != null ? prev.soBaoDanh : null;

        suppressThiSinhLoad = true;
        try {
            cbThiSinh.removeAllItems();
            for (ThiSinh t : cachedAllThiSinh) {
                if (kw.isEmpty()) {
                    cbThiSinh.addItem(t);
                } else {
                    String sbd = t.soBaoDanh == null ? "" : t.soBaoDanh.trim().toLowerCase();
                    if (sbd.contains(kw)) {
                        cbThiSinh.addItem(t);
                    }
                }
            }
            ThiSinh toSelect = null;
            if (prevSbd != null) {
                for (int i = 0; i < cbThiSinh.getItemCount(); i++) {
                    ThiSinh x = cbThiSinh.getItemAt(i);
                    if (prevSbd.equals(x.soBaoDanh)) {
                        toSelect = x;
                        break;
                    }
                }
            }
            if (toSelect == null && cbThiSinh.getItemCount() == 1) {
                toSelect = cbThiSinh.getItemAt(0);
            }
            if (toSelect != null) {
                cbThiSinh.setSelectedItem(toSelect);
            } else if (cbThiSinh.getItemCount() > 0) {
                cbThiSinh.setSelectedIndex(0);
            }
        } finally {
            suppressThiSinhLoad = false;
        }
        loadNvTable();
    }

    /** Nạp danh sách trường từ controller vào bảng tab Trường. */
    private void loadTruongTable() {
        DefaultTableModel model = EditableGridSupport.createModel(COLS_TRUONG, 0);
        for (TruongDaiHocTableRow r : controller.listTruongRows()) {
            model.addRow(new Object[]{r.maTruong(), r.tenTruong(), r.diaChi() == null ? "" : r.diaChi()});
        }
        tableTruong.setModel(model);
        UiStyles.enableSorting(tableTruong);
        UiStyles.applyColumnWidths(tableTruong, 100, 220, 320);
    }

    /** Cập nhật tên/địa chỉ trường theo dòng đang chọn. */
    private void luuTruongDangChon() {
        int vr = tableTruong.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng trường.");
            return;
        }
        int mr = tableTruong.convertRowIndexToModel(vr);
        DefaultTableModel m = (DefaultTableModel) tableTruong.getModel();
        String ma = TextUtil.str(m.getValueAt(mr, 0));
        try {
            controller.updateTruong(ma, TextUtil.str(m.getValueAt(mr, 1)), TextUtil.str(m.getValueAt(mr, 2)));
            loadTruongTable();
            selectTruongRowByMa(ma);
            JOptionPane.showMessageDialog(this, "Đã lưu.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /** Sau Lưu, giữ chọn dòng có mã trường {@code ma}. */
    private void selectTruongRowByMa(String ma) {
        DefaultTableModel m = (DefaultTableModel) tableTruong.getModel();
        for (int mr = 0; mr < m.getRowCount(); mr++) {
            if (ma.equals(TextUtil.str(m.getValueAt(mr, 0)))) {
                for (int vr = 0; vr < tableTruong.getRowCount(); vr++) {
                    if (tableTruong.convertRowIndexToModel(vr) == mr) {
                        tableTruong.setRowSelectionInterval(vr, vr);
                        tableTruong.scrollRectToVisible(tableTruong.getCellRect(vr, 0, true));
                        return;
                    }
                }
                return;
            }
        }
    }

    /** Nạp NV của thí sinh đang chọn trong combo; gán editor cột mã trường. */
    private void loadNvTable() {
        ThiSinh t = (ThiSinh) cbThiSinh.getSelectedItem();
        DefaultTableModel model = EditableGridSupport.createModel(COLS_NV, 0, 1, 2, 4);
        if (t != null) {
            for (DangKyNguyenVongTableRow r : controller.listNguyenVongByThiSinh(t.soBaoDanh)) {
                model.addRow(new Object[]{
                        r.id(), r.soBaoDanh(), r.hoTen(), r.maTruong(), r.tenTruong(),
                        r.thuTuNguyenVong(), r.nganhDangKy() == null ? "" : r.nganhDangKy()
                });
            }
        }
        tableNv.setModel(model);
        UiStyles.enableSorting(tableNv);
        UiStyles.applyColumnWidths(tableNv, 50, 90, 160, 90, 200, 80, 160);
        applyNvMaTruongEditor();
        tableNv.clearSelection();
    }

    /** Combo mã trường trong ô chỉnh sửa cột “Mã trường”. */
    private void applyNvMaTruongEditor() {
        JComboBox<String> cb = new JComboBox<>();
        for (TruongDaiHoc tr : lookupService.getAllTruongDaiHoc()) {
            cb.addItem(tr.maTruong);
        }
        UiStyles.styleCombo(cb);
        TableColumn col = tableNv.getColumnModel().getColumn(3);
        col.setCellEditor(new DefaultCellEditor(cb));
    }

    /** Lưu thay đổi một dòng NV (mã trường, thứ tự, ngành). */
    private void luuNvDangChon() {
        ThiSinh ts = (ThiSinh) cbThiSinh.getSelectedItem();
        if (ts == null) {
            JOptionPane.showMessageDialog(this, "Chọn thí sinh.");
            return;
        }
        int vr = tableNv.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng NV.");
            return;
        }
        int mr = tableNv.convertRowIndexToModel(vr);
        DefaultTableModel m = (DefaultTableModel) tableNv.getModel();
        try {
            long id = parseIdCell(m.getValueAt(mr, 0));
            String sbd = TextUtil.str(m.getValueAt(mr, 1));
            String ma = TextUtil.str(m.getValueAt(mr, 3));
            int thuTu = Integer.parseInt(TextUtil.str(m.getValueAt(mr, 5)).trim());
            String nganh = TextUtil.str(m.getValueAt(mr, 6));
            controller.updateNguyenVong(id, sbd, ma, thuTu, nganh);
            loadNvTable();
            selectNvRowById(id);
            JOptionPane.showMessageDialog(this, "Đã lưu.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /** Sau Lưu, chọn lại dòng có ID nguyện vọng {@code id}. */
    private void selectNvRowById(long id) {
        DefaultTableModel m = (DefaultTableModel) tableNv.getModel();
        for (int mr = 0; mr < m.getRowCount(); mr++) {
            if (parseIdCell(m.getValueAt(mr, 0)) == id) {
                for (int vr = 0; vr < tableNv.getRowCount(); vr++) {
                    if (tableNv.convertRowIndexToModel(vr) == mr) {
                        tableNv.setRowSelectionInterval(vr, vr);
                        tableNv.scrollRectToVisible(tableNv.getCellRect(vr, 0, true));
                        return;
                    }
                }
                return;
            }
        }
    }

    /** Hộp thoại nhập mã/tên/địa chỉ trường mới. */
    private void moDialogThemTruong() {
        JTextField ma = new JTextField(18);
        JTextField ten = new JTextField(24);
        JTextField dc = new JTextField(28);
        UiStyles.styleTextField(ma);
        UiStyles.styleTextField(ten);
        UiStyles.styleTextField(dc);
        JPanel p = new JPanel(new GridLayout(3, 2, 8, 8));
        p.add(UiStyles.toolbarLabel("Mã trường"));
        p.add(ma);
        p.add(UiStyles.toolbarLabel("Tên trường"));
        p.add(ten);
        p.add(UiStyles.toolbarLabel("Địa chỉ"));
        p.add(dc);
        int opt = JOptionPane.showConfirmDialog(
                this,
                p,
                "Thêm trường đại học",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (opt != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            controller.addTruong(ma.getText(), ten.getText(), dc.getText());
            loadTruongTable();
            JOptionPane.showMessageDialog(this, "Đã thêm.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /** Xóa trường theo mã ở dòng đang chọn (nếu không còn NV tham chiếu — xử lý ở service). */
    private void xoaTruong() {
        int vr = tableTruong.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng trường.");
            return;
        }
        int mr = tableTruong.convertRowIndexToModel(vr);
        String ma = TextUtil.str(((DefaultTableModel) tableTruong.getModel()).getValueAt(mr, 0));
        try {
            controller.deleteTruong(ma);
            loadTruongTable();
            JOptionPane.showMessageDialog(this, "Đã xóa.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /** Thêm NV: chọn trường, thứ tự, ngành (có thể rỗng → null). */
    private void moDialogThemNv() {
        ThiSinh t = (ThiSinh) cbThiSinh.getSelectedItem();
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Chọn thí sinh.");
            return;
        }
        JComboBox<TruongDaiHoc> cbTr = new JComboBox<>();
        for (TruongDaiHoc tr : lookupService.getAllTruongDaiHoc()) {
            cbTr.addItem(tr);
        }
        UiStyles.styleCombo(cbTr);
        cbTr.setRenderer(new TruongCellRenderer());
        JTextField thuTu = new JTextField(String.valueOf(controller.suggestNextThuTu(t.soBaoDanh)));
        JTextField nganh = new JTextField(24);
        UiStyles.styleTextField(thuTu);
        UiStyles.styleTextField(nganh);

        JPanel p = new JPanel(new GridLayout(3, 2, 10, 8));
        p.setBackground(Color.WHITE);
        p.add(UiStyles.toolbarLabel("Trường ĐH"));
        p.add(cbTr);
        p.add(UiStyles.toolbarLabel("Thứ tự NV"));
        p.add(thuTu);
        p.add(UiStyles.toolbarLabel("Ngành đăng ký"));
        p.add(nganh);
        for (Component c : p.getComponents()) {
            if (c instanceof JLabel lbl) {
                UiStyles.styleFormLabel(lbl);
            }
        }

        int opt = JOptionPane.showConfirmDialog(
                this,
                p,
                "Thêm nguyện vọng",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (opt != JOptionPane.OK_OPTION) {
            return;
        }
        TruongDaiHoc tr = (TruongDaiHoc) cbTr.getSelectedItem();
        if (tr == null) {
            JOptionPane.showMessageDialog(this, "Chọn trường.");
            return;
        }
        try {
            int tt = Integer.parseInt(thuTu.getText().trim());
            controller.addNguyenVong(t.soBaoDanh, tr.maTruong, tt, nganh.getText());
            loadNvTable();
            JOptionPane.showMessageDialog(this, "Đã thêm.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /**
     * Hộp thoại sắp xếp lại thứ tự NV (kéo dòng hoặc Lên/Xuống); lưu map id → thứ tự 1…n.
     */
    private void moDialogSuaThuTuNguyenVong() {
        ThiSinh t = (ThiSinh) cbThiSinh.getSelectedItem();
        if (t == null) {
            JOptionPane.showMessageDialog(this, "Chọn thí sinh.");
            return;
        }
        List<DangKyNguyenVongTableRow> rows = controller.listNguyenVongByThiSinh(t.soBaoDanh);
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có NV.");
            return;
        }

        int pr = tableNv.getSelectedRow();
        final long keepId = pr >= 0
                ? parseIdCell(tableNv.getModel().getValueAt(tableNv.convertRowIndexToModel(pr), 0))
                : -1L;

        DefaultTableModel tm = new DefaultTableModel(
                new Object[]{"ID", "Mã trường", "Tên trường", "Ngành đăng ký", "Thứ tự NV"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (DangKyNguyenVongTableRow r : rows) {
            String nganh = r.nganhDangKy() == null || r.nganhDangKy().isBlank() ? "" : r.nganhDangKy();
            tm.addRow(new Object[]{r.id(), r.maTruong(), r.tenTruong(), nganh, 0});
        }

        Runnable syncThuTu = () -> {
            for (int i = 0; i < tm.getRowCount(); i++) {
                tm.setValueAt(i + 1, i, 4);
            }
        };
        syncThuTu.run();

        JTable tbl = new JTable(tm);
        tbl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl.setDragEnabled(true);
        tbl.setDropMode(DropMode.INSERT_ROWS);
        tbl.setTransferHandler(new TableRowReorderTransferHandler(tbl, syncThuTu));
        UiStyles.styleTable(tbl);
        UiStyles.enableZebraRows(tbl);
        TableColumn idCol = tbl.getColumnModel().getColumn(0);
        idCol.setMinWidth(0);
        idCol.setMaxWidth(0);
        idCol.setPreferredWidth(0);
        UiStyles.applyColumnWidths(tbl, 0, 90, 200, 180, 72);

        JLabel hint = new JLabel(
                "<html><body style='width:420px'>Kéo dòng hoặc <b>Lên</b>/<b>Xuống</b> — trên = <b>1</b>.</body></html>");
        hint.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton btnUp = new JButton("↑ Lên");
        JButton btnDown = new JButton("↓ Xuống");
        UiStyles.styleButton(btnUp, false);
        UiStyles.styleButton(btnDown, false);
        btnUp.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r > 0) {
                tm.moveRow(r, r, r - 1);
                tbl.getSelectionModel().setSelectionInterval(r - 1, r - 1);
                syncThuTu.run();
            }
        });
        btnDown.addActionListener(e -> {
            int r = tbl.getSelectedRow();
            if (r >= 0 && r < tm.getRowCount() - 1) {
                tm.moveRow(r, r, r + 1);
                tbl.getSelectionModel().setSelectionInterval(r + 1, r + 1);
                syncThuTu.run();
            }
        });

        JPanel rowMove = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        rowMove.setOpaque(false);
        rowMove.add(btnUp);
        rowMove.add(btnDown);

        JButton btnOk = new JButton("Lưu");
        JButton btnCancel = new JButton("Hủy");
        UiStyles.styleButton(btnOk, true);
        UiStyles.styleButton(btnCancel, false);

        JDialog d = new JDialog(this, "Sửa thứ tự nguyện vọng", true);
        d.setLayout(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(hint, BorderLayout.NORTH);
        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.setOpaque(false);
        center.add(rowMove, BorderLayout.NORTH);
        center.add(UiStyles.wrapScrollPane(tbl), BorderLayout.CENTER);
        north.add(center, BorderLayout.CENTER);
        d.add(north, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyles.GAP_H, UiStyles.GAP_V));
        actions.setOpaque(false);
        actions.add(btnCancel);
        actions.add(btnOk);
        d.add(actions, BorderLayout.SOUTH);

        btnCancel.addActionListener(e -> d.dispose());
        btnOk.addActionListener(e -> {
            try {
                Map<Long, Integer> map = new HashMap<>();
                for (int i = 0; i < tm.getRowCount(); i++) {
                    long nvId = parseIdCell(tm.getValueAt(i, 0));
                    map.put(nvId, i + 1);
                }
                controller.reorderNguyenVongThuTu(t.soBaoDanh, map);
                loadNvTable();
                if (keepId >= 0) {
                    selectNvRowById(keepId);
                }
                d.dispose();
                JOptionPane.showMessageDialog(this, "Đã cập nhật thứ tự.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, ViewMessages.err(ex));
            }
        });

        d.pack();
        d.setSize(Math.min(680, d.getWidth() + 40), Math.min(460, d.getHeight() + 24));
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    /** Xóa một dòng NV theo ID. */
    private void xoaNv() {
        int vr = tableNv.getSelectedRow();
        if (vr < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng NV.");
            return;
        }
        try {
            int mr = tableNv.convertRowIndexToModel(vr);
            long id = parseIdCell(tableNv.getModel().getValueAt(mr, 0));
            controller.deleteNguyenVong(id);
            loadNvTable();
            JOptionPane.showMessageDialog(this, "Đã xóa.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.err(ex));
        }
    }

    /** Parse cột ID từ bảng (Number hoặc chuỗi). */
    private static long parseIdCell(Object raw) {
        if (raw instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(String.valueOf(raw).trim());
    }

    /** Đóng frame và hiện lại menu. */
    private void goBack() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }

    private void hienTongSoNguyenVong() {
        long tong = baoCaoController.statTongSoNguyenVong();
        JOptionPane.showMessageDialog(
                this,
                "Tổng số bản ghi nguyện vọng trong hệ thống: " + tong,
                "Tổng số nguyện vọng",
                JOptionPane.INFORMATION_MESSAGE);
    }

    /** Hộp thoại: chọn trường → bảng số thí sinh distinct theo ngành đăng ký. */
    private void moChiTietNganhTheoTruong() {
        JDialog d = new JDialog(this, "Chi tiết ngành theo trường", true);
        d.setSize(520, 400);
        d.setLocationRelativeTo(this);

        JComboBox<TruongDaiHocTableRow> cb = new JComboBox<>();
        UiStyles.styleCombo(cb);
        for (TruongDaiHocTableRow r : baoCaoController.listTruongForCombo()) {
            cb.addItem(r);
        }
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof TruongDaiHocTableRow tr) {
                    lbl.setText(tr.maTruong() + " — " + tr.tenTruong());
                }
                return lbl;
            }
        });

        JTable t = new JTable();
        UiStyles.styleTable(t);
        UiStyles.enableZebraRows(t);

        Runnable load = () -> {
            TruongDaiHocTableRow sel = (TruongDaiHocTableRow) cb.getSelectedItem();
            if (sel == null) {
                t.setModel(new DefaultTableModel());
                return;
            }
            List<NganhTheoTruongRow> rows = baoCaoController.statNganhTheoTruong(sel.maTruong());
            DefaultTableModel m = new DefaultTableModel(new Object[]{"Ngành đăng ký", "Số thí sinh"}, 0);
            for (NganhTheoTruongRow r : rows) {
                m.addRow(new Object[]{r.nganhDangKy(), r.soThiSinh()});
            }
            t.setModel(m);
            UiStyles.enableSorting(t);
            UiStyles.applyColumnWidths(t, 280, 120);
        };
        cb.addActionListener(e -> load.run());
        load.run();

        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        north.setOpaque(false);
        north.add(UiStyles.toolbarLabel("Trường:"));
        north.add(cb);

        JPanel p = new JPanel(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        p.add(north, BorderLayout.NORTH);
        p.add(UiStyles.wrapScrollPane(t), BorderLayout.CENTER);
        d.add(p);
        d.setVisible(true);
    }

    /** Hiển thị combo thí sinh: “SBD — Họ tên”. */
    private static class ThiSinhCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof ThiSinh ts) {
                lbl.setText(ts.soBaoDanh + " — " + ts.hoTen);
            }
            return lbl;
        }
    }

    /** Hiển thị combo trường: “Mã — Tên”. */
    private static class TruongCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof TruongDaiHoc tr) {
                lbl.setText(tr.maTruong + " — " + tr.tenTruong);
            }
            return lbl;
        }
    }
}
