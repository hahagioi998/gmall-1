package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 购物车管理
 */
@Service
public class CartServiceImpl implements CartService {


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Autowired
    private ProductFeignClient productFeignClient;

    //加入购物车
    @Override
    public CartInfo addToCart(Long skuId, Integer skuNum, String userId) {
        //缓存数据的Key值
        String cartKey = cacheCartKey(userId);
        //1:查询 Redis缓存  DB是存在永久
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForValue().get(cartKey);
        if(null == cartInfo){
            //条件： 当前用户ID  当前库存ID
            cartInfo = cartInfoMapper.selectOne(new QueryWrapper<CartInfo>()
                    .eq("user_id", userId).eq("sku_id", skuId));
        }
        // 2:保存或更新Mysql 判断此商品是否已经添加过购物车中
        if(null != cartInfo){
            //之前此商品 此用户已经加入过购物车中
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //实时更新价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            cartInfo.setSkuPrice(skuPrice);
            //选中
            cartInfo.setIsChecked(1);
            //更新DB
            cartInfoMapper.updateById(cartInfo);
        }else{
            //之前此商品 此用户未加入过购物车中
            cartInfo = new CartInfo();
            //购物车数据
            cartInfo.setSkuId(skuId);
            cartInfo.setIsChecked(1);
            cartInfo.setSkuNum(skuNum);
            //实时更新价格
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setUserId(userId);
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfoMapper.insert(cartInfo);
        }
        //3:保存Redis缓存
        redisTemplate.opsForValue().set(cartKey,cartInfo);
        setCartKeyExpire(cartKey);
        return null;
    }

    //设置购物车在缓存中的存活时间
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);//500分
    }

    //缓存的Key值  入参：用户ID   表明此购物车是哪个用户的
    private String cacheCartKey(String userId) {
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
