package com.atguigu.gmall.all.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 远程调用 拦截器
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    //远程调用之前 对 远程调用的请求进行处理的方法  入参：RequestTemplate 就是远程调用请求对象
    @Override
    public void apply(RequestTemplate requestTemplate) {
        //将当前微服务中的请求 头中 真实用户的Id 临时用户的Id 保存到RequestTemplate
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        if(null != servletRequestAttributes){
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if(null != request){
                String userId = request.getHeader("userId");
                if(!StringUtils.isEmpty(userId)){
                    requestTemplate.header("userId",userId);
                }
                String userTempId = request.getHeader("userTempId");
                if(!StringUtils.isEmpty(userTempId)){
                    requestTemplate.header("userTempId",userTempId);
                }
            }
        }


    }
}
