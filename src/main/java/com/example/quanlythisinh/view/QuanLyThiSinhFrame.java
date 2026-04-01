package com.example.quanlythisinh.view;

import com.example.quanlythisinh.controller.ThiSinhController;
import com.example.quanlythisinh.model.dto.ThiSinhTableRow;
import com.example.quanlythisinh.model.entity.ThiSinh;
import com.example.quanlythisinh.util.TextUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Quản lý thí sinh: bảng theo SBD tăng dần, tìm kết hợp. */
public class QuanLyThiSinhFrame extends JFrame {
    private static final String[] COLS = {
            "Số BD", "Họ tên", "Ngày sinh", "Dân tộc", "Tôn giáo", "Giới tính",
            "Nơi sinh", "Địa chỉ", "CCCD", "SĐT", "Email", "Khu vực", "Đối tượng", "Hội đồng thi"
    };

    private final ThiSinhController controller;
    private final Runnable onBack;
    private JTable table;
    private JTextField txtTimHoTen;
    private JTextField txtTimSbd;
    private JTextField txtTimNoiSinh;
    private JLabel lblTongThiSinh;

    public QuanLyThiSinhFrame(ThiSinhController controller, Runnable onBack) {
        this.controller = controller;
        this.onBack = onBack;
        initUi();
        loadData();
    }

    /** Thanh tìm, bảng dữ liệu, nút Thêm / Lưu / Xóa / Quay lại. */
    private void initUi() {
        configureFrame();
        initTable();

        JPanel searchCard = buildSearchCard();
        JPanel actionBar = buildActionBar();

        JPanel root = UiStyles.createRootPanel(new BorderLayout(UiStyles.GAP_PANEL, UiStyles.GAP_PANEL));
        root.add(searchCard, BorderLayout.NORTH);
        root.add(UiStyles.wrapScrollPane(table), BorderLayout.CENTER);
        root.add(actionBar, BorderLayout.SOUTH);
        add(root, BorderLayout.CENTER);
    }

    /** Cấu hình frame cơ bản (tiêu đề, kích thước, vị trí và style chung). */
    private void configureFrame() {
        setTitle("Quản lý Thí sinh");
        setSize(1100, 640);
        setLocationRelativeTo(null);
        UiStyles.styleChildFrame(this, onBack);
    }

    /** Tạo bảng, bật style và gắn listener chọn dòng. */
    private void initTable() {
        table = new JTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        UiStyles.styleTable(table);
        UiStyles.enableZebraRows(table);

        EditableGridSupport.installEnterCommits(table, this::luuDongDangChon);
    }

    /** Dựng card tìm kiếm: bộ lọc theo Họ tên/SBD/Nơi sinh + nhãn tổng số. */
    private JPanel buildSearchCard() {
        txtTimHoTen = new JTextField(12);
        txtTimSbd = new JTextField(10);
        txtTimNoiSinh = new JTextField(12);
        UiStyles.styleTextField(txtTimHoTen);
        UiStyles.styleTextField(txtTimSbd);
        UiStyles.styleTextField(txtTimNoiSinh);
        JButton btnTim = new JButton("Tìm");
        JButton btnHienTatCa = new JButton("Hiện tất cả");
        UiStyles.styleButton(btnTim, true);
        UiStyles.styleButton(btnHienTatCa, false);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, UiStyles.GAP_H, UiStyles.GAP_V));
        searchRow.setOpaque(false);
        searchRow.add(UiStyles.toolbarLabel("Họ tên:"));
        searchRow.add(txtTimHoTen);
        searchRow.add(UiStyles.toolbarLabel("Số BD:"));
        searchRow.add(txtTimSbd);
        searchRow.add(UiStyles.toolbarLabel("Nơi sinh:"));
        searchRow.add(txtTimNoiSinh);
        searchRow.add(btnTim);
        searchRow.add(btnHienTatCa);

        lblTongThiSinh = new JLabel(" ");
        lblTongThiSinh.setForeground(new Color(0x33, 0x33, 0x33));
        JPanel counterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiStyles.GAP_H, UiStyles.GAP_V));
        counterRow.setOpaque(false);
        counterRow.add(lblTongThiSinh);

        JPanel searchBlock = new JPanel(new BorderLayout(0, UiStyles.GAP_V));
        searchBlock.setOpaque(false);
        searchBlock.add(searchRow, BorderLayout.CENTER);
        searchBlock.add(counterRow, BorderLayout.SOUTH);

        JPanel searchCard = UiStyles.createCardPanel(new BorderLayout());
        searchCard.add(searchBlock, BorderLayout.CENTER);

        btnTim.addActionListener(e -> timKetHop());
        btnHienTatCa.addActionListener(e -> resetSearchFilters());
        return searchCard;
    }

    /** Dựng hàng nút thao tác chính và gắn sự kiện cho từng nút. */
    private JPanel buildActionBar() {
        JButton btnAdd = new JButton("Thêm");
        JButton btnLuu = new JButton("Lưu");
        JButton btnDelete = new JButton("Xóa");
        JButton btnExit = new JButton("Quay lại");
        UiStyles.styleButton(btnAdd, true);
        UiStyles.styleButton(btnLuu, false);
        UiStyles.styleButton(btnDelete, false);
        UiStyles.styleButton(btnExit, false);

        btnAdd.addActionListener(e -> addThiSinh());
        btnLuu.addActionListener(e -> luuDongDangChon());
        btnDelete.addActionListener(e -> deleteThiSinh());
        btnExit.addActionListener(e -> goBack());
        return UiStyles.createActionBar(btnAdd, btnLuu, btnDelete, btnExit);
    }

    /** Đặt lại các ô tìm kiếm về rỗng rồi tải lại danh sách đầy đủ. */
    private void resetSearchFilters() {
        txtTimHoTen.setText("");
        txtTimSbd.setText("");
        txtTimNoiSinh.setText("");
        loadData();
    }

    /** Nạp toàn bộ thí sinh từ server. */
    private void loadData() {
        fillTable(controller.list());
    }

    /** Đổ danh sách vào JTable và đặt độ rộng cột (luôn sắp theo số báo danh tăng dần). */
    private void fillTable(List<ThiSinhTableRow> rows) {
        List<ThiSinhTableRow> sorted = new ArrayList<>(rows);
        sorted.sort(Comparator.comparing(ThiSinhTableRow::soBaoDanh));
        DefaultTableModel model = EditableGridSupport.createModel(COLS, 0);
        for (ThiSinhTableRow r : sorted) {
            model.addRow(rowToObjects(r));
        }
        table.setModel(model);
        table.setRowSorter(null);
        int[] w = {90, 160, 100, 72, 72, 64, 120, 180, 110, 100, 160, 72, 72, 90};
        for (int i = 0; i < COLS.length && i < w.length; i++) {
            if (table.getColumnModel().getColumnCount() > i) {
                table.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
            }
        }
        capNhatNhanTongSo(sorted.size());
    }

    /** Cập nhật nhãn: tổng trong CSDL và số dòng đang hiển thị (có thể khác khi đang lọc). */
    private void capNhatNhanTongSo(int soDongHienThi) {
        long tongCsdl = controller.countAll();
        if (soDongHienThi == tongCsdl) {
            lblTongThiSinh.setText("Tổng số thí sinh: " + tongCsdl);
        } else {
            lblTongThiSinh.setText("Đang hiển thị: " + soDongHienThi + " / Tổng trong CSDL: " + tongCsdl);
        }
    }

    /** Một dòng DTO → mảng ô cho {@link DefaultTableModel}. */
    private static Object[] rowToObjects(ThiSinhTableRow r) {
        return new Object[]{
                r.soBaoDanh(), r.hoTen(), r.ngaySinh(), r.danToc(), r.tonGiao(), r.gioiTinh(),
                r.noiSinh(), r.diaChi(), r.soCanCuoc(), r.soDienThoai(), r.email(), r.khuVuc(),
                r.doiTuongUuTien(), r.hoiDongThi()
        };
    }

    /** Đọc một dòng model (index) thành {@link ThiSinhTableRow} để cập nhật DB. */
    private ThiSinhTableRow parseRowFromTable(int row) {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        String sbd = TextUtil.str(m.getValueAt(row, 0));
        String hoTen = TextUtil.str(m.getValueAt(row, 1));
        LocalDate ngay =
                m.getValueAt(row, 2) instanceof LocalDate ld ? ld : LocalDate.parse(TextUtil.str(m.getValueAt(row, 2)));
        int dt = parseIntCell(m.getValueAt(row, 12));
        return new ThiSinhTableRow(
                sbd,
                hoTen,
                ngay,
                TextUtil.str(m.getValueAt(row, 3)),
                TextUtil.str(m.getValueAt(row, 4)),
                TextUtil.str(m.getValueAt(row, 5)),
                TextUtil.str(m.getValueAt(row, 6)),
                TextUtil.str(m.getValueAt(row, 7)),
                TextUtil.str(m.getValueAt(row, 8)),
                TextUtil.str(m.getValueAt(row, 9)),
                TextUtil.str(m.getValueAt(row, 10)),
                TextUtil.str(m.getValueAt(row, 11)),
                dt,
                TextUtil.str(m.getValueAt(row, 13))
        );
    }

    /** Parse cột đối tượng ưu tiên (số nguyên). */
    private static int parseIntCell(Object o) {
        if (o instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(TextUtil.str(o).trim());
    }

    /** Sau khi reload bảng, chọn lại dòng có SBD đã lưu. */
    private void selectRowBySoBaoDanh(String sbd) {
        DefaultTableModel m = (DefaultTableModel) table.getModel();
        for (int mr = 0; mr < m.getRowCount(); mr++) {
            if (sbd.equals(TextUtil.str(m.getValueAt(mr, 0)))) {
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

    /** Lưu dòng đang chọn lên server qua {@link ThiSinhController#updateFromRow}. */
    private void luuDongDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn một dòng.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        try {
            ThiSinhTableRow dto = parseRowFromTable(modelRow);
            String keepSbd = dto.soBaoDanh();
            controller.updateFromRow(dto);
            loadData();
            selectRowBySoBaoDanh(keepSbd);
            UiStyles.showShortToast(this, "Đã lưu.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.thiSinhErr(ex));
        }
    }

    /** Lọc AND theo họ tên / SBD / nơi sinh (đều LIKE, không phân biệt hoa thường). */
    private void timKetHop() {
        String ho = txtTimHoTen.getText().trim();
        String sbd = txtTimSbd.getText().trim();
        String noi = txtTimNoiSinh.getText().trim();
        if (ho.isEmpty() && sbd.isEmpty() && noi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập họ tên, SBD hoặc nơi sinh.");
            return;
        }
        List<ThiSinhTableRow> rows = controller.searchCombined(ho, sbd, noi);
        if (rows.isEmpty()) {
            UiStyles.showShortToast(this, "Không có kết quả.");
        }
        fillTable(rows);
    }

    /** Thêm thí sinh → chọn dòng mới. */
    private void addThiSinh() {
        try {
            ThiSinh newThiSinh = showAddDialog();
            if (newThiSinh == null) {
                return;
            }
            ThiSinh saved = controller.add(newThiSinh);
            loadData();
            selectRowBySoBaoDanh(saved.soBaoDanh);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.thiSinhErr(ex));
        }
    }

    /** Xóa thí sinh theo SBD ở dòng đang chọn. */
    private void deleteThiSinh() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        String sbd = TextUtil.str(((DefaultTableModel) table.getModel()).getValueAt(modelRow, 0));
        if (sbd.isBlank()) {
            JOptionPane.showMessageDialog(this, "Thiếu SBD.");
            return;
        }
        try {
            String t = sbd.trim();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn xóa thí sinh SBD " + t + "?\n",
                    "Xác nhận xóa",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
            controller.delete(t);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ViewMessages.thiSinhErr(ex));
        }
    }

    /** Form nhập thí sinh mới (SBD sinh tự); trả {@code null} nếu hủy. */
    private ThiSinh showAddDialog() {
        JTextField fSbd = new JTextField();
        fSbd.setEditable(false);
        fSbd.setText(controller.generateNextSoBaoDanh());
        JTextField fHoTen = new JTextField();
        JTextField fNgaySinh = new JTextField();
        JTextField fDanToc = new JTextField("Kinh");
        JTextField fTonGiao = new JTextField("Không");
        JTextField fGioiTinh = new JTextField("Nam");
        JTextField fNoiSinh = new JTextField();
        JTextField fDiaChi = new JTextField();
        JTextField fCccd = new JTextField();
        JTextField fSdt = new JTextField();
        JTextField fEmail = new JTextField();
        JTextField fKhuVuc = new JTextField("KV3");
        JTextField fDoiTuong = new JTextField("0");
        JTextField fHoiDong = new JTextField("Hà Nội");

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(4, 8, 8, 8));

        form.add(createAddFormSbdRow(fSbd));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Họ tên:", fHoTen, "Bắt buộc"));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Ngày sinh (yyyy-MM-dd):", fNgaySinh, "Bắt buộc"));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Dân tộc:", fDanToc, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Tôn giáo:", fTonGiao, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Giới tính:", fGioiTinh, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Nơi sinh:", fNoiSinh, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Địa chỉ:", fDiaChi, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("CCCD:", fCccd, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("SĐT:", fSdt, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Email:", fEmail, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Khu vực:", fKhuVuc, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Đối tượng ưu tiên:", fDoiTuong, null));
        form.add(Box.createVerticalStrut(4));
        form.add(createAddFormFieldRow("Hội đồng thi:", fHoiDong, null));

        JScrollPane scroll = new JScrollPane(form);
        scroll.setPreferredSize(new Dimension(520, 420));
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        int result = JOptionPane.showConfirmDialog(
                this,
                scroll,
                "Thêm thí sinh mới",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        if (fHoTen.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Họ tên là bắt buộc.", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        if (fNgaySinh.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ngày sinh là bắt buộc (định dạng yyyy-MM-dd).", "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String sbd = fSbd.getText().trim();
        if (fCccd.getText().trim().isEmpty()) {
            fCccd.setText(sbd + "_CCCD");
        }

        return buildThiSinhFromFields(
                sbd,
                fHoTen.getText().trim(),
                fNgaySinh.getText().trim(),
                fDanToc.getText(),
                fTonGiao.getText(),
                fGioiTinh.getText(),
                fNoiSinh.getText(),
                fDiaChi.getText(),
                fCccd.getText().trim(),
                fSdt.getText(),
                fEmail.getText(),
                fKhuVuc.getText(),
                fDoiTuong.getText().trim(),
                fHoiDong.getText()
        );
    }

    /**
     * Một dòng form: nhãn + ô. Gợi ý bắt buộc / tùy chọn hiển thị trong ô (placeholder FlatLaf).
     */
    private static JPanel createAddFormFieldRow(String labelText, JTextField field, String placeholder) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line.setOpaque(false);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lab = new JLabel(labelText);
        UiStyles.styleFormLabel(lab);
        line.add(lab);
        UiStyles.styleTextField(field);
        field.setColumns(22);
        if (placeholder != null && !placeholder.isEmpty()) {
            field.putClientProperty("JTextField.placeholderText", placeholder);
        }
        line.add(field);
        return line;
    }

    /** Dòng số báo danh: giá trị luôn có; placeholder chỉ khi trống (thường không trống). */
    private static JPanel createAddFormSbdRow(JTextField fSbd) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        line.setOpaque(false);
        line.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lab = new JLabel("Số báo danh:");
        UiStyles.styleFormLabel(lab);
        line.add(lab);
        UiStyles.styleTextField(fSbd);
        fSbd.setColumns(22);
        fSbd.putClientProperty("JTextField.placeholderText", "Tự động tạo — không cần sửa");
        line.add(fSbd);
        return line;
    }

    /** Gán giá trị ô form vào entity {@link ThiSinh} (parse ngày, đối tượng ưu tiên). */
    private ThiSinh buildThiSinhFromFields(
            String sbd, String hoTen, String ngaySinh,
            String danToc, String tonGiao, String gioiTinh, String noiSinh, String diaChi,
            String cccd, String sdt, String email, String khuVuc, String doiTuong, String hoiDong
    ) {
        ThiSinh t = new ThiSinh();
        t.soBaoDanh = sbd;
        t.hoTen = hoTen;
        t.ngaySinh = LocalDate.parse(ngaySinh.trim());
        t.danToc = blankToNull(danToc);
        t.tonGiao = blankToNull(tonGiao);
        t.gioiTinh = blankToNull(gioiTinh);
        t.noiSinh = blankToNull(noiSinh);
        t.diaChi = blankToNull(diaChi);
        t.soCanCuoc = blankToNull(cccd);
        t.soDienThoai = blankToNull(sdt);
        t.email = blankToNull(email);
        t.khuVuc = blankToNull(khuVuc);
        t.doiTuongUuTien = Integer.parseInt(doiTuong.trim());
        t.hoiDongThi = blankToNull(hoiDong);
        return t;
    }

    /** Chuỗi rỗng sau trim → {@code null} cho cột nullable trong DB. */
    private static String blankToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Đóng frame và hiện lại menu. */
    private void goBack() {
        dispose();
        if (onBack != null) {
            onBack.run();
        }
    }
}
