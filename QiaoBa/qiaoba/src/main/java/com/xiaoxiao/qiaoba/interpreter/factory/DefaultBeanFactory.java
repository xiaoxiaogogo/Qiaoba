package com.xiaoxiao.qiaoba.interpreter.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangfei on 2016/12/20.
 */

public class DefaultBeanFactory implements BeanFactory {

    private Map<Class, Object> beanMap = new HashMap<>();

    public DefaultBeanFactory putBean(Class clazz, Object obj){
        beanMap.put(clazz, obj);
        return this;
    }

    @Override
    public <T> Object getBean(Class<T> clazz) {
        return beanMap.get(clazz);
    }
}
