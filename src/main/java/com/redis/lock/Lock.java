package com.redis.lock;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 17:03
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */
public interface Lock {

    /**
     * 加锁
     * @param key
     * @param keepAliveTime
     * @return
     */
    boolean lock(String key,long keepAliveTime);

    /**
     * 尝试加锁
     * @param key
     * @param keepAliveTime
     * @return
     */
    boolean tryLock(String key,long keepAliveTime);


    /**
     * 释放锁
     * @param key
     */
    void unlock(String key);
}
