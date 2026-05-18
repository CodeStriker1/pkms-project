package com.mca.pkms.controller;

import com.mca.pkms.entity.User;
import com.mca.pkms.service.UserService;
import org.springframework.security.core.Authentication;

public abstract class BaseController {
    private final UserService userService;

    protected BaseController(UserService userService) {
        this.userService = userService;
    }

    protected User currentUser(Authentication authentication) {
        return userService.current(authentication.getName());
    }
}
