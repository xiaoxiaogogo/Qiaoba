package com.xiaoxiao.qiaoba.interpreter.aop;

import android.util.Log;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import java.lang.reflect.Field;

/**
 * Created by wangfei on 2017/3/2.
 */
@Aspect
public class DIAspect {

    @Pointcut("get(@com.xiaoxiao.qiaoba.annotation.di.DependInsert * *.*) && @annotation(dependInsert)")
    public void pointcutOnDiField(DependInsert dependInsert){

    }

    @Around("pointcutOnDiField(dependInsert)")
    public Object adviceOnDiField(ProceedingJoinPoint joinPoint, DependInsert dependInsert){
        Object obj = null;
        try {
            obj = joinPoint.proceed();
            Log.e("mytest", "obj : " + obj);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        if(obj == null){
            return "fuck" + System.currentTimeMillis();
        }else {
            return "zzz";
        }
    }

}
