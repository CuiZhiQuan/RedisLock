package com.redis.lock.annotations;

import java.lang.annotation.*;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 20:36
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisMethodLock {

    String key();

    long expireTime() default 30;

    String msg() default "获取锁失败";
}
