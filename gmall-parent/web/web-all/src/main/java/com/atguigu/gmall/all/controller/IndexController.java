package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * 首页 渲染 展示
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    //进入首页  http://www.gmall.com   http://www.jd.com www.taobao.com
//    @GetMapping("/")
//    public String index(Model model){
//        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
//        model.addAttribute("list",baseCategoryList);
//        return "index/index";
//    }  springmvc 视图解析器

    @Autowired
    private TemplateEngine templateEngine;//模板引擎    对模板进行渲染

    //1：生成静态化页面  到指定位置   提前准备
    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml(){
        //重点：静态化程序
        //1) 模板 就是页面   index/index
        //2) 准备数据
        Context context = new Context();
        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
        context.setVariable("list",baseCategoryList);
        //输出流  乱码   绝对路径  不是相对路径
        Writer out = null;
        try {
            String templates = ClassUtils.getDefaultClassLoader().getResource("templates").getPath();
            System.out.println(templates + "/index.html");
            //写
            out = new PrintWriter(templates + "/index.html","utf-8");
//            out = new PrintWriter("D:\\temp\\index.html","utf-8");
//            out = new OutputStreamWriter(new FileOutputStream("D:\\temp\\index.html"),
//                    Charset.forName("utf-8"));
            //3)由静态化程序  将上面数据与模板进行渲染
            // Thymeleaf 就是静态化技术   页面标签  th:text th:each
            //Thymeleaf : 在后端 静态化技术  +  前端 页面标签
            //参数1 ： 模板路径及模板名称
            //参数2：数据
            //参数3：输出流 （将页面通过输出流输出到指定的位置）  读取的位置
            templateEngine.process("index/index",context,out);
        } catch (IOException e) {
            //e.printStackTrace();
        }finally {
            try {
                if(null != out){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Result.ok();
    }

    //2:直接访问此页面  进入首页
    @GetMapping("/")
    public String index(){
        return "index";
    }


}
