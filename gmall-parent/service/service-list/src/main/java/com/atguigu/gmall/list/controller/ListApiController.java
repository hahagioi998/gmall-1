package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * 索引管理
 */
@RestController
@RequestMapping("/api/list")
public class ListApiController {

    //@Autowired  // ES 1 ~5     TransportClient
    //private ElasticsearchTemplate elasticsearchTemplate;//低版本  性能低
    @Autowired // ES  6   TransportClient 还在兼容   6.8.1     将来达到 7以上 ES官方网站
    private ElasticsearchRestTemplate elasticsearchRestTemplate;//搜索方式 性能非常好  添加删除并不理想


    @Autowired
    private ListService listService;

    //索引库 Mappings映射
    @GetMapping("/index")
    public Result index() throws Exception{
        //1:创建索引库
        elasticsearchRestTemplate.createIndex(Goods.class);
        //2:Mappings 映射  字段及类型 （域及类型）
        elasticsearchRestTemplate.putMapping(Goods.class);

        return Result.ok();
    }
    //上架商品 保存索引库
    @GetMapping("/inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable(name = "skuId") Long skuId){

        listService.upperGoods(skuId);

        return Result.ok("上架商品成功");
    }
    //下架商品 删除索引库存
    @GetMapping("/inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable(name = "skuId") Long skuId){
        listService.lowerGoods(skuId);
        return Result.ok("下架商品成功");
    }
    //更新索引的热度
    @GetMapping("inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable(name = "skuId") Long skuId){
        listService.incrHotScore(skuId);

        return Result.ok();//默认  成功
    }

    //开始执行搜索
    //返回值    SearchResponseVo  4部分数据
    //   SearchParam 入参  6部分数据
    @PostMapping
    public SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listService.list(searchParam);
    }

}
