package com.atguigu.gmall.list.client;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 远程调用  搜索微服务
 */
@FeignClient("service-list")
public interface ListFeignClient {

    //更新索引的热度
    @GetMapping("/api/list/inner/incrHotScore/{skuId}")
    public Result incrHotScore(@PathVariable(name = "skuId") Long skuId);

    //开始执行搜索
    //返回值    SearchResponseVo  4部分数据
    //   SearchParam 入参  6部分数据
    @PostMapping("/api/list")
    public SearchResponseVo list(@RequestBody SearchParam searchParam);
}
