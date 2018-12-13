package com.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 16:42
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */
@Component
public class RedisLock implements Lock{

    /**
     * 最大尝试获取锁次数
     */
    private static final int MAX_TRY_LOCK_TIME = 2;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean lock(String key,long keepAliveTime) {
        for(int i = 0;i < MAX_TRY_LOCK_TIME ; i++){
            if(this.tryLock(key,keepAliveTime)){
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean tryLock(String key, long keepAliveTime) {
        long expireTime = Instant.now().getEpochSecond() + keepAliveTime;
        boolean success = stringRedisTemplate.opsForValue().setIfAbsent(key,String.valueOf(expireTime));
        if(success){
            stringRedisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
            return true;
        }
        //当前时间大于过期时间，判断为死锁，删除
        String redisValue = stringRedisTemplate.opsForValue().get(key);
        if (!StringUtils.isEmpty(redisValue)){
            expireTime = Long.valueOf(redisValue);
            if (Instant.now().getEpochSecond() > expireTime) {
                stringRedisTemplate.delete(key);
            }
        }
        return false;
    }

    @Override
    public void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
