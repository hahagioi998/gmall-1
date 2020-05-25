package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    //5:根据spuId查询销售组合与SkuId之间的对应   开发工程师  开荒  从无到有
    // 当你要查询的数据 没有对应的POJO或JavaBean对象  使用Map  无敌版JavaBean
    List<Map> getSkuValueIdsMap(Long spuId);
}
