package com.atguigu.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)     //作用范围
@Retention(RetentionPolicy.RUNTIME)      //生命周期
//source
//      表示注解只保留在源文件，当java文件编译成class文件，就会消失
//CLASS
//      注解被保留到class文件，但jvm加载class文件时候被遗弃，这是默认的生命周期
//RUNTIME
//      解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
public @interface ShopCache {

    //定义属性value
    String value() default "cache";

    //是否开启布隆过滤器
    boolean enableBloom() default true;
}
