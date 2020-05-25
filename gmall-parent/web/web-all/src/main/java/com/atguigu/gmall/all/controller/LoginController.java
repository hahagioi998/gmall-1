package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 登录管理
 */
@Controller
public class LoginController {

    //去登录页面
    @GetMapping("/login.html")
    public String login(String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "login";
    }
}
