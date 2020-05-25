package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 购物车管理
 */
@RestController
@RequestMapping("/api/cart")
public class CartApiController {

    @Autowired
    private CartService cartService;

    //加入购物车
    @GetMapping("/addToCart/{skuId}/{skuNum}")
    public CartInfo addToCart(@PathVariable(name = "skuId") Long skuId, @PathVariable(name = "skuNum")
                              Integer skuNum, HttpServletRequest request){
        //用户ID
        String userId = AuthContextHolder.getUserId(request);
        //判断真实用户ID为NULL 使用临时用户的Id
        if(StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        return cartService.addToCart(skuId,skuNum,userId);
    }
}
