package com.atguigu.gmall.product.mapper;


import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BaseAttrInfoMapper extends BaseMapper<BaseAttrInfo> {


    //1:打注解写Sql   违反 解耦
    //@Select("select * from ....")  #{0}  #{param1}
    List<BaseAttrInfo> attrInfoList(@Param("category1Id") Long category1Id,
                                    @Param("category2Id") Long category2Id,
                                    @Param("category3Id") Long category3Id);

    //2: Mapper文件 提取出了Sql语句  同包同名


}
