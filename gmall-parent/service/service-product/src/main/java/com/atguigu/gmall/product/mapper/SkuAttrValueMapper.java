package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.SkuAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SkuAttrValueMapper extends BaseMapper<SkuAttrValue> {

    //根据SkuId查询平台属性id/名称 平台属性值的名称
    List<SkuAttrValue> getAttrList(Long skuId);
}
