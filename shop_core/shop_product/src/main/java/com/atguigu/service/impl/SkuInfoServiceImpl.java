package com.atguigu.service.impl;

import com.atguigu.cache.ShopCache;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.entity.SkuPlatformPropertyValue;
import com.atguigu.entity.SkuSalePropertyValue;
import com.atguigu.exception.SleepUtils;
import com.atguigu.mapper.SkuInfoMapper;
import com.atguigu.service.SkuImageService;
import com.atguigu.service.SkuInfoService;
import com.atguigu.service.SkuPlatformPropertyValueService;
import com.atguigu.service.SkuSalePropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 库存单元表 服务实现类
 * </p>
 *
 * @author zhangqiang
 * @since 2022-11-01
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Autowired
    private SkuPlatformPropertyValueService skuPlatformPropertyValueService;
    @Autowired
    private SkuSalePropertyValueService skuSalePropertyValueService;
    @Autowired
    private SkuImageService skuImageService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        //保存sku的基本信息
        baseMapper.insert(skuInfo);
        //保存sku的平台属性
        List<SkuPlatformPropertyValue> skuPlatformPropertyValueList = skuInfo.getSkuPlatformPropertyValueList();
        if (!CollectionUtils.isEmpty(skuPlatformPropertyValueList)) {
            for (SkuPlatformPropertyValue skuPlatformPropertyValue : skuPlatformPropertyValueList) {
                skuPlatformPropertyValue.setSkuId(skuInfo.getId());
            }
        }
        skuPlatformPropertyValueService.saveBatch(skuPlatformPropertyValueList);

        //保存sku的销售属性
        List<SkuSalePropertyValue> skuSalePropertyValueList = skuInfo.getSkuSalePropertyValueList();
        if (!CollectionUtils.isEmpty(skuPlatformPropertyValueList)) {
            for (SkuSalePropertyValue skuSalePropertyValue : skuInfo.getSkuSalePropertyValueList()) {
                skuSalePropertyValue.setSkuId(skuInfo.getId());
                skuSalePropertyValue.setProductId(skuInfo.getProductId());
            }
        }
        skuSalePropertyValueService.saveBatch(skuSalePropertyValueList);
        //保存sku的图片信息
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuPlatformPropertyValueList)) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
            }
        }
    }

    public SkuInfo getSkuInfoFromDb(Long skuid) {
        //1.查询商品的基本信息
        SkuInfo skuInfo = baseMapper.selectById(skuid);
        //1.查询商品的图片信息
        if (skuInfo != null) {
            QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("sku_id", skuid);
            List<SkuImage> list = skuImageService.list(queryWrapper);
            skuInfo.setSkuImageList(list);
        }
        return skuInfo;
    }

    private SkuInfo getSkuInfoFromRedis(Long skuId) {
        //sku:24:info
        String cacheKey= RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfoRedis = (SkuInfo)redisTemplate.opsForValue().get(cacheKey);
        //如果为空
        if(skuInfoRedis==null){
            SkuInfo skuInfoDb = getSkuInfoFromDb(skuId);
            //把数据放入缓存
            redisTemplate.opsForValue().set(cacheKey,skuInfoDb,RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
            return skuInfoDb;
        }
        return skuInfoRedis;
    }


    Map<Thread, String> threadMap = new HashMap<>();


    //TODO: 锁的力度太大
    private SkuInfo getSkuInfoFromRedisWithThreadLocal(Long skuid) {
        //设置redis的（key，value）编码格式，防止存入redis中乱码
//        redisTemplate.setKeySerializer(new StringRedisSerializer());//utf-8
//        redisTemplate.setValueSerializer(new StringRedisSerializer());//utf-8

        String CacheKey = RedisConst.SKUKEY_PREFIX + skuid + RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfoRedis = (SkuInfo) redisTemplate.opsForValue().get(CacheKey);
        String lockKey="lock-"+skuid;

        if (skuInfoRedis == null) {
            //分布式锁优化--具备可重入性
            String token = threadMap.get(Thread.currentThread());
            Boolean accquireLock = false;
            if (token != null) {
                //已经拿到锁了，不要再去拿锁了
                accquireLock = true;
            } else {
                token = UUID.randomUUID().toString();
                //利用redis的setnx命令
                accquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
            }
            if (accquireLock) {
                //如果缓存中有值，就加1
                SkuInfo skuInfoDb = getSkuInfoFromDb(skuid);
//        把数据放入缓存
                redisTemplate.opsForValue().set(CacheKey, skuInfoDb, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);

                //判断和删除这两句话要具备原子性
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                //把脚本放到redisScript中
                redisScript.setScriptText(luaScript);
                //设置脚本的返回值类型
                redisScript.setResultType(Long.class);
                redisTemplate.execute(redisScript, Arrays.asList(lockKey), token);
                //删除redis线程
                threadMap.remove(Thread.currentThread());
                return skuInfoDb;
            } else {
                //递归的目的是为了拿锁，不是为了递归
                //自旋
                while (true) {
                    SleepUtils.millis(50);
                    Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockKey, token, 3, TimeUnit.SECONDS);
                    if (flag) {
                        threadMap.put(Thread.currentThread(), token);
                        //拿到锁了，结束自旋
                        break;
                    }
                }
                return getSkuInfoFromRedisWithThreadLocal(skuid);
            }
        }
        return skuInfoRedis;
    }

    @Override
    @ShopCache("skuInfo")
    public SkuInfo getSkuInfo(Long skuid) {
        SkuInfo skuInfo = getSkuInfoFromDb(skuid);
        //查redis
//        SkuInfo skuInfo = getSkuInfoFromRedisWithThreadLocal(skuid);
//        SkuInfo skuInfo = getSkuInfoFromRedisson(skuid);
        return skuInfo;
    }

    private SkuInfo getSkuInfoFromRedisson(Long skuid) {
        String lockKey = "lock-"+skuid;
        String cacheKey = RedisConst.SKUKEY_PREFIX+skuid+RedisConst.SKUKEY_SUFFIX;
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(cacheKey);
        RLock lock = redissonClient.getLock(lockKey);
        //如果为空
        if(skuInfo == null){
            SkuInfo skuInfoFromDb;
            try {
                //加锁，因为现在需要查询数据库，防止缓存击穿，分布式锁
                //默认看门狗机制，30秒
                lock.lock();
                boolean contains = bloomFilter.contains(skuid);
                if(contains){
                    skuInfoFromDb = getSkuInfoFromDb(skuid);
                    //把数据放入缓存
                    redisTemplate.opsForValue().set(cacheKey,skuInfoFromDb,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                    return skuInfoFromDb;
                }
            } finally {
                lock.unlock();
            }
        }
        return skuInfo;
    }


}
