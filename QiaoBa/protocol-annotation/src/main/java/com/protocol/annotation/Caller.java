package com.protocol.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by wangfei on 2016/12/6.
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Caller {
    String value();
}
