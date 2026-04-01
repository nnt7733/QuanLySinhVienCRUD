package com.example.quanlythisinh.controller;

import com.example.quanlythisinh.service.AuthService;

public class LoginController {
    private final AuthService authService;

    /** Ham khoi tao controller dang nhap. */
    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    /** Ham xu ly kiem tra thong tin dang nhap. */
    public boolean login(String username, String password) {
        return authService.login(username, password);
    }
}
