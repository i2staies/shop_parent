package com.atguigu.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        //单点redis
        config.useSingleServer().setAddress("redis://192.168.88.131:6389");
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    };


}
