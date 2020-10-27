package com.service;

import com.domain.User;

public class AtmService {
    public AtmService() {
    }

    public String login(User user) {
        return "zzt".equals(user.getName()) && 123 == user.getPass() ? "success" : "defeat";
    }
}