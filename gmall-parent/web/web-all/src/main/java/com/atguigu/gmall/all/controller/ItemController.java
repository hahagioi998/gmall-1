package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情页面 渲染
 */
@Controller
public class ItemController {


    @Autowired
    private ItemFeignClient itemFeignClient;

    //请求转发商品详情页面
    //  https://item.jd.com  / 100012407280 .html
    @GetMapping("/{skuId}.html")
    public String itemIndex(@PathVariable(name = "skuId") Long skuId, Model model){
        //回显数据 放在Request域中
        Map<String, Object> map = itemFeignClient.getItem(skuId);
        model.addAllAttributes(map);// K 使用Map的Key V使用Map的Value
        return "item/index";
    }
}
