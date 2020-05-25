package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuAttrValue;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 检索管理  索引库
 * 添加
 * 删除
 * 查询
 * 修改
 */
@Service
public class ListServiceImpl implements ListService {


    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private RedisTemplate redisTemplate;

    //添加索引
    @Override
    public void upperGoods(Long skuId) {

        Goods goods = new Goods();

        //1:SkuInfo   ID、标题（商品名称）价格 图片
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setId(skuInfo.getId());
        goods.setTitle(skuInfo.getSkuName());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());
        //2:品牌
        //ID
        BaseTrademark baseTrademark = productFeignClient.getBaseTrademark(skuInfo.getTmId());
        goods.setTmId(baseTrademark.getId());
        //名称
        goods.setTmName(baseTrademark.getTmName());
        //Logo图片
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        //3:一二三级分类  ID /名称
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());
        //4:平台属性集合  Lomda
        List<SkuAttrValue> attrList = productFeignClient.getAttrList(skuId);

        List<SearchAttr> searchAttrList = attrList.stream().map(skuAttrValue -> {
            SearchAttr searchAttr = new SearchAttr();
            //平台属性ID
            searchAttr.setAttrId(skuAttrValue.getBaseAttrInfo().getId());
            //平台属性名称
            searchAttr.setAttrName(skuAttrValue.getBaseAttrInfo().getAttrName());
            //平台属性值名称
            searchAttr.setAttrValue(skuAttrValue.getBaseAttrValue().getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrList);
        //5:时间
        goods.setCreateTime(new Date());
        //保存索引
        goodsDao.save(goods);

    }

    //删除索引
    @Override
    public void lowerGoods(Long skuId) {
        goodsDao.deleteById(skuId);
    }

    //更新索引的热度
    @Override
    public void incrHotScore(Long skuId) {
        //0:先使用缓存进行缓冲操作   每加1 不要直接操作索引库
        //先将分在缓存追加 追加到一定程度 再更新索引库
        //1~9分  11~19分... 保存缓存中
        //Redis 五大数据类型  ：
        // String类型（值类型）  Hash类型（散列类型）
        // List类型（列表类型） Set类型（集合类型） Zset类型（有序集合类型）
        //现在就要使用zset类型
        //小明  考试的成绩出来： 语文：99分  英文：88分  数学：90分  让你把小明的分数记录下来
//        redisTemplate.opsForZSet().add("小明","语文",99);
//        redisTemplate.opsForZSet().add("小明","英文",88);
//        redisTemplate.opsForZSet().add("小明","数学",90);
        //HotScore:热度     skuId 15    88分
        String hotScore = "hotScore";
        //10.0
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore, skuId, 1);//参数3：追加的分数
        System.out.println("当前分数：" + score);
        //本次为了测试 10分更新一次索引库   10 20 30 40
        if (score % 10 == 0) {
            System.out.println("满10分了吗：" + score);
            //1:根据skuId 查询Goods数据
            Optional<Goods> optional = goodsDao.findById(skuId);
            Goods goods = optional.get();
            //2:更新操作（追加分）
            goods.setHotScore(Math.round(score));
            //3:更新索引库
            goodsDao.save(goods);
            //如果保存的Goods中的ID已经存在了 更新 如果不存在就是添加
            //更新底层 是： 先删除 再添加
        }
    }

    //    @Autowired
//    private ElasticsearchRestTemplate elasticsearchRestTemplate;//Spring Date ES 框架
    @Autowired
    private RestHighLevelClient restHighLevelClient;//ES 原生Api客户端

    //开始搜索
    @Override
    public SearchResponseVo list(SearchParam searchParam) {
        //1: 构建 SearchRequest 条件对象
        SearchRequest searchRequest = buildSearchRequest(searchParam);
        try {
            //2: 执行搜索
            SearchResponse searchResponse = restHighLevelClient
                    .search(searchRequest, RequestOptions.DEFAULT);
            //3: 解析结果
            SearchResponseVo searchResponseVo = parseSearchResponse(searchResponse);
            //1)总条数
            //2）当前页
            searchResponseVo.setPageNo(searchParam.getPageNo());
            //3)每页数
            searchResponseVo.setPageSize(searchParam.getPageSize());
            //4)总页数
            Long totalPages =  (searchResponseVo.getTotal() + searchParam.getPageSize() -1 )
                    /searchResponseVo.getPageSize();
            searchResponseVo.setTotalPages(totalPages);
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //解析搜索结果  4部分数据
    private SearchResponseVo parseSearchResponse(SearchResponse searchResponse) {
        SearchResponseVo vo = new SearchResponseVo();
        SearchHits hits = searchResponse.getHits();
        //1:总条数
        long totalHits = hits.getTotalHits();
        System.out.println("总条数：" + totalHits);
        vo.setTotal(totalHits);
        //2:商品集合
        SearchHit[] hits1 = hits.getHits();
        List<Goods> goodsList = Arrays.stream(hits1).map(h -> {
            String sourceAsString = h.getSourceAsString();
            System.out.println(sourceAsString);
            Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
            //上面的Goods对象中的名称是普通名称 （不高亮的名称）
            //如果有高亮的名称 要使用高亮的名称
            //如果没有高亮的名称 使用普通名称
            HighlightField title = h.getHighlightFields().get("title");
            if(null != title){
                String t = title.fragments()[0].toString();
                goods.setTitle(t);
            }
            return goods;
        }).collect(Collectors.toList());
        vo.setGoodsList(goodsList);
        //3: 构建条件的时候 设置了品牌分组  解析品牌分组结果
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) searchResponse.getAggregations().asMap().get("tmIdAgg");
        List<SearchResponseTmVo> responseTmVoList = tmIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseTmVo tmVo = new SearchResponseTmVo();
            //1)品牌的ID
            tmVo.setTmId(Long.parseLong(bucket.getKeyAsString()));//3
            //2)品牌的名称
            ParsedStringTerms tmNameAgg = bucket.getAggregations().get("tmNameAgg");
            tmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            //3)品牌的Logo图片
            ParsedStringTerms tmLogoUrlAgg = bucket.getAggregations().get("tmLogoUrlAgg");
            tmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return tmVo;
        }).collect(Collectors.toList());
        vo.setTrademarkList(responseTmVoList);

        //4:构建条件的时候 设置了平台属性分组 解析平台属性分组结果
        ParsedNested attrsAgg = (ParsedNested) searchResponse.getAggregations().asMap().get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> responseAttrVoList = attrIdAgg.getBuckets().stream().map(bucket -> {
            SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
            //1)平台属性ID
            attrVo.setAttrId(Long.parseLong(bucket.getKeyAsString()));
            //2)平台属性名称
            ParsedStringTerms attrNameAgg = bucket.getAggregations().get("attrNameAgg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //3)平台属性值名称
            ParsedStringTerms attrValueAgg = bucket.getAggregations().get("attrValueAgg");
            List<String> attrValueList = attrValueAgg.getBuckets().
                    stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrValueList(attrValueList);
            return attrVo;
        }).collect(Collectors.toList());
        vo.setAttrsList(responseAttrVoList);
        return vo;
    }

    //构建条件对象    入参：6部分数据
    private SearchRequest buildSearchRequest(SearchParam searchParam) {
        //构建条件资源对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //构建组合条件对象（为了保存多条件）
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //  select * from 表 where  (name like %大家好%) and (category1Id=35) and (category2Id=56) and ...
        //1:关键词  （分词） 模糊搜索字段 商品名称   必须有值不能为NULL
        //operator(Operator.AND)  决定了 分词之后查询之后 处理方式  OR  AND  默认是OR 可改成And
        //判断关键词是否有值
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",keyword).operator(Operator.AND));
        }else{
            //匹配所有
        }
        //searchSourceBuilder.query(QueryBuilders.matchAllQuery());//查询所有 没有条件
        //2:一二三级分类的ID
        Long category1Id = searchParam.getCategory1Id();
        if(null != category1Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id){
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",category3Id));
        }
        //3:品牌  3:小米   品牌ID：品牌名称
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            String[] t = StringUtils.split(trademark, ":");
            boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",t[0]));
        }
        //4:平台属性  平台属性ID：平台属性值名称：平台属性名称
        String[] props = searchParam.getProps();//长度是5
        if(null != props && props.length >0){
            //有平台属性
            for (String prop : props) {
                String[] p = prop.split(":");//长度是3
                //子组合条件对象
                BoolQueryBuilder subBoolQueryBuilder = QueryBuilders.boolQuery();
                //平台属性ID
                subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrId",p[0]));
                //平台属性值名称
                subBoolQueryBuilder.must(QueryBuilders.termQuery("attrs.attrValue",p[1]));
                //将子组合对象 设置给父组合对象
                //参数1：路径
                //参数2：设置的子组合条件对象
                //参数3：模式  本次的None  "attrs" : [对象（三个属性），2，3，4，5]
                boolQueryBuilder.filter(
                        QueryBuilders.nestedQuery("attrs",subBoolQueryBuilder, ScoreMode.None));

            }
        }
        //给条件资源对象设置组合条件对象
        searchSourceBuilder.query(boolQueryBuilder);
        //////////////////////////////////////////
        //5:排序  默认是按照综合排序 （热度） //1：desc  DESC Desc 或是 1:asc  或是2：desc ...
        String order = searchParam.getOrder();
        if(!StringUtils.isEmpty(order)){
            String[] o = order.split(":");
            //有排序的值
            String feildName = "";
            switch (o[0]){
                case "1":feildName="hotScore";break;
                case "2":feildName="price";break;
            }
            searchSourceBuilder.sort(feildName, o[1].equals("asc")?SortOrder.ASC : SortOrder.DESC);
        }else{
            //无排序的值  走默认
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }
        //6:分页（当前页、每页数） //当不设置分页时  默认是1页 默认显示10条数据
        Long pageNo = searchParam.getPageNo();
        Long pageSize = searchParam.getPageSize();
        searchSourceBuilder.from((pageNo.intValue()-1)*pageSize.intValue());  // select * from 表  limit 开始行,每页数
        searchSourceBuilder.size(pageSize.intValue());
        //7:隐藏条件 高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        //1)高亮的字段（域）
        highlightBuilder.field("title").preTags("<font color='red'>").postTags("</font>");
        //2)高亮的前缀 <font color='red'>手机</font>
        //3)高亮的后缀
        searchSourceBuilder.highlighter(highlightBuilder);
        //8:隐藏条件 分组
        // 品牌分组的设置
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                           .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                           .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));
        // 设置平台属性分组  select id as 别名 from 表 group by id,name,price
        //AggregationBuilders.nested() 参数1：嵌套的别名 参数2：嵌套的名称
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg","attrs")
                   .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                   .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                   .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //搜索请求对象
        SearchRequest searchRequest = new SearchRequest();
        //设置索引库的名称
        searchRequest.indices("goods");
        //设置索引库的类型 （可有可无）不影响搜索  （7.0版本以上取消了 type)
        searchRequest.types("info");
        //设置构建条件资源对象
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }
}
