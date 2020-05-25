package com.atguigu.gmall.product.client;


import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.impl.DegradeProductFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 对外暴露的商品信息 远程调用
 */
@FeignClient(name = "service-product",fallback = DegradeProductFeignClient.class)
public interface ProductFeignClient {

    //1:根据SkuID查询SkuInfo信息
    @GetMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId);
    //2、根据三级分类ID查询一二三级分类
    @GetMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable(name = "category3Id") Long category3Id);
    //3: 根据skuId查询库存表中的价格
    @GetMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable(name = "skuId") Long skuId);
    //4:根据spuId查询销售属性及属性值 集合
    //      根据skuId查询当前选中项
    @GetMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(
            @PathVariable(name = "skuId") Long skuId,
            @PathVariable(name = "spuId") Long spuId
    );

    //5:根据spuId查询销售组合与SkuId之间的对应   开发工程师  开荒  从无到有
    // 当你要查询的数据 没有对应的POJO或JavaBean对象  使用Map  无敌版JavaBean
    @GetMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable(name = "spuId") Long spuId);


    //查询基本分类视图 所有集合
    @GetMapping("/api/product/getBaseCategoryList")
    public List<Map> getBaseCategoryList();

    //根本品牌ID 查询一个品牌
    @GetMapping("/api/product/getBaseTrademark/{tmId}")
    public BaseTrademark getBaseTrademark(@PathVariable(name = "tmId") Long tmId);

    //根据SkuId查询平台属性id/名称 平台属性值的名称
    @GetMapping("/api/product/inner/getAttrList/{skuId}")
    public List<SkuAttrValue> getAttrList(@PathVariable(name = "skuId") Long skuId);
}
