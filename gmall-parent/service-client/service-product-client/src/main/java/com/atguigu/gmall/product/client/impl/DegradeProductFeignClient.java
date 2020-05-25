package com.atguigu.gmall.product.client.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 降级处理实现类
 */
@Component
public class DegradeProductFeignClient implements ProductFeignClient {

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        return null;
    }

    @Override
    public List<Map> getBaseCategoryList() {
        return null;
    }

    @Override
    public BaseTrademark getBaseTrademark(Long tmId) {
        return null;
    }

    @Override
    public List<SkuAttrValue> getAttrList(Long skuId) {
        return null;
    }
}
