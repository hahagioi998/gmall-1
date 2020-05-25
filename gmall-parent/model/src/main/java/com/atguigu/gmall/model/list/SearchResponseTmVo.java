package com.atguigu.gmall.model.list;

import lombok.Data;

import java.io.Serializable;

@Data
public class SearchResponseTmVo implements Serializable {

    //当前属性值的所有值
    private Long tmId;
    //属性名称
    private String tmName;//网络制式，分类
    //属性图片
    private String tmLogoUrl;
}
