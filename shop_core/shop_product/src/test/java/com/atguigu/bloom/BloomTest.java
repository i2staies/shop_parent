package com.atguigu.bloom;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class BloomTest {
    @Autowired
    private RBloomFilter skuBloomFilter;

    @Test
    public void skuTest(){
        System.out.println(skuBloomFilter.contains(24L));
        System.out.println(skuBloomFilter.contains(55L));
    }
}
