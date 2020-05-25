package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.product.service.TestLockService;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
public class TestLockServiceImpl implements TestLockService {


    @Autowired
    private RedisTemplate redisTemplate;

    //采用本地锁   追加数量  执行下面的方法  5000次  高并发的时候
    // 上锁 本地锁
    @Override
    public synchronized void testLock() {

        Integer num = (Integer) redisTemplate.opsForValue().get("num");
        redisTemplate.opsForValue().set("num",++num);
    }

    @Autowired
    private RedissonClient  redissonClient;
    //读
    @Override
    public void testRead() {

        //大锁 （包含读与写的）大锁
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        RLock rLock = lock.readLock();
        rLock.lock(10, TimeUnit.SECONDS);
        System.out.println("上读锁成功 10秒后解锁");

    }

    //写
    @Override
    public void testWrite() {
        RReadWriteLock lock = redissonClient.getReadWriteLock("lock");
        RLock rLock = lock.writeLock();
        rLock.lock(10, TimeUnit.SECONDS);
        System.out.println("上写锁成功 10秒后解锁");
    }
}
