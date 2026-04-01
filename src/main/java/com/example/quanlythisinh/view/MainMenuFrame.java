package com.example.quanlythisinh.view;

import javax.swing.*;
import java.awt.*;

/**
 * Menu sau đăng nhập: nền #ECF0F1, header #2C3E50, lưới 2×2 nút lớn flat bo 10px.
 */
public class MainMenuFrame extends JFrame {
    /**
     * @param openThiSinh    mở {@link QuanLyThiSinhFrame}
     * @param openDiemThi    mở {@link QuanLyDiemThiFrame}
     * @param openNguyenVong mở {@link QuanLyNguyenVongFrame}
     */
    public MainMenuFrame(Runnable openThiSinh, Runnable openDiemThi, Runnable openNguyenVong) {
        setTitle("Hệ thống quản lý thí sinh");
        setSize(900, 520);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UiStyles.styleMainMenuFrame(this);

        String welcome =
                "Chào mừng đến với hệ thống quản lý thí sinh dự thi đại học";
        JPanel header = UiStyles.createDarkHeaderBar(welcome, true);

        JButton btnThiSinh = new JButton("Quản lý Thí sinh");
        JButton btnDiemThi = new JButton("Quản lý Điểm thi");
        JButton btnNguyenVong = new JButton(
                "<html><div style='text-align:center'>Quản lý nguyện vọng và<br>trường đại học</div></html>");
        JButton btnExit = new JButton("Thoát");
        UiStyles.styleMenuButton(btnThiSinh, true);
        UiStyles.styleMenuButton(btnDiemThi, false);
        UiStyles.styleMenuButton(btnNguyenVong, false);
        UiStyles.styleMenuButton(btnExit, false);

        btnThiSinh.addActionListener(e -> openThiSinh.run());
        btnDiemThi.addActionListener(e -> openDiemThi.run());
        btnNguyenVong.addActionListener(e -> openNguyenVong.run());
        btnExit.addActionListener(e -> System.exit(0));

        JPanel grid = new JPanel(new GridLayout(2, 2, 20, 20));
        grid.setOpaque(false);
        grid.add(btnThiSinh);
        grid.add(btnDiemThi);
        grid.add(btnNguyenVong);
        grid.add(btnExit);

        JPanel card = UiStyles.createMainMenuCard(new BorderLayout(0, 0));
        card.add(grid, BorderLayout.CENTER);

        JPanel root = UiStyles.createMainMenuRootPanel(new BorderLayout(0, 18));
        root.add(header, BorderLayout.NORTH);
        root.add(card, BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }
}
