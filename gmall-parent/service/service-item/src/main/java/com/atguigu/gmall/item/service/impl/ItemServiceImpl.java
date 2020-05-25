package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ListFeignClient listFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;//自定义线程池

    //查询所有商品详情页面 所需要的数据 汇总  返回值  Map
    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map<String, Object> result = new HashMap<>();
//        1、根据SkuID查询SkuInfo信息
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            result.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPoolExecutor);

//        2、根据三级分类ID查询一二三级分类
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            result.put("categoryView", categoryView);
        }, threadPoolExecutor);

//        3、根据skuId查询库存表中的价格
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            result.put("price", skuPrice);
        }, threadPoolExecutor);

//        4、根据spuId查询销售属性及属性值 集合
//        根据skuId查询当前选中项
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient
                    .getSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
            result.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        }, threadPoolExecutor);

//        5、根据spuId查询销售组合与SkuId之间的对应
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync((skuInfo) -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            result.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));
        }, threadPoolExecutor);
        //6:访问当前商品详情页面获取数据的时候  对此商品的热度进行加分
        CompletableFuture.runAsync(() -> {
            listFeignClient.incrHotScore(skuId);
        },threadPoolExecutor);
        //选择：要不要等待上面的线程执行完成  选择不等  也可以选择等待
        //本次是必须要等待上面多线程全部执行完成
        CompletableFuture.allOf(skuInfoCompletableFuture,priceCompletableFuture,categoryViewCompletableFuture,
                spuSaleAttrListCompletableFuture,valuesSkuJsonCompletableFuture).join();

        return result;
    }
}
