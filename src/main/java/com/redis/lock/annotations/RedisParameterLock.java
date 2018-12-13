package com.redis.lock.annotations;

import java.lang.annotation.*;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 20:59
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RedisParameterLock {
}
