package com.atguigu.gmall.item.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * 商品详情页面远程调用
 */
@FeignClient("service-item")
public interface ItemFeignClient {


    //查询数据
    @GetMapping("/api/item/getItem/{skuId}")
    public Map<String,Object> getItem(@PathVariable(name = "skuId") Long skuId);
}
