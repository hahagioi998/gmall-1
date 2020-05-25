package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试Thymeleaf
 * 页面  标签
 * 转发页面
 * 响应Json （不是）
 */
@Controller
public class TestThymeleafController {


    //测试请求  Springmvc 课程上 学习的  跳转视图
    @GetMapping("/thymeleaf/test")
    public String testThymeleaf( Model model){
        model.addAttribute("hello","大家好才是真的好");//request.setAttribute(k,v)

        List<String> persons = new ArrayList<>();
        persons.add("曾志传");
        persons.add("刘德华");
        model.addAttribute("persons",persons);
        return "test"; //默认为.html
    }
}
