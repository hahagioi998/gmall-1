package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录管理
 */
@RestController
@RequestMapping("/api/user/passport")
public class LoginApiController {

    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisTemplate redisTemplate;

    //提交订单 （异步）
    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo){
        //1:判断用户名不能为空
        //2:判断密码不能为空
        //3:判断用户名密码是否正确  连接DB  返回值要求必须有昵称
        UserInfo userInfo1 = loginService.login(userInfo);
        if(null == userInfo1){
            return Result.fail().message("此用户名或密码不正确");
        }else{

            //判断用户名或密码成功之后
            //生成令牌  将来是要保存在Cookie中的
            String token = UUID.randomUUID().toString().replaceAll("-","");
            //保存令牌（Redis)  V值 保存成String 类型 ，方便以后读取
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,
                    userInfo1.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //返回成功 200  信息是令牌
            Map map = new HashMap<>();
            map.put("token",token);
            map.put("nickName",userInfo1.getNickName());
            return Result.ok(map);
        }
    }
}
