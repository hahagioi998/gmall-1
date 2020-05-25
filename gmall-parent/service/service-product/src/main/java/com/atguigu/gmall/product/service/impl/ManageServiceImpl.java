package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import net.bytebuddy.asm.Advice;
import org.json.JSONObject;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.misc.Hash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
/*

后台管理中心服务层
 */

@SuppressWarnings("all")
@Service
public class ManageServiceImpl implements ManageService {


    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    //查询一级分类集合

    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    //2:获取二级分类
    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        return baseCategory2Mapper.selectList(new QueryWrapper<BaseCategory2>()
                .eq("category1_id", category1Id));
    }

    //获取三级分类
    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>()
                .eq("category2_id", category2Id));
    }

    //根据一二三级分类 查询平台属性集合
    @Override
    public List<BaseAttrInfo> attrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        //平台属性 及平台属性值  关联查询  Mybatis-Plus 只能单表操作  无能为力  手写Sql语句是无敌
        return baseAttrInfoMapper.attrInfoList(category1Id, category2Id, category3Id);
    }

    //保存平台属性
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //1:平台属性表
        baseAttrInfoMapper.insert(baseAttrInfo);
        //2:平台属性值表
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        attrValueList.forEach(attrValue -> {
            //外键
            attrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(attrValue);
        });

    }

    //SPu分页列表查询
    @Override
    public IPage<SpuInfo> spuPage(Integer page, Integer limit, Long category3Id) {
        IPage<SpuInfo> p = spuInfoMapper.selectPage
                (new Page(page, limit),
                        new QueryWrapper<SpuInfo>().eq("category3_id", category3Id));
        return p;
    }


    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;

    @Override
    public List<BaseTrademark> getTrademarkList() {
        return baseTrademarkMapper.selectList(null);
    }


    //查询销售属性集合
    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    //保存SPU
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        //1:商品表 spu_info
        spuInfoMapper.insert(spuInfo);
        //2:图片表 spu_image
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        spuImageList.forEach(spuImage -> {
            //外键
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        });

        //3:商品销售属性  spu_sale_attr
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        spuSaleAttrList.forEach(spuSaleAttr -> {
            // 1:选择颜色
            //2:选择版本
            //3:选择套装
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            //对应多个销售属性值
            //4:商品的销售属性值 spu_sale_attr_value
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                //外键
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                //销售属性名称  （冗余
                spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            });
        });


    }

    //查询图片集合 根据SpuId
    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id", spuId));
    }

    //根据SpuId查询销售属性及属性值
    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {
        //手写Sql语句
        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }


    //保存SKu
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        //默认未审核
        skuInfo.setIsSale(0);
        //1:库存表 sku_info
        skuInfoMapper.insert(skuInfo);//返回值  skuId
        //2:库存图片表 sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(skuImage -> {
            //外键
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        });
        //3:库存与平台属性关联表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        skuAttrValueList.forEach(skuAttrValue -> {
            //外键
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        });
        //4:库存与销售属性值关联表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        skuSaleAttrValueList.forEach(skuSaleAttrValue -> {
            //外键 skuID
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            //外键 spuId
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            //保存
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        });
    }

    //查询SKU分页列表
    @Override
    public IPage<SkuInfo> skuList(Integer page, Integer limit) {
        return skuInfoMapper.selectPage(new Page<SkuInfo>(page, limit)
                , null);
    }

    //上架
    @Override
    public void onSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    //下架
    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;


    //根据SKuID查询SkuINFO信息  从DB查询
    public SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        //查询图片
        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
        skuInfo.setSkuImageList(skuImageList);
        return skuInfo;
    }


    //根据SKUID查询SKUINFO信息   本次将使用Redisson来完成分布式锁的实现
    public SkuInfo getSkuInfoRedisson(Long skuId) {
        String cacheKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
        //1:优先从缓存中获取数据
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        if (null != skuInfo) {
            //2:有 直接返回
            return skuInfo;
        } else {
            //3: 获取上锁对象
            RLock lock = redissonClient.getLock(lockKey);
            //4:上锁   缓存击穿问题
            //lock.lock(10, TimeUnit.SECONDS);//可重入锁   如果未获取锁  处于阻塞状态 一直等待 直到获取到锁
            try {
                //参数1：尝试获取锁的时间 1s
                //参数2：设置获取到锁的过期时间
                boolean res = lock.tryLock(1, 3, TimeUnit.SECONDS);
                if (res) {
                    //我是第一人
                    //5:查询DB
                    skuInfo = skuInfoMapper.selectById(skuId);
                    //6:缓存穿透
                    if (null == skuInfo) {
                        skuInfo = new SkuInfo();
                        redisTemplate.opsForValue().set(cacheKey, skuInfo, 5, TimeUnit.MINUTES);
                    } else {
                        //查询图片
                        List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                        skuInfo.setSkuImageList(skuImageList);
                        //随机数 解决缓存雪崩问题
                        //缓存一天
                        redisTemplate.opsForValue().set(cacheKey, skuInfo,
                                RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    }
                } else {
                    //我不是第一人   获取缓存中数据
                    Thread.sleep(1000);
                    return (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                //手动解锁
                lock.unlock();
            }
        }
        //直接返回
        return skuInfo;
    }


    //根据SkuID查询SkuInfo信息  Redis 上锁
    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        String cacheKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
        //1:优先从缓存中获取数据
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        if (null != skuInfo) {
            //2:有 直接返回
            return skuInfo;
        } else {
            String uuid = UUID.randomUUID().toString();
            //100 万请求  上锁  setnx  返回值 1  true  返回值 0  false
            Boolean isLock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 1, TimeUnit.SECONDS);
            if (isLock) {

                //上锁  100万人当中有一个已经来了  当前就是这个人
                //3:没有 查询Mysql
                skuInfo = skuInfoMapper.selectById(skuId);
                //如果是缓存穿透的话  Mysql中数据也是不存在的
                if (null == skuInfo) {
                    skuInfo = new SkuInfo();// 空结果
                    //解决： 将空结果保存在缓存中
                    redisTemplate.opsForValue().set(cacheKey, skuInfo, 5, TimeUnit.MINUTES);
                } else {
                    //查询图片
                    List<SkuImage> skuImageList = skuImageMapper.selectList(new QueryWrapper<SkuImage>().eq("sku_id", skuId));
                    skuInfo.setSkuImageList(skuImageList);
                    //保存缓存一份   并且设置时间为 1天 或 24小时
                    //缓存的雪崩问题及解决方案
//                Random random = new Random();
//                int i = random.nextInt(3000);
                    redisTemplate.opsForValue().set(cacheKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                }
                //防误删
//                    String u = (String) redisTemplate.opsForValue().get(lockKey);
//                    if(!StringUtils.isEmpty(u) && uuid.equals(u)){
//                        //自己的锁
//                        redisTemplate.delete(lockKey);
//                    }
                //删除之原子性操作
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return tostring(redis.call('del',KEYS[1])) else return 0 end";
                this.redisTemplate.execute(new DefaultRedisScript<>(script), Collections.singletonList(lockKey), uuid);
            } else {
                //别人已经上锁了 我们不能上锁了
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //重新加载当前方法
                return getSkuInfo(skuId);
            }
        }
        //直接返回
        return skuInfo;
    }

    //根据三级分类ID查询一二三级分类
    @Override
    @GmallCache(prefix = "getCategoryView")
    public BaseCategoryView getCategoryView(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    //根据skuId查询库存表中的价格
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (null != skuInfo) {
            return skuInfo.getPrice();
        }
        return null;
    }

    //根据spuId查询销售属性及属性值 集合
    //      根据skuId查询当前选中项
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
    }

    //5:根据spuId查询销售组合与SkuId之间的对应   开发工程师  开荒  从无到有
    // 当你要查询的数据 没有对应的POJO或JavaBean对象  使用Map  无敌版JavaBean
    @Override
    public Map getSkuValueIdsMap(Long spuId) {
        Map result = new HashMap();
        List<Map> skuValueIdsMap = skuSaleAttrValueMapper.getSkuValueIdsMap(spuId);
        for (Map map : skuValueIdsMap) {
            result.put(map.get("value_ids"), map.get("sku_id"));
        }
        return result;
    }

    //查询基本分类视图 所有集合
    @Override
    public List<Map> getBaseCategoryList() {
        //准备集合
        List<Map> result = new ArrayList<>();
        //一二三级分类
        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);

        //1:对一级分类ID进行分组  Map k id : v group

//        baseCategoryViews.stream().collect(
//                Collectors.groupingBy((baseCategoryView) -> { return baseCategoryView.getCategory1Id();}));
        // K 一级分类ID  V：集合就是group
        // K  1          V: 60长度集合
        // K  2          V: 25长度集合
        // K  17         V: 对应的集合
        Map<Long, List<BaseCategoryView>> category1IdMap = baseCategoryViews.stream().collect(
                Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        //角标
        int index = 0;
        //遍历
        for (Map.Entry<Long, List<BaseCategoryView>> category1IdEntry : category1IdMap.entrySet()) {
            Map map1 = new HashMap();
            //1:一级分类ID
            map1.put("categoryId",category1IdEntry.getKey());
            //2: index
            map1.put("index",++index);
            //3:一级分类的名称
            map1.put("categoryName",category1IdEntry.getValue().get(0).getCategory1Name());
            //4:一级分类之子分类集合（二级分类）
            Map<Long, List<BaseCategoryView>> category2IdMap = category1IdEntry.getValue().stream().collect(
                    Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            List<Map> category2IdResult = new ArrayList<>();
            for (Map.Entry<Long, List<BaseCategoryView>> category2IdEntry : category2IdMap.entrySet()) {
                Map map2 = new HashMap();
                //1) 二级分类的ID
                map2.put("categoryId",category2IdEntry.getKey());
                //2)二级分类的名称
                map2.put("categoryName",category2IdEntry.getValue().get(0).getCategory2Name());
                //3)二级分类的子分类（三级分类集合）
                List<BaseCategoryView> category3IdList = category2IdEntry.getValue();
                List<Map> category3IdMap = new ArrayList<>();
                for (BaseCategoryView baseCategoryView : category3IdList) {
                    Map map3 = new HashMap();
                    //1)) 三级分类Id
                    map3.put("categoryId",baseCategoryView.getCategory3Id());
                    //2)) 三级分类名称
                    map3.put("categoryName",baseCategoryView.getCategory3Name());
                    category3IdMap.add(map3);
                }
                map2.put("categoryChild",category3IdMap);
                category2IdResult.add(map2);
            }
            map1.put("categoryChild",category2IdResult);
            result.add(map1);
        }
        return result;
    }
    //根本品牌ID 查询一个品牌
    @Override
    public BaseTrademark getBaseTrademark(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }
    //根据SkuId查询平台属性id/名称 平台属性值的名称
    @Override
    public List<SkuAttrValue> getAttrList(Long skuId) {
        return skuAttrValueMapper.getAttrList(skuId);
    }

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

}
