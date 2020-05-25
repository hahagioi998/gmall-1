package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 购物车管理
 */
@Controller
public class CartController {

    //加入购物车 跳转到加入购物车成功页面
    @GetMapping("/addCart.html")
    public String addCart(Long skuId, Integer skuNum, Model model){

        //将当前商品加入购物车（持久化） 返回值 ：CartInfo
        model.addAttribute("cartInfo",null);

        return "cart/addCart";

    }
}
