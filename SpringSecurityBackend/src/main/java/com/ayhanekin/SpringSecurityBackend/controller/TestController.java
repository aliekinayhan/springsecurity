package com.ayhanekin.SpringSecurityBackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


    @GetMapping("/")
    public String hello () {
        return "We are in";
    }

    @GetMapping("/admin")
    public String admin() {
        return "Admin page";
    }

    @GetMapping("/user")
    public String user() {
        return "User page";
    }
}
