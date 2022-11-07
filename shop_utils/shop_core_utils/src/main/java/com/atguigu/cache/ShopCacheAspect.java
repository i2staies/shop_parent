package com.atguigu.cache;

import com.atguigu.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class ShopCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private RBloomFilter bloomFilter;

    //@Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice01(ProceedingJoinPoint target) throws Throwable {

        //1.0 需要获取到目标方法中的参数
        Object[] methodParams = target.getArgs();

        //1.1 拿到目标方法
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();

        //拼接缓存key的名称
        Object skuid = methodParams[0];

        //1.2 拿到注解上面的参数
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.value();

        //缓存名称
        String cacheKey = prefix + ":" + skuid + RedisConst.SKUKEY_SUFFIX;
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        //判断redis中是否存在缓存，确定是否需要上锁
        if (cacheObject == null) {
            String lockKey = "lock-" + skuid;
            RLock lock = redissonClient.getLock(lockKey);
            //第二个作用是判断是否需要执行目标方法
            if (cacheObject == null) {
                try {
                    //加锁，因为现在需要查询数据库，防止缓存击穿，分布式锁
                    //默认看门狗机制，30秒
                    lock.lock();
                    boolean contains = bloomFilter.contains(skuid);
                    if (contains) {
                        //执行目标方法
                        Object objectDb = target.proceed();
                        //把数据放入缓存
                        redisTemplate.opsForValue().set(cacheKey, objectDb, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return objectDb;
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
        return cacheObject;
    }

    //本地锁 + 判断是否布隆（写数据库）
    //@Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice02(ProceedingJoinPoint target) throws Throwable {

        //1.0 需要获取到目标方法中的参数
        Object[] methodParams = target.getArgs();

        //1.1 拿到目标方法
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();

        //拼接缓存key的名称
        Object skuid = methodParams[0];

        //1.2 拿到注解上面的参数
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.value();
        boolean enableBloom = shopCache.enableBloom();

        //缓存名称
        String cacheKey = prefix + ":" + skuid + RedisConst.SKUKEY_SUFFIX;
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        //判断redis中是否存在缓存，确定是否需要上锁
        if (cacheObject == null) {
            String lockKey = "lock-" + skuid;
            //加锁对象为lockKey
            synchronized (lockKey.intern()) {
                Object objectDb = null;
                //第二个作用是判断是否需要执行目标方法
                if (cacheObject == null) {
                    if (enableBloom) {
                        boolean contains = bloomFilter.contains(skuid);
                        if (contains) {
                            //执行目标方法
                            objectDb = target.proceed();

                        }
                    }
                } else {
                    objectDb = target.proceed();
                }
                //把数据放入缓存
                redisTemplate.opsForValue().set(cacheKey, objectDb, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                return objectDb;
            }
        }
        return cacheObject;
    }

    //读写锁(会出现释放其他锁现象，以本地锁为主)
    //@Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice03(ProceedingJoinPoint target) throws Throwable {

        //1.0 需要获取到目标方法中的参数
        Object[] methodParams = target.getArgs();

        //1.1 拿到目标方法
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();

        //拼接缓存key的名称
        Object skuid = methodParams[0];

        //1.2 拿到注解上面的参数
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.value();

        //缓存名称
        String cacheKey = prefix + ":" + skuid + RedisConst.SKUKEY_SUFFIX;
        String lockKey = "lock-" + skuid;
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(lockKey);
        Object cacheObject;
        //拿读锁
        try {
            try {
                readWriteLock.readLock().lock();
                cacheObject = redisTemplate.opsForValue().get(cacheKey);
            } finally {
                readWriteLock.readLock().unlock();
            }
            if (cacheObject == null) {
                readWriteLock.writeLock().lock();
                boolean contains = bloomFilter.contains(skuid);
                if (contains) {
                    //执行目标方法
                    Object objectDb = target.proceed();
                    //把数据放入缓存
                    redisTemplate.opsForValue().set(cacheKey, objectDb, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return objectDb;
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
        return cacheObject;
    }

    //省略了布隆过滤器
    @Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint target) throws Throwable {

        //1.0 需要获取到目标方法中的参数
        Object[] methodParams = target.getArgs();

        //1.1 拿到目标方法
        MethodSignature signature = (MethodSignature) target.getSignature();
        Method method = signature.getMethod();

        //拼接缓存key的名称
        Object skuid = methodParams[0];

        //1.2 拿到注解上面的参数
        ShopCache shopCache = method.getAnnotation(ShopCache.class);
        String prefix = shopCache.value();
        boolean enableBloom = shopCache.enableBloom();

        //缓存名称
        String cacheKey = prefix + ":" + skuid + RedisConst.SKUKEY_SUFFIX;
        Object cacheObject = redisTemplate.opsForValue().get(cacheKey);
        //判断redis中是否存在缓存，确定是否需要上锁
        if (cacheObject == null) {
            String lockKey = "lock-" + skuid;
            //加锁对象为lockKey
            synchronized (lockKey.intern()) {
                //第二个作用是判断是否需要执行目标方法
                if (cacheObject == null) {
                    Object objectDb = target.proceed();
                    //把数据放入缓存
                    redisTemplate.opsForValue().set(cacheKey, objectDb, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                    return objectDb;
                }
            }
        }
        return cacheObject;
    }
}
