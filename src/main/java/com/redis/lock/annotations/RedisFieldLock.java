package com.redis.lock.annotations;

import java.lang.annotation.*;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 21:00
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisFieldLock {
}
