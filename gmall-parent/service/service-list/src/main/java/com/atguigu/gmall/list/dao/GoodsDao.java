package com.atguigu.gmall.list.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 添加
 * 删除
 * 更新
 * Mybaits-Plus相似
 */
public interface GoodsDao extends ElasticsearchRepository<Goods,Long> {
}
