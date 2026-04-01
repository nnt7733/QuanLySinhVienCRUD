package com.example.quanlythisinh;

import com.example.quanlythisinh.controller.DiemThiController;
import com.example.quanlythisinh.controller.LoginController;
import com.example.quanlythisinh.controller.NguyenVongController;
import com.example.quanlythisinh.controller.ThiSinhController;
import com.example.quanlythisinh.controller.TimKiemThongKeController;
import com.example.quanlythisinh.config.JpaUtil;
import com.example.quanlythisinh.service.*;
import com.example.quanlythisinh.view.*;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
/** Điểm vào ứng dụng: cấu hình Swing, wiring MVC, đăng nhập → menu. */
public class MainApp {
    
    public static void main(String[] args) {
        // Bật anti-aliasing để hiển thị chữ mượt hơn trên Swing.
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> {
            // Khởi tạo giao diện FlatLaf và theme chung cho toàn ứng dụng.
            try {
                FlatLightLaf.setup();
            } catch (Exception e) {
                throw new RuntimeException("Không khởi tạo được FlatLaf", e);
            }
            UiStyles.installGlobalSwingDefaults();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    JpaUtil.shutdown();
                } catch (Exception ignored) {
                    // đóng EMF an toàn khi thoát bằng bất kỳ cửa sổ nào
                }
            }));
            // Khởi tạo các service nghiệp vụ.
            AuthService authService = new AuthService();
            ThiSinhService thiSinhService = new ThiSinhService();
            DiemThiService diemThiService = new DiemThiService();
            BaoCaoService baoCaoService = new BaoCaoService();
            TruongDaiHocService truongDaiHocService = new TruongDaiHocService();
            DangKyNguyenVongService dangKyNguyenVongService = new DangKyNguyenVongService();
            LookupService lookupService = new LookupService();

            // Khởi tạo controller với các service tương ứng.
            LoginController loginController = new LoginController(authService);
            ThiSinhController thiSinhController = new ThiSinhController(thiSinhService);
            DiemThiController diemThiController = new DiemThiController(diemThiService);
            TimKiemThongKeController timKiemThongKeController = new TimKiemThongKeController(baoCaoService);
            NguyenVongController nguyenVongController = new NguyenVongController(
                    truongDaiHocService, dangKyNguyenVongService, lookupService);

            // Giữ tham chiếu menu chính để ẩn/hiện khi chuyển màn hình.
            final MainMenuFrame[] menuHolder = new MainMenuFrame[1];
            
            // Callback mở màn Quản lý thí sinh.
            final Runnable[] openThiSinhHolder = new Runnable[1];

            // Callback mở màn Quản lý điểm thi.
            final Runnable[] openDiemThiHolder = new Runnable[1];

            // Callback mở màn Quản lý nguyện vọng.
            final Runnable[] openNguyenVongHolder = new Runnable[1];

            // Định nghĩa hành động mở màn Quản lý thí sinh.
            openThiSinhHolder[0] = () -> {
                if (menuHolder[0] != null) {
                    menuHolder[0].setVisible(false);
                }
                QuanLyThiSinhFrame thiSinhFrame = new QuanLyThiSinhFrame(
                        thiSinhController,
                        () -> menuHolder[0].setVisible(true)
                );
                thiSinhFrame.setVisible(true);
            };

            // Định nghĩa hành động mở màn Quản lý điểm thi.
            openDiemThiHolder[0] = () -> {
                menuHolder[0].setVisible(false);
                QuanLyDiemThiFrame diemFrame = new QuanLyDiemThiFrame(
                        diemThiController,
                        timKiemThongKeController,
                        () -> menuHolder[0].setVisible(true),
                        null,
                        false
                );
                diemFrame.setVisible(true);
            };

            // Định nghĩa hành động mở màn Quản lý nguyện vọng.
            openNguyenVongHolder[0] = () -> {
                menuHolder[0].setVisible(false);
                QuanLyNguyenVongFrame nvFrame = new QuanLyNguyenVongFrame(
                        nguyenVongController,
                        timKiemThongKeController,
                        () -> menuHolder[0].setVisible(true)
                );
                nvFrame.setVisible(true);
            };

            // Tạo menu chính và gán callback điều hướng tới từng màn hình.
            MainMenuFrame menuFrame = new MainMenuFrame(
                    () -> openThiSinhHolder[0].run(),
                    () -> openDiemThiHolder[0].run(),
                    () -> openNguyenVongHolder[0].run()
            );
            menuHolder[0] = menuFrame;

            // Màn hình đăng nhập là màn đầu tiên; đăng nhập thành công sẽ hiện menu.
            LoginFrame loginFrame = new LoginFrame(loginController, () -> menuFrame.setVisible(true));
            loginFrame.setVisible(true);
        });
    }
}
