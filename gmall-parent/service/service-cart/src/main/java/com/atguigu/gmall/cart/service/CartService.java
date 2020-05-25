package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

public interface CartService {
    CartInfo addToCart(Long skuId, Integer skuNum, String userId);
}
