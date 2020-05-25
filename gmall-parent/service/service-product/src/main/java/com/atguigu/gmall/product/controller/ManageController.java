package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 后台管理中心
 */
@Api(tags = "后台管理中心")
@RestController
@RequestMapping("/admin/product") //公共路径
//@CrossOrigin  //跨域注解
public class ManageController {


    @Autowired
    private ManageService manageService;
    //1、获取一级分类
    @ApiOperation("获取一级分类")
    @GetMapping("/getCategory1")
    public Result getCategory1(){
        List<BaseCategory1> baseCategory1List =  manageService.getCategory1();
        return Result.ok(baseCategory1List);
    }
    //2:获取二级分类
    @GetMapping("/getCategory2/{category1Id}")
    public Result getCategory2(@PathVariable(name = "category1Id") Long category1Id){

        List<BaseCategory2> baseCategory2List = manageService.getCategory2(category1Id);

        return Result.ok(baseCategory2List);
    }
    //3:获取三级分类
    @GetMapping("/getCategory3/{category2Id}")
    public Result getCategory3(@PathVariable(name = "category2Id") Long category2Id){
        List<BaseCategory3> baseCategory3List = manageService.getCategory3(category2Id);
        return Result.ok(baseCategory3List);
    }
    //根据一二三级分类 查询平台属性集合
    @GetMapping("/attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result attrInfoList(
            @PathVariable(name = "category1Id") Long category1Id,
            @PathVariable(name = "category2Id") Long category2Id,
            @PathVariable(name = "category3Id") Long category3Id
    ){
        List<BaseAttrInfo> baseAttrInfoList =  manageService.attrInfoList(category1Id,category2Id,category3Id);

        return Result.ok(baseAttrInfoList);
    }
    //保存平台属性  http://api.gmall.com/admin/product/saveAttrInfo
    @PostMapping("/saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }
    //查询SPU分页列表
    @GetMapping("/{page}/{limit}")
    public Result  spuPage(@PathVariable(name = "page") Integer page,
                           @PathVariable(name = "limit") Integer limit,
                           Long category3Id
                           ){
        //分页列表  Mybatis-plus  分页
        IPage<SpuInfo> p = manageService.spuPage(page,limit,category3Id);
        return Result.ok(p);
    }
    //去SPU添加页面 加载所有品牌
    @GetMapping("/baseTrademark/getTrademarkList")
    public Result getTrademarkList(){
        List<BaseTrademark> baseTrademarkList =  manageService.getTrademarkList();
        return Result.ok(baseTrademarkList);
    }
    //查询字典表中 销售属性集合
    @GetMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){
        List<BaseSaleAttr> baseSaleAttrList = manageService.baseSaleAttrList();
        return Result.ok(baseSaleAttrList);
    }
    //保存SPU
    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){
        //保存SPU
        manageService.saveSpuInfo(spuInfo);
        return Result.ok();
    }
    //保存SKU之前查询图片集合
    @GetMapping("/spuImageList/{spuId}")
    public Result spuImageList(@PathVariable(name = "spuId") Long spuId){
        List<SpuImage>  spuImageList = manageService.spuImageList(spuId);
        return Result.ok(spuImageList);
    }
    //根据SpuId查询销售属性及属性值
    @GetMapping("/spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable(name = "spuId") Long spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.spuSaleAttrList(spuId);
        return Result.ok(spuSaleAttrList);
    }
    // 保存SKU
    @PostMapping("/saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){
        //保存SKU
        manageService.saveSkuInfo(skuInfo);
        return Result.ok();
    }
    //查询库存分页
    @GetMapping("/list/{page}/{limit}")
    public Result skuList(@PathVariable(name = "page") Integer page,@PathVariable(name = "limit") Integer limit){
        //分页对象
        IPage<SkuInfo> p = manageService.skuList(page,limit);
        return Result.ok(p);
    }
    //上架
    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable(name = "skuId") Long skuId){

        manageService.onSale(skuId);
        return Result.ok();
    }
    //下架
    @GetMapping("/cancelSale/{skuId}")
    public Result cancelSale(@PathVariable(name = "skuId") Long skuId){
        manageService.cancelSale(skuId);
        return Result.ok();
    }

}
