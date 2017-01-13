package com.xiaoxiao.qiaoba.annotation.communication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangfei on 17/1/6.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CallBack {
    String value();
}
