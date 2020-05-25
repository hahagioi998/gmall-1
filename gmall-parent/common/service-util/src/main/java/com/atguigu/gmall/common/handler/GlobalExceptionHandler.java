package com.atguigu.gmall.common.handler;

import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 全局异常处理类
 *
 * @author qy
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody //此注解 直接把返回结果抛到页面上    //去掉此注解 ：可以跳转指定页面
    public Result error(Exception e){
        e.printStackTrace();//打印在控制台
        return Result.fail(); //将异常返回到页面上  抛异常到页面
        //企业做法： 跳转错误页面   服务器太忙  美工前端 美丽页面 提示
    }

    /**
     * 自定义异常处理方法
     * @param e
     * @return
     */
    @ExceptionHandler(GmallException.class)
    @ResponseBody
    public Result error(GmallException e){
        return Result.fail(e.getMessage());
    }
}
