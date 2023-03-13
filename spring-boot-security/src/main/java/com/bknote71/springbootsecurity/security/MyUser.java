package com.bknote71.springbootsecurity.security;

import lombok.Data;

@Data
public class MyUser {
    String username;
    String password;
    String role;

    public MyUser() {
    }

    public MyUser(String username, String password) {
        this.username = username;
        this.password = password;
        this.role = "USER";
    }

    public void refine() {
        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password.trim() : "";
    }

}
