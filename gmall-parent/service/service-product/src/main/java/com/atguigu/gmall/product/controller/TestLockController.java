package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.service.TestLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试
 * 本地锁
 * 分布式锁
 */
@RestController
@RequestMapping("admin/product/test")
public class TestLockController {

    @Autowired
    private TestLockService testLockService;

    //测试
    @GetMapping("/testLock")
    public Result testLock(){

        testLockService.testLock();

        return Result.ok();
    }
    //测试 上读锁
    @GetMapping("/testRead")
    public Result testRead(){
        testLockService.testRead();

        return Result.ok("上读锁成功");
    }

    //测试上写锁
    @GetMapping("/testWrite")
    public Result testWrite(){
        testLockService.testWrite();
        return Result.ok("上写锁成功");
    }
}
