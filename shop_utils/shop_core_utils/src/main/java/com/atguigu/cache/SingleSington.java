package com.atguigu.cache;

public class SingleSington {

    public static volatile SingleSington instance;

    private SingleSington(){
    }

    public static SingleSington getInstance() {
        //第一个的作用是判断是否需要加锁
        if(instance==null){
            synchronized (SingleSington.class){
                //第二个的作用是判断是否需要创建对象
                if(instance==null){
                    //volatile防止指令重拍
                    instance=new SingleSington();
                }
            }
        }
        return instance;
    }
}
