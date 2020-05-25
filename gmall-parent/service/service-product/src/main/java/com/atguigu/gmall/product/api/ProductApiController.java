package com.atguigu.gmall.product.api;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *  商品
 *  库存
 *  对外暴露的Api接口
 *  Swagger2
 *    /admin/*  后台管理接口
 *    /api/*    前台管理接口   商品详情页面微服务
 */
@RestController
@RequestMapping("/api/product") //公共路径
public class ProductApiController {

    @Autowired
    private ManageService manageService;

    //1:根据SkuID查询SkuInfo信息
    @GetMapping("/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable(name = "skuId") Long skuId){

        return manageService.getSkuInfo(skuId);
    }
    //2、根据三级分类ID查询一二三级分类
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable(name = "category3Id") Long category3Id){
        return manageService.getCategoryView(category3Id);
    }
    //3: 根据skuId查询库存表中的价格
    @GetMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable(name = "skuId") Long skuId){
        return manageService.getSkuPrice(skuId);
    }
    //4:根据spuId查询销售属性及属性值 集合
    //      根据skuId查询当前选中项
    @GetMapping("/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(
            @PathVariable(name = "skuId") Long skuId,
            @PathVariable(name = "spuId") Long spuId
            ){

        return manageService.getSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    //5:根据spuId查询销售组合与SkuId之间的对应   开发工程师  开荒  从无到有
    // 当你要查询的数据 没有对应的POJO或JavaBean对象  使用Map  无敌版JavaBean
    @GetMapping("/inner/getSkuValueIdsMap/{spuId}")
    public Map getSkuValueIdsMap(@PathVariable(name = "spuId") Long spuId){
        return manageService.getSkuValueIdsMap(spuId);
    }
    //查询基本分类视图 所有集合
    @GetMapping("/getBaseCategoryList")
    public List<Map> getBaseCategoryList(){
        return manageService.getBaseCategoryList();
    }
    //根本品牌ID 查询一个品牌
    @GetMapping("/getBaseTrademark/{tmId}")
    public BaseTrademark getBaseTrademark(@PathVariable(name = "tmId") Long tmId){

        return manageService.getBaseTrademark(tmId);
    }
    //根据SkuId查询平台属性id/名称 平台属性值的名称
    @GetMapping("inner/getAttrList/{skuId}")
    public List<SkuAttrValue> getAttrList(@PathVariable(name = "skuId") Long skuId){

        return manageService.getAttrList(skuId);
    }


}
