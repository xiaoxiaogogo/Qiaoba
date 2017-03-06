package com.xiaoxiao.qiaoba.annotation.di;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangfei on 2017/2/28.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Dependency {
    String value() default "";
    boolean isSingleInstance() default false;
}
