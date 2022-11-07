package com.atguigu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    /**
     * 如果服务器允许跨域，需要在返回的响应头中携带下面信息：
     * - Access-Control-Allow-Origin：可接受的域，是一个具体域名或者*（代表任意域名）
     * - Access-Control-Allow-Credentials：是否允许携带cookie，默认情况下，cors不会携带cookie，除非这个值是true
     * - Access-Control-Allow-Methods：允许访问的方式
     * - Access-Control-Allow-Headers：允许携带的头
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        // 创建这个对象CorsConfiguration
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*"); //允许所有的网络请求。cookie.setDomain("http://localhost")
        corsConfiguration.setAllowCredentials(true); //配置这个允许携带cookie。
        corsConfiguration.addAllowedMethod("*"); //允许所有的请求方法
        corsConfiguration.addAllowedHeader("*"); //允许请求头中携带信息
        // 创建对应的 CorsConfigurationSource
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        // 设置配置选项
        // 第一个参数path 过来拦截哪个路径
        // 第二个参数CorsConfiguration 制作一个跨域的规则配置
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(configurationSource);
    }
}
