package org.ng.distribute.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("user")
public class LoginController {

    @GetMapping("login")
    public String login() {
        return "success";
    }
}
