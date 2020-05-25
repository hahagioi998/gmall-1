package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索管理
 */
@Controller
public class ListController {


    @Autowired
    private ListFeignClient listFeignClient;

    //搜索页面  入参 必须是对象  搜索按钮 一个关键词  页面发送的请求入参不是Json格式字符串
    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model){
        //1:入参对象进行回显
        model.addAttribute("searchParam",searchParam);
        //查询结果
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);
        //2:品牌集合
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        //3:平台属性集合
        model.addAttribute("attrsList",searchResponseVo.getAttrsList());
        //4:商品集合
        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        //5:分页 当前页  总页数
        model.addAttribute("pageNo",searchResponseVo.getPageNo());
        model.addAttribute("totalPages",searchResponseVo.getTotalPages());
        //6:UrlParam
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        //7:排序  显示红色底   箭头由高到低或是低到高
        Map orderMap = makeOrderMap(searchParam);
        model.addAttribute("orderMap",orderMap);
        //8: 品牌回显
        String trademarkParam = makeTrademarkParam(searchParam);
        model.addAttribute("trademarkParam",trademarkParam);
        //9: 回显 平台属性
        List<Map> propsParamList = makePropsParamList(searchParam);
        model.addAttribute("propsParamList",propsParamList);
        return "list/index";
    }

    //回显 平台属性
    private List<Map> makePropsParamList(SearchParam searchParam) {
        String[] props = searchParam.getProps();
        if(null != props && props.length > 0){
            return Arrays.stream(props).map(prop -> {
                //平台属性ID：平台属性值：平台属性名称
                String[] p = prop.split(":");//3
                Map map = new HashMap();
                map.put("attrName",p[2]);
                map.put("attrId",p[0]);
                map.put("attrValue",p[1]);
                return map;
            }).collect(Collectors.toList());

        }
        return null;
    }

    //品牌回显
    private String makeTrademarkParam(SearchParam searchParam) {

        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){// 品牌ID：品牌名称
            String[] t = trademark.split(":");
            return "品牌:" + t[1];
        }
        return null;
    }

    //排序  显示红色底   箭头由高到低或是低到高
    private Map makeOrderMap(SearchParam searchParam) {
        Map orderMap = new HashMap();
        //1: type 当前是按照综合还是价格等进行排序
        String order = searchParam.getOrder();//1:desc  1:asc  2:desc 2:asc  ...
        if(!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            orderMap.put("type",o[0]);
            orderMap.put("sort",o[1]);
        }else{
            //入参对象中无排序  走默认
            orderMap.put("type","1");
            orderMap.put("sort","desc");
        }
        return orderMap;

    }

    //UrlParam  字符串拼接
    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder sb = new StringBuilder();
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)){
            sb.append("keyword=").append(keyword);
        }
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            if(sb.length() > 0){
                sb.append("&trademark=").append(trademark);
            }else{
                sb.append("trademark=").append(trademark);
            }
        }
        Long category1Id = searchParam.getCategory1Id();
        if(null != category1Id){
            if(sb.length() > 0){
                sb.append("&category1Id=").append(category1Id);
            }else{
                sb.append("category1Id=").append(category1Id);
            }
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id){
            if(sb.length() > 0){
                sb.append("&category2Id=").append(category2Id);
            }else{
                sb.append("category2Id=").append(category2Id);
            }
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id){
            if(sb.length() > 0){
                sb.append("&category3Id=").append(category3Id);
            }else{
                sb.append("category3Id=").append(category3Id);
            }
        }
        //平台属性
        String[] props = searchParam.getProps();
        if(null != props && props.length > 0){
            for (String prop : props) {
                if(sb.length() > 0){
                    sb.append("&props=").append(prop);
                }else{
                    sb.append("props=").append(prop);
                }
            }
        }
        return "/list.html?" + sb.toString();
    }
}
