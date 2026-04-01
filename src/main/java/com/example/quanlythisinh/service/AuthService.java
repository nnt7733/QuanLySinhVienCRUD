package com.example.quanlythisinh.service;

public class AuthService {
    /** Ham kiem tra dang nhap theo thong tin hardcode admin/123. */
    public boolean login(String username, String password) {
        return "admin".equals(username) && "123".equals(password);
    }
}
