package com.atguigu.service.impl;

import com.atguigu.exception.SleepUtils;
import com.atguigu.service.ConCurrentService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ConCurrentServiceImpl implements ConCurrentService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    //@Override
    //存在问题，发送1000次请求，num值少于1000
    public String getNumOne() {
        //如果缓存中有值，就加1
        String value = String.valueOf(redisTemplate.opsForValue().get("num"));
        //如果缓存中没有值，就设置为1
        if(value == "null"){
            redisTemplate.opsForValue().set("num", 1);
        }else{
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num", ++num);
        }
        return "success";
    }

    //@Override
    //解决方案1，加锁synchronized
    //存在问题：多台微服务导致不能上锁，
    public synchronized String getNumTwo() {
        //如果缓存中有值，就加1
        String value = String.valueOf(redisTemplate.opsForValue().get("num"));
        //如果缓存中没有值，就设置为1
        if(value == "null"){
            redisTemplate.opsForValue().set("num", 1);
        }else{
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num", ++num);
        }
        return "success";
    }

    public void doBusiness(){
        //如果缓存中有值，就加1
        String value = String.valueOf(redisTemplate.opsForValue().get("num"));
        //如果缓存中没有值，就设置为1
        if(value == "null"){
            redisTemplate.opsForValue().set("num", 1);
        }else{
            int num = Integer.parseInt(value);
            redisTemplate.opsForValue().set("num", ++num);
        }
    }

    //解决办法：
    //      1.基于数据库实现分布式锁
    //          性能低，还可能出现锁表问题
    //      2.基于zookeeper（可靠性）
    //      3.基于redis（重点）
    //      4.基于redission（重点）

    //分布式锁案例1
    //@Override
    public void setNumOne() {
        //利用redis的setnx命令
        Boolean accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", "ok");
        if(accquireLock){
            //拿到了锁，就可以执行业务,如果doBusiness报错，可能导致锁一直占用，无法释放，给一个过期时间
            doBusiness();
            //昨完业务之后需要删除锁
            redisTemplate.delete("lock");
        }else{
            //如果没有拿到锁，递归。
            setNum();
        }
    }

    //分布式锁案例2
    //@Override
    public void setNumTwo() {
        //利用redis的setnx命令
        Boolean accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", "ok",3, TimeUnit.SECONDS);
        if(accquireLock){
            //拿到了锁，就可以执行业务,如果doBusiness报错，可能导致锁一直占用，无法释放，给一个过期时间
            //如果缓存中有值，就加1
            String value = String.valueOf(redisTemplate.opsForValue().get("num"));
            //如果缓存中没有值，就设置为1
            if(value == "null"){
                redisTemplate.opsForValue().set("num", 1);
            }else{
                int num = Integer.parseInt(value);
                redisTemplate.opsForValue().set("num", ++num);
            }
            //问题：在此处可能会发生锁销毁，线程2加锁，删了线程2刚加的锁，导致异常
            //锁的过期时间和业务时间不一致，可能会删除其他线程的锁
            //做完业务之后需要删除锁
            redisTemplate.delete("lock");
        }else{
            //如果没有拿到锁，递归。
            setNum();
        }
    }

    //分布式锁案例3
    //问题：在此处可能会发生锁销毁，线程2加锁，删了线程2刚加的锁，导致异常
    //锁的过期时间和业务时间不一致，可能会删除其他线程的锁
    //解决方案1：时间调大一些
    //解决方案2：放置一个标记，在删锁之前，先确认是否是自己的锁
    //@Override
    public void setNumThree() {
        String token = UUID.randomUUID().toString();
        //利用redis的setnx命令
        Boolean accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token,3, TimeUnit.SECONDS);
        if(accquireLock){
            //拿到了锁，就可以执行业务,如果doBusiness报错，可能导致锁一直占用，无法释放，给一个过期时间
            //如果缓存中有值，就加1
            doBusiness();
            //做完业务之后需要删除锁
            String redisToken = (String) redisTemplate.opsForValue().get("lock");
            if(redisToken.equals(token)){
                redisTemplate.delete("lock");
            }
        }else{
            //如果没有拿到锁，递归。
            setNum();
        }
    }

    //分布式锁案例4
    //问题：使用UUID也会产生问题。线程1执行到if里锁自动删除
    //解决办法：判断和删除必须具备原子性
    //  1.lua脚本
    //  2.redis事务
    //@Override
    public void setNumFour() {
        String token = UUID.randomUUID().toString();
        //利用redis的setnx命令
        Boolean accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token,3, TimeUnit.SECONDS);
        if(accquireLock){
            //如果缓存中有值，就加1
            doBusiness();
            //做完业务之后需要删除锁
//            String redisToken = (String) redisTemplate.opsForValue().get("lock");
//            if(redisToken.equals(token)){
//                //线程1执行到这里锁自动删除
//                redisTemplate.delete("lock");
//            }

            //判断和删除这两句话要具备原子性
            String luaScript="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //把脚本放到redisScript中
            redisScript.setScriptText(luaScript);
            //设置脚本的返回值类型
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), token);

        }else{
            //如果没有拿到锁，递归。
            setNum();
        }
    }


    //分布式锁案例5
    //问题：不具备可重入性，递归的目的是为了拿锁，不是为了递归
    //解决办法：

    //@Override
    public void setNumFive() {
        String token = UUID.randomUUID().toString();
        //利用redis的setnx命令
        Boolean accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token,3, TimeUnit.SECONDS);
        if(accquireLock){
            //如果缓存中有值，就加1
            doBusiness();
            //判断和删除这两句话要具备原子性
            String luaScript="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //把脚本放到redisScript中
            redisScript.setScriptText(luaScript);
            //设置脚本的返回值类型
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), token);

        }else{
            //递归的目的是为了拿锁，不是为了递归
            //自旋
            while (true){
                SleepUtils.millis(50);
                Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
                if(flag){
                    //拿到锁了，结束自旋
                    break;
                }
            }
            setNum();
        }
    }


    //分布式锁优化--具备可重入性
    Map<Thread,String> threadMap = new HashMap<>();
    //@Override
    public void setNumSix() {
        String token = threadMap.get(Thread.currentThread());
        Boolean accquireLock =false;
        if(token!=null){
            //已经拿到锁了，不要再去拿锁了
            accquireLock=true;
        }else {
            token = UUID.randomUUID().toString();
            //利用redis的setnx命令
            accquireLock = redisTemplate.opsForValue().setIfAbsent("lock", token,3, TimeUnit.SECONDS);
        }
        if(accquireLock){
            //如果缓存中有值，就加1
            doBusiness();
            //判断和删除这两句话要具备原子性
            String luaScript="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            //把脚本放到redisScript中
            redisScript.setScriptText(luaScript);
            //设置脚本的返回值类型
            redisScript.setResultType(Long.class);
            redisTemplate.execute(redisScript, Arrays.asList("lock"), token);
            //删除redis线程
            threadMap.remove(Thread.currentThread());
        }else{
            //递归的目的是为了拿锁，不是为了递归
            //自旋
            while (true){
                SleepUtils.millis(50);
                Boolean flag = redisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
                if(flag){
                    threadMap.put(Thread.currentThread(),token);
                    //拿到锁了，结束自旋
                    break;
                }
            }
            setNum();
        }
    }

    //分布式锁优化--具备可重入性
//    Map<Thread,String> threadMap = new HashMap<>();
    @Override
    public void setNum() {
        RLock lock = redissonClient.getLock("lock");
        lock.lock();
        try {
            lock.unlock();
        } finally {

        }
    }

}
