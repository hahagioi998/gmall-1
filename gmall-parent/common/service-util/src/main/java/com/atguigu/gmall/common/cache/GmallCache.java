package com.atguigu.gmall.common.cache;

import java.lang.annotation.*;

/**
 * 缓存自定义注解
 */
/**
 *@Target：
 *
 * @Target说明了Annotation所修饰的对象范围：Annotation可被用于
 *      packages、types（类、接口、枚举、Annotation类型）、
 *      类型成员（方法、构造方法、成员变量、枚举值）、
 *      方法参数和本地变量（如循环变量、catch参数）。
 *      在Annotation类型的声明中使用了target可更加明晰其修饰的目标。
 *
 * 作用：用于描述注解的使用范围（即：被描述的注解可以用在什么地方
 *
 * CLASS  RUNTIME   SOURCE
 * 按生命周期来划分可分为3类：
 * 1、RetentionPolicy.SOURCE：注解只保留在源文件，当Java文件编译成class文件的时候，注解被遗弃；
 * 2、RetentionPolicy.CLASS：注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期；
 * 3、RetentionPolicy.RUNTIME：注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在；
 *
 *
 * @Documented注解标记的元素，Javadoc工具会将此注解标记元素的注解信息包含在javadoc中
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GmallCache {
    //前缀
    String prefix() default "cache";//默认是cache 进行设置替换成需求要使用的

}
