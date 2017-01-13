package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/1/13.
 */

public class AnnotationNotFoundException extends RuntimeException {
    public AnnotationNotFoundException(){
        super("The annotation used on the target can't find!");
    }

    public AnnotationNotFoundException(String msg){
        super(msg);
    }
}
