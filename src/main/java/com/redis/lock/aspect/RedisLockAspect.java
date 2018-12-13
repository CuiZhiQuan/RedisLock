package com.redis.lock.aspect;

import com.redis.lock.Lock;
import com.redis.lock.annotations.RedisFieldLock;
import com.redis.lock.annotations.RedisMethodLock;
import com.redis.lock.annotations.RedisParameterLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * @author cuizhiquan
 * @Description
 * @date 2018/12/13 20:32
 * @Copyright (c) 2017, DaChen All Rights Reserved.
 */
@Aspect
@Component
public class RedisLockAspect {

    @Autowired
    private Lock redisLock;

    @Pointcut("@annotation(com.redis.lock.annotations.RedisMethodLock)")
    public void redisLockPointcut(){

    }

    @Around("redisLockPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        RedisMethodLock redisMethodLock = method.getAnnotation(RedisMethodLock.class);
        String methodKey = redisMethodLock.key();
        long expireTime = redisMethodLock.expireTime();
        String lockedKey = this.getLockedKey(methodKey,method,args);
        boolean success = redisLock.lock(lockedKey,expireTime);
        if(!success){
            throw new Exception(redisMethodLock.msg());
        }
        Object object;
        try {
            //执行业务代码
            object = joinPoint.proceed();
        }finally {
            redisLock.unlock(lockedKey);
        }
        return object;
    }

    /**
     * 获取key
     * @param method
     * @param args
     * @return
     */
    private String getLockedKey(String methodKey,Method method,Object[] args) throws IllegalAccessException{
        StringBuilder sb = new StringBuilder();
        //添加 Method key
        this.addMethodKey(methodKey,sb);
        Annotation[][] annotations = method.getParameterAnnotations();
        //添加 Parameter Key
        this.addParameterKey(annotations,args,sb);
        //TODO 这里是否要做MD5
        return sb.toString();
    }

    /**
     * 添加方法key
     * @param methodKey
     * @param sb
     */
    private void addMethodKey(String methodKey,StringBuilder sb){
        sb.append(methodKey);
    }

    /**
     * 添加 Parameter Key
     * @param annotations
     * @param args
     * @param sb
     */
    private void addParameterKey(Annotation[][] annotations,Object[] args,
                                 StringBuilder sb) throws IllegalAccessException{
        for (int i = 0; i < annotations.length; i++) {
            for (int j = 0; j < annotations[i].length; j++) {
                if (annotations[i][j] instanceof RedisParameterLock) {
                    Object obj = args[i];
                    if(Objects.nonNull(obj)){
                        if(this.isPrimitive(obj)){
                            sb.append(args[i].toString());
                        }else {
                            this.addFieldKey(obj,sb);
                        }
                    }
                }
            }
        }
    }

    /**
     * 添加 Field Key
     * @param arg
     * @param sb
     */
    private void addFieldKey(Object arg,StringBuilder sb) throws IllegalAccessException {
        Field[] fields = arg.getClass().getDeclaredFields();
        for (Field field : fields) {
            Annotation annotation = field.getAnnotation(RedisFieldLock.class);
            if (Objects.isNull(annotation)) {
                continue;
            }
            field.setAccessible(true);
            Object fieldObj = field.get(arg);
            if (Objects.isNull(fieldObj)
                    || fieldObj instanceof Collection
                    || fieldObj instanceof Map
                    || field.getClass().isArray()) {
                continue;
            }
            if (this.isPrimitive(fieldObj)) {
                sb.append(fieldObj.toString());
            } else {
                this.addFieldKey(fieldObj, sb);
            }
        }
    }

    /**
     * 判断是否为基础类型及其封装类
     * @param obj
     * @return
     */
    private boolean isPrimitive(Object obj){
        return obj instanceof String
                || obj instanceof Number
                || obj instanceof Character
                || obj instanceof Boolean
                || obj.getClass().isPrimitive();
    }
}
