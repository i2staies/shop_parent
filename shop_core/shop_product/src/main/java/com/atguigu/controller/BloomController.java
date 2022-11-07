package com.atguigu.controller;

import com.atguigu.entity.SkuInfo;
import com.atguigu.service.ConCurrentService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/init/")
public class BloomController {
    @Autowired
    private ConCurrentService conCurrentService;
    @Autowired
    private SkuInfoService skuInfoService;
    @Autowired
    private RBloomFilter bloomFilter;

    //TODO 定时任务，同步数据库与布隆过滤器的内容
    @GetMapping("/sku/bloom")
    public String setNum(){
        //删除原有的布隆过滤器信息
        bloomFilter.delete();
        //初始化布隆过滤器的大小与容错
        bloomFilter.tryInit(10000, 0.001);

        //查询数据库中所有id
        List<SkuInfo> skuInfoList = skuInfoService.list(new QueryWrapper<SkuInfo>().select("id"));
        for (SkuInfo skuInfo : skuInfoList) {
            Long skuId = skuInfo.getId();
            //把id放入容器当中
            bloomFilter.add(skuId);
        }
        return "success";
    }
}
