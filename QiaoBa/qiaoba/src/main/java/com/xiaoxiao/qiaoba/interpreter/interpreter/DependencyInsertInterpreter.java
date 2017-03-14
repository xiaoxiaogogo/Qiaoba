package com.xiaoxiao.qiaoba.interpreter.interpreter;

import android.text.TextUtils;

import com.xiaoxiao.qiaoba.annotation.di.DependInsert;
import com.xiaoxiao.qiaoba.annotation.model.DependencyInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2017/3/2.
 */

public class DependencyInsertInterpreter {

    public static Map<String, DependencyInfo> dependencyInfoMap = new HashMap<>();


    public void inject(Object obj){
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i=0; i< fields.length; i++){
            fields[i].setAccessible(true);
            if(fields[i].isAnnotationPresent(DependInsert.class)){
                DependInsert insertAnno = fields[i].getAnnotation(DependInsert.class);
                String value = insertAnno.value();
                if(TextUtils.isEmpty(value)){
                    value = fields[i].getName();
                }
                DependencyInfo dependencyInfo = dependencyInfoMap.get(value);
                if(dependencyInfo != null){
                    //判断当前类是否都是当前Field 类型的子类
                    if(dependencyInfo.getDenendencyClazz() == null){
                        //抛出异常
                        return;
                    }
                    if (fields[i].getType().isAssignableFrom(dependencyInfo.getDenendencyClazz())){//当前class是否是括号中class的父类
                        try {
                            fields[i].set(obj, dependencyInfo.getDenendencyClazz().getConstructor().newInstance());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else {
                        //类型异常，抛出异常
                    }

                }else {
                    //打出错误信息（含有注解，但是没有找到它对应的依赖实例）
                }
            }
        }
    }


}
