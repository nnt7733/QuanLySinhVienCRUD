package com.example.quanlythisinh.view;

import com.formdev.flatlaf.FlatClientProperties;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Giao diện chung: nền #F8F9FA, primary #4A90E2, header #2C3E50, font Segoe UI 14–16px, FlatLaf.
 * Menu chính sau đăng nhập: nền #ECF0F1, thẻ trắng bo 10px.
 */
public final class UiStyles {
    public static final int GAP_H = 12;
    public static final int GAP_V = 8;
    public static final int GAP_PANEL = 16;
    public static final int PAD_CARD = 20;

    public static final Font FONT_NORMAL;
    public static final Font FONT_BOLD;
    public static final Font FONT_TITLE;
    /** Nút menu chính — 16px đậm (Segoe UI) */
    public static final Font FONT_MENU_LARGE;

    /** Nền trang — #F8F9FA */
    public static final Color BG_PAGE = new Color(0xF8F9FA);
    /** Nền menu chính (sau đăng nhập) — #ECF0F1 */
    public static final Color BG_MENU = new Color(0xECF0F1);
    /** Thẻ / nền sáng */
    public static final Color BG_CARD = Color.WHITE;
    /** Header bảng / thanh tiêu đề — #2C3E50 */
    public static final Color BG_HEADER_DARK = new Color(0x2C3E50);
    /** Nút primary — #4A90E2 */
    public static final Color PRIMARY = new Color(0x4A90E2);
    public static final Color PRIMARY_HOVER = new Color(0x3A7BC8);
    public static final Color PRIMARY_PRESSED = new Color(0x2E6BB5);
    /** Chữ chính */
    public static final Color FG_TEXT = new Color(0x2C3E50);
    public static final Color FG_MUTED = new Color(0x6C757D);
    public static final Color BORDER = new Color(0xDEE2E6);
    /** Xen kẽ dòng bảng */
    public static final Color ZEBRA = new Color(0xF1F3F5);
    public static final Color SELECTION_BG = new Color(0xD6E8FC);

    static {
        Font base = pickUiBaseFont();
        FONT_NORMAL = base.deriveFont(Font.PLAIN, 15f);
        FONT_BOLD = base.deriveFont(Font.BOLD, 15f);
        FONT_TITLE = base.deriveFont(Font.BOLD, 20f);
        FONT_MENU_LARGE = base.deriveFont(Font.BOLD, 16f);
    }

    private UiStyles() {
    }

    private static Font pickUiBaseFont() {
        String[] names = {"Segoe UI", "Arial", Font.SANS_SERIF};
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String want : names) {
            for (String fam : available) {
                if (fam.equalsIgnoreCase(want)) {
                    return new Font(fam, Font.PLAIN, 15);
                }
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    }

    /**
     * Gọi sau {@link com.formdev.flatlaf.FlatLightLaf#setup()}: theme, bo góc, bảng, nút.
     */
    public static void installGlobalSwingDefaults() {
        UIManager.put("defaultFont", new FontUIResource(FONT_NORMAL));

        UIManager.put("Panel.background", BG_PAGE);
        UIManager.put("Label.foreground", FG_TEXT);

        UIManager.put("Button.font", new FontUIResource(FONT_BOLD));
        UIManager.put("Button.arc", 8);
        UIManager.put("Button.minimumWidth", 72);
        UIManager.put("Button.minimumHeight", 36);

        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("TextField.margin", new Insets(8, 12, 8, 12));
        UIManager.put("PasswordField.margin", new Insets(8, 12, 8, 12));

        UIManager.put("Table.font", new FontUIResource(FONT_NORMAL));
        UIManager.put("Table.selectionBackground", SELECTION_BG);
        UIManager.put("Table.selectionForeground", FG_TEXT);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.gridColor", new Color(0xE9ECEF));

        UIManager.put("TableHeader.font", new FontUIResource(FONT_BOLD.deriveFont(14f)));
        UIManager.put("TableHeader.background", BG_HEADER_DARK);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("TableHeader.height", 36);
        UIManager.put("TableHeader.cellBorder", BorderFactory.createEmptyBorder(4, 10, 4, 10));

        UIManager.put("ScrollPane.arc", 8);
        UIManager.put("ScrollPane.smoothScrolling", true);

        UIManager.put("ComboBox.padding", new Insets(6, 10, 6, 10));

        UIManager.put("Label.font", new FontUIResource(FONT_NORMAL));
        UIManager.put("OptionPane.messageFont", new FontUIResource(FONT_NORMAL));
        UIManager.put("TitledBorder.font", new FontUIResource(FONT_BOLD));
        UIManager.put("Panel.font", new FontUIResource(FONT_NORMAL));
    }

    /**
     * Nền trang + nút X kết thúc toàn bộ ứng dụng — dùng cho màn đăng nhập (cửa sổ gốc).
     */
    public static void styleFrame(JFrame frame) {
        frame.getContentPane().setBackground(BG_PAGE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Cửa sổ mở từ menu: nút X chỉ đóng cửa sổ này và gọi {@code onClosed} (hiện lại menu),
     * không gọi {@code System#exit} — tránh tắt cả app khi đóng màn phụ.
     */
    public static void styleChildFrame(JFrame frame, Runnable onClosed) {
        frame.getContentPane().setBackground(BG_PAGE);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (onClosed != null) {
                    onClosed.run();
                }
            }
        });
    }

    /**
     * Thông báo ngắn đáy cửa sổ, ~1,5 giây rồi tự tắt (không cần bấm OK).
     */
    public static void showShortToast(Window owner, String message) {
        if (owner == null || message == null || message.isBlank()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JLabel label = new JLabel(message);
            label.setFont(FONT_NORMAL);
            label.setForeground(Color.WHITE);
            label.setBackground(BG_HEADER_DARK);
            label.setOpaque(true);
            label.setBorder(new EmptyBorder(10, 22, 10, 22));

            JWindow toast = new JWindow(owner);
            toast.setAlwaysOnTop(true);
            toast.add(label);
            toast.pack();

            Point loc = owner.getLocationOnScreen();
            Dimension sz = owner.getSize();
            int x = loc.x + (sz.width - toast.getWidth()) / 2;
            int y = loc.y + sz.height - toast.getHeight() - 28;
            toast.setLocation(x, y);
            toast.setVisible(true);

            Timer t = new Timer(1500, e -> {
                toast.dispose();
                ((Timer) e.getSource()).stop();
            });
            t.setRepeats(false);
            t.start();
        });
    }

    /** Menu chính: nền xám nhạt #ECF0F1 (Windows 11 / giáo dục). */
    public static void styleMainMenuFrame(JFrame frame) {
        frame.getContentPane().setBackground(BG_MENU);
    }

    /**
     * Khối nội dung menu: thẻ trắng, bo 10px, viền nhẹ, đổ bóng mềm (FlatLaf 3+).
     */
    public static JPanel createMainMenuCard(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BG_CARD);
        panel.setOpaque(true);
        panel.putClientProperty(
                FlatClientProperties.STYLE,
                "arc: 10; borderColor: #D5DBE1; borderWidth: 1; shadow: medium");
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));
        return panel;
    }

    /** Panel gốc menu: nền {@link #BG_MENU}, lề xung quanh. */
    public static JPanel createMainMenuRootPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BG_MENU);
        panel.setBorder(new EmptyBorder(20, 24, 24, 24));
        return panel;
    }

    /** Thẻ trắng, viền nhẹ, bo góc (FlatLaf). */
    public static JPanel createCardPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BG_CARD);
        panel.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(PAD_CARD, PAD_CARD, PAD_CARD, PAD_CARD)
        ));
        return panel;
    }

    public static JPanel createRootPanel(LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BG_PAGE);
        panel.setBorder(new EmptyBorder(GAP_PANEL, GAP_PANEL, GAP_PANEL, GAP_PANEL));
        return panel;
    }

    /**
     * Thanh header tối (#2C3E50): tiêu đề trắng căn giữa.
     *
     * @param useHtml nếu true, {@code text} là snippet HTML (màu trắng trong style).
     */
    public static JPanel createDarkHeaderBar(String text, boolean useHtml) {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER_DARK);
        bar.setBorder(new EmptyBorder(22, 32, 22, 32));
        JLabel l = new JLabel();
        if (useHtml) {
            l.setText("<html><div style='text-align:center;color:#FFFFFF;font-family:Segoe UI;font-size:19px;font-weight:600'>"
                    + text
                    + "</div></html>");
        } else {
            l.setText(text);
            l.setFont(FONT_TITLE);
            l.setForeground(Color.WHITE);
        }
        l.setHorizontalAlignment(SwingConstants.CENTER);
        bar.add(l, BorderLayout.CENTER);
        return bar;
    }

    public static void applyFormTitle(JPanel panel, String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                FONT_BOLD,
                FG_TEXT
        );
        panel.setBorder(BorderFactory.createCompoundBorder(tb, new EmptyBorder(8, 8, 8, 8)));
    }

    /** Nút chuẩn: primary (xanh) hoặc secondary (trắng viền). Có hover. */
    public static void styleButton(JButton button, boolean primary) {
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setMargin(new Insets(10, 18, 10, 18));
        if (primary) {
            button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            attachPrimaryHover(button);
        } else {
            button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
            button.setBackground(BG_CARD);
            button.setForeground(FG_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(9, 17, 9, 17)
            ));
            attachSecondaryHover(button);
        }
    }

    /** Nút menu lớn (menu chính): flat, bo 10px, phông Segoe UI 16px đậm. */
    public static void styleMenuButton(JButton button, boolean primary) {
        button.setFont(FONT_MENU_LARGE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 64));
        button.setMinimumSize(new Dimension(220, 56));
        button.putClientProperty(FlatClientProperties.BUTTON_TYPE, FlatClientProperties.BUTTON_TYPE_ROUND_RECT);
        button.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        if (primary) {
            button.setBackground(PRIMARY);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));
            attachPrimaryHover(button);
        } else {
            button.setBackground(BG_CARD);
            button.setForeground(FG_TEXT);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER, 1, true),
                    new EmptyBorder(15, 21, 15, 21)
            ));
            attachSecondaryHover(button);
        }
    }

    private static void attachPrimaryHover(JButton b) {
        final Color base = PRIMARY;
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(PRIMARY_HOVER);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(base);
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                b.setBackground(PRIMARY_PRESSED);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                b.setBackground(b.contains(e.getX(), e.getY()) ? PRIMARY_HOVER : base);
            }
        });
    }

    private static void attachSecondaryHover(JButton b) {
        final Color baseBg = BG_CARD;
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(new Color(0xF8F9FA));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                b.setBackground(baseBg);
            }
        });
    }

    public static void styleInput(JLabel label, JComponent input) {
        label.setFont(FONT_BOLD);
        label.setForeground(FG_TEXT);
        styleFieldCore(input);
    }

    public static void styleTextField(JTextField field) {
        styleFieldCore(field);
    }

    public static void styleCombo(JComboBox<?> combo) {
        styleFieldCore(combo);
    }

    private static void styleFieldCore(JComponent c) {
        c.setFont(FONT_NORMAL);
        c.setForeground(FG_TEXT);
        c.putClientProperty(FlatClientProperties.STYLE, "arc: 6");
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    public static JLabel toolbarLabel(String text) {
        JLabel l = new JLabel(text);
        styleFormLabel(l);
        return l;
    }

    public static void styleFormLabel(JLabel label) {
        label.setFont(FONT_BOLD);
        label.setForeground(FG_TEXT);
    }

    public static void styleTable(JTable table) {
        table.setFont(FONT_NORMAL);
        table.setRowHeight(34);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(FG_TEXT);
        table.setFillsViewportHeight(true);
        table.setGridColor(new Color(0xE9ECEF));

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD.deriveFont(14f));
        header.setBackground(BG_HEADER_DARK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, BORDER));
    }

    public static void enableZebraRows(JTable table) {
        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
                } else {
                    c.setBackground(SELECTION_BG);
                }
                c.setForeground(FG_TEXT);
                if (c instanceof JComponent jc) {
                    jc.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
                }
                return c;
            }
        });
    }

    public static void enableSorting(JTable table) {
        TableModel model = table.getModel();
        if (model != null) {
            table.setRowSorter(new TableRowSorter<>(model));
        }
    }

    public static void applyColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static JScrollPane wrapScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        sp.putClientProperty(FlatClientProperties.STYLE, "arc: 10");
        return sp;
    }

    public static JPanel createActionBar(JComponent... buttons) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, GAP_H, GAP_V));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(GAP_V, 0, 0, 0));
        for (JComponent b : buttons) {
            p.add(b);
        }
        return p;
    }

    /** Viền bo nhẹ cho panel nổi (đăng nhập). */
    public static AbstractBorder roundedSoftBorder() {
        return new AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BORDER);
                g2.draw(new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, width - 1, height - 1, 12, 12));
                g2.dispose();
            }

            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(1, 1, 1, 1);
            }

            @Override
            public Insets getBorderInsets(Component c, Insets insets) {
                insets.set(1, 1, 1, 1);
                return insets;
            }
        };
    }
}
