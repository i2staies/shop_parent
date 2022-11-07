package com.atguigu.controller;

import com.atguigu.service.ConCurrentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/product")
@RestController
public class ConCurrentController {
    @Autowired
    private ConCurrentService conCurrentService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/setNum")
    public void setNum() {
        conCurrentService.setNum();
    }

}
