package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import org.apache.http.client.methods.HttpHead;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import javax.sound.midi.Soundbank;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLOutput;

/**
 * 全局过滤器
 * 统一鉴权
 *  SpringBoot 网关中  默认有9大过滤器
 *  + 当前自定义全局过滤器  == 共10大过滤器  执行顺序
 *
 *  优化级：
 *  大 -----------------------> 小
 *  最大负整数   -1  0 0  1 1   最大整数
 *
 */
///@Order(0)
@Component
public class LoginGlobalFilter implements GlobalFilter,Ordered{

    //判断路径是否符合规则
    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${auth.url}")
    private String[] authUrl;

    //过滤器执行的方法
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1:获取当前请求的
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String path = request.getURI().getPath();
        System.out.println("path:" + path);
        //1:内部资源不允许浏览器请求访问   /inner/**
        //参数1：路径规则  参数2：路径
        if(antPathMatcher.match("/inner/**",path)){
            //内部资源不允许  友情提示  { code:209,message:没有权限 }  json格式字符串
            return out(response,ResultCodeEnum.PERMISSION);
        }
        //2: 判断是否允许访问   登录还是未登录判断  必须登录才能访问的资源 /auth/**
        //获取用户ID
        String userId = getUserId(request);
        if(antPathMatcher.match("/auth/**",path) && StringUtils.isEmpty(userId)){
            //必须登录 才能访问  友情提示  { code:208,message:未登录 }  json格式字符串
            return out(response,ResultCodeEnum.LOGIN_AUTH);
        }
        //3:判断是否为刷新页面 （重定向到登录页面去）
        // list.html {skuId}.html cart.html      trade.html myOrder.html pay.html
        for (String url : authUrl) {
            if(path.contains(url) && StringUtils.isEmpty(userId)){
                //重定向
                response.setStatusCode(HttpStatus.SEE_OTHER);//重定向状态码
                String rawSchemeSpecificPart = request.getURI().getRawSchemeSpecificPart();
                System.out.println("url:" + rawSchemeSpecificPart);
                try {
                    response.getHeaders().set(HttpHeaders.LOCATION,
                            "http://passport.gmall.com/login.html?originUrl=" +
                                    URLEncoder.encode(rawSchemeSpecificPart,"utf-8"));
                    return response.setComplete();//响应浏览器
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        //资源可以直接访问 不需要登录  或 资源不允许访问 但是已经登录了
        //将用户ID传递给后面的微服务  mutate() 创建一个新的请求 并设置用户ID
        if(!StringUtils.isEmpty(userId)){
            request.mutate().header("userId",userId);
        }
        //临时用户ID的传递 36位长字符串
        String userTempId = getUserTempId(request);
        if(!StringUtils.isEmpty(userTempId)){
            request.mutate().header("userTempId",userTempId);
        }
        //放行
        return chain.filter(exchange);//最后路由转发微服务的时候 后面的微服务能不能获取到用户的ID呢
    }

    //返回值
    public Mono<Void> out(ServerHttpResponse response,ResultCodeEnum resultCodeEnum){
        String result = JSONObject.toJSONString(Result.build(null, resultCodeEnum));
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer dataBuffer = dataBufferFactory.wrap(result.getBytes());
        //编码是否需要设置  如果不设置编码  乱码   响应体内容  默认是GBK或IOS8859-1 决对不是UTF-8
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE,"application/json;charset=utf-8");
        return response.writeWith(Mono.just(dataBuffer));
    }

    //获取临时用户ID
    public String getUserTempId(ServerHttpRequest request){
        //1:从请求头中
        String userTempId = request.getHeaders().getFirst("userTempId");
        if(StringUtils.isEmpty(userTempId)){
            //2：从Cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("userTempId");
            if(null != httpCookie){
                userTempId = httpCookie.getValue();
            }
        }
        return userTempId;
    }

    //获取用户ID
    private String getUserId(ServerHttpRequest request) {
        //1:获取令牌  1） 请求头中  2） Cookie中
        String token = request.getHeaders().getFirst("token");
        if(StringUtils.isEmpty(token)){
            //从Cookie中获取
            HttpCookie httpCookie = request.getCookies().getFirst("token");
            if(null != httpCookie){
                token = httpCookie.getValue();
            }
        }
        //判断令牌是否已经获取到
        if(!StringUtils.isEmpty(token)){
            //将令牌换成用户的ID
            if(redisTemplate.hasKey("user:login:" + token)){
                return (String) redisTemplate.opsForValue().get("user:login:" + token);
            }
        }
        return null;
    }


    //过滤器执行的顺序
    @Override
    public int getOrder() {
        return 0;
    }
}
