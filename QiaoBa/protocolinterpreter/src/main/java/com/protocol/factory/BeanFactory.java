package com.protocol.factory;

/**
 * Created by wangfei on 2016/12/20.
 */

public interface BeanFactory {

    /**
     * 创建组件中Provider 具体实现类的实例，用户可以自定义
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Object getBean(Class<T> clazz);
}
