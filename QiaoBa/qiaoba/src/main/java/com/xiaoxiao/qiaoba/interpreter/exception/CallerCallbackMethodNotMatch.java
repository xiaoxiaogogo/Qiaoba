package com.xiaoxiao.qiaoba.interpreter.exception;

/**
 * Created by wangfei on 2017/3/6.
 */

public class CallerCallbackMethodNotMatch extends RuntimeException {
    public CallerCallbackMethodNotMatch(){
        super("Please check caller's callback's method name and arguments' type is right!!");
    }

    public CallerCallbackMethodNotMatch(String error){
        super(error);
    }
}
