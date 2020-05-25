package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品详情页面数据查询
 * Swagger2  /api/*
 */
@Api(value = "商品详情页面数据查询")
@RestController
@RequestMapping("/api/item")
public class ItemApiController {

    @Autowired
    private ItemService itemService;

    //查询数据
    @GetMapping("/getItem/{skuId}")
    public Map<String,Object> getItem(@PathVariable(name = "skuId") Long skuId){
        return itemService.getItem(skuId);
    }

}
