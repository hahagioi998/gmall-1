package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.constant.RedisConst;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 自定义切面  目的：为了实现自定义缓存注解
 */
@Component
@Aspect
@Slf4j  //企业上班的时候  如果企业让打印日志 保存下来 控制台打印 将日志保存成文件
public class GmallCacheAspect {


    @Autowired
    private RedisTemplate redisTemplate;//查询数据
    @Autowired
    private RedissonClient redissonClient;//上锁

    //切面中方法执行  方法前后 环绕式  @GmallCache 的方法   入参：切入点
    @Around(value = "@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object gmallCacheMethod(ProceedingJoinPoint proceedingJoinPoint) {

        //切入点：获取入参的
        Object[] args = proceedingJoinPoint.getArgs();
        //切入点：获取返回值类型
        MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        Class returnType = methodSignature.getReturnType();
        //切入点：获取注解中的前缀
        String prefix = methodSignature.getMethod().getAnnotation(GmallCache.class).prefix();
        //缓存数据的Key值  getSkuInfo或是其它的（前缀是变量）:[1,2,3]   9999999
        String cacheKey = prefix + ":" + Arrays.asList(args).toString();
        //1:获取缓存中数据
        Object o = redisTemplate.opsForValue().get(cacheKey);
        if (null != o) {
            //2:有 直接返回
            log.info("缓存中有数据、无需在查询DB");
            return o;
        }
        //上锁
        String cacheKeyLock = cacheKey + RedisConst.SKULOCK_SUFFIX;
        RLock lock = redissonClient.getLock(cacheKeyLock);
        //解决缓存击穿
        try {
            boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1,
                    RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
            //判断是否获取锁
            if (res) {
                //上锁成功
                log.info("上锁成功");
                //3:没有 查询DB
                Object result = proceedingJoinPoint.proceed(args);
                //缓存穿透 人为原因
                if (null == result) {
                    //空结果  当前方法的返回值的空结果
                    result = returnType.newInstance();//返回值必须实现序列化接口
                    //4:保存缓存中一份
                    redisTemplate.opsForValue().set(cacheKey, result, 5, TimeUnit.MINUTES);

                } else {
                    //随机数 防止雪崩
                    //4:保存缓存中一份
                    redisTemplate.opsForValue().set(cacheKey, result,
                            RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                }
                //5:返回
                return result;
            } else {
                log.info("上锁失败、已经有人获取到了此锁");
                Thread.sleep(1000);
                return redisTemplate.opsForValue().get(cacheKey);
            }
        } catch (Throwable e) {
            // e.printStackTrace();
            log.error("获取锁抛出异常:{},请管理员尽快处理！", e.getMessage());
        } finally {
            log.info("开始解锁");
            lock.unlock();
        }
        //注意事项：以上5步中 存在缓存的穿透 雪崩 【击穿 （上锁）】
        return null;
    }


}
