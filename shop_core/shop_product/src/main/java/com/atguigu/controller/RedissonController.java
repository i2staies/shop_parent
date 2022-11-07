package com.atguigu.controller;

import com.atguigu.exception.SleepUtils;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RequestMapping("/product/")
@RestController
public class RedissonController {

    @Autowired
    private RedissonClient redissonClient;


    //1.0最简单的获取分布式锁的方式
    //a.默认30s过期     lockWatchdogTimeout = 30*1000
    //b.每隔10s续期     internalLockLeaseTime / 3 也就是我们的看门狗机制
    @GetMapping("/lock")
    public String lock(){
        RLock lock = redissonClient.getLock("lock");
        String uuid = UUID.randomUUID().toString();
        try {
            lock.lock();
            SleepUtils.sleep(50);
            System.out.println(Thread.currentThread().getName()+"执行任务"+uuid);

            //先执行了return，在执行finally
            return Thread.currentThread().getName()+"执行任务"+uuid;
        } finally {
            lock.unlock();
        }
    }

    //1.1
    @GetMapping("/lock2")
    public String lock2(){
        RLock lock = redissonClient.getLock("lock");
        String uuid = UUID.randomUUID().toString();
        try {
            /**
             * waitTime: 尝试加锁 10秒之内拿不到锁就不拿了
             *           自动续期，可重入
             * leaseTime： 上锁之后20秒后自动解锁
             */
            lock.tryLock(10,20, TimeUnit.SECONDS);
            SleepUtils.sleep(50);
            System.out.println(Thread.currentThread().getName()+"执行任务"+uuid);
        }catch (Exception e){
            e.printStackTrace();
        } finally{
            lock.unlock();
        }

        return Thread.currentThread().getName()+"执行任务"+uuid;
    }


    //读写锁--写锁
    @GetMapping("write")
    public String write(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        RLock writeLock = rwLock.writeLock();
        writeLock.lock();
        SleepUtils.sleep(5);
        String uuid = UUID.randomUUID().toString();
        writeLock.unlock();
        return uuid;
    }

    //读写锁--读锁
    @GetMapping("read")
    public String read(){
        RReadWriteLock rwLock = redissonClient.getReadWriteLock("rwLock");
        RLock readLock = rwLock.readLock();
        try {
            readLock.lock();
            String uuid = UUID.randomUUID().toString();
            return uuid;
        } finally {
            readLock.unlock();
        }
    }

    //车位停车Semaphore
    @GetMapping("park")
    public String park() throws Exception {
        RSemaphore parkStation = redissonClient.getSemaphore("park_station");
        //信号量减1
        parkStation.acquire(1);
        return Thread.currentThread().getName()+"找到车位";
    }

    //车离开Semaphore
    @GetMapping("left")
    public String left() throws Exception {
        RSemaphore parkStation = redissonClient.getSemaphore("park_station");
        //信号量加1
        parkStation.release(1);
        return Thread.currentThread().getName()+"left";
    }


    //闭锁 CountDownLatch
    @GetMapping("leftClassRoom")
    public String leftClassRoom() throws Exception {
        RCountDownLatch leftClass = redissonClient.getCountDownLatch("left_class");
        //有人走了 数量减1
        leftClass.countDown();
        return Thread.currentThread().getName()+"学员离开";
    }

    //开锁
    @GetMapping("lockDoor")
    public String lockDoor() throws InterruptedException {
        RCountDownLatch leftClass = redissonClient.getCountDownLatch("left_class");
        //假如班上有6个人
        leftClass.trySetCount(6);
        leftClass.await();;
        SleepUtils.sleep(8);
        return Thread.currentThread().getName()+"班长锁门离开。";
    }



    //公平锁
    @GetMapping("fairLock/{id}")
    public String fairLock(@PathVariable Long id){
        RLock fairLock = redissonClient.getFairLock("rwLock");
        fairLock.lock();
        SleepUtils.sleep(8);
        System.out.println("公平锁-"+id);
        return "success";
    }


    //非公平锁
    @GetMapping("unFairLock/{id}")
    public String unFairLock(@PathVariable Long id) throws Exception {
        RLock unFairLock = redissonClient.getLock("unfair-lock");
        unFairLock.lock();
        SleepUtils.sleep(8);
        System.out.println("非公平锁-"+id);
        unFairLock.unlock();
        return "success";
    }


}
