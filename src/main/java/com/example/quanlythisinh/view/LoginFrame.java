package com.example.quanlythisinh.view;

import com.example.quanlythisinh.controller.LoginController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private final LoginController loginController;
    private final Runnable onLoginSuccess;
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    /**
     * Khởi tạo màn hình đăng nhập; {@code onLoginSuccess} gọi khi đăng nhập đúng (mở menu chính).
     */
    public LoginFrame(LoginController loginController, Runnable onLoginSuccess) {
        this.loginController = loginController;
        this.onLoginSuccess = onLoginSuccess;
        initUi();
    }

    /** Hàm xử lý sự kiện đăng nhập (xác thực qua LoginController / AuthService). */
    private void handleLogin() {
        String user = txtUsername.getText().trim();
        String pass = new String(txtPassword.getPassword());
        if (pass.isBlank()) {
            JOptionPane.showMessageDialog(this, "Nhập mật khẩu.");
            return;
        }
        if (loginController.login(user, pass)) {
            showLoginSuccessThenContinue();
        } else {
            JOptionPane.showMessageDialog(this, "Sai tài khoản/mật khẩu.");
        }
    }

    /**
     * Hiện popup ngắn báo đăng nhập thành công, đóng form đăng nhập rồi chạy {@code onLoginSuccess}.
     */
    private void showLoginSuccessThenContinue() {
        JDialog dlg = new JDialog(this, "Thông báo", true);
        dlg.setResizable(false);
        dlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(22, 32, 22, 32));
        panel.setBackground(UiStyles.BG_PAGE);
        JLabel msg = new JLabel("Đăng nhập thành công");
        msg.setFont(UiStyles.FONT_BOLD);
        msg.setForeground(UiStyles.FG_TEXT);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(msg, BorderLayout.CENTER);
        dlg.setContentPane(panel);
        dlg.pack();
        dlg.setLocationRelativeTo(this);

        ActionListener close = e -> dlg.dispose();
        Timer timer = new Timer(800, close);
        timer.setRepeats(false);
        timer.start();
        dlg.setVisible(true);
        timer.stop();
        dispose();
        onLoginSuccess.run();
    }

    /** Form đăng nhập: tiêu đề chỉ trên thanh cửa sổ; thẻ trắng bo góc, nút primary / secondary. */
    private void initUi() {
        setTitle("Đăng nhập hệ thống");
        setSize(460, 340);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UiStyles.styleFrame(this);

        JPanel formWrap = new JPanel(new GridBagLayout());
        formWrap.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 8, 0);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(UiStyles.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                UiStyles.roundedSoftBorder(),
                new javax.swing.border.EmptyBorder(28, 32, 28, 32)
        ));

        JLabel lblUser = new JLabel("Tên đăng nhập");
        txtUsername = new JTextField(20);
        UiStyles.styleInput(lblUser, txtUsername);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblUser, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 16, 0);
        card.add(txtUsername, gbc);

        JLabel lblPass = new JLabel("Mật khẩu");
        txtPassword = new JPasswordField(20);
        UiStyles.styleInput(lblPass, txtPassword);
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 4, 0);
        card.add(lblPass, gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(txtPassword, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        JButton btnLogin = new JButton("Đăng nhập");
        JButton btnExit = new JButton("Thoát");
        UiStyles.styleButton(btnLogin, true);
        UiStyles.styleButton(btnExit, false);
        btnRow.add(btnLogin);
        btnRow.add(btnExit);
        gbc.gridy = 4;
        gbc.insets = new Insets(8, 0, 0, 0);
        card.add(btnRow, gbc);

        GridBagConstraints c2 = new GridBagConstraints();
        c2.gridx = 0;
        c2.weightx = 1;
        c2.fill = GridBagConstraints.HORIZONTAL;
        formWrap.add(card, c2);

        btnLogin.addActionListener(e -> handleLogin());
        btnExit.addActionListener(e -> System.exit(0));

        JPanel root = UiStyles.createRootPanel(new BorderLayout(0, 0));
        root.add(formWrap, BorderLayout.CENTER);
        add(root);
        getRootPane().setDefaultButton(btnLogin);
    }
}